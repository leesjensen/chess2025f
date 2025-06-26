package chess.rules;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class PawnMovementRule extends MovementRule {
    @Override
    public Collection<ChessMove> moves(ChessBoard board, ChessPosition pos) {
        var pieceColor = board.getPiece(pos).getTeamColor();

        var moves = new HashSet<ChessMove>();
        var direction = (pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1);

        calculateMoves(board, pos, direction, 0, moves, false);
        calculateMoves(board, pos, direction, -1, moves, true);
        calculateMoves(board, pos, direction, 1, moves, true);

        if (pieceColor == ChessGame.TeamColor.WHITE && pos.getRow() == 2 || pieceColor == ChessGame.TeamColor.BLACK && pos.getRow() == 7) {
            if (board.isSquareEmpty(pos.getRow() + direction, pos.getColumn()) && board.isSquareEmpty(pos.getRow() + (direction * 2), pos.getColumn())) {
                moves.add(new ChessMove(pos, new ChessPosition(pos.getRow() + (direction * 2), pos.getColumn()), null));
            }
        }

        return moves;
    }

    @Override
    protected void calculateMoves(ChessBoard board, ChessPosition pos, int rowInc, int colInc, HashSet<ChessMove> moves, boolean attack) {
        var pieceColor = board.getPiece(pos).getTeamColor();
        int row = pos.getRow() + rowInc;
        int col = pos.getColumn() + colInc;
        if (row > 0 && col > 0 && row < 9 && col < 9) {
            var newPos = new ChessPosition(row, col);
            var pieceAt = board.getPiece(newPos);
            if ((attack && pieceAt != null && pieceAt.getTeamColor() != pieceColor)
                    || (!attack && pieceAt == null)) {
                addMoveWithPossiblePromotion(pos, newPos, moves);
            }
        }
    }

    private void addMoveWithPossiblePromotion(ChessPosition pos, ChessPosition newPos, HashSet<ChessMove> moves) {
        if (newPos.getRow() == 1 || newPos.getRow() == 8) {
            moves.add(new ChessMove(pos, newPos, ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(pos, newPos, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(pos, newPos, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(pos, newPos, ChessPiece.PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(pos, newPos, null));
        }
    }
}