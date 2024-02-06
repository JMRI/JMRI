package jmri.jmrit.logixng.implementation;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;

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

    private ValueAndType[] _stack = new ValueAndType[INITIAL_SIZE];

    /** {@inheritDoc} */
    @Override
    public void push(ValueAndType valueAndType) {
        if (_count+1 >= _size) {
            ValueAndType[] newStack = new ValueAndType[_size + GROW_SIZE];
            System.arraycopy(_stack, 0, newStack, 0, _size);
            _stack = newStack;
            _size += GROW_SIZE;
        }
        _stack[_count++] = valueAndType;
    }

    /** {@inheritDoc} */
    @Override
    public Object pop() {
        if (_count <= 0) throw new ArrayIndexOutOfBoundsException("Stack is empty");
        return _stack[--_count]._value;
    }

    /** {@inheritDoc} */
    @Override
    public Object getValueAtIndex(int index) {
        return _stack[index]._value;
    }

    /** {@inheritDoc} */
    @Override
    public void setValueAtIndex(int index, Object value) {
        LogixNGPreferences prefs = InstanceManager.getDefault(LogixNGPreferences.class);
        if (prefs.getStrictTypingLocalVariables()) {
            _stack[index]._value = SymbolTable.validateStrictTyping(
                    _stack[index]._type, _stack[index]._value, value);
        } else {
            _stack[index]._value = value;
        }
    }

    /** {@inheritDoc} */
    @Override
    public ValueAndType getValueAndTypeAtIndex(int index) {
        return _stack[index];
    }

    /** {@inheritDoc} */
    @Override
    public void setValueAndTypeAtIndex(int index, ValueAndType valueAndType) {
        _stack[index] = valueAndType;
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
