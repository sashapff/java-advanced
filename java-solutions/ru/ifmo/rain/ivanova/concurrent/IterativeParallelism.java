package ru.ifmo.rain.ivanova.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
    private final ParallelMapper mapper;

    /**
     * Default constructor. Implementation of {@code IterativeParallelism} without {@code ParallelMapper}.
     */
    public IterativeParallelism() {
        mapper = null;
    }

    /**
     * Mapper constructor. Implementation of {@code IterativeParallelism} with {@code ParallelMapper}.
     *
     * @param mapper mapper to use.
     */
    public IterativeParallelism(final ParallelMapper mapper) {
        this.mapper = mapper;
    }

    private <T> List<Stream<? extends T>> split(final int threads, final List<? extends T> values) {
        final List<Stream<? extends T>> blocks = new ArrayList<>();
        final int blockSize = values.size() / threads;
        final int blockRest = values.size() % threads;
        int position = 0;
        for (int i = 0; i < threads; i++) {
            final int begin = position;
            position += blockSize + ((blockRest > i) ? 1 : 0);
            final int end = position;
            if (end - begin > 0) {
                blocks.add(values.subList(begin, end).stream());
            }
        }
        return blocks;
    }

    private void joinAllThreads(final List<Thread> workers) throws InterruptedException {
        final int threads = workers.size();
        for (int index = 0; index < threads; index++) {
            try {
                workers.get(index).join();
            } catch (final InterruptedException e) {
                final InterruptedException exception = new InterruptedException();
                exception.addSuppressed(e);
                for (int i = index; i < threads; i++) {
                    workers.get(i).interrupt();
                }
                for (int i = index; i < threads; i++) {
                    try {
                        workers.get(i).join();
                    } catch (final InterruptedException e1) {
                        exception.addSuppressed(e1);
                        i--;
                    }
                }
                throw exception;
            }
        }
    }

    private <E, R> List<R> map(final Function<? super E, ? extends R> function,
                               final List<? extends E> blocks) throws InterruptedException {
        final List<R> blockAnswers = new ArrayList<>(Collections.nCopies(blocks.size(), null));
        final List<Thread> workers = IntStream.range(0, blocks.size())
                .mapToObj(index -> new Thread(() -> blockAnswers.set(index, function.apply(blocks.get(index)))))
                .collect(Collectors.toList());
        workers.forEach(Thread::start);
        joinAllThreads(workers);
        return blockAnswers;
    }

    private <T, E, A> A run(int threads, final List<? extends T> values,
                            final Function<Stream<? extends T>, E> function,
                            final Function<? super Stream<E>, A> reduce) throws InterruptedException {
        final List<Stream<? extends T>> blocks = split(threads, values);
        final List<E> blockAnswers;
        if (mapper == null) {
            blockAnswers = map(function, blocks);
        } else {
            blockAnswers = mapper.map(function, blocks);
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

    private <T> List<T> collectStream(final Stream<? extends List<? extends T>> stream) {
        return stream.flatMap(List::stream).collect(Collectors.toList());
    }

    private <T, R> List<R> runFilterAndMap(final int threads, final List<? extends T> values,
                                           final Function<Stream<? extends T>, Stream<? extends R>> function)
            throws InterruptedException {
        return run(threads, values, stream -> function.apply(stream).collect(Collectors.toList()), this::collectStream);
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
    public <T> List<T> filter(final int threads, final List<? extends T> values,
                              final Predicate<? super T> predicate) throws InterruptedException {
        return runFilterAndMap(threads, values, stream -> stream.filter(predicate));
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
    public <T, U> List<U> map(final int threads, final List<? extends T> values,
                              final Function<? super T, ? extends U> f) throws InterruptedException {
        return runFilterAndMap(threads, values, stream -> stream.map(f));
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
    public <T> T maximum(final int threads, final List<? extends T> values,
                         final Comparator<? super T> comparator) throws InterruptedException {
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
    public <T> T minimum(final int threads, final List<? extends T> values,
                         final Comparator<? super T> comparator) throws InterruptedException {
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
    public <T> boolean all(final int threads, final List<? extends T> values,
                           final Predicate<? super T> predicate) throws InterruptedException {
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
    public <T> boolean any(final int threads, final List<? extends T> values,
                           final Predicate<? super T> predicate) throws InterruptedException {
        return run(threads, values, stream -> stream.anyMatch(predicate),
                stream -> stream.anyMatch(Boolean::booleanValue));
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
        return mapReduce(threads, values, Function.identity(), monoid);
    }

    private <T> T reduceStream(final Stream<T> stream, final Monoid<T> monoid) {
        return stream.reduce(monoid.getOperator()).orElse(monoid.getIdentity());
    }

    private <T, R> R reduceMap(final Stream<? extends T> stream, final Monoid<R> monoid, final Function<T, R> lift) {
        return reduceStream(stream.map(lift), monoid);
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
    public <T, R> R mapReduce(final int threads, final List<T> values, final Function<T, R> lift,
                              final Monoid<R> monoid) throws InterruptedException {
        return run(threads, values, stream -> reduceMap(stream, monoid, lift), stream -> reduceStream(stream, monoid));
    }
}
