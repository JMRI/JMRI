package jmri;

import java.util.Objects;
import jmri.util.NamedBeanExpectedState;

/**
 * Represent a NamedBean (such as a Turnout) and specific desired setting for
 * it. These can be used to represent the setting a NamedBean has as part of a
 * particular path through a layout, or a condition that has to be true as part
 * of something.
 * <p>
 * Objects of this class are immutable, in that once created the selected bean
 * and required setting cannot be changed. However, the value of the
 * {@link #check} method does change, because it's a function of the current
 * bean setting(s).
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2008, 2010
 */
@javax.annotation.concurrent.Immutable
public class BeanSetting extends NamedBeanExpectedState<NamedBean> {

    public BeanSetting(NamedBean t, String pName, int setting) {
        super(t, pName, setting);
    }

    public BeanSetting(NamedBean t, int setting) {
        super(t, setting);
    }

    /**
     * Convenience method; check if the Bean currently has the desired setting.
     *
     * @return true if bean has expected setting; false otherwise
     */
    public boolean check() {
        return getObject().getState() == getExpectedState();
    }

    public NamedBean getBean() {
        return getObject();
    }

    public String getBeanName() {
        return super.getName();
    }

    public int getSetting() {
        return getExpectedState();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation always throws an UnsupportedOperationException since
     * a BeanSetting is immutable.
     *
     * @throws UnsupportedOperationException if called
     */
    @Override
    public void setExpectedState(Integer state) {
        throw new UnsupportedOperationException("The expected state of a BeanSetting is immutable");
    }

    // include bean and expected state in equals() and hashCode() because they can't
    // change after construction
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!Objects.equals(getClass(), obj.getClass())) {
            return false;
        } else {
            BeanSetting p = (BeanSetting) obj;
            if (p.getSetting() != this.getSetting()) {
                return false;
            }
            if (!p.getBean().equals(this.getBean())) {
                return false;
            }

        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = getSetting() * 1000;
        hash += getBean().hashCode();
        return hash;
    }
}
