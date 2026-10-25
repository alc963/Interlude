import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { ApiBridge } from './bridge/api_bridge'
import AppRouter from './routes' 
import './index.css'

ApiBridge.initialize()

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AppRouter />
  </StrictMode>,
)
