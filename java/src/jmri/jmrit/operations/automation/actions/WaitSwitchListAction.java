package jmri.jmrit.operations.automation.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitSwitchListAction extends Action implements PropertyChangeListener {

    private static final int _code = ActionCodes.WAIT_SWITCHLIST;

    @Override
    public int getCode() {
        return _code;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("WaitForSwitchListChange");
    }
    
    @Override
    public boolean isConcurrentAction() {
        return true;
    }

    @Override
    public void doAction() {
        if (getAutomationItem() != null) {
            setRunning(true);
            addPropertyChangeLocations();
        }
    }
    
    /*
     * Waiting for any location's switch list to change
     */
    private void checkForlocationChange() {
        for (Location location : InstanceManager.getDefault(LocationManager.class).getList()) {
            if (location != null && location.isSwitchListEnabled() && location.getStatus().equals(Location.MODIFIED)) {
                removePropertyChangeLocations();
                finishAction(true);
                break;
            }
        }
    }
    
    private synchronized void addPropertyChangeLocations() {
        for (Location location : InstanceManager.getDefault(LocationManager.class).getList()) {
            location.addPropertyChangeListener(this);
        }
    }

    private synchronized void removePropertyChangeLocations() {
        for (Location location : InstanceManager.getDefault(LocationManager.class).getList()) {
            location.removePropertyChangeListener(this);
        }
    }

    @Override
    public void cancelAction() {
        if (getAutomationItem() != null) {
            setRunning(false);
            removePropertyChangeLocations();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Control.SHOW_PROPERTY)
            log.debug("Property change: ({}) old: ({}) new: ({})", evt.getPropertyName(), evt.getOldValue(), evt
                    .getNewValue());
        if (evt.getPropertyName().equals(Location.STATUS_CHANGED_PROPERTY) && evt
                .getNewValue().equals(Location.MODIFIED)) {
            checkForlocationChange();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(WaitTrainAction.class);

}
