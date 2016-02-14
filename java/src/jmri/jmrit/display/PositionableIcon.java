// PositionableIcon.java
package jmri.jmrit.display;

/**
 * Gather common methods for Turnouts, Semsors, SignalHeads, Masts, etc.
 *
 * @author PeteCressman Copyright (C) 2011
 * @version $Revision$
 */
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import jmri.jmrit.catalog.NamedIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PositionableIcon extends PositionableLabel {
    private static final long serialVersionUID = 5192041937901708011L;
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

    public Positionable finishClone(Positionable p) {
        PositionableIcon pos = (PositionableIcon) p;
        pos._iconFamily = _iconFamily;
        pos._scale = _scale;
        pos._rotate = _rotate;
        pos._iconMap = cloneMap(_iconMap, pos);
        return super.finishClone(pos);
    }

    /**
     * Get icon by its bean state name key found in
     * jmri.NamedBeanBundle.properties Get icon by its localized bean state name
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
    protected void rotateOrthogonal() {
        Iterator<Entry<String, NamedIcon>> it = _iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            entry.getValue().setRotation(entry.getValue().getRotation() + 1, this);
        }
        updateSize();
    }

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

    private final static Logger log = LoggerFactory.getLogger(PositionableIcon.class.getName());
}
