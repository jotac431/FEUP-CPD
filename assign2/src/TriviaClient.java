import java.net.*;
import java.util.*;
import java.io.*;
 
public class TriviaClient {
 
    public static void main(String[] args) throws InterruptedException {
 
        try {
            Socket socket = new Socket("localhost", 8081);
            System.out.println("========== Trivia Game ==========" + "\n");

            // read token from file and send it to server, in case 
            try (BufferedReader reader = new BufferedReader(new FileReader("token.txt"))) {
                String token = reader.readLine();
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(token);
            } catch (IOException e) {
                System.out.println("Error reading from file " + e);
            }

            
            var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            var out = new PrintWriter(socket.getOutputStream(), true);


            String isTokenValid = in.readLine();

            if(isTokenValid.startsWith("token is valid")){ 
            // print token is valid in client console
            System.out.println("Welcome Back!" + "\n");
            }


            if (!isTokenValid.startsWith("token is valid")){
                System.out.println("          Authentication      " + "\n");
                var scanner = new Scanner(System.in);

                System.out.println(">Press 1 for login" + "\n" + ">Press 2 for register.");
                var loginOrRegister = scanner.nextLine();
                while(!loginOrRegister.equals("1") && !loginOrRegister.equals("2")){
                    System.out.println("Input error.\nIf you already have an account type 'login', else type 'register'.");
                    loginOrRegister = scanner.nextLine();
                }
                out.println(loginOrRegister);

                if(loginOrRegister.equals("2")){

                    System.out.println(">Your username:");
                    out.println(scanner.nextLine());

                    System.out.println(">Your password:");
                    out.println(scanner.nextLine());

                    String serverMessage = in.readLine();

                    while(!serverMessage.startsWith("Registration successful")){
                        System.out.println(serverMessage);

                        System.out.println(">Your username:");
                        out.println(scanner.nextLine());

                        System.out.println(">Your password:");
                        out.println(scanner.nextLine());

                        serverMessage = in.readLine();
                    }

                    System.out.println(serverMessage);

                } else {

                    System.out.println(">Your login:");
                    out.println(scanner.nextLine());

                    System.out.println(">Your password:");
                    out.println(scanner.nextLine());

                    String serverMessage = in.readLine();

                    while(!serverMessage.startsWith("Login successful")){
                        System.out.println(serverMessage);

                        System.out.println(">Your login:");
                        out.println(scanner.nextLine());

                        System.out.println(">Your password:");
                        out.println(scanner.nextLine());

                        serverMessage = in.readLine();
                    }

                    System.out.println(serverMessage);

                    // receive token from server
                    String tokenSendByServer = in.readLine();
                        
                    // write token to file
                    try (PrintWriter writer = new PrintWriter(new FileWriter("token.txt"))) {
                        writer.println(tokenSendByServer);
                    } catch (IOException e) {
                        System.out.println("Error writing to file " + e);
                    }

                    System.out.println("Your token is :" + tokenSendByServer);

                }
            }
                System.out.println("Please wait for the game to start!");

                // Create input and output streams for the socket
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

                // Jogar o jogo
                while(true){
                // ask user to enter guess
                System.out.print(inputStream.readUTF());

                var scanner = new Scanner(System.in);
                // read guess which is a string and not an int from user
                String guess = scanner.nextLine();

                // Send guess to server
                outputStream.writeUTF(guess);

                // receive result from the server (Congratulations or Sorry you lost)
                String result = inputStream.readUTF();
                System.out.println(result);
                if(result.startsWith("Sorry")){
                    break;
                }

            }

            } catch (EOFException e) {
                System.out.println("Connection to server lost.");
    
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }