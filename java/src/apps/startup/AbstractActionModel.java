package apps.startup;

import java.text.MessageFormat;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jmri.util.ConnectionNameFromSystemName;

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

    private String systemPrefix = "";
    private String className = "";

    public String getClassName() {
        return className;
    }

    @Override
    public String getName() {
        return StartupActionModelUtil.getDefault().getActionName(className);
    }

    @Override
    public void setName(@Nonnull String n) {
        this.className = StartupActionModelUtil.getDefault().getClassName(n);
    }

    public void setClassName(@Nonnull String n) {
        className = n;
    }

    @Nonnull
    public String getSystemPrefix() {
        return this.systemPrefix;
    }

    public void setSystemPrefix(@Nullable String name) {
        if (name == null) {
            this.systemPrefix = "";
        } else {
            this.systemPrefix = name;
        }
    }

    public boolean isSystemConnectionAction() {
        return StartupActionModelUtil.getDefault().isSystemConnectionAction(className);
    }

    @Override
    public String toString() {
        if (!this.systemPrefix.isEmpty()) {
            return MessageFormat.format("<html>{0}<br>{1}</html>", this.getName(), ConnectionNameFromSystemName.getConnectionName(this.systemPrefix));
        }
        return this.getName();
    }
}
