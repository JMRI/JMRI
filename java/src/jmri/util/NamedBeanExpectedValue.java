package jmri.util;

import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import jmri.ExpectedState;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.beans.Bean;

/**
 * Retain a NamedBean and its expected value (called a state in this class).
 * <p>
 * Note the NamedBean is held in a {@link NamedBeanHandle}.
 *
 * @author Randall Wood Copyright 2017
 * @param <T> the supported type of NamedBean
 * @param <S> the supported type of value
 */
public class NamedBeanExpectedValue<T extends NamedBean, S extends Object> extends Bean implements ExpectedState<T, S> {

    private final NamedBeanHandle<T> handle;
    private S state;

    /**
     * Create a NamedBeanExpectedValue, using the provided tracked name for the
     * NamedBean.
     *
     * @param bean  the bean
     * @param name  the name
     * @param state the expected state
     */
    public NamedBeanExpectedValue(@Nonnull T bean, @Nonnull String name, @Nonnull S state) {
        this.handle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(name, bean);
        this.state = state;
    }

    /**
     * Create a NamedBeanExpectedValue, using {@link NamedBean#getDisplayName()}
     * to provide the tracked name for the NamedBean.
     *
     * @param bean  the bean
     * @param state the expected state
     */
    public NamedBeanExpectedValue(@Nonnull T bean, S state) {
        this(bean, bean.getDisplayName(), state);
    }

    @Override
    public S getExpectedState() {
        return state;
    }

    @Override
    public void setExpectedState(@Nonnull S state) throws UnsupportedOperationException {
        S old = this.state;
        this.state = state;
        this.propertyChangeSupport.firePropertyChange(EXPECTED_STATE, old, state);
    }

    @Override
    public T getObject() {
        return this.handle.getBean();
    }

    /**
     * Get the name of the contained NamedBean object.
     *
     * @return the name
     */
    public String getName() {
        return this.handle.getName();
    }
}
