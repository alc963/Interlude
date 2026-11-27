import { local_storage_get, local_storage_remove, local_storage_set } from "./local_storage";
import type { AuthTokenResponse, LoginRequest, LoginResponse, MeResponse, SignupRequest, SignupResponse, UsernameCheckResponse } from "./types/auth";
import { jwt_expired } from "./validator";
import type { CurrentlyPlayingResponse, RecentlyPlayedRequest, RecentlyPlayedResponse } from "./types/spotify";
import type { TopArtistSummaryResponse } from "./types/interlude";
// import { mockTopArtistSummary } from "./mock_data";

type ApiResponse<T> = Promise<T | Response>;

function clean_request(request: Record<string, unknown>): Record<string, string> {
    return Object.fromEntries(
        Object.entries(request)
            .filter(([, value]) => value !== undefined)
            .map(([key, value]) => [key, String(value)])
    );
}

export const JWT_LOCAL_STORAGE_KEY: string = "JWT";
export const REFRESH_TOKEN_LOCAL_STORAGE_KEY: string = "refreshToken";
export const BASE_URL: string =
    window.location.hostname === "interlude.sumii.me"
        ? "https://iapi.sumii.me"
        : "http://127.0.0.1:8082";

function clear_auth() {
    ApiBridge.jwt = null;
    local_storage_remove(JWT_LOCAL_STORAGE_KEY);
    local_storage_remove(REFRESH_TOKEN_LOCAL_STORAGE_KEY);
};

export const ApiBridge = {
    jwt: null as string | null,

    // returns true if JWT was loaded or refreshed
    async initialize(): Promise<boolean> {
        const local_jwt = local_storage_get(JWT_LOCAL_STORAGE_KEY);
        if (local_jwt && !jwt_expired(local_jwt)) {
            ApiBridge.jwt = local_jwt;
            return true;
        }

        return await ApiBridge.refresh_if_needed();
    },

    async refresh_if_needed(force = false): Promise<boolean> {
        const stored_jwt = local_storage_get(JWT_LOCAL_STORAGE_KEY);
        if (!force && stored_jwt && !jwt_expired(stored_jwt)) {
            ApiBridge.jwt = stored_jwt;
            return true;
        }

        const refresh_token = local_storage_get(REFRESH_TOKEN_LOCAL_STORAGE_KEY);
        if (!refresh_token) {
            clear_auth();
            return false;
        }

        try {
            const response = await fetch(`${BASE_URL}/auth/refreshtoken`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ refreshToken: refresh_token }),
            });

            if (!response.ok) {
                clear_auth();
                return false;
            }

            const tokens = await response.json() as AuthTokenResponse;
            local_storage_set(JWT_LOCAL_STORAGE_KEY, tokens.accessToken);
            local_storage_set(REFRESH_TOKEN_LOCAL_STORAGE_KEY, tokens.refreshToken);
            ApiBridge.jwt = tokens.accessToken;
            return true;
        } catch {
            clear_auth();
            return false;
        }
    },

    async get<Res>(path: string, params: Record<string, string> = {}, require_auth = true): ApiResponse<Res> {
        const query = new URLSearchParams(params).toString();
        const fetch_if_authenticated = async () => fetch(`${BASE_URL}${path}${query ? `?${query}` : ""}`, {
            headers: ApiBridge.auth_headers(),
            credentials: "include"
        });

        if (require_auth) {
            const refreshed = await ApiBridge.refresh_if_needed();
            if (!refreshed && ApiBridge.jwt === null) {
                return new Response(null, { status: 401, statusText: "Unauthorized" });
            }
        }

        let response = await fetch_if_authenticated();
        if (require_auth && response.status === 401) {
            const refreshed = await ApiBridge.refresh_if_needed(true);
            if (refreshed) {
                response = await fetch_if_authenticated();
            }
        }

        if (!response.ok || response.status === 204) return response;
        return await response.json();
    },

    async post<Req, Res>(path: string, body: Req, only_text = false, require_auth = true): ApiResponse<Res> {
        const fetch_if_authenticated = async () => fetch(`${BASE_URL}${path}`, {
            method: "POST",
            headers: { "Content-Type": "application/json", ...ApiBridge.auth_headers() },
            body: JSON.stringify(body),
        });

        if (require_auth) {
            const refreshed = await ApiBridge.refresh_if_needed();
            if (!refreshed && ApiBridge.jwt === null) {
                return new Response(null, { status: 401, statusText: "Unauthorized" });
            }
        }

        let response = await fetch_if_authenticated();
        if (require_auth && response.status === 401) {
            const refreshed = await ApiBridge.refresh_if_needed(true);
            if (refreshed) {
                response = await fetch_if_authenticated();
            }
        }

        if (!response.ok) return response;
        if (only_text) return await response.text() as Res;
        return await response.json();
    },

    async post_token<Req>(path: string, body: Req): Promise<AuthTokenResponse | Response> {
        const response = await fetch(`${BASE_URL}${path}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body),
        });

        if (!response.ok) return response;

        const tokens = await response.json() as AuthTokenResponse;
        local_storage_set(JWT_LOCAL_STORAGE_KEY, tokens.accessToken);
        local_storage_set(REFRESH_TOKEN_LOCAL_STORAGE_KEY, tokens.refreshToken);
        ApiBridge.jwt = tokens.accessToken;
        return tokens;
    },

    async signup(request: SignupRequest): ApiResponse<SignupResponse> {
        return await ApiBridge.post_token("/auth/signup", request);
    },
    async login(request: LoginRequest): ApiResponse<LoginResponse> {
        return await ApiBridge.post_token("/auth/login", request);
    },

    async logout(): Promise<void> {
        const refresh_token = local_storage_get(REFRESH_TOKEN_LOCAL_STORAGE_KEY);
        // always clear locally, even if the server call fails
        try {
            if (refresh_token) {
                await fetch(`${BASE_URL}/auth/logout`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ refreshToken: refresh_token }),
                });
            }
        } catch {
            // ignore network errors
        } finally {
            clear_auth();
        }
    },

    me(): ApiResponse<MeResponse> {
        return ApiBridge.get<MeResponse>("/me");
    },

    async username_check(username: string): ApiResponse<UsernameCheckResponse> {
        return await ApiBridge.get("/auth/username-check", { username }, false);
    },

    auth_headers(): Record<string, string> {
        return ApiBridge.jwt ? { Authorization: `Bearer ${ApiBridge.jwt}` } : {};
    },

    spotify_login_url() {
        const query = new URLSearchParams({ token: ApiBridge.jwt ?? "" }).toString();
        return `${BASE_URL}/auth/spotify/login?${query}`;
    },

    spotify_login() {
        window.location.assign(ApiBridge.spotify_login_url());
    },

    spotify_currently_playing() {
        return ApiBridge.get<CurrentlyPlayingResponse>("/spotify/currently-playing");
    },

    spotify_recently_played(request: RecentlyPlayedRequest = {}) {
        return ApiBridge.get<RecentlyPlayedResponse>("/spotify/recently-played", clean_request(request));
    },

    async top_artists_summary(): ApiResponse<TopArtistSummaryResponse> {
        return ApiBridge.get<TopArtistSummaryResponse>("/me/top/artists/summary");
        // return mockTopArtistSummary;
    },
};
