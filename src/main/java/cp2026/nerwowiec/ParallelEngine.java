package cp2026.nerwowiec;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;

public class ParallelEngine implements Engine {
    @Override
    public List<RecordedMove> playGame(List<Player> players, List<Iterator<Integer>> stacks, Duration timeout) throws InterruptedException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
