package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataListIterator;

/**
 * Created by Marc-Antoine on 2017-12-03.
 */

public final class DataLongListIterator<T extends Data<Long>> extends LongListIterator<T> implements DataListIterator<T,Long> {

    /**
     * Returns the getIndex of the first occurrence of the specified element in this list, or -1 if this list does not contain the element. More formally, returns the lowest getIndex i such that (o==null ? get(i)==null : o.equals(get(i))), or -1 if there is no such getIndex.
     *
     * @param pKey key
     * @return the getIndex of the first occurrence of the specified element in this list, or -1 if this list does not contain the element
     */
    @Override
    @NotNull
    public final Long indexOfKey(@Nullable final Long pKey) {
        final long[] result = new long[1];
        result[0] = NULL_INDEX;
        Parallel.Operation<T> operation = new Parallel.Operation<T>() {
            boolean follow = true;
            long index = NULL_INDEX;

            @Override
            public void perform(T pParameter) {
                if (pParameter != null && ((pKey == null && pParameter.getId() == null) || (pKey != null && pKey.equals(pParameter.getId())))) {
                    synchronized (this) {
                        index++;
                        follow = false;
                    }
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
                return follow;
            }
        };
        for (Collection<T> collection : this.allCollections()) {
            Parallel.For(collection, operation);
            if (result[0] != NULL_INDEX) {
                break;
            }
        }
        return result[0];
    }

    /**
     * Returns the getIndex of the last occurrence of the specified element in this list, or -1 if this list does not contain the element. More formally, returns the highest getIndex i such that (o==null ? get(i)==null : o.equals(get(i))), or -1 if there is no such getIndex.
     *
     * @param pKey key
     * @return the getIndex of the last occurrence of the specified element in this list, or -1 if this list does not contain the element
     */
    @NotNull
    @Override
    public final Long lastIndexOfKey(@Nullable final Long pKey) {
        final long[] result = new long[1];
        result[0] = NULL_INDEX;
        Parallel.Operation<T> operation = new Parallel.Operation<T>() {
            long index = NULL_INDEX;

            @Override
            public void perform(T pParameter) {
                synchronized (this) {
                    index++;
                }
                if (pParameter != null) {
                    boolean equals;
                    if (pKey == null) {
                        equals = pParameter.getId() == null;
                    } else {
                        equals = pKey.equals(pParameter.getId());
                    }
                    if (equals) {
                        synchronized (result) {
                            result[0] = index;
                        }
                    }
                }
            }

            @Override
            public boolean follow() {
                return true;
            }
        };
        for (Collection<T> collection : this.allCollections()) {
            Parallel.For(collection, operation);
        }
        return result[0];
    }
}
