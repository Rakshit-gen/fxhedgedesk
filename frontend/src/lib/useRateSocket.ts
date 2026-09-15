"use client";

import { useEffect, useRef } from "react";
import { Client } from "@stomp/stompjs";
import { API_BASE_URL } from "./api";
import type { RateUpdate } from "./types";

/**
 * Subscribes to live rate pushes for a set of currency pairs at once.
 * Connects straight to the SockJS endpoint's raw WebSocket transport, no
 * sockjs-client dependency needed for a browser that already speaks native
 * WebSocket, which is every browser this app targets.
 */
export function useRateSocket(pairCodes: string[], onUpdate: (update: RateUpdate) => void) {
  const onUpdateRef = useRef(onUpdate);

  useEffect(() => {
    onUpdateRef.current = onUpdate;
  }, [onUpdate]);

  const pairKey = pairCodes.join(",");

  useEffect(() => {
    if (!pairCodes.length) return;

    const wsUrl = API_BASE_URL.replace(/^http/, "ws") + "/ws/websocket";
    const client = new Client({
      brokerURL: wsUrl,
      reconnectDelay: 4000,
      onConnect: () => {
        for (const pairCode of pairCodes) {
          client.subscribe(`/topic/rates/${pairCode}`, (message) => {
            try {
              onUpdateRef.current(JSON.parse(message.body) as RateUpdate);
            } catch {
              // Malformed push, ignore this one, the next tick will self-correct.
            }
          });
        }
      },
    });

    client.activate();
    return () => {
      client.deactivate();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps -- pairKey is the stable identity for pairCodes
  }, [pairKey]);
}
