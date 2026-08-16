package jmri.util;

import java.util.*;

/**
 * A list that loggs some of its calls.
 *
 * @param <E> the type of elements in this list
 *
 * @author Daniel Bergqvist (C) 2026
 */
public class ListLogger<E extends Object> implements List<E> {

    /**
     * Log where it's called?
     */
    private static final boolean LOG_WHERE = false;

    private final List<E> list;

    public ListLogger(List<E> list) {
        log.warn("ListLogger created");
        if (LOG_WHERE) LoggingUtil.shortenStacktrace(new Exception()).printStackTrace();
        this.list = list;
    }

    @Override
    public int size() {
        log.warn("size() called");
        if (LOG_WHERE) LoggingUtil.shortenStacktrace(new Exception()).printStackTrace();
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T extends Object> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(E element) {
        return list.add(element);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return list.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        log.warn("clear() called");
        if (LOG_WHERE) LoggingUtil.shortenStacktrace(new Exception()).printStackTrace();
        list.clear();
    }

    @Override
    public E get(int index) {
        return list.get(index);
    }

    @Override
    public E set(int index, E element) {
        return list.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        list.add(index, element);
    }

    @Override
    public E remove(int index) {
        return list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        log.warn("subList() called");
        if (LOG_WHERE) LoggingUtil.shortenStacktrace(new Exception()).printStackTrace();
        return list.subList(fromIndex, toIndex);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ListLogger.class);
}
