package ru.ifmo.rain.ivanova.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private class ReverseArrayList<T> extends AbstractList<T> implements RandomAccess {
        private final List<T> arrayList;
        private boolean reverse;

        ReverseArrayList() {
            arrayList = Collections.emptyList();
        }

        ReverseArrayList(List<T> list) {
            arrayList = list;
        }

        ReverseArrayList(ReverseArrayList<T> reverseArrayList, boolean change) {
            this.arrayList = reverseArrayList.arrayList;
            this.reverse = reverseArrayList.reverse ^ change;
        }

        private int getIndex(int index) {
            return reverse ? (size() - 1 - index) : index;
        }

        @Override
        public T get(int index) {
            return arrayList.get(getIndex(index));
        }

        @Override
        public int size() {
            return arrayList.size();
        }

    }

    private final ReverseArrayList<E> arrayList;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        arrayList = new ReverseArrayList<>();
        comparator = null;
    }

    public ArraySet(Collection<E> collection) {
        this(collection, null);
    }

    private ArraySet(List<E> list) {
        this(list, null);
    }


    public ArraySet(Comparator<? super E> comparator) {
        arrayList = new ReverseArrayList<>();
        this.comparator = comparator;
    }

    private ArraySet(List<E> list, Comparator<? super E> comparator) {
        arrayList = new ReverseArrayList<>(list);
        this.comparator = comparator;
    }

    public ArraySet(Collection<E> collection, Comparator<? super E> comparator) {
        TreeSet<E> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(collection);
        arrayList = new ReverseArrayList<>(new ArrayList<>(treeSet));
        this.comparator = comparator;
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(arrayList).iterator();
    }

    @Override
    public int size() {
        return arrayList.size();
    }

    private int binarySearch(E e) {
        return Collections.binarySearch(arrayList, e, comparator);
    }

    @SuppressWarnings("unchecked")
    public boolean contains(Object e) {
        try {
            return binarySearch((E) e) >= 0;
        } catch (ClassCastException error) {
            return false;
        }
    }

    private E checkGet(int index) {
        if (index >= 0 && index < size()) {
            return arrayList.get(index);
        }
        return null;
    }

    private E binarySearch(E e, int a, int b) {
        int index = binarySearch(e);
        if (index < 0) {
            index = -1 - a - index;
        } else {
            index += b;
        }
        return checkGet(index);
    }

    @Override
    public E lower(E e) {
        return binarySearch(e, 1, -1);
    }

    @Override
    public E floor(E e) {
        return binarySearch(e, 1, 0);
    }

    @Override
    public E ceiling(E e) {
        return binarySearch(e, 0, 0);
    }

    @Override
    public E higher(E e) {
        return binarySearch(e, 0, 1);
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(new ReverseArrayList<E>(arrayList, true),
                Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    private int binarySearch(E e, boolean necessaryInclusive) {
        int index = binarySearch(e);
        if (index < 0) {
            index = (-1 - index);
        } else if (necessaryInclusive) {
            index = index + 1;
        }
        return index;
    }

    private ArraySet<E> getNewArraySet(Comparator<? super E> comparator) {
        return new ArraySet<>(comparator);
    }

    private NavigableSet<E> inclusiveSubSet(E fromElement, boolean fromInclusive,
                                            E toElement, boolean toInclusive) {
        int from = binarySearch(fromElement, !fromInclusive);
        int to = binarySearch(toElement, toInclusive);
        if (to - from > 0) {
            return new ArraySet<>(arrayList.subList(from, to), comparator);
        }
        return getNewArraySet(comparator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        if (comparator != null && comparator.compare(fromElement, toElement) > 0 ||
                comparator == null && ((Comparable<E>) fromElement).compareTo(toElement) > 0) {
            throw new IllegalArgumentException("fromElement > toElement");
        }
        return inclusiveSubSet(fromElement, fromInclusive, toElement, toInclusive);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        if (!isEmpty()) {
            return inclusiveSubSet(first(), true, toElement, inclusive);
        }
        return getNewArraySet(comparator);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        if (!isEmpty()) {
            return inclusiveSubSet(fromElement, inclusive, last(), true);
        }
        return getNewArraySet(comparator);
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true,
                toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    private void checkEmpty() {
        if (arrayList.isEmpty()) {
            throw new NoSuchElementException();
        }
    }

    @Override
    public E first() {
        checkEmpty();
        return arrayList.get(0);
    }

    @Override
    public E last() {
        checkEmpty();
        return arrayList.get(size() - 1);
    }
}