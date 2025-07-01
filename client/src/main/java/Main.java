import ui.ChessClient;


public class Main {
    public static void main(String[] args) {
        try {
            new ChessClient().run();
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e);
        }
    }
}