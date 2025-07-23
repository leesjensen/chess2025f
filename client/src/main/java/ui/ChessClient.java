package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import model.AuthData;
import model.GameData;
import service.MessageObserver;
import service.ServerFacade;
import utils.StringUtils;

import java.util.*;

import static ui.EscapeSequences.*;

public class ChessClient implements MessageObserver {
    final private ServerFacade server;
    private State playerState = State.LOGGED_OUT;
    private String authToken;
    private GameData currentGame;
    private List<GameData> games = new ArrayList<>();

    record CommandInfo(String name, Command cmd, String syntax, String description) {
    }

    interface Command {
        String invoke(String[] params) throws Exception;
    }

    private final Map<String, CommandInfo> commands = new HashMap<>();

    public ChessClient(String serverUrl) throws Exception {
        server = new ServerFacade(serverUrl, this);

        final CommandInfo[] commandList = {
                new CommandInfo("help", this::help, "help", "with possible commands"),
                new CommandInfo("quit", this::quit, "quit", "playing chess"),
                new CommandInfo("login", this::login, "login <USERNAME> <PASSWORD>", "to play chess"),
                new CommandInfo("register", this::register, "register <USERNAME> <PASSWORD> <EMAIL>", "to create an account"),
                new CommandInfo("logout", this::logout, "logout", "when you are done"),
                new CommandInfo("create", this::create, "create <NAME>", "a game"),
                new CommandInfo("list", this::list, "list", "games"),
                new CommandInfo("join", this::join, "join <POSITION> [WHITE|BLACK]", "a game"),
                new CommandInfo("observe", this::observe, "observe <ID>", "a game"),
                new CommandInfo("redraw", this::redraw, "redraw", "the board"),
                new CommandInfo("legal", this::legal, "legal", "moves for the current board"),
                new CommandInfo("move", this::move, "move <crcr> [q|r|b|n]", "a piece with optional promotion"),
                new CommandInfo("leave", this::leave, "leave", "the game"),
                new CommandInfo("resign", this::resign, "resign", "the game without leaving it")
        };

        for (var cmd : commandList) {
            commands.put(cmd.name(), cmd);
        }
    }

    public void run() {
        System.out.println("👑 Welcome to 240 chess. Type Help to get started. 👑");
        Scanner scanner = new Scanner(System.in);

        var keepRunning = true;
        while (keepRunning) {
            printPrompt();
            String input = scanner.nextLine();

            keepRunning = eval(input);
        }

    }

    private void printPrompt() {
        System.out.printf("%s[%s]>%s ", SET_TEXT_COLOR_GREEN, playerState, RESET_TEXT_COLOR);
    }

    private boolean eval(String input) {
        CommandInfo cmdInfo = null;
        try {
            input = input.toLowerCase();
            var tokens = input.split(" ");
            if (tokens.length == 0) {
                tokens = new String[]{"Help"};
            }

            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            cmdInfo = commands.get(tokens[0]);
            if (cmdInfo == null) {
                cmdInfo = commands.get("help");
            }

            String result = cmdInfo.cmd.invoke(params);
            System.out.printf("%s%s\n", RESET_TEXT_COLOR, result);
        } catch (Throwable e) {
            System.out.printf("%s%s\n", RESET_TEXT_COLOR, e.getMessage());
        }
        return cmdInfo == null || !cmdInfo.name.equals("quit");
    }


    private String help(String[] ignoredParams) {
        final List<String> loggedOutHelp = List.of("register", "login", "quit", "help");
        final List<String> loggedInHelp = List.of("create", "list", "join", "observe", "logout", "quit", "help");
        final List<String> observingHelp = List.of("legal", "redraw", "leave", "quit", "help");
        final List<String> playingHelp = List.of("redraw", "leave", "move", "resign", "legal", "quit", "help");

        return switch (playerState) {
            case LOGGED_IN -> getHelp(loggedInHelp);
            case OBSERVING -> getHelp(observingHelp);
            case BLACK, WHITE -> getHelp(playingHelp);
            default -> getHelp(loggedOutHelp);
        };
    }

    private String getHelp(List<String> helpList) {
        StringBuilder sb = new StringBuilder();
        for (var helpItem : helpList) {
            CommandInfo cmdInfo = commands.get(helpItem);
            sb.append(String.format("  %s%s%s - %s%s%s\n", SET_TEXT_COLOR_BLUE, cmdInfo.syntax(), RESET_TEXT_COLOR, SET_TEXT_COLOR_MAGENTA, cmdInfo.description(), RESET_TEXT_COLOR));
        }
        return sb.toString();
    }


    private String quit(String[] ignoredParams) {
        return "Thanks for playing!";
    }


    private String login(String[] params) throws Exception {
        if (playerState != State.LOGGED_OUT) {
            return "Already logged in";
        }
        var username = getStringParam("username", params, 0);
        var password = getStringParam("password", params, 1);

        AuthData authData = server.login(username, password);
        playerState = State.LOGGED_IN;
        authToken = authData.authToken();
        return String.format("Logged in as %s%n%n%s", username, list(params));
    }

    private String register(String[] params) throws Exception {
        if (playerState != State.LOGGED_OUT) {
            return "Already logged in";
        }
        var username = getStringParam("username", params, 0);
        var password = getStringParam("password", params, 1);
        var email = getStringParam("email", params, 2);

        AuthData authData = server.register(username, password, email);
        playerState = State.LOGGED_IN;
        authToken = authData.authToken();
        list(params);
        return String.format("Registered in as %s%n%n%s", username, list(params));
    }

    private String logout(String[] ignoredParams) throws Exception {
        verify(authenticated(), "Not logged in");

        server.logout(authToken);
        playerState = State.LOGGED_OUT;
        authToken = null;
        return "Logged out";
    }

    private String create(String[] params) throws Exception {
        verify(authenticated(), "Not logged in");

        var gameName = getStringParam("game name", params, 0);
        server.createGame(authToken, gameName);

        return String.format("Created %s%n%s", gameName, list(params));

    }

    private String list(String[] ignoredParams) throws Exception {
        verify(authenticated(), "Not logged in");

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
        verify(authenticated() && !playing() && !observing(), "Cannot join game if not logged in or already in a game");

        var game = getGame(params);
        var color = getColor(params);

        server.joinGame(authToken, game.gameID(), color);
        playerState = (color == ChessGame.TeamColor.WHITE ? State.WHITE : State.BLACK);
        currentGame = game;

        return String.format("Joined %s as %s", game.gameName(), color);
    }

    private String observe(String[] params) throws Exception {
        verify(authenticated() && !playing() && !observing(), "Cannot join game if not logged in or already in a game");

        var game = getGame(params);
        server.observeGame(authToken, game.gameID());
        playerState = State.OBSERVING;
        currentGame = game;

        return String.format("Joined %d as observer", game.gameID());
    }

    private String redraw(String[] ignoredParams) throws Exception {
        verify(gameOver() || playing() || observing(), "Not in a game");

        printGame();
        return "";
    }

    private String legal(String[] params) throws Exception {
        verify(gameOver() || playing() || observing(), "Not in a game");

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
        verify(playing() && isMyTurn(), "Not your turn");

        var move = new ChessMove(getStringParam("move", params, 0));
        server.makeMove(authToken, currentGame.gameID(), move);
        return String.format("moved %s", move);
    }

    private String leave(String[] ignoredParams) throws Exception {
        verify(gameOver() || playing() || observing(), "Not in a game");

        playerState = State.LOGGED_IN;
        currentGame = null;
        return "Left game";
    }

    private String resign(String[] ignoredParams) throws Exception {
        verify(playing(), "Not playing a game");

        playerState = State.LOGGED_IN;
        currentGame = null;
        return "Resigned game";
    }

    @Override
    public void notify(String message) {
        System.out.printf("%s[NOTIFICATION] %s%s%n", SET_TEXT_COLOR_BLUE, message, RESET_TEXT_COLOR);
        printPrompt();
    }

    public void loadGame(GameData gameData) {
        currentGame = gameData;
        printGame();
        printPrompt();
    }

    private void verify(boolean expected, String message) throws Exception {
        if (!expected) {
            throw new Exception(message);
        }
    }

    private boolean authenticated() {
        return (playerState != State.LOGGED_OUT && authToken != null);
    }

    public boolean playing() {
        return (authenticated() && currentGame != null && (playerState == State.WHITE || playerState == State.BLACK) && !gameOver());
    }


    public boolean observing() {
        return (authenticated() && currentGame != null && (playerState == State.OBSERVING));
    }

    public boolean gameOver() {
        return (currentGame != null && currentGame.isGameOver());
    }

    public boolean isMyTurn() {
        return (playing() && playerState.isTurn(currentGame.game().getTeamTurn()));
    }

    private void printGame() {
        printGame(null);
    }

    private void printGame(Collection<ChessPosition> highlights) {
        var color = playerState == State.BLACK ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        System.out.println("\n");
        System.out.print((currentGame.game().getBoard()).toString(color, highlights));
        System.out.println();
        System.out.printf("[GAME STATE] %s%n", currentGame.description());
    }

    private String getStringParam(String name, String[] params, int pos) throws Exception {
        if (params.length <= pos) {
            throw new Exception(String.format("Missing %s parameter", name));
        }
        return params[pos];
    }

    private GameData getGame(String[] params) throws Exception {
        var gamePos = getGamePos(params) - 1;
        if (gamePos >= 0 && gamePos >= games.size()) {
            throw new Exception("invalid game requested");
        }

        return games.get(gamePos);
    }


    private int getGamePos(String[] params) throws Exception {
        if (params.length == 0) {
            throw new Exception("Missing game pos parameter");
        }

        var result = StringUtils.tryParseInt(params[0]);
        if (result.isEmpty()) {
            throw new Exception("Parameter game pos is not an int");
        }
        return result.getAsInt();
    }

    private ChessGame.TeamColor getColor(String[] params) throws Exception {
        var colorText = getStringParam("color", params, 1).toUpperCase();
        if (!colorText.equals("WHITE") && !colorText.equals("BLACK")) {
            throw new Exception("color must be black or white");
        }
        return ChessGame.TeamColor.valueOf(colorText);
    }
}
