// import { Link } from 'react-router'
import TracksWidget from '../components/table/TracksWidget';
import hero from "../assets/hero.png";

export default function Friends() {

  return (
    <main>
      <h1 className="mx-5 my-5 text-3xl font-bold">Your friends</h1>
      <h2 className="mx-5 my-5 text-3xm font-bold">See what your friends have been up to!</h2>
      <div style={{ paddingLeft: '10%', paddingRight: '10%' }}>
        {/* TODO make the widget accept different users to display tracks for */}
        {/* TODO once friend system is implemented, */}
        {/* grab three most recently active friends for the widgets */}
        <TracksWidget trackAmount={3}/>
        <TracksWidget trackAmount={3}/>
        <TracksWidget trackAmount={3}/>
      </div>
      <div className="grid grid-cols-8 gap-2 sm:grid-cols-6">
        {/* TODO: friend profile pictures here, */}
        {/* for now using the assets as placeholder */}
        {/* TODO: fix weird spacing */}
        <img className="max-w-20" src={hero} alt="Profile picture"/>
        <img className="max-w-20" src={hero} alt="Profile picture"/>
        <img className="max-w-20" src={hero} alt="Profile picture"/>
        <img className="max-w-20" src={hero} alt="Profile picture"/>
        <img className="max-w-20" src={hero} alt="Profile picture"/>
        <img className="max-w-20" src={hero} alt="Profile picture"/>
        <img className="max-w-20" src={hero} alt="Profile picture"/>
        <img className="max-w-20" src={hero} alt="Profile picture"/>
        <img className="max-w-20" src={hero} alt="Profile picture"/>
        <img className="max-w-20" src={hero} alt="Profile picture"/>
        <img className="max-w-20" src={hero} alt="Profile picture"/>
        <img className="max-w-20" src={hero} alt="Profile picture"/>
      </div>
    </main>
  )
}
