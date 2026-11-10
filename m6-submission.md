# M6 Submission

## 1. Who decides, who shows

`MoveGenerator.status()` decides whether the position is in progress, check, checkmate, or stalemate. 
`GameController.handle()` announces the result to the player through the view. 
`MoveGenerator` answers a rule question, while `GameController` handles the interaction between the game and the view.

## 2. The dependency rule

`Main` imports classes from `edu.sfsu.csc413.chess.view` because it chooses and starts the view. The engine classes, including `Game` and `MoveGenerator`, must not import view classes because the game rules should work independently of the console or any future graphical interface.

## 3. Test 2 on your engine

For a Swing view, I expect to add or modify the Swing view class and possibly update `Main` to select it. I should not need to change `GameController` or anything in `engine`, because the controller already communicates through the stable `BoardView` interface.