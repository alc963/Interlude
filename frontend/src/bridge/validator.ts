export function validate_username(username: string): boolean {
    if(typeof username !== "string") return false;
    const username_regex = /^[a-zA-Z0-9_-]{3,16}$/;
    return username_regex.test(username);
}
export function validate_email(email: string): boolean {
    if(typeof email !== "string") return false;
    const email_regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return email_regex.test(email);
}
export function validate_password(password: string): boolean {
    if(typeof password !== "string") return false;
    return password.length >= 8;
}