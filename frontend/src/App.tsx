import { useState } from 'react'
import heroImg from './assets/hero.png'
import reactLogo from './assets/react.svg'
import viteLogo from './assets/vite.svg'
import './App.css'

// just for testing
type DataItem = {
  id: number,
  message: string,
}

function App() {
  const [data, setData] = useState<DataItem[]>([]);
  const [count, setCount] = useState(0);
  const [isLoading, setIsLoading] = useState(false);

  const getData = async () => {
    setIsLoading(true);

    try {
      const response = await fetch('/api/test', {
        method: 'GET',
        headers: {
          Accept: 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
      }

      const result = await response.json();

      console.log('result is:\n', JSON.stringify(result, null, 2));

      setData(result);
    } catch (err) {
      console.log(err);
    } finally {
      setIsLoading(false);
    }
  };

  const submitPost = async () => {
    setIsLoading(true);
    const msg = 'Submitted on count ' + count;

    try {
      const response = await fetch('/api/test?message=' + msg, {
        method: 'POST',
      });

      if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
      }

      const result = await response.json();

      console.log('result is:\n', JSON.stringify(result, null, 2));

    } catch (err) {
      console.log(err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <section id="center">
        <div className="hero">
          <img src={heroImg} className="base" width="170" height="179" alt="" />
          <img src={reactLogo} className="framework" alt="React logo" />
          <img src={viteLogo} className="vite" alt="Vite logo" />
        </div>
        <div>
          <h1>Get started</h1>
          <p>
            Edit <code>src/App.tsx</code> and save to test <code>HMR</code>
          </p>
        </div>
        <button
          type="button"
          className="counter"
          onClick={() => setCount((count) => count + 1)}
        >
          Count is {count}
        </button>
        <button
          type='button'
          className='counter'
          onClick={submitPost}
        >
          Submit
        </button>
        <button
          type="button"
          className='counter'
          onClick={getData}
        >
          Get data
        </button>

        {isLoading && <h2>Loading...</h2>}

        <div>
          <table className='border-collapse border border-gray-400 '>
            <thead>
              <tr>
                <th className='border border-gray-300 p-2 bg-blue-400'>id</th>
                <th className='border border-gray-300 p-2 bg-blue-400'>message</th>
              </tr>
            </thead>
            <tbody>
              {data.map((item) => (
                <tr key={item.id}>
                  <td className='border border-gray-300 p-2 bg-blue-300'>{item.id}</td>
                  <td className='border border-gray-300 p-2 bg-blue-300'>{item.message}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  )
}

export default App
