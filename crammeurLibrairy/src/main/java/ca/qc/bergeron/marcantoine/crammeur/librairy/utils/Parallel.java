package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
                if ((operation.follow() || index[0] == 0) && index[0] < elements.size()) {
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
                if ((operation.follow() || first) && iterator.hasNext()) {
                    T result;
                    result = iterator.next();
                    operation.perform(result);
                    if (first) {
                        synchronized (this) {
                            first = false;
                        }
                    }
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
                if ((operation.follow() || index[0] == 0 ) && index[0] < elements.length) {
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
                if ((operation.follow() || first) && iterator.hasNext()) {
                    T result;
                    result = iterator.next();
                    operation.perform(result);
                    if (first) {
                        synchronized (this) {
                            first = false;
                        }
                    }
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


    public static <T> void Execute(@NotNull final Running<T> pRunning, @Nullable final Running<?> pPreviousRunning) {
        final ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREAD);
        final Throwable[] throwable = new Throwable[1];
        final boolean[] first = new boolean[1];
        first[0] = true;
        final Callable<Void> callable = new Callable<Void>() {
            @Override
            public final Void call() throws Exception {
                if (pRunning.follow() || first[0]) {
                    pRunning.perform(pRunning.actualParam());
                }
                return null;
            }
        };
        final Runnable runnable = new Runnable() {
            @Override
            public final void run() {
                while (pRunning.follow() || first[0]) {
                    if (first[0]) {
                        synchronized (first) {
                            first[0] = false;
                        }
                    }
                    try {
                        synchronized (callable) {
                            callable.call();
                        }
                        if (!pRunning.follow()) {
                            break;
                        }
                        if (pRunning.startNext()) {
                            Execute(pRunning.nextRun(),pRunning);
                        }
                        if (pRunning.restartPrevious()){
                            Execute(pPreviousRunning,pPreviousRunning.previousRun());
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
        for (int threadIndex=0; threadIndex<MAX_THREAD; threadIndex++) {
            executorService.execute(runnable);
        }
        executorService.shutdown();
        while (!executorService.isTerminated()) {
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
            int getIndex = -1;
            @Override
            public void run() {
                while (getIndex < maxIndex) {
                    //Performance sync this only
                    synchronized (this) {
                        getIndex++;
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

    public interface Running<T> extends Operation<T> {
        boolean restartPrevious();
        @Nullable
        Running<?> previousRun();
        T actualParam();
        boolean startNext();
        @Nullable
        Running<?> nextRun();
    }
}
