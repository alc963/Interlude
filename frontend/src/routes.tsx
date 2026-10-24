import { createBrowserRouter } from 'react-router'
import Layout from './layout'
import Hero from './pages/Hero'
import Login from './pages/Login'
import Signup from './pages/Signup'
import Dashboard from './pages/Dashboard'
import useAuthenticated from './hooks/useAuthenticated'

function GetHomePage() {
  const { authenticated } = useAuthenticated();
  return authenticated ? <Dashboard /> : <Hero />;
}

export const router = createBrowserRouter([
  {
    path: '/',
    Component: Layout,
    children: [
      { index: true, Component: GetHomePage },
      { path: '/Login', Component: Login },
      { path: '/Signup', Component: Signup },
      { path: '/Dashboard', Component: Dashboard }
    ],
  },
])