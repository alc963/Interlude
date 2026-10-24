import { Link } from 'react-router'

export default function Hero() {

  return (
    <section className="flex flex-1 flex-col items-center justify-center gap-4 px-6 text-center">
      <h1 className="text-5xl font-bold tracking-tight">Interlude</h1>
      <p className="max-w-md text-muted">Music platform... fill in later</p>
      <Link to="/signup" className="btn-primary px-8">Get Started</Link>
    </section>
  )
}