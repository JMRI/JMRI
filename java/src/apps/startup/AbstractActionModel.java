package apps.startup;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Provide services for invoking actions during configuration and startup.
 * <P>
 * The action classes and corresponding human-readable names are kept in the
 * apps.ActionListBundle properties file (which can be translated). They are
 * displayed in lexical order by human-readable name.
 * <P>
 * @author	Bob Jacobsen Copyright 2003, 2007, 2014
 * @see apps.startup.AbstractActionModelFactory
 */
public abstract class AbstractActionModel implements StartupModel {

    private SystemConnectionMemo memo = null;
    private String className = "";

    public String getClassName() {
        return className;
    }

    @Override
    public String getName() {
        return InstanceManager.getDefault(StartupActionModelUtil.class).getActionName(className);
    }

    @Override
    public void setName(@Nonnull String n) {
        this.className = InstanceManager.getDefault(StartupActionModelUtil.class).getClassName(n);
    }

    public void setClassName(@Nonnull String n) {
        className = n;
    }

    @CheckForNull
    public SystemConnectionMemo getSystemConnectionMemo() {
        return this.memo;
    }

    public void setSystemConnectionMemo(SystemConnectionMemo memo) {
        this.memo = memo;
    }

    public boolean isSystemConnectionAction() {
        return InstanceManager.getDefault(StartupActionModelUtil.class).isSystemConnectionAction(className);
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
