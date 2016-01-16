package jmri.jmrit.operations.automation.actions;

import javax.swing.JOptionPane;
import jmri.jmrit.operations.automation.Automation;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.Train;

public abstract class Action {

    public static final String ACTION_COMPLETE_CHANGED_PROPERTY = "actionComplete"; // NOI18N

    public static final int OKAY = 0;
    public static final int HALT = 1;
    public static final int CLOSED = JOptionPane.CLOSED_OPTION; // -1
    public static final int NO_MESSAGE_SENT = -2;

    protected AutomationItem _automationItem = null;

    abstract public int getCode();

    abstract public String toString(); // for combo boxes

    abstract public void doAction();

    abstract public void cancelAction();

    /**
     * Mask off menu bits.
     * 
     * @param code
     * @return code & ActionCodes.CODE_MASK
     */
    protected int getCode(int code) {
        return code & ActionCodes.CODE_MASK;
    }

    public boolean isTrainMenuEnabled() {
        return (getCode() & ActionCodes.ENABLE_TRAINS) == ActionCodes.ENABLE_TRAINS;
    }

    public boolean isRouteMenuEnabled() {
        return (getCode() & ActionCodes.ENABLE_ROUTES) == ActionCodes.ENABLE_ROUTES;
    }

    public boolean isMessagesEnabled() {
        return (getCode() & ActionCodes.ENABLE_MESSAGES) == ActionCodes.ENABLE_MESSAGES;
    }

    public boolean isAutomationMenuEnabled() {
        return (getCode() & ActionCodes.ENABLE_AUTOMATION_LIST) == ActionCodes.ENABLE_AUTOMATION_LIST;
    }

    public boolean isGotoMenuEnabled() {
        return (getCode() & ActionCodes.ENABLE_GOTO_LIST) == ActionCodes.ENABLE_GOTO_LIST;
    }

    public void setAutomationItem(AutomationItem item) {
        _automationItem = item;
    }

    public AutomationItem getAutomationItem() {
        return _automationItem;
    }

    /**
     * Displays message if there's one. Gives the user the option to halt the
     * automation.
     * 
     * @return OKAY, HALT, NO_MESSAGE_SENT, CLOSED
     */
    public int finishAction() {
        int response = NO_MESSAGE_SENT;
        if (getAutomationItem() != null) {
            if (!getAutomationItem().getMessage().equals(AutomationItem.NONE)) {
                String trainName = "";
                Train train = getAutomationItem().getTrain();
                if (train != null) {
                    trainName = " " + train.getName();
                }
                String routeLocationName = "";
                RouteLocation rl = getAutomationItem().getRouteLocation();
                if (rl != null) {
                    routeLocationName = " " + rl.getName();
                }
                String automationName = "";
                Automation automation = getAutomationItem().getAutomation();
                if (automation != null) {
                    automationName = " " + automation.getName();
                }
                String title = getAutomationItem().getId() +
                        " " +
                        toString() +
                        trainName +
                        routeLocationName +
                        automationName;
                response = JOptionPane.showOptionDialog(null, getAutomationItem().getMessage(), title,
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new Object[]
                        {Bundle.getMessage("OK"), Bundle.getMessage("HALT")}, null);
            }
            if (response != HALT) {
                firePropertyChange(ACTION_COMPLETE_CHANGED_PROPERTY, false, true);
            }
        }
        return response;
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

}
