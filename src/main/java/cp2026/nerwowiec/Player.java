package cp2026.nerwowiec;

public interface Player {
    sealed interface Move {
        /** Put card with index {@code cardIndex} in {@code hand} on stack with index {@code stackIndex} */
        record PlayCard(int cardIndex, int stackIndex) implements Move {}

        record Wait() implements Move {}
        /** Just wait */
        Move WAIT = new Wait();
    }

    /**
     * Computes the player's next attempted move. The move might or might not succeed,
     * depending on whether the state of the discard stacks changes between the time when player
     * chooses the move and when the engine attempts to make it.
     *
     * @param hand     the cards in player's hand
     * @param topCards the top cards on each of the discard stacks
     * @return the {@link Move} to make
     */
    Move chooseMove(int[] hand, int[] topCards) throws InterruptedException;
}
