package cp2026.nerwowiec;

import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class Main {
    public static void main(String[] args) throws InterruptedException {
        Engine engine = new SingleThreadedEngine();
        List<Player> players = Arrays.asList(new SimplePlayer(), new SimplePlayer());
        Iterator<Integer> stack1 = Arrays.asList(3, 5, 7, 9, 1, 2).iterator();
        Iterator<Integer> stack2 = Arrays.asList(2, 4, 6, 8, 13, 10).iterator();
        List<Iterator<Integer>> stacks = Arrays.asList(stack1, stack2);
        // this gets stuck:
        // List<Player> players = Arrays.asList(new SimplePlayer(), new SimplePlayer(), new SleepingPlayer());
        // Iterator<Integer> stack3 = Arrays.asList(1, 2, 3, 4, 13, 5).iterator();
        // List<Iterator<Integer>> stacks = Arrays.asList(stack1, stack2, stack3);
        List<RecordedMove> transcript = engine.playGame(players, stacks, Duration.ofSeconds(10));
        for (RecordedMove recordedMove : transcript) {
            System.err.println(recordedMove);
        }
    }
}
