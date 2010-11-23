package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.Turnout;

import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.display.palette.IndicatorTOItemPanel;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.logix.OBlock;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Hashtable;
import jmri.util.NamedBeanHandle;
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
 * @version $Revision: 1.4 $
 */

public class IndicatorTurnoutIcon extends TurnoutIcon {

    Hashtable<String, Hashtable<Integer, NamedIcon>> _iconMaps;


    private NamedBeanHandle<Sensor> namedOccSensor = null;
    private NamedBeanHandle<OBlock> namedOccBlock = null;
    private NamedBeanHandle<Sensor> namedErrSensor = null;

    private String _status;
    private String _train;
    private boolean _showTrain; // this track should display _train when occupied

    public IndicatorTurnoutIcon(Editor editor) {
        super(editor);
        log.debug("IndicatorTurnoutIcon ctor: isIcon()= "+isIcon()+", isText()= "+isText());
        _status = "DontUseTrack";
        initMaps();

    }

    void initMaps() {
        _iconMaps = new Hashtable<String, Hashtable<Integer, NamedIcon>>();
        _iconMaps.put("ClearTrack", new Hashtable <Integer, NamedIcon>());
        _iconMaps.put("OccupiedTrack", new Hashtable <Integer, NamedIcon>());
        _iconMaps.put("PositionTrack", new Hashtable <Integer, NamedIcon>());
        _iconMaps.put("AllocatedTrack", new Hashtable <Integer, NamedIcon>());
        _iconMaps.put("DontUseTrack", new Hashtable <Integer, NamedIcon>());
        _iconMaps.put("ErrorTrack", new Hashtable <Integer, NamedIcon>());
    }

    public Positionable deepClone() {
        IndicatorTurnoutIcon pos = new IndicatorTurnoutIcon(_editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        IndicatorTurnoutIcon pos = (IndicatorTurnoutIcon)p;
        pos.setOccBlockHandle(namedOccBlock);
        pos.setOccSensorHandle(namedOccSensor);
        pos.setErrSensorHandle(namedErrSensor);
        pos.initMaps();
        Iterator<Entry<String, Hashtable<Integer, NamedIcon>>> it = _iconMaps.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Hashtable<Integer, NamedIcon>> entry = it.next();
            Hashtable <Integer, NamedIcon> clone = pos._iconMaps.get(entry.getKey());
            Iterator<Entry<Integer, NamedIcon>> iter = entry.getValue().entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Integer, NamedIcon> ent = iter.next();
//                if (log.isDebugEnabled()) log.debug("key= "+ent.getKey());
                clone.put(ent.getKey(), cloneIcon(ent.getValue(), pos));
            }
        }
        return super.finishClone(pos);
    }

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
                 setOccSensorHandle(new NamedBeanHandle<Sensor>(pName, sensor));                
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
            if (_iconMaps==null) {
                initMaps();
            }
            getOccSensor().addPropertyChangeListener(this);
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
    public void setOccBlockHandle(NamedBeanHandle<OBlock> block) {
        if (namedOccBlock != null) {
            getOccBlock().removePropertyChangeListener(this);
        }
        namedOccBlock = block;
        if (namedOccBlock != null) {
            if (_iconMaps==null) {
                initMaps();
            }
            getOccBlock().addPropertyChangeListener(this);
        } 
    }
    public OBlock getOccBlock() { 
        if (namedOccBlock==null) {
            return null;
        }
        return namedOccBlock.getBean(); 
    }    
    public NamedBeanHandle <OBlock> getNamedOccBlock() { return namedOccBlock; }


    /**
     * Attached a named sensor to display status from error detector
     * @param pName Used as a system/user name to lookup the sensor object
     */
     public void setErrSensor(String pName) {
         if (pName==null || pName.trim().length()==0) {
             setErrSensorHandle(null);
             return;
         }
         if (InstanceManager.sensorManagerInstance()!=null) {
             Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
             if (sensor != null) {
                 setErrSensorHandle(new NamedBeanHandle<Sensor>(pName, sensor));                
             } else {
                 log.error("Error Sensor '"+pName+"' not available, icon won't see changes");
             }
         } else {
             log.error("No SensorManager for this protocol, error icon won't see changes");
         }
     }

    public void setErrSensorHandle(NamedBeanHandle<Sensor> sen) {
        if (namedErrSensor != null) {
            getErrSensor().removePropertyChangeListener(this);
        }
        namedErrSensor = sen;
        if (namedErrSensor != null) {
            if (_iconMaps==null) {
                initMaps();
            }
            getErrSensor().addPropertyChangeListener(this);
        }
    }
    
    public Sensor getErrSensor() { 
        if (namedErrSensor==null) {
            return null;
        }
        return namedErrSensor.getBean(); 
    }    
    public NamedBeanHandle <Sensor> getNamedErrSensor() { return namedErrSensor; }

    public void setShowTrain(boolean set) {
        _showTrain = set;
    }
    public boolean showTrain() {
        return _showTrain;
    }

    /**
    * Place icon by its localized bean state name
    * @param status - the track condition of the icon
    * @param stateName - NamedBean name of turnout state
    * @param icon - icon corresponding to status and state
    */
    public void setIcon(String status, String stateName, NamedIcon icon) {
        if (log.isDebugEnabled()) log.debug("setIcon for status \""+status+"\", stateName= \""+stateName+
                                            " icom= "+icon);
//                                            ") state= "+_name2stateMap.get(stateName)+
//                                            " icon: w= "+icon.getIconWidth()+" h= "+icon.getIconHeight());
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
    void displayState(int state) {
//        updateSize();
        if (getNamedTurnout() == null) {
            log.debug("Display state "+state+", disconnected");
        } else {
            if (_status!=null && _iconMaps!=null) {
                log.debug(getNameString()+" displayState "+_state2nameMap.get(state)+", status= "+_status+
                          ", isIcon()= "+isIcon());
                if (_showTrain && "PositionTrack".equals(_status)) {
                    super.setText(_train);
                } else {
                    super.setText("");
                }
                NamedIcon icon = getIcon(_status, state);
                if (icon!=null) {
                    super.setIcon(icon);
                } else {
                    log.warn("No icon for state "+_state2nameMap.get(state)+", status= "+_status);
                }
            }
        }
        super.displayState(state);
        updateSize();
    }

    public String getNameString() {
        return "ITrack "+super.getNameString();
    }

    // update icon as state of turnout changes and status of track changes
    // Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
		if (log.isDebugEnabled())
			log.debug("property change: " + getNameString() + " property " + evt.getPropertyName() + " is now "
					+ evt.getNewValue()+" from "+evt.getSource().getClass().getName());

        Object source = evt.getSource();
        if (source instanceof Turnout) {
            super.propertyChange(evt);
            return;
        }
        if (namedOccBlock!=null && evt.getSource() instanceof OBlock) {
            if ("state".equals(evt.getPropertyName())) {
                int now = ((Integer)evt.getNewValue()).intValue();
                if ((now & OBlock.OUT_OF_SERVICE)!=0) {
                    _status = "DontUseTrack";
                } else if ((now & OBlock.UNOCCUPIED)!=0) {
                    _status = "ClearTrack";
                }
                if ((now & OBlock.OCCUPIED)!=0) {
                    if ((now & OBlock.RUNNING)!=0) {
                        _status = "PositionTrack";
                        OBlock block = (OBlock)evt.getSource();
                        _train = (String)block.getValue();
                    } else {
                        _status = "OccupiedTrack";
                    }
                } else if ((now & OBlock.ALLOCATED)!=0) {
                    _status = "AllocatedTrack";
                }
            }
        } else if (evt.getPropertyName().equals("KnownState") && evt.getSource() instanceof Sensor) {
                int now = ((Integer)evt.getNewValue()).intValue();
                if (namedOccSensor!=null) {
                    if (now==Sensor.ACTIVE) {
                        _status = "OccupiedTrack";
                    } else if (now==Sensor.INACTIVE) {
                        _status = "ClearTrack";
                    } else if (now==Sensor.UNKNOWN) {
                        _status = "DontUseTrack";
                    } else {
                        _status = "ErrorTrack";
                    }
                }
                if (evt.getSource().equals(getErrSensor())) {
                    if (now==Sensor.ACTIVE) {
                        _status = "ErrorTrack";
                    } else {
                        _status = "DontUseTrack";
                    }
                }
        }
        displayState(turnoutState());
	}

    IndicatorTOItemPanel _TOPanel;
    protected void editItem() {
        if (log.isDebugEnabled()) log.debug("edit: ");
        makePalettteFrame(java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString("IndicatorTO")));
        _TOPanel = new IndicatorTOItemPanel(_paletteFrame, "IndicatorTO",
                                       PickListModel.turnoutPickModelInstance(), _editor);
        _TOPanel.init( new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateItem();
            }
        });
        _TOPanel.setSelection(getTurnout());
        if (namedErrSensor!=null) {
            _TOPanel.setErrSensor(namedErrSensor.getName());
        }
        if (namedOccSensor!=null) {
            _TOPanel.setOccDetector(namedOccSensor.getName());
        }
        if (namedOccBlock!=null) {
            _TOPanel.setOccDetector(namedOccBlock.getName());
        }
        _TOPanel.setShowTrainName(_showTrain);
        _paletteFrame.add(_TOPanel);
        _paletteFrame.pack();
    }

    void updateItem() {
        setTurnout(_TOPanel.getTableSelection().getSystemName());
        setErrSensor(_TOPanel.getErrSensor());
        setOccSensor(_TOPanel.getOccSensor());
        setOccBlock(_TOPanel.getOccBlock());
        _showTrain = _TOPanel.getShowTrainName();
        Hashtable<String, Hashtable<String, NamedIcon>> iconMap = _TOPanel.getIconMaps();

        Iterator<Entry<String, Hashtable<String, NamedIcon>>> it = iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Hashtable<String, NamedIcon>> entry = it.next();
            String status = entry.getKey();
//            Hashtable<Integer, NamedIcon> oldMap = cloneMap(_iconMaps.get(status), null);
            Hashtable <Integer, NamedIcon> oldMap = _iconMaps.get(entry.getKey());
            Iterator<Entry<String, NamedIcon>> iter = entry.getValue().entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, NamedIcon> ent = iter.next();
                if (log.isDebugEnabled()) log.debug("key= "+ent.getKey());
                NamedIcon newIcon = ent.getValue();
                NamedIcon oldIcon = oldMap.get(_name2stateMap.get(ent.getKey()));
                newIcon.setLoad(oldIcon.getDegrees(), oldIcon.getScale(), this);
                newIcon.setRotation(oldIcon.getRotation(), this);
                setIcon(status, ent.getKey(), newIcon);
            }
        }
        _paletteFrame.dispose();
        _paletteFrame = null;
        _TOPanel = null;
        invalidate();
    }

    public void dispose() {
        if (namedOccSensor != null) {
            getOccSensor().removePropertyChangeListener(this);
        }
        namedOccSensor = null;
        if (namedErrSensor != null) {
            getErrSensor().removePropertyChangeListener(this);
        }
        namedOccSensor = null;
        namedErrSensor = null;
        super.dispose();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IndicatorTurnoutIcon.class.getName());
}
