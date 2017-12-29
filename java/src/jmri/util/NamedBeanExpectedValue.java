package jmri.util;

import jmri.ExpectedState;
import jmri.NamedBean;
import jmri.beans.Bean;

/**
 * Retain a NamedBean and its expected value (called a state in this class).
 *
 * @author Randall Wood Copyright 2017
 * @param <T> the supported type of NamedBean
 * @param <S> the supported type of value
 */
public class NamedBeanExpectedValue<T extends NamedBean, S extends Object> extends Bean implements ExpectedState<T, S> {

    private final T bean;
    private S state;

    public NamedBeanExpectedValue(T bean, S state) {
        this.bean = bean;
        NamedBeanExpectedValue.this.setExpectedState(state);
    }

    @Override
    public S getExpectedState() {
        return state;
    }

    @Override
    public void setExpectedState(S state) throws UnsupportedOperationException {
        S old = this.state;
        this.state = state;
        this.propertyChangeSupport.firePropertyChange(EXPECTED_STATE, old, state);
    }

    @Override
    public T getObject() {
        return this.bean;
    }

}
