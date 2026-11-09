package cp2026.nerwowiec;


public final class SimplePlayer implements Player {
    @Override
    public Move chooseMove(int[] hand, int[] topCards) {
        for (int s = 0; s < topCards.length; s++) {
            for (int h = 0; h < hand.length; h++) {
                if (GameRules.isInSequence(hand[h], topCards[s])) {
                    return new Move.PlayCard(h, s);
                }
            }
        }
        return Move.WAIT;
    }
}
