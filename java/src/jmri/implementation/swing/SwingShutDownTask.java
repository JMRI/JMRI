package jmri.implementation.swing;

import java.awt.Component;
import javax.swing.JOptionPane;
import jmri.implementation.AbstractShutDownTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a base for using Swing to ask if shutdown should conditionally
 * continue.
 * <p>
 * Sequence:
 * <ol>
 * <li>checkPromptNeeded determines if ready to shutdown. If so, return ready.
 * <li>Issue a prompt, asking if the user wants to continue or do something else
 * <li>Recheck until something decided.
 * </ol>
 *
 * <p>
 * If no "action" name is provided, only the continue and cancel options are
 * shown.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class SwingShutDownTask extends AbstractShutDownTask {

    /**
     * Constructor specifies the warning message and action to take
     *
     * @param name      the name of the task (used in logs)
     * @param warning   the prompt to display
     * @param action    the action button to display
     * @param component the parent component of the dialog
     */
    public SwingShutDownTask(String name, String warning, String action, Component component) {
        super(name);
        this.component = component;
        this.warning = warning;
        this.action = action;
    }

    String warning;
    String action;
    Component component;

    /**
     * Take the necessary action.
     *
     * @return true if the shutdown should continue, false to abort.
     */
    @Override
    public boolean execute() {
        while (!checkPromptNeeded()) {
            // issue prompt
            Object[] possibleValues;
            if (action != null) {
                possibleValues = new Object[]{Bundle.getMessage("ButtonContinue"),
                    Bundle.getMessage("ButtonAbort"),
                    action};
            } else {
                possibleValues = new Object[]{Bundle.getMessage("ButtonContinue"),
                    Bundle.getMessage("ButtonAbort")};
            }

            int selectedValue = JOptionPane.showOptionDialog(component,
                    warning,
                    Bundle.getMessage("ShutDownWarningTitle"),
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE, null,
                    possibleValues, possibleValues[possibleValues.length - 1]);
            switch (selectedValue) {
                case 1:
                    // abort quit
                    return false;
                case 0:
                    // quit anyway
                    return true;
                case 2:
                    // take action and try again
                    return doPrompt();
                case -1:
                    // dialog window closed
                    return doClose();
                default:
                    // unexpected value, log but continue
                    log.error("unexpected selection: " + selectedValue);
                    return true;
            }
        }
        // break out of loop when ready to continue       
        return true;
    }

    /**
     * Provide a subclass-specific check as to whether it's OK to shutdown. If
     * not, issue a prompt before continuing. Default implementation never
     * passes, causing message to be emitted.
     *
     * @return true if ready to shutdown, and no prompt needed. false to present
     *         dialog before shutdown proceeds
     */
    protected boolean checkPromptNeeded() {
        return false;
    }

    /**
     * Provide a subclass-specific method to handle the request to fix the
     * problem. This is a dummy implementation, intended to be overloaded.
     *
     * @return true if ready to shutdown, false to end shutdown
     */
    protected boolean doPrompt() {
        return true;
    }

    /**
     * Provide a subclass-specific method to handle the case where the user has
     * chosen the close window option.
     *
     * @return true if ready to shutdown, false to end shutdown
     */
    protected boolean doClose() {
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(SwingShutDownTask.class);

}
