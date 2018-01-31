package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by Marc-Antoine on 2017-12-10.
 */

public class ParallelLinkedList<E> extends LinkedList<E> {

    public ParallelLinkedList(@NotNull Collection<? extends E> c) {
        super(c);
    }

    public ParallelLinkedList() {
        super();
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        if (this.size() + c.size() < 0) throw new IllegalStateException("The collection has too much elements");
        final boolean[] result = new boolean[1];
        final Collection<E> collection = this;
        result[0] = !c.isEmpty();
        Parallel.For(c, new Parallel.Operation<E>() {
            @Override
            public void perform(E pParameter) {
                synchronized (result) {
                    synchronized (collection) {
                        result[0] = collection.add(pParameter);
                    }
                }
            }

            @Override
            public boolean follow() {
                return result[0];
            }
        });
        return result[0];
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends E> c) {
        if (this.size() + c.size() < 0) throw new IllegalStateException("The collection has too much elements");
        if (index < 0 || index > this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
        final java.util.ListIterator<E> list = this.listIterator(index);
        Parallel.For(c, new Parallel.Operation<E>() {
            @Override
            public void perform(E pParameter) {
                synchronized (list) {
                    list.add(pParameter);
                }
            }

            @Override
            public boolean follow() {
                return true;
            }
        });
        return !c.isEmpty();
    }

    @Override
    public int indexOf(final @Nullable Object o) {
        final int[] result = new int[1];
        result[0] = -1;
        Parallel.For(this, new Parallel.Operation<E>() {
            int index = 0;
            @Override
            public void perform(E pParameter) {
                if ((o == pParameter) || (pParameter != null && pParameter.equals(o))) {
                    synchronized (result) {
                        result[0] = index;
                    }
                } else {
                    synchronized (this) {
                        index++;
                    }
                }
            }

            @Override
            public boolean follow() {
                return result[0] == -1;
            }
        });
        return result[0];
    }

    @Override
    public int lastIndexOf(final @Nullable Object o) {
        final int[] result = new int[1];
        result[0] = -1;
        Parallel.For(this, new Parallel.Operation<E>() {
            int index = 0;
            @Override
            public void perform(E pParameter) {
                if ((o == pParameter) || (pParameter != null && pParameter.equals(o))) {
                    synchronized (result) {
                        result[0] = index;
                    }
                }
                synchronized (this) {
                    index++;
                }
            }

            @Override
            public boolean follow() {
                return true;
            }
        });
        return result[0];
    }

    @Override
    public boolean contains(final @Nullable Object o) {
        final boolean[] result = new boolean[1];
        Parallel.For(this, new Parallel.Operation<E>() {
            @Override
            public void perform(E pParameter) {
                if ((o == pParameter) || (pParameter != null && pParameter.equals(o))) {
                    synchronized (result) {
                        result[0] = true;
                    }
                }
            }

            @Override
            public boolean follow() {
                return !result[0];
            }
        });
        return result[0];
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        final boolean[] result = new boolean[1];
        final Collection<E> collection = this;
        result[0] = !c.isEmpty();
        Parallel.For(c, new Parallel.Operation<Object>() {
            @Override
            public void perform(Object pParameter) {
                if (!collection.contains(pParameter))
                    synchronized (result) {
                        result[0] = false;
                    }
            }

            @Override
            public boolean follow() {
                return result[0];
            }
        });
        return result[0];
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        final boolean[] result = new boolean[1];
        result[0] = !c.isEmpty();
        final Collection<E> collection = this;
        Parallel.For(c, new Parallel.Operation<Object>() {
            @Override
            public void perform(Object pParameter) {
                if (!collection.remove(pParameter))
                    synchronized (result) {
                        result[0] = false;
                    }
            }

            @Override
            public boolean follow() {
                return result[0];
            }
        });
        return result[0];
    }

    @Override
    public boolean retainAll(final @NotNull Collection<?> c) {
        final boolean[] result = new boolean[1];
        final Collection<E> delete = new LinkedList<>();
        result[0] = !c.isEmpty();
        Parallel.For(this, new Parallel.Operation<E>() {
            @Override
            public void perform(E pParameter) {
                if (!c.contains(pParameter)) {
                    synchronized (result) {
                        synchronized (delete) {
                            result[0] = delete.add(pParameter);
                        }
                    }
                }
            }

            @Override
            public boolean follow() {
                return result[0];
            }
        });
        return result[0] && this.removeAll(delete);
    }

}
