package jmri.jmrit.throttle;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.table.AbstractTableModel;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.Throttle;

public class ThrottlesTableModel extends AbstractTableModel implements AddressListener, java.beans.PropertyChangeListener {

    private ArrayList<ThrottleFrame> throttleFrames = new ArrayList<ThrottleFrame>(5);

    @Override
    public int getRowCount() {
        return throttleFrames.size();
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public Object getValueAt(int i, int i1) {
        return throttleFrames.get(i);
    }

    public Iterator<ThrottleFrame> iterator() {
        return throttleFrames.iterator();
    }

    public void addThrottleFrame(ThrottleFrame tf) {
        throttleFrames.add(tf);
        fireTableDataChanged();
    }

    public void removeThrottleFrame(ThrottleFrame tf, DccLocoAddress la) {
        throttleFrames.remove(tf);
        if (la != null) {
            jmri.InstanceManager.throttleManagerInstance().removeListener(la, this);
        }
        fireTableDataChanged();
    }

    @Override
    public void notifyAddressChosen(LocoAddress la) {
    }

    @Override
    public void notifyAddressReleased(LocoAddress addr) {
        if(addr instanceof DccLocoAddress ) {
           DccLocoAddress la = (DccLocoAddress) addr;
           fireTableDataChanged();
           jmri.InstanceManager.throttleManagerInstance().removeListener(la, this);
        }
    }

    @Override
    public void notifyAddressThrottleFound(DccThrottle throttle) {
        fireTableDataChanged();
        throttle.addPropertyChangeListener(this);
    }

    @Override
    public void notifyConsistAddressChosen(int newAddress, boolean isLong) {
    }

    @Override
    public void notifyConsistAddressReleased(int address, boolean isLong) {
    }

    @Override
    public void notifyConsistAddressThrottleFound(DccThrottle throttle) {
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if ((e.getPropertyName().equals(Throttle.SPEEDSETTING)) ||
                (e.getPropertyName().equals(Throttle.SPEEDSTEPS)) ||
                (e.getPropertyName().equals(Throttle.ISFORWARD))) {
            fireTableDataChanged();
        }
    }
}
