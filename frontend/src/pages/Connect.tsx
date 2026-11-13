import { ApiBridge } from '../bridge/api_bridge'

export default function Connect() {
    return (
    <div className="flex flex-1 items-center justify-center px-6">
      <div className="w-full max-w-sm space-y-4">
        <h1 className="text-2xl font-bold">Connect Spotify</h1>
        {/* moves the browser to Spotify and back */}
        <a href={ApiBridge.spotify_login_url()} className="btn-primary block w-full">
          Connect Spotify
        </a>
      </div>
    </div>
  )
}