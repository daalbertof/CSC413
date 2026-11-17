package edu.sfsu.csc413.chess;
import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.view.*;

/**
 * Entry point.
 *
 * <p>At M0 this does nothing but prove the toolchain works. It grows into the
 * real launcher as the engine appears underneath it.
 */

public final class Main {

    public static void main(String[] args) {
        Game game = new Game();
        String choice = args.length > 0 ? args[0].toLowerCase() : "console";
        BoardView view = switch (choice) {
            case "swing" -> new SwingView();
            case "terminal" -> new TerminalView();
            case "emoji" -> new EmojiView();
            case "console" -> new ConsoleView();
            default -> {
                System.err.println("Unknown view '" + choice
                        + "'. Choose swing, terminal, console, or emoji.");
                yield new ConsoleView();
            }
        };
        new GameController(game, view).start();
    }
    private Main() {
    }
}
