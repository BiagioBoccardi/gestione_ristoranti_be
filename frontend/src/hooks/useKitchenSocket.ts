import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { OrdineStatoEvent } from '@/types/ordine';

const WS_URL =
  (import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api').replace('/api', '') + '/ws';

/**
 * Hook che mantiene una connessione STOMP/SockJS con il backend per ricevere
 * aggiornamenti in tempo reale sullo stato degli ordini nella vista cucina.
 *
 * Si connette a `/topic/cucina/ordini` e include il JWT dal localStorage
 * nell'header di connessione. Riprova automaticamente ogni 5 secondi in caso
 * di disconnessione.
 *
 * @returns `connected` — true se la connessione WebSocket è attiva;
 *          `lastEvent` — ultimo evento di cambio stato ordine ricevuto, o null
 */
export function useKitchenSocket() {
  const [connected, setConnected] = useState(false);
  const [lastEvent, setLastEvent] = useState<OrdineStatoEvent | null>(null);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('token');

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true);
        client.subscribe('/topic/cucina/ordini', (message) => {
          setLastEvent(JSON.parse(message.body) as OrdineStatoEvent);
        });
      },
      onDisconnect: () => setConnected(false),
      onStompError: () => setConnected(false),
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, []);

  return { connected, lastEvent };
}
