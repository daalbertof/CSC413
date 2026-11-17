package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.model.Position;

import java.io.PrintStream;
import java.util.Scanner;
import java.util.stream.Collectors;

/** A terminal view with a bordered board and a move-history display. */
public class TerminalView implements BoardView {

    private final PrintStream out;
    private final Scanner in;

    public TerminalView() {
        this(System.out, new Scanner(System.in));
    }

    public TerminalView(PrintStream out, Scanner in) {
        this.out = out;
        this.in = in;
    }

    @Override
    public void render(Game game) {
        out.println();
        out.println("    +----+----+----+----+----+----+----+----+");
        for (int rank = Position.BOARD_SIZE - 1; rank >= 0; rank--) {
            out.printf(" %d  |", rank + 1);
            for (int file = 0; file < Position.BOARD_SIZE; file++) {
                var piece = game.board().pieceAt(new Position(file, rank));
                String symbol = piece == null ? " " : String.valueOf(piece.symbol());
                out.printf(" %s  |", symbol);
            }
            out.println();
            out.println("    +----+----+----+----+----+----+----+----+");
        }
        out.println("       a    b    c    d    e    f    g    h");
        out.println(game.sideToMove() + " to move | " + game.status());
        String history = game.history().stream()
                .map(Object::toString)
                .collect(Collectors.joining(" "));
        out.println("Moves: " + (history.isEmpty() ? "(none)" : history));
    }

    @Override
    public void showMessage(String message) {
        out.println(message);
    }

    @Override
    public void start(GameController controller) {
        controller.runLoop();
    }

    @Override
    public PlayerAction nextAction(Game game) {
        out.print("Move (e2e4; promotions: e7e8q) or undo, moves, quit: ");
        out.flush();
        return PlayerAction.parse(in.hasNextLine() ? in.nextLine() : "quit");
    }
}
