package service;

import chess.ChessGame;

public record JoinGameReq(
        ChessGame.TeamColor playerColor,
        int gameID
) {
}