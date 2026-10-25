import { createBrowserRouter } from 'react-router'
import useAuthenticated from './hooks/useAuthenticated'
/* pages and layout */
import Layout from './layout'
import Hero from './pages/Hero'
import Login from './pages/Login'
import Signup from './pages/Signup'
import Dashboard from './pages/Dashboard'
// import Connect from './pages/Connect'



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
