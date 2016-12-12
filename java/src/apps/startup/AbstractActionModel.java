package apps.startup;

import java.util.Objects;
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
        return this.className;
    }

    @Override
    public String getName() {
        if (className != null) {
            return StartupActionModelUtil.getDefault().getActionName(className);
        }
        return null;
    }

    @Override
    public void setName(@Nonnull String n) {
        // can set className to null if no class found for n
        this.className = StartupActionModelUtil.getDefault().getClassName(n);
    }

    public void setClassName(@Nonnull String n) {
        Objects.requireNonNull(n, "Class name cannot be null");
        this.className = n;
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
    public boolean isValid() {
        if (this.className != null && !this.className.isEmpty()) {
            try {
                // don't need return value, just want to know if exception is triggered
                Class.forName(className);
                return true;
            } catch (ClassNotFoundException ex) {
                return false;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String name = this.getName();
        if (name != null) {
            if (!this.systemPrefix.isEmpty()) {
                return Bundle.getMessage("AbstractActionModel.ToolTip", name, ConnectionNameFromSystemName.getConnectionName(this.systemPrefix)); // NOI18N
            }
            return name;
        }
        if (this.className != null && this.isValid()) {
            return Bundle.getMessage("AbstractActionModel.UnknownClass", this.className);
        } else if (this.className != null && !this.className.isEmpty()) {
            return Bundle.getMessage("AbstractActionModel.InvalidClass", this.className);
        }
        return Bundle.getMessage("AbstractActionModel.InvalidAction", super.toString());
    }
}
