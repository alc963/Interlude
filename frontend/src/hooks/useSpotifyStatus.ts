import { useEffect, useState } from "react";
import { useLocation } from "react-router";
import { ApiBridge } from "../bridge/api_bridge";

// 'unknown' = still checking, or the check failed for some other reason
export type SpotifyStatus = "unknown" | "connected" | "disconnected";

// currently-playing track: 401 = not connected, 200/204 = connected.
export default function useSpotifyStatus(enabled: boolean): SpotifyStatus {
	const [status, setStatus] = useState<SpotifyStatus>("unknown");
	const { search } = useLocation();

	useEffect(() => {
		if (!enabled) return;
		let cancelled = false;
		ApiBridge.spotify_currently_playing()
			.then((res) => {
				if (cancelled) return;
				if (!(res instanceof Response)) setStatus("connected");
				else if (res.status === 401) setStatus("disconnected");
				else if (res.status === 204) setStatus("connected");
				else setStatus("unknown");
			})
			.catch(() => !cancelled && setStatus("unknown"));
		return () => {
			cancelled = true;
		};
	}, [enabled, search]);

	return enabled ? status : "unknown";
}
