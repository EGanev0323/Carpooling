<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'
import type { ChatChannel, ChatMessage } from '@/types'

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()
const toast = useToast()
const chatStore = useChatStore()
const authStore = useAuthStore()

const messageInput = ref('')
const messagesEndRef = ref<HTMLDivElement | null>(null)
const loadingChannels = ref(false)
const loadingMessages = ref(false)
const sidebarOpen = ref(true)

// ──────────────────────────────────────────────
// Active channel
// ──────────────────────────────────────────────
const activeChannelId = computed(() => {
  const paramId = route.params.channelId
  if (paramId) return Number(paramId)
  return chatStore.activeChannelId
})

const activeChannel = computed<ChatChannel | undefined>(() =>
  chatStore.channels.find(c => c.id === activeChannelId.value)
)

const activeMessages = computed<ChatMessage[]>(() =>
  activeChannelId.value !== null
    ? (chatStore.messages[activeChannelId.value] ?? [])
    : []
)

// ──────────────────────────────────────────────
// Helpers
// ──────────────────────────────────────────────
function formatTime(iso: string): string {
  return new Intl.DateTimeFormat(locale.value === 'bg' ? 'bg-BG' : 'en-GB', {
    timeZone: 'Europe/Sofia',
    hour: '2-digit',
    minute: '2-digit',
    day: '2-digit',
    month: 'short',
  }).format(new Date(iso))
}

function isMine(msg: ChatMessage): boolean {
  return msg.sender.id === Number(authStore.user?.id)
}

async function scrollToBottom(): Promise<void> {
  await nextTick()
  messagesEndRef.value?.scrollIntoView({ behavior: 'smooth' })
}

// ──────────────────────────────────────────────
// Channel selection
// ──────────────────────────────────────────────
async function selectChannel(channel: ChatChannel): Promise<void> {
  chatStore.activeChannelId = channel.id
  await router.push({ name: 'ChatChannel', params: { channelId: channel.id } })

  if (!chatStore.messages[channel.id]) {
    loadingMessages.value = true
    try {
      await chatStore.fetchMessages(channel.id)
    } finally {
      loadingMessages.value = false
    }
  }

  chatStore.subscribeToChannel(channel.id)

  // On mobile, switch to the message panel
  sidebarOpen.value = false

  await scrollToBottom()
}

// ──────────────────────────────────────────────
// Send message
// ──────────────────────────────────────────────
async function sendMessage(): Promise<void> {
  const content = messageInput.value.trim()
  if (!content || activeChannelId.value === null) return

  if (!chatStore.connected) {
    toast.add({
      severity: 'warn',
      summary: t('chat.connecting'),
      life: 3000,
    })
    return
  }

  try {
    chatStore.sendMessage(activeChannelId.value, content)
    messageInput.value = ''
    await scrollToBottom()
  } catch (err: unknown) {
    const error = err as { response?: { status: number } }
    if (error.response?.status === 429) {
      toast.add({
        severity: 'warn',
        summary: t('chat.rateLimitError'),
        life: 4000,
      })
    } else {
      toast.add({
        severity: 'error',
        summary: t('auth.errors.generic'),
        life: 4000,
      })
    }
  }
}

function onInputKeydown(event: KeyboardEvent): void {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

// ──────────────────────────────────────────────
// Watchers
// ──────────────────────────────────────────────
watch(
  () => activeMessages.value.length,
  () => {
    scrollToBottom()
  },
)

// ──────────────────────────────────────────────
// Lifecycle
// ──────────────────────────────────────────────
onMounted(async () => {
  loadingChannels.value = true
  try {
    await chatStore.fetchChannels()
    chatStore.connect()

    // If a channelId is in the URL, load it immediately
    if (route.params.channelId) {
      const channelId = Number(route.params.channelId)
      chatStore.activeChannelId = channelId
      loadingMessages.value = true
      try {
        await chatStore.fetchMessages(channelId)
      } finally {
        loadingMessages.value = false
      }
      await scrollToBottom()
      sidebarOpen.value = false
    }
  } catch {
    toast.add({
      severity: 'error',
      summary: t('auth.errors.generic'),
      life: 4000,
    })
  } finally {
    loadingChannels.value = false
  }
})

onUnmounted(() => {
  chatStore.disconnect()
})
</script>

<template>
  <div class="chat-view">
    <!-- Mobile: toggle button -->
    <div class="chat-mobile-topbar">
      <Button
        v-if="!sidebarOpen && activeChannel"
        icon="pi pi-arrow-left"
        text
        :label="t('chat.channel')"
        @click="sidebarOpen = true"
        class="chat-mobile-back"
      />
      <Button
        v-else-if="sidebarOpen"
        icon="pi pi-comments"
        text
        :label="t('chat.title')"
        class="chat-mobile-title"
        :disabled="true"
      />
    </div>

    <div class="chat-layout">
      <!-- ── Sidebar ── -->
      <aside
        class="chat-sidebar"
        :class="{ 'chat-sidebar--visible': sidebarOpen }"
      >
        <div class="chat-sidebar__header">
          <h2 class="chat-sidebar__title">{{ t('chat.title') }}</h2>
          <span
            class="chat-status-dot"
            :class="chatStore.connected ? 'chat-status-dot--connected' : 'chat-status-dot--disconnected'"
            :title="chatStore.connected ? t('chat.connected') : t('chat.connecting')"
          />
        </div>

        <div v-if="loadingChannels" class="chat-sidebar__loading">
          <ProgressSpinner style="width: 32px; height: 32px" />
        </div>

        <p v-else-if="chatStore.channels.length === 0" class="chat-sidebar__empty">
          {{ t('chat.noChannels') }}
        </p>

        <ul v-else class="chat-channel-list">
          <li
            v-for="channel in chatStore.channels"
            :key="channel.id"
            class="chat-channel-item"
            :class="{ 'chat-channel-item--active': channel.id === activeChannelId }"
            @click="selectChannel(channel)"
          >
            <div class="channel-avatar">
              <Avatar
                :image="channel.driver.avatarUrl ?? undefined"
                :label="!channel.driver.avatarUrl ? channel.driver.firstName.charAt(0) : undefined"
                shape="circle"
                size="normal"
              />
            </div>
            <div class="channel-info">
              <div class="channel-route">
                <span class="channel-origin">{{ channel.originCity }}</span>
                <i class="pi pi-arrow-right channel-arrow" />
                <span class="channel-dest">{{ channel.destinationCity }}</span>
              </div>
              <div class="channel-participants">
                {{ channel.driver.firstName }} &amp; {{ channel.passenger.firstName }}
              </div>
            </div>
            <Badge
              v-if="channel.unreadCount > 0"
              :value="channel.unreadCount"
              severity="danger"
              class="channel-unread"
            />
          </li>
        </ul>
      </aside>

      <!-- ── Main chat area ── -->
      <main
        class="chat-main"
        :class="{ 'chat-main--visible': !sidebarOpen || !activeChannel }"
      >
        <!-- Empty state -->
        <div v-if="!activeChannel" class="chat-main__empty">
          <i class="pi pi-comments empty-icon" />
          <p class="empty-text">{{ t('chat.noChannels') }}</p>
        </div>

        <template v-else>
          <!-- Chat header -->
          <div class="chat-main__header">
            <div class="chat-header-route">
              <i class="pi pi-map-marker" />
              <span class="chat-header-origin">{{ activeChannel.originCity }}</span>
              <i class="pi pi-arrow-right" style="font-size: 0.75rem; color: var(--muted)" />
              <span class="chat-header-dest">{{ activeChannel.destinationCity }}</span>
            </div>
            <div
              class="chat-connection-indicator"
              :class="chatStore.connected ? 'chat-connection-indicator--ok' : 'chat-connection-indicator--warn'"
            >
              <i :class="chatStore.connected ? 'pi pi-wifi' : 'pi pi-spin pi-spinner'" style="font-size: 0.8rem" />
              <span>{{ chatStore.connected ? t('chat.connected') : t('chat.connecting') }}</span>
            </div>
          </div>

          <!-- Messages -->
          <div class="chat-messages" role="log" aria-live="polite">
            <div v-if="loadingMessages" class="chat-messages__loading">
              <ProgressSpinner style="width: 32px; height: 32px" />
            </div>

            <template v-else>
              <div v-if="activeMessages.length === 0" class="chat-messages__empty">
                <p>{{ t('chat.typeMessage') }}</p>
              </div>

              <div
                v-for="msg in activeMessages"
                :key="msg.id"
                class="chat-message"
                :class="isMine(msg) ? 'chat-message--mine' : 'chat-message--theirs'"
              >
                <Avatar
                  v-if="!isMine(msg)"
                  :image="msg.sender.avatarUrl ?? undefined"
                  :label="!msg.sender.avatarUrl ? msg.sender.firstName.charAt(0) : undefined"
                  shape="circle"
                  size="normal"
                  class="chat-message__avatar"
                />
                <div class="chat-message__body">
                  <span
                    v-if="!isMine(msg)"
                    class="chat-message__sender"
                  >
                    {{ msg.sender.firstName }} {{ msg.sender.lastName }}
                  </span>
                  <div class="chat-message__bubble">
                    {{ msg.content }}
                  </div>
                  <span class="chat-message__time">{{ formatTime(msg.createdAt) }}</span>
                </div>
              </div>

              <!-- Scroll anchor -->
              <div ref="messagesEndRef" />
            </template>
          </div>

          <!-- Input area -->
          <div class="chat-input-area">
            <Textarea
              v-model="messageInput"
              :placeholder="chatStore.connected ? t('chat.typeMessage') : t('chat.connecting')"
              :disabled="!chatStore.connected"
              rows="2"
              autoResize
              class="chat-input"
              @keydown="onInputKeydown"
            />
            <Button
              icon="pi pi-send"
              :label="t('chat.send')"
              :disabled="!chatStore.connected || !messageInput.trim()"
              @click="sendMessage"
              class="chat-send-btn"
            />
          </div>
        </template>
      </main>
    </div>
  </div>
</template>

<style scoped>
.chat-view {
  height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Mobile top bar */
.chat-mobile-topbar {
  display: none;
  align-items: center;
  padding: var(--spacing-2) var(--spacing-4);
  border-bottom: 1px solid var(--rule);
  background: var(--p-surface-0);
}

.chat-mobile-back,
.chat-mobile-title {
  flex-shrink: 0;
}

/* Layout: sidebar + main */
.chat-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  flex: 1;
  overflow: hidden;
}

/* ── Sidebar ── */
.chat-sidebar {
  border-right: 1px solid var(--rule);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--p-surface-0);
}

.chat-sidebar__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-4) var(--spacing-4) var(--spacing-3);
  border-bottom: 1px solid var(--rule);
  flex-shrink: 0;
}

.chat-sidebar__title {
  font-size: var(--fs-body);
  font-weight: 700;
  margin: 0;
}

.chat-status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
  transition: background 0.3s;
}

.chat-status-dot--connected {
  background: #22c55e;
}

.chat-status-dot--disconnected {
  background: #f59e0b;
}

.chat-sidebar__loading {
  display: flex;
  justify-content: center;
  align-items: center;
  flex: 1;
}

.chat-sidebar__empty {
  color: var(--muted);
  font-size: var(--fs-small);
  text-align: center;
  padding: var(--spacing-6) var(--spacing-4);
  margin: 0;
}

.chat-channel-list {
  list-style: none;
  margin: 0;
  padding: var(--spacing-2) 0;
  overflow-y: auto;
  flex: 1;
}

.chat-channel-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  padding: var(--spacing-3) var(--spacing-4);
  cursor: pointer;
  transition: background 0.15s;
  position: relative;
}

.chat-channel-item:hover {
  background: var(--p-surface-100, rgba(0, 0, 0, 0.04));
}

.chat-channel-item--active {
  background: var(--p-primary-50, rgba(63, 124, 172, 0.08));
}

.channel-avatar {
  flex-shrink: 0;
}

.channel-info {
  flex: 1;
  min-width: 0;
}

.channel-route {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
  font-weight: 600;
  font-size: var(--fs-small);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.channel-arrow {
  font-size: 0.7rem;
  color: var(--muted);
  flex-shrink: 0;
}

.channel-participants {
  font-size: 0.75rem;
  color: var(--muted);
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.channel-unread {
  flex-shrink: 0;
}

/* ── Main area ── */
.chat-main {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--p-surface-50, var(--p-surface-0));
}

.chat-main__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  gap: var(--spacing-4);
  color: var(--muted);
}

.empty-icon {
  font-size: 3rem;
}

.empty-text {
  font-size: var(--fs-body);
  margin: 0;
}

.chat-main__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-3) var(--spacing-5);
  border-bottom: 1px solid var(--rule);
  background: var(--p-surface-0);
  flex-shrink: 0;
}

.chat-header-route {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  font-weight: 600;
}

.chat-header-origin {
  color: var(--mark);
}

.chat-connection-indicator {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
  font-size: 0.75rem;
  padding: 2px var(--spacing-2);
  border-radius: 12px;
}

.chat-connection-indicator--ok {
  color: #22c55e;
  background: rgba(34, 197, 94, 0.08);
}

.chat-connection-indicator--warn {
  color: #f59e0b;
  background: rgba(245, 158, 11, 0.08);
}

/* ── Messages ── */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-4) var(--spacing-5);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-3);
}

.chat-messages__loading {
  display: flex;
  justify-content: center;
  align-items: center;
  flex: 1;
}

.chat-messages__empty {
  color: var(--muted);
  font-size: var(--fs-small);
  text-align: center;
  margin: auto;
}

.chat-message {
  display: flex;
  align-items: flex-end;
  gap: var(--spacing-2);
  max-width: 75%;
}

.chat-message--mine {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.chat-message--theirs {
  align-self: flex-start;
}

.chat-message__avatar {
  flex-shrink: 0;
}

.chat-message__body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.chat-message--mine .chat-message__body {
  align-items: flex-end;
}

.chat-message--theirs .chat-message__body {
  align-items: flex-start;
}

.chat-message__sender {
  font-size: 0.7rem;
  color: var(--muted);
  font-weight: 500;
}

.chat-message__bubble {
  padding: var(--spacing-2) var(--spacing-3);
  border-radius: 16px;
  font-size: var(--fs-body);
  line-height: 1.4;
  word-break: break-word;
  white-space: pre-wrap;
}

.chat-message--mine .chat-message__bubble {
  background: var(--mark, #3f7cac);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.chat-message--theirs .chat-message__bubble {
  background: var(--p-surface-200, #e5e7eb);
  color: var(--text);
  border-bottom-left-radius: 4px;
}

.chat-message__time {
  font-size: 0.7rem;
  color: var(--muted);
}

/* ── Input area ── */
.chat-input-area {
  display: flex;
  gap: var(--spacing-2);
  padding: var(--spacing-3) var(--spacing-5);
  border-top: 1px solid var(--rule);
  background: var(--p-surface-0);
  align-items: flex-end;
  flex-shrink: 0;
}

.chat-input {
  flex: 1;
  resize: none;
}

.chat-send-btn {
  flex-shrink: 0;
}

/* ── Mobile ── */
@media (max-width: 768px) {
  .chat-mobile-topbar {
    display: flex;
  }

  .chat-layout {
    grid-template-columns: 1fr;
  }

  .chat-sidebar {
    display: none;
  }

  .chat-sidebar--visible {
    display: flex;
  }

  .chat-main {
    display: none;
  }

  .chat-main--visible {
    display: flex;
  }
}
</style>
