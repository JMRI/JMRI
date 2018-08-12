package jmri.util;
import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Nonnull;

/**
 * An ArrayList that SpotBugs understands will never contain null elements.
 *
 * @see java.util.ArrayList
 * @see java.util.List
 * @author	Bob Jacobsen, Copyright (C) 2017
 */
public class NonNullArrayList<E> extends ArrayList<E> {

    @Override
    public boolean add(@Nonnull E e) {
        if (e == null) throw new IllegalArgumentException("NonNullArrayList.addAll cannot add null item");
        return super.add(e);
    }

    @Override
    public void add(int i, @Nonnull E e) {
        if (e == null) throw new IllegalArgumentException("NonNullArrayList.addAll cannot add null item");
        super.add(i, e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) { // ideally, would be "extends @Nonnull e", but that's not this annotation
        for (E e : c ) {
            if (e == null) throw new IllegalArgumentException("NonNullArrayList.addAll cannot accept collection containing null");
        }
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int i, Collection<? extends E> c) { // ideally, would be "extends @Nonnull e", but that's not this annotation
        for (E e : c ) {
            if (e == null) throw new IllegalArgumentException("NonNullArrayList.addAll cannot accept collection containing null");
        }
        return super.addAll(i, c);
    }

    @Override
    @Nonnull
    public E get(int i) { return super.get(i); }

    @Override
    @Nonnull
    public E remove(int i) { return super.remove(i); }

    @Override
    @Nonnull
    public E set(int i, @Nonnull E e) {
        if (e == null) throw new IllegalArgumentException("NonNullArrayList.addAll cannot set item null");
        return super.set(i, e);
    }

    // test routines for SpotBugs checking - protected so you don't see them
    // These should be clean
    protected NonNullArrayList<Integer> testAddAndReturn() {
        NonNullArrayList<Integer> t = new NonNullArrayList<>();
        t.add(100);
        // t.add(null); // SpotBugs will tag this
        return t;
    }

    protected boolean testLoop(String c) {
        NonNullArrayList<Integer> t = new NonNullArrayList<>();
        t.add(100);
        for (Integer s : t) {
            if (s.toString().equals(c)) return true; // SpotBugs should not require null check
        }
        return false;
    }

    protected boolean asArgumentCheck(NonNullArrayList<Integer> t) {
        if (t.get(0).toString().equals("100")) return true; // dereference of element of unknown Collection
        for (Integer s : t) {
            if (s.toString().equals("123")) return true; // dereference of element of unknown Collection
        }
        return false;
    }


}
