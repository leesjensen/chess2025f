package ui;

import chess.ChessGame;
import chess.ChessPosition;
import model.GameData;
import utils.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static ui.EscapeSequences.*;

public class ChessClient {

    private State userState = State.LOGGED_OUT;
    private String username = "";
    private String authToken;
    private GameData gameData;
    private ArrayList<GameData> games = new ArrayList<>();

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
        } catch (InvocationTargetException e) {
            result = String.format("Error: %s", e.getCause().getMessage());
        } catch (Throwable e) {
            result = String.format("Error: %s", e.getMessage());
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


    private String login(String[] params) throws Exception {
        if (userState != State.LOGGED_OUT) {
            return "Must be logged out";
        }
        var username = getStringParam("username", params, 0);
        var password = getStringParam("password", params, 1);

        userState = State.LOGGED_IN;
        authToken = "x";
        this.username = username;
        return String.format("Logged in as %s", username);
    }

    private String register(String[] params) throws Exception {
        if (userState != State.LOGGED_OUT) {
            return "Must be logged out";
        }
        var username = getStringParam("username", params, 0);
        var password = getStringParam("password", params, 1);
        var email = getStringParam("email", params, 2);

        userState = State.LOGGED_IN;
        authToken = "x";
        this.username = username;
        return String.format("Logged in as %s", username);
    }

    private String logout(String[] ignore) throws Exception {
        verifyAuth();

        userState = State.LOGGED_OUT;
        authToken = null;
        this.username = "";
        return "Logged out";
    }

    private String create(String[] params) throws Exception {
        verifyAuth();
        var gameName = getStringParam("game name", params, 0);

        var game = new GameData(300, "", "", gameName, new ChessGame(), GameData.State.UNDECIDED);
        games.add(game);

        return String.format("Created %s", gameName);
    }

    private String list(String[] ignore) throws Exception {
        verifyAuth();

        int pos = 1;
        StringBuilder buf = new StringBuilder("games\n--------------------\n");
        for (var game : games) {
            var gameText = String.format("%d. %s white:%s black:%s state: %s%n", pos, game.gameName(), game.whiteUsername(), game.blackUsername(), game.state());
            buf.append(gameText);
            pos++;
        }
        return buf.toString();
    }

    private String join(String[] params) throws Exception {
        verifyAuth();
        var game = getGame(params, 0);
        var color = getColor(params, 1);
        if (isPlaying() || isObserving()) {
            throw new Exception("Already in game");
        }

        if (color == ChessGame.TeamColor.WHITE) {
            game = game.setWhite(username);
            userState = State.WHITE;
        } else {
            game = game.setBlack(username);
            userState = State.BLACK;
        }

        this.gameData = game;
        return String.format("Joined %d as %s", game.gameID(), color);
    }

    private String observe(String[] params) throws Exception {
        verifyAuth();
        var game = getGame(params, 0);
        if (isPlaying() || isObserving()) {
            throw new Exception("Already in game");
        }

        userState = State.OBSERVING;
        this.gameData = game;
        return String.format("Joined %d as observer", game.gameID());
    }

    private String redraw(String[] params) throws Exception {
        verifyAuth();
        if (!isPlaying() && !isObserving()) {
            throw new Exception("No game being played");
        }

        printGame();
        return "";
    }

    private String legal(String[] params) throws Exception {
        verifyAuth();
        if (!isPlaying() && !isObserving()) {
            throw new Exception("No game being played");
        }

        printGame();
        return "";
    }

    private String move(String[] params) throws Exception {
        verifyAuth();
        if (!isPlaying()) {
            throw new Exception("No game being played");
        }
        var move = getStringParam("move", params, 0);
        return String.format("move %s", move);
    }

    private String leave(String[] params) throws Exception {
        if (!isPlaying() && !isObserving()) {
            throw new Exception("No game being played");
        }

        userState = State.LOGGED_IN;
        gameData = null;
        return "Left game";
    }

    private String resign(String[] params) throws Exception {
        if (!isPlaying() && !isObserving()) {
            throw new Exception("No game being played");
        }

        userState = State.LOGGED_IN;
        gameData = null;
        return "Left game";
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
            new Help("join <POSITION> [WHITE|BLACK]", "a game"),
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

    private void verifyAuth() throws Exception {
        if (userState == State.LOGGED_OUT || authToken == null) {
            throw new Exception("Please login or register");
        }
    }

    public boolean isPlaying() {
        return (gameData != null && (userState == State.WHITE || userState == State.BLACK) && !isGameOver());
    }


    public boolean isObserving() {
        return (gameData != null && (userState == State.OBSERVING));
    }

    public boolean isGameOver() {
        return (gameData != null && gameData.isGameOver());
    }

    public boolean isTurn() {
        return (isPlaying() && userState.isTurn(gameData.game().getTeamTurn()));
    }

    private void printGame() {
        var color = userState == State.BLACK ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        printGame(color, null);
    }

    private void printGame(ChessGame.TeamColor color, Collection<ChessPosition> highlights) {
        System.out.println("\n");
        System.out.print((gameData.game().getBoard()).toString(color, highlights));
        System.out.println();
    }

    private String getStringParam(String name, String[] params, int pos) throws Exception {
        if (params.length <= pos) {
            throw new Exception(String.format("Missing %s parameter", name));
        }
        return params[pos];
    }

    private int getIntParam(String name, String[] params, int pos) throws Exception {
        if (params.length <= pos) {
            throw new Exception(String.format("Missing %s parameter", name));
        }

        var result = StringUtils.tryParseInt(params[pos]);
        if (result.isEmpty()) {
            throw new Exception(String.format("Parameter %s is not an int", name));
        }
        return result.getAsInt();
    }

    private GameData getGame(String[] params, int pos) throws Exception {
        var gamePos = getIntParam("game Pos", params, 0) - 1;
        if (gamePos >= 0 && gamePos >= games.size()) {
            throw new Exception("invalid game position");
        }

        return games.get(gamePos);
    }

    private ChessGame.TeamColor getColor(String[] params, int pos) throws Exception {
        var colorText = getStringParam("color", params, 1).toUpperCase();
        if (!colorText.equals("WHITE") && !colorText.equals("BLACK")) {
            throw new Exception("color must be black or white");
        }
        return ChessGame.TeamColor.valueOf(colorText);
    }
}
