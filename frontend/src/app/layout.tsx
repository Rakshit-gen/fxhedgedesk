import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import Providers from "@/components/Providers";
import TickerBackground from "@/components/TickerBackground";

const inter = Inter({
  variable: "--font-inter",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "FX Hedge Desk",
  description: "A simulated corporate treasury desk for hedging foreign-currency exposure. No real money changes hands.",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" className={inter.variable}>
      <body>
        <div className="gradient-glow" />
        <TickerBackground />
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
