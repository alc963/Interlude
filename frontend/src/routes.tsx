import { createBrowserRouter, RouterProvider } from 'react-router'
import useAuthenticated from './hooks/useAuthenticated'
/* pages and layout */
import Layout from './layout'
import Hero from './pages/Hero'
import Login from './pages/Login'
import Signup from './pages/Signup'
import Dashboard from './pages/Dashboard'
import Connect from './pages/Connect'
// import Connect from './pages/Connect'



function GetHomePage() {
  const { authenticated } = useAuthenticated();
  return authenticated ? <Dashboard /> : <Hero />;
}

const router = createBrowserRouter([
  {
    path: '/',
    Component: Layout,
    children: [
      { index: true, Component: GetHomePage },
      { path: '/login', Component: Login },
      { path: '/signup', Component: Signup },
      { path: '/dashboard', Component: Dashboard },
      { path: '/connect', Component: Connect }
    ],
  },
])

export function AppRouter() {
  return <RouterProvider router={router} />;
}
