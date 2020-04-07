package ru.ifmo.rain.ivanova.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Consumer;
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
    private final TasksQueue queue = new TasksQueue();
    private volatile boolean closed = false;

    /**
     * Thread-number constructor. Create implementation of {@code ParallelMapper} with {@code threads} threads.
     *
     * @param threads number of available threads.
     */
    public ParallelMapperImpl(final int threads) {
        final Runnable startTask = () -> {
            try {
                while (!Thread.interrupted()) {
                    queue.runNextTask();
                }
            } catch (final InterruptedException ignored) {
            } finally {
                Thread.currentThread().interrupt();
            }
        };
        workers = IntStream.range(0, threads).mapToObj(unused -> new Thread(startTask)).collect(Collectors.toList());
        workers.forEach(Thread::start);
    }

    private class TasksQueue {
        private final Queue<Task<?, ?>> elements = new ArrayDeque<>();

        Runnable waitAndRun() throws InterruptedException {
            while (elements.isEmpty()) {
                wait();
            }
            return elements.peek().getAndRun();
        }

        synchronized void runNextTask() throws InterruptedException {
            Runnable runnableTask = waitAndRun();
            if (runnableTask == null) {
                elements.poll();
                waitAndRun();
            }
        }

        synchronized void add(final Task<?, ?> task) {
            elements.add(task);
            notify();
        }

        synchronized void forEach(final Consumer<? super Task<?, ?>> consumer) {
            elements.forEach(consumer);
        }
    }

    private class Task<T, R> {
        private final Queue<Runnable> runnableTasks = new ArrayDeque<>();
        private final List<R> results;
        private final List<RuntimeException> exceptions = new ArrayList<>();
        private int finished = 0;
        private boolean terminated = false;

        Task(final Function<? super T, ? extends R> function, final List<? extends T> list) {
            results = new ArrayList<>(Collections.nCopies(list.size(), null));
            int index = 0;
            for (T value : list) {
                int i = index++;
                runnableTasks.add(() -> {
                    try {
                        set(i, function.apply(value));
                    } catch (RuntimeException e) {
                        addException(e);
                    }
                });
            }
        }

        synchronized Runnable getAndRun() {
            Runnable runnable = runnableTasks.poll();
            if (runnable != null) {
                runnable.run();
                finished++;
                if (finished == results.size()) {
                    terminate();
                }
            }
            return runnable;
        }

        synchronized void terminate() {
            terminated = true;
            notify();
        }

        synchronized void set(final int index, final R result) {
            if (!terminated) {
                results.set(index, result);
            }
        }

        synchronized void addException(final RuntimeException element) {
            if (!terminated) {
                exceptions.add(element);
            }
        }

        synchronized List<R> getResult() throws InterruptedException {
            while (!terminated) {
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
        final Task<T, R> task = new Task<>(function, list);
        synchronized (this) {
            if (!closed) {
                queue.add(task);
            }
        }
        return task.getResult();
    }

    /**
     * Method to stop all threads.
     */
    @Override
    public void close() {
        closed = true;
        workers.forEach(Thread::interrupt);
        queue.forEach(Task::terminate);
        for (int i = 0; i < workers.size(); i++) {
            try {
                workers.get(i).join();
            } catch (InterruptedException e) {
                i--;
            }
        }
    }
}