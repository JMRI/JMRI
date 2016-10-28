package apps.startup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jmri.util.ConnectionNameFromSystemName;

/**
 * Provide services for invoking actions during configuration and startup.
 * <p>
 * The action classes and corresponding human-readable names provided by
 * {@link apps.startup.StartupActionFactory} instances.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2014
 * @see apps.startup.AbstractActionModelFactory
 */
public abstract class AbstractActionModel implements StartupModel {

    private String systemPrefix = ""; // NOI18N
    private String className = ""; // NOI18N

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
            this.systemPrefix = ""; // NOI18N
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
            return Bundle.getMessage("AbstractActionModel.ToolTip", this.getName(), ConnectionNameFromSystemName.getConnectionName(this.systemPrefix)); // NOI18N
        }
        return this.getName();
    }
}
