# Instructions for coding agents

This is a chess engine built with MVC. The model and controller were
written by hand and are finished. Your job is to create views only.

## Do

- Add views under:
  `src/main/java/edu/sfsu/csc413/chess/view/`
- Read `BoardView`, `ConsoleView`, `EmojiView`, `PlayerAction`,
  `GameController`, and `Main` before writing code.
- Implement the existing `BoardView` interface.
- Read game information through `Game`:
  `board()`, `sideToMove()`, `legalMoves()`, `status()`, and `history()`.
- Send player actions to the controller using `PlayerAction`.
- Modify `Main` only as necessary to select the new views from the
  command line, following the existing structure.
- Use only the Java JDK. Do not add dependencies.

## Do not

- Do not change anything in `model/`, `engine/`, or `factory/`.
- Do not change `GameController`, `BoardView`, `PlayerAction`,
  `ConsoleView`, or `EmojiView`.
- Do not call `game.play()` or `game.undoLastMove()`.
- Do not directly change the game state from a view.
- Do not calculate chess rules in a view.
- Legal moves must come from `game.legalMoves()`.
- Check, checkmate, and stalemate must come from `game.status()`.
- Do not call `controller.runLoop()` from a Swing view.
- A Swing view's `start()` method must build the window and return.
- Do not change `pom.xml`.
- Do not modify or delete existing tests.

A modal dialog for choosing a promotion piece is allowed. The restriction
is against waiting for the next move inside a click handler or calling the
controller's game loop from the Swing view.

## Check your work

Run:

```bash
./mvnw test