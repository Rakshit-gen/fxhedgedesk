"use client";

import { useEffect, useRef } from "react";

interface Point {
  x: number;
  y: number;
}

interface TickerLine {
  points: Point[];
  baseY: number;
  volatility: number;
  color: string;
  lastValue: number;
}

const DX = 7;

/**
 * The hero's backdrop: a handful of live-scrolling rate lines, the same
 * shape as a trading terminal's ticker chart, because that is the closest
 * real-world thing to what this app actually shows you. Each line is its
 * own tiny random walk, scrolled left every frame and extended on the right,
 * same mechanism as the rate simulation itself, just drawn instead of traded.
 */
export default function HeroBackground() {
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
    const scrollSpeed = 0.5 * dpr;

    function randomWalk(previous: number, volatility: number, bounds: [number, number]) {
      const next = previous + (Math.random() - 0.5) * volatility;
      return Math.min(bounds[1], Math.max(bounds[0], next));
    }

    function buildLine(baseY: number, volatility: number, color: string): TickerLine {
      const points: Point[] = [];
      let value = baseY;
      for (let x = 0; x <= width + DX; x += DX) {
        value = randomWalk(value, volatility, [baseY - height * 0.14, baseY + height * 0.14]);
        points.push({ x, y: value });
      }
      return { points, baseY, volatility, color, lastValue: value };
    }

    function resize() {
      if (!canvas) return;
      width = canvas.width = canvas.offsetWidth * dpr;
      height = canvas.height = canvas.offsetHeight * dpr;

      const bands = [0.28, 0.46, 0.64, 0.82];
      const colors = ["62,198,224", "240,180,41", "62,213,152", "125,220,239"];
      lines = bands.map((band, i) => buildLine(height * band, height * 0.012, colors[i % colors.length]));
    }

    function step() {
      if (!ctx) return;
      ctx.clearRect(0, 0, width, height);

      // faint terminal grid
      ctx.strokeStyle = "rgba(255,255,255,0.035)";
      ctx.lineWidth = 1;
      for (let gx = 0; gx < width; gx += 60 * dpr) {
        ctx.beginPath();
        ctx.moveTo(gx, 0);
        ctx.lineTo(gx, height);
        ctx.stroke();
      }
      for (let gy = 0; gy < height; gy += 60 * dpr) {
        ctx.beginPath();
        ctx.moveTo(0, gy);
        ctx.lineTo(width, gy);
        ctx.stroke();
      }

      for (const line of lines) {
        for (const point of line.points) {
          point.x -= scrollSpeed;
        }
        while (line.points.length && line.points[0].x < -DX) {
          line.points.shift();
        }
        const rightmost = line.points[line.points.length - 1];
        while (rightmost.x < width + DX) {
          line.lastValue = randomWalk(line.lastValue, line.volatility, [
            line.baseY - height * 0.14,
            line.baseY + height * 0.14,
          ]);
          line.points.push({ x: rightmost.x + DX, y: line.lastValue });
          rightmost.x += DX;
        }

        const gradient = ctx.createLinearGradient(0, 0, width, 0);
        gradient.addColorStop(0, `rgba(${line.color}, 0)`);
        gradient.addColorStop(0.5, `rgba(${line.color}, 0.55)`);
        gradient.addColorStop(1, `rgba(${line.color}, 0)`);

        ctx.beginPath();
        line.points.forEach((point, i) => {
          if (i === 0) ctx.moveTo(point.x, point.y);
          else ctx.lineTo(point.x, point.y);
        });
        ctx.strokeStyle = gradient;
        ctx.lineWidth = 1.4 * dpr;
        ctx.stroke();

        const fillGradient = ctx.createLinearGradient(0, line.baseY - height * 0.14, 0, height);
        fillGradient.addColorStop(0, `rgba(${line.color}, 0.08)`);
        fillGradient.addColorStop(1, `rgba(${line.color}, 0)`);
        ctx.lineTo(line.points[line.points.length - 1].x, height);
        ctx.lineTo(line.points[0].x, height);
        ctx.closePath();
        ctx.fillStyle = fillGradient;
        ctx.fill();
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
        position: "absolute",
        inset: 0,
        width: "100%",
        height: "100%",
      }}
    />
  );
}
