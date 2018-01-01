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
public class NamedBeanExpectedState<T extends NamedBean> extends Bean implements ExpectedState<T, Integer> {

    private final T bean;
    private Integer state;

    public NamedBeanExpectedState(T bean, Integer state) {
        this.bean = bean;
        NamedBeanExpectedState.this.setExpectedState(state);
    }

    @Override
    public Integer getExpectedState() {
        return state;
    }

    @Override
    public void setExpectedState(Integer state) throws UnsupportedOperationException {
        Integer old = this.state;
        this.state = state;
        this.propertyChangeSupport.firePropertyChange(EXPECTED_STATE, old, state);
    }

    @Override
    public T getObject() {
        return this.bean;
    }

}
