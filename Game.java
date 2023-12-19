import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

/**
 * This is a simple whack-a-mole game. The game has 64 buttons, and one mole
 * will appear randomly on one of the buttons
 * every second. The player needs to click on the mole to get a point. The game
 * lasts for 20 seconds. After the game
 * ends, the player can click on the start button to start a new game.
 *
 * @author Nirbhay Bagmar (nbagmar@andrew.cmu.edu)
 */
public class Game extends JFrame implements ActionListener {

    /**
     * Off string constant.
     */
    private static final String OFF_STRING = ":-(";
    /**
     * On string constant.
     */
    private static final String ON_STRING = ":-)";
    /**
     * Off color constant.
     */
    private static final Color OFF_COLOR = Color.RED;
    /**
     * On color constant.
     */
    private static final Color ON_COLOR = Color.GREEN;

    /**
     * The start button.
     */
    private JButton startButton;

    /**
     * The score field.
     */
    private JTextField scoreField;

    /**
     * The time field.
     */
    private JTextField timeField;

    /**
     * The mole buttons.
     */
    private ArrayList<JButton> buttons;

    /**
     * The mole threads.
     */
    private ArrayList<Thread> moleThreads = new ArrayList<>();

    /**
     * The game running flag.
     */
    private boolean gameRunning = true;

    public Game() {
        // Create the window.
        JFrame window = new JFrame("Whack-a-Mole Game");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(520, 420);

        // Create the main panel.
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBounds(EXIT_ON_CLOSE, ABORT, WIDTH, HEIGHT);

        JPanel panel = new JPanel();
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // add components to the panel.
        startButton = new JButton("Start");

        JLabel timeLabel = new JLabel("Time Left: ");
        timeField = new JTextField("0", 5);
        JLabel scoreLabel = new JLabel("Score: ");
        scoreField = new JTextField("0", 5);
        buttons = new ArrayList<>();

        startButton.addActionListener(this);
        scoreField.setEditable(false);
        timeField.setEditable(false);
        panel.add(startButton);
        panel.add(scoreLabel);
        panel.add(scoreField);
        panel.add(timeLabel);
        panel.add(timeField);

        // create the buttons.
        for (int i = 0; i < 12; i++) {
            JButton button = new JButton();
            button.setBackground(Color.RED);
            button.addActionListener(this);
            button.setOpaque(true);
            buttons.add(button);
            buttonPanel.add(button);
        }

        mainPanel.add(panel);
        mainPanel.add(buttonPanel);
        window.add(mainPanel);
        window.setVisible(true);
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        new Game();
    }

    /**
     * The game timer class implements runnable making in a thread.
     */
    private class GameTimer implements Runnable {

        /**
         * The run method for game timer thread.
         */
        @Override
        public void run() {
            int count = 20;
            while (count > 0) {
                try {
                    Thread.sleep(1000);
                    count--;
                    timeField.setText(Integer.toString(count));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            gameRunning = false;

            // Disable all the buttons.
            for (JButton button : buttons) {
                button.setEnabled(false);
                if (button.getText().equals(ON_STRING)) {
                    button.setText("");
                    button.setBackground(OFF_COLOR);
                }
            }

            // Wait for 5 seconds before enabling the start button.
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Enable the start button and all the mole buttons.
            for (JButton button : buttons) {
                button.setEnabled(true);
            }
            startButton.setEnabled(true);
        }
    }

    /**
     * The mole class implements runnable making in a thread.
     */
    private class Mole implements Runnable {

        /**
         * The button.
         */
        private JButton button;

        /**
         * Instantiates a new mole.
         *
         * @param btn the button
         */
        Mole(JButton btn) {
            this.button = btn;
        }

        @Override
        public void run() {
            Random rand = new Random();

            // if game is running, then keep the mole up for a random time.
            while (gameRunning) {
                try {
                    if (button.getText().equals("")) {
                        int upTime = 1000 + rand.nextInt(2500);
                        button.setText(ON_STRING);
                        button.setBackground(ON_COLOR);
                        Thread.sleep(upTime);
                        button.setText("");
                        button.setBackground(OFF_COLOR);
                        Thread.sleep(2000);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    /**
     * The action performed method for the action listener checks for start button.
     * If start button is clicked, then start the game. If a mole button is clicked,
     * then check if it is up. If it is up, then increment the score.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        // If start button is clicked, then start the game.
        if (e.getSource() == startButton) {
            startButton.setEnabled(false);
            scoreField.setText("0");
            gameRunning = true;

            for (Thread moleThread : moleThreads) {
                moleThread.interrupt();
            }
            moleThreads.clear();

            // start a timer thread and 30 mole threads.
            Thread timer = new Thread(new GameTimer());
            timer.start();

            for (int i = 0; i < 12; i++) {
                Thread mole = new Thread(new Mole(buttons.get(i)));
                moleThreads.add(mole);
                mole.start();
            }
        } else {
            // If a mole button is clicked, then check if it is up.
            JButton button = (JButton) e.getSource();
            synchronized (button) {
                if (button.getText().equals(ON_STRING)) {
                    int score = Integer.parseInt(scoreField.getText());
                    score = score + 1;
                    scoreField.setText(Integer.toString(score));
                    button.setText(OFF_STRING);
                    button.setBackground(OFF_COLOR);
                }
            }
        }
    }
}
