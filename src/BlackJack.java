import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class BlackJack {
    private class Card {
        String value;
        String type;

        Card(String value, String type) {
            this.value = value;
            this.type = type;
        }

        public String toString() {
            return value + "_of_" + type;
        }

        public int getValue() {
            if ("acejackkingqueen".contains(value)) {
                if (value.equals("ace")) {
                    return 11;
                }
                return 10;
            }
            return Integer.parseInt(value);
        }

        public boolean isAce() {
            return value.equals("ace");
        }

        public String getImagePath() {
            return "./cards/" + toString() + ".png";
        }
    }

    private class AnimatedCard {
        Card card;
        Image image;
        int x, y;
        int targetX, targetY;
        boolean moving = true;
        boolean hidden;

        public AnimatedCard(Card card, int startX, int startY, int targetX, int targetY, boolean hidden) {
            this.card = card;
            this.hidden = hidden;
            String path = hidden ? "./cards/BACK.png" : card.getImagePath();
            this.image = new ImageIcon(getClass().getResource(path)).getImage();
            this.x = startX;
            this.y = startY;
            this.targetX = targetX;
            this.targetY = targetY;
        }

        public boolean update() {
            int speed = 20;
            if (x < targetX) x = Math.min(x + speed, targetX);
            if (x > targetX) x = Math.max(x - speed, targetX);
            if (y < targetY) y = Math.min(y + speed, targetY);
            if (y > targetY) y = Math.max(y - speed, targetY);

            moving = (x != targetX || y != targetY);
            return !moving; // true when finished
        }

        public void flip() {
            if (hidden) {
                image = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                hidden = false;
            }
        }
    }

    ArrayList<Card> deck;
    Random random = new Random();

    // dealer
    Card hiddenCard;
    ArrayList<Card> dealerHand;
    int dealerSum;
    int dealerAceCount;

    // player
    ArrayList<Card> playerHand;
    int playerSum;
    int playerAceCount;

    ArrayList<AnimatedCard> animatedCards = new ArrayList<>();
    Timer animationTimer;

    int cardWidth = 110;
    int cardHeight = 154;

    JFrame frame = new JFrame("Blackjack");
    JPanel gamePanel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            try {
                // draw background
                Image background = new ImageIcon(getClass().getResource("./background/background.png")).getImage();
                int x = (getWidth() - background.getWidth(this)) / 2;
                int y = (getHeight() - background.getHeight(this)) / 2;
                g.drawImage(background, x, y, this);

                // draw DECK
                Image deck = new ImageIcon(getClass().getResource("./background/DECK.png")).getImage();
                x = (getWidth() - cardWidth) / 2 + 650;
                y = (getHeight() - cardHeight) / 2 - 350;
                g.drawImage(deck, x, y, 130, 205, this);

                // draw dealer’s committed hand
                for (int i = 0; i < dealerHand.size(); i++) {
                    Card card = dealerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    x = (getWidth() - cardWidth) / 2 + 60;
                    y = (getHeight() - cardHeight) / 2 - 300;
                    g.drawImage(cardImg, x + (cardWidth + 5) * i, y, cardWidth, cardHeight, null);
                }

                // draw hidden card (if not flipped yet)
                if (hiddenCard != null) {
                    x = (getWidth() - cardWidth) / 2 - 60;
                    y = (getHeight() - cardHeight) / 2 - 300;
                    Image hiddenCardImg;
                    if (!stayButton.isEnabled()) {
                        hiddenCardImg = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
                    } else {
                        hiddenCardImg = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
                    }
                    g.drawImage(hiddenCardImg, x, y, cardWidth, cardHeight, null);
                }

                // draw player’s committed hand
                for (int i = 0; i < playerHand.size(); i++) {
                    Card card = playerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    x = (getWidth() - cardWidth) / 2 - 60;
                    y = (getHeight() - cardHeight) / 2 + 300;
                    g.drawImage(cardImg, x + (cardWidth + 5) * i, y, cardWidth, cardHeight, null);
                } 

                // draw animated cards (in transit)
                for (AnimatedCard ac : animatedCards) {
                    g.drawImage(ac.image, ac.x, ac.y, cardWidth, cardHeight, null);
                }

                if (!stayButton.isEnabled()) {
                    dealerSum = reduceDealerAce();
                    playerSum = reducePlayerAce();
                    String message = "";
                    if (playerSum > 21) {
                        message = "You Lose!";
                    } else if (dealerSum > 21) {
                        message = "You Win!";
                    } else if (playerSum == dealerSum) {
                        message = "Tie!";
                    } else if (playerSum > dealerSum) {
                        message = "You Win!";
                    } else if (playerSum < dealerSum) {
                        message = "You Lose!";
                    }

                    g.setFont(new Font("Arial", Font.PLAIN, 30));
                    g.setColor(Color.white);
                    g.drawString(message, 220, 250);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    JPanel buttonPanel = new JPanel();
    JButton hitButton = new JButton("Hit");
    JButton stayButton = new JButton("Stay");
    JButton dealButton = new JButton("Deal");

    BlackJack() {
        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(0, 86, 39));
        frame.setContentPane(gamePanel);

        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        hitButton.setFocusable(false);
        buttonPanel.add(hitButton);
        stayButton.setFocusable(false);
        buttonPanel.add(stayButton);
        dealButton.setFocusable(false);
        buttonPanel.add(dealButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // ✅ FIX 2: commit cards only after animation finishes
        animationTimer = new Timer(16, e -> {
            ArrayList<AnimatedCard> finished = new ArrayList<>();
            for (AnimatedCard ac : animatedCards) {
                if (ac.update()) {
                    finished.add(ac);
                }
            }
            animatedCards.removeAll(finished);

            for (AnimatedCard ac : finished) {
                if (ac.hidden) {
                    hiddenCard = ac.card;
                } else {
                    // commit based on Y position
                    if (ac.targetY > gamePanel.getHeight() / 2) {
                        playerHand.add(ac.card);
                    } else {
                        dealerHand.add(ac.card);
                    }
                }
            }

            gamePanel.repaint();
        });

        hitButton.setEnabled(false);
        stayButton.setEnabled(false);

        dealButton.addActionListener(e -> {
            // reset everything
            animatedCards.clear();
            dealerHand = new ArrayList<>();
            playerHand = new ArrayList<>();
            dealerSum = 0;
            dealerAceCount = 0;
            playerSum = 0;
            playerAceCount = 0;
            hiddenCard = null;

            startGame();

            hitButton.setEnabled(true);
            stayButton.setEnabled(true);
            dealButton.setEnabled(false);
        });

        hitButton.addActionListener(e -> {
            Card card = deck.remove(deck.size() - 1);
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;

            int startX = (gamePanel.getWidth() - cardWidth) / 2 + 650, startY = (gamePanel.getHeight()- cardWidth) / 2 - 350;
            int targetX = (gamePanel.getWidth() - cardWidth) / 2 + (cardWidth + 5) * playerHand.size();
            int targetY = (gamePanel.getHeight() - cardHeight) / 2 + 300;
            animatedCards.add(new AnimatedCard(card, startX, startY, targetX, targetY, false));
            animationTimer.start();

            if (reducePlayerAce() > 21) {
                hitButton.setEnabled(false);
            }
        });

        stayButton.addActionListener(e -> {
            hitButton.setEnabled(false);
            stayButton.setEnabled(false);

            while (dealerSum < 17) {
                Card card = deck.remove(deck.size() - 1);
                dealerSum += card.getValue();
                dealerAceCount += card.isAce() ? 1 : 0;

                int startX = (gamePanel.getWidth() - cardWidth) / 2 + 650, startY = (gamePanel.getHeight()- cardWidth) / 2 - 350;
                int targetX = (gamePanel.getWidth() - cardWidth) / 2 + 60 + (cardWidth + 5) * dealerHand.size();
                int targetY = (gamePanel.getHeight() - cardHeight) / 2 - 300;
                animatedCards.add(new AnimatedCard(card, startX, startY, targetX, targetY, false));
                animationTimer.start();
            }
        });

        gamePanel.repaint();
    }

    public void startGame() {
        buildDeck();
        shuffleDeck();

        dealerHand = new ArrayList<>();
        dealerSum = 0;
        dealerAceCount = 0;
        int startX = (gamePanel.getWidth() - cardWidth) / 2 + 650, startY = (gamePanel.getHeight()- cardWidth) / 2 - 350;

        // hidden card
        hiddenCard = deck.remove(deck.size() - 1);
        dealerSum += hiddenCard.getValue();
        dealerAceCount += hiddenCard.isAce() ? 1 : 0;
        int targetXHidden = (gamePanel.getWidth() - cardWidth) / 2 - 60;
        int targetYHidden = (gamePanel.getHeight() - cardHeight) / 2 - 300;
        animatedCards.add(new AnimatedCard(hiddenCard, startX, startY, targetXHidden, targetYHidden, true));

        // dealer visible card
        Card card = deck.remove(deck.size() - 1);
        dealerSum += card.getValue();
        dealerAceCount += card.isAce() ? 1 : 0;
        int targetXDealer = (gamePanel.getWidth() - cardWidth) / 2 + 60;
        int targetYDealer = (gamePanel.getHeight() - cardHeight) / 2 - 300;
        animatedCards.add(new AnimatedCard(card, startX, startY, targetXDealer, targetYDealer, false));

        // player cards
        playerHand = new ArrayList<>();
        playerSum = 0;
        playerAceCount = 0;

        for (int i = 0; i < 2; i++) {
            card = deck.remove(deck.size() - 1);
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;
            int targetXPlayer = (gamePanel.getWidth() - cardWidth) / 2 - 60 + (cardWidth + 5) * i;
            int targetYPlayer = (gamePanel.getHeight() - cardHeight) / 2 + 300;
            animatedCards.add(new AnimatedCard(card, startX, startY, targetXPlayer, targetYPlayer, false));
        }
        animationTimer.start();
    }

    public void buildDeck() {
        deck = new ArrayList<>();
        String[] values = {"ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "jack", "queen", "king"};
        String[] types = {"clubs", "diamonds", "hearts", "spades"};

        for (String type : types) {
            for (String value : values) {
                deck.add(new Card(value, type));
            }
        }
    }

    public void shuffleDeck() {
        for (int i = 0; i < deck.size(); i++) {
            int j = random.nextInt(deck.size());
            Card currCard = deck.get(i);
            deck.set(i, deck.get(j));
            deck.set(j, currCard);
        }
    }

    public int reducePlayerAce() {
        while (playerSum > 21 && playerAceCount > 0) {
            playerSum -= 10;
            playerAceCount -= 1;
        }
        return playerSum;
    }

    public int reduceDealerAce() {
        while (dealerSum > 21 && dealerAceCount > 0) {
            dealerSum -= 10;
            dealerAceCount -= 1;
        }
        return dealerSum;
    }

    public static void main(String[] args) {
        new BlackJack();
    }
}
