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
    private List<Thread> workers;
    private final Queue<Runnable> tasks;

    /**
     * Thread-number constructor. Create implementation of {@code ParallelMapper} with {@code threads} threads.
     *
     * @param threads number of available threads.
     */
    public ParallelMapperImpl(int threads) {
        tasks = new ArrayDeque<>();
        Runnable start_task = () -> {
            try {
                while (!Thread.interrupted()) {
                    synchronizedRun();
                }
            } catch (InterruptedException ignored) {
            } finally {
                Thread.currentThread().interrupt();
            }
        };
        workers = IntStream.range(0, threads).mapToObj(unused -> new Thread(start_task)).collect(Collectors.toList());
        workers.forEach(Thread::start);
    }

    private void synchronizedRun() throws InterruptedException {
        Runnable task;
        synchronized (tasks) {
            while (tasks.isEmpty()) {
                tasks.wait();
            }
            task = tasks.poll();
        }
        task.run();
    }

    private void synchronizedAdd(Runnable task) throws InterruptedException {
        synchronized (tasks) {
            tasks.add(task);
            tasks.notify();
        }
    }

    private class ParallelCollector<T> {
        private List<T> results;
        private int changed;

        ParallelCollector(int size) {
            results = new ArrayList<>(Collections.nCopies(size, null));
            changed = 0;
        }

        synchronized void setResult(int index, T result) {
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
    public <T, R> List<R> map(Function<? super T, ? extends R> function,
                              List<? extends T> list) throws InterruptedException {
        ParallelCollector collector = new ParallelCollector(list.size());
        List<RuntimeException> exceptions = new ArrayList<>();
        int index = 0;
        for (Iterator<? extends T> iterator = list.iterator(); iterator.hasNext(); index++) {
            T value = iterator.next();
            final int i = index;
            synchronizedAdd(() -> {
                try {
                    collector.setResult(i, function.apply(value));
                } catch (RuntimeException e) {
                    exceptions.add(e);
                }
            });
        }
        if (!exceptions.isEmpty()) {
            RuntimeException exception = new RuntimeException();
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
            } catch (InterruptedException ignored) {
            }
        });
    }
}
