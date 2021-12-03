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
    private boolean didPrompt = false;

    /**
     * {@inheritDoc}
     *
     * This implementation displays a dialog allowing a user continue stopping
     * the app, abort stopping the app, or take a custom action. Closing the
     * dialog without choosing any button is treated as aborting stopping the
     * app.
       *
     * @see #doClose()
     * @see #didPrompt()
     * @see #doPrompt()
     */
    @Override
    public final Boolean call() {
        if (!checkPromptNeeded()) {
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
                case -1:
                    // abort quit
                    return false;
                case 0:
                    // quit anyway
                    return true;
                case 2:
                    // take action and try again
                    didPrompt = true;
                    return doPrompt();
                default:
                    // unexpected value, log but continue
                    log.error("unexpected selection: {}", selectedValue);
                    return true;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * This implementation calls {@link #didPrompt()} if the user took the
     * prompt action.
     */
    @Override
    public void run() {
        if (didPrompt) {
            didPrompt();
        }
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
     * Handle the request to address a potential blocker to stopping. This
     * method is called in {@link #run()} and must not interact with the user.
     * <p>
     * This is a dummy implementation, intended to be overloaded.
     */
    protected void didPrompt() {
        // do nothing
    }

    /**
     * Handle the request to address a potential blocker to stopping. This
     * method is called in {@link #call()} and can interact with the user.
     * <p>
     * This is a dummy implementation, intended to be overloaded.
     *
     * @return true if ready to shutdown, false to end shutdown
     */
    protected boolean doPrompt() {
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(SwingShutDownTask.class);

}
