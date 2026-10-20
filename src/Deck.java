import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    private final ArrayList<Card> cards = new ArrayList<>();

    // a new deck is always built and shuffled
    public Deck() {
        String[] values = {"ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "jack", "queen", "king"};
        String[] types = {"clubs", "diamonds", "hearts", "spades"};

        for (String type : types) {
            for (String value : values) {
                cards.add(new Card(value, type));
            }
        }
        Collections.shuffle(cards);
    }

    public Card draw() {
        return cards.remove(cards.size() - 1);
    }
}