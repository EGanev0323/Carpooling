<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  visible: Boolean,
  trip: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['update:visible', 'confirm', 'cancel'])

const seatsBooked = ref(1)
const message = ref('')

const availableSeats = computed(
  () => (props.trip?.totalSeats ?? 1) - (props.trip?.bookedSeats ?? 0)
)

function handleConfirm() {
  emit('confirm', { seatsBooked: seatsBooked.value, message: message.value })
  resetForm()
}

function handleCancel() {
  emit('cancel')
  emit('update:visible', false)
  resetForm()
}

function resetForm() {
  seatsBooked.value = 1
  message.value = ''
}
</script>

<template>
  <Dialog
    :visible="visible"
    @update:visible="emit('update:visible', $event)"
    header="Резервирай места"
    :style="{ width: '400px' }"
    modal
    :closable="true"
  >
    <div class="book-form">
      <div class="field">
        <label for="seats">Брой места</label>
        <InputNumber
          id="seats"
          v-model="seatsBooked"
          :min="1"
          :max="availableSeats"
          :showButtons="true"
          fluid
        />
        <small class="hint">Свободни: {{ availableSeats }} места</small>
      </div>

      <div class="field">
        <label for="message">Съобщение до шофьора (незадължително)</label>
        <Textarea
          id="message"
          v-model="message"
          :rows="3"
          placeholder="Напишете съобщение..."
          fluid
        />
      </div>
    </div>

    <template #footer>
      <Button label="Отказ" text @click="handleCancel" />
      <Button
        label="Резервирай"
        icon="pi pi-check"
        :disabled="seatsBooked < 1 || seatsBooked > availableSeats"
        @click="handleConfirm"
      />
    </template>
  </Dialog>
</template>

<style scoped>
.book-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0.5rem 0;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.field label {
  font-size: 0.875rem;
  font-weight: 500;
}

.hint {
  color: var(--p-text-muted-color);
  font-size: 0.8rem;
}
</style>
