package chess;

import java.util.Locale;
import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private final int row;
    private final int col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }


    public ChessPosition(String notation) throws Exception {
        notation = notation.toLowerCase(Locale.ROOT);
        if (notation.length() == 2) {
            col = notation.charAt(0) - 'a' + 1;
            row = notation.charAt(1) - '1' + 1;
            if (col >= 1 && col <= 8 && row >= 1 && row <= 8) {
                return;
            }
        }
        throw new Exception("Invalid notation. Must be like A2");
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return col;
    }

    @Override
    public String toString() {
        return String.format("%c%d", 'a' + (col-1), row);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessPosition that)) {
            return false;
        }
        return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
