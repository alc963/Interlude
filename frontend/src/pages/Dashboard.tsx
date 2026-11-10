import { useOutletContext } from "react-router";
import { ApiBridge } from "../bridge/api_bridge";
import type { SpotifyStatus } from "../hooks/useSpotifyStatus";
import TopTracksWidget from "../components/table/TracksWidget";

function Dashboard() {
	const { spotify } = useOutletContext<{ spotify: SpotifyStatus }>();
	return (
		<div>
			<h1 className="mx-5 my-5 text-3xl font-bold">Welcome back!</h1>
			<div className="px-4 sm:px-[10%]">
				{spotify === "disconnected" ? (
					<section className="rounded-2xl border border-line bg-surface p-6 text-center">
						<h2 className="text-xl font-bold">Connect your Spotify</h2>
						<p className="mt-2 text-sm text-muted">
							Link your account so Interlude can see what you listen to.
						</p>
						<button
							onClick={() => ApiBridge.spotify_login()}
							className="btn-primary mt-4"
						>
							Connect Spotify
						</button>
					</section>
				) : (
					<TopTracksWidget />
				)}
			</div>
		</div>
	);
}

export default Dashboard;
