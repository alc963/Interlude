import type { LoginRequest, LoginResponse, SignupRequest, SignupResponse, UsernameCheckResponse } from "./types/auth";
import { jwt_expired } from "./validator";

type ApiResponse<T> = Promise<T|Response>;

const JWT_LOCAL_STORAGE_KEY: string = "JWT";
const BASE_URL: string =
    window.location.hostname === "interlude.sumii.me"
        ? "https://iapi.sumii.me"
        : "http://localhost:8082";

export const ApiBridge = {
    jwt: null as string | null,

    // returns true if JWT was loaded
    initialize() {
        const local_jwt = localStorage.getItem(JWT_LOCAL_STORAGE_KEY);
        if(!jwt_expired(local_jwt)) {
            ApiBridge.jwt = local_jwt;
            return true;
        }
        else {
            localStorage.removeItem(JWT_LOCAL_STORAGE_KEY);
            return false;
        }
    },

    async get<Res>(path: string, params: Record<string, string> = {}): ApiResponse<Res> {
        const query = new URLSearchParams(params).toString();
        const response = await fetch(`${BASE_URL}${path}${query ? `?${query}` : ""}`, {
            headers: ApiBridge.auth_headers(),
        });
        if (!response.ok) return response;
        return await response.json();
    },

    async post<Req, Res>(path: string, body: Req, only_text = false): ApiResponse<Res> {
        const response = await fetch(`${BASE_URL}${path}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body),
        });
        if (!response.ok) return response;
        if(only_text) return await response.text() as Res;
        return await response.json();
    },

    async post_token<Req>(path: string, body: Req): Promise<{ token: string } | Response> {
        const token = await ApiBridge.post<Req, string>(path, body, true);
        if(typeof token !== "string") return token;
        localStorage.setItem(JWT_LOCAL_STORAGE_KEY, token);
        ApiBridge.jwt = token;
        
        return { token };
    },

    async signup(request: SignupRequest): ApiResponse<SignupResponse> {
        return await ApiBridge.post_token("/auth/signup", request);
    },
    async login(request: LoginRequest): ApiResponse<LoginResponse> {
        return await ApiBridge.post_token("/auth/login", request);
    },

    async username_check(username: string): ApiResponse<UsernameCheckResponse> {
        return await ApiBridge.get("/auth/username-check", { username });
    },

    auth_headers(): Record<string, string> {
        return ApiBridge.jwt ? { Authorization: `Bearer ${ApiBridge.jwt}` } : {};
    },
};
