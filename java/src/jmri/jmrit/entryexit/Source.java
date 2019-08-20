package jmri.jmrit.entryexit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JMenuItem;
import jmri.NamedBean;
import jmri.SignalMast;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutEditor;

public class Source implements PropertyChangeListener {

    JMenuItem clear = null;
    JMenuItem cancel = null;
    JMenuItem editCancel = null;
    JMenuItem editClear = null;
    JMenuItem editOneClick = null;
    JMenuItem oneClick = null;

    NamedBean sourceObject = null;
    NamedBean sourceSignal = null;
    //String ref = "Empty";
    transient PointDetails pd = null;

    EntryExitPairs manager = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class);

    //Using Object here rather than sourceSensor, working on the basis that it might
    //one day be possible to have a signal icon selectable on a panel and
    //generate a propertychange, so hence do not want to tie it down at this stage.
    transient HashMap<PointDetails, DestinationPoints> pointToDest = new HashMap<PointDetails, DestinationPoints>();

    public boolean isEnabled(Object dest, LayoutEditor panel) {
        PointDetails lookingFor = manager.getPointDetails(dest, panel);
        if (pointToDest.containsKey(lookingFor)) {
            return pointToDest.get(lookingFor).isEnabled();
        }
        return true;
    }

    public void setEnabled(Object dest, LayoutEditor panel, boolean boo) {
        PointDetails lookingFor = manager.getPointDetails(dest, panel);
        if (pointToDest.containsKey(lookingFor)) {
            pointToDest.get(lookingFor).setEnabled(boo);
        }
    }

    public Source(PointDetails point/*, ArrayList<LayoutBlock> protectingBlock*/) {
        if (point.getSensor() != null) {
            addSourceObject(point.getSensor());
        } else {
            addSourceObject(point.getSignal());
        }
        //protectingBlocks = protectingBlock;
        point.setSource(this);
        sourceSignal = point.getSignal();
        pd = point;
    }

    /**
     * Property change support for table in AddEntryExitPairPanel.
     * Catch when paths go active.
     * @since 4.17.4 
     */
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * @since 4.17.4
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    
    /**
     * @since 4.17.4
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    /**
     * @since 4.17.4
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        pcs.firePropertyChange("active", evt.getOldValue(), evt.getNewValue());
    }
    

    void cancelClearInterlockFromSource(int cancelClear) {
        for (DestinationPoints dp : pointToDest.values()) {
            if (dp.isActive()) {
                dp.cancelClearInterlock(cancelClear);
            }
        }
    }

    void setMenuEnabled(boolean boo) {
        if (clear != null) {
            clear.setEnabled(boo);
        }
        if (cancel != null) {
            cancel.setEnabled(boo);
        }
        if (editClear != null) {
            editClear.setEnabled(boo);
        }
        if (editCancel != null) {
            editCancel.setEnabled(boo);
        }
        if (oneClick != null) {
            oneClick.setEnabled(!boo);
        }
        if (editOneClick != null) {
            editOneClick.setEnabled(!boo);
        }
    }

    /**
     * @since 4.17.4
     * Making the source object available for scripting in Jython.
     */
    public PointDetails getPoint() {
        return pd;
    }

    LayoutBlock getStart() {
        return pd.getFacing();
    }

    List<LayoutBlock> getSourceProtecting() {
        return pd.getProtecting();
    }

    NamedBean getSourceSignal() {
        if (sourceSignal == null) {
            pd.getSignal();
        }
        return sourceSignal;
    }

    /**
     * @since 4.17.4
     * Add Property Change Listener.
     */
    public void addDestination(PointDetails dest, String id) {
        if (pointToDest.containsKey(dest)) {
            return;
        }

        DestinationPoints dstPoint = new DestinationPoints(dest, id, this);
        dest.setDestination(dstPoint, this);
        pointToDest.put(dest, dstPoint);
        dstPoint.addPropertyChangeListener(this);
    }

    /**
     * @since 4.17.4
     * Remov Property Change Listener.
     */
    public void removeDestination(PointDetails dest) {
        pointToDest.get(dest).dispose();
        pointToDest.remove(dest);
        dest.removePropertyChangeListener(this);
        if (pointToDest.size() == 0) {
            getPoint().removeSource(this);
        }
    }

    void addSourceObject(NamedBean source) {
        if (sourceObject == source) {
            return;
        }
        sourceObject = source;
    }

    Object getSourceObject() {
        return sourceObject;
    }

    public ArrayList<PointDetails> getDestinationPoints() {
        //ArrayList<PointDetails> rtn =
        return new ArrayList<PointDetails>(pointToDest.keySet());
    }

    public boolean isDestinationValid(PointDetails destPoint) {
        return pointToDest.containsKey(destPoint);
    }

    public boolean getUniDirection(Object dest, LayoutEditor panel) {
        //Work on the principle that if the source is uniDirection, then the destination has to be.
        PointDetails lookingFor = manager.getPointDetails(dest, panel);
        if (pointToDest.containsKey(lookingFor)) {
            return pointToDest.get(lookingFor).getUniDirection();
        }
        return true;
    }

    public void setUniDirection(Object dest, LayoutEditor panel, boolean set) {

        PointDetails lookingFor = manager.getPointDetails(dest, panel);
        if (pointToDest.containsKey(lookingFor)) {
            pointToDest.get(lookingFor).setUniDirection(set);
        }
    }

    public boolean canBeBiDirection(Object dest, LayoutEditor panel) {
        if (getSourceSignal() == null) {
            return true;
        }
        //Work on the pinciple that if the source is uniDirection, then the destination has to be.
        PointDetails lookingFor = manager.getPointDetails(dest, panel);
        if (pointToDest.containsKey(lookingFor)) {
            return pointToDest.get(lookingFor).getSignal() == null;
        }
        return false;
    }

    public boolean isRouteActive(PointDetails endpoint) {
        if (pointToDest.containsKey(endpoint)) {
            return pointToDest.get(endpoint).activeEntryExit;
        }
        return false;
    }

    public void activeBean(DestinationPoints dest, boolean reverseDirection) {
        if (dest != null) {
            dest.activeBean(reverseDirection);
        }
    }

    public DestinationPoints getDestForPoint(PointDetails dp) {
        return pointToDest.get(dp);
    }

    public int getNumberOfDestinations() {
        return pointToDest.size();
    }

    public void setEntryExitType(Object dest, LayoutEditor panel, int type) {
        PointDetails lookingFor = manager.getPointDetails(dest, panel);
        if (pointToDest.containsKey(lookingFor)) {
            pointToDest.get(lookingFor).setEntryExitType(type);
        }
        if (type == EntryExitPairs.FULLINTERLOCK) {
            if (sourceSignal instanceof SignalMast) {
                ((SignalMast) sourceSignal).setHeld(true);
            }
        }
    }

    public int getEntryExitType(Object dest, LayoutEditor panel) {
        PointDetails lookingFor = manager.getPointDetails(dest, panel);
        if (pointToDest.containsKey(lookingFor)) {
            return pointToDest.get(lookingFor).getEntryExitType();
        }

        return 0x00;
    }

    public void cancelInterlock(Object dest, LayoutEditor panel) {
        PointDetails lookingFor = manager.getPointDetails(dest, panel);
        if (pointToDest.containsKey(lookingFor)) {
            pointToDest.get(lookingFor).cancelClearInterlock(EntryExitPairs.CANCELROUTE);
        }
    }

    public String getUniqueId(Object dest, LayoutEditor panel) {
        PointDetails lookingFor = manager.getPointDetails(dest, panel);
        if (pointToDest.containsKey(lookingFor)) {
            return pointToDest.get(lookingFor).getUniqueId();
        }
        return null;
    }

    public ArrayList<String> getDestinationUniqueId() {
        ArrayList<String> rList = new ArrayList<String>();
        for (DestinationPoints d : pointToDest.values()) {
            rList.add(d.getUniqueId());
        }
        return rList;
    }

    public DestinationPoints getByUniqueId(String id) {
        for (DestinationPoints d : pointToDest.values()) {
            if (d.getUniqueId().equals(id)) {
                return d;
            }
        }
        return null;
    }

    public DestinationPoints getByUserName(String id) {
        for (DestinationPoints d : pointToDest.values()) {
            String uname = d.getUserName();
            if (uname != null && uname.equals(id)) {
                return d;
            }
        }
        return null;
    }

}
