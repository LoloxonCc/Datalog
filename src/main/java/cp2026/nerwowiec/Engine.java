package cp2026.nerwowiec;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;

public interface Engine {
    List<RecordedMove> playGame(List<Player> players,
                                List<Iterator<Integer>> stacks,
                                Duration timeout) throws InterruptedException;
}
