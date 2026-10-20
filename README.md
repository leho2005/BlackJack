# Blackjack

A Blackjack game in Java Swing, made as a personal project. It's playable, but not finished yet.

You play against the dealer. Hit to draw a card, Stay to end your turn. The dealer draws until 17, aces count as 11 or 1, and going over 21 ends the round right away. Both scores are shown on screen while you play (the dealer's face-down card is only counted once it's revealed), and you can start a new round with the Play Again button.

## Running it

You need a JDK (Java 8 or newer). Put the `cards/` and `background/` image folders next to the `.java` files, then:

```
javac *.java
java App
```

The card images are named like `cards/ace_of_spades.png` and `cards/10_of_hearts.png`, and there is also `cards/BACK.png`, `background/background.png` and `background/DECK.png`.

## Code

I split it up so the game logic doesn't depend on the drawing code. `Card`, `Deck` and `Hand` are the model (`Hand` also handles the ace value), `GamePanel` only draws the table, and `BlackJack` runs the game and the buttons. `AnimatedCard` is a card on its way from the deck to the table.

## Not done yet

- Betting
- Double down and split
- The layout is made for a maximized window and doesn't adapt to other sizes