package jmri.jmrit.display;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.palette.IndicatorTOItemPanel;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.picker.PickListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display a status and state of a color coded turnout.<p>
 * This responds to only KnownState, leaving CommandedState to some other
 * graphic representation later.
 * <p>
 * "state" is the state of the underlying turnout ("closed", "thrown", etc.)
 * <p>
 * "status" is the operating condition of the track ("clear", "occupied", etc.)
 * <p>
 * A click on the icon will command a state change. Specifically, it will set
 * the CommandedState to the opposite (THROWN vs CLOSED) of the current
 * KnownState. This will display the setting of the turnout points.
 * <p>
 * The status is indicated by color and changes are done only done by the
 * occupancy sensing - OBlock or other sensor.
 * <p>
 * The default icons are for a left-handed turnout, facing point for east-bound
 * traffic.
 *
 * @author Bob Jacobsen Copyright (c) 2002
 * @author Pete Cressman Copyright (c) 2010 2012
 */
public class IndicatorTurnoutIcon extends TurnoutIcon implements IndicatorTrack {

    HashMap<String, HashMap<Integer, NamedIcon>> _iconMaps;

    private NamedBeanHandle<Sensor> namedOccSensor = null;
    private NamedBeanHandle<OBlock> namedOccBlock = null;

    private IndicatorTrackPaths _pathUtil;
    private IndicatorTOItemPanel _itemPanel;
    private String _status;

    public IndicatorTurnoutIcon(Editor editor) {
        super(editor);
        log.debug("IndicatorTurnoutIcon ctor: isIcon()= {}, isText()= {}", isIcon(), isText());
        _pathUtil = new IndicatorTrackPaths();
        _status = "DontUseTrack";
        _iconMaps = initMaps();

    }

    static HashMap<String, HashMap<Integer, NamedIcon>> initMaps() {
        HashMap<String, HashMap<Integer, NamedIcon>> iconMaps = new HashMap<>();
        iconMaps.put("ClearTrack", new HashMap<>());
        iconMaps.put("OccupiedTrack", new HashMap<>());
        iconMaps.put("PositionTrack", new HashMap<>());
        iconMaps.put("AllocatedTrack", new HashMap<>());
        iconMaps.put("DontUseTrack", new HashMap<>());
        iconMaps.put("ErrorTrack", new HashMap<>());
        return iconMaps;
    }

    HashMap<String, HashMap<Integer, NamedIcon>> cloneMaps(IndicatorTurnoutIcon pos) {
        HashMap<String, HashMap<Integer, NamedIcon>> iconMaps = initMaps();
        for (Entry<String, HashMap<Integer, NamedIcon>> entry : _iconMaps.entrySet()) {
            HashMap<Integer, NamedIcon> clone = iconMaps.get(entry.getKey());
            for (Entry<Integer, NamedIcon> ent : entry.getValue().entrySet()) {
                //                if (log.isDebugEnabled()) log.debug("key= "+ent.getKey());
                clone.put(ent.getKey(), cloneIcon(ent.getValue(), pos));
            }
        }
        return iconMaps;
    }

    @Override
    public Positionable deepClone() {
        IndicatorTurnoutIcon pos = new IndicatorTurnoutIcon(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(IndicatorTurnoutIcon pos) {
        pos.setOccBlockHandle(namedOccBlock);
        pos.setOccSensorHandle(namedOccSensor);
        pos._iconMaps = cloneMaps(pos);
        pos._pathUtil = _pathUtil.deepClone();
        pos._iconFamily = _iconFamily;
        return super.finishClone(pos);
    }

    public HashMap<String, HashMap<Integer, NamedIcon>> getIconMaps() {
        return new HashMap<>(_iconMaps);
    }

    /**
     * Attached a named sensor to display status from OBlocks
     *
     * @param pName Used as a system/user name to lookup the sensor object
     */
    @Override
    public void setOccSensor(String pName) {
        if (pName == null || pName.trim().length() == 0) {
            setOccSensorHandle(null);
            return;
        }
        if (InstanceManager.getNullableDefault(jmri.SensorManager.class) != null) {
            try {
                Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
                setOccSensorHandle(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, sensor));
            } catch (IllegalArgumentException ex) {
                log.error("Occupancy Sensor '{}' not available, icon won't see changes", pName);
            }
        } else {
            log.error("No SensorManager for this protocol, block icons won't see changes");
        }
    }

    @Override
    public void setOccSensorHandle(NamedBeanHandle<Sensor> sen) {
        if (namedOccSensor != null) {
            getOccSensor().removePropertyChangeListener(this);
        }
        namedOccSensor = sen;
        if (namedOccSensor != null) {
            Sensor sensor = getOccSensor();
            sensor.addPropertyChangeListener(this, namedOccSensor.getName(), "Indicator Turnout Icon");
            _status = _pathUtil.getStatus(sensor.getKnownState());
            if (_iconMaps != null) {
                displayState(turnoutState());
            }
        }
    }

    @Override
    public Sensor getOccSensor() {
        if (namedOccSensor == null) {
            return null;
        }
        return namedOccSensor.getBean();
    }

    @Override
    public NamedBeanHandle<Sensor> getNamedOccSensor() {
        return namedOccSensor;
    }

    /**
     * Attached a named OBlock to display status
     *
     * @param pName Used as a system/user name to lookup the OBlock object
     */
    @Override
    public void setOccBlock(String pName) {
        if (pName == null || pName.trim().length() == 0) {
            setOccBlockHandle(null);
            return;
        }
        OBlock block = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock(pName);
        if (block != null) {
            setOccBlockHandle(InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(pName, block));
        } else {
            log.error("Detection OBlock '{}' not available, icon won't see changes", pName);
        }
    }

    @Override
    public void setOccBlockHandle(NamedBeanHandle<OBlock> blockHandle) {
        if (namedOccBlock != null) {
            getOccBlock().removePropertyChangeListener(this);
        }
        namedOccBlock = blockHandle;
        if (namedOccBlock != null) {
            OBlock block = getOccBlock();
            block.addPropertyChangeListener(this, namedOccBlock.getName(), "Indicator Turnout Icon");
            setStatus(block, block.getState());
            if (_iconMaps != null) {
                displayState(turnoutState());
            }
            setToolTip(new ToolTip(block.getDescription(), 0, 0));
        } else {
            setToolTip(new ToolTip(null, 0, 0));
        }
    }

    @Override
    public OBlock getOccBlock() {
        if (namedOccBlock == null) {
            return null;
        }
        return namedOccBlock.getBean();
    }

    @Override
    public NamedBeanHandle<OBlock> getNamedOccBlock() {
        return namedOccBlock;
    }

    @Override
    public void setShowTrain(boolean set) {
        _pathUtil.setShowTrain(set);
    }

    @Override
    public boolean showTrain() {
        return _pathUtil.showTrain();
    }

    @Override
    public ArrayList<String> getPaths() {
        return _pathUtil.getPaths();
    }

    public void setPaths(ArrayList<String> paths) {
        _pathUtil.setPaths(paths);
    }

    @Override
    public void addPath(String path) {
        _pathUtil.addPath(path);
    }

    @Override
    public void removePath(String path) {
        _pathUtil.removePath(path);
    }

    /**
     * get track name for known state of occupancy sensor
     */
    @Override
    public void setStatus(int state) {
        _status = _pathUtil.getStatus(state);
    }

    /**
     * Place icon by its localized bean state name
     *
     * @param status     the track condition of the icon
     * @param stateName  NamedBean name of turnout state
     * @param icon       icon corresponding to status and state
     */
    public void setIcon(String status, String stateName, NamedIcon icon) {
        if (log.isDebugEnabled()) {
            log.debug("setIcon for status \"{}\", stateName= \"{} icom= {}", status, stateName, icon.getURL());
        }
//                                            ") state= "+_name2stateMap.get(stateName)+
//                                            " icon: w= "+icon.getIconWidth()+" h= "+icon.getIconHeight());
        if (_iconMaps == null) {
            _iconMaps = initMaps();
        }
        _iconMaps.get(status).put(_name2stateMap.get(stateName), icon);
        setIcon(_iconMaps.get("ClearTrack").get(_name2stateMap.get("BeanStateInconsistent")));
    }

    /*
     * Get icon by its localized bean state name
     */
    public NamedIcon getIcon(String status, int state) {
        log.debug("getIcon: status= {}, state= {}", status, state);
        HashMap<Integer, NamedIcon> map = _iconMaps.get(status);
        if (map == null) {
            return null;
        }
        return map.get(state);
    }

    public String getStateName(Integer state) {
        return _state2nameMap.get(state);
    }

    public String getStatus() {
        return _status;
    }

    @Override
    public int maxHeight() {
        int max = 0;
        if (_iconMaps != null) {
            for (HashMap<Integer, NamedIcon> integerNamedIconHashMap : _iconMaps.values()) {
                for (NamedIcon namedIcon : integerNamedIconHashMap.values()) {
                    max = Math.max(namedIcon.getIconHeight(), max);
                }
            }
        }
        return max;
    }

    @Override
    public int maxWidth() {
        int max = 0;
        if (_iconMaps != null) {
            for (HashMap<Integer, NamedIcon> integerNamedIconHashMap : _iconMaps.values()) {
                for (NamedIcon namedIcon : integerNamedIconHashMap.values()) {
                    max = Math.max(namedIcon.getIconWidth(), max);
                }
            }
        }
        return max;
    }

    /**
     * ****** popup AbstractAction.actionPerformed method overrides ********
     */
    @Override
    protected void rotateOrthogonal() {
        if (_iconMaps != null) {
            for (HashMap<Integer, NamedIcon> integerNamedIconHashMap : _iconMaps.values()) {
                for (NamedIcon icon : integerNamedIconHashMap.values()) {
                    icon.setRotation(icon.getRotation() + 1, this);
                }
            }
        }
        displayState(turnoutState());
    }

    @Override
    public void setScale(double s) {
        _scale = s;
        if (_iconMaps != null) {
            for (HashMap<Integer, NamedIcon> integerNamedIconHashMap : _iconMaps.values()) {
                for (NamedIcon namedIcon : integerNamedIconHashMap.values()) {
                    namedIcon.scale(s, this);
                }
            }
        }
        displayState(turnoutState());
    }

    @Override
    public void rotate(int deg) {
        if (_iconMaps != null) {
            for (HashMap<Integer, NamedIcon> integerNamedIconHashMap : _iconMaps.values()) {
                for (NamedIcon namedIcon : integerNamedIconHashMap.values()) {
                    namedIcon.rotate(deg, this);
                }
            }
        }
        setDegrees(deg %360);
        displayState(turnoutState());
    }

    /**
     * Drive the current state of the display from the state of the turnout and
     * status of track.
     */
    @Override
    public void displayState(int state) {
        if (getNamedTurnout() == null) {
            log.debug("Display state {}, disconnected", state);
        } else {
            if (_status != null && _iconMaps != null) {
                NamedIcon icon = getIcon(_status, state);
                if (icon != null) {
                    super.setIcon(icon);
                }
            }
        }
        super.displayState(state);
        updateSize();
    }

    @Override
    public String getNameString() {
        String str = "";
        if (namedOccBlock != null) {
            str = " in " + namedOccBlock.getBean().getDisplayName();
        } else if (namedOccSensor != null) {
            str = " on " + namedOccSensor.getBean().getDisplayName();
        }
        return "ITrack " + super.getNameString() + str;
    }

    // update icon as state of turnout changes and status of track changes
    // Override
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (log.isDebugEnabled()) {
            log.debug("property change: {} property \"{}\"= {} from {}", getNameString(), evt.getPropertyName(), evt.getNewValue(), evt.getSource().getClass().getName());
        }

        Object source = evt.getSource();
        if (source instanceof Turnout) {
            super.propertyChange(evt);
        } else if (source instanceof OBlock) {
            String property = evt.getPropertyName();
            if ("state".equals(property) || "pathState".equals(property)) {
                int now = (Integer) evt.getNewValue();
                setStatus((OBlock) source, now);
            } else if ("pathName".equals(property)) {
                _pathUtil.removePath((String) evt.getOldValue());
                _pathUtil.addPath((String) evt.getNewValue());
            }
        } else if (source instanceof Sensor) {
            if (evt.getPropertyName().equals("KnownState")) {
                int now = (Integer) evt.getNewValue();
                if (source.equals(getOccSensor())) {
                    _status = _pathUtil.getStatus(now);
                }
            }
        }
        displayState(turnoutState());
    }

    private void setStatus(OBlock block, int state) {
        _status = _pathUtil.getStatus(block, state);
        if ((state & (OBlock.OCCUPIED | OBlock.RUNNING)) != 0) {
            _pathUtil.setLocoIcon(block, getLocation(), getSize(), _editor);
            repaint();
        }
        if ((block.getState() & OBlock.OUT_OF_SERVICE) != 0) {
            setControlling(false);
        } else {
            setControlling(true);
        }
    }

    @Override
    protected void editItem() {
        _paletteFrame = makePaletteFrame(java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("IndicatorTO")));
        _itemPanel = new IndicatorTOItemPanel(_paletteFrame, "IndicatorTO", _iconFamily,
                PickListModel.turnoutPickModelInstance());
        ActionListener updateAction = a -> updateItem();
        // Convert _iconMaps state (ints) to Palette's bean names
        HashMap<String, HashMap<String, NamedIcon>> iconMaps
                = new HashMap<>();
        iconMaps.put("ClearTrack", new HashMap<>());
        iconMaps.put("OccupiedTrack", new HashMap<>());
        iconMaps.put("PositionTrack", new HashMap<>());
        iconMaps.put("AllocatedTrack", new HashMap<>());
        iconMaps.put("DontUseTrack", new HashMap<>());
        iconMaps.put("ErrorTrack", new HashMap<>());
        for (Entry<String, HashMap<Integer, NamedIcon>> entry : _iconMaps.entrySet()) {
            HashMap<String, NamedIcon> clone = iconMaps.get(entry.getKey());
            for (Entry<Integer, NamedIcon> ent : entry.getValue().entrySet()) {
                NamedIcon oldIcon = ent.getValue();
                NamedIcon newIcon = cloneIcon(oldIcon, this);
                newIcon.rotate(0, this);
                newIcon.scale(1.0, this);
                newIcon.setRotation(4, this);
                clone.put(_state2nameMap.get(ent.getKey()), newIcon);
            }
        }
        _itemPanel.initUpdate(updateAction, iconMaps);
        
        if (namedOccSensor != null) {
            _itemPanel.setOccDetector(namedOccSensor.getBean().getDisplayName());
        }
        if (namedOccBlock != null) {
            _itemPanel.setOccDetector(namedOccBlock.getBean().getDisplayName());
        }
        _itemPanel.setShowTrainName(_pathUtil.showTrain());
        _itemPanel.setPaths(_pathUtil.getPaths());
        _itemPanel.setSelection(getTurnout());  // do after all other params set - calls resize()
        
        initPaletteFrame(_paletteFrame, _itemPanel);
    }

    @Override
    void updateItem() {
        if (log.isDebugEnabled()) {
            log.debug("updateItem: {} family= {}", getNameString(), _itemPanel.getFamilyName());
        }
        setTurnout(_itemPanel.getTableSelection().getSystemName());
        setOccSensor(_itemPanel.getOccSensor());
        setOccBlock(_itemPanel.getOccBlock());
        _pathUtil.setShowTrain(_itemPanel.getShowTrainName());
        _iconFamily = _itemPanel.getFamilyName();
        _pathUtil.setPaths(_itemPanel.getPaths());
        HashMap<String, HashMap<String, NamedIcon>> iconMap = _itemPanel.getIconMaps();
        if (iconMap != null) {
            for (Entry<String, HashMap<String, NamedIcon>> entry : iconMap.entrySet()) {
                String status = entry.getKey();
                HashMap<Integer, NamedIcon> oldMap = _iconMaps.get(entry.getKey());
                for (Entry<String, NamedIcon> ent : entry.getValue().entrySet()) {
                    if (log.isDebugEnabled()) {
                        log.debug("key= {}", ent.getKey());
                    }
                    NamedIcon newIcon = cloneIcon(ent.getValue(), this);
                    NamedIcon oldIcon = oldMap.get(_name2stateMap.get(ent.getKey()));
                    newIcon.setLoad(oldIcon.getDegrees(), oldIcon.getScale(), this);
                    newIcon.setRotation(oldIcon.getRotation(), this);
                    setIcon(status, ent.getKey(), newIcon);
                }
            }
        }   // otherwise retain current map
        finishItemUpdate(_paletteFrame, _itemPanel);
        displayState(turnoutState());
    }

    @Override
    public void dispose() {
        if (namedOccSensor != null) {
            getOccSensor().removePropertyChangeListener(this);
        }
        if (namedOccBlock != null) {
            getOccBlock().removePropertyChangeListener(this);
        }
        namedOccSensor = null;
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(IndicatorTurnoutIcon.class);
}
