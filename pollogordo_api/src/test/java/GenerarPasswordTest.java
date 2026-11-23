import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerarPasswordTest {

    @Test
    void verificarPassword() {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "admin123";
        String hash = "$2a$10$HtKRNQWBqigx9fuvP.5ly.3oRb1SuX6AK041iZ3LjEBlMFY9fQbtW";

        boolean coincide = encoder.matches(password, hash);

        System.out.println("¿La contraseña coincide? " + coincide);
    }
}