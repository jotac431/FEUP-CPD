import java.util.UUID;

public class TokenManager {
    public static String generateToken(String username) {
        String token = UUID.randomUUID().toString();
        return token;
    }


}
