package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.automation.AutomationItem;


public abstract class Action {
    
    public static final String ACTION_COMPLETE_CHANGED_PROPERTY = "actionComplete"; // NOI18N
    
    protected AutomationItem _automationItem = null;
    
    abstract public int getCode();
    abstract public String toString(); // for combo boxes
    abstract public void doAction();
    abstract public void cancelAction();
    
    /**
     * Mask off menu bits.
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
    
    public void setAutomationItem (AutomationItem item) {
        _automationItem = item;
    }
    
    public AutomationItem getAutomationItem() {
        return _automationItem;
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
