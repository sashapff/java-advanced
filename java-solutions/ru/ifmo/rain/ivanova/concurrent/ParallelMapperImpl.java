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
    private boolean closed = false;

    /**
     * Thread-number constructor. Create implementation of {@code ParallelMapper} with {@code threads} threads.
     *
     * @param threads number of available threads.
     */
    public ParallelMapperImpl(final int threads) {
        final Runnable startTask = () -> {
            try {
                while (!Thread.interrupted()) {
                    final Runnable runnable = queue.getNext();
                    if (runnable != null) {
                        runnable.run();
                    }
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

        synchronized Runnable waitAndGet() throws InterruptedException {
            while (elements.isEmpty()) {
                wait();
            }
            return elements.peek().getRunnableTask();
        }

        synchronized Runnable getNext() throws InterruptedException {
            final Runnable runnableTask = waitAndGet();
            if (runnableTask == null) {
                elements.poll();
            }
            return runnableTask;
        }

        synchronized void add(final Task<?, ?> task) {
            elements.add(task);
            notifyAll();
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
        final Function<? super T, ? extends R> function;

        Task(final Function<? super T, ? extends R> function, final List<? extends T> list) {
            results = new ArrayList<>(Collections.nCopies(list.size(), null));
            this.function = function;
            int index = 0;
            for (final T value : list) {
                final int i = index++;
                runnableTasks.add(() -> applyAndSet(i, value));
            }
        }

        synchronized void terminate() {
            terminated = true;
            notify();
        }

        synchronized void finish() {
            finished++;
            if (finished == results.size()) {
                terminate();
            }
        }

        void applyAndSet(final int i, final T value) {
            try {
                set(i, function.apply(value));
            } catch (final RuntimeException e) {
                addException(e);
            } finally {
                finish();
            }
        }

        synchronized Runnable getRunnableTask() {
            return runnableTasks.poll();
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
            if (closed) {
                return null;
            }
            queue.add(task);
        }
        return task.getResult();
    }

    /**
     * Method to stop all threads.
     */
    @Override
    public void close() {
        synchronized (this) {
            closed = true;
        }

        workers.forEach(Thread::interrupt);
        queue.forEach(Task::terminate);
        for (int index = 0; index < workers.size(); index++) {
            try {
                workers.get(index).join();
            } catch (final InterruptedException e) {
                for (int i = index; i < workers.size(); i++) {
                    try {
                        workers.get(i).join();
                    } catch (final InterruptedException e1) {
                        i--;
                    }
                }
            }
        }
    }
}