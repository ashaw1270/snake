import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Scanner;

public class Game {
    private static final boolean rainbow = false;
    private static final int FRAME_SIZE = 650;
    private static final int SCORE_SIZE = 100;
    private static final int STARTING_LENGTH = 5;
    private int gameSize;
    private int waitTime;
    private int adjustedWaitTime;
    private JPanel[][] panels;
    private final JPanel gameFrame;
    private final JLabel scoreLabel;
    private ArrayList<int[]> snake;
    private int[] food;
    private int score;
    private int level;
    private final boolean playingWithLevels;

    private enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }
    private Direction currentDirection;

    public enum Difficulty {
        EASY("EASY"),
        MEDIUM("MEDIUM"),
        HARD("HARD");

        Difficulty(String difficulty) {}
    }

    public static void playGame() {
        Difficulty difficulty;
        try (Scanner reader = new Scanner(System.in)) {
            while (true) {
                System.out.print("Enter difficulty (easy/medium/hard): ");
                try {
                    difficulty = Difficulty.valueOf(reader.nextLine().toUpperCase());
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid difficulty.");
                }
            }
        }
        new Game(difficulty, false);
    }

    public static void playWithLevels() {
        new Game(Difficulty.EASY, true);
    }

    public Game(Difficulty difficulty, boolean playingWithLevels) {
        switch (difficulty) {
            case EASY -> {
                gameSize = 15;
                adjustedWaitTime = waitTime = 150;
            }
            case MEDIUM -> {
                gameSize = 40;
                adjustedWaitTime = waitTime = 75;
            }
            // HARD
            default -> {
                gameSize = 80;
                adjustedWaitTime = waitTime = 35;
            }
        }

        this.playingWithLevels = playingWithLevels;

        panels = new JPanel[gameSize][gameSize];
        snake = new ArrayList<>();
        currentDirection = Direction.UP;
        food = new int[2];
        score = STARTING_LENGTH;
        level = 0;

        gameFrame = new JPanel();
        gameFrame.setLayout(new GridLayout(gameSize, gameSize));

        for (int row = 0; row < gameSize; row++) {
            for (int col = 0; col < gameSize; col++) {
                JPanel panel = new JPanel();
                panel.setVisible(true);
                panel.setBackground(Color.BLACK);
                panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                panels[row][col] = panel;
                gameFrame.add(panel);
            }
        }

        for (int row = gameSize / 2; row < gameSize / 2 + STARTING_LENGTH; row++) {
            snake.add(new int[]{row, gameSize / 2});
        }
        fill();

        gameFrame.setBackground(Color.BLACK);
        gameFrame.setBounds(0, SCORE_SIZE, FRAME_SIZE, FRAME_SIZE);
        gameFrame.setVisible(true);

        JPanel scoreBoard = new JPanel();
        scoreBoard.setLayout(null);
        scoreBoard.setBackground(Color.DARK_GRAY);
        scoreBoard.setBounds(0, 0, FRAME_SIZE, SCORE_SIZE);
        scoreBoard.setVisible(true);

        scoreLabel = new JLabel();
        scoreLabel.setSize(FRAME_SIZE, SCORE_SIZE);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        Font font = new Font("Arial", Font.BOLD, SCORE_SIZE);
        scoreLabel.setFont(font);
        scoreLabel.setForeground(Color.GREEN);
        scoreLabel.setVisible(true);
        scoreBoard.add(scoreLabel);

        JFrame mainFrame = new JFrame();
        mainFrame.setSize(FRAME_SIZE, FRAME_SIZE + SCORE_SIZE + 30);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.add(gameFrame);
        mainFrame.add(scoreBoard);
        mainFrame.setVisible(true);
        mainFrame.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    adjustedWaitTime = waitTime;
                }
            }
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    adjustedWaitTime = waitTime;
                }
            }
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> {
                        if (currentDirection != Direction.DOWN) {
                            currentDirection = Direction.UP;
                        }
                    }
                    case KeyEvent.VK_DOWN -> {
                        if (currentDirection != Direction.UP) {
                            currentDirection = Direction.DOWN;
                        }
                    }
                    case KeyEvent.VK_LEFT -> {
                        if (currentDirection != Direction.RIGHT) {
                            currentDirection = Direction.LEFT;
                        }
                    }
                    case KeyEvent.VK_RIGHT -> {
                        if (currentDirection != Direction.LEFT) {
                            currentDirection = Direction.RIGHT;
                        }
                    }
                    case KeyEvent.VK_SPACE -> {
                        if (adjustedWaitTime == waitTime) {
                            adjustedWaitTime /= 2;
                        }
                    }
                }
            }
        });

        if (playingWithLevels) {
            levelUp();
        }

        play();
    }

    public void play() {
        // absorbs the window opening
        try {Thread.sleep(250);} catch (InterruptedException ignored) {}

        for (int i = 3; i >= 1; i--) {
            scoreLabel.setText(String.valueOf(i));
            try {Thread.sleep(500);} catch (InterruptedException ignored) {}
        }
        scoreLabel.setText("GO!");

        generateFood();

        for (int i = 0; i < 5; i++) {
            move();
        }

        scoreLabel.setText("Score: " + score);

        while (!crossed()) {
            try {
                move();
            } catch (ArrayIndexOutOfBoundsException e) {
                break;
            }

            if (playingWithLevels) {
                int scoreNeeded = gameSize * gameSize / 40;
                if (score == scoreNeeded) {
                    win();
                }
            }
        }

        die();
    }

    public boolean crossed() {
        int[] head = snake.get(0);
        for (int i = 1; i < snake.size(); i++) {
            if (snake.get(i)[0] == head[0] && snake.get(i)[1] == head[1]) {
                return true;
            }
        }
        return false;
    }

    public void move() {
        try {Thread.sleep(adjustedWaitTime);} catch (InterruptedException ignored) {}

        ArrayList<int[]> updatedSnake = new ArrayList<>();

        int[] snakeHead = snake.get(0);
        updatedSnake.add(switch (currentDirection) {
            case UP -> new int[]{snakeHead[0] - 1, snakeHead[1]};
            case DOWN -> new int[]{snakeHead[0] + 1, snakeHead[1]};
            case LEFT -> new int[]{snakeHead[0], snakeHead[1] - 1};
            case RIGHT -> new int[]{snakeHead[0], snakeHead[1] + 1};
        });

        for (int i = 0; i < snake.size() - 1; i++) {
            updatedSnake.add(snake.get(i));
        }

        int[] updatedHead = updatedSnake.get(0);
        int[] oldTail = snake.get(snake.size() - 1);
        if (updatedHead[0] == food[0] && updatedHead[1] == food[1]) {
            updatedSnake.add(oldTail);
            incrementScore();
            generateFood();
        } else {
            setPanelColor(oldTail, Color.BLACK);
        }

        snake = updatedSnake;
        fill();
    }

    public void incrementScore() {
        score++;
        scoreLabel.setText("Score: " + score);
    }

    public void setPanelColor(int[] location, Color color) {
        if (color == Color.BLACK) {
            panels[location[0]][location[1]].setBackground(Color.BLACK);
        } else {
            panels[location[0]][location[1]].setBackground(rainbow ? randomColor() : color);
        }
    }

    public void fill() {
        for (int[] block : snake) {
            setPanelColor(block, Color.GREEN);
        }
    }

    public void generateFood() {
        ArrayList<int[]> emptySpaces = getEmptySpaces();
        int random = (int) (Math.random() * emptySpaces.size());
        food = emptySpaces.get(random);
        setPanelColor(food, Color.RED);
    }

    public ArrayList<int[]> getEmptySpaces() {
        ArrayList<int[]> emptySpaces = new ArrayList<>();
        for (int row = 0; row < gameSize; row++) {
            for (int col = 0; col < gameSize; col++) {
                if (isEmpty(row, col)) {
                    emptySpaces.add(new int[]{row, col});
                }
            }
        }
        return emptySpaces;
    }

    public boolean isEmpty(int row, int col) {
        for (int[] block : snake) {
            if (block[0] == row && block[1] == col) {
                return false;
            }
        }
        return true;
    }

    public void win() {
        end(true);
    }

    public void die() {
        end(false);
    }

    // true for win, false for die
    public void end(boolean winOrDie) {
        setPanelColor(food, Color.BLACK);

        scoreLabel.setText(winOrDie ? "Level up! :)" : "You lose! :(");

        for (int i = 0; i < snake.size(); i++) {
            try {
                int waitTime = (int) (Math.pow(Math.E, - (double) (i * 5) / snake.size()) * 400);
                setPanelColor(snake.get(i), winOrDie ? Color.GREEN : Color.RED);
                if (i > 2) {
                    setPanelColor(snake.get(i - 3), Color.BLACK);
                }
                try {Thread.sleep(waitTime);} catch (InterruptedException ignored) {}
            } catch (ArrayIndexOutOfBoundsException ignored) {}
        }

        for (int i = snake.size() - 3; i < snake.size(); i++) {
            try {Thread.sleep(500);} catch (InterruptedException ignored) {}
            setPanelColor(snake.get(i), Color.BLACK);
        }

        explode(snake.get(snake.size() - 1));

        if (winOrDie) {
            levelUp();
        }

        snake.clear();
        for (int row = gameSize / 2; row < gameSize / 2 + STARTING_LENGTH; row++) {
            snake.add(new int[]{row, gameSize / 2});
        }
        fill();

        currentDirection = Direction.UP;
        adjustedWaitTime = waitTime;

        score = STARTING_LENGTH;
        scoreLabel.setText("");
        if (!winOrDie && playingWithLevels) {
            level = 0;
            gameSize = 15;
            waitTime = 150;
            adjustedWaitTime = 150;
            levelUp();
            snake.clear();
            for (int row = gameSize / 2; row < gameSize / 2 + STARTING_LENGTH; row++) {
                snake.add(new int[]{row, gameSize / 2});
            }
            fill();
        }

        play();
    }

    public void levelUp() {
        level++;

        gameSize += 10;
        waitTime -= 20;
        panels = new JPanel[gameSize][gameSize];
        gameFrame.removeAll();
        gameFrame.setLayout(new GridLayout(gameSize, gameSize));

        for (int row = 0; row < gameSize; row++) {
            for (int col = 0; col < gameSize; col++) {
                JPanel panel = new JPanel();
                panel.setVisible(true);
                panel.setBackground(Color.BLACK);
                panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                panels[row][col] = panel;
                gameFrame.add(panel);
            }
        }

        int scoreNeeded = gameSize * gameSize / 40;
        scoreLabel.setText("Level " + level);
        try {Thread.sleep(1000);} catch (InterruptedException ignored) {}
        scoreLabel.setText("Get " + scoreNeeded);
        try {Thread.sleep(1000);} catch (InterruptedException ignored) {}
    }

    public void explode(int[] location) {
        drawSquare(location, 3);
        for (int i = 5; !boardIsEmpty(); i += 2) {
            if (i % 4 > 1) {
                clear();
            }
            drawSquare(location, i);
            // 1350 is somewhat arbitrary
            try {Thread.sleep(1350 / gameSize);} catch (InterruptedException ignored) {}
        }
    }

    public void drawSquare(int[] center, int sideLength) {
        // top side
        for (int i = 0; i < sideLength; i++) {
            int[] location = new int[]{center[0] - sideLength / 2, center[1] - sideLength / 2 + i};
            try {setPanelColor(location, randomColor());} catch (ArrayIndexOutOfBoundsException ignored) {}
        }
        
        // right side
        for (int i = 0; i < sideLength; i++) {
            int[] location = new int[]{center[0] - sideLength / 2 + i, center[1] + sideLength / 2};
            try {setPanelColor(location, randomColor());} catch (ArrayIndexOutOfBoundsException ignored) {}
        }
        
        // bottom side
        for (int i = 0; i < sideLength; i++) {
            int[] location = new int[]{center[0] + sideLength / 2, center[1] - sideLength / 2 + i};
            try {setPanelColor(location, randomColor());} catch (ArrayIndexOutOfBoundsException ignored) {}
        }
        
        // left side
        for (int i = 0; i < sideLength; i++) {
            int[] location = new int[]{center[0] - sideLength / 2 + i, center[1] - sideLength / 2};
            try {setPanelColor(location, randomColor());} catch (ArrayIndexOutOfBoundsException ignored) {}
        }
    }

    public boolean boardIsEmpty() {
        for (int row = 0; row < gameSize; row++) {
            for (int col = 0; col < gameSize; col++) {
                if (panels[row][col].getBackground() != Color.BLACK) {
                    return false;
                }
            }
        }
        return true;
    }

    public void clear() {
        for (int row = 0; row < gameSize; row++) {
            for (int col = 0; col < gameSize; col++) {
                setPanelColor(new int[]{row, col}, Color.BLACK);
            }
        }
    }

    public Color randomColor() {
        float hue = (float) Math.random();
        float saturation = 0.9F;
        float luminance = 1.0F;
        return Color.getHSBColor(hue, saturation, luminance);
    }

    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);
        System.out.print("Play with levels? (y/n): ");
        String yn = reader.nextLine();
        if (yn.equalsIgnoreCase("y")) {
            playWithLevels();
        } else {
            playGame();
        }
    }
}
