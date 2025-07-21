import ui.ChessClient;


public class Main {
    public static void main(String[] args) {
        try {
            new ChessClient("http://localhost:8080").run();
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e);
        }
    }
}