package jmri.jmrit.logixng.implementation;

import jmri.jmrit.logixng.Stack;

/**
 * The default implementation of a NamedTable
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultStack implements Stack {

    static final int INITIAL_SIZE = 100;
    static final int GROW_SIZE = 100;
    
    int _size;
    int _count;
    
    private Object[] _stack = new Object[INITIAL_SIZE];
    
    /** {@inheritDoc} */
    @Override
    public void push(Object value) {
//        System.out.format("Stack.push: %s, count: %d%n", value, _count);
        if (_count+1 >= _size) {
            Object[] newStack = new Object[_size + GROW_SIZE];
            System.arraycopy(_stack, 0, newStack, 0, _size);
            _stack = newStack;
            _size += GROW_SIZE;
        }
        _stack[_count++] = value;
    }
    
    /** {@inheritDoc} */
    @Override
    public Object pop() {
//        System.out.format("Stack.pop: %s, count: %d%n", _stack[_count-1], _count);
        if (_count <= 0) throw new ArrayIndexOutOfBoundsException("Stack is empty");
        return _stack[--_count];
    }

    /** {@inheritDoc} */
    @Override
    public Object getValueAtIndex(int index) {
//        System.out.format("Stack.getValueAtIndex: %d, %s%n", index, _stack[index]);
        return _stack[index];
    }

    /** {@inheritDoc} */
    @Override
    public void setValueAtIndex(int index, Object value) {
//        System.out.format("Stack.setValueAtIndex: %d, %s%n", index, value);
        _stack[index] = value;
    }

    /** {@inheritDoc} */
    @Override
    public int getCount() {
        return _count;
    }

    /** {@inheritDoc} */
    @Override
    public void setCount(int newCount) {
        if ((newCount < 0) || (newCount > _count)) throw new IllegalArgumentException("newCount has invalid value: " + Integer.toString(newCount));
        _count = newCount;
    }

}
