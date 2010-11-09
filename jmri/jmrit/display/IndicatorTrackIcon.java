package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.logix.OBlock;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPopupMenu;
import java.util.Hashtable;
import jmri.util.NamedBeanHandle;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * An icon to display the status of a track segment in a block
 * <P>
 * This responds to the following conditions: 
 *  1. KnownState of an occupancy sensor of the block where the track segment appears
 *  2. Allocation of a route by a Warrant where the track segment appears
 *  3. Current position of a train being run under a Warrant where the track segment appears in a block of the route
 *  4. Out of Service for a block that cannot or should not be used
 *  5. An error state of the block where the track segment appears (short/no power etc.) 
 * <P>
 * A click on the icon does not change any of the above conditions..
 *<P>
 * @author Pete Cressman  Copyright (c) 2010
 * @version $Revision: 1.2 $
 */

public class IndicatorTrackIcon extends PositionableLabel 
                        implements java.beans.PropertyChangeListener {

    Hashtable<String, NamedIcon> _iconMap;

    private NamedBeanHandle<Sensor> namedOccSensor = null;
    private NamedBeanHandle<OBlock> namedOccBlock = null;
    private NamedBeanHandle<Sensor> namedErrSensor = null;

    private String _status;     // is a key for _iconMap
    private String _train;

    public IndicatorTrackIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/tracksegments/block.gif",
                            "resources/icons/smallschematics/tracksegments/block.gif"), editor);
        setPopupUtility(null);
        _status = "DontUseTrack";
        _text = true;
        _icon = true;
    }

    public Positionable deepClone() {
        IndicatorTrackIcon pos = new IndicatorTrackIcon(_editor);        
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        IndicatorTrackIcon pos = (IndicatorTrackIcon)p;
        pos.setOccSensor(namedOccSensor);
        if (namedErrSensor!=null) {
            pos.setErrSensor(namedErrSensor);
        }
        Iterator<Entry<String, NamedIcon>> it = _iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            pos.setIcon(entry.getKey(), entry.getValue());
        }
        return super.finishClone(pos);
    }

    public Hashtable<String, NamedIcon> getIconMap() {
        return _iconMap;
    }

    /**
     * Attached a named sensor to display status
     * @param pName Used as a system/user name to lookup the sensor object
     */
     public void setOccSensor(String pName) {
         if (InstanceManager.sensorManagerInstance()!=null) {
             Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
             if (sensor != null) {
                 setOccSensor(new NamedBeanHandle<Sensor>(pName, sensor));                
             } else {
                 log.error("Occupancy Sensor '"+pName+"' not available, icon won't see changes");
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
             _iconMap = new Hashtable<String, NamedIcon>();
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
                 log.error("OBlock Sensor '"+pName+"' not available, icon won't see changes");
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
            _iconMap = new Hashtable<String, NamedIcon>();
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
    * Place icon by its bean state name
    */
    public void setIcon(String name, NamedIcon icon) {
        if (log.isDebugEnabled()) log.debug("set \""+name+"\" icon= "+icon);
        _iconMap.put(name, icon);
        setIcon(_iconMap.get("ClearTrack"));
    }

    public NamedIcon getIcon(String name) {
        if (log.isDebugEnabled()) log.debug("get \""+name+"\" icon");
        return _iconMap.get(name);
    }

    public String getStatus() {
        return _status;
    }


    public int maxHeight() {
        int max = 0;
        if (_iconMap!=null) {
            Iterator<NamedIcon> iter = _iconMap.values().iterator();
            while (iter.hasNext()) {
                max = Math.max(iter.next().getIconHeight(), max);
            }
        }
        return max;
    }
    public int maxWidth() {
        int max = 0;
        if (_iconMap!=null) {
            Iterator<NamedIcon> iter = _iconMap.values().iterator();
            while (iter.hasNext()) {
                max = Math.max(iter.next().getIconWidth(), max);
            }
        }
        return max;
    }

    public void propertyChange(java.beans.PropertyChangeEvent evt) {
		if (log.isDebugEnabled())
			log.debug("property change: " + getNameString() + " property " + evt.getPropertyName() + " is now "
					+ evt.getNewValue()+" from "+evt.getSource().getClass().getName());

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
        displayState(_status);
	}

    public String getNameString() {
        return "ITrack";
    }

    /**
     * Pop-up displays unique attributes
     */
    public boolean showPopUp(JPopupMenu popup) {
        return false;
	}

    /******** popup AbstractAction.actionPerformed method overrides *********/

    protected void rotateOrthogonal() {
        Iterator<NamedIcon> it = _iconMap.values().iterator();
        while (it.hasNext()) {
            NamedIcon icon = it.next();
            icon.setRotation(icon.getRotation()+1, this);
        }
        displayState(_status);
    }

    public void setScale(double s) {
        Iterator<NamedIcon> it = _iconMap.values().iterator();
        while (it.hasNext()) {
            it.next().scale(s, this);
        }
        displayState(_status);
    }

    public void rotate(int deg) {
        Iterator<NamedIcon> it = _iconMap.values().iterator();
        while (it.hasNext()) {
            it.next().rotate(deg, this);
        }
        displayState(_status);
    }

    /**
	 * Drive the current state of the display from the state of the turnout.
	 */
    void displayState(String status) {
        log.debug(getNameString() +" displayStatus "+_status);
        if ("PositionTrack".equals(_status)) {
            super.setText(_train);
        } else {
            super.setText("");
        }
        NamedIcon icon = getIcon(status);
        if (icon!=null) {
            super.setIcon(icon);
        }
        updateSize();
    }

    public boolean setEditIconMenu(JPopupMenu popup) {
        /*
        String txt = java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString("Track"));
        popup.add(new javax.swing.AbstractAction(txt) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
        return true;
        */
        return false;
    }
/*
    protected void edit() {
        makeIconEditorFrame(this, "Track", true, null);
        _iconEditor.setIcon(0, "ClearTrack", _iconMap.get( "ClearTrack"));
        _iconEditor.setIcon(1, "TrackOccupied", _occupiedIcon);
        _iconEditor.setIcon(2, "TrackAllocated", _allocIcon);
        _iconEditor.setIcon(3, "TrackCurrentPosition", _positionIcon);
        _iconEditor.setIcon(4, "TrackDoNotUse", _notInServiceIcon);
        _iconEditor.setIcon(5, "TrackError", _errorIcon);
        _iconEditor.makeIconPanel();

        // set default icons, then override with this turnout's icons
        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateTrack();
            }
        };
        _iconEditor.complete(addIconAction, true, true, true);
    }

    void updateTrack() {
        _clearIcon = _iconEditor.getIcon("ClearTrack");
        _occupiedIcon = _iconEditor.getIcon("TrackOccupied");
        _allocIcon = _iconEditor.getIcon("TrackAllocated");
        _positionIcon = _iconEditor.getIcon("TrackCurrentPosition");
        _notInServiceIcon = _iconEditor.getIcon("TrackDoNotUse");
        _errorIcon = _iconEditor.getIcon("TrackError");
        updateSize();
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();

        setOccSensor(_iconEditor.getTableSelection().getDisplayName());
        setErrSensor(_iconEditor.getTableSelection().getDisplayName());
    }
*/
    public void dispose() {
        if (namedOccSensor != null) {
            getOccSensor().removePropertyChangeListener(this);
        }
        namedOccSensor = null;

        if (namedOccBlock != null) {
            getOccBlock().removePropertyChangeListener(this);
        }
        namedOccBlock = null;

        if (namedErrSensor != null) {
            getErrSensor().removePropertyChangeListener(this);
        }
        namedErrSensor = null;
        _iconMap = null;
        super.dispose();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IndicatorTrackIcon.class.getName());
}

