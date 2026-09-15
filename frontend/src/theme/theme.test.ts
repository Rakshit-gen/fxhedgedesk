import { describe, expect, it } from "vitest";
import { pnlColor } from "./theme";

describe("pnlColor", () => {
  it("is green for a gain", () => {
    expect(pnlColor(125.5)).toBe("#3ED598");
  });

  it("is red for a loss", () => {
    expect(pnlColor(-40)).toBe("#F2545B");
  });

  it("is neutral for exactly zero", () => {
    expect(pnlColor(0)).toBe("rgba(255,255,255,0.6)");
  });
});
