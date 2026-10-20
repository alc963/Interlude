export interface SignupRequest {
    username: string;
    email: string;
    password: string;
}
export interface SignupResponse {
    token: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}
export interface LoginResponse {
    token: string;
}
