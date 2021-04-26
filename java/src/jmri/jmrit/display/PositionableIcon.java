package jmri.jmrit.display;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import jmri.jmrit.catalog.NamedIcon;

/**
 * Gather common methods for Turnouts, Sensors, SignalHeads, Masts, etc.
 *
 * <a href="doc-files/Heirarchy.png"><img src="doc-files/Heirarchy.png" alt="UML class diagram for package" height="33%" width="33%"></a>
 * @author Pete Cressman Copyright (C) 2011
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

    public Collection<String> getStateNameCollection() {
        log.error("getStateNameCollection() must be implemented by extensions!");
        return null;
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

    public HashMap<String, NamedIcon> getIconMap() {
        return cloneMap(_iconMap, this);
    }

    @Override
    public int maxHeight() {
        int max = super.maxHeight();
        if (_iconMap != null) {
            for (NamedIcon namedIcon : _iconMap.values()) {
                max = Math.max(namedIcon.getIconHeight(), max);
            }
        }
        return max;
    }

    @Override
    public int maxWidth() {
        int max = super.maxWidth();
        if (_iconMap != null) {
            for (NamedIcon namedIcon : _iconMap.values()) {
                max = Math.max(namedIcon.getIconWidth(), max);
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
        for (Entry<String, NamedIcon> entry : _iconMap.entrySet()) {
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
        for (Entry<String, NamedIcon> entry : _iconMap.entrySet()) {
            entry.getValue().scale(s, this);
        }
        updateSize();
    }

    @Override
    public double getScale() {
        return _scale;
    }

    @Override
    public void rotate(int deg) {
        _rotate = deg % 360;
        setDegrees(deg);
        if (_iconMap != null) {
            for (Entry<String, NamedIcon> entry : _iconMap.entrySet()) {
                entry.getValue().rotate(deg, this);
            }
        }
        super.rotate(deg);
        updateSize();
    }

    public static HashMap<String, NamedIcon> cloneMap(HashMap<String, NamedIcon> map,
            PositionableLabel pos) {
        HashMap<String, NamedIcon> clone = new HashMap<>();
        if (map != null) {
            for (Entry<String, NamedIcon> entry : map.entrySet()) {
                clone.put(entry.getKey(), cloneIcon(entry.getValue(), pos));
            }
        }
        return clone;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PositionableIcon.class);
}
