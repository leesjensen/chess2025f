package chess.rules;

import chess.ChessPiece;

import java.util.HashMap;

import static chess.ChessPiece.PieceType.*;

public class Rules {
    static private final HashMap<ChessPiece.PieceType, MovementRule> RULES = new HashMap<>();

    static {
        RULES.put(KING, new KingMovementRule());
        RULES.put(QUEEN, new QueenMovementRule());
        RULES.put(KNIGHT, new KnightMovementRule());
        RULES.put(BISHOP, new BishopMovementRule());
        RULES.put(ROOK, new RookMovementRule());
        RULES.put(PAWN, new PawnMovementRule());
    }

    static public MovementRule movementRule(ChessPiece.PieceType type) {
        return RULES.get(type);
    }

}
