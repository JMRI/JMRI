package jmri.jmrit.signalling.entryexit;

import jmri.NamedBean;
import java.util.HashMap;
import java.util.ArrayList;
import jmri.SignalMast;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import jmri.jmrit.signalling.EntryExitPairs;

import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutBlock;

public class Source {

    JMenu entryExitPopUp = null;
    JMenuItem clear = null;
    JMenuItem cancel = null;

    NamedBean sourceObject = null;
    NamedBean sourceSignal = null;
    //String ref = "Empty";
    transient PointDetails pd = null;
    
    EntryExitPairs manager = jmri.InstanceManager.getDefault(jmri.jmrit.signalling.EntryExitPairs.class);
    
    //Using Object here rather than sourceSensor, working on the basis that it might
    //one day be possible to have a signal icon selectable on a panel and 
    //generate a propertychange, so hence do not want to tie it down at this stage.
    transient HashMap<PointDetails, DestinationPoints> pointToDest = new HashMap<PointDetails, DestinationPoints>();
    
    
    public boolean isEnabled(Object dest,LayoutEditor panel){
        PointDetails lookingFor = manager.getPointDetails(dest, panel);
        if(pointToDest.containsKey(lookingFor)){
            return pointToDest.get(lookingFor).isEnabled();
        }
        return true;
    }
    
    public void setEnabled(Object dest, LayoutEditor panel, boolean boo){
        PointDetails lookingFor = manager.getPointDetails(dest, panel);
        if(pointToDest.containsKey(lookingFor)){
            pointToDest.get(lookingFor).setEnabled(boo);
        }
    }
    
    public Source(PointDetails point){
        if(point.getSensor()!=null){
            addSourceObject(point.getSensor());
        } else {
            addSourceObject(point.getSignal());
        }
        point.setSource(this);
        sourceSignal = point.getSignal();
        pd = point;
        createPopUpMenu();
    }
    
    void createPopUpMenu(){
        if(entryExitPopUp!=null)
            return;
        entryExitPopUp = new JMenu("Entry Exit");
        JMenuItem editClear = new JMenuItem("Clear Route");
        editClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelClearInterlockFromSource(EntryExitPairs.CLEARROUTE);
            }
        });
        entryExitPopUp.add(editClear);
        JMenuItem editCancel = new JMenuItem("Cancel Route");
        editCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                cancelClearInterlockFromSource(EntryExitPairs.CANCELROUTE);
            }
        });
        entryExitPopUp.add(editCancel);
        
        clear = new JMenuItem("Clear Route");
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelClearInterlockFromSource(EntryExitPairs.CLEARROUTE);
            }
        });

        cancel = new JMenuItem("Cancel Route");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                cancelClearInterlockFromSource(EntryExitPairs.CANCELROUTE);
            }
        });
        
        pd.getPanel().addToPopUpMenu(pd.getSensor(), entryExitPopUp, jmri.jmrit.display.Editor.EDITPOPUPONLY);
        pd.getPanel().addToPopUpMenu(pd.getSensor(), clear, jmri.jmrit.display.Editor.VIEWPOPUPONLY);
        pd.getPanel().addToPopUpMenu(pd.getSensor(), cancel, jmri.jmrit.display.Editor.VIEWPOPUPONLY);
        setMenuEnabled(false);
    }
    
    void cancelClearInterlockFromSource(int cancelClear){
        for(DestinationPoints dp:pointToDest.values()){
            if(dp.isActive()){
                dp.cancelClearInterlock(cancelClear);
            }
        }
    }
    
    void setMenuEnabled(boolean boo){
        if (entryExitPopUp!=null)
            entryExitPopUp.setEnabled(boo);
        if (clear!=null)
            clear.setEnabled(boo);
        if (cancel!=null)
            cancel.setEnabled(boo);
        
    
    }
    
    PointDetails getPoint(){
        return pd;
    }
    
    LayoutBlock getStart(){
        return pd.getFacing();
    }
    
    LayoutBlock getSourceProtecting(){
        return pd.getProtecting();
    }
    
    NamedBean getSourceSignal(){
        if(sourceSignal==null){
            pd.getSignal();
        }
        return sourceSignal;
    }
    
    public void addDestination(PointDetails dest, String id){
        if(pointToDest.containsKey(dest)){
            return;
        }
        
        DestinationPoints dstPoint = new DestinationPoints(dest, id, this);
        dest.setDestination(dstPoint, this);
        pointToDest.put(dest, dstPoint);
    }
    
    public void removeDestination(PointDetails dest){
        pointToDest.get(dest).dispose();
        pointToDest.remove(dest);
        if(pointToDest.size()==0){
            getPoint().removeSource(this);
        }
    }

    void addSourceObject(NamedBean source){
        if (sourceObject==source)
            return;
        sourceObject = source;
    }
    
    Object getSourceObject() { return sourceObject; }
    
    public ArrayList<PointDetails> getDestinationPoints() {
        //ArrayList<PointDetails> rtn = 
        return new ArrayList<PointDetails>(pointToDest.keySet());
    }
    
    public boolean isDestinationValid(PointDetails destPoint){
        return pointToDest.containsKey(destPoint);
    }
    
    public boolean getUniDirection(Object dest, LayoutEditor panel){
        //Work on the principle that if the source is uniDirection, then the destination has to be.
        PointDetails lookingFor = manager.getPointDetails(dest, panel);
        if(pointToDest.containsKey(lookingFor)){
            return pointToDest.get(lookingFor).getUniDirection();
        }
        return true;
    }
    
    public void setUniDirection(Object dest, LayoutEditor panel, boolean set){
        
        PointDetails lookingFor = manager.getPointDetails(dest, panel);
        if(pointToDest.containsKey(lookingFor)){
            pointToDest.get(lookingFor).setUniDirection(set);
        }
    }
    
    public boolean canBeBiDirection(Object dest, LayoutEditor panel){
        if(getSourceSignal()==null){
            return true;
        }
        //Work on the pinciple that if the source is uniDirection, then the destination has to be.
        PointDetails lookingFor = manager.getPointDetails(dest, panel);
        if(pointToDest.containsKey(lookingFor)){
            return pointToDest.get(lookingFor).getSignal()==null;
        }
        return false;
    }
    
    public boolean isRouteActive(PointDetails endpoint){
        if(pointToDest.containsKey(endpoint)){
            return pointToDest.get(endpoint).activeEntryExit;
        }
        return false;
    }
    
    void activeBean(DestinationPoints dest, boolean reverseDirection){
        dest.activeBean(reverseDirection);
    }

    public int getNumberOfDestinations() { return pointToDest.size(); }
    
    public void setEntryExitType(Object dest, LayoutEditor panel, int type){
        PointDetails lookingFor = manager.getPointDetails(dest, panel);
        if(pointToDest.containsKey(lookingFor)){
            pointToDest.get(lookingFor).setEntryExitType(type);
        }
        if(type==EntryExitPairs.FULLINTERLOCK){
            if (sourceSignal instanceof SignalMast){
                ((SignalMast) sourceSignal).setHeld(true);
            }
        }
    }
    
    public int getEntryExitType(Object dest, LayoutEditor panel){
        PointDetails lookingFor = manager.getPointDetails(dest, panel);
        if(pointToDest.containsKey(lookingFor)){
            return pointToDest.get(lookingFor).getEntryExitType();
        }
        
        return 0x00;
    }
    
    public void cancelInterlock(Object dest, LayoutEditor panel){
        PointDetails lookingFor = manager.getPointDetails(dest, panel);
        if(pointToDest.containsKey(lookingFor)){
            pointToDest.get(lookingFor).cancelClearInterlock(EntryExitPairs.CANCELROUTE);
        }
    }
    
    public String getUniqueId(Object dest, LayoutEditor panel){
        PointDetails lookingFor = manager.getPointDetails(dest, panel);
        if(pointToDest.containsKey(lookingFor)){
            return pointToDest.get(lookingFor).getUniqueId();
        }
        return null;
    }
    
    public ArrayList<String> getDestinationUniqueId(){
        ArrayList<String> rList = new ArrayList<String>();
        for(DestinationPoints d: pointToDest.values()){
            rList.add(d.getUniqueId());
        }
        return rList;
    }
    
    public DestinationPoints getByUniqueId(String id){
        for(DestinationPoints d: pointToDest.values()){
            if(d.getUniqueId().equals(id))
                return d;
        }
        return null;
    }
    
    public DestinationPoints getByUserName(String id){
        for(DestinationPoints d: pointToDest.values()){
            if(d.getUserName().equals(id))
                return d;
        }
        return null;
    }
}