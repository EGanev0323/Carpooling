<script setup>
import { useRouter } from 'vue-router'
import TripSearchForm from '../components/TripSearchForm.vue'

const router = useRouter()

function handleSearch(params) {
  const query = {}
  if (params.originCity) query.originCity = params.originCity
  if (params.destinationCity) query.destinationCity = params.destinationCity
  if (params.departureDate) query.departureDate = params.departureDate
  if (params.minSeats) query.minSeats = params.minSeats
  router.push({ path: '/trips', query })
}

const steps = [
  {
    icon: 'pi pi-search',
    title: 'Търсете',
    desc: 'Въведете начален и краен град, изберете дата и намерете подходящо пътуване.',
  },
  {
    icon: 'pi pi-check-circle',
    title: 'Резервирайте',
    desc: 'Заявете места и изчакайте потвърждение от шофьора.',
  },
  {
    icon: 'pi pi-car',
    title: 'Пътувайте',
    desc: 'Качете се и насладете се на пътуването. Оценете шофьора след пристигане.',
  },
]
</script>

<template>
  <div class="home-page">
    <!-- Hero Section -->
    <section class="hero">
      <div class="hero-content">
        <h1 class="hero-title">Споделено пътуване</h1>
        <p class="hero-subtitle">
          Пътувайте умно, пестете средства и опазвайте природата.<br />
          Свържете се с шофьори и пътници по вашия маршрут.
        </p>
        <div class="hero-search">
          <TripSearchForm @search="handleSearch" />
        </div>
      </div>
    </section>

    <!-- How it works -->
    <section class="how-it-works">
      <div class="section-inner">
        <h2 class="section-title">Как работи?</h2>
        <p class="section-subtitle">Три лесни стъпки до вашето пътуване</p>
        <div class="steps-grid">
          <div v-for="(step, i) in steps" :key="i" class="step-card">
            <div class="step-number">{{ i + 1 }}</div>
            <div class="step-icon-wrapper">
              <i :class="step.icon" class="step-icon" />
            </div>
            <h3 class="step-title">{{ step.title }}</h3>
            <p class="step-desc">{{ step.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section">
      <div class="cta-inner">
        <h2>Предлагате превози?</h2>
        <p>Регистрирайте се като шофьор и публикувайте своите пътувания.</p>
        <div class="cta-buttons">
          <Button
            label="Регистрирай се"
            icon="pi pi-user-plus"
            size="large"
            @click="$router.push('/register')"
          />
          <Button
            label="Разгледай пътувания"
            icon="pi pi-search"
            size="large"
            outlined
            @click="$router.push('/trips')"
          />
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-page {
  min-height: 100vh;
}

/* Hero */
.hero {
  background: linear-gradient(135deg, var(--p-primary-600, #1d4ed8) 0%, var(--p-primary-400, #3b82f6) 100%);
  padding: 5rem 1.5rem;
  color: white;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
  text-align: center;
}

.hero-title {
  font-size: 3rem;
  font-weight: 800;
  margin-bottom: 1rem;
  text-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.hero-subtitle {
  font-size: 1.15rem;
  opacity: 0.92;
  margin-bottom: 2.5rem;
  line-height: 1.7;
}

.hero-search {
  max-width: 700px;
  margin: 0 auto;
}

/* How it works */
.how-it-works {
  background: var(--p-surface-50);
  padding: 4rem 1.5rem;
}

.section-inner {
  max-width: 900px;
  margin: 0 auto;
  text-align: center;
}

.section-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.section-subtitle {
  color: var(--p-text-muted-color);
  margin-bottom: 2.5rem;
  font-size: 1rem;
}

.steps-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2rem;
}

.step-card {
  background: var(--p-surface-0);
  border: 1px solid var(--p-surface-200);
  border-radius: 12px;
  padding: 2rem 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  position: relative;
}

.step-number {
  position: absolute;
  top: -0.75rem;
  left: 50%;
  transform: translateX(-50%);
  background: var(--p-primary-color);
  color: white;
  width: 1.75rem;
  height: 1.75rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: 700;
}

.step-icon-wrapper {
  background: var(--p-primary-50, #eff6ff);
  border-radius: 50%;
  width: 4rem;
  height: 4rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.step-icon {
  font-size: 1.75rem;
  color: var(--p-primary-color);
}

.step-title {
  font-size: 1.1rem;
  font-weight: 600;
}

.step-desc {
  font-size: 0.875rem;
  color: var(--p-text-muted-color);
  line-height: 1.6;
}

/* CTA */
.cta-section {
  background: var(--p-surface-0);
  padding: 4rem 1.5rem;
}

.cta-inner {
  max-width: 600px;
  margin: 0 auto;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cta-inner h2 {
  font-size: 1.75rem;
  font-weight: 700;
}

.cta-inner p {
  color: var(--p-text-muted-color);
}

.cta-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
  margin-top: 0.5rem;
}

@media (max-width: 640px) {
  .hero-title {
    font-size: 2rem;
  }

  .steps-grid {
    grid-template-columns: 1fr;
  }
}
</style>
