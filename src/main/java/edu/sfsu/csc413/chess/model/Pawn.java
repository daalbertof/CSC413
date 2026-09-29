package edu.sfsu.csc413.chess.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The pawn — the piece that breaks every rule the others follow.
 *
 * <p>It is the only piece that moves in just one direction, the only one whose
 * capture differs from its move, the only one with a special first move, and
 * the only one that turns into something else. It is worth noticing that all of
 * that awkwardness is contained in this one file. No other class in the engine
 * knows that pawns are strange. That containment is the payoff of polymorphism:
 * the irregular case costs one class, not a special case in every method that
 * touches a piece.
 *
 * <p>En passant is not handled here. Like castling, it depends on the previous
 * move rather than on the current board, so it waits for Week 15 when
 * {@code Game} owns the move history.
 */
public class Pawn extends Piece {

    /**
     * What a pawn may become on reaching the far rank.
     */
    private static final PieceType[] PROMOTION_CHOICES = { PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT };

    public Pawn(Color color) {
        super(color, PieceType.PAWN);
    }

    @Override
    public List<Move> pseudoLegalMoves(Board board, Position from) {
        List<Move> moves = new ArrayList<>();
        int direction = color().pawnDirection();
        Position oneMove = from.offsetOrNull(0, direction);
        Position twoMove = from.offsetOrNull(0, direction * 2);
        if (oneMove != null && board.pieceAt(oneMove) == null) {
            if (oneMove.rank() == color().promotionRank()) {
                for (PieceType chosenPiece : PROMOTION_CHOICES) {
                    moves.add(Move.promotion(from, oneMove, this, null, chosenPiece));
                }
            } else {
                moves.add(Move.quiet(from, oneMove, this));
                if (from.rank() == color().pawnStartRank()) {
                    if (twoMove != null && board.pieceAt(twoMove) == null) {
                        moves.add(Move.quiet(from, twoMove, this));
                    }
                }
            }
        }
        Position rightMove = from.offsetOrNull(1, direction);
        Position leftMove = from.offsetOrNull(-1, direction);
        if (rightMove != null){
            Piece piece = board.pieceAt(rightMove);
            if (piece != null && piece.color() != this.color()) {
                if (rightMove.rank() == color().promotionRank()) {
                    for (PieceType chosenPiece : PROMOTION_CHOICES) {
                        moves.add(Move.promotion(from, rightMove, this, piece, chosenPiece));
                    }
                } else {
                    moves.add(Move.capture(from, rightMove, this, piece));
                }
            }
        }
        if (leftMove != null){
            Piece piece = board.pieceAt(leftMove);

            if (piece != null && piece.color() != this.color()) {
                if (leftMove.rank() == color().promotionRank()) {
                    for (PieceType chosenPiece : PROMOTION_CHOICES) {
                        moves.add(Move.promotion(from, leftMove, this, piece, chosenPiece));
                    }
                } else {
                    moves.add(Move.capture(from, leftMove, this, piece));
                }
            }
        }
        return moves;
    }

    /**
     * A pawn attacks the two squares diagonally ahead of it, whether or not
     * anything stands there.
     *
     * <p>This override exists because the inherited version answers "can this
     * piece move to that square", and for a pawn that is the wrong question.
     * An empty square in front of a pawn is a square the pawn can move to but
     * does <em>not</em> attack — which matters enormously for king safety: a
     * king may not be blocked from a square merely because a pawn could advance
     * onto it, but it certainly may not step onto a square a pawn guards.
     */
    @Override
    public boolean attacks(Board board, Position from, Position target) {
        int direction = color().pawnDirection();
        Position rightMove = from.offsetOrNull(1,direction);
        Position leftMove = from.offsetOrNull(-1,direction);
        if(target.equals(rightMove) || target.equals(leftMove)) {
            return true;
        }
        return false;
    }
}
