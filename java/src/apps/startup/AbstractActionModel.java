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
public abstract class AbstractActionModel extends AbstractStartupModel {

    private String systemPrefix = ""; // NOI18N

    public String getClassName() {
        return this.getName();
    }

    @Override
    public String getName() {
        String name = super.getName();
        if (name != null) {
            return StartupActionModelUtil.getDefault().getActionName(name);
        }
        return null;
    }

    @Override
    public void setName(@Nonnull String n) {
        super.setName(StartupActionModelUtil.getDefault().getClassName(n));
    }

    public void setClassName(@Nonnull String n) {
        this.setName(n);
    }

    @Nonnull
    public String getSystemPrefix() {
        return this.systemPrefix;
    }

    public void setSystemPrefix(@Nullable String prefix) {
        if (prefix == null) {
            this.systemPrefix = ""; // NOI18N
        } else {
            this.systemPrefix = prefix;
        }
    }

    public boolean isSystemConnectionAction() {
        String name = this.getName();
        if (name != null) {
            return StartupActionModelUtil.getDefault().isSystemConnectionAction(name);
        }
        return false;
    }

    @Override
    public String toString() {
        String name = this.getName();
        if (name != null) {
            if (!this.systemPrefix.isEmpty()) {
                return Bundle.getMessage("AbstractActionModel.ToolTip", this.getName(), ConnectionNameFromSystemName.getConnectionName(this.systemPrefix)); // NOI18N
            }
            return name;
        }
        return super.toString();
    }
}
