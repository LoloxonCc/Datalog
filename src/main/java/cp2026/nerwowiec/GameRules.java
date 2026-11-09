package cp2026.nerwowiec;

public final class GameRules {
    public static final int HAND_SIZE = 4;
    public static final int MIN_CARD = 1;   // ace
    public static final int MAX_CARD = 13;  // king

    public static int nextCard(int card) {
        return card == MAX_CARD ? MIN_CARD : card + 1;
    }

    public static int prevCard(int card) {
        return card == MIN_CARD ? MAX_CARD : card - 1;
    }

    public static boolean isInSequence(int card1, int card2) {
        return nextCard(card1) == card2 || nextCard(card2) == card1;
    }

    private GameRules() {}
}
