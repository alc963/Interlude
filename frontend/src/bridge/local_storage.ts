const callbacks: ((key: string, value: string|null) => void)[] = [];
export function local_storage_listen(callback: typeof callbacks[0]) {
    callbacks.push(callback);
}
export function local_storage_get(key: string): string|null {
    return localStorage.getItem(key);
}
export function local_storage_set(key: string, value: string){
    localStorage.setItem(key, value);
    callbacks.forEach(c => c(key, value));
}
export function local_storage_remove(key: string){
    localStorage.removeItem(key);
    callbacks.forEach(c => c(key, null));
}