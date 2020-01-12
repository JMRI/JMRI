package apps.startup;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import javax.swing.Action;
import jmri.JmriException;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.swing.SystemConnectionAction;
import jmri.util.ConnectionNameFromSystemName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide services for invoking actions during configuration and startup.
 * <p>
 * The action classes and corresponding human-readable names are provided by
 * {@link apps.startup.StartupActionFactory} instances.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2014
 * @see apps.startup.AbstractActionModelFactory
 */
public abstract class AbstractActionModel implements StartupModel {

    private String systemPrefix = ""; // NOI18N
    private String className = ""; // NOI18N
    private final List<Exception> exceptions = new ArrayList<>();
    private final static Logger log = LoggerFactory.getLogger(AbstractActionModel.class);

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
    public void setName(String n) {
        log.debug("setName(\"{}\")", n);
        // can set className to null if no class found for n
        this.className = StartupActionModelUtil.getDefault().getClassName(n);
    }

    public void setClassName(@Nonnull String n) {
        log.debug("setClassName(\"{}\")", n);
        Objects.requireNonNull(n, "Class name cannot be null");
        this.className = n;
    }

    @Nonnull
    public String getSystemPrefix() {
        return this.systemPrefix;
    }

    public void setSystemPrefix(@CheckForNull String prefix) {
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

    @Override
    public void performAction() throws JmriException {
        log.debug("Invoke Action from {}", className);
        try {
            Action action = (Action) Class.forName(className).getDeclaredConstructor().newInstance();
            if (SystemConnectionAction.class.isAssignableFrom(action.getClass())) {
                SystemConnectionMemo memo = ConnectionNameFromSystemName.getSystemConnectionMemoFromSystemPrefix(this.getSystemPrefix());
                if (memo != null) {
                    ((SystemConnectionAction) action).setSystemConnectionMemo(memo);
                } else {
                    log.warn("Connection \"{}\" does not exist and cannot be assigned to action {}\nThis warning can be silenced by configuring the connection associated with the startup action.", this.getSystemPrefix(), className);
                }
            }
            jmri.util.ThreadingUtil.runOnLayout(() -> {
                try {
                 this.performAction(action);
                } catch (JmriException ex) {
                    log.error("Error while performing startup action for class: {}", className, ex);
               }
            });
        } catch (ClassNotFoundException ex) {
            log.error("Could not find specified class: {}", className);
        } catch (IllegalAccessException ex) {
            log.error("Unexpected access exception for class: {}", className, ex);
            throw new JmriException(ex);
        } catch (InstantiationException ex) {
            log.error("Could not instantiate specified class: {}", className, ex);
            throw new JmriException(ex);
        } catch (java.lang.reflect.InvocationTargetException ex) {
            log.error("Error while invoking startup action for class: {}", className, ex);
            throw new JmriException(ex);
        } catch (NoSuchMethodException ex) {
            log.error("Could not locate specified method: {}", className, ex);
            throw new JmriException(ex);
        } catch (Exception ex) {
            log.error("Error while performing startup action for class: {}", className, ex);
            throw new JmriException(ex);
        }
    }

    @Override
    public List<Exception> getExceptions() {
        return new ArrayList<>(this.exceptions);
    }

    @Override
    public void addException(Exception exception) {
        this.exceptions.add(exception);
    }

    protected abstract void performAction(Action action) throws JmriException;
}
