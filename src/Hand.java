import java.util.ArrayList;

// The cards of one player (or the dealer).
// "cards" holds every card that has been drawn, "landed" only the ones that
// already arrived on the table. The game logic uses the first, the screen the second.
public class Hand {
    private final ArrayList<Card> cards = new ArrayList<>();
    private final ArrayList<Card> landed = new ArrayList<>();

    public void add(Card card) {
        cards.add(card);
    }

    public void land(Card card) {
        landed.add(card);
    }

    public void clear() {
        cards.clear();
        landed.clear();
    }

    // number of drawn cards (including the ones still flying)
    public int size() {
        return cards.size();
    }

    public int landedSize() {
        return landed.size();
    }

    public Card getLanded(int index) {
        return landed.get(index);
    }

    // value of all drawn cards
    public int value() {
        return valueOf(cards);
    }

    public boolean isBust() {
        return value() > 21;
    }

    // value of the cards on the table
    public int landedValue() {
        return valueOf(landed);
    }

    // value of the cards on the table plus one extra card (the dealer's hidden one)
    public int landedValueWith(Card extra) {
        ArrayList<Card> all = new ArrayList<>(landed);
        if (extra != null) {
            all.add(extra);
        }
        return valueOf(all);
    }

    // aces count as 11, and as 1 when the hand would go over 21
    private static int valueOf(ArrayList<Card> list) {
        int sum = 0;
        int aces = 0;
        for (Card c : list) {
            sum += c.getValue();
            if (c.isAce()) {
                aces++;
            }
        }
        while (sum > 21 && aces > 0) {
            sum -= 10;
            aces--;
        }
        return sum;
    }
}