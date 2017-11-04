package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Marc-Antoine on 2017-09-24.
 */

public final class Parallel {

    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();
    private static final int MAX_THREAD = NUM_CORES*2;

    @NotNull
    public static <T,R> Collection<R> For(final Collection<T> elements, final Operation<T,R> operation) {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD);
        final List<R> result = new ArrayList<>(elements.size());
        final Iterator<T> iterator = elements.iterator();
        final Runnable runnable = new Runnable() {
            final Callable<R> callable = new Callable<R>() {
                @Override
                public R call() throws Exception {
                    T result;
                    synchronized (iterator) {
                        result = iterator.next();
                    }
                    return operation.perform(result);
                }
            };
            @Override
            public void run() {
                while (iterator.hasNext()) {
                    R r;
                    try {
                        if (operation.async()) {
                            r = callable.call();
                            if (operation.result()) {
                                synchronized (result) {
                                    result.add(r);
                                }
                            }
                            if (!operation.follow()) {
                                break;
                            }
                        } else {
                            synchronized (callable) {
                                r = callable.call();
                                if (operation.result()) {
                                    synchronized (result) {
                                        result.add(r);
                                    }
                                }
                                if (!operation.follow()) {
                                    break;
                                }
                            }
                        }
                    } catch (NoSuchElementException e) {
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }
        };
        for (int threadIndex=0; threadIndex<MAX_THREAD; threadIndex++) {
            executor.execute(runnable);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(0,1);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    @NotNull
    public static <T,R> Iterable<R> For(final Iterable<T> elements, final Operation<T,R> operation) {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD);
        final List<R> result = new LinkedList<>();
        final Iterator<T> iterator = elements.iterator();
        final Runnable runnable = new Runnable() {
            final Callable<R> callable = new Callable<R>() {
                @Override
                public R call() throws Exception {
                    T result;
                    synchronized (iterator) {
                        result = iterator.next();
                    }
                    return operation.perform(result);
                }
            };
            @Override
            public void run() {
                while (iterator.hasNext()) {
                    R r;
                    try {
                        if (operation.async()) {
                            r = callable.call();
                            if (operation.result()) {
                                synchronized (result) {
                                    result.add(r);
                                }
                            }
                            if (!operation.follow()) {
                                break;
                            }
                        } else {
                            synchronized (callable) {
                                r = callable.call();
                                if (operation.result()) {
                                    synchronized (result) {
                                        result.add(r);
                                    }
                                }
                                if (!operation.follow()) {
                                    break;
                                }
                            }
                        }
                    } catch (NoSuchElementException e) {
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }

        };
        for (int threadIndex=0; threadIndex<MAX_THREAD; threadIndex++) {
            executor.execute(runnable);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(0,1);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    @NotNull
    public static <T,R> Collection<Callable<R>> createCallables(final Collection<T> elements, final Operation<T,R> operation) {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD);
        final Collection<Callable<R>> callables = new ArrayList<>(elements.size());
        final Runnable runnable = new Runnable() {
            final Iterator<T> iterator = elements.iterator();
            final int maxIndex = elements.size()-1;
            final Callable<R> callable = new Callable<R>() {
                @Override
                public R call() throws Exception {
                    T result;
                    synchronized (iterator) {
                        result = iterator.next();
                    }
                    return operation.perform(result);
                }
            };
            int index = -1;
            @Override
            public void run() {
                while (index < maxIndex) {
                    //Performance sync this only
                    synchronized (this) {
                        index++;
                    }
                    if (operation.async()) {
                        callables.add(callable);
                    } else {
                        synchronized (callables) {
                            callables.add(callable);
                        }
                    }

                }
            }
        };
        for (int indexThread = 0; indexThread< MAX_THREAD; indexThread++) {
            executor.execute(runnable);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(0,1);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return callables;
    }

    @NotNull
    public static <T,R> Iterable<Callable<R>> createCallables(final Iterable<T> elements, final Operation<T,R> operation) {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD);
        final Collection<Callable<R>> result = new LinkedList<>();
        final Iterator<T> iterator = elements.iterator();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        final T element;
                        if (operation.async()) {
                            element = iterator.next();
                            synchronized (result) {
                                result.add(new Callable<R>() {
                                    @Override
                                    public R call() throws Exception {
                                        return operation.perform(element);
                                    }
                                });
                            }
                        } else {
                            synchronized (iterator) {
                                element = iterator.next();
                                synchronized (result) {
                                    result.add(new Callable<R>() {
                                        @Override
                                        public R call() throws Exception {
                                            return operation.perform(element);
                                        }
                                    });
                                }
                            }
                        }
                    } catch (NoSuchElementException e) {
                        break;
                    }
                }
            }
        };
        for (int indexThread = 0; indexThread< MAX_THREAD; indexThread++) {
            executor.execute(runnable);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(0,1);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public interface Operation<T,R> {
        R perform(T pParameter);
        boolean follow();
        boolean result();
        boolean async();
    }
}
