package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Marc-Antoine on 2017-09-24.
 */

public final class Parallel {

    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();
    private static final int MAX_THREAD = NUM_CORES*2;

    public static <T2 extends T, T> void For(final List<T2> elements, final Operation<T> operation) {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD);
        final int[] index = new int[1];
        final Throwable[] throwable = new Throwable[1];
        final Callable<Void> callable = new Callable<Void>() {
            @Override
            public final Void call() throws Exception {
                if (index[0] < elements.size() && (index[0] == 0 || operation.follow())) {
                    T result = elements.get(index[0]);
                    operation.perform(result);
                    synchronized (index) {
                        index[0]++;
                    }
                }
                return null;
            }
        };
        final Runnable runnable = new Runnable() {
            @Override
            public final void run() {
                while (index[0] < elements.size()) {
                    try {
                        synchronized (callable) {
                            callable.call();
                        }
                        if (!operation.follow()) {
                            break;
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        synchronized (throwable) {
                            throwable[0] = t;
                        }
                        throw new RuntimeException(t);
                    }
                }
            }
        };
        for (int threadIndex=0; threadIndex<MAX_THREAD && threadIndex < elements.size(); threadIndex++) {
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
        if (throwable[0] != null) throw new RuntimeException(throwable[0]);
    }

    public static <T2 extends T, T> void For(final Collection<T2> elements, final Operation<T> operation) {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD);
        final Iterator<T2> iterator = elements.iterator();
        final Throwable[] throwable = new Throwable[1];
        final Callable<Void> callable = new Callable<Void>() {
            boolean first = true;
            @Override
            public final Void call() throws Exception {
                if (iterator.hasNext() && (first || operation.follow())) {
                    T result;
                    synchronized (iterator) {
                        result = iterator.next();
                    }
                    operation.perform(result);
                    if (first) first = false;
                }
                return null;
            }
        };
        final Runnable runnable = new Runnable() {
            @Override
            public final void run() {
                while (iterator.hasNext()) {
                    try {
                        synchronized (callable) {
                            callable.call();
                        }
                        if (!operation.follow()) {
                            break;
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        synchronized (throwable) {
                            throwable[0] = t;
                        }
                        throw new RuntimeException(t);
                    }
                }
            }
        };
        for (int threadIndex=0; threadIndex<MAX_THREAD && threadIndex < elements.size(); threadIndex++) {
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
        if (throwable[0] != null) throw new RuntimeException(throwable[0]);
    }

    public static <T2 extends T, T> void For(final T2[] elements, final Operation<T> operation) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final int[] index = new int[1];
        final Throwable[] throwable = new Throwable[1];
        final Callable<Void> callable = new Callable<Void>() {
            @Override
            public final Void call() throws Exception {
                if (index[0] < elements.length && (index[0] == 0 || operation.follow())) {
                    T result = elements[index[0]];
                    operation.perform(result);
                    synchronized (index) {
                        index[0]++;
                    }
                }
                return null;
            }
        };
        final Runnable runnable = new Runnable() {
            @Override
            public final void run() {
                while (index[0] < elements.length) {
                    try {
                        synchronized (callable) {
                            callable.call();
                        }
                        if (!operation.follow()) {
                            break;
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        synchronized (throwable) {
                            throwable[0] = t;
                        }
                        throw new RuntimeException(t);
                    }
                }
            }
        };
        executor.execute(runnable);
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(0,1);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        if (throwable[0] != null) throw new RuntimeException(throwable[0]);
    }

    public static <T2 extends T, T> void For(final Iterable<T2> elements, final Operation<T> operation) {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD);
        final Iterator<T2> iterator = elements.iterator();
        final Throwable[] throwable = new Throwable[1];
        final Callable<Void> callable = new Callable<Void>() {
            boolean first = true;
            @Override
            public final Void call() throws Exception {
                if (iterator.hasNext() && (first || operation.follow())) {
                    T result;
                    synchronized (iterator) {
                        result = iterator.next();
                    }
                    operation.perform(result);
                    if (first) first = false;
                }
                return null;
            }
        };
        final Runnable runnable = new Runnable() {
            @Override
            public final void run() {
                while (iterator.hasNext()) {
                    try {
                        synchronized (callable) {
                            callable.call();
                        }
                        if (!operation.follow()) {
                            break;
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        synchronized (throwable) {
                            throwable[0] = t;
                        }
                        throw new RuntimeException(t);
                    }
                }
            }
        };
        for (int threadIndex=0; threadIndex<MAX_THREAD && iterator.hasNext(); threadIndex++) {
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
        if (throwable[0] != null) throw new RuntimeException(throwable[0]);
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
