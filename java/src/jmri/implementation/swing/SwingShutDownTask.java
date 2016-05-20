// SwingShutDownTask.java

package jmri.implementation.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.implementation.AbstractShutDownTask;
import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * Provides a base for using Swing to ask if shutdown should
 * conditionally continue.
 * <p>
 * Sequence:
 * <ol>
 * <li>checkPromptNeeded determines if ready to shutdown. If so, 
 * return ready.
 * <li>Issue a prompt, asking if the user wants to continue or do something else
 * <li>Recheck until something decided.
 * </ul>
 * 
 * <p>
 * If no "action" name is provided, only the continue and cancel options are shown.
 * 
 * @author Bob Jacobsen Copyright (C) 2008
 * @version $Revision$
 */
public class SwingShutDownTask extends AbstractShutDownTask {
    
    /** 
     * Constructor specifies the warning message
     * and action to take
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
     * @return true if the shutdown should continue, false
     * to abort.
     */
    public boolean execute() {
        while (!checkPromptNeeded()) {
            // issue prompt
            Object[] possibleValues;
            if (action!=null) possibleValues = new Object[] {Bundle.getMessage("ButtonContinue"), 
                                       Bundle.getMessage("ButtonAbort"), 
                                       action};
            else possibleValues = new Object[] {Bundle.getMessage("ButtonContinue"), 
                                       Bundle.getMessage("ButtonAbort")}; 

            int selectedValue = JOptionPane.showOptionDialog(component,
                                                             warning,
                                                             Bundle.getMessage("ShutDownWarningTitle"),
                                                             JOptionPane.DEFAULT_OPTION,
                                                             JOptionPane.WARNING_MESSAGE, null,
                                                             possibleValues, possibleValues[possibleValues.length-1]);
            if (selectedValue == 1) {
                // abort quit
                return false;
            } else if (selectedValue == 0) {
                // quit anyway
                return true;
            } else if (selectedValue == 2) {
                // take action and try again
                return doPrompt();
            } else if (selectedValue == -1) {
                // dialog window closed
                return doClose();
            } else {
                // unexpected value, log but continue
                log.error("unexpected selection: "+selectedValue);
                return true;
            }
        }
        // break out of loop when ready to continue       
        return true;
    }

    
    /**
     * Provide a subclass-specific check as to whether it's
     * OK to shutdown.  If not, issue a prompt before continuing.
     * Default implementation never passes, causing message to be emitted.
     * @return true if ready to shutdown, and no prompt needed. false to present dialog
     * before shutdown proceeds
     */
    protected boolean checkPromptNeeded() {
        return false;
    }
    
    /**
     * Provide a subclass-specific method to handle the
     * request to fix the problem. This is a dummy implementation,
     * intended to be overloaded.
     * @return true if ready to shutdown, false to end shutdown
     */
    protected boolean doPrompt() {
        return true;
    }
    
    /**
     * Provide a subclass-specific method to handle the case
     * where the user has chosen the close window option.
     * @return true if ready to shutdown, false to end shutdown
     */
    protected boolean doClose() {
    	return true;
    }
    
    static Logger log = LoggerFactory.getLogger(SwingShutDownTask.class.getName());

}

/* @(#)SwingShutDownTask.java */
