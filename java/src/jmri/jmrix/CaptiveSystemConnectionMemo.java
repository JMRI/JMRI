package jmri.jmrix;

import java.util.Comparator;
import java.util.ResourceBundle;
import jmri.NamedBean;
import jmri.util.NamedBeanPreferNumericComparator;

/**
 * For historical reasons, some NamedBean types have out-of-pattern system prefixes:
 * <ul>
 * <li>OBlocks use O (as in OB)
 * <li>RailCom tags use R
 * <li>Transponding tags use L
 * </ul>
 * This {@link jmri.SystemConnectionMemo} implementation is meant to handle these
 * by providing connection memo services without being connected to a particular system
 * instantiation.
 * <p>
 * An object of this classe does not get registered
 * with the {@link jmri.InstanceManager} and is likely to have a system prefix
 * and user name that conflict with the default values of another, user
 * controllable, manager. This SystemConnectionMemo is not intended to be
 * invoked on any system connection that is configurable by the user.
 * Note that this is handled as a special case in
 * {@link DefaultSystemConnectionMemo}.
 * <p>
 * Though not deprecated per se, this should not be used in new code.  Instead.
 * please use an {@link jmri.jmrix.internal.InternalSystemConnectionMemo}.
 *
 * @author Randall Wood Copyright 2019
 */
public class CaptiveSystemConnectionMemo extends DefaultSystemConnectionMemo {

    public CaptiveSystemConnectionMemo(String prefix, String userName) {
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
