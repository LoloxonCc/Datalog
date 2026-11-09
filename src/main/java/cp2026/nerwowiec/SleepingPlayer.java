package cp2026.nerwowiec;

public final class SleepingPlayer implements Player {
    @Override
    public Move chooseMove(int[] hand, int[] topCards) throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
        return Move.WAIT;
    }
}
