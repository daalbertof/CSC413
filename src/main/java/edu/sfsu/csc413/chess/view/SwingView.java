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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

/** A clickable 2D chess board. All actions are delegated to the controller. */
public class SwingView implements BoardView {

    private static final Color LIGHT_SQUARE = new Color(238, 216, 181);
    private static final Color DARK_SQUARE = new Color(181, 136, 99);
    private static final Color SELECTED_SQUARE = new Color(255, 170, 80);
    private static final Color LEGAL_DESTINATION = new Color(159, 201, 111);

    private final JButton[][] squares =
            new JButton[Position.BOARD_SIZE][Position.BOARD_SIZE];
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JTextArea historyArea = new JTextArea();
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
            statusLabel.setText(game.sideToMove() + " to move  |  " + statusText(game));
            historyArea.setText(formatHistory(game));
            historyArea.setCaretPosition(historyArea.getDocument().getLength());
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
        Runnable update = () -> messageLabel.setText(
                message == null || message.isBlank() ? " " : message);
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
        JPanel topFiles = createFileLabels();
        JPanel bottomFiles = createFileLabels();
        JPanel leftRanks = createRankLabels();
        JPanel rightRanks = createRankLabels();
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

        JPanel labeledBoard = new JPanel(new BorderLayout(12, 4));
        labeledBoard.add(topFiles, BorderLayout.NORTH);
        labeledBoard.add(bottomFiles, BorderLayout.SOUTH);
        labeledBoard.add(leftRanks, BorderLayout.WEST);
        labeledBoard.add(rightRanks, BorderLayout.EAST);
        labeledBoard.add(boardPanel, BorderLayout.CENTER);

        historyArea.setEditable(false);
        historyArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        historyArea.setLineWrap(false);
        JScrollPane historyScroll = new JScrollPane(historyArea);
        historyScroll.setPreferredSize(new Dimension(220, 420));
        JPanel sidePanel = new JPanel(new BorderLayout(6, 8));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 4));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 16f));
        sidePanel.add(statusLabel, BorderLayout.NORTH);
        JPanel historyPanel = new JPanel(new BorderLayout(4, 4));
        historyPanel.add(new JLabel("Move history"), BorderLayout.NORTH);
        historyPanel.add(historyScroll, BorderLayout.CENTER);
        sidePanel.add(historyPanel, BorderLayout.CENTER);

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
        JPanel actions = new JPanel();
        actions.add(undoButton);
        actions.add(quitButton);

        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.add(messageLabel, BorderLayout.CENTER);
        footer.add(actions, BorderLayout.EAST);

        frame.add(labeledBoard, BorderLayout.CENTER);
        frame.add(sidePanel, BorderLayout.EAST);
        frame.add(footer, BorderLayout.SOUTH);
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        if (game != null) {
            render(game);
        }
    }

    private JPanel createFileLabels() {
        JPanel panel = new JPanel(new GridLayout(1, Position.BOARD_SIZE));
        for (int file = 0; file < Position.BOARD_SIZE; file++) {
            panel.add(coordinateLabel(String.valueOf((char) ('a' + file))));
        }
        return panel;
    }

    private JPanel createRankLabels() {
        JPanel panel = new JPanel(new GridLayout(Position.BOARD_SIZE, 1));
        for (int displayRow = 0; displayRow < Position.BOARD_SIZE; displayRow++) {
            panel.add(coordinateLabel(String.valueOf(Position.BOARD_SIZE - displayRow)));
        }
        return panel;
    }

    private JLabel coordinateLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
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

        String notation = selected + position.toString();
        boolean promotion = game.legalMoves().stream().anyMatch(move ->
                move.from().equals(selected)
                        && move.to().equals(position)
                        && move.isPromotion());
        if (promotion) {
            notation += Character.toLowerCase(choosePromotionPiece().symbol());
        }
        clearSelection();
        controller.handle(new PlayerAction.Move(notation));
    }

    private PieceType choosePromotionPiece() {
        PieceType[] choices = {
                PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT
        };
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

    private String formatHistory(Game game) {
        StringBuilder text = new StringBuilder();
        List<Move> moves = game.history();
        for (int index = 0; index < moves.size(); index++) {
            Move move = moves.get(index);
            boolean whiteMove = move.moved().color()
                    == edu.sfsu.csc413.chess.model.Color.WHITE;
            String color = whiteMove ? "White" : "Black";
            String turn = whiteMove ? "first turn" : "second turn";
            text.append(color).append(" (")
                    .append(turn)
                    .append("): ").append(move)
                    .append('\n');
        }
        if (text.isEmpty()) {
            return "No moves yet.";
        }
        return text.toString();
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
        return (position.file() + position.rank()) % 2 == 0
                ? DARK_SQUARE : LIGHT_SQUARE;
    }

    private String statusText(Game game) {
        return switch (game.status()) {
            case CHECK -> "Check";
            case CHECKMATE -> "Checkmate";
            case STALEMATE -> "Stalemate";
            case IN_PROGRESS -> "In progress";
        };
    }
}
