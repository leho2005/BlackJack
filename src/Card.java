import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

public class Card {
    // images are loaded once and reused, not on every repaint
    private static final Map<String, Image> imageCache = new HashMap<>();

    private final String value;
    private final String type;

    public Card(String value, String type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public String toString() {
        return value + "_of_" + type;
    }

    public int getValue() {
        if (value.equals("ace")) {
            return 11;
        }
        if (value.equals("jack") || value.equals("queen") || value.equals("king")) {
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

    public static Image loadImage(String path) {
        return imageCache.computeIfAbsent(path, p -> new ImageIcon(Card.class.getResource(p)).getImage());
    }
}