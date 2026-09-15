import { describe, expect, it, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import BookExposureForm from "./BookExposureForm";

const { postMock, getMock } = vi.hoisted(() => ({
  postMock: vi.fn().mockResolvedValue({ data: {} }),
  getMock: vi.fn().mockResolvedValue({ data: [] }),
}));

vi.mock("@/lib/api", async () => {
  const actual = await vi.importActual<typeof import("@/lib/api")>("@/lib/api");
  return {
    ...actual,
    api: { post: postMock, get: getMock },
  };
});

function renderForm() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <BookExposureForm />
    </QueryClientProvider>
  );
}

describe("BookExposureForm", () => {
  beforeEach(() => {
    postMock.mockClear();
  });

  it("rejects submission with no amount without calling the API", () => {
    renderForm();

    fireEvent.change(screen.getByLabelText(/Description/i), { target: { value: "Q2 invoice" } });
    fireEvent.click(screen.getByRole("button", { name: /Book exposure/i }));

    expect(screen.getByText(/Enter an amount above zero/i)).toBeInTheDocument();
    expect(postMock).not.toHaveBeenCalled();
  });

  it("rejects submission with a blank description without calling the API", () => {
    renderForm();

    fireEvent.change(screen.getByLabelText(/Amount/i), { target: { value: "5000" } });
    fireEvent.click(screen.getByRole("button", { name: /Book exposure/i }));

    expect(screen.getByText(/Give it a short description/i)).toBeInTheDocument();
    expect(postMock).not.toHaveBeenCalled();
  });

  it("submits the booking payload once amount and description are valid", async () => {
    renderForm();

    fireEvent.change(screen.getByLabelText(/Amount/i), { target: { value: "5000" } });
    fireEvent.change(screen.getByLabelText(/Description/i), { target: { value: "Q2 invoice" } });
    fireEvent.click(screen.getByRole("button", { name: /Book exposure/i }));

    await waitFor(() =>
      expect(postMock).toHaveBeenCalledWith(
        "/api/exposures",
        expect.objectContaining({ amount: 5000, description: "Q2 invoice", direction: "RECEIVABLE" })
      )
    );
  });
});
