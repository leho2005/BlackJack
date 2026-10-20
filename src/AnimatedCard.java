import java.awt.Image;
import java.awt.Point;

// A card that is flying from the deck to its place on the table.
public class AnimatedCard {
    final Card card;
    final Hand hand;      // the hand the card belongs to once it lands
    final boolean hidden; // the dealer's face-down card
    final Image image;
    final int targetX, targetY;
    int x, y;

    public AnimatedCard(Card card, Hand hand, Point start, Point target, boolean hidden) {
        this.card = card;
        this.hand = hand;
        this.hidden = hidden;
        this.image = Card.loadImage(hidden ? "./cards/BACK.png" : card.getImagePath());
        this.x = start.x;
        this.y = start.y;
        this.targetX = target.x;
        this.targetY = target.y;
    }

    // moves the card one step, returns true when it has arrived
    public boolean update() {
        int speed = 20;
        if (x < targetX) x = Math.min(x + speed, targetX);
        if (x > targetX) x = Math.max(x - speed, targetX);
        if (y < targetY) y = Math.min(y + speed, targetY);
        if (y > targetY) y = Math.max(y - speed, targetY);

        return x == targetX && y == targetY;
    }
}