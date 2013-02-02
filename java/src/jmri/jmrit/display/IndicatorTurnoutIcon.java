package jmri.jmrit.display;

import org.apache.log4j.Logger;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.Turnout;

import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.display.palette.IndicatorTOItemPanel;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.logix.OBlock;
import jmri.NamedBeanHandle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
//import javax.swing.JPopupMenu;

/**
 * An icon to display a status and state of a color coded turnout.<P>
 * This responds to only KnownState, leaving CommandedState to some other
 * graphic representation later. 
 * <p>"state" is the state of the underlying turnout ("closed", "thrown", etc.)
 * <p>"status" is the operating condition of the track ("clear", "occupied", etc.)
 * <P>
 * A click on the icon will command a state change. Specifically, it
 * will set the CommandedState to the opposite (THROWN vs CLOSED) of
 * the current KnownState. This will display the setting of the turnout points.
 *<P>
 * The status is indicated by color and changes are done only done by the occupancy
 * sensing - OBlock or other sensor.
 * <p>
 * The default icons are for a left-handed turnout, facing point
 * for east-bound traffic.
 * @author Bob Jacobsen  Copyright (c) 2002
 * @author Pete Cressman  Copyright (c) 2010 2012
 * @version $Revision$
 */

public class IndicatorTurnoutIcon extends TurnoutIcon implements IndicatorTrack {

    Hashtable<String, Hashtable<Integer, NamedIcon>> _iconMaps;

    private NamedBeanHandle<Sensor> namedOccSensor = null;
    private NamedBeanHandle<OBlock> namedOccBlock = null;

    private IndicatorTrackPaths _pathUtil;
    private IndicatorTOItemPanel _TOPanel;
    private String _status;

    public IndicatorTurnoutIcon(Editor editor) {
        super(editor);
        log.debug("IndicatorTurnoutIcon ctor: isIcon()= "+isIcon()+", isText()= "+isText());
        _pathUtil = new IndicatorTrackPaths();
        _status = "DontUseTrack";
        _iconMaps = initMaps();

    }

    Hashtable<String, Hashtable<Integer, NamedIcon>> initMaps() {
        Hashtable<String, Hashtable<Integer, NamedIcon>> iconMaps = new Hashtable<String, Hashtable<Integer, NamedIcon>>();
        iconMaps.put("ClearTrack", new Hashtable <Integer, NamedIcon>());
        iconMaps.put("OccupiedTrack", new Hashtable <Integer, NamedIcon>());
        iconMaps.put("PositionTrack", new Hashtable <Integer, NamedIcon>());
        iconMaps.put("AllocatedTrack", new Hashtable <Integer, NamedIcon>());
        iconMaps.put("DontUseTrack", new Hashtable <Integer, NamedIcon>());
        iconMaps.put("ErrorTrack", new Hashtable <Integer, NamedIcon>());
        return iconMaps;
    }

    Hashtable<String, Hashtable<Integer, NamedIcon>> cloneMaps(IndicatorTurnoutIcon pos) {
        Hashtable<String, Hashtable<Integer, NamedIcon>> iconMaps = initMaps();
        Iterator<Entry<String, Hashtable<Integer, NamedIcon>>> it = _iconMaps.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Hashtable<Integer, NamedIcon>> entry = it.next();
            Hashtable <Integer, NamedIcon> clone = iconMaps.get(entry.getKey());
            Iterator<Entry<Integer, NamedIcon>> iter = entry.getValue().entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Integer, NamedIcon> ent = iter.next();
//                if (log.isDebugEnabled()) log.debug("key= "+ent.getKey());
                clone.put(ent.getKey(), cloneIcon(ent.getValue(), pos));
            }
        }
        return iconMaps;
    }

    public Positionable deepClone() {
        IndicatorTurnoutIcon pos = new IndicatorTurnoutIcon(_editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        IndicatorTurnoutIcon pos = (IndicatorTurnoutIcon)p;
        pos.setOccBlockHandle(namedOccBlock);
        pos.setOccSensorHandle(namedOccSensor);
        pos._iconMaps = cloneMaps(pos);
        pos._pathUtil = _pathUtil.deepClone();
        pos._iconFamily = _iconFamily;
        return super.finishClone(pos);
    }
    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP", 
            justification="OK until Java 1.6 allows more efficient return of copy") 
    public Hashtable<String, Hashtable<Integer, NamedIcon>> getIconMaps() {
        return _iconMaps;
    }

    /**
     * Attached a named sensor to display status from OBlocks
     * @param pName Used as a system/user name to lookup the sensor object
     */
     public void setOccSensor(String pName) {
         if (pName==null || pName.trim().length()==0) {
             setOccSensorHandle(null);
             return;
         }
         if (InstanceManager.sensorManagerInstance()!=null) {
             Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
             if (sensor != null) {
                 setOccSensorHandle(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, sensor));                
             } else {
                 log.error("Occupancy Sensor '"+pName+"' not available, icon won't see changes");
             }
         } else {
             log.error("No SensorManager for this protocol, block icons won't see changes");
         }
     }

    public void setOccSensorHandle(NamedBeanHandle<Sensor> sen) {
        if (namedOccSensor != null) {
            getOccSensor().removePropertyChangeListener(this);
        }
        namedOccSensor = sen;
        if (namedOccSensor != null) {
            Sensor sensor = getOccSensor();
            sensor.addPropertyChangeListener(this, namedOccSensor.getName(), "Indicator Turnout Icon");
            _status = _pathUtil.setStatus(sensor.getKnownState());
            if (_iconMaps!=null) {
                displayState(turnoutState());
            }
        } 
    }

    public Sensor getOccSensor() {
        if (namedOccSensor==null) {
            return null;
        }
        return namedOccSensor.getBean(); 
    }    
    public NamedBeanHandle <Sensor> getNamedOccSensor() { return namedOccSensor; }

    /**
     * Attached a named OBlock to display status
     * @param pName Used as a system/user name to lookup the OBlock object
     */
     public void setOccBlock(String pName) {
         if (pName==null || pName.trim().length()==0) {
             setOccBlockHandle(null);
             return;
         }
         OBlock block = InstanceManager.oBlockManagerInstance().getOBlock(pName);
         if (block != null) {
             setOccBlockHandle(new NamedBeanHandle<OBlock>(pName, block));                
         } else {
             log.error("Detection OBlock '"+pName+"' not available, icon won't see changes");
         }
     }   
    public void setOccBlockHandle(NamedBeanHandle<OBlock> blockHandle) {
        if (namedOccBlock != null) {
            getOccBlock().removePropertyChangeListener(this);
        }
        namedOccBlock = blockHandle;
        if (namedOccBlock != null) {
            OBlock block = getOccBlock();
            block.addPropertyChangeListener(this, namedOccBlock.getName(), "Indicator Turnout Icon");
            setStatus(block, block.getState());
            if (_iconMaps!=null) {
                displayState(turnoutState());
            }
            setTooltip(new ToolTip(block.getDescription(), 0, 0));
        } 
    }
    public OBlock getOccBlock() { 
        if (namedOccBlock==null) {
            return null;
        }
        return namedOccBlock.getBean(); 
    }    
    public NamedBeanHandle <OBlock> getNamedOccBlock() { return namedOccBlock; }

    public void setShowTrain(boolean set) {
    	_pathUtil.setShowTrain(set);
    }
    public boolean showTrain() {
        return _pathUtil.showTrain();
    }
    public ArrayList<String> getPaths() {
    	return _pathUtil.getPaths();
    }
    public void setPaths(ArrayList<String>paths) {
    	_pathUtil.setPaths(paths);
    }
    public void addPath(String path) {
    	_pathUtil.addPath(path);
    }
    public void removePath(String path) {
    	_pathUtil.removePath(path);
    }
    public void setStatus(int state) {
    	_pathUtil.setStatus(state);
    }

    /**
    * Place icon by its localized bean state name
    * @param status - the track condition of the icon
    * @param stateName - NamedBean name of turnout state
    * @param icon - icon corresponding to status and state
    */
    public void setIcon(String status, String stateName, NamedIcon icon) {
        if (log.isDebugEnabled()) log.debug("setIcon for status \""+status+"\", stateName= \""
                                +stateName+" icom= "+icon.getURL());
//                                            ") state= "+_name2stateMap.get(stateName)+
//                                            " icon: w= "+icon.getIconWidth()+" h= "+icon.getIconHeight());
        if (_iconMaps==null) {
            initMaps();
        }
        _iconMaps.get(status).put(_name2stateMap.get(stateName), icon);
        setIcon(_iconMaps.get("ClearTrack").get(_name2stateMap.get("BeanStateInconsistent")));
    }

    /**
    * Get clear icon by its localized bean state name
    */
    public NamedIcon getIcon(String status, int state) {
        log.debug("getIcon: status= "+status+", state= "+state);
        Hashtable<Integer, NamedIcon> map = _iconMaps.get(status);
        if (map==null) { return null; }
        return map.get(Integer.valueOf(state));
    }

    public String getStateName(Integer state) {
        return _state2nameMap.get(state);
    }

    public String getStatus() {
        return _status;
    }

    public int maxHeight() {
        int max = 0;
        if (_iconMaps!=null) {
            Iterator<Hashtable<Integer, NamedIcon>> it = _iconMaps.values().iterator();
            while (it.hasNext()) {
                Iterator<NamedIcon> iter = it.next().values().iterator();
                while (iter.hasNext()) {
                    max = Math.max(iter.next().getIconHeight(), max);
                }
            }
        }
        return max;
    }
    public int maxWidth() {
        int max = 0;
        if (_iconMaps!=null) {
            Iterator<Hashtable<Integer, NamedIcon>> it = _iconMaps.values().iterator();
            while (it.hasNext()) {
                Iterator<NamedIcon> iter = it.next().values().iterator();
                while (iter.hasNext()) {
                    max = Math.max(iter.next().getIconWidth(), max);
                }
            }
        }
        return max;
    }

    /******** popup AbstractAction.actionPerformed method overrides *********/

    protected void rotateOrthogonal() {
        if (_iconMaps!=null) {
            Iterator<Hashtable<Integer, NamedIcon>> it = _iconMaps.values().iterator();
            while (it.hasNext()) {
                Iterator<NamedIcon> iter = it.next().values().iterator();
                while (iter.hasNext()) {
                    NamedIcon icon = iter.next();
                    icon.setRotation(icon.getRotation()+1, this);
                }
            }
        }
        displayState(turnoutState());
    }

    public void setScale(double s) {
        if (_iconMaps!=null) {
            Iterator<Hashtable<Integer, NamedIcon>> it = _iconMaps.values().iterator();
            while (it.hasNext()) {
                Iterator<NamedIcon> iter = it.next().values().iterator();
                while (iter.hasNext()) {
                    iter.next().scale(s, this);
                }
            }
        }
        displayState(turnoutState());
    }

    public void rotate(int deg) {
        if (_iconMaps!=null) {
            Iterator<Hashtable<Integer, NamedIcon>> it = _iconMaps.values().iterator();
            while (it.hasNext()) {
                Iterator<NamedIcon> iter = it.next().values().iterator();
                while (iter.hasNext()) {
                    iter.next().rotate(deg, this);
                }
            }
        }
        displayState(turnoutState());
    }

    /**
	 * Drive the current state of the display from the state of the turnout and status of track.
	 */
    public void displayState(int state) {
        if (getNamedTurnout() == null) {
            log.debug("Display state "+state+", disconnected");
        } else {
            if (_status!=null && _iconMaps!=null) {
                NamedIcon icon = getIcon(_status, state);
                if (icon!=null) {
                    super.setIcon(icon);
                }
            }
        }
        super.displayState(state);
        updateSize();
    }

    public String getNameString() {
        String str = "";
        if (namedOccBlock!=null) {
            str = " in "+namedOccBlock.getBean().getDisplayName();
        } else if (namedOccSensor!=null) {
            str = " on "+namedOccSensor.getBean().getDisplayName();
        }
        return "ITrack "+super.getNameString()+str;
    }

    // update icon as state of turnout changes and status of track changes
    // Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
		if (log.isDebugEnabled())
			log.debug("property change: "+getNameString()+" property \""+evt.getPropertyName()+"\"= "
					+evt.getNewValue()+" from "+evt.getSource().getClass().getName());

        Object source = evt.getSource();
        if (source instanceof Turnout) {
            super.propertyChange(evt);
        } else if (source instanceof OBlock) {
            if ("state".equals(evt.getPropertyName()) || "path".equals(evt.getPropertyName())) {
                int now = ((Integer)evt.getNewValue()).intValue();
                setStatus((OBlock)source, now);
            }
        } else if (source instanceof Sensor) {
            if (evt.getPropertyName().equals("KnownState")) {
                int now = ((Integer)evt.getNewValue()).intValue();
                if (source.equals(getOccSensor())) {
                	_status = _pathUtil.setStatus(now);
                }
            }
        }
        displayState(turnoutState());
	}

    private void setStatus(OBlock block, int state) {
        _status = _pathUtil.setStatus(block, state);
        _pathUtil.setLocoIcon((String)block.getValue(), getLocation(), getSize(), _editor);
        if (_status.equals("DontUseTrack")) {
        	setControlling(false);
        }
    }

    protected void editItem() {
        makePalettteFrame(java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("IndicatorTO")));
        _TOPanel = new IndicatorTOItemPanel(_paletteFrame, "IndicatorTO", _iconFamily,
                                       PickListModel.turnoutPickModelInstance(), _editor);
        ActionListener updateAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateItem();
            }
        };
        // Convert _iconMaps state (ints) to Palette's bean names
        Hashtable<String, Hashtable<String, NamedIcon>> iconMaps =
                     new Hashtable<String, Hashtable<String, NamedIcon>>();
        iconMaps.put("ClearTrack", new Hashtable <String, NamedIcon>());
        iconMaps.put("OccupiedTrack", new Hashtable <String, NamedIcon>());
        iconMaps.put("PositionTrack", new Hashtable <String, NamedIcon>());
        iconMaps.put("AllocatedTrack", new Hashtable <String, NamedIcon>());
        iconMaps.put("DontUseTrack", new Hashtable <String, NamedIcon>());
        iconMaps.put("ErrorTrack", new Hashtable <String, NamedIcon>());
        Iterator<Entry<String, Hashtable<Integer, NamedIcon>>> it = _iconMaps.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Hashtable<Integer, NamedIcon>> entry = it.next();
            Hashtable <String, NamedIcon> clone = iconMaps.get(entry.getKey());
            Iterator<Entry<Integer, NamedIcon>> iter = entry.getValue().entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Integer, NamedIcon> ent = iter.next();
                NamedIcon oldIcon = ent.getValue();
                NamedIcon newIcon = cloneIcon(oldIcon, this);
                newIcon.rotate(0, this);
                newIcon.scale(1.0, this);
                newIcon.setRotation(4, this);
                clone.put(_state2nameMap.get(ent.getKey()), newIcon);
            }
        }
        _TOPanel.initUpdate(updateAction, iconMaps);
        _TOPanel.setSelection(getTurnout());
        if (namedOccSensor!=null) {
            _TOPanel.setOccDetector(namedOccSensor.getBean().getDisplayName());
        }
        if (namedOccBlock!=null) {
            _TOPanel.setOccDetector(namedOccBlock.getBean().getDisplayName());
        }
        _TOPanel.setShowTrainName(_pathUtil.showTrain());
        _TOPanel.setPaths(_pathUtil.getPaths());
        _paletteFrame.add(_TOPanel);
        _paletteFrame.pack();
        _paletteFrame.setVisible(true);
    }

    void updateItem() {
		if (log.isDebugEnabled()) log.debug("updateItem: "+getNameString()+" family= "+_TOPanel.getFamilyName());
        setTurnout(_TOPanel.getTableSelection().getSystemName());
        setOccSensor(_TOPanel.getOccSensor());
        setOccBlock(_TOPanel.getOccBlock());
        _pathUtil.setShowTrain(_TOPanel.getShowTrainName());
        _iconFamily = _TOPanel.getFamilyName();
        _pathUtil.setPaths(_TOPanel.getPaths());
        Hashtable<String, Hashtable<String, NamedIcon>> iconMap = _TOPanel.getIconMaps();
        if (iconMap!=null) {
            Iterator<Entry<String, Hashtable<String, NamedIcon>>> it = iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, Hashtable<String, NamedIcon>> entry = it.next();
                String status = entry.getKey();
                Hashtable <Integer, NamedIcon> oldMap = _iconMaps.get(entry.getKey());
                Iterator<Entry<String, NamedIcon>> iter = entry.getValue().entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, NamedIcon> ent = iter.next();
                    if (log.isDebugEnabled()) log.debug("key= "+ent.getKey());
                    NamedIcon newIcon = cloneIcon(ent.getValue(), this);
                    NamedIcon oldIcon = oldMap.get(_name2stateMap.get(ent.getKey()));
                    newIcon.setLoad(oldIcon.getDegrees(), oldIcon.getScale(), this);
                    newIcon.setRotation(oldIcon.getRotation(), this);
                    setIcon(status, ent.getKey(), newIcon);
                }
            }
        }   // otherwise retain current map
//        jmri.jmrit.catalog.ImageIndexEditor.checkImageIndex();
        _paletteFrame.dispose();
        _paletteFrame = null;
        _TOPanel.dispose();
        _TOPanel = null;
        displayState(turnoutState());
    }

    public void dispose() {
        if (namedOccSensor != null) {
            getOccSensor().removePropertyChangeListener(this);
        }
        namedOccSensor = null;
        namedOccSensor = null;
        super.dispose();
    }

    static Logger log = Logger.getLogger(IndicatorTurnoutIcon.class.getName());
}
