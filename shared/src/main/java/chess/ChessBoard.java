package chess;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static chess.ChessGame.TeamColor;
import static chess.ChessPiece.PieceType;


/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {

    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        var pieces = new PieceType[]{
                PieceType.ROOK,
                PieceType.KNIGHT,
                PieceType.BISHOP,
                PieceType.QUEEN,
                PieceType.KING,
                PieceType.BISHOP,
                PieceType.KNIGHT,
                PieceType.ROOK
        };
        for (var i = 0; i < 8; i++) {
            squares[0][i] = new ChessPiece(TeamColor.WHITE, pieces[i]);
            squares[1][i] = new ChessPiece(TeamColor.WHITE, PieceType.PAWN);
            squares[2][i] = null;
            squares[3][i] = null;
            squares[4][i] = null;
            squares[5][i] = null;
            squares[6][i] = new ChessPiece(TeamColor.BLACK, PieceType.PAWN);
            squares[7][i] = new ChessPiece(TeamColor.BLACK, pieces[i]);
        }
    }


    public boolean isSquareEmpty(int row, int col) {
        var pieceAt = getPiece(new ChessPosition(row, col));
        return pieceAt == null;
    }

    private static final int BLACK = 0;
    private static final int RED = 1;
    private static final int GREEN = 2;
    private static final int YELLOW = 3;
    //    private static final int BLUE = 4;
    private static final int MAGENTA = 5;
    //    private static final int CYAN = 6;
    private static final int WHITE = 7;

    private static final String COLOR_RESET = "\u001b[0m";

    /**
     * Set both the foreground and background color. Foreground is 3, background is 4.
     */
    private static String color(int FG, int BG) {
        return String.format("\u001b[3%d;4%dm", FG, BG);
    }

    /**
     * Set the foreground color.
     */
    private static String color(int FG) {
        return String.format("\u001b[1;3%dm", FG);
    }

    private static final String BORDER = color(BLACK, YELLOW);

    private static final String BOARD_BLACK = color(WHITE, BLACK);
    private static final String BOARD_WHITE = color(BLACK, WHITE);
    private static final String BOARD_HIGHLIGHT = color(GREEN, MAGENTA);

    private static final String BLACK_PIECE = color(RED);
    private static final String WHITE_PIECE = color(GREEN);

    private static final String EMPTY_SQUARE = "   ";
    private static final String SPACE = " ";
    private static final String NEW_ROW = "\n";

    private static final Map<PieceType, String> pieceMap = Map.of(
            PieceType.KING, "K",
            PieceType.QUEEN, "Q",
            PieceType.BISHOP, "B",
            PieceType.KNIGHT, "N",
            PieceType.ROOK, "R",
            PieceType.PAWN, "P"
    );

    @Override
    public String toString() {
        return toString(TeamColor.WHITE, null);
    }


    public String toString(TeamColor playerColor, Collection<ChessPosition> highlights) {
        var sb = new StringBuilder();
        var rows = new int[]{7, 6, 5, 4, 3, 2, 1, 0};
        var columns = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
        var columnsLetters = "    a  b  c  d  e  f  g  h    ";

        if (playerColor == TeamColor.BLACK) {
            columnsLetters = "    h  g  f  e  d  c  b  a    ";
            rows = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
            columns = new int[]{7, 6, 5, 4, 3, 2, 1, 0};
        }
        sb.append(BORDER).append(columnsLetters).append(COLOR_RESET).append("\n");
        for (var i : rows) {
            var row = SPACE + (i + 1) + SPACE;
            sb.append(BORDER).append(row).append(COLOR_RESET);
            for (var j : columns) {
                var squareColor = ((i + j) % 2 == 0 ? BOARD_BLACK : BOARD_WHITE);
                if (highlights != null && highlights.contains(new ChessPosition(i + 1, j + 1))) {
                    squareColor = BOARD_HIGHLIGHT;
                }
                var piece = squares[i][j];
                sb.append(renderSquare(piece, squareColor));
            }
            sb.append(BORDER).append(row).append(COLOR_RESET);
            sb.append(NEW_ROW);
        }
        sb.append(BORDER).append(columnsLetters).append(COLOR_RESET).append(NEW_ROW);
        return sb.toString();
    }

    private String renderSquare(ChessPiece piece, String squareColor) {
        if (piece == null) {
            return squareColor + EMPTY_SQUARE + COLOR_RESET;
        }
        String color = (piece.getTeamColor() == TeamColor.WHITE) ? WHITE_PIECE : BLACK_PIECE;
        String symbol = pieceMap.get(piece.getPieceType());
        return squareColor + color + SPACE + symbol + SPACE + COLOR_RESET;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessBoard that)) {
            return false;
        }
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }
}
