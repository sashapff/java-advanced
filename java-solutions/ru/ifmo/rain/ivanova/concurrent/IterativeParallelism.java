package ru.ifmo.rain.ivanova.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;

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
 * @see info.kgeorgiy.java.advanced.concurrent.AdvancedIP
 * @see info.kgeorgiy.java.advanced.concurrent.ListIP
 * @see info.kgeorgiy.java.advanced.concurrent.ScalarIP
 */
public class IterativeParallelism implements AdvancedIP {

    // :NOTE: ths -- не экономьте на буквах
    private static <T> List<Stream<T>> split(final int ths, final List<T> values) {
        final List<Stream<T>> blocks = new ArrayList<>();
        final int blockSize = values.size() / ths;
        final int blockRest = values.size() % ths;
        int position = 0;
        for (int i = 0; i < ths; i++) {
            final int begin = position;
            position += blockSize + ((blockRest > i) ? 1 : 0);
            final int end = position;
            if (end - begin > 0) {
                blocks.add(values.subList(begin, end).stream());
            }
        }
        return blocks;
    }

    private static <T, E, A> A run(int ths, final List<T> values, final Function<Stream<T>, E> function,
                                   final Function<Stream<E>, A> reduce) throws InterruptedException {
        final List<Stream<T>> blocks = split(ths, values);
        ths = blocks.size();
        // :NOTE: Лучше было создать пустым и добавлять элементы
        final List<E> blockAnswers = new ArrayList<>(Collections.nCopies(ths, null));
        final List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < ths; i++) {
            final int index = i;
            final Thread thread = new Thread(() -> blockAnswers.set(index, function.apply(blocks.get(index))));
            threads.add(thread);
            thread.start();
        }
        InterruptedException exception = null;
        int index;
        for (index = 0; index < threads.size(); index++) {
            try {
                threads.get(index).join();
            } catch (final InterruptedException e) {
                exception = new InterruptedException();
                exception.addSuppressed(e);
                break;
            }
        }
        if (exception != null) {
            for (int i = index; i < threads.size(); i++) {
                threads.get(i).interrupt();
            }
            for (int i = index; i < threads.size(); i++) {
                try {
                    threads.get(i).join();
                } catch (final InterruptedException e) {
                    exception.addSuppressed(e);
                    i--;
                }
            }
            throw exception;
        }
        return reduce.apply(blockAnswers.stream());
    }

    /**
     * Joins {@code values} to {@code String}.
     *
     * @param threads number of concurrent threads.
     * @param values  values to join.
     * @return concatenated {@code String} with result of calling {@link #toString()} method of each value
     * @throws InterruptedException if any thread was interrupted
     */
    @Override
    public String join(final int threads, final List<?> values) throws InterruptedException {
        return run(threads, values, stream -> stream.map(Object::toString).collect(Collectors.joining()),
                stream -> stream.collect(Collectors.joining()));
    }

    private static <T> List<T> collect(final Stream<? extends Stream<? extends T>> stream) {
        // :NOTE: Вся работа выполняется в этом методе однопоточно
        return stream.flatMap(Function.identity()).collect(Collectors.toList());
    }

    /**
     * Filters {@code values} by {@code predicate}.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to filter.
     * @param predicate filter predicate.
     * @return {@code List} of values that satisfy a predicate
     * @throws InterruptedException if any thread was interrupted
     */
    @Override
    public <T> List<T> filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return run(threads, values, stream -> stream.filter(predicate), IterativeParallelism::collect);
    }

    /**
     * Applies function {@code f} to {@code values}.
     *
     * @param threads number of concurrent threads.
     * @param values  values to filter.
     * @param f       mapper function.
     * @return {@code List} of values mapped by function
     * @throws InterruptedException if any thread was interrupted
     */
    @Override
    public <T, U> List<U> map(final int threads, final List<? extends T> values, final Function<? super T, ? extends U> f) throws InterruptedException {
        return run(threads, values, stream -> stream.map(f), IterativeParallelism::collect);
    }

    /**
     * Finds maximum of {@code values}.
     *
     * @param threads    number or concurrent threads.
     * @param values     values to get maximum of.
     * @param comparator value comparator.
     * @return maximum of values
     * @throws InterruptedException if any thread was interrupted
     */
    @Override
    public <T> T maximum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return run(threads, values, stream -> stream.max(comparator).orElseThrow(),
                stream -> stream.max(comparator).orElseThrow());
    }

    /**
     * Finds minimum of {@code values}.
     *
     * @param threads    number or concurrent threads.
     * @param values     values to get minimum of.
     * @param comparator value comparator.
     * @return minimum of values
     * @throws InterruptedException if any thread was interrupted
     */
    @Override
    public <T> T minimum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    /**
     * Checks if all {@code values} are satisfied by {@code predicate}.
     *
     * @param threads   number or concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return {@code boolean} value indicating if all values are satisfied by predicate
     * @throws InterruptedException if any thread was interrupted
     */
    @Override
    public <T> boolean all(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return !any(threads, values, predicate.negate());
    }

    /**
     * Checks if any {@code value} are satisfied by {@code predicate}.
     *
     * @param threads   number or concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return {@code boolean} value indicating if any value is satisfied by predicate
     * @throws InterruptedException if any thread was interrupted
     */
    @Override
    public <T> boolean any(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return run(threads, values, stream -> stream.anyMatch(predicate),
                stream -> stream.anyMatch(Boolean::booleanValue));
    }

    private static <T> T reduceStream(final Stream<T> stream, final Monoid<T> monoid) {
        return stream.reduce(monoid.getIdentity(), monoid.getOperator());
    }

    /**
     * Reduces values using monoid.
     *
     * @param threads number of concurrent threads.
     * @param values  values to reduce.
     * @param monoid  monoid to use.
     * @return values reduced by provided monoid or {@link Monoid#getIdentity() identity} if not values specified.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> T reduce(final int threads, final List<T> values, final Monoid<T> monoid) throws InterruptedException {
        final Function<Stream<T>, T> reduceStream = stream -> reduceStream(stream, monoid);
        return run(threads, values, reduceStream, reduceStream);
    }

    /**
     * Maps and reduces values using monoid.
     *
     * @param threads number of concurrent threads.
     * @param values  values to reduce.
     * @param lift    mapping function.
     * @param monoid  monoid to use.
     * @return values reduced by provided monoid or {@link Monoid#getIdentity() identity} if not values specified.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T, R> R mapReduce(final int threads, final List<T> values, final Function<T, R> lift, final Monoid<R> monoid) throws InterruptedException {
        return run(threads, values, stream -> reduceStream(stream.map(lift), monoid),
                stream -> reduceStream(stream, monoid));
    }
}
