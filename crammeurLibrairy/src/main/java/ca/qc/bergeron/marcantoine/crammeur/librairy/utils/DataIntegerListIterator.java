package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataListIterator;

/**
 * Created by Marc-Antoine on 2017-09-18.*/



public final class DataIntegerListIterator<T extends Data<Integer>> extends ca.qc.bergeron.marcantoine.crammeur.librairy.utils.DataListIterator<T, Integer> {

    protected final LinkedList<T> values;
    protected transient int mIndex = NULL_INDEX;

    private DataIntegerListIterator(LinkedList<T> pValues) {
        values = pValues;
    }

    public DataIntegerListIterator() {
        values = new LinkedList<T>();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mIndex = NULL_INDEX;
    }

    @Override
    public T get(@NotNull Integer pIndex) {
        return null;
    }

    @Override
    public T set(@NotNull Integer pIndex, @NotNull T pData) {
        return null;
    }

    @Override
    public void add(@NotNull Integer pIndex, @NotNull T pData) {

    }

    @NotNull
    @Override
    public Integer indexOf(@NotNull T pData) {
        return null;
    }

    @NotNull
    @Override
    public Integer lastIndexOf(@NotNull T pData) {
        return null;
    }

    @NotNull
    @Override
    public List<T> currentCollection() {
        return null;
    }

    @NotNull
    @Override
    public Iterable<Collection<T>> allCollections() {
        return null;
    }

    @NotNull
    @Override
    public List<T> collectionOf(@NotNull Integer pIndex) {
        return null;
    }

    @Override
    public <E extends T> void addAll(@NotNull Integer pIndex, @NotNull DataListIterator<E, Integer> pDataListIterator) {

    }

    @Override
    public boolean contains(@NotNull T pData) {
        return false;
    }

    @Override
    public boolean remove(@NotNull T pData) {
        return false;
    }

    @Override
    public <E extends T> void retainAll(@NotNull DataListIterator<E, Integer> pDataListIterator) {

    }

    @Override
    public void clear() {
        values.clear();
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        return null;
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(@NotNull Integer pIndex) {
        return null;
    }

    @NotNull
    @Override
    public DataListIterator<T, Integer> subDataListIterator(@NotNull Integer pIndex1, @NotNull Integer pIndex2) {
        return null;
    }

    @NotNull
    @Override
    public Integer size() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @NotNull
    @Override
    public Integer indexOfKey(@Nullable Integer pKey) {
        return null;
    }

    @NotNull
    @Override
    public Integer lastIndexOfKey(@Nullable Integer pKey) {
        return null;
    }

    @Override
    public int currentCollectionIndex() {
        return mIndex;
    }

    @Deprecated
    @Override
    public int collectionIndexOf(@NotNull Integer pIndex) {
        return pIndex;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public T next() {
        return null;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public T previous() {
        return null;
    }

    @Override
    public int nextIndex() {
        return 0;
    }

    @Override
    public int previousIndex() {
        return 0;
    }

    @Override
    public void remove() {
        values.remove(mIndex);
    }

    @Override
    public void set(T t) {

    }

    @Override
    public void add(T t) {

    }
}
