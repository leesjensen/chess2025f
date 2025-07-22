package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import model.AuthData;
import model.GameData;
import service.MessageObserver;
import service.ServerFacade;
import utils.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static ui.EscapeSequences.*;

public class ChessClient implements MessageObserver {
    final private ServerFacade server;
    private State userState = State.LOGGED_OUT;
    private String authToken;
    private GameData currentGame;
    private List<GameData> games = new ArrayList<>();

    public ChessClient(String serverUrl) throws Exception {
        server = new ServerFacade(serverUrl, this);
    }

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
            result = e.getCause().getMessage();
        } catch (Throwable e) {
            result = e.getMessage();
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
            return "Already logged in";
        }
        var username = getStringParam("username", params, 0);
        var password = getStringParam("password", params, 1);

        AuthData authData = server.login(username, password);
        userState = State.LOGGED_IN;
        authToken = authData.authToken();
        return String.format("Logged in as %s", username);
    }

    private String register(String[] params) throws Exception {
        if (userState != State.LOGGED_OUT) {
            return "Already logged in";
        }
        var username = getStringParam("username", params, 0);
        var password = getStringParam("password", params, 1);
        var email = getStringParam("email", params, 2);

        AuthData authData = server.register(username, password, email);
        userState = State.LOGGED_IN;
        authToken = authData.authToken();
        return String.format("Logged in as %s", username);
    }

    private String logout(String[] ignore) throws Exception {
        verify(authenticated());

        server.logout(authToken);
        userState = State.LOGGED_OUT;
        authToken = null;
        return "Logged out";
    }

    private String create(String[] params) throws Exception {
        verify(authenticated());

        var gameName = getStringParam("game name", params, 0);
        server.createGame(authToken, gameName);
        return String.format("Created %s", gameName);
    }

    private String list(String[] params) throws Exception {
        verify(authenticated());

        var gameList = server.listGames(authToken);
        games = Arrays.stream(gameList).toList();

        if (!games.isEmpty()) {
            int pos = 1;
            StringBuilder buf = new StringBuilder("Games:\n———————————————————————————————\n");
            for (var game : games) {
                var gameText = String.format("%d. %s%n", pos, game.display());
                buf.append(gameText);
                pos++;
            }
            return buf.toString();
        }

        return "No games. Perhaps you would like to create one?";
    }

    private String join(String[] params) throws Exception {
        verify(authenticated() && !playing() && !observing());

        var game = getGame(params, 0);
        var color = getColor(params, 1);

        server.joinGame(authToken, game.gameID(), color);
        userState = (color == ChessGame.TeamColor.WHITE ? State.WHITE : State.BLACK);
        currentGame = game;

        return String.format("Joined %s as %s", game.gameName(), color);
    }

    private String observe(String[] params) throws Exception {
        verify(authenticated() && !playing() && !observing());

        var game = getGame(params, 0);
        server.observeGame(authToken, game.gameID());
        userState = State.OBSERVING;
        currentGame = game;

        return String.format("Joined %d as observer", game.gameID());
    }

    private String redraw(String[] params) throws Exception {
        verify(playing() || observing());

        printGame();
        return "";
    }

    private String legal(String[] params) throws Exception {
        verify(playing() || observing());

        var pos = new ChessPosition(params[0]);
        var highlights = new ArrayList<ChessPosition>();
        highlights.add(pos);
        for (var move : currentGame.game().validMoves(pos)) {
            highlights.add(move.getEndPosition());
        }

        printGame(highlights);
        return "";
    }

    private String move(String[] params) throws Exception {
        verify(playing());

        var move = new ChessMove(getStringParam("move", params, 0));
        server.makeMove(authToken, currentGame.gameID(), move);
        return String.format("moved %s", move);
    }

    private String leave(String[] params) throws Exception {
        verify(playing() || observing());

        userState = State.LOGGED_IN;
        currentGame = null;
        return "Left game";
    }

    private String resign(String[] params) throws Exception {
        verify(playing());

        userState = State.LOGGED_IN;
        currentGame = null;
        return "Resigned game";
    }

    @Override
    public void notify(String message) {
        System.out.println(message);
    }

    public void loadGame(GameData gameData) {
        currentGame = gameData;
        printGame();

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
            new Help("legal <cr>", "moves for piece"),
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

    private void verify(boolean expected) throws Exception {
        if (!expected) {
            throw new Exception("Bad request");
        }
    }

    private boolean authenticated() {
        return (userState != State.LOGGED_OUT && authToken != null);
    }

    public boolean playing() {
        return (authenticated() && currentGame != null && (userState == State.WHITE || userState == State.BLACK) && !gameOver());
    }


    public boolean observing() {
        return (authenticated() && currentGame != null && (userState == State.OBSERVING));
    }

    public boolean gameOver() {
        return (currentGame != null && currentGame.isGameOver());
    }

    public boolean isMyTurn() {
        return (playing() && userState.isTurn(currentGame.game().getTeamTurn()));
    }

    private void printGame() {
        printGame(null);
    }

    private void printGame(Collection<ChessPosition> highlights) {
        var color = userState == State.BLACK ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        System.out.println("\n");
        System.out.print((currentGame.game().getBoard()).toString(color, highlights));
        System.out.println();
        System.out.printf("%s%n", currentGame.description());
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
            throw new Exception("invalid game requested");
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
