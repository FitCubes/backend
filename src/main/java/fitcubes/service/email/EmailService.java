package fitcubes.service.email;

public interface EmailService {

    void sendPasswordResetEmail(String toEmail, String resetLink);
}
