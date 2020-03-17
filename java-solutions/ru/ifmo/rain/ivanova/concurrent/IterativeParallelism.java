package ru.ifmo.rain.ivanova.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implements {@code ListIP} interface.
 *
 * @author sasha.pff
 * @see info.kgeorgiy.java.advanced.concurrent.ListIP
 * @see info.kgeorgiy.java.advanced.concurrent.ScalarIP
 */
public class IterativeParallelism implements ListIP {

    private <T> List<Stream<? extends T>> split(int ths, List<? extends T> values) {
        List<Stream<? extends T>> blocks = new ArrayList<>();
        int blockSize = values.size() / ths;
        int blockRest = values.size() % ths;
        int position = 0;
        for (int i = 0; i < ths; i++) {
            int begin = position;
            position += blockSize + ((blockRest > i) ? 1 : 0);
            int end = position;
            if (end - begin > 0) {
                blocks.add(values.subList(begin, end).stream());
            }
        }
        return blocks;
    }

    private <T, E, A> A run(int ths, List<? extends T> values, Function<Stream<? extends T>, E> function,
                            Function<Stream<? extends E>, A> reduce) throws InterruptedException {
        List<Stream<? extends T>> blocks = split(ths, values);
        ths = blocks.size();
        List<E> blockAnswers = new ArrayList<>(Collections.nCopies(ths, null));
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < ths; i++) {
            int index = i;
            Thread thread = new Thread(() -> blockAnswers.set(index, function.apply(blocks.get(index))));
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        return reduce.apply(blockAnswers.stream());
    }

    /**
     * Joins {@code values} to {@code String}.
     *
     * @param threads number of concurrent threads.
     * @param values values to join.
     *
     * @return concatenated {@code String} with result of calling {@link #toString()} method of each value
     * @throws InterruptedException if any thread was interrupted
     */
    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return run(threads, values, stream -> stream.map(Object::toString).collect(Collectors.joining()),
                stream -> stream.collect(Collectors.joining()));
    }

    private <T> List<T> collect(Stream<? extends Stream<? extends T>> stream) {
        return stream.flatMap(Function.identity()).collect(Collectors.toList());
    }

    /**
     * Filters {@code values} by {@code predicate}.
     *
     * @param threads number of concurrent threads.
     * @param values values to filter.
     * @param predicate filter predicate.
     *
     * @return {@code List} of values that satisfy a predicate
     * @throws InterruptedException if any thread was interrupted
     */
    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return run(threads, values, stream -> stream.filter(predicate), this::collect);
    }

    /**
     * Applies function {@code f} to {@code values}.
     *
     * @param threads number of concurrent threads.
     * @param values values to filter.
     * @param f mapper function.
     *
     * @return {@code List} of values mapped by function
     * @throws InterruptedException if any thread was interrupted
     */
    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return run(threads, values, stream -> stream.map(f), this::collect);
    }

    /**
     * Finds maximum of {@code values}.
     *
     * @param threads number or concurrent threads.
     * @param values values to get maximum of.
     * @param comparator value comparator.
     *
     * @return maximum of values
     * @throws InterruptedException if any thread was interrupted
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return run(threads, values, stream -> stream.max(comparator).orElseThrow(),
                stream -> stream.max(comparator).orElseThrow());
    }

    /**
     * Finds minimum of {@code values}.
     *
     * @param threads number or concurrent threads.
     * @param values values to get minimum of.
     * @param comparator value comparator.
     *
     * @return minimum of values
     * @throws InterruptedException if any thread was interrupted
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return run(threads, values, stream -> stream.min(comparator).orElseThrow(),
                stream -> stream.min(comparator).orElseThrow());
    }

    /**
     * Checks if all {@code values} are satisfied by {@code predicate}.
     *
     * @param threads number or concurrent threads.
     * @param values values to test.
     * @param predicate test predicate.
     *
     * @return {@code boolean} value indicating if all values are satisfied by predicate
     * @throws InterruptedException if any thread was interrupted
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return run(threads, values, stream -> stream.allMatch(predicate),
                stream -> stream.allMatch(Boolean::booleanValue));
    }

    /**
     * Checks if any {@code value} are satisfied by {@code predicate}.
     *
     * @param threads number or concurrent threads.
     * @param values values to test.
     * @param predicate test predicate.
     *
     * @return {@code boolean} value indicating if any value is satisfied by predicate
     * @throws InterruptedException if any thread was interrupted
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return run(threads, values, stream -> stream.anyMatch(predicate),
                stream -> stream.anyMatch(Boolean::booleanValue));
    }
}
