import java.awt.*;
import javax.swing.JPanel;

// Only draws the table, the game logic is in BlackJack.
public class GamePanel extends JPanel {
    public static final int CARD_WIDTH = 110;
    public static final int CARD_HEIGHT = 154;
    public static final int CARD_GAP = 5;

    private final BlackJack game;

    public GamePanel(BlackJack game) {
        this.game = game;
        setLayout(new BorderLayout());
        setBackground(new Color(0, 86, 39));
    }

    // ---- positions on the table (shared with the game logic) ----

    public static Point deckPosition(int w, int h) {
        return new Point((w - CARD_WIDTH) / 2 + 650, (h - CARD_HEIGHT) / 2 - 350);
    }

    public static Point hiddenCardPosition(int w, int h) {
        return new Point((w - CARD_WIDTH) / 2 - 60, (h - CARD_HEIGHT) / 2 - 300);
    }

    public static Point dealerSlot(int w, int h, int index) {
        return new Point((w - CARD_WIDTH) / 2 + 60 + (CARD_WIDTH + CARD_GAP) * index, (h - CARD_HEIGHT) / 2 - 300);
    }

    public static Point playerSlot(int w, int h, int index) {
        return new Point((w - CARD_WIDTH) / 2 - 60 + (CARD_WIDTH + CARD_GAP) * index, (h - CARD_HEIGHT) / 2 + 300);
    }

    // ---- drawing ----

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();

        try {
            // background and deck
            Image background = Card.loadImage("./background/background.png");
            g.drawImage(background, (w - background.getWidth(this)) / 2, (h - background.getHeight(this)) / 2, this);
            Point deckPos = deckPosition(w, h);
            g.drawImage(Card.loadImage("./background/DECK.png"), deckPos.x, deckPos.y, 130, 205, this);

            Hand dealer = game.getDealerHand();
            Hand player = game.getPlayerHand();
            Card hiddenCard = game.getHiddenCard();

            // dealer's visible cards
            for (int i = 0; i < dealer.landedSize(); i++) {
                drawCard(g, Card.loadImage(dealer.getLanded(i).getImagePath()), dealerSlot(w, h, i));
            }

            // dealer's hidden card (face down until the round is over)
            if (hiddenCard != null) {
                String path = game.isRoundOver() ? hiddenCard.getImagePath() : "./cards/BACK.png";
                drawCard(g, Card.loadImage(path), hiddenCardPosition(w, h));
            }

            // player's cards
            for (int i = 0; i < player.landedSize(); i++) {
                drawCard(g, Card.loadImage(player.getLanded(i).getImagePath()), playerSlot(w, h, i));
            }

            // cards in transit
            for (AnimatedCard ac : game.getAnimatedCards()) {
                g.drawImage(ac.image, ac.x, ac.y, CARD_WIDTH, CARD_HEIGHT, null);
            }

            if (game.isGameStarted()) {
                // live score counters, to the left of each hand
                g.setFont(new Font("Arial", Font.BOLD, 26));
                g.setColor(Color.white);
                int labelX = (w - CARD_WIDTH) / 2 - 300;
                int dealerLabelY = (h - CARD_HEIGHT) / 2 - 300 + CARD_HEIGHT / 2;
                int playerLabelY = (h - CARD_HEIGHT) / 2 + 300 + CARD_HEIGHT / 2;

                String dealerLabel;
                if (game.isRoundOver()) {
                    dealerLabel = "Dealer: " + dealer.landedValueWith(hiddenCard);
                } else if (dealer.landedSize() == 0) {
                    dealerLabel = "Dealer: ?";
                } else {
                    // the hidden card is not counted until it is revealed
                    dealerLabel = "Dealer: " + dealer.landedValue() + " + ?";
                }
                g.drawString(dealerLabel, labelX, dealerLabelY);
                g.drawString("You: " + player.landedValue(), labelX, playerLabelY);
            }

            // result message, shown once all cards have landed
            if (game.isRoundOver() && game.getAnimatedCards().isEmpty()) {
                int playerTotal = player.landedValue();
                int dealerTotal = dealer.landedValueWith(hiddenCard);
                String message;
                if (playerTotal > 21) {
                    message = "You Lose!";
                } else if (dealerTotal > 21) {
                    message = "You Win!";
                } else if (playerTotal == dealerTotal) {
                    message = "Tie!";
                } else if (playerTotal > dealerTotal) {
                    message = "You Win!";
                } else {
                    message = "You Lose!";
                }

                g.setFont(new Font("Arial", Font.BOLD, 40));
                g.setColor(Color.white);
                FontMetrics fm = g.getFontMetrics();
                g.drawString(message, (w - fm.stringWidth(message)) / 2, h / 2);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawCard(Graphics g, Image image, Point p) {
        g.drawImage(image, p.x, p.y, CARD_WIDTH, CARD_HEIGHT, null);
    }
}