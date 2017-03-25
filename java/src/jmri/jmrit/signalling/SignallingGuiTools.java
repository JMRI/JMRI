package jmri.jmrit.signalling;

import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.SignalMast;
import jmri.util.JmriJFrame;

/**
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class SignallingGuiTools {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.signalling.SignallingGuiTools");

    private SignallingGuiTools() {
    }

    /**
     * Display a message to the user asking them to
     * confirm they wish to update the Signal Mast Logic from the old signal
     * mast to the new one.
     *
     * @param frame the frame initiating the dialog
     * @param oldMast original signal mast (object) for this SML
     * @param newMast new main signal mast (object) to attach to SML
     */
    static public void updateSignalMastLogic(JmriJFrame frame, SignalMast oldMast, SignalMast newMast) {
        Object[] options = {Bundle.getMessage("ButtonUpdate"),
            rb.getString("LeaveButton")};
        int n = JOptionPane.showOptionDialog(frame,
                java.text.MessageFormat.format(rb.getString("UpdateLogic"),
                        new Object[]{oldMast.getDisplayName(), newMast.getDisplayName()}),
                "Update Logic",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (n == 0) {
            InstanceManager.getDefault(jmri.SignalMastLogicManager.class).replaceSignalMast(oldMast, newMast);
        }
    }

    /**
     * Display a message to the user asking them to confirm they wish to update
     * the Signal Mast Logic by swapping two signal masts.
     *
     * @param frame the frame initiating the dialog
     * @param oldMast signal mast (object) #1
     * @param newMast signal mast (object) #2
     */
    static public void swapSignalMastLogic(JmriJFrame frame, SignalMast oldMast, SignalMast newMast) {
        Object[] options = {Bundle.getMessage("ButtonUpdate"),
            rb.getString("LeaveButton")};
        int n = JOptionPane.showOptionDialog(frame,
                java.text.MessageFormat.format(rb.getString("SwapLogic"),
                        new Object[]{oldMast.getDisplayName(), newMast.getDisplayName()}),
                rb.getString("UpdateLogicTitle"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (n == 0) {
            InstanceManager.getDefault(jmri.SignalMastLogicManager.class).swapSignalMasts(oldMast, newMast);
        }
    }

    /**
     * Display a message to the user asking them to
     * confirm they wish to remove the Signal Mast Logic for a given signal.
     *
     * @param frame the frame initiating the dialog
     * @param mast the main signal mast (object) selected on that frame
     * @return true if user confirmed delete request
     */
    static public boolean removeSignalMastLogic(JmriJFrame frame, SignalMast mast) {
        Object[] options = {rb.getString("RemoveButton"),
            rb.getString("LeaveButton")};
        int n = JOptionPane.showOptionDialog(frame,
                java.text.MessageFormat.format(rb.getString("RemoveLogic"),
                        new Object[]{mast.getDisplayName()}),
                rb.getString("RemoveLogicTitle"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (n == 0) {
            InstanceManager.getDefault(jmri.SignalMastLogicManager.class).removeSignalMast(mast);
            return true;
        }
        return false;
    }

    /**
     * Display a message to the user asking them to
     * confirm they wish to remove the Signal Mast Logic for a given Signal Mast.
     * <p>
     * This is the same as removeSignalMastLogic, but with different text.
     *
     * @param frame the frame initiating the dialog
     * @param mast the main signal mast (object) selected on that frame
     */
    static public void removeAlreadyAssignedSignalMastLogic(JmriJFrame frame, SignalMast mast) {
        Object[] options = {rb.getString("RemoveButton"),
            rb.getString("LeaveButton")};
        int n = JOptionPane.showOptionDialog(frame,
                java.text.MessageFormat.format(rb.getString("RemoveAlreadyLogic"),
                        new Object[]{mast.getDisplayName()}),
                rb.getString("RemoveLogicTitle"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (n == 0) {
            InstanceManager.getDefault(jmri.SignalMastLogicManager.class).removeSignalMast(mast);
        }
    }
}
