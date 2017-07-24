package jmri.util;
import javax.annotation.Nonnull;

import java.util.ArrayList;

/**
 * An ArrayList that FindBugs understands will never contain null elements.
 *
 * @author	Bob Jacobsen, Copyright (C) 2017
 */
public class NonNullArrayList<E> extends ArrayList<E> {

    @Override
    public boolean add(@Nonnull E e) { return super.add(e); }

    @Override
    public void add(int i, @Nonnull E e) { super.add(i, e); }

    @Override
    @Nonnull
    public E get(int i) { return super.get(i); }
    
    @Override
    @Nonnull
    public E remove(int i) { return super.remove(i); }
    
    @Override
    @Nonnull
    public E set(int i, @Nonnull E e) { return super.set(i, e); }
    
    // test routines 
    protected NonNullArrayList<Integer> testAddAndReturn() {
        NonNullArrayList<Integer> t = new NonNullArrayList<>();
        t.add(100);
        // t.add(null); // FindBugs will tag this
        return t;
    }

    protected boolean testLoop(String c) {
        NonNullArrayList<Integer> t = new NonNullArrayList<>();
        t.add(100);
        for (Integer s : t) {
            if (s.toString().equals(c)) return true;
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
