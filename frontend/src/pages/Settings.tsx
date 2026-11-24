import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { ApiBridge } from "../bridge/api_bridge";
import type { MeResponse } from "../bridge/types/auth";

export default function Settings() {
	const navigate = useNavigate();
	const [me, setMe] = useState<MeResponse | null>(null);
	const [error, setError] = useState<string | null>(null);

	useEffect(() => {
		let active = true;
		ApiBridge.me()
			.then((res) => {
				if (!active) return;
				if (res instanceof Response) {
					if (res.status === 401) navigate("/login");
					else setError("Could not load your account");
				} else {
					setMe(res);
				}
			})
			.catch(() => active && setError("Can't reach the server"));
		return () => {
			active = false;
		};
	}, [navigate]);

	return (
		<div className="mx-auto w-full max-w-md px-6 py-10">
			<h1 className="mb-6 text-2xl font-bold">Settings</h1>
			{error && (
				<p
					role="alert"
					className="text-sm text-danger"
				>
					{error}
				</p>
			)}
			{!me && !error && <p className="text-muted">Loading…</p>}
			{me && (
				<dl className="space-y-4">
					<div>
						<dt className="text-xs uppercase tracking-wide text-muted">
							Username
						</dt>
						<dd>{me.username}</dd>
					</div>
					<div>
						<dt className="text-xs uppercase tracking-wide text-muted">
							Email
						</dt>
						<dd>{me.email}</dd>
					</div>
					<div>
						<dt className="text-xs uppercase tracking-wide text-muted">
							Password
						</dt>
						{/* the server only stores a hash of the password rn */}
						<dd>••••••••</dd>
					</div>
				</dl>
			)}
		</div>
	);
}
