package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
    private IndicatorTOItemPanel _TOPanel;
    private String _status;

    public IndicatorTurnoutIcon(Editor editor) {
        super(editor);
        log.debug("IndicatorTurnoutIcon ctor: isIcon()= " + isIcon() + ", isText()= " + isText());
        _pathUtil = new IndicatorTrackPaths();
        _status = "DontUseTrack";
        _iconMaps = initMaps();

    }

    static HashMap<String, HashMap<Integer, NamedIcon>> initMaps() {
        HashMap<String, HashMap<Integer, NamedIcon>> iconMaps = new HashMap<>();
        iconMaps.put("ClearTrack", new HashMap<Integer, NamedIcon>());
        iconMaps.put("OccupiedTrack", new HashMap<Integer, NamedIcon>());
        iconMaps.put("PositionTrack", new HashMap<Integer, NamedIcon>());
        iconMaps.put("AllocatedTrack", new HashMap<Integer, NamedIcon>());
        iconMaps.put("DontUseTrack", new HashMap<Integer, NamedIcon>());
        iconMaps.put("ErrorTrack", new HashMap<Integer, NamedIcon>());
        return iconMaps;
    }

    HashMap<String, HashMap<Integer, NamedIcon>> cloneMaps(IndicatorTurnoutIcon pos) {
        HashMap<String, HashMap<Integer, NamedIcon>> iconMaps = initMaps();
        Iterator<Entry<String, HashMap<Integer, NamedIcon>>> it = _iconMaps.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, HashMap<Integer, NamedIcon>> entry = it.next();
            HashMap<Integer, NamedIcon> clone = iconMaps.get(entry.getKey());
            Iterator<Entry<Integer, NamedIcon>> iter = entry.getValue().entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Integer, NamedIcon> ent = iter.next();
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
                log.error("Occupancy Sensor '" + pName + "' not available, icon won't see changes");
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
            log.error("Detection OBlock '" + pName + "' not available, icon won't see changes");
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
            log.debug("setIcon for status \"" + status + "\", stateName= \""
                    + stateName + " icom= " + icon.getURL());
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
        log.debug("getIcon: status= " + status + ", state= " + state);
        HashMap<Integer, NamedIcon> map = _iconMaps.get(status);
        if (map == null) {
            return null;
        }
        return map.get(Integer.valueOf(state));
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
            Iterator<HashMap<Integer, NamedIcon>> it = _iconMaps.values().iterator();
            while (it.hasNext()) {
                Iterator<NamedIcon> iter = it.next().values().iterator();
                while (iter.hasNext()) {
                    max = Math.max(iter.next().getIconHeight(), max);
                }
            }
        }
        return max;
    }

    @Override
    public int maxWidth() {
        int max = 0;
        if (_iconMaps != null) {
            Iterator<HashMap<Integer, NamedIcon>> it = _iconMaps.values().iterator();
            while (it.hasNext()) {
                Iterator<NamedIcon> iter = it.next().values().iterator();
                while (iter.hasNext()) {
                    max = Math.max(iter.next().getIconWidth(), max);
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
            Iterator<HashMap<Integer, NamedIcon>> it = _iconMaps.values().iterator();
            while (it.hasNext()) {
                Iterator<NamedIcon> iter = it.next().values().iterator();
                while (iter.hasNext()) {
                    NamedIcon icon = iter.next();
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
            Iterator<HashMap<Integer, NamedIcon>> it = _iconMaps.values().iterator();
            while (it.hasNext()) {
                Iterator<NamedIcon> iter = it.next().values().iterator();
                while (iter.hasNext()) {
                    iter.next().scale(s, this);
                }
            }
        }
        displayState(turnoutState());
    }

    @Override
    public void rotate(int deg) {
        if (_iconMaps != null) {
            Iterator<HashMap<Integer, NamedIcon>> it = _iconMaps.values().iterator();
            while (it.hasNext()) {
                Iterator<NamedIcon> iter = it.next().values().iterator();
                while (iter.hasNext()) {
                    iter.next().rotate(deg, this);
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
            log.debug("Display state " + state + ", disconnected");
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
            log.debug("property change: " + getNameString() + " property \"" + evt.getPropertyName() + "\"= "
                    + evt.getNewValue() + " from " + evt.getSource().getClass().getName());
        }

        Object source = evt.getSource();
        if (source instanceof Turnout) {
            super.propertyChange(evt);
        } else if (source instanceof OBlock) {
            String property = evt.getPropertyName();
            if ("state".equals(property) || "pathState".equals(property)) {
                int now = ((Integer) evt.getNewValue()).intValue();
                setStatus((OBlock) source, now);
            } else if ("pathName".equals(property)) {
                _pathUtil.removePath((String) evt.getOldValue());
                _pathUtil.addPath((String) evt.getNewValue());
            }
        } else if (source instanceof Sensor) {
            if (evt.getPropertyName().equals("KnownState")) {
                int now = ((Integer) evt.getNewValue()).intValue();
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
        _TOPanel = new IndicatorTOItemPanel(_paletteFrame, "IndicatorTO", _iconFamily,
                PickListModel.turnoutPickModelInstance(), _editor);
        ActionListener updateAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                updateItem();
            }
        };
        // Convert _iconMaps state (ints) to Palette's bean names
        HashMap<String, HashMap<String, NamedIcon>> iconMaps
                = new HashMap<>();
        iconMaps.put("ClearTrack", new HashMap<String, NamedIcon>());
        iconMaps.put("OccupiedTrack", new HashMap<String, NamedIcon>());
        iconMaps.put("PositionTrack", new HashMap<String, NamedIcon>());
        iconMaps.put("AllocatedTrack", new HashMap<String, NamedIcon>());
        iconMaps.put("DontUseTrack", new HashMap<String, NamedIcon>());
        iconMaps.put("ErrorTrack", new HashMap<String, NamedIcon>());
        Iterator<Entry<String, HashMap<Integer, NamedIcon>>> it = _iconMaps.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, HashMap<Integer, NamedIcon>> entry = it.next();
            HashMap<String, NamedIcon> clone = iconMaps.get(entry.getKey());
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
        if (namedOccSensor != null) {
            _TOPanel.setOccDetector(namedOccSensor.getBean().getDisplayName());
        }
        if (namedOccBlock != null) {
            _TOPanel.setOccDetector(namedOccBlock.getBean().getDisplayName());
        }
        _TOPanel.setShowTrainName(_pathUtil.showTrain());
        _TOPanel.setPaths(_pathUtil.getPaths());
        initPaletteFrame(_paletteFrame, _TOPanel);
    }

    @Override
    void updateItem() {
        if (log.isDebugEnabled()) {
            log.debug("updateItem: " + getNameString() + " family= " + _TOPanel.getFamilyName());
        }
        setTurnout(_TOPanel.getTableSelection().getSystemName());
        setOccSensor(_TOPanel.getOccSensor());
        setOccBlock(_TOPanel.getOccBlock());
        _pathUtil.setShowTrain(_TOPanel.getShowTrainName());
        _iconFamily = _TOPanel.getFamilyName();
        _pathUtil.setPaths(_TOPanel.getPaths());
        HashMap<String, HashMap<String, NamedIcon>> iconMap = _TOPanel.getIconMaps();
        if (iconMap != null) {
            Iterator<Entry<String, HashMap<String, NamedIcon>>> it = iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, HashMap<String, NamedIcon>> entry = it.next();
                String status = entry.getKey();
                HashMap<Integer, NamedIcon> oldMap = _iconMaps.get(entry.getKey());
                Iterator<Entry<String, NamedIcon>> iter = entry.getValue().entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, NamedIcon> ent = iter.next();
                    if (log.isDebugEnabled()) {
                        log.debug("key= " + ent.getKey());
                    }
                    NamedIcon newIcon = cloneIcon(ent.getValue(), this);
                    NamedIcon oldIcon = oldMap.get(_name2stateMap.get(ent.getKey()));
                    newIcon.setLoad(oldIcon.getDegrees(), oldIcon.getScale(), this);
                    newIcon.setRotation(oldIcon.getRotation(), this);
                    setIcon(status, ent.getKey(), newIcon);
                }
            }
        }   // otherwise retain current map
        finishItemUpdate(_paletteFrame, _TOPanel);
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
