package jmri.jmrit.signalling;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.SignalMast;
import jmri.SignalMastLogicManager;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriJOptionPane;

/**
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class SignallingGuiTools {

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
    public static void updateSignalMastLogic(@CheckForNull JmriJFrame frame,
            @Nonnull SignalMast oldMast, @Nonnull SignalMast newMast) {
        Object[] options = {Bundle.getMessage("ButtonUpdate"), Bundle.getMessage("LeaveButton")};
        int n = JmriJOptionPane.showOptionDialog(frame,
                java.text.MessageFormat.format(Bundle.getMessage("UpdateLogic"),  // NOI18N
                        new Object[]{oldMast.getDisplayName(), newMast.getDisplayName()}),
                Bundle.getMessage("UpdateLogicTitle"),
                JmriJOptionPane.DEFAULT_OPTION,
                JmriJOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (n == 0) { // array position 0, ButtonUpdate 
            InstanceManager.getDefault(SignalMastLogicManager.class).replaceSignalMast(oldMast, newMast);
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
    public static void swapSignalMastLogic(@CheckForNull JmriJFrame frame,
            @Nonnull SignalMast oldMast, @Nonnull SignalMast newMast) {
        Object[] options = {Bundle.getMessage("ButtonUpdate"), Bundle.getMessage("LeaveButton")};
        int n = JmriJOptionPane.showOptionDialog(frame,
                java.text.MessageFormat.format(Bundle.getMessage("SwapLogic"),  // NOI18N
                        new Object[]{oldMast.getDisplayName(), newMast.getDisplayName()}),
                Bundle.getMessage("UpdateLogicTitle"),  // NOI18N
                JmriJOptionPane.DEFAULT_OPTION,
                JmriJOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (n == 0) { // array position 0, ButtonUpdate
            InstanceManager.getDefault(SignalMastLogicManager.class).swapSignalMasts(oldMast, newMast);
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
    public static boolean removeSignalMastLogic(@CheckForNull JmriJFrame frame, @Nonnull SignalMast mast) {
        Object[] options = {Bundle.getMessage("RemoveButton"), Bundle.getMessage("LeaveButton")};
        int n = JmriJOptionPane.showOptionDialog(frame,
                java.text.MessageFormat.format(Bundle.getMessage("RemoveLogic"),  // NOI18N
                        new Object[]{mast.getDisplayName()}),
                Bundle.getMessage("RemoveLogicTitle"),  // NOI18N
                JmriJOptionPane.DEFAULT_OPTION,
                JmriJOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (n == 0) { // array position 0, RemoveButton
            InstanceManager.getDefault(SignalMastLogicManager.class).removeSignalMast(mast);
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
    public static void removeAlreadyAssignedSignalMastLogic(@CheckForNull JmriJFrame frame, @Nonnull SignalMast mast) {
        Object[] options = {Bundle.getMessage("RemoveButton"),  // NOI18N
            Bundle.getMessage("LeaveButton")};  // NOI18N
        int n = JmriJOptionPane.showOptionDialog(frame,
                java.text.MessageFormat.format(Bundle.getMessage("RemoveAlreadyAssignedLogic"),  // NOI18N
                        new Object[]{mast.getDisplayName()}),
                Bundle.getMessage("RemoveLogicTitle"),  // NOI18N
                JmriJOptionPane.DEFAULT_OPTION,
                JmriJOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (n == 0) { // array position 0, RemoveButton
            InstanceManager.getDefault(SignalMastLogicManager.class).removeSignalMast(mast);
        }
    }
}
