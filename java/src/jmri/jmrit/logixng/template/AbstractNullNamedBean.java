package jmri.jmrit.logixng.template;

//import jmri.;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.implementation.AbstractBase;

/**
 * A null named bean.
 */
public abstract class AbstractNullNamedBean extends AbstractBase {
    
    /**
     * Create a new NamedBean instance using only a system name.
     *
     * @param sys the system name for this bean; must not be null and must
     *            be unique within the layout
     */
    protected AbstractNullNamedBean(@Nonnull String sys) {
        this(sys, null);
    }

    /**
     * Create a new NamedBean instance using both a system name and
     * (optionally) a user name.
     * <p>
     * Refuses construction if unable to use the normalized user name, to prevent
     * subclass from overriding {@link #setUserName(java.lang.String)} during construction.
     *
     * @param sys  the system name for this bean; must not be null
     * @param user the user name for this bean; will be normalized if needed; can be null
     * @throws jmri.NamedBean.BadUserNameException   if the user name cannot be
     *                                               normalized
     * @throws jmri.NamedBean.BadSystemNameException if the system name is null
     */
    protected AbstractNullNamedBean(@Nonnull String sys, @CheckForNull String user) throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException {
        super(sys, user);
    }

    @Override
    public void setState(int s) throws JmriException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getState() {
        throw new UnsupportedOperationException("Not supported.");
    }

}
