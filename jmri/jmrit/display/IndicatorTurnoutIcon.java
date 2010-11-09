package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.Turnout;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.logix.OBlock;

import java.util.Hashtable;
import jmri.util.NamedBeanHandle;
import java.util.Iterator;
import java.util.Map.Entry;
/*
import jmri.Turnout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPopupMenu;
*/
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
 * @version $Revision: 1.1 $
 */

public class IndicatorTurnoutIcon extends TurnoutIcon {

    Hashtable<String, Hashtable<Integer, NamedIcon>> _iconMaps;


    private NamedBeanHandle<Sensor> namedOccSensor = null;
    private NamedBeanHandle<OBlock> namedOccBlock = null;
    private NamedBeanHandle<Sensor> namedErrSensor = null;

    private String _status;
    private String _train;

    public IndicatorTurnoutIcon(Editor editor) {
        super(editor);
        log.debug("IndicatorTurnoutIcon ctor: isIcon()= "+isIcon()+", isText()= "+isText());
        _status = "DontUseTrack";
    }

    public Positionable deepClone() {
        IndicatorTurnoutIcon pos = new IndicatorTurnoutIcon(_editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        IndicatorTurnoutIcon pos = (IndicatorTurnoutIcon)p;
        pos.setOccSensor(namedOccSensor);
        if (namedErrSensor!=null) {
            pos.setErrSensor(namedErrSensor);
        }
        Iterator<Entry<String, Hashtable<Integer, NamedIcon>>> it = _iconMaps.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Hashtable<Integer, NamedIcon>> entry = it.next();
            pos._iconMaps.put(entry.getKey(), cloneMap(entry.getValue(), pos));
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
         if (InstanceManager.sensorManagerInstance()!=null) {
             Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
             if (sensor != null) {
                 setOccSensor(new NamedBeanHandle<Sensor>(pName, sensor));                
             } else {
                 log.error("Block Sensor '"+pName+"' not available, icon won't see changes");
             }
         } else {
             log.error("No SensorManager for this protocol, block icons won't see changes");
         }
     }

    public void setOccSensor(NamedBeanHandle<Sensor> sen) {
        if (namedOccSensor != null) {
            getOccSensor().removePropertyChangeListener(this);
        }
        namedOccSensor = sen;
        if (namedOccSensor != null) {
            _iconMaps = new Hashtable<String, Hashtable<Integer, NamedIcon>>();
            _iconMaps.put("ClearTrack", new Hashtable <Integer, NamedIcon>());
            _iconMaps.put("OccupiedTrack", new Hashtable <Integer, NamedIcon>());
            _iconMaps.put("PositionTrack", new Hashtable <Integer, NamedIcon>());
            _iconMaps.put("AllocatedTrack", new Hashtable <Integer, NamedIcon>());
            _iconMaps.put("DontUseTrack", new Hashtable <Integer, NamedIcon>());
            _iconMaps.put("ErrorTrack", new Hashtable <Integer, NamedIcon>());
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
         if (InstanceManager.sensorManagerInstance()!=null) {
             OBlock block = InstanceManager.oBlockManagerInstance().getOBlock(pName);
             if (block != null) {
                 setOccBlock(new NamedBeanHandle<OBlock>(pName, block));                
             } else {
                 log.error("Detection Sensor '"+pName+"' not available, icon won't see changes");
             }
         } else {
             log.error("No SensorManager for this protocol, block icons won't see changes");
         }
     }   
    public void setOccBlock(NamedBeanHandle<OBlock> block) {
        if (namedOccBlock != null) {
            getOccBlock().removePropertyChangeListener(this);
        }
        namedOccBlock = block;
        if (namedOccBlock != null) {
            _iconMaps = new Hashtable<String, Hashtable<Integer, NamedIcon>> ();
            _iconMaps.put("ClearTrack", new Hashtable <Integer, NamedIcon>());
            _iconMaps.put("OccupiedTrack", new Hashtable <Integer, NamedIcon>());
            _iconMaps.put("PositionTrack", new Hashtable <Integer, NamedIcon>());
            _iconMaps.put("AllocatedTrack", new Hashtable <Integer, NamedIcon>());
            _iconMaps.put("DontUseTrack", new Hashtable <Integer, NamedIcon>());
            _iconMaps.put("ErrorTrack", new Hashtable <Integer, NamedIcon>());
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
         if (InstanceManager.sensorManagerInstance()!=null) {
             Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
             if (sensor != null) {
                 setErrSensor(new NamedBeanHandle<Sensor>(pName, sensor));                
             } else {
                 log.error("Error Sensor '"+pName+"' not available, icon won't see changes");
             }
         } else {
             log.error("No SensorManager for this protocol, error icon won't see changes");
         }
     }

    public void setErrSensor(NamedBeanHandle<Sensor> sen) {
        if (namedErrSensor != null) {
            getErrSensor().removePropertyChangeListener(this);
        }
        namedErrSensor = sen;
        if (namedErrSensor != null) {
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
            if (_status==null || _iconMaps==null) {
                super.displayState(state);
            } else {
                log.debug(getNameString()+" displayState "+_state2nameMap.get(state)+", status= "+_status+
                          ", isIcon()= "+isIcon());
                if (isText()) {
                    super.setText(_state2nameMap.get(state));
                }
                if ("PositionTrack".equals(_status)) {
                    super.setText(_train);
                }
                if (isIcon()) {
                    NamedIcon icon = getIcon(_status, state);
                    if (icon!=null) {
                        super.setIcon(icon);
                    } else {
                        super.displayState(state);
                    }
                }
            }
        }
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
                } else if (namedErrSensor!=null) {
                    if (now==Sensor.ACTIVE) {
                        _status = "ErrorTrack";
                    }
                }
        }
        displayState(turnoutState());
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
