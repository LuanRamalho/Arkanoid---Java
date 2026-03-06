import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*; // Necessário para manipulação de arquivos

public class Gameplay extends JPanel implements MouseMotionListener, ActionListener {
    private boolean play = false;
    private int score = 0;
    private int highScore = 0; // Variável para o recorde
    private int totalBricks = 64;

    private Timer timer;
    private int delay = 5; 

    private int playerX = 310;
    private int ballposX = 350;
    private int ballposY = 450;
    private int ballXdir = -2;
    private int ballYdir = -4;

    private MapGenerator map;
    private final String highScoreFile = "highscore.txt";

    public Gameplay() {
        map = new MapGenerator(8, 8);
        highScore = loadHighScore(); // Carrega o recorde ao iniciar
        addMouseMotionListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        timer = new Timer(delay, this);
        timer.start();
    }

    // Método para carregar o Recorde do arquivo
    private int loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(highScoreFile))) {
            return Integer.parseInt(reader.readLine());
        } catch (IOException | NumberFormatException e) {
            return 0; // Se o arquivo não existir, o recorde é 0
        }
    }

    // Método para salvar o Recorde no arquivo
    private void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(highScoreFile))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void paint(Graphics g) {
        // Fundo
        g.setColor(Color.black);
        g.fillRect(1, 1, 692, 592);

        map.draw((Graphics2D) g);

        // Pontuação Atual e Recorde
        g.setColor(Color.white);
        g.setFont(new Font("serif", Font.BOLD, 20));
        g.drawString("Score: " + score, 50, 30);
        g.setColor(Color.YELLOW);
        g.drawString("High Score: " + highScore, 500, 30);

        // Palheta
        g.setColor(Color.green);
        g.fillRect(playerX, 550, 100, 8);

        // Bola
        g.setColor(Color.yellow);
        g.fillOval(ballposX, ballposY, 20, 20);

        // Game Over
        if (ballposY > 570) {
            play = false;
            g.setColor(Color.RED);
            g.setFont(new Font("serif", Font.BOLD, 30));
            g.drawString("GAME OVER!", 250, 300);
            
            if(score >= highScore) {
                g.setColor(Color.YELLOW);
                g.drawString("NOVO RECORDE: " + score, 210, 340);
            }
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("serif", Font.BOLD, 20));
            g.drawString("Mova o mouse para reiniciar", 220, 380);
        }

        g.dispose();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if(!play && ballposY > 570) {
            resetGame();
        }
        play = true;
        playerX = e.getX() - 50;
        if (playerX >= 590) playerX = 590;
        if (playerX < 5) playerX = 5;
        repaint();
    }

    private void resetGame() {
        score = 0;
        totalBricks = 64;
        ballposX = 350;
        ballposY = 450;
        ballXdir = -2;
        ballYdir = -4;
        map = new MapGenerator(8, 8);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (play) {
            // Loop Infinito: Se quebrar tudo, reseta o mapa e aumenta velocidade
            if (totalBricks <= 0) {
                map = new MapGenerator(8, 8);
                totalBricks = 64;
                ballXdir = (ballXdir > 0) ? ballXdir + 1 : ballXdir - 1;
                ballYdir = (ballYdir > 0) ? ballYdir + 1 : ballYdir - 1;
            }

            // Colisão Paddle
            if (new Rectangle(ballposX, ballposY, 20, 20).intersects(new Rectangle(playerX, 550, 100, 8))) {
                ballYdir = -ballYdir;
            }

            // Colisão Blocos
            A: for (int i = 0; i < map.map.length; i++) {
                for (int j = 0; j < map.map[0].length; j++) {
                    if (map.map[i][j] > 0) {
                        int brickX = j * map.brickWidth + 80;
                        int brickY = i * map.brickHeight + 50;
                        Rectangle rect = new Rectangle(brickX, brickY, map.brickWidth, map.brickHeight);
                        if (new Rectangle(ballposX, ballposY, 20, 20).intersects(rect)) {
                            map.setBrickValue(0, i, j);
                            totalBricks--;
                            score += (8 - i) * 10;

                            // Atualiza o High Score em tempo real
                            if (score > highScore) {
                                highScore = score;
                                saveHighScore(); // Salva no arquivo
                            }

                            if (ballposX + 19 <= rect.x || ballposX + 1 >= rect.x + rect.width) ballXdir = -ballXdir;
                            else ballYdir = -ballYdir;
                            break A;
                        }
                    }
                }
            }

            ballposX += ballXdir;
            ballposY += ballYdir;

            if (ballposX < 0 || ballposX > 670) ballXdir = -ballXdir;
            if (ballposY < 0) ballYdir = -ballYdir;
        }
        repaint();
    }

    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }
}
