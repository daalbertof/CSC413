# M7 Submission

## 1. Group

Alone.

## 2. Tools

GitHub Copilot in IntelliJ IDEA, Agent mode, Copilot Fusion Standard.

## 3. Other view

The other view is selected with:

```text
terminal
```

## 4. Runs

### Run 1

Branch: `m7-run-1`

Request:

> Start M7 Run 1. Add a graphical 2D view of the board using Swing. I want to play by clicking a piece and then clicking the square to move it to. Show whose turn it is and messages like check, checkmate, stalemate, and illegal moves. Include an Undo button. Handle promotion without crashing. Also add one other playable non-Swing view selectable from the command line. Follow AGENTS.md exactly. Do not change model/, engine/, factory/, GameController, BoardView, PlayerAction, ConsoleView, EmojiView, pom.xml, or any existing tests. First inspect the repository and explain your plan before editing files.

Result: Added `SwingView` and `TerminalView`, and added `swing` and `terminal` command-line options. The Swing view initially closed before the final checkmate message could be read, so `SwingView.java` was corrected.

### Run 2

Branch: `m7-run-2`

Request:

> Start M7 Run 2. Add a graphical 2D view of the board using Swing. I want to play by clicking a piece and then clicking the square to move it to. Show whose turn it is and messages like check, checkmate, stalemate, and illegal moves. Include an Undo button. Handle promotion without crashing. Also add one other playable non-Swing view selectable from the command line. Follow AGENTS.md exactly. Do not change model/, engine/, factory/, GameController, BoardView, PlayerAction, ConsoleView, EmojiView, pom.xml, or any existing tests. First inspect the repository and explain your plan before editing files.

> Additional Run 2 instruction: include coordinate labels on the Swing board and visibly highlight the legal destination squares after a piece is selected.

Result: Added coordinate labels and legal-destination highlighting. The first layout placed labels inside the chessboard grid, so `SwingView.java` was corrected to use a separate 8×8 board panel with labels outside it.

### Run 3

Branch: `m7-run-3`

Request:

> Start M7 Run 3. Add a graphical 2D view of the board using Swing. I want to play by clicking a piece and then clicking the square to move it to. Show whose turn it is and messages like check, checkmate, stalemate, and illegal moves. Include an Undo button. Handle promotion without crashing. Also add one other playable non-Swing view selectable from the command line. Follow AGENTS.md exactly. Do not change model/, engine/, factory/, GameController, BoardView, PlayerAction, ConsoleView, EmojiView, pom.xml, or any existing tests. First inspect the repository and explain your plan before editing files.

> Additional Run 3 instruction: show the move history in the Swing window and keep the status and history easy to read while the game is being played.

Result: Kept Run 3 because it added a readable move-history panel while preserving coordinates, legal-destination highlighting, Undo, Quit, promotion handling, and checkmate messages.

## 5. AGENTS.md

I adapted `AGENTS.md` to this repository because `Main` does not have a `chooseView` method. The instructions tell the agent to follow the existing `Main` switch structure.

I also clarified that a modal promotion dialog is allowed. The restriction is against calling `controller.runLoop()` or waiting for the next move inside a Swing click handler.

## 6. Review

### `SwingView.java:47-64`

`render` updates the board, status, move history, piece glyphs, and square colors. I checked this by launching Swing and making moves. I kept this code.

### `SwingView.java:198-235`

`handleSquareClick` stores the selected square and sends moves through `controller.handle(new PlayerAction.Move(...))`. It does not call `game.play`. I checked this with the debugger and observed:

```text
First click: position=e2, selected=null
Second click: position=e4, selected=e2
```

I kept this code.

### `SwingView.java:227-250`

Promotion is detected using legal moves from `game.legalMoves()`. A modal dialog chooses the promotion piece, and dismissing the dialog defaults to a queen. I checked that the view does not implement chess rules itself. I kept this code.

### `SwingView.java:277-287`

Selected squares and legal destinations receive separate highlight colors. Legal destinations come from `game.legalMoves()`. I checked this by selecting a pawn and observing its legal destination squares. I kept this code.

### `TerminalView.java:26-61`

`TerminalView` draws its own bordered board, displays the current status and move history, and sends commands through `PlayerAction`. I checked it by running `Main terminal` and playing moves, undoing, and quitting. I kept this code.

### `Main.java:14-28`

`Main` selects `swing`, `terminal`, `console`, or `emoji` without changing the controller or model. I checked the command-line options and kept this code.

## 7. Comparison

### 8. Where does the state of a half-finished move live?

In `SwingView`, the selected square is stored in the field `selected` at `SwingView.java:43`. The click handler updates it at `SwingView.java:204-235`.

`TerminalView` does not store a half-finished move. It reads complete move notation from the terminal and sends it through `PlayerAction.parse`.

### 9. How does each view find out which moves are legal?

`SwingView` reads legal moves through `game.legalMoves()` at `SwingView.java:56` and `SwingView.java:227`.

`TerminalView` does not calculate legal moves. It sends the typed action to `GameController`, which uses the game's legal-move logic.

### 10. What does each do on a promotion?

`SwingView` opens a modal promotion dialog at `SwingView.java:238-250`. If the dialog is dismissed, it chooses a queen.

`TerminalView` allows promotion notation such as `e7e8q`, which is parsed and handled through `PlayerAction` and `GameController`.

### 11. Which is easier to change, and why? Which did you keep?

`TerminalView` is easier to change because it uses straightforward text input and output. `SwingView` has layout, event-handler, and Swing-thread concerns.

I kept both views. `SwingView` is the primary new view because it satisfies the graphical requirement, and `TerminalView` satisfies the separate playable-view requirement.

## 12. Applied

I applied the Run 2 idea of visible coordinates and legal-destination highlighting to the final kept `SwingView`. This appears at `SwingView.java:104-130`, `SwingView.java:176-196`, and `SwingView.java:277-287`.

## 13. Debugger evidence

I set a breakpoint in `SwingView.handleSquareClick` at line 198 and followed one move.

My prediction was that the first click would receive `e2` while no square was selected, and that the second click would receive `e4` while `e2` was stored as the selected square.

The debugger showed:

```text
First click: position=e2, selected=null
Second click: position=e4, selected=e2
```

The move then continued through the controller as `e2e4`.

## 14. By hand

Commit:

```text
M7 by hand: changed color of the selected square
```

I changed `SwingView.java:32` by hand:

```java
private static final Color SELECTED_SQUARE = new Color(255, 170, 80);
```

This changed the selected-square highlight from yellow to orange. I read `render` and `squareColor` to understand how the selected square receives its color before making the change.