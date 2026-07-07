import { createApp } from 'vue'
import { createPinia } from 'pinia'
import PrimeVue from 'primevue/config'
import Aura from '@primevue/themes/aura'
import ToastService from 'primevue/toastservice'
import ConfirmationService from 'primevue/confirmationservice'
import 'primeicons/primeicons.css'
import App from './App.vue'
import router from './router'

import Button from 'primevue/button'
import Card from 'primevue/card'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Message from 'primevue/message'
import Checkbox from 'primevue/checkbox'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import DatePicker from 'primevue/datepicker'
import InputNumber from 'primevue/inputnumber'
import Tag from 'primevue/tag'
import Toast from 'primevue/toast'
import ConfirmDialog from 'primevue/confirmdialog'
import Dialog from 'primevue/dialog'
import Menu from 'primevue/menu'
import Paginator from 'primevue/paginator'
import Tabs from 'primevue/tabs'
import TabList from 'primevue/tablist'
import Tab from 'primevue/tab'
import TabPanels from 'primevue/tabpanels'
import TabPanel from 'primevue/tabpanel'
import ProgressSpinner from 'primevue/progressspinner'
import Divider from 'primevue/divider'
import ToggleSwitch from 'primevue/toggleswitch'
import Panel from 'primevue/panel'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Avatar from 'primevue/avatar'
import Badge from 'primevue/badge'
import Skeleton from 'primevue/skeleton'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(PrimeVue, { theme: { preset: Aura } })
app.use(ToastService)
app.use(ConfirmationService)

app.component('Button', Button)
app.component('Card', Card)
app.component('InputText', InputText)
app.component('Password', Password)
app.component('Message', Message)
app.component('Checkbox', Checkbox)
app.component('Select', Select)
app.component('Textarea', Textarea)
app.component('DatePicker', DatePicker)
app.component('InputNumber', InputNumber)
app.component('Tag', Tag)
app.component('Toast', Toast)
app.component('ConfirmDialog', ConfirmDialog)
app.component('Dialog', Dialog)
app.component('Menu', Menu)
app.component('Paginator', Paginator)
app.component('Tabs', Tabs)
app.component('TabList', TabList)
app.component('Tab', Tab)
app.component('TabPanels', TabPanels)
app.component('TabPanel', TabPanel)
app.component('ProgressSpinner', ProgressSpinner)
app.component('Divider', Divider)
app.component('ToggleSwitch', ToggleSwitch)
app.component('Panel', Panel)
app.component('DataTable', DataTable)
app.component('Column', Column)
app.component('Avatar', Avatar)
app.component('Badge', Badge)
app.component('Skeleton', Skeleton)

app.mount('#app')
