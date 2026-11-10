package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Connects the player's decisions to the game: carry out what was asked, then
 * ask the view to redraw.
 *
 * <p>This is the C in MVC. It is the only class that knows about both the
 * {@link Game} and the {@link BoardView}. It holds no game state of its own:
 * the model owns the position, the view owns the display, and the controller
 * owns only the conversation between them.
 *
 * <p>When you are done, this class mentions no {@code Scanner}, no
 * {@code System.out}, no window, and none of the words "undo", "moves", or
 * "quit". Those belong to the view and to {@link PlayerAction#parse}.
 */
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.model.PieceType;
public class GameController {

    private final Game game;
    private final BoardView view;
    private boolean stop;

    public GameController(Game game, BoardView view) {
        this.game = game;
        this.view = view;
    }

    /** The game being played, so a view can read it in order to draw it. */
    public Game game() {
        return game;
    }

    /**
     * Greets the player, draws the opening position, and hands control to the
     * view with {@link BoardView#start}.
     */
    public void start() {
        stop = false;
        view.showMessage("Chess!");
        view.render(game);
        view.start(this);
    }

    /**
     * The ask-and-handle loop, for views that can wait for input.
     *
     * <p>Asks the view for the next action and handles it, until the player
     * quits or the game is over. A console view calls this from its
     * {@code start}; an event-driven view never calls it.
     */
    public void runLoop() {
        while (!stop && !game.isGameOver()) {
            handle(view.nextAction(game));
        }
    }

    /**
     * Carries out one player action and redraws.
     *
     * <p>Every route into the game goes through here: {@link #runLoop()} calls
     * it, and so will a button's click handler in M7. The handout lists what
     * each of the four actions does and what the player is told.
     */
    public void handle(PlayerAction action) { //had trouble with this part....
        switch (action) {
            case PlayerAction.Move move -> {
                String notation = move.notation();
                var legalMove = game.findLegalMove(notation);

                if (legalMove.isEmpty() && notation.length() == 4) {
                    PieceType choice = view.promotionChoice(game);
                    String promotedNotation = notation
                            + Character.toLowerCase(choice.symbol());
                    legalMove = game.findLegalMove(promotedNotation);
                }

                if (legalMove.isPresent()) {
                    game.play(legalMove.get());
                } else {
                    view.showMessage("Not a legal move: '" + notation + "'.");
                }
            }

            case PlayerAction.Undo ignored -> {
                if (game.undoLastMove().isEmpty()) {
                    view.showMessage("Nothing to undo.");
                }
            }

            case PlayerAction.ShowMoves ignored -> {
                String moves = game.legalMoves()
                        .stream()
                        .map(Move::toString)
                        .sorted(Comparator.naturalOrder())
                        .collect(Collectors.joining(" "));

                view.showMessage(game.legalMoves().size()
                        + " legal moves: " + moves);
            }

            case PlayerAction.Quit ignored -> {
                stop = true;
                view.showMessage("Goodbye.");
                view.close();
                return;
            }
        }

        view.render(game);

        if (game.isGameOver()) {
            if (game.status() == GameStatus.CHECKMATE) {
                Color winner = game.sideToMove().opposite();
                view.showMessage("Checkmate. " + winner + " wins.");
            } else if (game.status() == GameStatus.STALEMATE) {
                view.showMessage("Stalemate. The game is a draw.");
            }

            view.close();
        }
    }
}
