package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.PieceType;
import edu.sfsu.csc413.chess.model.Position;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/** A clickable 2D chess board. All actions are delegated to the controller. */
public class SwingView implements BoardView {

    private static final Color LIGHT_SQUARE = new Color(238, 216, 181);
    private static final Color DARK_SQUARE = new Color(181, 136, 99);
    private static final Color SELECTED_SQUARE = new Color(246, 246, 105);
    private static final Color LEGAL_DESTINATION = new Color(159, 201, 111);

    private final JButton[][] squares = new JButton[Position.BOARD_SIZE][Position.BOARD_SIZE];
    private final JLabel turnLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);
    private GameController controller;
    private Game game;
    private JFrame frame;
    private Position selected;
    private boolean quitRequested;

    @Override
    public void render(Game game) {
        this.game = game;
        if (frame == null) {
            return;
        }
        Runnable update = () -> {
            turnLabel.setText(game.sideToMove() + " to move  |  " + statusText(game));
            List<Move> legalMoves = selected == null ? List.of() : game.legalMoves();
            for (int rank = 0; rank < Position.BOARD_SIZE; rank++) {
                for (int file = 0; file < Position.BOARD_SIZE; file++) {
                    Position position = new Position(file, rank);
                    JButton square = squares[rank][file];
                    Piece piece = game.board().pieceAt(position);
                    square.setText(piece == null ? "" : PieceGlyphs.FIGURES.glyphFor(piece));
                    square.setBackground(squareColor(position, legalMoves));
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        } else {
            SwingUtilities.invokeLater(update);
        }
    }

    @Override
    public void showMessage(String message) {
        Runnable update = () -> messageLabel.setText(message == null || message.isBlank()
                ? " " : message);
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        } else {
            SwingUtilities.invokeLater(update);
        }
    }

    @Override
    public void start(GameController controller) {
        this.controller = controller;
        SwingUtilities.invokeLater(this::createWindow);
    }

    @Override
    public PieceType promotionChoice(Game game) {
        PieceType[] choices = {PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT};
        Object choice = JOptionPane.showInputDialog(
                frame,
                "Choose a promotion piece:",
                "Pawn promotion",
                JOptionPane.QUESTION_MESSAGE,
                null,
                choices,
                PieceType.QUEEN);
        return choice instanceof PieceType ? (PieceType) choice : PieceType.QUEEN;
    }

    @Override
    public void close() {
        if (frame != null && (quitRequested || game == null || !game.isGameOver())) {
            SwingUtilities.invokeLater(frame::dispose);
        }
    }

    private void createWindow() {
        frame = new JFrame("Chess");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel boardPanel = new JPanel(new GridLayout(
                Position.BOARD_SIZE, Position.BOARD_SIZE));
        Font pieceFont = new Font("Serif", Font.PLAIN, 42);
        for (int displayRow = 0; displayRow < Position.BOARD_SIZE; displayRow++) {
            int rank = Position.BOARD_SIZE - 1 - displayRow;
            for (int file = 0; file < Position.BOARD_SIZE; file++) {
                Position position = new Position(file, rank);
                JButton square = new JButton();
                square.setFont(pieceFont);
                square.setOpaque(true);
                square.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                square.setFocusable(false);
                square.addActionListener(event -> handleSquareClick(position));
                squares[rank][file] = square;
                boardPanel.add(square);
            }
        }

        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(event -> {
            clearSelection();
            controller.handle(new PlayerAction.Undo());
        });
        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(event -> {
            quitRequested = true;
            controller.handle(new PlayerAction.Quit());
        });

        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.add(turnLabel, BorderLayout.NORTH);
        footer.add(messageLabel, BorderLayout.CENTER);
        JPanel actions = new JPanel();
        actions.add(undoButton);
        actions.add(quitButton);
        footer.add(actions, BorderLayout.EAST);

        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(footer, BorderLayout.SOUTH);
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        if (game != null) {
            render(game);
        }
    }

    private void handleSquareClick(Position position) {
        if (game == null || controller == null || game.isGameOver()) {
            return;
        }

        Piece piece = game.board().pieceAt(position);
        if (selected == null) {
            if (piece != null && piece.color() == game.sideToMove()) {
                selected = position;
                showMessage("Selected " + position + ". Choose a destination.");
                render(game);
            }
            return;
        }

        if (selected.equals(position)) {
            clearSelection();
            render(game);
            return;
        }

        if (piece != null && piece.color() == game.sideToMove()) {
            selected = position;
            showMessage("Selected " + position + ". Choose a destination.");
            render(game);
            return;
        }

        String from = selected.toString();
        String to = position.toString();
        List<String> legalMoves = new ArrayList<>();
        for (var move : game.legalMoves()) {
            if (move.from().equals(selected) && move.to().equals(position)) {
                legalMoves.add(move.toString());
            }
        }

        String notation = from + to;
        if (!legalMoves.isEmpty() && legalMoves.size() > 1) {
            PieceType promotion = promotionChoice(game);
            notation += Character.toLowerCase(promotion.symbol());
        }

        clearSelection();
        controller.handle(new PlayerAction.Move(notation));
    }

    private void clearSelection() {
        selected = null;
    }

    private Color squareColor(Position position, List<Move> legalMoves) {
        if (position.equals(selected)) {
            return SELECTED_SQUARE;
        }
        if (selected != null && legalMoves.stream().anyMatch(move ->
                move.from().equals(selected) && move.to().equals(position))) {
            return LEGAL_DESTINATION;
        }
        return (position.file() + position.rank()) % 2 == 0 ? DARK_SQUARE : LIGHT_SQUARE;
    }

    private String statusText(Game game) {
        GameStatus status = game.status();
        return switch (status) {
            case CHECK -> "Check";
            case CHECKMATE -> "Checkmate";
            case STALEMATE -> "Stalemate";
            case IN_PROGRESS -> "In progress";
        };
    }
}
