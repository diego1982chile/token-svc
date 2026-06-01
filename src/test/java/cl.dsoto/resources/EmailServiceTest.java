package cl.dsoto.resources;

import cl.dsoto.services.EmailService;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

@QuarkusTest
class EmailServiceTest {

    @Inject
    EmailService emailService;

    @Inject
    MockMailbox mailbox;

    @BeforeEach
    void clearMailbox() {
        mailbox.clear();
    }

    @Test
    void shouldSendRenderedEmailConfirmationHtml() {
        String email = "new.user@example.com";
        String confirmationUrl = "http://localhost:9090/token-service/users/confirm-email?token=abc123";

        emailService.sendEmailConfirmation(email, confirmationUrl);

        List<Mail> messages = mailbox.getMailsSentTo(email);
        assertThat(messages, hasSize(1));

        Mail message = messages.getFirst();
        assertThat(message.getSubject(), is("Confirma tu correo"));
        assertThat(message.getHtml(), containsString("Confirma tu correo"));
        assertThat(message.getHtml(), containsString("Se creo una cuenta asociada a este correo"));
        assertThat(message.getHtml(), containsString("href=\"" + confirmationUrl + "\""));
        assertThat(message.getHtml(), containsString("Este enlace expira el "));
    }
}
