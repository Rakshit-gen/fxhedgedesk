"use client";

import { useEffect, useRef } from "react";

interface Point {
  x: number;
  y: number;
}

interface TickerLine {
  points: Point[];
  baseY: number;
  lastValue: number;
}

const DX = 9;

/**
 * The page-wide version of the hero's ticker lines: same mechanism, far
 * fewer lines, far lower opacity. It sits behind every screen in the app so
 * the "live market" feel doesn't stop once you leave the homepage.
 */
export default function TickerBackground() {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    const dpr = window.devicePixelRatio || 1;
    let width = 0;
    let height = 0;
    let lines: TickerLine[] = [];
    let animationFrame = 0;
    const scrollSpeed = 0.3 * dpr;

    function randomWalk(previous: number, volatility: number, bounds: [number, number]) {
      const next = previous + (Math.random() - 0.5) * volatility;
      return Math.min(bounds[1], Math.max(bounds[0], next));
    }

    function buildLine(baseY: number): TickerLine {
      const points: Point[] = [];
      let value = baseY;
      for (let x = 0; x <= width + DX; x += DX) {
        value = randomWalk(value, height * 0.006, [baseY - height * 0.08, baseY + height * 0.08]);
        points.push({ x, y: value });
      }
      return { points, baseY, lastValue: value };
    }

    function resize() {
      if (!canvas) return;
      width = canvas.width = canvas.offsetWidth * dpr;
      height = canvas.height = canvas.offsetHeight * dpr;
      lines = [0.2, 0.55, 0.85].map((band) => buildLine(height * band));
    }

    function step() {
      if (!ctx) return;
      ctx.clearRect(0, 0, width, height);

      for (const line of lines) {
        for (const point of line.points) {
          point.x -= scrollSpeed;
        }
        while (line.points.length && line.points[0].x < -DX) {
          line.points.shift();
        }
        const rightmost = line.points[line.points.length - 1];
        while (rightmost.x < width + DX) {
          line.lastValue = randomWalk(line.lastValue, height * 0.006, [
            line.baseY - height * 0.08,
            line.baseY + height * 0.08,
          ]);
          line.points.push({ x: rightmost.x + DX, y: line.lastValue });
          rightmost.x += DX;
        }

        ctx.beginPath();
        line.points.forEach((point, i) => {
          if (i === 0) ctx.moveTo(point.x, point.y);
          else ctx.lineTo(point.x, point.y);
        });
        ctx.strokeStyle = "rgba(62,198,224,0.07)";
        ctx.lineWidth = 1 * dpr;
        ctx.stroke();
      }

      animationFrame = requestAnimationFrame(step);
    }

    resize();
    step();
    window.addEventListener("resize", resize);
    return () => {
      window.removeEventListener("resize", resize);
      cancelAnimationFrame(animationFrame);
    };
  }, []);

  return (
    <canvas
      ref={canvasRef}
      aria-hidden
      style={{
        position: "fixed",
        inset: 0,
        width: "100%",
        height: "100%",
        zIndex: -1,
        pointerEvents: "none",
      }}
    />
  );
}
