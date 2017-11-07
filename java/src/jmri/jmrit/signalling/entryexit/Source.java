package jmri.jmrit.signalling.entryexit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import jmri.NamedBean;
import jmri.SignalMast;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.signalling.EntryExitPairs;

public class Source {

    JMenu entryExitPopUp = null;
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

    EntryExitPairs manager = jmri.InstanceManager.getDefault(jmri.jmrit.signalling.EntryExitPairs.class);

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
        createPopUpMenu();
    }

    //ArrayList<LayoutBlock> protectingBlocks;
    void createPopUpMenu() {
        if (entryExitPopUp != null) {
            return;
        }
        entryExitPopUp = new JMenu(Bundle.getMessage("MenuEntryExit"));  // NOI18N
        editClear = new JMenuItem(Bundle.getMessage("MenuItemClearRoute"));  // NOI18N
        editClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelClearInterlockFromSource(EntryExitPairs.CLEARROUTE);
            }
        });
        entryExitPopUp.add(editClear);
        editCancel = new JMenuItem(Bundle.getMessage("MenuItemCancelRoute"));  // NOI18N
        editCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelClearInterlockFromSource(EntryExitPairs.CANCELROUTE);
            }
        });
        entryExitPopUp.add(editCancel);

        editOneClick = new JMenuItem(Bundle.getMessage("MenuItemLockManualRoute"));  // NOI18N
        editOneClick.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ManuallySetRoute(pd);
            }
        });
        entryExitPopUp.add(editOneClick);

        clear = new JMenuItem(Bundle.getMessage("MenuItemClearRoute"));  // NOI18N
        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelClearInterlockFromSource(EntryExitPairs.CLEARROUTE);
            }
        });

        cancel = new JMenuItem(Bundle.getMessage("MenuItemCancelRoute"));  // NOI18N
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelClearInterlockFromSource(EntryExitPairs.CANCELROUTE);
            }
        });

        oneClick = new JMenuItem(Bundle.getMessage("MenuItemLockManualRoute"));  // NOI18N
        oneClick.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ManuallySetRoute(pd);
            }
        });

        pd.getPanel().addToPopUpMenu(pd.getSensor(), entryExitPopUp, jmri.jmrit.display.Editor.EDITPOPUPONLY);
        pd.getPanel().addToPopUpMenu(pd.getSensor(), clear, jmri.jmrit.display.Editor.VIEWPOPUPONLY);
        pd.getPanel().addToPopUpMenu(pd.getSensor(), cancel, jmri.jmrit.display.Editor.VIEWPOPUPONLY);
        pd.getPanel().addToPopUpMenu(pd.getSensor(), oneClick, jmri.jmrit.display.Editor.VIEWPOPUPONLY);
        setMenuEnabled(false);
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

    PointDetails getPoint() {
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

    public void addDestination(PointDetails dest, String id) {
        if (pointToDest.containsKey(dest)) {
            return;
        }

        DestinationPoints dstPoint = new DestinationPoints(dest, id, this);
        dest.setDestination(dstPoint, this);
        pointToDest.put(dest, dstPoint);
    }

    public void removeDestination(PointDetails dest) {
        pointToDest.get(dest).dispose();
        pointToDest.remove(dest);
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
