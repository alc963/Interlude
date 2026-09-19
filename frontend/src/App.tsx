// src/App.tsx
import Hero from './pages/Hero';
import './App.css'; // where styles are

export default function App() {
  return (
    <div className="app-container">
      {/* Navigation Bar */}
      <nav className="navbar">
        <div className="logo">Interlude</div>
        <div className="nav-links">
          <a href="#home">Home</a>
        </div>
      </nav>

      {/* Landing Page Content */}
      <main>
        <Hero />
      </main>

      {/* Footer */}
      <footer className="footer">
        <p>&copy; {new Date().getFullYear()} MyBrand. All rights reserved.</p>
      </footer>
    </div>
  );
}
