package jmri.util;

import jmri.ExpectedState;
import jmri.NamedBean;
import jmri.beans.Bean;

/**
 * Retain a NamedBean and its expected state.
 *
 * @author Randall Wood Copyright 2017
 * @param <T> the supported type of NamedBean
 */
public class NamedBeanExpectedState<T extends NamedBean> extends Bean implements ExpectedState<T> {

    private final T bean;
    private int state;

    public NamedBeanExpectedState(T bean, int state) {
        this.bean = bean;
        NamedBeanExpectedState.this.setExpectedState(state);
    }

    @Override
    public int getExpectedState() {
        return state;
    }

    @Override
    public void setExpectedState(int state) throws UnsupportedOperationException {
        int old = this.state;
        this.state = state;
        this.propertyChangeSupport.firePropertyChange(EXPECTED_STATE, old, state);
    }

    @Override
    public T getObject() {
        return this.bean;
    }

}
