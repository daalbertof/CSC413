package edu.sfsu.csc413.chess.model;
import java.util.ArrayList;
import java.util.List;

public abstract class Piece {
    private final Color color;
    private final PieceType type;

    protected Piece(Color color, PieceType type){
        this.color =  color;
        this.type = type;
    }

    public Color color() {
        return color;
    }

    public PieceType type() {
        return type;
    }
    public abstract List<Move> pseudoLegalMoves(Board board, Position from);

    public boolean attacks(Board board, Position from, Position target) {
        for(Move move : pseudoLegalMoves(board,from)){
            if(move.to().equals(target)){
                return true;
            }
        }
        return false;
    }

    protected List<Move> slidingMoves(Board board, Position from, int[][] directions) {
        List<Move> list = new ArrayList<>();

        for (int i = 0; i < directions.length; i++) {
            int dx = directions[i][0];
            int dy = directions[i][1];

            Position current = from.offsetOrNull(dx, dy);
            while (current != null) {
                Piece destinationPiece = board.pieceAt(current);
                if (destinationPiece == null) {
                    list.add(Move.quiet(from, current, this));
                }
                else if (destinationPiece.color() != this.color()) {
                    list.add(Move.capture(from, current, this, destinationPiece));
                    break;
                }
                else {
                    break;
                }
                current = current.offsetOrNull(dx, dy);
            }
        }
        return list;
    }

    protected List<Move> steppingMoves(Board board, Position from, int[][] offsets){
        List<Move> list = new ArrayList<>();
        for(int i = 0; i < offsets.length; i++){
            Position off = from.offsetOrNull(offsets[i][0],offsets[i][1]);

            if(off == null){
                continue;
            }
            Piece where = board.pieceAt(off);
            if(where == null){
                list.add(Move.quiet(from,off,this));
            }else if(where.color() != this.color()){
                list.add(Move.capture(from,off,this, where));
            }
        }
        return list;
    }

    public char symbol() {
        char letter = type.symbol();
        return color == Color.WHITE ? letter : Character.toLowerCase(letter);
    }

    @Override
    public String toString() {
        return String.valueOf(symbol());
    }
}
