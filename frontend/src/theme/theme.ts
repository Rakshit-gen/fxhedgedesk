import { createTheme } from "@mui/material/styles";

// A trading-desk palette instead of LendMesh's indigo, graphite and amber,
// the colors of a terminal you'd actually watch a rate tick on. Green and
// red carry the one signal that matters everywhere in this app: P&L.
export const theme = createTheme({
  palette: {
    mode: "dark",
    primary: {
      main: "#3EC6E0",
      light: "#7DDCEF",
      dark: "#2694AB",
    },
    secondary: {
      main: "#F0B429",
    },
    background: {
      default: "#0A0E14",
      paper: "#12161F",
    },
    success: {
      main: "#3ED598",
    },
    warning: {
      main: "#F0B429",
    },
    error: {
      main: "#F2545B",
    },
    divider: "rgba(255,255,255,0.08)",
  },
  shape: {
    borderRadius: 14,
  },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica Neue", Arial, sans-serif',
    h1: { fontWeight: 700 },
    h2: { fontWeight: 700 },
    h3: { fontWeight: 700 },
    h4: { fontWeight: 600 },
    h5: { fontWeight: 600 },
    h6: { fontWeight: 600 },
    button: { fontWeight: 600, textTransform: "none" },
  },
  components: {
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: "none",
          backdropFilter: "blur(20px)",
          backgroundColor: "rgba(18, 22, 31, 0.72)",
          border: "1px solid rgba(255,255,255,0.06)",
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 10,
        },
        contained: {
          boxShadow: "none",
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          backgroundImage: "none",
          backdropFilter: "blur(20px)",
          backgroundColor: "rgba(18, 22, 31, 0.72)",
          border: "1px solid rgba(255,255,255,0.06)",
        },
      },
    },
    MuiLinearProgress: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          height: 8,
        },
      },
    },
  },
});

/** Green for a gain, red for a loss, the one color code this whole app runs on. */
export function pnlColor(value: number): string {
  if (value > 0) return "#3ED598";
  if (value < 0) return "#F2545B";
  return "rgba(255,255,255,0.6)";
}
