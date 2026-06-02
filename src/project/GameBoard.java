package project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameBoard extends JPanel {
    public final int cols, rows, cellSize;
    private final GameController controller;

    public GameBoard(int cols, int rows, int cellSize) {
        this.cols = cols;
        this.rows = rows;
        this.cellSize = cellSize;
        
        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize + 150));
        setBackground(Color.BLACK);

        controller = new GameController(this);

        // Control panel
        JPanel control = new JPanel();
        control.setBackground(Color.WHITE);
        control.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));

        // Pause/Resume button
        JButton pauseResume = new JButton("Pause");
        pauseResume.addActionListener(ev -> {
            boolean isPaused = controller.togglePause();
            pauseResume.setText(isPaused ? "Resume" : "Pause");
        });

        // Restart button
        JButton restart = new JButton("Restart");
        restart.addActionListener(ev -> {
            controller.restart();
            pauseResume.setText("Pause");
        });

        // Maze button
        JButton mazeButton = new JButton("Maze Pattern");
        mazeButton.addActionListener(ev -> {
            String[] patterns = {
                "None (Clear)", 
                "Vertical Lines", 
                "Horizontal Lines", 
                "Grid", 
                "Spiral", 
                "Cross", 
                "Rooms",
                "Random"
            };

            String choice = (String) JOptionPane.showInputDialog(
                this, 
                "Select Maze Pattern:", 
                "Maze Configuration",
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                patterns, 
                "None (Clear)"
            );

            if (choice != null) {
                if (choice.equals("None (Clear)")) {
                    controller.clearMaze();
                } else {
                    String pattern = choice.toLowerCase().replace(" ", "_");
                    controller.changeMazePattern(pattern);
                }
            }
        });

        // Speed controls
        JLabel speedLabel = new JLabel("Speed (moves/sec):");
        JSlider speedSlider = new JSlider(1, 12, 5);
        speedSlider.setPreferredSize(new Dimension(200, 30));
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setPaintTicks(true);

        JTextField speedField = new JTextField("5", 4);
        speedField.setHorizontalAlignment(JTextField.CENTER);
        speedField.setPreferredSize(new Dimension(40, 25));

        speedSlider.addChangeListener(ev -> {
            int speed = speedSlider.getValue();
            speedField.setText(String.valueOf(speed));
            controller.setSpeed(speed);
        });

        speedField.addActionListener(ev -> {
            try {
                int speed = Integer.parseInt(speedField.getText());
                if (speed >= 1 && speed <= 20) {
                    speedSlider.setValue(speed);
                    controller.setSpeed(speed);
                } else {
                    speedField.setText(String.valueOf(speedSlider.getValue()));
                }
            } catch (NumberFormatException ex) {
                speedField.setText(String.valueOf(speedSlider.getValue()));
            }
        });

        // Add components to control panel
        control.add(pauseResume);
        control.add(restart);
        control.add(mazeButton);
        control.add(speedLabel);
        control.add(speedSlider);
        control.add(speedField);

        setLayout(new BorderLayout());
        add(control, BorderLayout.SOUTH);

        // Mouse listener for placing food
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int gridX = e.getX() / cellSize;
                int gridY = e.getY() / cellSize;

                if (gridX >= 0 && gridX < cols && gridY >= 0 && gridY < rows) {
                    controller.placeFood(gridX, gridY);
                }
            }
        });
    }

    public void startGame() {
        controller.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        controller.render(g);
    }
}
