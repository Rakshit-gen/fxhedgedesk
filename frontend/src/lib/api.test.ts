import { describe, expect, it } from "vitest";
import { extractErrorMessage } from "./api";

describe("extractErrorMessage", () => {
  it("surfaces the backend's message for an API error response", () => {
    const error = {
      isAxiosError: true,
      response: { data: { status: 409, error: "Conflict", message: "Email already in use" } },
    };

    expect(extractErrorMessage(error)).toBe("Email already in use");
  });

  it("falls back to a generic message when the API error has no body", () => {
    const error = { isAxiosError: true, response: undefined };

    expect(extractErrorMessage(error)).toBe("Something went wrong. Please try again.");
  });

  it("falls back to a generic message for a non-axios error", () => {
    expect(extractErrorMessage(new Error("some unrelated failure"))).toBe(
      "Something went wrong. Please try again."
    );
  });
});
