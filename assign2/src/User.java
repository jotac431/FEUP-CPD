import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class User {
    private final String username;
    private final String password;
    private String token;
    private Socket socket;

    public User(String username, String password, String token, Socket socket) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.socket = socket;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public OutputStream getOutputStream() {
        try {
            return socket.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public InputStream getInputStream() {
        try {
            return socket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
