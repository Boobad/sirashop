package com.sirashop.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Vérifie si la chaîne de caractères est une adresse email valide
     */
    public boolean isValidEmail(String email) {
        if (email == null) return false;
        String trimmed = email.trim();
        return EMAIL_PATTERN.matcher(trimmed).matches() || (trimmed.contains("@") && trimmed.contains("."));
    }

    /**
     * Envoie de manière asynchrone l'email contenant les identifiants de compte
     *
     * @param toEmail      Adresse email du destinataire
     * @param login        Identifiant (login)
     * @param rawPassword  Mot de passe en clair
     * @param roleOrType   Description du rôle ou du compte
     * @param companyName  Nom de l'entreprise (ou null)
     */
    public void sendAccountCreatedEmailAsync(String toEmail, String login, String rawPassword, String roleOrType, String companyName) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("⚠️ Impossible d'envoyer l'email : l'adresse email est vide.");
            return;
        }

        String recipient = toEmail.trim();
        if (!isValidEmail(recipient)) {
            log.warn("⚠️ Impossible d'envoyer l'email : '{}' n'est pas une adresse email valide.", recipient);
            return;
        }

        // Exécution en tâche de fond pour ne pas ralentir la requête HTTP
        CompletableFuture.runAsync(() -> {
            try {
                sendAccountCreatedEmail(recipient, login, rawPassword, roleOrType, companyName);
            } catch (Exception e) {
                log.error("❌ Erreur lors de l'envoi de l'email à {} : {}", recipient, e.getMessage(), e);
            }
        });
    }

    /**
     * Construit et envoie l'email au format HTML
     */
    public void sendAccountCreatedEmail(String toEmail, String login, String rawPassword, String roleOrType, String companyName) throws Exception {
        String recipient = toEmail.trim();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        try {
            helper.setFrom(senderEmail, "SiraShop");
        } catch (Exception e) {
            helper.setFrom(senderEmail);
        }
        
        helper.setTo(recipient);
        helper.setSubject("🎉 Bienvenue sur SiraShop - Vos identifiants de connexion");

        String htmlContent = buildWelcomeEmailHtml(login, rawPassword, roleOrType, companyName);
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("✅ Email d'inscription envoyé avec succès à : {} depuis {}", recipient, senderEmail);
    }

    /**
     * Génère le template HTML moderne et responsive
     */
    private String buildWelcomeEmailHtml(String login, String rawPassword, String roleOrType, String companyName) {
        String companySection = (companyName != null && !companyName.isBlank()) 
                ? "<p style='margin: 6px 0; color: #4b5563;'><strong>Entreprise :</strong> " + companyName + "</p>" 
                : "";

        return """
        <!DOCTYPE html>
        <html lang="fr">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Bienvenue sur SiraShop</title>
        </head>
        <body style="margin: 0; padding: 0; background-color: #f3f4f6; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #f3f4f6; padding: 40px 10px;">
                <tr>
                    <td align="center">
                        <table role="presentation" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);">
                            <!-- En-tête -->
                            <tr>
                                <td style="background: linear-gradient(135deg, #4f46e5 0%%, #7c3aed 100%%); padding: 35px 30px; text-align: center;">
                                    <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: 700; letter-spacing: 0.5px;">SiraShop</h1>
                                    <p style="color: #e0e7ff; margin: 8px 0 0 0; font-size: 15px;">Plateforme de Gestion & Commerce</p>
                                </td>
                            </tr>

                            <!-- Corps du message -->
                            <tr>
                                <td style="padding: 35px 30px; color: #1f2937;">
                                    <h2 style="color: #111827; margin-top: 0; font-size: 22px;">Bienvenue sur SiraShop ! 🎉</h2>
                                    <p style="color: #4b5563; font-size: 15px; line-height: 1.6;">
                                        Votre compte d'accès a été créé avec succès sur la plateforme. Voici vos identifiants pour vous connecter :
                                    </p>

                                    <!-- Boîte des identifiants -->
                                    <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-left: 4px solid #4f46e5; border-radius: 8px; padding: 20px; margin: 25px 0;">
                                        <p style="margin: 6px 0; color: #4b5563;">
                                            <strong style="color: #1e293b;">Identifiant (Login) :</strong> 
                                            <span style="color: #4f46e5; font-family: monospace; font-size: 15px; font-weight: 600;">%s</span>
                                        </p>
                                        <p style="margin: 6px 0; color: #4b5563;">
                                            <strong style="color: #1e293b;">Mot de passe :</strong> 
                                            <span style="color: #dc2626; font-family: monospace; font-size: 15px; font-weight: 600; background: #fee2e2; padding: 2px 8px; border-radius: 4px;">%s</span>
                                        </p>
                                        <p style="margin: 6px 0; color: #4b5563;">
                                            <strong style="color: #1e293b;">Rôle / Accès :</strong> 
                                            <span style="color: #1e293b;">%s</span>
                                        </p>
                                        %s
                                    </div>

                                    <!-- Recommandation de sécurité -->
                                    <div style="background-color: #fffbeb; border: 1px solid #fef3c7; border-radius: 8px; padding: 14px 16px; margin-bottom: 25px;">
                                        <p style="margin: 0; color: #92400e; font-size: 13px; line-height: 1.5;">
                                            🔒 <strong>Sécurité importante :</strong> Il s'agit d'un mot de passe temporaire par défaut. Vous serez obligatoirement invité(e) à le modifier dès votre première connexion pour activer et sécuriser votre accès.
                                        </p>
                                    </div>

                                    <p style="color: #4b5563; font-size: 14px; line-height: 1.5; margin-bottom: 0;">
                                        Si vous n'êtes pas à l'origine de cette inscription, veuillez ignorer ce message.
                                    </p>
                                </td>
                            </tr>

                            <!-- Pied de page -->
                            <tr>
                                <td style="background-color: #f9fafb; border-top: 1px solid #f3f4f6; padding: 20px 30px; text-align: center;">
                                    <p style="color: #9ca3af; font-size: 12px; margin: 0;">
                                        Envoyé automatiquement par <strong>SiraShop</strong> depuis <em>%s</em>.
                                    </p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """.formatted(login, rawPassword, roleOrType, companySection, senderEmail);
    }
}
