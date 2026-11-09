package cp2026.nerwowiec;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ParallelEngine implements Engine {
    private int numberOfGames = 0;
    @Override
    public List<RecordedMove> playGame(List<Player> players, List<Iterator<Integer>> stacks, Duration timeout) throws InterruptedException {
        assert (numberOfGames < 100);
        numberOfGames++;

        Game game = new Game(players, stacks);
        List<RecordedMove> transcript = game.play(timeout);

        numberOfGames--;

        return transcript;
    }

    private class Game {
        private List<Iterator<Integer>> reserveStacks;
        private List<List<Integer>> hands;
        private List<Integer> topCards;
        private List<RecordedMove> transcript;
        private long startNanos;

        private List<Player> players;

        public Game (List<Player> players, List<Iterator<Integer>> stacks) {
            assert (players.size() == stacks.size());
            assert (players.size() >= 2 && players.size() <= 1000);
            int numPlayers = players.size();

            this.reserveStacks = Collections.synchronizedList(stacks);
            this.hands = Collections.synchronizedList(new ArrayList<>());
            this.topCards = Collections.synchronizedList(new ArrayList<>());
            this.transcript = new ArrayList<>();
            this.players = players;
        }

        public List<RecordedMove> play(Duration timeout) {
            int numPlayers = players.size();

            // Firstly we need to initialize hands.
            for (int i = 0; i < numPlayers; i++) {
                Iterator<Integer> stack = reserveStacks.get(i);
                setupHand(stack);
                topCards.add(stack.next());
            }

            return transcript;
        }

        private void setupHand(Iterator<Integer> stack) {
            List<Integer> hand = new ArrayList<>();
            for (int i = 0; i < GameRules.HAND_SIZE; i++) {
                hand.add(stack.next());
            }
            hands.add(hand);
        }
    }
}
