import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class TriviaServer {
    static final List<User> queue = new ArrayList<>();
    private static Map<String, String> currentUsers = new HashMap<>();
    private static Map<String,String> users = new HashMap<>();
    private static final ReentrantLock lock = new ReentrantLock();
    static DataBaseParse dataBaseParse = new DataBaseParse();

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(8081)) {
            System.out.println("Players are joining...");

            ExecutorService executor = Executors.newFixedThreadPool(5);
            Semaphore semaphore = new Semaphore(5);

            users = dataBaseParse.loadUsersFromFile("users.txt");

            while (true) {
                // accept a socket
                Socket socket = server.accept();
                System.out.println("A Player is trying to connect!");

                var input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                var output = new PrintWriter(socket.getOutputStream(), true);

                boolean isTokenValid = false;
                // checks if the token send by the client is valid
                Map<String, String> tokenMap = dataBaseParse.loadTokensFromFile("users.txt");

                String token1 = input.readLine();


                // if the token is the same as the secound part of the map, the token is valid 
                if (tokenMap.containsValue(token1)) {
                    isTokenValid = true;
                    //send to client that the token is valid
                    output.println("token is valid");
                    String login = getLoginFromToken(token1);
                    String password = getPasswordFromLogin(login);
                    try{
                        lock.lock();
                        queue.add(new User(login,password, token1, socket));
                    } finally {
                        lock.unlock();
                    }

                } 
                else {
                    //send to client that the token is invalid
                    output.println("token is invalid");
                }
                if (!isTokenValid) {
                    var registerOrlogin = input.readLine();

                    if(registerOrlogin.equals("2")){
                        var loginToRegister = input.readLine();
                        var passwordToRegister = input.readLine();

                        while (users.containsKey(loginToRegister)) {
                            output.println("This login you are trying to register with is already taken. Please try again.");

                            loginToRegister = input.readLine();
                            passwordToRegister = input.readLine();
                        }
                        users.put(loginToRegister, passwordToRegister);

                        currentUsers.put(loginToRegister, passwordToRegister);
                        output.println("Registration successful! You are now in the queue, wait for other players to join.");

                        String token = TokenManager.generateToken(loginToRegister);
                        updateUsersFile(loginToRegister,token);

                        try{
                            lock.lock();
                            queue.add(new User(loginToRegister,passwordToRegister, token, socket));
                        } finally {
                            lock.unlock();
                        }

                    }
                    else if (registerOrlogin.equals("1")) {
                        var login = input.readLine();
                        var password = input.readLine();
                    
                        while (!authenticateUser(login, password)) {
                            output.println("Invalid username or password. Please try again.");
                            login = input.readLine();
                            password = input.readLine();
                        }
                    
                        currentUsers.put(login, password);

                        String token = TokenManager.generateToken(login);

                        updateUsersFile(login,token);
                    
                        try {
                            lock.lock();
                            queue.add(new User(login,password, token, socket));
                        } finally {
                            lock.unlock();
                        }
                    
                        output.println("Login successful! You are now in the queue, wait for other players.");

                        // Send token to client
                        output.println(token);
                    }
                }

                Thread threadQ = new Thread(() -> {
                    while (true) {
                        try {
                            Thread.sleep(10000);
                            if (queue.size() >= 2) {

                                for (int i = 0; i < queue.size(); i++) {
                                    User first = queue.get(i);
                                    for (int j = i + 1; j < queue.size(); j++) {
                                        User secound = queue.get(j);
                                        List<User> list = new ArrayList<>();
                                        list.add(queue.get(i));
                                        list.add(queue.get(j));
                                        var game = new Game(list);
                                        executor.execute(game);
                                        list.clear();
                                        queue.remove(first);
                                        queue.remove(secound);
                                        System.out.println("The game started.");
                                    }
                                }

                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                });

                threadQ.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

        private static boolean authenticateUser(String login, String password) throws IOException {

            // check if users contain login and password and if currentUsers doesn't contain login
            if (users.containsKey(login) && users.get(login).equals(password) && !currentUsers.containsKey(login)) {
                return true;
            }
            return false;
        }

        private static void updateUsersFile(String login, String token){
            lock.lock();
            try (PrintWriter writer = new PrintWriter(new FileWriter("users.txt"))) {
                Map<String, String> updatedUsers = new HashMap<>();
                for (Map.Entry<String, String> entry : users.entrySet()) {
                    if (entry.getKey().equals(login)) {
                        writer.println(entry.getKey() + "," + entry.getValue() + "," + token);
                        updatedUsers.put(entry.getKey(), entry.getValue() + "," + token);
                    } else {
                        writer.println(entry.getKey() + "," + entry.getValue());
                        updatedUsers.put(entry.getKey(), entry.getValue());
                    }
                }
                // Atualiza o mapa "users" com as informações atualizadas
                users = updatedUsers;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }

        }

        public static String getLoginFromToken(String token) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                if (entry.getValue().contains(token)) {
                    return entry.getKey();
                }
            }
            return null;
        }

        public static String getPasswordFromLogin(String login) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                if (entry.getKey().equals(login)) {
                    return entry.getValue();
                }
            }
            return null;
        }
    }

