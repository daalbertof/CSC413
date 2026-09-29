package edu.sfsu.csc413.chess.model;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private final Piece[][] squares = new Piece[Position.BOARD_SIZE][Position.BOARD_SIZE];
    public Board(){
    }                                      // an empty board

    public Piece pieceAt(Position position){  // what is here? null if nothing
        return squares[position.file()][position.rank()];
    }

    public boolean isEmpty(Position position){
        return pieceAt(position) == null;
    }

    public void place(Position position, Piece piece){ // put this here (null clears)
        squares[position.file()][position.rank()] = piece;
    }

    public List<Position> positionsOf(Color color){// where are all of white's pieces?
        List<Position> positions = new ArrayList<>();
        for(int i = 0; i < Position.BOARD_SIZE; i++)
            for (int j = 0; j < Position.BOARD_SIZE; j++) {
                Position checking = new Position(i, j);
                if (pieceAt(checking) != null) {
                    if(pieceAt(checking).color() == color){
                        positions.add(checking);
                    }
                }
            }
        return positions;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();

        for (int rank = Position.BOARD_SIZE - 1; rank >= 0; rank--) {  // rank 8 first
            int empties = 0;
            for (int file = 0; file < Position.BOARD_SIZE; file++) {   // ... this rank's squares, file a to h: a letter per piece,
                Position checking = new Position(file, rank);
                if(isEmpty(checking)){
                    empties++;
                }else{
                    if(empties > 0){
                        text.append(empties);
                        empties = 0;
                    }
                    text.append(pieceAt(checking).symbol());
                }
            }
            if (empties > 0) {
                text.append(empties);
            }
            if (rank > 0) {
                text.append('/');
            }
        }
        return text.toString();
    }

}
