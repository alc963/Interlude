import { createBrowserRouter } from 'react-router'
import Layout from './layout'
import Hero from './pages/Hero'

export const router = createBrowserRouter([
  {
    path: '/',
    Component: Layout,
    children: [
      { index: true, Component: Hero },
    ],
  },
])