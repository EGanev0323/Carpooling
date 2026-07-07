import { defineStore } from 'pinia'
import { ref } from 'vue'
import { Client, type IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { chatApi } from '@/shared/api/chat'
import type { ChatChannel, ChatMessage } from '@/types'

export const useChatStore = defineStore('chat', () => {
  const channels = ref<ChatChannel[]>([])
  const activeChannelId = ref<number | null>(null)
  const messages = ref<Record<number, ChatMessage[]>>({})
  const connected = ref(false)
  let stompClient: Client | null = null

  /** Loads all chat channels for the current user. */
  async function fetchChannels(): Promise<void> {
    const { data } = await chatApi.getChannels()
    channels.value = data
  }

  /** Loads paginated messages for the given channel and appends to store. */
  async function fetchMessages(channelId: number, page = 0): Promise<void> {
    const { data } = await chatApi.getMessages(channelId, page)
    if (page === 0) {
      messages.value[channelId] = data.content
    } else {
      const existing = messages.value[channelId] ?? []
      messages.value[channelId] = [...data.content, ...existing]
    }
  }

  /** Opens the STOMP connection and subscribes to all known channels. */
  function connect(): void {
    if (stompClient?.active) return

    stompClient = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        connected.value = true
        channels.value.forEach(ch => {
          stompClient!.subscribe(`/topic/chat.${ch.id}`, (msg: IMessage) => {
            const message: ChatMessage = JSON.parse(msg.body) as ChatMessage
            if (!messages.value[ch.id]) messages.value[ch.id] = []
            messages.value[ch.id].push(message)
          })
        })
      },
      onDisconnect: () => {
        connected.value = false
      },
      onStompError: () => {
        connected.value = false
      },
    })

    stompClient.activate()
  }

  /** Closes the STOMP connection. */
  function disconnect(): void {
    stompClient?.deactivate()
    connected.value = false
  }

  /** Publishes a message to the given channel via STOMP. */
  function sendMessage(channelId: number, content: string): void {
    if (!stompClient?.connected) return
    stompClient.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({ channelId, content }),
    })
  }

  /** Subscribes to a single channel's topic (used when entering a new channel). */
  function subscribeToChannel(channelId: number): void {
    if (!stompClient?.connected) return
    stompClient.subscribe(`/topic/chat.${channelId}`, (msg: IMessage) => {
      const message: ChatMessage = JSON.parse(msg.body) as ChatMessage
      if (!messages.value[channelId]) messages.value[channelId] = []
      messages.value[channelId].push(message)
    })
  }

  return {
    channels,
    activeChannelId,
    messages,
    connected,
    fetchChannels,
    fetchMessages,
    connect,
    disconnect,
    sendMessage,
    subscribeToChannel,
  }
})
