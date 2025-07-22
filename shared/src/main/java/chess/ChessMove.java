package chess;

import java.util.Locale;
import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    private final ChessPosition startPosition;
    private final ChessPosition endPosition;
    private final ChessPiece.PieceType promotionPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }


    public ChessMove(String notation) throws Exception {
        notation = notation.toLowerCase(Locale.ROOT);
        if (notation.length() >= 4) {
            int colStart = notation.charAt(0) - 'a' + 1;
            int rowStart = notation.charAt(1) - '1' + 1;
            int colEnd = notation.charAt(2) - 'a' + 1;
            int rowEnd = notation.charAt(3) - '1' + 1;

            startPosition = new ChessPosition(rowStart, colStart);
            endPosition = new ChessPosition(rowEnd, colEnd);
            if (notation.length() == 5) {
                promotionPiece = switch (notation.charAt(4)) {
                    case 'q' -> ChessPiece.PieceType.QUEEN;
                    case 'b' -> ChessPiece.PieceType.BISHOP;
                    case 'n' -> ChessPiece.PieceType.KNIGHT;
                    case 'r' -> ChessPiece.PieceType.ROOK;
                    default -> null;
                };
            } else {
                promotionPiece = null;
            }
            return;
        }
        throw new Exception("Invalid move notation");
    }


    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }

    @Override
    public String toString() {
        return String.format("%s%s", startPosition, endPosition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessMove chessMove)) {
            return false;
        }
        return Objects.equals(startPosition, chessMove.startPosition) && Objects.equals(endPosition, chessMove.endPosition) && promotionPiece == chessMove.promotionPiece;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPosition, endPosition, promotionPiece);
    }
}
