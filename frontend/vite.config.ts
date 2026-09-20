import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    host: 'localhost',
    port: 8081,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8082/api',
        changeOrigin: true,
      },
    },
  },
})