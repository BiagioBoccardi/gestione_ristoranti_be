package com.gestione.ristoranti.gestione_ristoranti.ordini.service;

import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.OrdineStatoEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrdineEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void pubblicaStatoOrdine(OrdineStatoEvent event) {
        messagingTemplate.convertAndSend("/topic/cucina/ordini", event);
    }
}