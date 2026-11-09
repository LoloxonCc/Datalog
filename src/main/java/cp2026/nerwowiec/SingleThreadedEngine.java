package cp2026.nerwowiec;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SingleThreadedEngine implements Engine {
    private List<Iterator<Integer>> reserveStacks;
    private List<List<Integer>> hands;
    private int[] topCards;
    private List<RecordedMove> transcript;
    private long startNanos;

    @Override
    public List<RecordedMove> playGame(List<Player> players, List<Iterator<Integer>> stacks, Duration timeout) throws InterruptedException {
        assert (players.size() == stacks.size());
        int numPlayers = players.size();
        transcript = new ArrayList<>();
        reserveStacks = stacks;
        hands = new ArrayList<>();
        topCards = new int[numPlayers];

        for (int i = 0; i < numPlayers; i++) {
            Iterator<Integer> stack = stacks.get(i);
            setupHand(stack);
            topCards[i] = stack.next();
        }

        boolean allWaited = false;
        Instant startTime = Instant.now();
        startNanos = Clock.nanoTime();
        while (!allWaited && Instant.now().isBefore(startTime.plus(timeout))) {
            allWaited = true;
            for (int i = 0; i < numPlayers; i++) {
                if (hands.get(i).isEmpty()) continue;
                Player.Move move = players.get(i).chooseMove(asArray(hands.get(i)), topCards);
                switch (move) {
                    case Player.Move.PlayCard playCard -> {
                        allWaited = false;
                        playIfLegal(i, playCard);
                    }
                    case Player.Move.Wait ignored -> {
                    }
                }
            }
        }
        return transcript;
    }

    private void playIfLegal(int playerIdx, Player.Move.PlayCard move) {
        List<Integer> hand = hands.get(playerIdx);
        if (move.cardIndex() < 0 || move.cardIndex() >= hand.size()) {
            System.err.printf("[%d] illegal move: card index out of range%n", playerIdx);
            return;
        }
        if (move.stackIndex() < 0 || move.stackIndex() >= topCards.length) {
            System.err.printf("[%d] illegal move: stack index out of range%n", playerIdx);
            return;
        }
        int card = hand.get(move.cardIndex());
        if (!GameRules.isInSequence(card, topCards[move.stackIndex()])) {
            System.err.printf("[%d] illegal move: playing card out of sequence%n", playerIdx);
            return;
        }

        hand.remove(move.cardIndex());
        topCards[move.stackIndex()] = card;
        transcript.add(new RecordedMove(Clock.nanoTime() - startNanos, playerIdx, move.stackIndex(), card));

        Iterator<Integer> reserveStack = reserveStacks.get(playerIdx);
        if (reserveStack.hasNext()) {
            hand.add(reserveStack.next());
        }
        if (hand.isEmpty()) {
            System.err.printf("[%d] finished!%n", playerIdx);
        }
    }

    private static int[] asArray(List<Integer> integers) {
        return integers.stream().mapToInt(i -> i).toArray();
    }

    private void setupHand(Iterator<Integer> stack) {
        List<Integer> hand = new ArrayList<>();
        for (int i = 0; i < GameRules.HAND_SIZE; i++) {
            hand.add(stack.next());
        }
        hands.add(hand);
    }
}
