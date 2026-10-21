import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
// import { RouterProvider } from 'react-router/dom'
// import { router } from './routes'
import './index.css'
import Playground from './dev/Playground'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    {/* <RouterProvider router={router} /> */}
    <Playground />
  </StrictMode>,
)