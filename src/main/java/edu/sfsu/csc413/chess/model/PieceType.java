package edu.sfsu.csc413.chess.model;

public enum PieceType {
    PAWN('P'), KNIGHT('N'), BISHOP('B'), ROOK('R'), QUEEN('Q'), KING('K');

    private final char symbol;

    PieceType(char symbol) {
        this.symbol = symbol;
    }

    /** The uppercase letter for this type, as used in FEN and algebraic notation. */
    public char symbol() {
        return symbol;
    }

    /** The inverse: the type for a letter, in either case. Throws if it names no piece. */
    public static PieceType fromSymbol(char letter) {
        char upperCase = Character.toUpperCase(letter);

        for(PieceType piece : PieceType.values()){
            if(piece.symbol() == upperCase){
                return piece;
            }
        }
        throw new  IllegalArgumentException("Symbol invalid: " + letter);
    }
}
