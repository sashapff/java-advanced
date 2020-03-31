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
    private final ParallelQueue<Runnable> queue;

    /**
     * Thread-number constructor. Create implementation of {@code ParallelMapper} with {@code threads} threads.
     *
     * @param threads number of available threads.
     */
    public ParallelMapperImpl(final int threads) {
        queue = new ParallelQueue<>(new ArrayDeque<>());
        final Runnable startTask = () -> {
            try {
                while (!Thread.interrupted()) {
                    Runnable task = queue.poll();
                    task.run();
                }
            } catch (final InterruptedException ignored) {
            } finally {
                Thread.currentThread().interrupt();
            }
        };
        workers = IntStream.range(0, threads).mapToObj(unused -> new Thread(startTask)).collect(Collectors.toList());
        workers.forEach(Thread::start);
    }

    private class ParallelQueue<T> {
        private final Queue<T> elements;

        ParallelQueue(Queue<T> elements) {
            this.elements = elements;
        }

        synchronized T poll() throws InterruptedException {
            while (elements.isEmpty()) {
                wait();
            }
            return elements.poll();
        }

        synchronized void add(final T task) {
            elements.add(task);
            notify();
        }

    }

    private class ParallelList<T> {
        private final List<T> results;
        private final List<RuntimeException> exceptions;
        private int changed;

        ParallelList(final int size) {
            results = new ArrayList<>(Collections.nCopies(size, null));
            changed = 0;
            exceptions = new ArrayList<>();
        }

        private synchronized void updateChanged() {
            changed++;
            if (changed == results.size()) {
                notify();
            }
        }

        synchronized void set(final int index, final T result) {
            results.set(index, result);
            updateChanged();
        }

        synchronized void addException(final RuntimeException element) {
            exceptions.add(element);
            updateChanged();
        }

        synchronized List<T> getList() throws InterruptedException {
            while (changed < results.size()) {
                wait();
            }
            if (!exceptions.isEmpty()) {
                final RuntimeException exception = exceptions.get(0);
                exceptions.subList(1, exceptions.size() - 1).forEach(exception::addSuppressed);
                throw exception;
            }
            return results;
        }

    }

    /**
     * Parallel mapping function {@code function} to {@code List} of arguments.
     *
     * @param function function to map.
     * @param list     {@code List} of arguments.
     * @param <T>      type of arguments.
     * @param <R>      type of result arguments.
     * @return {@code List} of result arguments.
     * @throws InterruptedException if any error occur during execution.
     */
    @Override
    public <T, R> List<R> map(final Function<? super T, ? extends R> function,
                              final List<? extends T> list) throws InterruptedException {
        final ParallelList<R> collector = new ParallelList<>(list.size());
        int index = 0;
        for (final T value : list) {
            final int i = index++;
            synchronized (queue) {
                queue.add(() -> {
                    try {
                        collector.set(i, function.apply(value));
                    } catch (final RuntimeException e) {
                        collector.addException(e);
                    }
                });
            }
        }
        return collector.getList();
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
