package jmri.jmrit.display.layoutEditor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrit.catalog.NamedIcon;
import jmri.Reportable;

/**
 * An icon to display a status of a GlobalVariable.
 *
 * This is the same name as display.GlobalVariableIcon, but a very
 * separate class. That's not good. Unfortunately, it's too
 * hard to disentangle that now because it's resident in the
 * panel file that have been written out, so we just annotated
 * the fact, but now we want to leave it on the list to fix.
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification="Cannot rename for user data compatiblity reasons.")
public class GlobalVariableIcon extends jmri.jmrit.display.GlobalVariableIcon {

    private final String defaultText = " ";

    public GlobalVariableIcon(String s, LayoutEditor panel) {
        super(s, panel);
        log.debug("GlobalVariableIcon ctor= {}", GlobalVariableIcon.class.getName());
    }

    @Override
    public void setText(String text) {
        if (text == null || text.isEmpty()) {
            super.setText(defaultText);
        } else {
            super.setText(text);
        }
    }

    @Override
    public void displayState() {
        log.debug("displayState");
        if (getGlobalVariable() == null) {  // use default if not connected yet
            setText(defaultText);
            updateSize();
            return;
        }

        Object key = getGlobalVariable().getValue();
        if (key != null) {
            java.util.HashMap<String, NamedIcon> map = getMap();
            if (map == null) {
                // no map, attempt to show object directly
                Object val = key;

                if (val instanceof String) {
                    if (((String)val).isEmpty()) {
                        setText(defaultText);
                    } else {
                        setText((String) val);
                    }
                    setIcon(null);
                    _text = true;
                    _icon = false;
                    setAttributes(getPopupUtility(), this);
                    updateSize();
                } else if (val instanceof javax.swing.ImageIcon) {
                    setIcon((javax.swing.ImageIcon) val);
                    setText(null);
                    _text = false;
                    _icon = true;
                    updateSize();
                } else if (val instanceof Number) {
                    setText(val.toString());
                    setIcon(null);
                    _text = true;
                    _icon = false;
                    updateSize();
                } else if (val instanceof jmri.IdTag){
                    // most IdTags are Reportable objects, so
                    // this needs to be before Reportable
                    setText(((jmri.IdTag)val).getDisplayName());
                    setIcon(null);
                    _text = true;
                    _icon = false;
                    updateSize();
                } else if (val instanceof Reportable) {
                    setText(((Reportable)val).toReportString());
                    setIcon(null);
                    _text = true;
                    _icon = false;
                    updateSize();
                } else {
                    log.warn("can't display current value of {}, val= {} of Class {}", getNamedGlobalVariable().getName(), val, val.getClass().getName());
                }
            } else {
                // map exists, use it
                NamedIcon newicon = map.get(key.toString());
                if (newicon != null) {

                    setText(null);
                    super.setIcon(newicon);
                    _text = false;
                    _icon = true;
                    updateSize();
                } else {
                    // no match, use default
                    setIcon(getDefaultIcon());

                    setText(null);
                    _text = false;
                    _icon = true;
                    updateSize();
                }
            }
        } else {
            setIcon(null);
            setText(defaultText);
            _text = true;
            _icon = false;
            updateSize();
        }
    }

    @Override
    public void setGlobalVariable(String pName) {
        super.setGlobalVariable(pName);
    }

    @Override
    protected void setValue(Object obj) {
        getGlobalVariable().setValue(obj);
        updateSize();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalVariableIcon.class);
}
