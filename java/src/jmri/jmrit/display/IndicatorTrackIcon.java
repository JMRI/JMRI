package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.logix.OBlock;

import jmri.jmrit.display.palette.IndicatorItemPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPopupMenu;

import java.util.ArrayList;
import java.util.Hashtable;
import jmri.NamedBeanHandle;
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
 * @version $Revision$
 */

public class IndicatorTrackIcon extends PositionableIcon 
                        implements java.beans.PropertyChangeListener, IndicatorTrack {

    ArrayList<String> _paths;      // list of paths that include this icon

    private NamedBeanHandle<Sensor> namedOccSensor = null;
    private NamedBeanHandle<OBlock> namedOccBlock = null;

    private String _status;     // is a key for _iconMap
    private boolean _showTrain; // this track should display _loco when occupied
    private LocoIcon _loco = null;


    public IndicatorTrackIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(editor);
        _status = "ClearTrack";
        _iconMap = new Hashtable<String, NamedIcon>();
    }

    public Positionable deepClone() {
        IndicatorTrackIcon pos = new IndicatorTrackIcon(_editor);        
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        IndicatorTrackIcon pos = (IndicatorTrackIcon)p;
        pos.setOccSensorHandle(namedOccSensor);
        pos.setOccBlockHandle(namedOccBlock);
        pos._iconMap = cloneMap(_iconMap, pos);
        if (_paths!=null) {
            pos._paths = new ArrayList<String>();
            for (int i=0; i<_paths.size(); i++) {
                pos._paths.add(_paths.get(i));
            }
        }
        pos._iconFamily = _iconFamily;
        pos._showTrain = _showTrain;
        return super.finishClone(pos);
    }

    public Hashtable<String, NamedIcon> getIconMap() {
        return cloneMap(_iconMap, this);
    }

    /**
     * Attached a named sensor to display status
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
     public void setOccSensorHandle(NamedBeanHandle<Sensor> senHandle) {
         if (namedOccSensor != null) {
             getOccSensor().removePropertyChangeListener(this);
         }
         namedOccSensor = senHandle;
         if (namedOccSensor != null) {
             if (_iconMap==null) {
                 _iconMap = new Hashtable<String, NamedIcon>();
             }
             Sensor sensor = getOccSensor();
             sensor.addPropertyChangeListener(this, namedOccSensor.getName(), "Indicator Track");
             setStatus(sensor.getKnownState());
             displayState(_status);
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
            if (_iconMap==null) {
                _iconMap = new Hashtable<String, NamedIcon>();
            }
            OBlock block = getOccBlock();
            block.addPropertyChangeListener(this, namedOccBlock.getName(), "Indicator Track");
            setStatus(block, block.getState());
            displayState(_status);
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
        _showTrain = set;
    }
    public boolean showTrain() {
        return _showTrain;
    }

    public Iterator<String> getPaths() {
        if (_paths==null) {
            return null;
        }
        return _paths.iterator();
    }
    public void setPaths(ArrayList<String>paths) {
        _paths = paths;
        if (log.isDebugEnabled()) {
            log.debug("setPaths");
            for (int i=0; i<_paths.size(); i++) {
                log.debug("pathName= "+_paths.get(i));
            }
        }
    }

    public void addPath(String path) {
        if (_paths==null) {
            _paths = new ArrayList<String>();
        }
        if (!_paths.contains(path)) {
            _paths.add(path);
        }
        if (log.isDebugEnabled()) log.debug("addPath \""+path+"\" #paths= "+_paths.size());
    }
    public void removePath(String path) {
        if (_paths!=null) {
            _paths.remove(path);
        }
    }

    /**
    * Place icon by its bean state name
    */
    public void setIcon(String name, NamedIcon icon) {
        if (log.isDebugEnabled()) log.debug("set \""+name+"\" icon= "+icon);
        _iconMap.put(name, icon);
        setIcon(_iconMap.get(_status));
    }
    
    public String getStatus() {
        return _status;
    }

	public void propertyChange(java.beans.PropertyChangeEvent evt) {
		if (log.isDebugEnabled())
			log.debug("property change: " + getNameString() + " property " + evt.getPropertyName() + " is now "
					+ evt.getNewValue()+" from "+evt.getSource().getClass().getName());
        
        Object source = evt.getSource();
        if (source instanceof OBlock) {
            if ("state".equals(evt.getPropertyName()) || "path".equals(evt.getPropertyName())) {
                int now = ((Integer)evt.getNewValue()).intValue();
                setStatus((OBlock)source, now);
            }
        } else if (source instanceof Sensor) {
            if (evt.getPropertyName().equals("KnownState")) {
                int now = ((Integer)evt.getNewValue()).intValue();
                if (source.equals(getOccSensor())) {
                    setStatus(now);
                }
            }
        }
        displayState(_status);
	}

    private void setStatus(OBlock block, int state) {
        String pathName = block.getAllocatedPathName();
        if ((state & OBlock.TRACK_ERROR)!=0) {
            _status = "ErrorTrack";
        } else if ((state & OBlock.OUT_OF_SERVICE)!=0) {
            setControlling(false);
            /*
            if ((state & OBlock.OCCUPIED)!=0) {
                _status = "OccupiedTrack";
            } else {
                _status = "DontUseTrack";
            }
            */
            _status = "DontUseTrack";
        } else if ((state & OBlock.OCCUPIED)!=0) {
            setControlling(true);
            if (_showTrain) {
                setLocoIcon((String)block.getValue());
            }
            if ((state & OBlock.RUNNING)!=0) {
                if (_paths!=null && _paths.contains(pathName)) {
                    _status = "PositionTrack";
                } else {
                    _status = "ClearTrack";     // icon not on path
                }
            } else {
                _status = "OccupiedTrack";
            }
        } else {
            setControlling(true);
            if (_loco!=null) {
                _loco.remove();
                _loco = null;
            }
            if ((state & OBlock.ALLOCATED)!=0) {
                if (_paths!=null && _paths.contains(pathName)) {
                    _status = "AllocatedTrack";     // icon on path
                } else {
                    _status = "ClearTrack";
                }
            } else if ((state & Sensor.UNKNOWN)!=0) {
                _status = "DontUseTrack";
            } else {
                _status = "ClearTrack";
            }
        }
    }

    public void setStatus(int state) {
        if (state==Sensor.ACTIVE) {
            _status = "OccupiedTrack";
        } else if (state==Sensor.INACTIVE) {
            _status = "ClearTrack";
        } else if (state==Sensor.UNKNOWN) {
            _status = "DontUseTrack";
        } else {
            _status = "ErrorTrack";
        }
    }

    private void setLocoIcon(String trainName) {
        if (trainName==null) {
            if (_loco!=null) {
                _loco.remove();
                _loco = null;
            }
            return;
        }
        if (_loco!=null) {
            return;
        }
        trainName = trainName.trim();
        _loco = _editor.selectLoco(trainName);
        if (_loco==null) {
            _loco = _editor.addLocoIcon(trainName);
        }
        if (_loco!=null) {
            java.awt.Point pt = getLocation();
            pt.x = pt.x + (getWidth() - _loco.getWidth())/2;
            pt.y = pt.y + (getHeight() - _loco.getHeight())/2;
            _loco.setLocation(pt);
            log.debug("Display Loco \""+trainName+"\" ("+_status+") at ("+pt.x+", "+pt.y+")");
        }
    }

    public String getNameString() {
        String str = "";
        if (namedOccBlock!=null) {
            str = "in "+namedOccBlock.getBean().getDisplayName();
        } else if (namedOccSensor!=null) {
            str = "on "+namedOccSensor.getBean().getDisplayName();
        }
        return "ITrack "+str;
    }

    /**
     * Pop-up displays unique attributes
     */
    public boolean showPopUp(JPopupMenu popup) {
        return false;
	}

    /**
	 * Drive the current state of the display from the state of the turnout.
	 */
    void displayState(String status) {
        log.debug(getNameString() +" displayStatus "+_status);
        NamedIcon icon = getIcon(status);
        if (icon!=null) {
            super.setIcon(icon);
        }
        updateSize();
    }

    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString("IndicatorTrack"));
        popup.add(new javax.swing.AbstractAction(txt) {
                public void actionPerformed(ActionEvent e) {
                    editItem();
                }
            });
        return true;
    }

    IndicatorItemPanel _trackPanel;

    protected void editItem() {
        makePalettteFrame(java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString("IndicatorTrack")));
        _trackPanel = new IndicatorItemPanel(_paletteFrame, "IndicatorTrack", _iconFamily, _editor);

        ActionListener updateAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateItem();
            }
        };
        // duplicate _iconMap map with unscaled and unrotated icons
        Hashtable<String, NamedIcon> map = new Hashtable<String, NamedIcon>();
        Iterator<Entry<String, NamedIcon>> it = _iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            NamedIcon oldIcon = entry.getValue();
            NamedIcon newIcon = cloneIcon(oldIcon, this);
            newIcon.rotate(0, this);
            newIcon.scale(1.0, this);
            newIcon.setRotation(4, this);
            map.put(entry.getKey(), newIcon);
        }
        _trackPanel.init(updateAction, map);
        if (namedOccSensor!=null) {
            _trackPanel.setOccDetector(namedOccSensor.getName());
        }
        if (namedOccBlock!=null) {
            _trackPanel.setOccDetector(namedOccBlock.getName());
        }
        _trackPanel.setShowTrainName(_showTrain);
        _trackPanel.setPaths(_paths);
        _paletteFrame.add(_trackPanel);
        _paletteFrame.setLocationRelativeTo(this);
        _paletteFrame.toFront();
        _paletteFrame.pack();
        _paletteFrame.setVisible(true);
    }

    void updateItem() {
        setOccSensor(_trackPanel.getOccSensor());
        setOccBlock(_trackPanel.getOccBlock());
        _showTrain = _trackPanel.getShowTrainName();
        _iconFamily = _trackPanel.getFamilyName();
        _paths = _trackPanel.getPaths();
        Hashtable<String, NamedIcon> iconMap = _trackPanel.getIconMap();
        if (iconMap!=null) {
            Hashtable<String, NamedIcon> oldMap = cloneMap(_iconMap, this);
            Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                if (log.isDebugEnabled()) log.debug("key= "+entry.getKey());
                NamedIcon newIcon = entry.getValue();
                NamedIcon oldIcon = oldMap.get(entry.getKey());
                newIcon.setLoad(oldIcon.getDegrees(), oldIcon.getScale(), this);
                newIcon.setRotation(oldIcon.getRotation(), this);
                setIcon(entry.getKey(), newIcon);
            }
        }   // otherwise retain current map
        jmri.jmrit.catalog.ImageIndexEditor.checkImageIndex(_editor);
        _paletteFrame.dispose();
        _paletteFrame = null;
        _trackPanel.dispose();
        _trackPanel = null;
        displayState(_status);
    }

    public void dispose() {
        if (namedOccSensor != null) {
            getOccSensor().removePropertyChangeListener(this);
        }
        namedOccSensor = null;

        if (namedOccBlock != null) {
            getOccBlock().removePropertyChangeListener(this);
        }
        namedOccBlock = null;

        _iconMap = null;
        super.dispose();
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IndicatorTrackIcon.class.getName());
}

