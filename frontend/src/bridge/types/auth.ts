export interface SignupRequest {
    username: string;
    email: string;
    password: string;
}
export interface AuthTokenResponse {
    accessToken: string;
    tokenType: string;
    refreshToken: string;
}

export type SignupResponse = AuthTokenResponse;

export interface LoginRequest {
    email: string;
    password: string;
}
export type LoginResponse = AuthTokenResponse;

export interface UsernameCheckResponse {
    exists: boolean;
}
