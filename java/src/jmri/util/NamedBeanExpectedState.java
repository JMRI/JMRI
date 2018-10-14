package jmri.util;

import java.util.Objects;
import javax.annotation.Nonnull;
import jmri.NamedBean;

/**
 * Retain a NamedBean and its expected state.
 *
 * @author Randall Wood Copyright 2017
 * @param <T> the supported type of NamedBean
 */
public class NamedBeanExpectedState<T extends NamedBean> extends NamedBeanExpectedValue<T, Integer> {

    public NamedBeanExpectedState(@Nonnull T bean, @Nonnull String name, @Nonnull Integer state) {
        super(bean, name, state);
        Objects.requireNonNull(state, "state Integer must not be null");
    }

    public NamedBeanExpectedState(@Nonnull T bean, @Nonnull Integer state) {
        this(bean, bean.getDisplayName(), state);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation requires a non-null parameter.
     *
     * @throws NullPointerException if state is null
     */
    @Override
    public void setExpectedState(@Nonnull Integer state) {
        Objects.requireNonNull(state, "state Integer must not be null");
        super.setExpectedState(state);
    }
}
