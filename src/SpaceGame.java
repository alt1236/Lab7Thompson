/**
 * Project: Solo Lab 7 Assignment
 * Purpose Details: A 2D space shooter game using Java where the player avoids and shoots obstacles.
 * Course: IST 242
 * Author: Alyssa Thompson
 * Date Developed: 4/28
 * Last Date Changed: 4/29
 * Rev: 2.0
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;

/**
 * Main game class that creates the window and handles gameplay logic.
 */
public class SpaceGame extends JFrame implements KeyListener {
    /** Width of the game window */
    private static final int WIDTH = 500;
    /** Height of the game window */
    private static final int HEIGHT = 500;
    /** Player dimensions */
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 50;
    /** Obstacle dimensions */
    private static final int OBSTACLE_WIDTH = 20;
    private static final int OBSTACLE_HEIGHT = 20;
    /** Projectile dimensions */
    private static final int PROJECTILE_WIDTH = 5;
    private static final int PROJECTILE_HEIGHT = 10;
    /** Movement speeds */
    private static final int PLAYER_SPEED = 50;
    private static final int OBSTACLE_SPEED = 3;
    private static final int PROJECTILE_SPEED = 10;

    /** Player stats */
    private int score = 0;
    private int health = 100;
    private int level = 1;
    private int timeLeft = 60;

    /** JPanel */
    private JPanel gamePanel;
    private JLabel scoreLabel;
    private JLabel healthLabel;
    private JLabel timerLabel;

    /** Game loop timer */
    private Timer timer;

    /** Indicates if the game is over */
    private boolean isGameOver;

    /** Player position */
    private int playerX, playerY;

    /** Projectile position */
    private int projectileX, projectileY;

    /** Projectile visibility flag */
    private boolean isProjectileVisible;

    /** Prevents rapid firing */
    private boolean isFiring;

    /** Lists */
    private List<Point> obstacles = new ArrayList<>();
    private List<Point> stars = new ArrayList<>();
    private List<Point> healthPacks = new ArrayList<>();

    /** Player ship image */
    private BufferedImage shipImage;

    /** Sprite sheet for obstacles */
    private BufferedImage spriteSheet;

    /** Sprite dimensions */
    private int spriteWidth = 64;
    private int spriteHeight = 64;

    /** Sound clip for firing */
    private Clip clip;

    /** Shield state */
    private boolean shieldActive = false;
    /** Shield duration in milliseconds */
    private int shieldDuration = 5000;
    /** Time when shield was activated */
    private long shieldStarTime;

    private long lastSecondTick = System.currentTimeMillis();

    /**
     * Generates random star positions.
     *
     * @param numStars number of stars to generate
     * @return list of star positions
     */
    private List<Point> generateStars(int numStars){
        List<Point> starsList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < numStars; i++){
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            starsList.add(new Point(x,y));
        }
        return starsList;
    }

    /** Activates shield */
    private void activateShield(){
        shieldActive = true;
        shieldStarTime = System.currentTimeMillis();
    }
    /** Deactivates shield */
    private void deactivateShield(){
        shieldActive = false;
    }
    /**
     * Checks if shield is active.
     *
     * @return true if active
     */
    private boolean isShieldActive(){
        return shieldActive && (System.currentTimeMillis() - shieldStarTime) < shieldDuration;
    }
    /** Resets game state */
    private void reset(){
        score = 0;
        isGameOver = false;
        repaint();
    }
    /** Constructor initializes game */
    public SpaceGame() {

        try {
            shipImage = ImageIO.read(new File("ship.png"));
            spriteSheet = ImageIO.read(new File("astro.png"));
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("fire.wav").getAbsoluteFile());
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        } catch (UnsupportedAudioFileException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        setTitle("Space Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gamePanel = new JPanel() {
            /**
             * Paints game graphics.
             */
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };

        scoreLabel = new JLabel("Score: 0 | Level: 1");
        scoreLabel.setForeground(Color.BLUE);
        gamePanel.add(scoreLabel);

        healthLabel = new JLabel("Health: 100");
        healthLabel.setForeground(Color.RED);
        gamePanel.add(healthLabel);

        timerLabel = new JLabel("Time: 60");
        timerLabel.setForeground(Color.YELLOW);
        gamePanel.add(timerLabel);

        add(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);

        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 20;
        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
        projectileY = playerY;
        isProjectileVisible = false;
        isGameOver = false;
        isFiring = false;
        obstacles = new java.util.ArrayList<>();

        timer = new Timer(20, e -> {
            if (!isGameOver) {
                update();
                repaint();
            }
        });
        timer.start();
    }

    /** Plays firing sound */
    public void playSound(){
        if(clip != null){
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public static Color generateRandomColor(){
        Random rand = new Random();
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        return new Color(r,g,b);
    }

    /**
     * Draws all game elements.
     *
     * @param g graphics object
     */
    private void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.drawImage(shipImage, playerX, playerY, null);

        //g.setColor(Color.BLUE);
        //g.fillRect(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

        if (isProjectileVisible) {
            g.setColor(Color.GREEN);
            g.fillRect(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }

        //g.setColor(Color.RED);
        //for (Point obstacle : obstacles) {
        //    g.fillRect(obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
        //}

        for(Point obstacle : obstacles){
            if(spriteSheet != null){
                Random random = new Random();
                int spriteIndex = random.nextInt(4);

                int spriteX = spriteIndex * spriteWidth;
                int spriteY = 0;

                g.drawImage(spriteSheet.getSubimage(spriteX,spriteY,spriteWidth,
                        spriteHeight),obstacle.x, obstacle.y, null);
            }
        }

        g.setColor(Color.GREEN);
        for (Point p : healthPacks){
            g.fillRect(p.x, p.y, 15,15);
        }

        g.setColor(generateRandomColor());
        //g.setColor(Color.WHITE);
        for (Point star : stars){
            g.fillOval(star.x, star.y, 2, 2);
        }

        if (isShieldActive()){
            g.setColor(new Color(0,255,255,100));
            g.fillOval(playerX, playerY, 60,60);
        }

        if (isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over!", WIDTH / 2 - 80, HEIGHT / 2);
        }
    }

    /** Updates game state */
    private void update() {

        // Timer countdown
        if (System.currentTimeMillis() - lastSecondTick >= 1000){
            timeLeft--;
            timerLabel.setText("Time: " + timeLeft);
            lastSecondTick = System.currentTimeMillis();
            if (timeLeft <= 0) isGameOver = true;
        }

        // Level scaling
        if (score >= 100) level = 3;
        else if (score >= 50) level = 2;

        scoreLabel.setText("Score: " + score + " | Level: " + level);
        healthLabel.setText("Health: " + health);

        // Move obstacles
        for (int i = 0; i < obstacles.size(); i++) {
            obstacles.get(i).y += OBSTACLE_SPEED + level;
            if (obstacles.get(i).y > HEIGHT) {
                obstacles.remove(i);
                i--;
            }
        }

        // Spawn obstacles
        if (Math.random() < 0.02 + level * 0.01) {
            int obstacleX = (int) (Math.random() * (WIDTH - OBSTACLE_WIDTH));
            obstacles.add(new Point(obstacleX, 0));
        }

        // Spawn health packs
        if (Math.random() < 0.01){
            healthPacks.add(new Point(new Random().nextInt(WIDTH),0));
        }

        // Move health packs
        for (Point p : healthPacks) {
            p.y += 2;
        }

        // Stars refresh
        if (Math.random() < 0.1){
            stars = generateStars(200);
        }

        // Move projectile
        if (isProjectileVisible) {
            projectileY -= PROJECTILE_SPEED;
            if (projectileY < 0) {
                isProjectileVisible = false;
            }
        }

        Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

        // Player vs obstacles
        for (int i = 0; i < obstacles.size(); i++) {
            Rectangle r = new Rectangle(obstacles.get(i).x, obstacles.get(i).y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
            if (playerRect.intersects(r) && !isShieldActive()) {
                health -= 20;
                obstacles.remove(i);
                if (health <= 0) isGameOver = true;
                break;
            }
        }

        // Player vs health packs
        for (int i = 0; i < healthPacks.size(); i++){
            Rectangle rect = new Rectangle(healthPacks.get(i).x, healthPacks.get(i).y,15,15);
            if (playerRect.intersects(rect)){
                health = Math.min(100,health + 20);
                healthPacks.remove(i);
                break;
            }
        }

        // Projectile collisions
        Rectangle projectileRect = new Rectangle(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        for (int i = 0; i < obstacles.size(); i++) {
            Rectangle obstacleRect = new Rectangle(obstacles.get(i).x, obstacles.get(i).y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
            if (projectileRect.intersects(obstacleRect)) {
                obstacles.remove(i);
                score += 10;
                isProjectileVisible = false;
                break;
            }
        }
    }

    /** Handles key press events */
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT && playerX > 0) {
            playerX -= PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_RIGHT && playerX < WIDTH - PLAYER_WIDTH) {
            playerX += PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            reset();
        } else if (keyCode == KeyEvent.VK_CONTROL){
            activateShield();
        } else if (keyCode == KeyEvent.VK_SPACE && !isFiring) {
            playSound();
            isFiring = true;
            projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
            projectileY = playerY;
            isProjectileVisible = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500); // Limit firing rate
                        isFiring = false;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    /** Launches the game */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SpaceGame().setVisible(true);
            }
        });
    }
}

