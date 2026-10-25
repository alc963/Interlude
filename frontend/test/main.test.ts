import { describe, expect, test } from 'vitest';
import { EMAIL_ERROR, PASSWORD_ERROR, USERNAME_ERROR, jwt_expired, validate_email, validate_password, validate_username } from '../src/bridge/validator';

describe("Input Validation", () => {
  test("validate_username", () => {
    expect(validate_username("sumi-123")).toBeNull();
    expect(validate_username("12345678901234567")).toBe(USERNAME_ERROR);
    expect(validate_username("ab")).toBe(USERNAME_ERROR);
    expect(validate_username(123 as unknown as string)).toBe(USERNAME_ERROR);
  });

  test("validate_email", () => {
    expect(validate_email("user@example.com")).toBeNull();
    expect(validate_email("not-an-email@.com")).toBe(EMAIL_ERROR);
    expect(validate_email(null as unknown as string)).toBe(EMAIL_ERROR);
  });

  test("validate_password", () => {
    expect(validate_password("12345678")).toBeNull();
    expect(validate_password("1234567")).toBe(PASSWORD_ERROR);
    expect(validate_password(undefined as unknown as string)).toBe(PASSWORD_ERROR);
  });

  test("jwt_expired", () => {
    expect(jwt_expired(null)).toBe(true);
    expect(jwt_expired("not-a-jwt")).toBe(true);
    expect(jwt_expired("h.eyJleHAiOjF9.s")).toBe(true);
    expect(jwt_expired("h.eyJleHAiOjk5OTk5OTk5OTk5fQ.s")).toBe(false);
    expect(jwt_expired("h.e30.s")).toBe(false);
  });
});
