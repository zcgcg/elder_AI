<template>
  <router-view />
  <server-settings v-if="isNativeApp && route.path !== '/login'" floating />
</template>

<script setup>
import { useRoute } from 'vue-router'
import ServerSettings from './components/ServerSettings.vue'
import { isNativeApp } from './config/runtime.js'

const route = useRoute()
</script>
