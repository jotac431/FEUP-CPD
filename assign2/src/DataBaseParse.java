import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DataBaseParse {

    public Map<String, String> loadUsersFromFile(String filename) throws IOException {
        Map<String, String> users = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
    
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length >= 2) {
                String login = parts[0];
                String password = parts[1];
                users.put(login, password);
            }
        }
    
        br.close();
        return users;
    }

    //create me a function that returns a Map<String,String> of logins and their tokens. If there is no token, return a string "0"
    public Map<String, String> loadTokensFromFile(String filename) throws IOException {
        Map<String, String> tokens = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
    
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length == 3) {
                String login = parts[0];
                String token = parts[2];
                tokens.put(login, token);
            }
        }
    
        br.close();
        return tokens;
    }
    
    
    public Map<String, String> loadQuestionsFromFile(String filename) throws IOException {
        Map<String, String> questions = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
    
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            String question = parts[0];
            String answer = parts[1];
            questions.put(question, answer);
        }
    
        br.close();
        return questions;
    }

    // Function to add a new user to the file
    public  void addUserToFile(String username, String password, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))) {
            writer.println(username + "," + password+ "," + "0");
        }
    }

}