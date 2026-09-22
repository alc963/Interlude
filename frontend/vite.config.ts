/* eslint-disable @typescript-eslint/no-unused-vars */
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/api': {
        target: 'https://localhost:8082',
        changeOrigin: true,
        secure: false,
        ws: true,
        configure: (proxy, _options) => {
          proxy.on('error', (err, _req, _res) => {
            console.log('proxy error', err);
          });
          proxy.on('proxyReq', (_proxyReq, req, _res) => {
            console.log('Sending Request to the Target:', req.method, req.url);
          });
          proxy.on('proxyRes', (proxyRes, req, _res) => {
            console.log('Received Response from the Target:', proxyRes.statusCode, req.url);
          });
        },
      }
    }
  }
  // server: {
  //   host: 'localhost',
  //   port: 8081,
  //   strictPort: true,
  //   proxy: {
  //     '/api': {
  //       target: 'http://localhost:8082/',
  //       changeOrigin: true,
  //     },
  //   },
  // },
})