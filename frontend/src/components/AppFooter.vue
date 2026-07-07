<script setup>
const year = new Date().getFullYear()
</script>

<template>
  <footer class="app-footer">
    <div class="footer-inner">
      <span>
        <i class="pi pi-car" />
        Споделено пътуване &copy; {{ year }}
      </span>
      <span class="footer-tagline">Пътувай умно, пътувай заедно</span>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  background: var(--p-surface-100);
  border-top: 1px solid var(--p-surface-200);
  padding: 1.25rem 1.5rem;
  margin-top: auto;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--p-text-muted-color);
  font-size: 0.875rem;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.footer-tagline {
  font-style: italic;
}
</style>
