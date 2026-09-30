package fitcubes.service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Service
@ConditionalOnProperty(name = "app.email.provider", havingValue = "ses")
public class SesEmailServiceImpl implements EmailService {

    private final SesClient sesClient;
    private final String senderEmail;

    public SesEmailServiceImpl(
            @Value("${aws.ses.region:eu-north-1}") String region,
            @Value("${aws.ses.sender-email:no-reply@fitcubes.uk}") String senderEmail) {
        this.sesClient = SesClient.builder().region(Region.of(region)).build();
        this.senderEmail = senderEmail;
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        String subject = "Reset your FitCubes password";
        String bodyText = "Click the link below to reset your password. "
                + "This link expires in 30 minutes.\n\n" + resetLink;

        SendEmailRequest request = SendEmailRequest.builder()
                .source(senderEmail)
                .destination(Destination.builder().toAddresses(toEmail).build())
                .message(Message.builder()
                        .subject(Content.builder().data(subject).build())
                        .body(Body.builder()
                                .text(Content.builder().data(bodyText).build())
                                .build())
                        .build())
                .build();

        sesClient.sendEmail(request);
    }
}
