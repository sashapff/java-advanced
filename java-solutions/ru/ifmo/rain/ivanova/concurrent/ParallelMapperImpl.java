package ru.ifmo.rain.ivanova.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implements {@code ParallelMapper} interface.
 *
 * @author sasha.pff
 * @see info.kgeorgiy.java.advanced.mapper.ParallelMapper
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> workers;
    private final Queue<Runnable> tasks = new ArrayDeque<>();

    /**
     * Thread-number constructor. Create implementation of {@code ParallelMapper} with {@code threads} threads.
     *
     * @param threads number of available threads.
     */
    public ParallelMapperImpl(final int threads) {
        final Runnable startTask = () -> {
            try {
                while (!Thread.interrupted()) {
                    synchronizedRun();
                }
            } catch (final InterruptedException ignored) {
            } finally {
                Thread.currentThread().interrupt();
            }
        };
        workers = IntStream.range(0, threads).mapToObj(unused -> new Thread(startTask)).collect(Collectors.toList());
        workers.forEach(Thread::start);
    }

    private void synchronizedRun() throws InterruptedException {
        final Runnable task;
        // :NOTE: move to tasks
        synchronized (tasks) {
            while (tasks.isEmpty()) {
                tasks.wait();
            }
            task = tasks.poll();
        }
        task.run();
    }

    private void synchronizedAdd(final Runnable task) {
        synchronized (tasks) {
            tasks.add(task);
            tasks.notify();
        }
    }

    private class ParallelCollector<T> {
        private final List<T> results;
        private int changed;

        ParallelCollector(final int size) {
            results = new ArrayList<>(Collections.nCopies(size, null));
            changed = 0;
        }

        synchronized void setResult(final int index, final T result) {
            results.set(index, result);
            changed++;
            if (changed == results.size()) {
                notify();
            }
        }

        synchronized List<T> getResults() throws InterruptedException {
            while (changed < results.size()) {
                wait();
            }
            return results;
        }
    }

    /**
     * Parallel mapping function {@code function} to {@code List} of arguments.
     *
     * @param function function to map.
     * @param list {@code List} of arguments.
     * @param <T> type of arguments.
     * @param <R> type of result arguments.
     * @return {@code List} of result arguments.
     * @throws InterruptedException if any error occur during execution.
     */
    @Override
    public <T, R> List<R> map(final Function<? super T, ? extends R> function,
                              final List<? extends T> list) throws InterruptedException {
        // :NOTE: Unchecked warning
        final ParallelCollector collector = new ParallelCollector(list.size());
        final List<RuntimeException> exceptions = new ArrayList<>();
        int index = 0;
        for (final Iterator<? extends T> iterator = list.iterator(); iterator.hasNext(); index++) {
            final T value = iterator.next();
            final int i = index;
            synchronizedAdd(() -> {
                // :NOTE: Унифицировать
                try {
                    collector.setResult(i, function.apply(value));
                } catch (final RuntimeException e) {
                    exceptions.add(e);
                }
            });
        }
        if (!exceptions.isEmpty()) {
            final RuntimeException exception = new RuntimeException();
            exceptions.forEach(exception::addSuppressed);
            throw exception;
        }
        return collector.getResults();
    }

    /**
     * Method to stop all threads.
     */
    @Override
    public void close() {
        workers.forEach(Thread::interrupt);
        workers.forEach(worker -> {
            try {
                worker.join();
            } catch (final InterruptedException ignored) {
            }
        });
    }
}
