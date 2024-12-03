import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class WhacARoach {

    int boardWidth = 600;
    int boardHeight = 650;

    JFrame frame = new JFrame("Whack a Roach");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JPanel controlPanel = new JPanel();

    JButton[] board = new JButton[9];
    ImageIcon moleIcon;
    ImageIcon plantIcon;
    ImageIcon animeCatGirlIcon;

    JButton currMoleTile;
    JButton[] currPlantTiles;
    JButton[] currCatGirlTiles;

    Random random = new Random();
    Timer setMoleTimer;
    Timer setPlantTimer;
    Timer gameTimer;

    int score;
    int moleSpeed = 1000;
    int plantSpeed = 1500;
    int maxPlants = 1;
    int gameDuration = 0;

    Cursor customCursor;

    WhacARoach() {
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        textLabel.setFont(new Font("Arial", Font.PLAIN, 50));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Score: 0");
        textLabel.setOpaque(true);

        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel);
        frame.add(textPanel, BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(3, 3));
        frame.add(boardPanel, BorderLayout.CENTER);

        controlPanel.setLayout(new FlowLayout());
        JButton startButton = new JButton("Start");
        startButton.setFont(new Font("Arial", Font.BOLD, 20));
        controlPanel.add(startButton);

        JButton restartButton = new JButton("Restart");
        restartButton.setFont(new Font("Arial", Font.BOLD, 20));
        restartButton.setEnabled(false); // Initially disabled
        controlPanel.add(restartButton);

        frame.add(controlPanel, BorderLayout.SOUTH);

        plantIcon = loadImage("piranha.png", 150, 150);
        moleIcon = loadImage("monty.png", 150, 150);
        animeCatGirlIcon = loadImage("anime_cat_girl.png", 150, 150);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image cursorImage = toolkit.getImage(getClass().getResource("hammer.png"));
        Point cursorHotSpot = new Point(0, 0);
        customCursor = toolkit.createCustomCursor(cursorImage, cursorHotSpot, "Hammer Cursor");

        currPlantTiles = new JButton[9];
        currCatGirlTiles = new JButton[9];
        score = 0;

        for (int i = 0; i < 9; i++) {
            JButton tile = new JButton();
            board[i] = tile;
            boardPanel.add(tile);
            tile.setFocusable(false);
            tile.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JButton tile = (JButton) e.getSource();
                    if (tile == currMoleTile) {
                        playSound("mole_hit.wav");
                        score += 10;
                        textLabel.setText("Score: " + score);
                        currMoleTile.setIcon(null);
                        currMoleTile = null;
                    } else {
                        for (int j = 0; j < maxPlants; j++) {
                            if (tile == currPlantTiles[j]) {
                                playSound("plant_hit.wav");
                                textLabel.setText("Game Over: " + score);
                                stopGame();
                                restartButton.setEnabled(true);
                                return;
                            }
                        }
                        for (int j = 0; j < maxPlants; j++) {
                            if (tile == currCatGirlTiles[j]) {
                                playSound("cat_girl_hit.wav");
                                textLabel.setText("Game Over: " + score);
                                stopGame();
                                restartButton.setEnabled(true);
                                return;
                            }
                        }
                    }
                }
            });
        }

        setMoleTimer = new Timer(moleSpeed, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (currMoleTile != null) {
                    currMoleTile.setIcon(null);
                    currMoleTile = null;
                }
                int num = random.nextInt(9);
                JButton tile = board[num];
                if (tileOccupiedByPlant(tile) || tileOccupiedByCatGirl(tile)) {
                    return;
                }
                currMoleTile = tile;
                currMoleTile.setIcon(moleIcon);
            }
        });

        setPlantTimer = new Timer(plantSpeed, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearTiles();
                for (int i = 0; i < maxPlants; i++) {
                    int num;
                    JButton tile;
                    do {
                        num = random.nextInt(9);
                        tile = board[num];
                    } while (tile == currMoleTile || tileOccupiedByPlant(tile) || tileOccupiedByCatGirl(tile));

                    if (random.nextInt(5) == 0) {
                        currCatGirlTiles[i] = tile;
                        currCatGirlTiles[i].setIcon(animeCatGirlIcon);
                    } else {
                        currPlantTiles[i] = tile;
                        currPlantTiles[i].setIcon(plantIcon);
                    }
                }
            }
        });

        gameTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gameDuration++;
                if (gameDuration % 10 == 0) {
                    if (moleSpeed > 300) {
                        moleSpeed -= 100;
                        setMoleTimer.setDelay(moleSpeed);
                    }
                    if (plantSpeed > 500) {
                        plantSpeed -= 100;
                        setPlantTimer.setDelay(plantSpeed);
                    }
                }
                if (gameDuration % 20 == 0 && maxPlants < 5) {
                    maxPlants++;
                }
            }
        });

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startButton.setEnabled(false);
                restartButton.setEnabled(false);
                frame.setCursor(customCursor);
                setPlantTimer.start();
                setMoleTimer.start();
                gameTimer.start();
            }
        });

        restartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetGame();
                startButton.setEnabled(true);
                restartButton.setEnabled(false);
                frame.setCursor(Cursor.getDefaultCursor());
            }
        });

        frame.setVisible(true);
    }

    private void resetGame() {
        score = 0;
        gameDuration = 0;
        moleSpeed = 1000;
        plantSpeed = 1500;
        maxPlants = 1;

        textLabel.setText("Score: 0");

        for (JButton tile : board) {
            tile.setEnabled(true);
            tile.setIcon(null);
        }

        setMoleTimer.setDelay(moleSpeed);
        setPlantTimer.setDelay(plantSpeed);
        setMoleTimer.start();
        setPlantTimer.start();
        gameTimer.start();
    }

    private void clearTiles() {
        for (int i = 0; i < maxPlants; i++) {
            if (currPlantTiles[i] != null) {
                currPlantTiles[i].setIcon(null);
                currPlantTiles[i] = null;
            }
            if (currCatGirlTiles[i] != null) {
                currCatGirlTiles[i].setIcon(null);
                currCatGirlTiles[i] = null;
            }
        }
    }

    private boolean tileOccupiedByPlant(JButton tile) {
        for (int i = 0; i < maxPlants; i++) {
            if (tile == currPlantTiles[i]) {
                return true;
            }
        }
        return false;
    }

    private boolean tileOccupiedByCatGirl(JButton tile) {
        for (int i = 0; i < maxPlants; i++) {
            if (tile == currCatGirlTiles[i]) {
                return true;
            }
        }
        return false;
    }

    private void stopGame() {
        setMoleTimer.stop();
        setPlantTimer.stop();
        playSound("game_over.wav");
        gameTimer.stop();
        showScoreDialog(); // Display the message based on the score
    }

    private void showScoreDialog() {
        String message = (score >= 1000) ? "God Among Men" : "Skill Issue";
        JOptionPane.showMessageDialog(frame, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    private ImageIcon loadImage(String imagePath, int width, int height) {
        Image img = new ImageIcon(getClass().getResource(imagePath)).getImage();
        return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    public void playSound(String soundFileName) {
        try {
            File soundFile = new File(getClass().getResource(soundFileName).getFile());
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}

