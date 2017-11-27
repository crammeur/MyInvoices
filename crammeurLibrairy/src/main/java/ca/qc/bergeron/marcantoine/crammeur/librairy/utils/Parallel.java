package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import java.util.Collection;
import java.util.Iterator;
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

    public static <T> void For(final Collection<T> elements, final Operation<T> operation) {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD);
        final Iterator<T> iterator = elements.iterator();
        final Runnable runnable = new Runnable() {
            final Callable<Void> callable = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    T result;
                    synchronized (iterator) {
                        result = iterator.next();
                    }
                    operation.perform(result);
                    return null;
                }
            };
            @Override
            public void run() {
                while (iterator.hasNext()) {
                    try {
                        synchronized (operation) {
                            if (operation.follow()) {
                                synchronized (callable) {
                                    callable.call();
                                }
                                if (!operation.follow()) {
                                    break;
                                }
                            } else {
                                break;
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
    }

    public static <T> void For(final Iterable<T> elements, final Operation<T> operation) {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD);
        final Iterator<T> iterator = elements.iterator();
        final Runnable runnable = new Runnable() {
            final Callable<Void> callable = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    T result;
                    synchronized (iterator) {
                        result = iterator.next();
                    }
                    operation.perform(result);
                    return null;
                }
            };
            @Override
            public void run() {
                while (iterator.hasNext()) {
                    try {
                        synchronized (operation) {
                            if (operation.follow()) {
                                synchronized (callable) {
                                    callable.call();
                                }
                                if (!operation.follow()) {
                                    break;
                                }
                            } else {
                                break;
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
    }
/*    @NotNull
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
    }*/

    public interface Operation<T> {
        void perform(T pParameter);
        boolean follow();
    }
}
