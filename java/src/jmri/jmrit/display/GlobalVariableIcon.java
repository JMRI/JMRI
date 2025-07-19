package jmri.jmrit.display;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Reportable;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.logixng.GlobalVariable;
import jmri.jmrit.logixng.GlobalVariableManager;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.swing.JmriMouseEvent;

/**
 * An icon to display a status of a GlobalVariable.
 * <p>
 * The value of the global variable can't be changed with this icon.
 *
 * @author Bob Jacobsen     Copyright (c) 2004
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class GlobalVariableIcon extends MemoryOrGVIcon implements java.beans.PropertyChangeListener/*, DropTargetListener*/ {

    NamedIcon defaultIcon = null;
    // the map of icons
    java.util.HashMap<String, NamedIcon> map = null;
    private NamedBeanHandle<GlobalVariable> namedGlobalVariable;

    public GlobalVariableIcon(String s, Editor editor) {
        super(s, editor);
        resetDefaultIcon();
        _namedIcon = defaultIcon;
        //By default all content is left justified
        _popupUtil.setJustification(LEFT);
    }

    public GlobalVariableIcon(NamedIcon s, Editor editor) {
        super(s, editor);
        setDisplayLevel(Editor.LABELS);
        defaultIcon = s;
        _popupUtil.setJustification(LEFT);
        log.debug("GlobalVariableIcon ctor= {}", GlobalVariableIcon.class.getName());
    }

    @Override
    public Positionable deepClone() {
        GlobalVariableIcon pos = new GlobalVariableIcon("", _editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(GlobalVariableIcon pos) {
        pos.setGlobalVariable(namedGlobalVariable.getName());
        pos.setOriginalLocation(getOriginalX(), getOriginalY());
        if (map != null) {
            for (Map.Entry<String, NamedIcon> entry : map.entrySet()) {
                String url = entry.getValue().getName();
                pos.addKeyAndIcon(NamedIcon.getIconByName(url), entry.getKey());
            }
        }
        return super.finishClone(pos);
    }

    public void resetDefaultIcon() {
        defaultIcon = new NamedIcon("resources/icons/misc/X-red.gif",
                "resources/icons/misc/X-red.gif");
    }

    public void setDefaultIcon(NamedIcon n) {
        defaultIcon = n;
    }

    public NamedIcon getDefaultIcon() {
        return defaultIcon;
    }

    private void setMap() {
        if (map == null) {
            map = new java.util.HashMap<>();
        }
    }

    /**
     * Attach a named GlobalVariable to this display item.
     *
     * @param pName Used as a system/user name to lookup the GlobalVariable object
     */
    public void setGlobalVariable(String pName) {
        if (InstanceManager.getNullableDefault(GlobalVariableManager.class) != null) {
            try {
                GlobalVariable globalVariable = InstanceManager.getDefault(GlobalVariableManager.class).getGlobalVariable(pName);
                setGlobalVariable(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, globalVariable));
            } catch (IllegalArgumentException e) {
                log.error("GlobalVariable '{}' not available, icon won't see changes", pName);
            }
        } else {
            log.error("No GlobalVariableManager for this protocol, icon won't see changes");
        }
        updateSize();
    }

    /**
     * Attach a named GlobalVariable to this display item.
     *
     * @param m The GlobalVariable object
     */
    public void setGlobalVariable(NamedBeanHandle<GlobalVariable> m) {
        if (namedGlobalVariable != null) {
            getGlobalVariable().removePropertyChangeListener(this);
        }
        namedGlobalVariable = m;
        if (namedGlobalVariable != null) {
            getGlobalVariable().addPropertyChangeListener(this, namedGlobalVariable.getName(), "GlobalVariable Icon");
            displayState();
            setName(namedGlobalVariable.getName());
        }
    }

    public NamedBeanHandle<GlobalVariable> getNamedGlobalVariable() {
        return namedGlobalVariable;
    }

    public GlobalVariable getGlobalVariable() {
        if (namedGlobalVariable == null) {
            return null;
        }
        return namedGlobalVariable.getBean();
    }

    @Override
    public jmri.NamedBean getNamedBean() {
        return getGlobalVariable();
    }

    public java.util.HashMap<String, NamedIcon> getMap() {
        return map;
    }

    // display icons
    public void addKeyAndIcon(NamedIcon icon, String keyValue) {
        if (map == null) {
            setMap(); // initialize if needed
        }
        map.put(keyValue, icon);
        // drop size cache
        //height = -1;
        //width = -1;
        displayState(); // in case changed
    }

    // update icon as state of GlobalVariable changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("property change: {} is now {}",
                    e.getPropertyName(), e.getNewValue());
        }
        if (e.getPropertyName().equals("value")) {
            displayState();
        }
        if (e.getSource() instanceof jmri.Throttle) {
            if (e.getPropertyName().equals(jmri.Throttle.ISFORWARD)) {
                Boolean boo = (Boolean) e.getNewValue();
                if (boo) {
                    flipIcon(NamedIcon.NOFLIP);
                } else {
                    flipIcon(NamedIcon.HORIZONTALFLIP);
                }
            }
        }
    }

    @Override
    @Nonnull
    public String getTypeString() {
        return Bundle.getMessage("PositionableType_GlobalVariableIcon");
    }

    @Override
    public String getNameString() {
        String name;
        if (namedGlobalVariable == null) {
            name = Bundle.getMessage("NotConnected");
        } else {
            name = getGlobalVariable().getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
        }
        return name;
    }

    public void setSelectable(boolean b) {
        selectable = b;
    }

    public boolean isSelectable() {
        return selectable;
    }
    boolean selectable = false;

    @Override
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable() && selectable) {
            popup.add(new JSeparator());

            for (String key : map.keySet()) {
                //String value = ((NamedIcon)map.get(key)).getName();
                popup.add(new AbstractAction(key) {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String key = e.getActionCommand();
                        setValue(key);
                    }
                });
            }
            return true;
        }  // end of selectable
        return false;
    }

    /**
     * Text edits cannot be done to GlobalVariable text - override
     */
    @Override
    public boolean setTextEditMenu(JPopupMenu popup) {
        popup.add(new AbstractAction(Bundle.getMessage("EditGlobalVariableValue")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                editGlobalVariableValue();
            }
        });
        return true;
    }

    protected void flipIcon(int flip) {
        if (_namedIcon != null) {
            _namedIcon.flip(flip, this);
        }
        updateSize();
        repaint();
    }
    Color _saveColor;

    /**
     * Drive the current state of the display from the state of the GlobalVariable.
     */
    @Override
    public void displayState() {
        log.debug("displayState()");

        if (namedGlobalVariable == null) {  // use default if not connected yet
            setIcon(defaultIcon);
            updateSize();
            return;
        }
        Object key = getGlobalVariable().getValue();
        displayState(key);
    }

    /**
     * Special method to transfer a setAttributes call from the LE version of
     * GlobalVariableIcon. This eliminates the need to change references to public.
     *
     * @since 4.11.6
     * @param util The LE popup util object.
     * @param that The current positional object (this).
     */
    public void setAttributes(PositionablePopupUtil util, Positionable that) {
        _editor.setAttributes(util, that);
    }

    protected void displayState(Object key) {
        log.debug("displayState({})", key);
        if (key != null) {
            if (map == null) {
                Object val = key;
                // no map, attempt to show object directly
                if (val instanceof String) {
                    String str = (String) val;
                    _icon = false;
                    _text = true;
                    setText(str);
                    updateIcon(null);
                    if (log.isDebugEnabled()) {
                        log.debug("String str= \"{}\" str.trim().length()= {}", str, str.trim().length());
                        log.debug("  maxWidth()= {}, maxHeight()= {}", maxWidth(), maxHeight());
                        log.debug("  getBackground(): {}", getBackground());
                        log.debug("  _editor.getTargetPanel().getBackground(): {}", _editor.getTargetPanel().getBackground());
                        log.debug("  setAttributes to getPopupUtility({}) with", getPopupUtility());
                        log.debug("     hasBackground() {}", getPopupUtility().hasBackground());
                        log.debug("     getBackground() {}", getPopupUtility().getBackground());
                        log.debug("    on editor {}", _editor);
                    }
                    _editor.setAttributes(getPopupUtility(), this);
                } else if (val instanceof javax.swing.ImageIcon) {
                    _icon = true;
                    _text = false;
                    setIcon((javax.swing.ImageIcon) val);
                    setText(null);
                } else if (val instanceof Number) {
                    _icon = false;
                    _text = true;
                    setText(val.toString());
                    setIcon(null);
                } else if (val instanceof jmri.IdTag){
                    // most IdTags are Reportable objects, so
                    // this needs to be before Reportable
                    _icon = false;
                    _text = true;
                    setIcon(null);
                    setText(((jmri.IdTag)val).getDisplayName());
                } else if (val instanceof Reportable) {
                    _icon = false;
                    _text = true;
                    setText(((Reportable)val).toReportString());
                    setIcon(null);
                } else {
                    // don't recognize the type, do our best with toString
                    log.debug("display current value of {} as String, val= {} of Class {}",
                            getNameString(), val, val.getClass().getName());
                    _icon = false;
                    _text = true;
                    setText(val.toString());
                    setIcon(null);
                }
            } else {
                // map exists, use it
                NamedIcon newicon = map.get(key.toString());
                if (newicon != null) {

                    setText(null);
                    super.setIcon(newicon);
                } else {
                    // no match, use default
                    _icon = true;
                    _text = false;
                    setIcon(defaultIcon);
                    setText(null);
                }
            }
        } else {
            log.debug("object null");
            _icon = true;
            _text = false;
            setIcon(defaultIcon);
            setText(null);
        }
        updateSize();
    }

    /*As the size of a global variable label can change we want to adjust
     the position of the x,y if the width is fixed*/
    @SuppressWarnings("hiding")  // OVerrides a value in SwingConstants
    static final int LEFT = 0x00;
    @SuppressWarnings("hiding")  // OVerrides a value in SwingConstants
    static final int RIGHT = 0x02;
    static final int CENTRE = 0x04;

    @Override
    public void updateSize() {
        if (_popupUtil.getFixedWidth() == 0) {
            //setSize(maxWidth(), maxHeight());
            switch (_popupUtil.getJustification()) {
                case LEFT:
                    super.setLocation(getOriginalX(), getOriginalY());
                    break;
                case RIGHT:
                    super.setLocation(getOriginalX() - maxWidth(), getOriginalY());
                    break;
                case CENTRE:
                    super.setLocation(getOriginalX() - (maxWidth() / 2), getOriginalY());
                    break;
                default:
                    log.warn("Unhandled justification code: {}", _popupUtil.getJustification());
                    break;
            }
            setSize(maxWidth(), maxHeight());
        } else {
            super.updateSize();
            if (_icon && _namedIcon != null) {
                _namedIcon.reduceTo(maxWidthTrue(), maxHeightTrue(), 0.2);
            }
        }
    }

    /*Stores the original location of the memory, this is then used to calculate
     the position of the text dependant upon the justification*/
    private int originalX = 0;
    private int originalY = 0;

    public void setOriginalLocation(int x, int y) {
        originalX = x;
        originalY = y;
        updateSize();
    }

    @Override
    public int getOriginalX() {
        return originalX;
    }

    @Override
    public int getOriginalY() {
        return originalY;
    }

    @Override
    public void setLocation(int x, int y) {
        if (_popupUtil.getFixedWidth() == 0) {
            setOriginalLocation(x, y);
        } else {
            super.setLocation(x, y);
        }
    }

    @Override
    public boolean setEditIconMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameGlobalVariable"));
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
        makeIconEditorFrame(this, "GlobalVariable", true, null);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.globalVariablePickModelInstance());
        ActionListener addIconAction = (ActionEvent a) -> editGlobalVariable();
        _iconEditor.complete(addIconAction, false, false, true);
        _iconEditor.setSelection(getGlobalVariable());
    }

    void editGlobalVariable() {
        setGlobalVariable(_iconEditor.getTableSelection().getDisplayName());
        updateSize();
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }

    @Override
    public void dispose() {
        if (getGlobalVariable() != null) {
            getGlobalVariable().removePropertyChangeListener(this);
        }
        namedGlobalVariable = null;
        super.dispose();
    }

    @Override
    public void doMouseClicked(JmriMouseEvent e) {
        if (e.getClickCount() == 2) { // double click?
            if (!getEditor().isEditable() && isValueEditDisabled()) {
                log.debug("Double click global variable value edit is disabled");
                return;
            }
            editGlobalVariableValue();
        }
    }

    protected void editGlobalVariableValue() {

        String reval = (String)JmriJOptionPane.showInputDialog(this,
                                     Bundle.getMessage("EditCurrentGlobalVariableValue", namedGlobalVariable.getName()),
                                     getGlobalVariable().getValue());

        setValue(reval);
        updateSize();
    }

    protected Object getValue() {
        if (getGlobalVariable() == null) {
            return null;
        }
        return getGlobalVariable().getValue();
    }

    protected void setValue(Object val) {
        getGlobalVariable().setValue(val);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalVariableIcon.class);

}
