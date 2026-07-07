<script setup>
import { ref } from 'vue'
import { tripsApi } from '../api/trips.api'
import { useToast } from 'primevue/usetoast'

const props = defineProps({
  tripId: {
    type: [Number, String],
    required: true,
  },
  ratedUser: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['submitted'])
const toast = useToast()

const score = ref(5)
const comment = ref('')
const submitting = ref(false)

const stars = [1, 2, 3, 4, 5]

async function handleSubmit() {
  if (!score.value) return
  submitting.value = true
  try {
    await tripsApi.submitRating(props.tripId, {
      ratedUserId: props.ratedUser?.id,
      score: score.value,
      comment: comment.value,
    })
    toast.add({
      severity: 'success',
      summary: 'Успех',
      detail: 'Оценката е изпратена',
      life: 3000,
    })
    emit('submitted')
    comment.value = ''
    score.value = 5
  } catch {
    toast.add({
      severity: 'error',
      summary: 'Грешка',
      detail: 'Неуспешно изпращане на оценка',
      life: 3000,
    })
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="rating-form">
    <h4 v-if="ratedUser" class="rating-title">
      Оцени {{ ratedUser.firstName || ratedUser.username }}
    </h4>

    <div class="star-input">
      <button
        v-for="s in stars"
        :key="s"
        type="button"
        class="star-btn"
        @click="score = s"
      >
        <i :class="s <= score ? 'pi pi-star-fill' : 'pi pi-star'" />
      </button>
    </div>

    <Textarea
      v-model="comment"
      :rows="3"
      placeholder="Добавете коментар (незадължително)..."
      fluid
    />

    <Button
      label="Изпрати оценка"
      icon="pi pi-send"
      :loading="submitting"
      :disabled="!score"
      @click="handleSubmit"
    />
  </div>
</template>

<style scoped>
.rating-form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.rating-title {
  font-size: 0.95rem;
  font-weight: 600;
}

.star-input {
  display: flex;
  gap: 0.25rem;
}

.star-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.25rem;
  font-size: 1.5rem;
  color: var(--p-yellow-500);
  transition: transform 0.1s;
}

.star-btn:hover {
  transform: scale(1.2);
}
</style>
