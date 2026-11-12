package cp2026.nerwowiec;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ParallelEngine implements Engine {
    private AtomicInteger numberOfGames = new AtomicInteger(0);
    @Override
    public List<RecordedMove> playGame(List<Player> players, List<Iterator<Integer>> stacks, Duration timeout) throws InterruptedException {
        int current = numberOfGames.incrementAndGet();
        try {
            assert (current <= 100);
        } catch (AssertionError e) {
            numberOfGames.decrementAndGet();
        }

        Game game = new Game(players, stacks, timeout);
        List<RecordedMove> transcript;
        try {
            transcript = game.play();
        } finally {
            numberOfGames.decrementAndGet();
        }

        return transcript;
    }

    private class Game {
        private List<Iterator<Integer>> reserveStacks;
        private List<List<Integer>> hands;
        private AtomicIntegerArray topCards;
        private Duration timeout;
        private long startNanos;
        private List<Player> players;
        private CountDownLatch gameFinished;
        private ReentrantLock[] topCardsLock;
        private List<List<RecordedMove>> playersTranscripts;

        public Game (List<Player> players, List<Iterator<Integer>> stacks, Duration timeout) throws InterruptedException {
            assert (players.size() == stacks.size());
            assert (players.size() >= 2 && players.size() <= 1000);

            this.reserveStacks = stacks;
            this.hands = new ArrayList<>();
            this.topCards = new AtomicIntegerArray(players.size());
            this.players = players;
            this.timeout = timeout;
            this.topCardsLock = new ReentrantLock[players.size()];
            for (int i = 0; i < players.size(); i++) {
                this.topCardsLock[i] = new ReentrantLock(true);
            }
            this.playersTranscripts = new ArrayList<>();
            for (int i = 0; i < players.size(); i++) {
                this.playersTranscripts.add(new ArrayList<RecordedMove>());
            }
        }

        public List<RecordedMove> play() throws InterruptedException {
            int numPlayers = players.size();

            // Firstly we need to initialize hands.
            for (int i = 0; i < numPlayers; i++) {
                Iterator<Integer> stack = reserveStacks.get(i);
                setupHand(stack);
                topCards.set(i, stack.next());
            }

            List<Thread> playerThreads = new ArrayList<>();
            for (int i = 0; i < numPlayers; i++) {
                playerThreads.add(new Thread(new Play(i)));
            }

            this.startNanos = Clock.nanoTime();
            this.gameFinished = new CountDownLatch(numPlayers - 1);

            for (Thread t : playerThreads) {
                t.start();
            }

            try {
                this.gameFinished.await(timeout.toNanos(), TimeUnit.NANOSECONDS);
            } finally {
                for (Thread t : playerThreads) {
                    t.interrupt();
                }

                for (Thread t : playerThreads) {
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            return playersTranscripts.stream()
                    .flatMap(List::stream)
                    .sorted(Comparator.comparing(RecordedMove::nanos))
                    .toList();

        }

        private void setupHand(Iterator<Integer> stack) {
            List<Integer> hand = new ArrayList<>();
            for (int i = 0; i < GameRules.HAND_SIZE; i++) {
                hand.add(stack.next());
            }
            hands.add(hand);
        }

        private class Play implements Runnable {
            int playerNumber;

            public Play(int playerNumber) {
                this.playerNumber = playerNumber;
            }

            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted() && !hands.get(playerNumber).isEmpty()) {
                        int[] handsCopy = asArray(hands.get(playerNumber));
                        int[] topCardsCopy = new int[topCards.length()];
                        for (int i = 0; i < topCards.length(); i++)
                            topCardsCopy[i] = topCards.get(i);

                        Player.Move move = players.get(playerNumber).chooseMove(handsCopy, topCardsCopy);
                        if (move == null) {
                            continue;
                        }
                        switch (move) {
                            case Player.Move.PlayCard playCard -> playIfLegal(playerNumber, playCard);
                            case Player.Move.Wait ignored -> {
                            }
                        }
                    }

                    if (hands.get(playerNumber).isEmpty()) {
                        gameFinished.countDown();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private static int[] asArray(List<Integer> list) {
            int[] arr = new int[list.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = list.get(i);
            }
            return arr;
        }

        private void playIfLegal(int playerIdx, Player.Move.PlayCard move) {
            List<Integer> hand = hands.get(playerIdx);
            if (move.cardIndex() < 0 || move.cardIndex() >= hand.size()) {
                return;
            }
            int stackIdx = move.stackIndex();
            if (stackIdx < 0 || stackIdx >= topCards.length()) {
                return;
            }
            int card = hand.get(move.cardIndex());
            ReentrantLock lock = topCardsLock[stackIdx];
            lock.lock();
            long moveTime;
            try {
                if (!GameRules.isInSequence(card, topCards.get(stackIdx))) {
                    return;
                }
                topCards.set(stackIdx, card);
                moveTime = Clock.nanoTime() - startNanos;
            } finally {
                lock.unlock();
            }


            playersTranscripts.get(playerIdx).add(new RecordedMove(moveTime, playerIdx, move.stackIndex(), card));

            hand.remove(move.cardIndex());

            Iterator<Integer> reserveStack = reserveStacks.get(playerIdx);
            if (reserveStack.hasNext()) {
                hand.add(reserveStack.next());
            }
        }
    }
}