package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.palette.MultiSensorItemPanel;
import jmri.jmrit.picker.PickListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display a status of set of Sensors.
 * <p>
 * Each sensor has an associated image. Normally, only one sensor will be active
 * at a time, and in that case the associated image will be shown. If more than
 * one is active, one of the corresponding images will be shown, but which one
 * is not guaranteed.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2007
 */
public class MultiSensorIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    String _iconFamily;

    public MultiSensorIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                "resources/icons/smallschematics/tracksegments/circuit-error.gif"), editor);
        _control = true;
        displayState();
        setPopupUtility(null);
    }

    boolean updown = false;

    // if not updown, is rightleft
    public void setUpDown(boolean b) {
        updown = b;
    }

    public boolean getUpDown() {
        return updown;
    }

    ArrayList<Entry> entries = new ArrayList<>();

    @Override
    public Positionable deepClone() {
        MultiSensorIcon pos = new MultiSensorIcon(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(MultiSensorIcon pos) {
        pos.setInactiveIcon(cloneIcon(getInactiveIcon(), pos));
        pos.setInconsistentIcon(cloneIcon(getInconsistentIcon(), pos));
        pos.setUnknownIcon(cloneIcon(getUnknownIcon(), pos));
        for (int i = 0; i < entries.size(); i++) {
            pos.addEntry(getSensorName(i), cloneIcon(getSensorIcon(i), pos));
        }
        return super.finishClone(pos);
    }

    public void addEntry(NamedBeanHandle<Sensor> sensor, NamedIcon icon) {
        if (sensor != null) {
            if (log.isDebugEnabled()) {
                log.debug("addEntry: sensor= {}", sensor.getName());
            }
            Entry e = new Entry();
            sensor.getBean().addPropertyChangeListener(this, sensor.getName(), "MultiSensor Icon");
            e.namedSensor = sensor;
            e.icon = icon;
            entries.add(e);
            displayState();
        } else {
            log.error("Sensor not available, icon won't see changes");
        }
    }

    public void addEntry(String pName, NamedIcon icon) {
        NamedBeanHandle<Sensor> sensor;
        if (InstanceManager.getNullableDefault(jmri.SensorManager.class) != null) {
            sensor = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class)
                    .getNamedBeanHandle(pName, InstanceManager.sensorManagerInstance().provideSensor(pName));
            addEntry(sensor, icon);
        } else {
            log.error("No SensorManager for this protocol, icon won't see changes");
        }
    }

    public int getNumEntries() {
        return entries.size();
    }

    public List<Sensor> getSensors() {
        ArrayList<Sensor> list = new ArrayList<>(getNumEntries());
        for (Entry handle : entries) {
            list.add(handle.namedSensor.getBean());
        }
        return list;
    }

    public String getSensorName(int i) {
        return entries.get(i).namedSensor.getName();
    }

    public NamedIcon getSensorIcon(int i) {
        return entries.get(i).icon;
    }

    public String getFamily() {
        return _iconFamily;
    }

    public void setFamily(String family) {
        _iconFamily = family;
    }

    // display icons
    String inactiveName = "resources/icons/USS/plate/levers/l-inactive.gif";
    NamedIcon inactive = new NamedIcon(inactiveName, inactiveName);

    String inconsistentName = "resources/icons/USS/plate/levers/l-inconsistent.gif";
    NamedIcon inconsistent = new NamedIcon(inconsistentName, inconsistentName);

    String unknownName = "resources/icons/USS/plate/levers/l-unknown.gif";
    NamedIcon unknown = new NamedIcon(unknownName, unknownName);

    public NamedIcon getInactiveIcon() {
        return inactive;
    }

    public void setInactiveIcon(NamedIcon i) {
        inactive = i;
    }

    public NamedIcon getInconsistentIcon() {
        return inconsistent;
    }

    public void setInconsistentIcon(NamedIcon i) {
        inconsistent = i;
    }

    public NamedIcon getUnknownIcon() {
        return unknown;
    }

    public void setUnknownIcon(NamedIcon i) {
        unknown = i;
    }

    // update icon as state of turnout changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            String prop = e.getPropertyName();
            Sensor sen = (Sensor) e.getSource();
            log.debug("property change({}) Sensor state= {} - old= {}, new= {}",
                    prop, sen.getKnownState(), e.getOldValue(), e.getNewValue());
        }
        if (e.getPropertyName().equals("KnownState")) {
            displayState();
            _editor.repaint();
        }
    }

    @Override
    public String getNameString() {
        StringBuilder name = new StringBuilder();
        if ((entries == null) || (entries.size() < 1)) {
            name.append(Bundle.getMessage("NotConnected"));
        } else {
            name.append(entries.get(0).namedSensor.getName());
            entries.forEach((entry) -> {
                name.append(",").append(entry.namedSensor.getName());
            });
        }
        return name.toString();
    }

    /**
     * ****** popup AbstractAction.actionPerformed method overrides ********
     */
    @Override
    protected void rotateOrthogonal() {
        for (int i = 0; i < entries.size(); i++) {
            NamedIcon icon = entries.get(i).icon;
            icon.setRotation(icon.getRotation() + 1, this);
        }
        inactive.setRotation(inactive.getRotation() + 1, this);
        unknown.setRotation(unknown.getRotation() + 1, this);
        inconsistent.setRotation(inconsistent.getRotation() + 1, this);
        displayState();
        // bug fix, must repaint icons that have same width and height
        repaint();
    }

    @Override
    public void setScale(double s) {
        for (int i = 0; i < entries.size(); i++) {
            NamedIcon icon = entries.get(i).icon;
            icon.scale(s, this);
        }
        inactive.scale(s, this);
        unknown.scale(s, this);
        inconsistent.scale(s, this);
        displayState();
    }

    @Override
    public void rotate(int deg) {
        for (int i = 0; i < entries.size(); i++) {
            NamedIcon icon = entries.get(i).icon;
            icon.rotate(deg, this);
        }
        inactive.rotate(deg, this);
        unknown.rotate(deg, this);
        inconsistent.rotate(deg, this);
        displayState();
    }

    @Override
    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = Bundle.getMessage("EditItem", Bundle.getMessage("MultiSensor"));
        popup.add(new javax.swing.AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                editItem();
            }
        });
        return true;
    }

    MultiSensorItemPanel _itemPanel;

    protected void editItem() {
        _paletteFrame = makePaletteFrame(Bundle.getMessage("EditItem", Bundle.getMessage("MultiSensor")));
        _itemPanel = new MultiSensorItemPanel(_paletteFrame, "MultiSensor", _iconFamily,
                PickListModel.multiSensorPickModelInstance(), _editor);
        ActionListener updateAction = (ActionEvent a) -> {
            updateItem();
        };
        // duplicate _iconMap map with unscaled and unrotated icons
        HashMap<String, NamedIcon> map = new HashMap<>();
        map.put("SensorStateInactive", inactive);
        map.put("BeanStateInconsistent", inconsistent);
        map.put("BeanStateUnknown", unknown);
        for (int i = 0; i < entries.size(); i++) {
            map.put(MultiSensorItemPanel.getPositionName(i), entries.get(i).icon);
        }
        _itemPanel.init(updateAction, map);
        for (int i = 0; i < entries.size(); i++) {
            _itemPanel.setSelection(entries.get(i).namedSensor.getBean());
        }
        _itemPanel.setUpDown(getUpDown());
        initPaletteFrame(_paletteFrame, _itemPanel);
    }

    void updateItem() {
        if (!_itemPanel.oktoUpdate()) {
            return;
        }
        HashMap<String, NamedIcon> iconMap = _itemPanel.getIconMap();
        ArrayList<Sensor> selections = _itemPanel.getTableSelections();
        setInactiveIcon(new NamedIcon(iconMap.get("SensorStateInactive")));
        setInconsistentIcon(new NamedIcon(iconMap.get("BeanStateInconsistent")));
        setUnknownIcon(new NamedIcon(iconMap.get("BeanStateUnknown")));
        entries = new ArrayList<>(selections.size());
        for (int i = 0; i < selections.size(); i++) {
            addEntry(selections.get(i).getDisplayName(), new NamedIcon(iconMap.get(MultiSensorItemPanel.getPositionName(i))));
        }
        _iconFamily = _itemPanel.getFamilyName();
        _itemPanel.clearSelections();
        setUpDown(_itemPanel.getUpDown());
        finishItemUpdate(_paletteFrame, _itemPanel);
    }

    @Override
    public boolean setEditIconMenu(JPopupMenu popup) {
        String txt = Bundle.getMessage("EditItem", Bundle.getMessage("MultiSensor"));
        popup.add(new AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit();
            }
        });
        return true;
    }

    @Override
    protected void edit() {
        MultiSensorIconAdder iconEditor = new MultiSensorIconAdder("MultiSensor");
        makeIconEditorFrame(this, "MultiSensor", false, iconEditor);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.sensorPickModelInstance());
        _iconEditor.setIcon(2, "SensorStateInactive", inactive);
        _iconEditor.setIcon(0, "BeanStateInconsistent", inconsistent);
        _iconEditor.setIcon(1, "BeanStateUnknown", unknown);
        if (_iconEditor instanceof MultiSensorIconAdder) {
            ((MultiSensorIconAdder) _iconEditor).setMultiIcon(entries);
            _iconEditor.makeIconPanel(false);

            ActionListener addIconAction = (ActionEvent a) -> {
                updateSensor();
            };
            iconEditor.complete(addIconAction, true, true, true);
        }
    }

    void updateSensor() {
        if (_iconEditor instanceof MultiSensorIconAdder) {
            MultiSensorIconAdder iconEditor = (MultiSensorIconAdder) _iconEditor;
            setInactiveIcon(iconEditor.getIcon("SensorStateInactive"));
            setInconsistentIcon(iconEditor.getIcon("BeanStateInconsistent"));
            setUnknownIcon(iconEditor.getIcon("BeanStateUnknown"));
            for (int i = 0; i < entries.size(); i++) {
                entries.get(i).namedSensor.getBean().removePropertyChangeListener(this);
            }
            int numPositions = iconEditor.getNumIcons();
            entries = new ArrayList<>(numPositions);
            for (int i = 3; i < numPositions; i++) {
                NamedIcon icon = iconEditor.getIcon(i);
                NamedBeanHandle<Sensor> namedSensor = iconEditor.getSensor(i);
                addEntry(namedSensor, icon);
            }
            setUpDown(iconEditor.getUpDown());
        }
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }
    /**
     * *********** end popup action methods ***************
     */

    int displaying = -1;

    /**
     * Drive the current state of the display from the state of the turnout.
     */
    public void displayState() {

        updateSize();

        // run the entries
        boolean foundActive = false;

        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);

            int state = e.namedSensor.getBean().getKnownState();

            switch (state) {
                case Sensor.ACTIVE:
                    if (isText()) {
                        super.setText(Bundle.getMessage("SensorStateActive"));
                    }
                    if (isIcon()) {
                        super.setIcon(e.icon);
                    }
                    foundActive = true;
                    displaying = i;
                    break;  // look at the next ones too
                case Sensor.UNKNOWN:
                    if (isText()) {
                        super.setText(Bundle.getMessage("BeanStateUnknown"));
                    }
                    if (isIcon()) {
                        super.setIcon(unknown);
                    }
                    return;  // this trumps all others
                case Sensor.INCONSISTENT:
                    if (isText()) {
                        super.setText(Bundle.getMessage("BeanStateInconsistent"));
                    }
                    if (isIcon()) {
                        super.setIcon(inconsistent);
                    }
                    break;
                default:
                    break;
            }
        }
        // loop has gotten to here
        if (foundActive) {
            return;  // set active
        }        // only case left is all inactive
        if (isText()) {
            super.setText(Bundle.getMessage("SensorStateInactive"));
        }
        if (isIcon()) {
            super.setIcon(inactive);
        }
    }

    // Use largest size. If icons are not same size, 
    // this can result in drawing artifacts.
    @Override
    public int maxHeight() {
        int size = Math.max(
                ((inactive != null) ? inactive.getIconHeight() : 0),
                Math.max((unknown != null) ? unknown.getIconHeight() : 0,
                        (inconsistent != null) ? inconsistent.getIconHeight() : 0)
        );
        if (entries != null) {
            for (int i = 0; i < entries.size(); i++) {
                size = Math.max(size, entries.get(i).icon.getIconHeight());
            }
        }
        return size;
    }

    // Use largest size. If icons are not same size, 
    // this can result in drawing artifacts.
    @Override
    public int maxWidth() {
        int size = Math.max(
                ((inactive != null) ? inactive.getIconWidth() : 0),
                Math.max((unknown != null) ? unknown.getIconWidth() : 0,
                        (inconsistent != null) ? inconsistent.getIconWidth() : 0)
        );
        if (entries != null) {
            for (int i = 0; i < entries.size(); i++) {
                size = Math.max(size, entries.get(i).icon.getIconWidth());
            }
        }
        return size;
    }

    public void performMouseClicked(java.awt.event.MouseEvent e, int xx, int yy) {
        if (log.isDebugEnabled()) {
            log.debug("performMouseClicked: location ({}, {}), click from ({}, {}) displaying={}",
                    getX(), getY(), xx, yy, displaying);
        }
        if (!buttonLive() || (entries == null || entries.size() < 1)) {
            if (log.isDebugEnabled()) {
                log.debug("performMouseClicked: buttonLive={}, entries={}", buttonLive(), entries.size());
            }
            return;
        }

        // find if we want to increment or decrement
        // regardless of the zooming scale, (getX(), getY()) is the un-zoomed position in _editor._contents
        // but the click is at the zoomed position
        double ratio = _editor.getPaintScale();
        boolean dec = false;
        if (updown) {
            if ((yy/ratio - getY()) > (double)(maxHeight()) / 2) {
                dec = true;
            }
        } else {
           if ((xx/ratio - getX()) < (double)(maxWidth()) / 2) {
                dec = true;
            }
        }

        // get new index
        int next;
        if (dec) {
            next = displaying - 1;
        } else {
            next = displaying + 1;
        }
        if (next < 0) {
            next = 0;
        }
        if (next >= entries.size()) {
            next = entries.size() - 1;
        }

        int drop = displaying;
        if (log.isDebugEnabled()) {
            log.debug("dec= {} displaying={} next= {}", dec, displaying, next);
        }
        try {
            entries.get(next).namedSensor.getBean().setKnownState(Sensor.ACTIVE);
            if (drop >= 0 && drop != next) {
                entries.get(drop).namedSensor.getBean().setKnownState(Sensor.INACTIVE);
            }
        } catch (jmri.JmriException ex) {
            log.error("Click failed to set sensor: ", ex);
        }
    }

    boolean buttonLive() {
        return _editor.getFlag(Editor.OPTION_CONTROLS, isControlling());
    }

    @Override
    public void doMouseClicked(MouseEvent e) {
        if (!e.isAltDown() && !e.isMetaDown()) {
            performMouseClicked(e, e.getX(), e.getY());
        }
    }

    @Override
    public void dispose() {
        // remove listeners
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).namedSensor.getBean()
                    .removePropertyChangeListener(this);
        }
        super.dispose();
    }

    static class Entry {

        NamedBeanHandle<Sensor> namedSensor;
        NamedIcon icon;
    }

    private final static Logger log = LoggerFactory.getLogger(MultiSensorIcon.class);

}
