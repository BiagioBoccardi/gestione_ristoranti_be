package com.gestione.ristoranti.gestione_ristoranti.config;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Categoria;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Piatto;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Ruolo;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoTavolo;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Tavolo;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Utente;
import com.gestione.ristoranti.gestione_ristoranti.auth.repository.RuoloRepository;
import com.gestione.ristoranti.gestione_ristoranti.auth.repository.UtenteRepository;
import com.gestione.ristoranti.gestione_ristoranti.menu.repository.CategoriaRepository;
import com.gestione.ristoranti.gestione_ristoranti.menu.repository.PiattoRepository;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.TavoloRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${ADMIN_DEFAULT_PASSWORD}")
    private String adminDefaultPassword;

    private final RuoloRepository ruoloRepository;
    private final UtenteRepository utenteRepository;
    private final CategoriaRepository categoriaRepository;
    private final PiattoRepository piattoRepository;
    private final TavoloRepository tavoloRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RuoloRepository ruoloRepository,
                           UtenteRepository utenteRepository,
                           CategoriaRepository categoriaRepository,
                           PiattoRepository piattoRepository,
                           TavoloRepository tavoloRepository,
                           PasswordEncoder passwordEncoder) {
        this.ruoloRepository = ruoloRepository;
        this.utenteRepository = utenteRepository;
        this.categoriaRepository = categoriaRepository;
        this.piattoRepository = piattoRepository;
        this.tavoloRepository = tavoloRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedRuoli();
        seedAdmin();
        seedMenuDemo();
        seedTavoli();
    }

    private void seedRuoli() {
        List<Ruolo> ruoli = List.of(
            new Ruolo("ADMIN",     "Gestione completa: menu, staff, analytics, configurazione sistema"),
            new Ruolo("CAMERIERE", "Presa ordini, gestione prenotazioni, generazione conto"),
            new Ruolo("CUOCO",     "Vista ordini live, aggiornamento stato piatti, segnalazione esaurimento"),
            new Ruolo("CLIENTE",   "Prenotazione online, menu digitale, ordine da QR code al tavolo")
        );
        for (Ruolo ruolo : ruoli) {
            if (ruoloRepository.findByNome(ruolo.getNome()).isEmpty()) {
                ruoloRepository.save(ruolo);
            }
        }
    }

    private void seedAdmin() {
        String adminEmail = "admin@restora.it";
        if (utenteRepository.findByEmail(adminEmail).isPresent()) return;

        Ruolo ruoloAdmin = ruoloRepository.findByNome("ADMIN")
                .orElseThrow(() -> new IllegalStateException("Ruolo ADMIN non trovato"));

        Utente admin = new Utente();
        admin.setNome("Admin");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminDefaultPassword));
        admin.setRuolo(ruoloAdmin);
        utenteRepository.save(admin);
        log.info("Account admin iniziale creato per: {}", adminEmail);
    }

    private void seedMenuDemo() {
        record CategoriaData(String nome, String descrizione, String piatto, String descPiatto, BigDecimal prezzo) {}

        List<CategoriaData> dati = List.of(
            new CategoriaData("Antipasti",  "Stuzzichini e entrée",
                "Bruschetta al pomodoro",   "Pane tostato con pomodori freschi, aglio e basilico",          new BigDecimal("6.50")),
            new CategoriaData("Primi",      "Pasta, risotti e zuppe",
                "Spaghetti alla carbonara", "Spaghetti con uova, guanciale croccante e pecorino romano",     new BigDecimal("12.00")),
            new CategoriaData("Secondi",    "Carne e pesce",
                "Tagliata di manzo",        "Tagliata con rucola, scaglie di parmigiano e glassa di balsamico", new BigDecimal("18.50")),
            new CategoriaData("Contorni",   "Verdure e insalate",
                "Insalata mista",           "Lattuga, radicchio, pomodorini e carote con olio e limone",     new BigDecimal("4.50")),
            new CategoriaData("Dolci",      "Dessert e dolci al cucchiaio",
                "Tiramisù della casa",      "Classico tiramisù con mascarpone, savoiardi e caffè espresso",  new BigDecimal("7.00")),
            new CategoriaData("Bevande",    "Acqua, vini e analcolici",
                "Acqua naturale 1L",        "Acqua minerale naturale in bottiglia da 1 litro",               new BigDecimal("2.00"))
        );

        for (var d : dati) {
            Categoria categoria = categoriaRepository.findByNome(d.nome()).orElseGet(() -> {
                Categoria c = new Categoria(d.nome(), d.descrizione());
                return categoriaRepository.save(c);
            });

            if (piattoRepository.findByCategoria(categoria).isEmpty()) {
                Piatto piatto = new Piatto();
                piatto.setCategoria(categoria);
                piatto.setNome(d.piatto());
                piatto.setDescrizione(d.descPiatto());
                piatto.setPrezzo(d.prezzo());
                piatto.setDisponibile(true);
                piattoRepository.save(piatto);
            }
        }
    }

    private void seedTavoli() {
        if (tavoloRepository.count() > 0) return;

        record TavoloData(int numero, int coperti) {}

        List<TavoloData> tavoli = List.of(
            new TavoloData(1, 2),
            new TavoloData(2, 2),
            new TavoloData(3, 4),
            new TavoloData(4, 4),
            new TavoloData(5, 4),
            new TavoloData(6, 6),
            new TavoloData(7, 6),
            new TavoloData(8, 8),
            new TavoloData(9, 8),
            new TavoloData(10, 10)
        );

        for (var t : tavoli) {
            tavoloRepository.save(new Tavolo(t.numero(), t.coperti(), StatoTavolo.LIBERO));
        }
        log.info("Tavoli inizializzati: {}", tavoli.size());
    }
}