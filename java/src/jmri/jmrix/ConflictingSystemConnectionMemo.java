package jmri.jmrix;

import java.util.Comparator;
import java.util.ResourceBundle;
import jmri.NamedBean;
import jmri.util.NamedBeanPreferNumericComparator;

/**
 * A SystemConnectionMemo that does not get registered its prefix registered
 * with the {@link jmri.InstanceManager} and is likely to have a system prefix
 * and user name that conflict with the default values of another, user
 * controllable, manager. This SystemConnectionMemo is not intended to be
 * invoked on any system connection that is configurable by the user, and should
 * only be used to maintain backwards compatibility with incorrectly prefixed
 * NamedBeans defined in JMRI 4.16.
 *
 * @author Randall Wood Copyright 2019
 * @deprecated used only to maintain backwards compatibility with JMRI 4.16;
 * remove immediately when no longer used
 */
@Deprecated
public class ConflictingSystemConnectionMemo extends SystemConnectionMemo {

    public ConflictingSystemConnectionMemo(String prefix, String userName) {
        super(prefix, userName);
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null; // no resource bundle
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanPreferNumericComparator<>();
    }

    @Override
    public void register() {
        // do nothing
    }
}
