package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.swing.JPopupMenu;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Sensor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.palette.IndicatorItemPanel;
import jmri.jmrit.logix.OBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display the status of a track segment in a block.
 * <p>
 * This responds to the following conditions: 1. KnownState of an occupancy
 * sensor of the block where the track segment appears 2. Allocation of a route
 * by a Warrant where the track segment appears 3. Current position of a train
 * being run under a Warrant where the track segment appears in a block of the
 * route 4. Out of Service for a block that cannot or should not be used 5. An
 * error state of the block where the track segment appears (short/no power
 * etc.)
 * <p>
 * A click on the icon does not change any of the above conditions.
 *
 * @author Pete Cressman Copyright (c) 2010
 */
public class IndicatorTrackIcon extends PositionableIcon
        implements java.beans.PropertyChangeListener, IndicatorTrack {

    private NamedBeanHandle<Sensor> namedOccSensor = null;
    private NamedBeanHandle<OBlock> namedOccBlock = null;

    private IndicatorTrackPaths _pathUtil;
    private IndicatorItemPanel _trackPanel;
    private String _status;     // is a key for _iconMap

    public IndicatorTrackIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(editor);
        _pathUtil = new IndicatorTrackPaths();
        _status = "ClearTrack";
        _iconMap = new HashMap<>();
    }

    @Override
    @Nonnull
    public Positionable deepClone() {
        IndicatorTrackIcon pos = new IndicatorTrackIcon(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(IndicatorTrackIcon pos) {
        pos.setOccSensorHandle(namedOccSensor);
        pos.setOccBlockHandle(namedOccBlock);
        pos._iconMap = cloneMap(_iconMap, pos);
        pos._pathUtil = _pathUtil.deepClone();
        pos._iconFamily = _iconFamily;
        pos._namedIcon = null;
        pos._status = _status;
        return super.finishClone(pos);
    }

    public HashMap<String, NamedIcon> getIconMap() {
        return cloneMap(_iconMap, this);
    }

    /**
     * Attach a named sensor to display status.
     *
     * @param pName Used as a system/user name to lookup the sensor object
     */
    @Override
    public void setOccSensor(String pName) {
        if (pName == null || pName.trim().isEmpty()) {
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
    public void setOccSensorHandle(NamedBeanHandle<Sensor> senHandle) {
        if (namedOccSensor != null) {
            getOccSensor().removePropertyChangeListener(this);
        }
        namedOccSensor = senHandle;
        if (namedOccSensor != null) {
            if (_iconMap == null) {
                _iconMap = new HashMap<>();
            }
            Sensor sensor = getOccSensor();
            sensor.addPropertyChangeListener(this, namedOccSensor.getName(), "Indicator Track");
            _status = _pathUtil.getStatus(sensor.getKnownState());
            displayState(_status);
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
     * Attach a named OBlock to display status.
     *
     * @param pName Used as a system/user name to look up the OBlock object
     */
    @Override
    public void setOccBlock(String pName) {
        if (pName == null || pName.trim().isEmpty()) {
            setOccBlockHandle(null);
            return;
        }
        OBlock block = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock(pName);
        if (block != null) {
            setOccBlockHandle(InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(pName, block));
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
            if (_iconMap == null) {
                _iconMap = new HashMap<>();
            }
            OBlock block = getOccBlock();
            block.addPropertyChangeListener(this, namedOccBlock.getName(), "Indicator Track");
            setStatus(block, block.getState());
            displayState(_status);
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

    /*
     * Place icon by its bean state name
     */
    public void setIcon(String name, NamedIcon icon) {
        log.debug("set \"{}\" icon= {}", name, icon);
        _iconMap.put(name, icon);
        if (_status.equals(name)) {
            setIcon(icon);            
        }
    }

    public String getStatus() {
        return _status;
    }

    @Override
    public int maxHeight() {
        int max = 0;
        Iterator<NamedIcon> iter = _iconMap.values().iterator();
        while (iter.hasNext()) {
            max = Math.max(iter.next().getIconHeight(), max);
        }
        return max;
    }

    @Override
    public int maxWidth() {
        int max = 0;
        Iterator<NamedIcon> iter = _iconMap.values().iterator();
        while (iter.hasNext()) {
            max = Math.max(iter.next().getIconWidth(), max);
        }
        return max;
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (log.isDebugEnabled()) {
            log.debug("property change: {} property {} is now {} from {}", getNameString(), evt.getPropertyName(),
                    evt.getNewValue(), evt.getSource().getClass().getName());
        }

        Object source = evt.getSource();
        if (source instanceof OBlock) {
            String property = evt.getPropertyName();
            if ("state".equals(property) || "pathState".equals(property)) {
                int now = ((Integer) evt.getNewValue());
                setStatus((OBlock) source, now);
            } else if ("pathName".equals(property)) {
                _pathUtil.removePath((String) evt.getOldValue());
                _pathUtil.addPath((String) evt.getNewValue());
            }
        } else if (source instanceof Sensor) {
            if (evt.getPropertyName().equals("KnownState")) {
                int now = ((Integer) evt.getNewValue());
                if (source.equals(getOccSensor())) {
                    _status = _pathUtil.getStatus(now);
                }
            }
        }
        displayState(_status);
    }

    private void setStatus(OBlock block, int state) {
        _status = _pathUtil.getStatus(block, state);
        if ((state & (OBlock.OCCUPIED | OBlock.RUNNING)) != 0) {
            // It is rather unpleasant that the following needs to be done in a try-catch, but exceptions have been observed
            try {
                _pathUtil.setLocoIcon(block, getLocation(), getSize(), _editor);
            } catch (Exception e) {
                log.error("setStatus on indicator track icon failed in thread {} {}: ",
                    Thread.currentThread().getName(), Thread.currentThread().getId(), e);
            }
        }
        repaint();
        if ((block.getState() & OBlock.OUT_OF_SERVICE) != 0) {
            setControlling(false);
        } else {
            setControlling(true);
        }
    }

    @Override
    @Nonnull
    public String getNameString() {
        String str = "";
        if (namedOccBlock != null) {
            str = "in " + namedOccBlock.getBean().getDisplayName();
        } else if (namedOccSensor != null) {
            str = "on " + namedOccSensor.getBean().getDisplayName();
        }
        return "ITrack " + str;
    }

    /**
     * Pop-up displays unique attributes.
     */
    @Override
    public boolean showPopUp(JPopupMenu popup) {
        return false;
    }

    /*
     * Drive the current state of the display from the status.
     */
    public void displayState(String status) {
        log.debug("{} displayStatus {}", getNameString(), _status);
        NamedIcon icon = getIcon(status);
        if (icon != null) {
            super.setIcon(icon);
        }
        updateSize();
    }
    
    @Override
    public void rotate(int deg) {
        super.rotate(deg);
        displayState(_status);
    }

    @Override
    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("IndicatorTrack"));
        popup.add(new javax.swing.AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                editItem();
            }
        });
        return true;
    }

    protected void editItem() {
        _paletteFrame = makePaletteFrame(java.text.MessageFormat.format(Bundle.getMessage("EditItem"),
                Bundle.getMessage("IndicatorTrack")));
        _trackPanel = new IndicatorItemPanel(_paletteFrame, "IndicatorTrack", _iconFamily, _editor);

        ActionListener updateAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                updateItem();
            }
        };
        // duplicate _iconMap map with unscaled and unrotated icons
        HashMap<String, NamedIcon> map = new HashMap<>();

        for (Entry<String, NamedIcon> entry : _iconMap.entrySet()) {
            NamedIcon oldIcon = entry.getValue();
            NamedIcon newIcon = cloneIcon(oldIcon, this);
            newIcon.rotate(0, this);
            newIcon.scale(1.0, this);
            newIcon.setRotation(4, this);
            map.put(entry.getKey(), newIcon);
        }
        _trackPanel.init(updateAction, map);
        if (namedOccSensor != null) {
            _trackPanel.setOccDetector(namedOccSensor.getBean().getDisplayName());
        }
        if (namedOccBlock != null) {
            _trackPanel.setOccDetector(namedOccBlock.getBean().getDisplayName());
        }
        _trackPanel.setShowTrainName(_pathUtil.showTrain());
        _trackPanel.setPaths(_pathUtil.getPaths());
        initPaletteFrame(_paletteFrame, _trackPanel);
    }

    private void updateItem() {
        setOccSensor(_trackPanel.getOccSensor());
        setOccBlock(_trackPanel.getOccBlock());
        _pathUtil.setShowTrain(_trackPanel.getShowTrainName());
        _iconFamily = _trackPanel.getFamilyName();
        _pathUtil.setPaths(_trackPanel.getPaths());
        HashMap<String, NamedIcon> iconMap = _trackPanel.getIconMap();
        if (iconMap != null) {
            HashMap<String, NamedIcon> oldMap = cloneMap(_iconMap, this);
            for (Entry<String, NamedIcon> entry : _iconMap.entrySet()) {
                if (log.isDebugEnabled()) {
                    log.debug("key= {}", entry.getKey());
                }
                NamedIcon newIcon = entry.getValue();
                NamedIcon oldIcon = oldMap.get(entry.getKey());
                newIcon.setLoad(oldIcon.getDegrees(), oldIcon.getScale(), this);
                newIcon.setRotation(oldIcon.getRotation(), this);
                setIcon(entry.getKey(), newIcon);
            }
        }   // otherwise retain current map
        finishItemUpdate(_paletteFrame, _trackPanel);
        displayState(_status);
    }

    @Override
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

    @Override
    public jmri.NamedBean getNamedBean() {
        if (namedOccBlock != null) {
            return namedOccBlock.getBean();
        } else if (namedOccSensor != null) {
            return namedOccSensor.getBean();
        }
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(IndicatorTrackIcon.class);

}
