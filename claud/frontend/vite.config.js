import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      manifest: {
        name: '지금 붐비나요?',
        short_name: '혼잡도',
        theme_color: '#1A56C4',
        display: 'standalone',
        start_url: '/',
      }
    })
  ],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8083',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})