export type ValidationError = string | null;

export const USERNAME_ERROR = "Username must be 3-16 characters: letters, numbers, _ or -";
export const EMAIL_ERROR = "Enter a valid email address";
export const PASSWORD_ERROR = "Password must be at least 8 characters";

export function validate_username(username: string): ValidationError {
    if(typeof username !== "string") return USERNAME_ERROR;
    const username_regex = /^[a-zA-Z0-9_-]{3,16}$/;
    return username_regex.test(username) ? null : USERNAME_ERROR;
}
export function validate_email(email: string): ValidationError {
    if(typeof email !== "string") return EMAIL_ERROR;
    const email_regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return email_regex.test(email) ? null : EMAIL_ERROR;
}
export function validate_password(password: string): ValidationError {
    if(typeof password !== "string") return PASSWORD_ERROR;
    return password.length >= 8 ? null : PASSWORD_ERROR;
}

export function jwt_expired(token: string | null): boolean {
    try {
        if(token === null) return true;
        const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
        return typeof payload.exp === "number" && payload.exp * 1000 <= Date.now();
    } catch {
        return true;
    }
}