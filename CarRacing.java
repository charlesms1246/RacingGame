import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CarRacing extends Application {
    private static final double WINDOW_WIDTH = 400;
    private static final double WINDOW_HEIGHT = 600;
    private static final double CAR_WIDTH = 50;
    private static final double CAR_HEIGHT = 80;
    private static final double OBSTACLE_WIDTH = 50;
    private static final double OBSTACLE_HEIGHT = 80;

    private ImageView playerCar;
    private List<ImageView> obstacles = new ArrayList<>();
    private int score = 0;
    private Label scoreLabel = new Label("Score: 0");
    private boolean gameRunning = true;
    private Random random = new Random();
    private AnimationTimer gameLoop;

    // MediaPlayer for background music and collision sound
    private MediaPlayer backgroundMusicPlayer;
    private MediaPlayer collisionSoundPlayer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Start screen pane
        Pane startRoot = new Pane();
        Scene startScene = new Scene(startRoot, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Load background image for start screen
        Image startImage = new Image(getClass().getResource("start1.jpg").toString());
        ImageView startBackground = new ImageView(startImage);
        startBackground.setFitWidth(WINDOW_WIDTH);
        startBackground.setFitHeight(WINDOW_HEIGHT);
        startRoot.getChildren().add(startBackground);

        // Add "Start Game" button
        Button startButton = new Button("Start Game");
        startButton.setLayoutX(WINDOW_WIDTH / 2 - 50);
        startButton.setLayoutY(WINDOW_HEIGHT / 2);
        startButton.setStyle("-fx-font-size: 20px; -fx-padding: 10px;");
        startButton.setOnAction(event -> startGame(primaryStage));

        startRoot.getChildren().add(startButton);

        // Add event listener to start the game on "Enter" key
        startScene.setOnKeyPressed(event -> {
            if (event.getCode().equals(javafx.scene.input.KeyCode.ENTER)) {
                startGame(primaryStage);
            }
        });

        primaryStage.setScene(startScene);
        primaryStage.setTitle("SHIFT HAPPENS");
        primaryStage.show();
    }

    private void startGame(Stage primaryStage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Load audio files (background music and collision sound)
        if (!loadAudioFiles()) {
            System.out.println("Error loading audio files. Exiting game.");
            Platform.exit(); // Exit the game if audio files are not loaded
            return;
        }

        // Load images
        Image roadImage = new Image(getClass().getResource("road.png").toString());
        Image carImage = new Image(getClass().getResource("car.png").toString());
        Image obstacleImage = new Image(getClass().getResource("skull.png").toString());

        // Background
        ImageView road = new ImageView(roadImage);
        road.setFitWidth(WINDOW_WIDTH);
        road.setFitHeight(WINDOW_HEIGHT);
        root.getChildren().add(road);

        // Player car
        playerCar = new ImageView(carImage);
        playerCar.setFitWidth(CAR_WIDTH);
        playerCar.setFitHeight(CAR_HEIGHT);
        playerCar.setX(WINDOW_WIDTH / 2 - CAR_WIDTH / 2);
        playerCar.setY(WINDOW_HEIGHT - CAR_HEIGHT - 20);
        root.getChildren().add(playerCar);

        // Score label
        scoreLabel.setLayoutX(10);
        scoreLabel.setLayoutY(10);
        scoreLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        root.getChildren().add(scoreLabel);

        // Key controls for the car
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT:
                    if (playerCar.getX() > 0) {
                        playerCar.setX(playerCar.getX() - 10);
                    }
                    break;
                case RIGHT:
                    if (playerCar.getX() < WINDOW_WIDTH - CAR_WIDTH) {
                        playerCar.setX(playerCar.getX() + 10);
                    }
                    break;
                case UP:
                    if (playerCar.getY() > 0) {
                        playerCar.setY(playerCar.getY() - 10); // Move forward
                    }
                    break;
                case DOWN:
                    if (playerCar.getY() < WINDOW_HEIGHT - CAR_HEIGHT) {
                        playerCar.setY(playerCar.getY() + 10); // Move backward
                    }
                    break;
            }
        });

        // Ensure the scene listens for key events
        scene.getRoot().requestFocus();  // This line ensures that the root node listens for key events

        // Game loop
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameRunning) {
                    spawnObstacles(obstacleImage);
                    moveObstacles();
                    checkCollisions();
                    updateScore();
                }
            }
        };
        gameLoop.start();

        // Play background music
        playBackgroundMusic();

        primaryStage.setScene(scene);
        primaryStage.setTitle("Car Racing Game");
        primaryStage.show();
    }

    private boolean loadAudioFiles() {
        try {
            // Load the background music file
            Media backgroundMusic = new Media(getClass().getResource("background.mp3").toString());
            backgroundMusicPlayer = new MediaPlayer(backgroundMusic);

            // Load the collision sound file
            Media collisionSound = new Media(getClass().getResource("collision.mp3").toString());
            collisionSoundPlayer = new MediaPlayer(collisionSound);

            return true;
        } catch (Exception e) {
            System.out.println("Error loading audio files: " + e.getMessage());
            return false;
        }
    }

    private void playBackgroundMusic() {
        try {
            // Set the music to loop indefinitely
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusicPlayer.setVolume(0.5);  // Set volume level (optional)
            backgroundMusicPlayer.play();
        } catch (Exception e) {
            System.out.println("Error playing background music: " + e.getMessage());
        }
    }

    private void playCollisionSound() {
        try {
            // Play the collision sound
            collisionSoundPlayer.play();
        } catch (Exception e) {
            System.out.println("Error playing collision sound: " + e.getMessage());
        }
    }

    private void spawnObstacles(Image obstacleImage) {
        // Randomly spawn obstacles
        if (random.nextDouble() < 0.03) { // Adjust frequency here
            ImageView obstacle = new ImageView(obstacleImage);
            obstacle.setFitWidth(OBSTACLE_WIDTH);
            obstacle.setFitHeight(OBSTACLE_HEIGHT);
            obstacle.setX(random.nextInt((int) (WINDOW_WIDTH - OBSTACLE_WIDTH)));
            obstacle.setY(-OBSTACLE_HEIGHT);  // Start above the screen
            obstacles.add(obstacle);
            ((Pane) playerCar.getParent()).getChildren().add(obstacle);  // Add to root pane
        }
    }

    private void moveObstacles() {
        Iterator<ImageView> iterator = obstacles.iterator();
        while (iterator.hasNext()) {
            ImageView obstacle = iterator.next();
            obstacle.setY(obstacle.getY() + 3); // Adjust speed here

            if (obstacle.getY() > WINDOW_HEIGHT) {
                iterator.remove();  // Remove obstacles that move off-screen
                ((Pane) playerCar.getParent()).getChildren().remove(obstacle);
            }
        }
    }

    private void checkCollisions() {
        for (ImageView obstacle : obstacles) {
            // Create a buffer for more accurate collision detection
            double buffer = 10.0;

            // Get the player car's collision area with a buffer
            double carX = playerCar.getX() + buffer;
            double carY = playerCar.getY() + buffer;
            double carWidth = playerCar.getFitWidth() - buffer * 2;
            double carHeight = playerCar.getFitHeight() - buffer * 2;

            // Get the obstacle's collision area with a buffer
            double obstacleX = obstacle.getX() + buffer;
            double obstacleY = obstacle.getY() + buffer;
            double obstacleWidth = obstacle.getFitWidth() - buffer * 2;
            double obstacleHeight = obstacle.getFitHeight() - buffer * 2;

            // Check if the car's buffered area intersects the obstacle's buffered area
            if (carX < obstacleX + obstacleWidth &&
                carX + carWidth > obstacleX &&
                carY < obstacleY + obstacleHeight &&
                carY + carHeight > obstacleY) {
                playCollisionSound();  // Play collision sound
                gameOver();
                break;
            }
        }
    }

    private void updateScore() {
        score++;
        scoreLabel.setText("Score: " + score);
    }

    private void gameOver() {
        gameRunning = false;
        gameLoop.stop();  // Stop the game loop

        // Stop background music when game is over
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
        }

        // Show the Game Over dialog with a colorful exit box
        Platform.runLater(() -> {
            Stage gameOverStage = new Stage();
            gameOverStage.setTitle("Game Over");

            // Set up the game over layout
            Pane pane = new Pane();
            Scene gameOverScene = new Scene(pane, 300, 250); // Adjusted size to fit the score

            // Colorful background for the game over screen
            pane.setStyle("-fx-background-color: #FF6347; -fx-border-color: #FFD700; -fx-border-width: 5px;");

            // Game Over Label
            Label gameOverLabel = new Label("Game Over!");
            gameOverLabel.setLayoutX(100);
            gameOverLabel.setLayoutY(50);
            gameOverLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");
            pane.getChildren().add(gameOverLabel);

            // Display the final score
            Label scoreLabelGameOver = new Label("Score: " + score);
            scoreLabelGameOver.setLayoutX(100);
            scoreLabelGameOver.setLayoutY(100);
            scoreLabelGameOver.setStyle("-fx-font-size: 18px; -fx-text-fill: yellow;");
            pane.getChildren().add(scoreLabelGameOver);

            // Exit button
            Button exitButton = new Button("Exit");
            exitButton.setLayoutX(100);
            exitButton.setLayoutY(150);
            exitButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");
            exitButton.setOnAction(event -> Platform.exit()); // Exit the application
            pane.getChildren().add(exitButton);

            gameOverStage.setScene(gameOverScene);
            gameOverStage.show(); // Show the Game Over dialog
        });
    }
}