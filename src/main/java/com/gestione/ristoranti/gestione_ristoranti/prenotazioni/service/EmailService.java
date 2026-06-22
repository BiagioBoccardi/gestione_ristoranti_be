package com.gestione.ristoranti.gestione_ristoranti.prenotazioni.service;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Prenotazione;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mittente;

    @Value("${app.mail.from-name}")
    private String nomeMittente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void inviaConfermaPrenotazione(Prenotazione p) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(nomeMittente + " <" + mittente + ">");
        msg.setTo(p.getUtente().getEmail());
        msg.setSubject("Prenotazione confermata — " + nomeMittente);
        msg.setText(
            "Ciao " + p.getUtente().getNome() + ",\n\n" +
            "La tua prenotazione è confermata:\n" +
            "  Data:    " + p.getData() + "\n" +
            "  Ora:     " + p.getOra() + "\n" +
            "  Tavolo:  " + p.getTavolo().getNumero() + "\n" +
            "  Coperti: " + p.getCoperti() + "\n" +
            (p.getNote() != null && !p.getNote().isBlank() ? "  Note:    " + p.getNote() + "\n" : "") +
            "\nA presto!\n" + nomeMittente
        );
        mailSender.send(msg);
    }

    public void inviaAggiornamentiPrenotazione(Prenotazione p) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(nomeMittente + " <" + mittente + ">");
        msg.setTo(p.getUtente().getEmail());
        msg.setSubject("Prenotazione aggiornata — " + nomeMittente);
        msg.setText(
            "Ciao " + p.getUtente().getNome() + ",\n\n" +
            "La tua prenotazione è stata aggiornata:\n" +
            "  Data:    " + p.getData() + "\n" +
            "  Ora:     " + p.getOra() + "\n" +
            "  Tavolo:  " + p.getTavolo().getNumero() + "\n" +
            "  Coperti: " + p.getCoperti() + "\n" +
            (p.getNote() != null && !p.getNote().isBlank() ? "  Note:    " + p.getNote() + "\n" : "") +
            "\nA presto!\n" + nomeMittente
        );
        mailSender.send(msg);
    }

    public void inviaCancellazionePrenotazione(Prenotazione p) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(nomeMittente + " <" + mittente + ">");
        msg.setTo(p.getUtente().getEmail());
        msg.setSubject("Prenotazione cancellata — " + nomeMittente);
        msg.setText(
            "Ciao " + p.getUtente().getNome() + ",\n\n" +
            "La tua prenotazione del " + p.getData() + " alle " + p.getOra() +
            " (Tavolo " + p.getTavolo().getNumero() + ") è stata cancellata.\n\n" +
            "A presto!\n" + nomeMittente
        );
        mailSender.send(msg);
    }

    @Async
    public void inviaBenvenutoStaff(String toEmail, String nomeUtente, String passwordTemporanea) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mittente, nomeMittente);
            helper.setTo(toEmail);
            helper.setSubject("Benvenuto nel team — " + nomeMittente);
            String html = """
                    <!DOCTYPE html>
                    <html lang="it">
                    <head><meta charset="UTF-8"></head>
                    <body style="margin:0;padding:0;background:#09090b;font-family:sans-serif;">
                      <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr><td align="center" style="padding:40px 20px;">
                          <table width="480" cellpadding="0" cellspacing="0"
                                 style="background:#18181b;border:1px solid #27272a;border-radius:16px;padding:40px;">
                            <tr><td>
                              <p style="color:#818cf8;font-size:12px;font-weight:600;letter-spacing:0.2em;text-transform:uppercase;margin:0 0 24px;">
                                %s
                              </p>
                              <h1 style="color:#f4f4f5;font-size:22px;font-weight:600;margin:0 0 12px;">
                                Il tuo account è pronto
                              </h1>
                              <p style="color:#a1a1aa;font-size:14px;line-height:1.6;margin:0 0 20px;">
                                Ciao %s,<br><br>
                                Un account staff è stato creato per te. Accedi con le seguenti credenziali e completa la verifica.
                              </p>
                              <table style="background:#09090b;border:1px solid #27272a;border-radius:10px;padding:20px;margin-bottom:24px;width:100%%%%;">
                                <tr>
                                  <td style="color:#71717a;font-size:12px;padding-bottom:6px;">Email</td>
                                </tr>
                                <tr>
                                  <td style="color:#f4f4f5;font-size:14px;font-weight:600;padding-bottom:16px;">%s</td>
                                </tr>
                                <tr>
                                  <td style="color:#71717a;font-size:12px;padding-bottom:6px;">Password temporanea</td>
                                </tr>
                                <tr>
                                  <td style="color:#f4f4f5;font-size:16px;font-weight:700;letter-spacing:0.1em;">%s</td>
                                </tr>
                              </table>
                              <p style="color:#a1a1aa;font-size:13px;line-height:1.6;margin:0 0 8px;">
                                Dopo il login riceverai via email un codice di verifica a 6 cifre da inserire per completare il primo accesso.
                              </p>
                              <p style="color:#52525b;font-size:12px;margin:28px 0 0;line-height:1.6;">
                                Se non ti aspettavi questa email, contatta l'amministratore.
                              </p>
                            </td></tr>
                          </table>
                        </td></tr>
                      </table>
                    </body>
                    </html>
                    """.formatted(nomeMittente, nomeUtente, toEmail, passwordTemporanea);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Errore invio email benvenuto staff: " + e.getMessage(), e);
        }
    }

    @Async
    public void inviaCodiceVerificaPrimoAccesso(String toEmail, String nomeUtente, String codice) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mittente, nomeMittente);
            helper.setTo(toEmail);
            helper.setSubject("Codice di verifica — " + nomeMittente);
            String html = """
                    <!DOCTYPE html>
                    <html lang="it">
                    <head><meta charset="UTF-8"></head>
                    <body style="margin:0;padding:0;background:#09090b;font-family:sans-serif;">
                      <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr><td align="center" style="padding:40px 20px;">
                          <table width="480" cellpadding="0" cellspacing="0"
                                 style="background:#18181b;border:1px solid #27272a;border-radius:16px;padding:40px;">
                            <tr><td>
                              <p style="color:#818cf8;font-size:12px;font-weight:600;letter-spacing:0.2em;text-transform:uppercase;margin:0 0 24px;">
                                %s
                              </p>
                              <h1 style="color:#f4f4f5;font-size:22px;font-weight:600;margin:0 0 12px;">
                                Codice di verifica primo accesso
                              </h1>
                              <p style="color:#a1a1aa;font-size:14px;line-height:1.6;margin:0 0 28px;">
                                Ciao %s,<br><br>
                                Inserisci il codice qui sotto per completare il tuo primo accesso.
                                Il codice è valido per <strong style="color:#f4f4f5;">15 minuti</strong>.
                              </p>
                              <div style="background:#09090b;border:1px solid #27272a;border-radius:12px;padding:24px;text-align:center;margin-bottom:24px;">
                                <span style="color:#818cf8;font-size:36px;font-weight:700;letter-spacing:0.3em;">%s</span>
                              </div>
                              <p style="color:#52525b;font-size:12px;margin:0;line-height:1.6;">
                                Se non hai tentato di accedere, ignora questa email e contatta l'amministratore.
                              </p>
                            </td></tr>
                          </table>
                        </td></tr>
                      </table>
                    </body>
                    </html>
                    """.formatted(nomeMittente, nomeUtente, codice);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Errore invio codice verifica: " + e.getMessage(), e);
        }
    }

    @Async
    public void inviaImpostaPassword(String toEmail, String nomeUtente, String link) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mittente, nomeMittente);
            helper.setTo(toEmail);
            helper.setSubject("Imposta la tua password — " + nomeMittente);
            String html = """
                    <!DOCTYPE html>
                    <html lang="it">
                    <head><meta charset="UTF-8"></head>
                    <body style="margin:0;padding:0;background:#09090b;font-family:sans-serif;">
                      <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr><td align="center" style="padding:40px 20px;">
                          <table width="480" cellpadding="0" cellspacing="0"
                                 style="background:#18181b;border:1px solid #27272a;border-radius:16px;padding:40px;">
                            <tr><td>
                              <p style="color:#818cf8;font-size:12px;font-weight:600;letter-spacing:0.2em;text-transform:uppercase;margin:0 0 24px;">
                                %s
                              </p>
                              <h1 style="color:#f4f4f5;font-size:22px;font-weight:600;margin:0 0 12px;">
                                Il tuo account è pronto
                              </h1>
                              <p style="color:#a1a1aa;font-size:14px;line-height:1.6;margin:0 0 28px;">
                                Ciao %s,<br><br>
                                Un account è stato creato per te su <strong style="color:#f4f4f5;">%s</strong>.
                                Clicca il pulsante qui sotto per impostare la tua password e accedere.
                                Il link è valido per <strong style="color:#f4f4f5;">24 ore</strong>.
                              </p>
                              <a href="%s"
                                 style="display:inline-block;background:#4f46e5;color:#ffffff;text-decoration:none;
                                        padding:14px 32px;border-radius:10px;font-size:14px;font-weight:600;
                                        letter-spacing:0.02em;">
                                Imposta Password
                              </a>
                              <p style="color:#52525b;font-size:12px;margin:28px 0 0;line-height:1.6;">
                                Se non ti aspettavi questa email, contatta l'amministratore e ignora questo messaggio.
                              </p>
                            </td></tr>
                          </table>
                        </td></tr>
                      </table>
                    </body>
                    </html>
                    """.formatted(nomeMittente, nomeUtente, nomeMittente, link);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Errore invio email imposta password: " + e.getMessage(), e);
        }
    }

    @Async
    public void inviaResetPassword(String toEmail, String nomeUtente, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mittente, nomeMittente);
            helper.setTo(toEmail);
            helper.setSubject("Reimposta la tua password — " + nomeMittente);
            String html = """
                    <!DOCTYPE html>
                    <html lang="it">
                    <head><meta charset="UTF-8"></head>
                    <body style="margin:0;padding:0;background:#09090b;font-family:sans-serif;">
                      <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr><td align="center" style="padding:40px 20px;">
                          <table width="480" cellpadding="0" cellspacing="0"
                                 style="background:#18181b;border:1px solid #27272a;border-radius:16px;padding:40px;">
                            <tr><td>
                              <p style="color:#818cf8;font-size:12px;font-weight:600;letter-spacing:0.2em;text-transform:uppercase;margin:0 0 24px;">
                                %s
                              </p>
                              <h1 style="color:#f4f4f5;font-size:22px;font-weight:600;margin:0 0 12px;">
                                Reimposta la tua password
                              </h1>
                              <p style="color:#a1a1aa;font-size:14px;line-height:1.6;margin:0 0 28px;">
                                Ciao %s,<br><br>
                                Abbiamo ricevuto una richiesta di reimpostazione della password.
                                Il link è valido per <strong style="color:#f4f4f5;">1 ora</strong>.
                              </p>
                              <a href="%s"
                                 style="display:inline-block;background:#4f46e5;color:#ffffff;text-decoration:none;
                                        padding:12px 28px;border-radius:8px;font-size:13px;font-weight:600;">
                                Reimposta password
                              </a>
                              <p style="color:#52525b;font-size:12px;margin:28px 0 0;line-height:1.6;">
                                Se non hai richiesto il reset, ignora questa email.
                              </p>
                            </td></tr>
                          </table>
                        </td></tr>
                      </table>
                    </body>
                    </html>
                    """.formatted(nomeMittente, nomeUtente, resetLink);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Errore invio email di reset: " + e.getMessage(), e);
        }
    }
}
