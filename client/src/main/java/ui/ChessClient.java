package ui;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ChessClient {

    private State userState = State.LOGGED_OUT;

    public void run() {
        System.out.println("👑 Welcome to 240 chess. Type Help to get started. 👑");
        Scanner scanner = new Scanner(System.in);

        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String input = scanner.nextLine();

            try {
                result = eval(input);
                System.out.printf("%s%s\n", RESET_TEXT_COLOR, result);
            } catch (Throwable e) {
                System.out.println(e.getMessage());
            }
        }

    }

    private void printPrompt() {

    }

    private String eval(String input) {
        var result = "Error with command. Try: Help";
        try {
            input = input.toLowerCase();
            var tokens = input.split(" ");
            if (tokens.length == 0) {
                tokens = new String[]{"Help"};
            }

            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            try {
                result = (String) this.getClass().getDeclaredMethod(tokens[0], String[].class).invoke(this, new Object[]{params});
            } catch (NoSuchMethodException e) {
                result = String.format("Unknown command\n%s", help(params));
            }
        } catch (Throwable e) {
            result = String.format("Error: %s", e);
        }
        return result;
    }


    private String help(String[] params) {
        return switch (userState) {
            case LOGGED_IN -> getHelp(loggedInHelp);
            case OBSERVING -> getHelp(ObservingHelp);
            case BLACK, WHITE -> getHelp(playingHelp);
            default -> getHelp(loggedOutHelp);
        };
    }


    private String quit(String[] params) {
        return "quit";
    }


    private String login(String[] params) {
        return "logged in";
    }

    private String register(String[] params) {
        return "registered";
    }

    private String logout(String[] ignore) {
        return "logout";
    }

    private String create(String[] params) {
        return "create";
    }

    private String list(String[] ignore) {
        return "list";
    }

    private String join(String[] params) {
        return "join";
    }

    private String observe(String[] params) {
        return "observe";
    }

    private String redraw(String[] params) {
        return "redraw";
    }

    private String legal(String[] params) {
        return "legal";
    }

    private String move(String[] params) {
        return "move";
    }

    private String leave(String[] params) {
        return "leave";
    }

    private String resign(String[] params) {
        return "resign";
    }


    private record Help(String cmd, String description) {
    }

    static final List<Help> loggedOutHelp = List.of(
            new Help("register <USERNAME> <PASSWORD> <EMAIL>", "to create an account"),
            new Help("login <USERNAME> <PASSWORD>", "to play chess"),
            new Help("quit", "playing chess"),
            new Help("help", "with possible commands")
    );

    static final List<Help> loggedInHelp = List.of(
            new Help("create <NAME>", "a game"),
            new Help("list", "games"),
            new Help("join <ID> [WHITE|BLACK]", "a game"),
            new Help("observe <ID>", "a game"),
            new Help("logout", "when you are done"),
            new Help("quit", "playing chess"),
            new Help("help", "with possible commands")
    );

    static final List<Help> ObservingHelp = List.of(
            new Help("legal", "moves for the current board"),
            new Help("redraw", "the board"),
            new Help("leave", "the game"),
            new Help("quit", "playing chess"),
            new Help("help", "with possible commands")
    );

    static final List<Help> playingHelp = List.of(
            new Help("redraw", "the board"),
            new Help("leave", "the game"),
            new Help("move <crcr> [q|r|b|n]", "a piece with optional promotion"),
            new Help("resign", "the game without leaving it"),
            new Help("legal <cr>", "moves a given piece"),
            new Help("quit", "playing chess"),
            new Help("help", "with possible commands")
    );

    private String getHelp(List<Help> help) {
        StringBuilder sb = new StringBuilder();
        for (var me : help) {
            sb.append(String.format("  %s%s%s - %s%s%s\n", SET_TEXT_COLOR_BLUE, me.cmd, RESET_TEXT_COLOR, SET_TEXT_COLOR_MAGENTA, me.description, RESET_TEXT_COLOR));
        }
        return sb.toString();

    }
}
