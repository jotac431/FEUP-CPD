import java.io.*;
import java.util.*;

public class Game implements Runnable {
    private final List<User> users = new ArrayList<>();
    private int numberOfPlayers;
    Map<String, String> questions = new HashMap<>();
    private boolean didGameEnd = false;

    public Game(List<User> users) {
        this.numberOfPlayers = 2;
        this.users.addAll(users);
    }

    @Override
    public void run() {        
        // before every game starts, wait 1 second
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
                DataBaseParse dataBaseParse = new DataBaseParse();
                questions = dataBaseParse.loadQuestionsFromFile("questions.txt");

                // generate a random question from the database
                Random random = new Random();
                List<String> keys = new ArrayList<>(questions.keySet());
                String question = keys.get(random.nextInt(keys.size()));
                String answer = questions.get(question);

                System.out.println("Starting game with " + numberOfPlayers + " players.");

                // send the question to all clients
                for (User user : users) {
                    DataOutputStream outputStream = new DataOutputStream(user.getOutputStream());
                    outputStream.writeUTF("Question: " + question + "\n" + "Answer: ");
                }

                // Create input and output streams for each client socket
                List<DataInputStream> inputStreams = new ArrayList<>();
                List<DataOutputStream> outputStreams = new ArrayList<>();
                for (User user : users) {
                    inputStreams.add(new DataInputStream(user.getInputStream()));
                    outputStreams.add(new DataOutputStream(user.getOutputStream()));
                }
                // Play the game;
                String[] guesses = new String[numberOfPlayers];
                boolean[] hasGuessed = new boolean[numberOfPlayers];
                int numberOfGuesses = 0;
                while (numberOfGuesses < numberOfPlayers) {
                    // Read guesses from clients
                    for (int i = 0; i < numberOfPlayers; i++) {
                        if (!hasGuessed[i]) {
                            String guess;
                            try {
                                guess = inputStreams.get(i).readUTF();
                                System.out.println("Player " + (i+1) + " guessed " + guess);
                                if(!answer.equals(guess)) {
                                    System.out.println("Wrong answer");
                                }
                                else {
                                    System.out.println("Correct answer");
                                }
                            } catch (IOException e) {
                                System.err.println("Failed to receive guess from player " + (i + 1) + ".");
                                hasGuessed[i] = true;
                                numberOfGuesses++;
                                continue;
                            }
                            guesses[i] = guess;
                            hasGuessed[i] = true;
                            numberOfGuesses++;
                        }
                    }
                }
                // Determine the winner
                int winnerIndex = -1;
                for (int i = 0; i < numberOfPlayers; i++) {
                    if (guesses[i].equals(answer)) {
                        winnerIndex = i;
                        break;
                    }
                }
            
                // Send result to all clients
                for (int i = 0; i < numberOfPlayers; i++) {
                    try {
                        if (i == winnerIndex) {
                            outputStreams.get(i).writeUTF("Congratulations, you won! The answer was " + answer + "." + "\n" + "You will be redirected to the queue.");
                            TriviaServer.queue.add(users.get(i));

                        } else {
                            outputStreams.get(i).writeUTF("Sorry, you lost. The answer was " + answer + ".");
                        }
                    } catch (IOException e) {
                        System.err.println("Failed to send game result to player " + (i + 1) + ".");
                    }
                }
        }

        catch (IOException e) {
            e.printStackTrace();
        } 
    }

}