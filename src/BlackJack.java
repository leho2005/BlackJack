import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

// The game itself: buttons, rounds and the dealer's rules.
public class BlackJack {
    private Deck deck;
    private final Hand dealerHand = new Hand(); // the hidden card is the first card of this hand
    private final Hand playerHand = new Hand();
    private Card hiddenCard; // set when the face-down card has landed

    private boolean gameStarted = false; // true after the first Deal
    private boolean roundOver = false;   // true once the player stayed or busted

    private final ArrayList<AnimatedCard> animatedCards = new ArrayList<>();
    private Timer animationTimer;

    private final JFrame frame = new JFrame("Blackjack");
    private final GamePanel gamePanel = new GamePanel(this);
    private final JPanel buttonPanel = new JPanel();
    private final JButton hitButton = new JButton("Hit");
    private final JButton stayButton = new JButton("Stay");
    private final JButton dealButton = new JButton("Deal");
    private final JButton exitButton = new JButton("Exit");

    public BlackJack() {
        frame.setContentPane(gamePanel);

        for (JButton button : new JButton[]{hitButton, stayButton, dealButton, exitButton}) {
            button.setFocusable(false);
            buttonPanel.add(button);
        }
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        hitButton.setEnabled(false);
        stayButton.setEnabled(false);

        // cards are added to the table only after their animation has finished
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
                    ac.hand.land(ac.card);
                }
            }

            gamePanel.repaint();

            // nothing is moving anymore
            if (animatedCards.isEmpty()) {
                animationTimer.stop();
                if (roundOver) {
                    dealButton.setText("Play Again");
                    dealButton.setEnabled(true);
                }
            }
        });

        dealButton.addActionListener(e -> startRound());
        hitButton.addActionListener(e -> hit());
        stayButton.addActionListener(e -> stay());
        exitButton.addActionListener(e -> System.exit(0));
    }

    // ---- game flow ----

    private void startRound() {
        // reset everything
        animatedCards.clear();
        dealerHand.clear();
        playerHand.clear();
        hiddenCard = null;
        roundOver = false;
        gameStarted = true;
        deck = new Deck();

        dealCard(dealerHand, GamePanel.hiddenCardPosition(gamePanel.getWidth(), gamePanel.getHeight()), true);
        dealCard(dealerHand, nextDealerSlot(), false);
        dealCard(playerHand, nextPlayerSlot(), false);
        dealCard(playerHand, nextPlayerSlot(), false);

        hitButton.setEnabled(true);
        stayButton.setEnabled(true);
        dealButton.setEnabled(false);
        dealButton.setText("Deal");
    }

    private void hit() {
        dealCard(playerHand, nextPlayerSlot(), false);
        if (playerHand.isBust()) {
            endRound();
        }
    }

    private void stay() {
        hitButton.setEnabled(false);
        stayButton.setEnabled(false);

        // the dealer draws until 17
        while (dealerHand.value() < 17) {
            dealCard(dealerHand, nextDealerSlot(), false);
        }
        endRound();
    }

    // ends the round; the Play Again button appears once the animations are done
    private void endRound() {
        roundOver = true;
        hitButton.setEnabled(false);
        stayButton.setEnabled(false);
        animationTimer.start();
    }

    // draws a card from the deck and sends it flying to the target position
    private void dealCard(Hand hand, Point target, boolean hidden) {
        Card card = deck.draw();
        hand.add(card);
        Point start = GamePanel.deckPosition(gamePanel.getWidth(), gamePanel.getHeight());
        animatedCards.add(new AnimatedCard(card, hand, start, target, hidden));
        animationTimer.start();
    }

    // the slot is based on the number of drawn cards, so cards still flying are counted too
    private Point nextPlayerSlot() {
        return GamePanel.playerSlot(gamePanel.getWidth(), gamePanel.getHeight(), playerHand.size());
    }

    // the dealer's first card is the hidden one, which has its own position
    private Point nextDealerSlot() {
        return GamePanel.dealerSlot(gamePanel.getWidth(), gamePanel.getHeight(), dealerHand.size() - 1);
    }

    // ---- used by GamePanel ----

    public Hand getDealerHand() {
        return dealerHand;
    }

    public Hand getPlayerHand() {
        return playerHand;
    }

    public Card getHiddenCard() {
        return hiddenCard;
    }

    public ArrayList<AnimatedCard> getAnimatedCards() {
        return animatedCards;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public boolean isRoundOver() {
        return roundOver;
    }

    public static void main(String[] args) {
        new BlackJack();
    }
}