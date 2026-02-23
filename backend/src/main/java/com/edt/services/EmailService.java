// C:\projets\java\edt-generator\backend\src\main\java\com\edt\services\EmailService.java
package com.edt.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    /**
     * Envoie le code de connexion par email (version réelle)
     */
    public void envoyerCodeConnexion(String destinataire, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinataire);
            message.setSubject("🔐 Code de connexion - EDT Generator");
            message.setText(
                "Bonjour,\n\n" +
                "Voici votre code de connexion à l'application EDT Generator :\n\n" +
                "🔑 CODE: " + code + "\n\n" +
                "Ce code est valable 10 minutes.\n\n" +
                "Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.\n\n" +
                "Cordialement,\n" +
                "L'équipe EDT Generator"
            );
            
            mailSender.send(message);
            System.out.println("✅ Email envoyé avec succès à: " + destinataire);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: afficher le code dans la console
            System.out.println("\n" + "=".repeat(60));
            System.out.println("⚠️ ERREUR D'ENVOI EMAIL - CODE À UTILISER MANUELLEMENT");
            System.out.println("📧 À: " + destinataire);
            System.out.println("🔑 Code: " + code);
            System.out.println("=".repeat(60) + "\n");
        }
    }
}