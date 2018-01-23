package jmri.jmrit.display;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import jmri.jmrit.catalog.NamedIcon;

/**
 * Gather common methods for Turnouts, Semsors, SignalHeads, Masts, etc.
 *
 * @author PeteCressman Copyright (C) 2011
 */
public class PositionableIcon extends PositionableLabel {

    protected HashMap<String, NamedIcon> _iconMap;
    protected String _iconFamily;
    protected double _scale = 1.0;          // getScale, come from net result found in one of the icons
    protected int _rotate = 0;

    public PositionableIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/misc/X-red.gif", "resources/icons/misc/X-red.gif"), editor);
    }

    public PositionableIcon(NamedIcon s, Editor editor) {
        // super ctor call to make sure this is an icon label
        super(s, editor);
    }

    public PositionableIcon(String s, Editor editor) {
        // super ctor call to make sure this is an icon label
        super(s, editor);
    }

    @Override
    public Positionable deepClone() {
        PositionableIcon pos = new PositionableIcon(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(PositionableIcon pos) {
        pos._iconFamily = _iconFamily;
        pos._scale = _scale;
        pos._rotate = _rotate;
        pos._iconMap = cloneMap(_iconMap, pos);
        return super.finishClone(pos);
    }

    /**
     * Get icon by its localized bean state name.
     *
     * @param state the state name
     * @return the icon or null if no match
     */
    public NamedIcon getIcon(String state) {
        return _iconMap.get(state);
    }

    public String getFamily() {
        return _iconFamily;
    }

    public void setFamily(String family) {
        _iconFamily = family;
    }

    public Iterator<String> getIconStateNames() {
        return _iconMap.keySet().iterator();
    }

    @Override
    public int maxHeight() {
        int max = super.maxHeight();
        if (_iconMap != null) {
            Iterator<NamedIcon> iter = _iconMap.values().iterator();
            while (iter.hasNext()) {
                max = Math.max(iter.next().getIconHeight(), max);
            }
        }
        return max;
    }

    @Override
    public int maxWidth() {
        int max = super.maxWidth();
        if (_iconMap != null) {
            Iterator<NamedIcon> iter = _iconMap.values().iterator();
            while (iter.hasNext()) {
                max = Math.max(iter.next().getIconWidth(), max);
            }
        }
        return max;
    }

    public void displayState(int state) {
    }

    /**
     * ****** popup AbstractAction method overrides ********
     */
    @Override
    protected void rotateOrthogonal() {
        Iterator<Entry<String, NamedIcon>> it = _iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            entry.getValue().setRotation(entry.getValue().getRotation() + 1, this);
        }
        updateSize();
    }

    @Override
    public void setScale(double s) {
        _scale = s;
        if (_iconMap == null) {
            return;
        }
        Iterator<Entry<String, NamedIcon>> it = _iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            entry.getValue().scale(s, this);
        }
        updateSize();
    }

    @Override
    public double getScale() {
        return _scale;
    }

    @Override
    public int getDegrees() {
        if (_text) {
            return super.getDegrees();
        }
        if (_iconMap != null) {
            Iterator<NamedIcon> it = _iconMap.values().iterator();
            if (it.hasNext()) {
                return it.next().getDegrees();
            }
        }
        return super.getDegrees();
    }

    @Override
    public void rotate(int deg) {
        _rotate = deg % 360;
        setDegrees(deg);
        if (_iconMap != null) {
            Iterator<Entry<String, NamedIcon>> it = _iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                entry.getValue().rotate(deg, this);
            }
        }
        super.rotate(deg);
        updateSize();
    }

    public static HashMap<String, NamedIcon> cloneMap(HashMap<String, NamedIcon> map,
            PositionableLabel pos) {
        HashMap<String, NamedIcon> clone = new HashMap<String, NamedIcon>();
        if (map != null) {
            Iterator<Entry<String, NamedIcon>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                clone.put(entry.getKey(), cloneIcon(entry.getValue(), pos));
            }
        }
        return clone;
    }

}
