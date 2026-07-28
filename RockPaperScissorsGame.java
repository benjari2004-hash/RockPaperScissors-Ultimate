
import javax.swing.*;
import javax.swing.Timer;  // Explicitly import Swing Timer
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.border.EmptyBorder;

public class RockPaperScissorsGame extends JFrame {
    private static final String ROCK = "ROCK";
    private static final String PAPER = "PAPER";
    private static final String SCISSORS = "SCISSORS";
    
    // UI Components
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private AnimatedButton pvpButton, pvcButton, backButton, newGameButton;
    private JLabel countdownLabel;
    private JLabel resultLabel;
    private JLabel scoreLabel;
    private GameGesturePanel player1Panel, player2Panel;
    private JPanel gamePanel;
    private JLabel roundLabel;
    
    // Game state
    private Timer countdownTimer, animationTimer;
    private int countdown;
    private boolean gameActive;
    private String gameMode;
    private String player1Choice = "";
    private String player2Choice = "";
    private boolean player1Pressed = false;
    private boolean player2Pressed = false;
    private boolean player1Cheated = false;
    private boolean player2Cheated = false;
    
    // Multi-round game
    private int totalRounds = 3;
    private int currentRound = 1;
    private int player1Wins = 0;
    private int player2Wins = 0;
    private int draws = 0;
    
    // AI Strategy
    private List<String> playerHistory = new ArrayList<>();
    private Random random = new Random();
    
    // Animation variables
    private float pulseScale = 1.0f;
    private boolean pulseDirection = true;
    
    public RockPaperScissorsGame() {
        setTitle("Rock Paper Scissors - Ultimate Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        
        initializeComponents();
        showMainMenu();
        
        addKeyListener(new GameKeyListener());
        setFocusable(true);
        requestFocus();
        
        // Start animation timer
        startAnimationTimer();
    }
    
    private void initializeComponents() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(15, 25, 35));
        
        add(mainPanel);
    }
    
    private void startAnimationTimer() {
        animationTimer = new Timer(50, e -> {
            if (pulseDirection) {
                pulseScale += 0.02f;
                if (pulseScale >= 1.1f) pulseDirection = false;
            } else {
                pulseScale -= 0.02f;
                if (pulseScale <= 0.9f) pulseDirection = true;
            }
            repaint();
        });
        animationTimer.start();
    }
    
    private void showMainMenu() {
        JPanel menuPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(0, 0, new Color(15, 25, 35), 
                                                         0, getHeight(), new Color(25, 45, 65));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Animated background elements
                drawBackgroundElements(g2d);
            }
        };
        menuPanel.setLayout(new BorderLayout());
        
        // Title Panel
        JPanel titlePanel = createTitlePanel();
        menuPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Center Panel with buttons
        JPanel centerPanel = createMenuCenterPanel();
        menuPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Instructions Panel
        JPanel instructionsPanel = createInstructionsPanel();
        menuPanel.add(instructionsPanel, BorderLayout.SOUTH);
        
        mainPanel.add(menuPanel, "MENU");
        cardLayout.show(mainPanel, "MENU");
        requestFocus();
    }
    
    private void drawBackgroundElements(Graphics2D g2d) {
        // Animated floating shapes
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
        
        for (int i = 0; i < 5; i++) {
            float scale = pulseScale + (i * 0.1f);
            int size = (int)(40 * scale);
            int x = 100 + i * 200;
            int y = 100 + (int)(50 * Math.sin(System.currentTimeMillis() / 1000.0 + i));
            
            g2d.setColor(new Color(70, 130, 180));
            g2d.fillOval(x, y, size, size);
        }
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(40, 0, 30, 0));
        
        JLabel titleLabel = new JLabel("ROCK PAPER SCISSORS", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Text shadow
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2;
                g2d.drawString(getText(), x + 3, y + 3);
                
                // Main text with gradient
                GradientPaint textGradient = new GradientPaint(0, 0, new Color(255, 215, 0), 
                                                             0, getHeight(), new Color(255, 140, 0));
                g2d.setPaint(textGradient);
                g2d.drawString(getText(), x, y);
            }
        };
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(new Color(255, 215, 0));
        
        JLabel subtitleLabel = new JLabel("Ultimate Edition", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 18));
        subtitleLabel.setForeground(new Color(200, 200, 200));
        
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        titlePanel.add(subtitleLabel);
        
        return titlePanel;
    }
    
    private JPanel createMenuCenterPanel() {
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        
        // Create animated buttons
        pvpButton = new AnimatedButton("🥊 PLAYER VS PLAYER 🥊", new Color(220, 20, 60), new Color(255, 69, 0));
        pvcButton = new AnimatedButton("🤖 PLAYER VS COMPUTER 🤖", new Color(70, 130, 180), new Color(100, 149, 237));
        
        pvpButton.addActionListener(e -> startGame("PVP"));
        pvcButton.addActionListener(e -> startGame("PVC"));
        
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(pvpButton);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(pvcButton);
        centerPanel.add(Box.createVerticalGlue());
        
        return centerPanel;
    }
    
    private JPanel createInstructionsPanel() {
        JPanel instructionsPanel = new JPanel();
        instructionsPanel.setOpaque(false);
        instructionsPanel.setBorder(new EmptyBorder(20, 40, 30, 40));
        
        JLabel instructionsLabel = new JLabel("<html><center>" +
            "<div style='color: #CCCCCC; font-size: 14px;'>" +
            "<b>CONTROLS:</b><br>" +
            "🔵 PvP Mode: Player 1 [A-Rock, S-Paper, D-Scissors] | Player 2 [J-Rock, K-Paper, L-Scissors]<br>" +
            "🔴 PvC Mode: [SPACE-Rock, B-Paper, N-Scissors]<br>" +
            "<i>Remember: Only press once during countdown or you'll be flagged for cheating!</i>" +
            "</div>" +
            "</center></html>");
        instructionsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        instructionsPanel.add(instructionsLabel);
        return instructionsPanel;
    }
    
    private void startGame(String mode) {
        gameMode = mode;
        currentRound = 1;
        player1Wins = 0;
        player2Wins = 0;
        draws = 0;
        playerHistory.clear();
        
        showGameScreen();
        startRound();
    }
    
    private void showGameScreen() {
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(0, 0, new Color(25, 25, 35), 
                                                         0, getHeight(), new Color(45, 45, 65));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        gamePanel.setLayout(new BorderLayout());
        
        // Top panel
        JPanel topPanel = createGameTopPanel();
        gamePanel.add(topPanel, BorderLayout.NORTH);
        
        // Center panel
        JPanel centerPanel = createGameCenterPanel();
        gamePanel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel
        JPanel bottomPanel = createGameBottomPanel();
        gamePanel.add(bottomPanel, BorderLayout.SOUTH);
        
        mainPanel.add(gamePanel, "GAME");
        cardLayout.show(mainPanel, "GAME");
        requestFocus();
    }
    
    private JPanel createGameTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        
        roundLabel = new JLabel("ROUND " + currentRound + " OF " + totalRounds, SwingConstants.CENTER);
        roundLabel.setFont(new Font("Arial", Font.BOLD, 24));
        roundLabel.setForeground(new Color(255, 215, 0));
        
        backButton = new AnimatedButton("← MENU", new Color(100, 100, 100), new Color(150, 150, 150));
        backButton.setPreferredSize(new Dimension(120, 40));
        backButton.addActionListener(e -> showMainMenu());
        
        topPanel.add(roundLabel, BorderLayout.CENTER);
        topPanel.add(backButton, BorderLayout.EAST);
        
        return topPanel;
    }
    
    private JPanel createGameCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        
        // Countdown label
        countdownLabel = new JLabel("", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (!getText().isEmpty() && gameActive) {
                    // Pulsing effect for countdown
                    Font originalFont = getFont();
                    float scaledSize = originalFont.getSize() * pulseScale;
                    g2d.setFont(originalFont.deriveFont(scaledSize));
                    
                    // Text shadow
                    g2d.setColor(new Color(0, 0, 0, 150));
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(getText())) / 2;
                    int y = (getHeight() + fm.getAscent()) / 2;
                    g2d.drawString(getText(), x + 4, y + 4);
                    
                    // Main text
                    g2d.setColor(getForeground());
                    g2d.drawString(getText(), x, y);
                } else {
                    super.paintComponent(g);
                }
            }
        };
        countdownLabel.setFont(new Font("Arial", Font.BOLD, 72));
        countdownLabel.setForeground(new Color(255, 69, 0));
        countdownLabel.setBorder(new EmptyBorder(30, 0, 30, 0));
        
        // Players panel
        JPanel playersPanel = new JPanel(new GridLayout(1, 2, 50, 0));
        playersPanel.setOpaque(false);
        playersPanel.setBorder(new EmptyBorder(20, 50, 20, 50));
        
        player1Panel = new GameGesturePanel(gameMode.equals("PVP") ? "PLAYER 1" : "YOU");
        player2Panel = new GameGesturePanel(gameMode.equals("PVP") ? "PLAYER 2" : "COMPUTER");
        
        playersPanel.add(player1Panel);
        playersPanel.add(player2Panel);
        
        // Result label
        resultLabel = new JLabel("", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 28));
        resultLabel.setForeground(new Color(255, 215, 0));
        resultLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        centerPanel.add(countdownLabel, BorderLayout.NORTH);
        centerPanel.add(playersPanel, BorderLayout.CENTER);
        centerPanel.add(resultLabel, BorderLayout.SOUTH);
        
        return centerPanel;
    }
    
    private JPanel createGameBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        
        scoreLabel = new JLabel("", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 18));
        scoreLabel.setForeground(new Color(200, 200, 200));
        
        bottomPanel.add(scoreLabel);
        return bottomPanel;
    }
    
    private void startRound() {
        resetRoundState();
        countdown = 3;
        gameActive = true;
        
        countdownLabel.setText("GET READY!");
        resultLabel.setText("");
        player1Panel.setGesture("❓");
        player2Panel.setGesture("❓");
        player1Panel.setReady(false);
        player2Panel.setReady(false);
        
        roundLabel.setText("ROUND " + currentRound + " OF " + totalRounds);
        scoreLabel.setText(getScoreText());
        
        Timer readyTimer = new Timer(1500, e -> startCountdown());
        readyTimer.setRepeats(false);
        readyTimer.start();
    }
    
    private void startCountdown() {
        // Stop any existing countdown timer
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        
        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (countdown > 0) {
                    countdownLabel.setText(String.valueOf(countdown));
                    playSound();
                    countdown--;
                } else {
                    countdownTimer.stop();
                    endRound();
                }
            }
        });
        countdownTimer.setRepeats(true);
        countdownTimer.start();
    }
    
    private void resetRoundState() {
        player1Choice = "";
        player2Choice = "";
        player1Pressed = false;
        player2Pressed = false;
        player1Cheated = false;
        player2Cheated = false;
    }
    
    private void endRound() {
        gameActive = false;
        countdownLabel.setText("SHOW!");
        
        // Stop countdown timer if running
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        
        // Handle computer choice in PvC mode
        if (gameMode.equals("PVC")) {
            player2Choice = getComputerChoice();
        }
        
        // Display gestures with animation
        player1Panel.setGesture(getGestureEmoji(player1Choice));
        player2Panel.setGesture(getGestureEmoji(player2Choice));
        player1Panel.setReady(true);
        player2Panel.setReady(true);
        
        // Determine winner
        String roundResult = determineWinner();
        resultLabel.setText(roundResult);
        
        // Update scores
        updateScores(roundResult);
        scoreLabel.setText(getScoreText());
        
        // Add to player history for AI
        if (gameMode.equals("PVC") && !player1Choice.isEmpty()) {
            playerHistory.add(player1Choice);
        }
        
        // Check if game is complete
        Timer resultTimer = new Timer(3000, e -> {
            ((Timer)e.getSource()).stop(); // Stop this timer
            if (isGameComplete()) {
                showFinalResult();
            } else {
                currentRound++;
                showGameScreen();
                startRound();
            }
        });
        resultTimer.setRepeats(false);
        resultTimer.start();
    }
    
    private String getComputerChoice() {
        if (playerHistory.size() < 2) {
            String[] choices = {ROCK, PAPER, SCISSORS};
            return choices[random.nextInt(3)];
        }
        
        // Advanced AI strategy
        Map<String, Integer> frequency = new HashMap<>();
        frequency.put(ROCK, 0);
        frequency.put(PAPER, 0);
        frequency.put(SCISSORS, 0);
        
        int startIdx = Math.max(0, playerHistory.size() - 3);
        for (int i = startIdx; i < playerHistory.size(); i++) {
            String choice = playerHistory.get(i);
            frequency.put(choice, frequency.get(choice) + 1);
        }
        
        String mostFrequent = ROCK;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : frequency.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequent = entry.getKey();
            }
        }
        
        // Counter the most frequent choice
        switch (mostFrequent) {
            case ROCK: return PAPER;
            case PAPER: return SCISSORS;
            case SCISSORS: return ROCK;
            default: return ROCK;
        }
    }
    
    private String determineWinner() {
        // Handle cheating
        if (gameMode.equals("PVP")) {
            if (player1Cheated && !player2Cheated) {
                return "🚫 PLAYER 2 WINS! (Player 1 Cheated) 🚫";
            } else if (player2Cheated && !player1Cheated) {
                return "🚫 PLAYER 1 WINS! (Player 2 Cheated) 🚫";
            } else if (player1Cheated && player2Cheated) {
                return "🚫 BOTH PLAYERS CHEATED! ROUND VOID 🚫";
            }
        }
        
        // Handle no input
        if (player1Choice.isEmpty() && player2Choice.isEmpty()) {
            return "⏰ NO INPUT FROM BOTH PLAYERS! ⏰";
        } else if (player1Choice.isEmpty()) {
            return gameMode.equals("PVP") ? "⏰ PLAYER 2 WINS! (Player 1 No Input) ⏰" : "⏰ COMPUTER WINS! (No Input) ⏰";
        } else if (player2Choice.isEmpty()) {
            return gameMode.equals("PVP") ? "⏰ PLAYER 1 WINS! (Player 2 No Input) ⏰" : "⏰ YOU WIN! (Computer Error) ⏰";
        }
        
        // Normal game logic
        if (player1Choice.equals(player2Choice)) {
            return "🤝 DRAW! 🤝";
        }
        
        boolean player1Wins = (player1Choice.equals(ROCK) && player2Choice.equals(SCISSORS)) ||
                             (player1Choice.equals(PAPER) && player2Choice.equals(ROCK)) ||
                             (player1Choice.equals(SCISSORS) && player2Choice.equals(PAPER));
        
        if (gameMode.equals("PVP")) {
            return player1Wins ? "🎉 PLAYER 1 WINS! 🎉" : "🎉 PLAYER 2 WINS! 🎉";
        } else {
            return player1Wins ? "🎉 YOU WIN! 🎉" : "🤖 COMPUTER WINS! 🤖";
        }
    }
    
    private void updateScores(String result) {
        if (result.contains("DRAW") || result.contains("VOID") || result.contains("NO INPUT")) {
            draws++;
        } else if (result.contains("PLAYER 1") || result.contains("YOU WIN")) {
            player1Wins++;
        } else if (result.contains("PLAYER 2") || result.contains("COMPUTER WINS")) {
            player2Wins++;
        }
    }
    
    private String getScoreText() {
        if (gameMode.equals("PVP")) {
            return String.format("🏆 SCORE - Player 1: %d | Player 2: %d | Draws: %d 🏆", 
                               player1Wins, player2Wins, draws);
        } else {
            return String.format("🏆 SCORE - You: %d | Computer: %d | Draws: %d 🏆", 
                               player1Wins, player2Wins, draws);
        }
    }
    
    private boolean isGameComplete() {
        return player1Wins > totalRounds / 2 || player2Wins > totalRounds / 2 || currentRound >= totalRounds;
    }
    
    private void showFinalResult() {
        // Stop all timers to prevent conflicts
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        
        String finalResult;
        String winnerIcon;
        
        if (player1Wins > player2Wins) {
            finalResult = gameMode.equals("PVP") ? "🏆 PLAYER 1 WINS THE MATCH! 🏆" : "🎉 CONGRATULATIONS! YOU WIN! 🎉";
            winnerIcon = "🏆";
        } else if (player2Wins > player1Wins) {
            finalResult = gameMode.equals("PVP") ? "🏆 PLAYER 2 WINS THE MATCH! 🏆" : "🤖 COMPUTER WINS THE MATCH! 🤖";
            winnerIcon = gameMode.equals("PVP") ? "🏆" : "🤖";
        } else {
            finalResult = "🤝 MATCH ENDS IN A DRAW! 🤝";
            winnerIcon = "🤝";
        }
        
        // Create custom dialog
        JDialog resultDialog = new JDialog(this, "Match Complete", true);
        resultDialog.setSize(400, 250);
        resultDialog.setLocationRelativeTo(this);
        resultDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        JPanel dialogPanel = new JPanel();
        dialogPanel.setBackground(new Color(25, 25, 35));
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
        dialogPanel.setBorder(new EmptyBorder(30, 20, 20, 20));
        
        JLabel iconLabel = new JLabel(winnerIcon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel resultTextLabel = new JLabel(finalResult, SwingConstants.CENTER);
        resultTextLabel.setFont(new Font("Arial", Font.BOLD, 18));
        resultTextLabel.setForeground(new Color(255, 215, 0));
        resultTextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel scoreTextLabel = new JLabel(getScoreText(), SwingConstants.CENTER);
        scoreTextLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        scoreTextLabel.setForeground(new Color(200, 200, 200));
        scoreTextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        newGameButton = new AnimatedButton("NEW GAME", new Color(70, 130, 180), new Color(100, 149, 237));
        newGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGameButton.addActionListener(e -> {
            resultDialog.dispose(); // Properly dispose of the dialog
            resetGameState(); // Reset all game state
            showMainMenu(); // Return to main menu
            startAnimationTimer(); // Restart animation timer
        });
        
        dialogPanel.add(iconLabel);
        dialogPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        dialogPanel.add(resultTextLabel);
        dialogPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        dialogPanel.add(scoreTextLabel);
        dialogPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        dialogPanel.add(newGameButton);
        
        resultDialog.add(dialogPanel);
        resultDialog.setVisible(true);
    }
    
    private void resetGameState() {
        // Reset all game variables to initial state
        gameActive = false;
        gameMode = "";
        player1Choice = "";
        player2Choice = "";
        player1Pressed = false;
        player2Pressed = false;
        player1Cheated = false;
        player2Cheated = false;
        currentRound = 1;
        player1Wins = 0;
        player2Wins = 0;
        draws = 0;
        playerHistory.clear();
        
        // Stop any running timers
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
            countdownTimer = null;
        }
    }
    
    private String getGestureEmoji(String gesture) {
        switch (gesture) {
            case ROCK: return "✊";
            case PAPER: return "✋";
            case SCISSORS: return "✌️";
            default: return "❓";
        }
    }
    
    private void playSound() {
        Toolkit.getDefaultToolkit().beep();
    }
    
    // Custom animated button class
    private class AnimatedButton extends JButton {
        private Color baseColor, hoverColor;
        private boolean isHovered = false;
        private Timer hoverTimer;
        private float hoverProgress = 0f;
        
        public AnimatedButton(String text, Color baseColor, Color hoverColor) {
            super(text);
            this.baseColor = baseColor;
            this.hoverColor = hoverColor;
            
            setFont(new Font("Arial", Font.BOLD, 16));
            setForeground(Color.WHITE);
            setPreferredSize(new Dimension(350, 60));
            setMaximumSize(new Dimension(350, 60));
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    animateHover(true);
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    animateHover(false);
                }
            });
        }
        
        private void animateHover(boolean entering) {
            if (hoverTimer != null) hoverTimer.stop();
            
            hoverTimer = new Timer(20, e -> {
                if (entering) {
                    hoverProgress = Math.min(1f, hoverProgress + 0.1f);
                } else {
                    hoverProgress = Math.max(0f, hoverProgress - 0.1f);
                }
                
                repaint();
                
                if ((entering && hoverProgress >= 1f) || (!entering && hoverProgress <= 0f)) {
                    hoverTimer.stop();
                }
            });
            hoverTimer.start();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Interpolate colors
            int r = (int)(baseColor.getRed() + (hoverColor.getRed() - baseColor.getRed()) * hoverProgress);
            int gr = (int)(baseColor.getGreen() + (hoverColor.getGreen() - baseColor.getGreen()) * hoverProgress);
            int b = (int)(baseColor.getBlue() + (hoverColor.getBlue() - baseColor.getBlue()) * hoverProgress);
            
            Color currentColor = new Color(r, gr, b);
            
            // Draw button with gradient
            GradientPaint gradient = new GradientPaint(0, 0, currentColor, 0, getHeight(), 
                                                     currentColor.darker());
            g2d.setPaint(gradient);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            
            // Draw border
            g2d.setColor(currentColor.brighter());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 15, 15);
            
            // Draw text
            super.paintComponent(g);
        }
    }
    
    // Custom gesture panel class
    private class GameGesturePanel extends JPanel {
        private String playerName;
        private String gesture = "❓";
        private boolean isReady = false;
        private Timer glowTimer;
        private float glowIntensity = 0f;
        private boolean glowDirection = true;
        
        public GameGesturePanel(String playerName) {
            this.playerName = playerName;
            setOpaque(false);
            setPreferredSize(new Dimension(200, 300));
            
            startGlowAnimation();
        }
        
        private void startGlowAnimation() {
            glowTimer = new Timer(50, e -> {
                if (isReady) {
                    if (glowDirection) {
                        glowIntensity += 0.05f;
                        if (glowIntensity >= 1f) glowDirection = false;
                    } else {
                        glowIntensity -= 0.05f;
                        if (glowIntensity <= 0f) glowDirection = true;
                    }
                    repaint();
                }
            });
            glowTimer.start();
        }
        
        public void setGesture(String gesture) {
            this.gesture = gesture;
            repaint();
        }
        
        public void setReady(boolean ready) {
            this.isReady = ready;
            if (!ready) {
                glowIntensity = 0f;
                glowDirection = true;
            }
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            
            // Draw player name
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            FontMetrics fm = g2d.getFontMetrics();
            int nameWidth = fm.stringWidth(playerName);
            
            // Name shadow
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.drawString(playerName, centerX - nameWidth/2 + 2, 32);
            
            // Name text
            g2d.setColor(new Color(255, 215, 0));
            g2d.drawString(playerName, centerX - nameWidth/2, 30);
            
            // Draw gesture circle background
            int circleSize = 150;
            int circleX = centerX - circleSize/2;
            int circleY = centerY - circleSize/2 + 20;
            
            // Glow effect when ready
            if (isReady && glowIntensity > 0) {
                int glowSize = (int)(circleSize + 20 * glowIntensity);
                int glowX = centerX - glowSize/2;
                int glowY = centerY - glowSize/2 + 20;
                
                g2d.setColor(new Color(255, 215, 0, (int)(50 * glowIntensity)));
                g2d.fillOval(glowX, glowY, glowSize, glowSize);
            }
            
            // Circle gradient background
            GradientPaint circleGradient = new GradientPaint(
                circleX, circleY, new Color(60, 60, 80),
                circleX, circleY + circleSize, new Color(40, 40, 60)
            );
            g2d.setPaint(circleGradient);
            g2d.fillOval(circleX, circleY, circleSize, circleSize);
            
            // Circle border
            g2d.setColor(new Color(100, 100, 120));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawOval(circleX, circleY, circleSize, circleSize);
            
            // Draw gesture
            g2d.setFont(new Font("Arial", Font.PLAIN, 80));
            fm = g2d.getFontMetrics();
            int gestureWidth = fm.stringWidth(gesture);
            int gestureHeight = fm.getAscent();
            
            // Gesture shadow
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.drawString(gesture, centerX - gestureWidth/2 + 3, centerY + gestureHeight/2 - 10 + 3);
            
            // Gesture emoji
            g2d.setColor(Color.WHITE);
            g2d.drawString(gesture, centerX - gestureWidth/2, centerY + gestureHeight/2 - 10);
            
            // Draw input indicator
            if (gameActive) {
                String indicator = "";
                Color indicatorColor = new Color(150, 150, 150);
                
                if (gameMode.equals("PVP")) {
                    if (playerName.equals("PLAYER 1")) {
                        indicator = player1Pressed ? "✓ READY" : "A S D";
                        indicatorColor = player1Pressed ? new Color(0, 255, 0) : new Color(150, 150, 150);
                    } else {
                        indicator = player2Pressed ? "✓ READY" : "J K L";
                        indicatorColor = player2Pressed ? new Color(0, 255, 0) : new Color(150, 150, 150);
                    }
                } else {
                    if (playerName.equals("YOU")) {
                        indicator = player1Pressed ? "✓ READY" : "SPACE B N";
                        indicatorColor = player1Pressed ? new Color(0, 255, 0) : new Color(150, 150, 150);
                    } else {
                        indicator = "🤖 THINKING...";
                        indicatorColor = new Color(100, 149, 237);
                    }
                }
                
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                fm = g2d.getFontMetrics();
                int indicatorWidth = fm.stringWidth(indicator);
                
                // Indicator background
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(centerX - indicatorWidth/2 - 10, getHeight() - 40, 
                                indicatorWidth + 20, 25, 10, 10);
                
                // Indicator text
                g2d.setColor(indicatorColor);
                g2d.drawString(indicator, centerX - indicatorWidth/2, getHeight() - 22);
            }
        }
    }
    
    private class GameKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!gameActive) return;
            
            int keyCode = e.getKeyCode();
            
            if (gameMode.equals("PVP")) {
                handlePvPInput(keyCode);
            } else {
                handlePvCInput(keyCode);
            }
        }
    }
    
    private void handlePvPInput(int keyCode) {
        // Player 1 controls: A, S, D
        if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_D) {
            if (player1Pressed) {
                player1Cheated = true;
                return;
            }
            
            player1Pressed = true;
            switch (keyCode) {
                case KeyEvent.VK_A: player1Choice = ROCK; break;
                case KeyEvent.VK_S: player1Choice = PAPER; break;
                case KeyEvent.VK_D: player1Choice = SCISSORS; break;
            }
            player1Panel.setReady(true);
            repaint();
        }
        
        // Player 2 controls: J, K, L
        if (keyCode == KeyEvent.VK_J || keyCode == KeyEvent.VK_K || keyCode == KeyEvent.VK_L) {
            if (player2Pressed) {
                player2Cheated = true;
                return;
            }
            
            player2Pressed = true;
            switch (keyCode) {
                case KeyEvent.VK_J: player2Choice = ROCK; break;
                case KeyEvent.VK_K: player2Choice = PAPER; break;
                case KeyEvent.VK_L: player2Choice = SCISSORS; break;
            }
            player2Panel.setReady(true);
            repaint();
        }
    }
    
    private void handlePvCInput(int keyCode) {
        if (player1Pressed) return; // Only allow one input
        
        // Player controls: Space, B, N
        switch (keyCode) {
            case KeyEvent.VK_SPACE:
                player1Choice = ROCK;
                player1Pressed = true;
                player1Panel.setReady(true);
                break;
            case KeyEvent.VK_B:
                player1Choice = PAPER;
                player1Pressed = true;
                player1Panel.setReady(true);
                break;
            case KeyEvent.VK_N:
                player1Choice = SCISSORS;
                player1Pressed = true;
                player1Panel.setReady(true);
                break;
        }
        repaint();
    }
    
    public static void main(String[] args) {
        // Enable hardware acceleration
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.java2d.d3d", "true");
        
        SwingUtilities.invokeLater(() -> {
            new RockPaperScissorsGame().setVisible(true);
        });
    }
}