import { createBrowserRouter } from 'react-router'
import Layout from './layout'
import Hero from './pages/Hero'
import Login from './pages/Login'
import Signup from './pages/Signup'

export const router = createBrowserRouter([
  {
    path: '/',
    Component: Layout,
    children: [
      { index: true, Component: Hero },
      { path: '/login', Component: Login },
      { path: '/signup', Component: Signup },
    ],
  },
])