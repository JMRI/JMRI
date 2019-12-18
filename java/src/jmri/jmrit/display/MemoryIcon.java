package jmri.jmrit.display;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.NamedBeanHandle;
import jmri.Reportable;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterIconFactory;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.throttle.ThrottleFrameManager;
import jmri.util.datatransfer.RosterEntrySelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display a status of a Memory.
 * <p>
 * The value of the memory can't be changed with this icon.
 *
 * @author Bob Jacobsen Copyright (c) 2004
 */
public class MemoryIcon extends PositionableLabel implements java.beans.PropertyChangeListener/*, DropTargetListener*/ {

    NamedIcon defaultIcon = null;
    // the map of icons
    java.util.HashMap<String, NamedIcon> map = null;
    private NamedBeanHandle<Memory> namedMemory;

    public MemoryIcon(String s, Editor editor) {
        super(s, editor);
        resetDefaultIcon();
        _namedIcon = defaultIcon;
        //By default all memory is left justified
        _popupUtil.setJustification(LEFT);
        this.setTransferHandler(new TransferHandler());
    }

    public MemoryIcon(NamedIcon s, Editor editor) {
        super(s, editor);
        setDisplayLevel(Editor.LABELS);
        defaultIcon = s;
        _popupUtil.setJustification(LEFT);
        log.debug("MemoryIcon ctor= {}", MemoryIcon.class.getName());
        this.setTransferHandler(new TransferHandler());
    }

    @Override
    public Positionable deepClone() {
        MemoryIcon pos = new MemoryIcon("", _editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(MemoryIcon pos) {
        pos.setMemory(namedMemory.getName());
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
            map = new java.util.HashMap<String, NamedIcon>();
        }
    }

    /**
     * Attach a named Memory to this display item.
     *
     * @param pName Used as a system/user name to lookup the Memory object
     */
    public void setMemory(String pName) {
        if (InstanceManager.getNullableDefault(jmri.MemoryManager.class) != null) {
            try {
                Memory memory = InstanceManager.memoryManagerInstance().provideMemory(pName);
                setMemory(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, memory));
            } catch (IllegalArgumentException e) {
                log.error("Memory '{}' not available, icon won't see changes", pName);
            }
        } else {
            log.error("No MemoryManager for this protocol, icon won't see changes");
        }
        updateSize();
    }

    /**
     * Attach a named Memory to this display item.
     *
     * @param m The Memory object
     */
    public void setMemory(NamedBeanHandle<Memory> m) {
        if (namedMemory != null) {
            getMemory().removePropertyChangeListener(this);
        }
        namedMemory = m;
        if (namedMemory != null) {
            getMemory().addPropertyChangeListener(this, namedMemory.getName(), "Memory Icon");
            displayState();
            setName(namedMemory.getName());
        }
    }

    public NamedBeanHandle<Memory> getNamedMemory() {
        return namedMemory;
    }

    public Memory getMemory() {
        if (namedMemory == null) {
            return null;
        }
        return namedMemory.getBean();
    }

    @Override
    public jmri.NamedBean getNamedBean() {
        return getMemory();
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

    // update icon as state of Memory changes
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
    public String getNameString() {
        String name;
        if (namedMemory == null) {
            name = Bundle.getMessage("NotConnected");
        } else {
            name = getMemory().getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
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

            java.util.Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
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
        if (re != null) {
            popup.add(new AbstractAction(Bundle.getMessage("OpenThrottle")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    ThrottleFrame tf = InstanceManager.getDefault(ThrottleFrameManager.class).createThrottleFrame();
                    tf.toFront();
                    tf.getAddressPanel().setRosterEntry(re);
                }
            });
            //don't like the idea of refering specifically to the layout block manager for this, but it has to be done if we are to allow the panel editor to also assign trains to block, when used with a layouteditor
            if ((InstanceManager.getDefault(jmri.SectionManager.class).getNamedBeanSet().size()) > 0 && jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).getBlockWithMemoryAssigned(getMemory()) != null) {
                final jmri.jmrit.dispatcher.DispatcherFrame df = jmri.InstanceManager.getNullableDefault(jmri.jmrit.dispatcher.DispatcherFrame.class);
                if (df != null) {
                    final jmri.jmrit.dispatcher.ActiveTrain at = df.getActiveTrainForRoster(re);
                    if (at != null) {
                        popup.add(new AbstractAction(Bundle.getMessage("MenuTerminateTrain")) {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                df.terminateActiveTrain(at);
                            }
                        });
                        popup.add(new AbstractAction(Bundle.getMessage("MenuAllocateExtra")) {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                //Just brings up the standard allocate extra frame, this could be expanded in the future
                                //As a point and click operation.
                                df.allocateExtraSection(e, at);
                            }
                        });
                        if (at.getStatus() == jmri.jmrit.dispatcher.ActiveTrain.DONE) {
                            popup.add(new AbstractAction(Bundle.getMessage("MenuRestartTrain")) {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    at.allocateAFresh();
                                }
                            });
                        }
                    } else {
                        popup.add(new AbstractAction(Bundle.getMessage("MenuNewTrain")) {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (!df.getNewTrainActive()) {
                                    df.getActiveTrainFrame().initiateTrain(e, re, jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).getBlockWithMemoryAssigned(getMemory()).getBlock());
                                    df.setNewTrainActive(true);
                                } else {
                                    df.getActiveTrainFrame().showActivateFrame(re);
                                }
                            }

                        });
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Text edits cannot be done to Memory text - override
     */
    @Override
    public boolean setTextEditMenu(JPopupMenu popup) {
        popup.add(new AbstractAction(Bundle.getMessage("EditMemoryValue")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                editMemoryValue();
            }
        });
        return true;
    }

    protected void flipIcon(int flip) {
        _namedIcon.flip(flip, this);
        updateSize();
        repaint();
    }
    Color _saveColor;

    /**
     * Drive the current state of the display from the state of the Memory.
     */
    public void displayState() {
        log.debug("displayState()");

        if (namedMemory == null) {  // use default if not connected yet
            setIcon(defaultIcon);
            updateSize();
            return;
        }
        if (re != null) {
            jmri.InstanceManager.throttleManagerInstance().removeListener(re.getDccLocoAddress(), this);
            re = null;
        }
        Object key = getMemory().getValue();
        displayState(key);
    }

    /**
     * Special method to transfer a setAttributes call from the LE version of
     * MemoryIcon. This eliminates the need to change references to public.
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
                if (val instanceof jmri.jmrit.roster.RosterEntry) {
                    jmri.jmrit.roster.RosterEntry roster = (jmri.jmrit.roster.RosterEntry) val;
                    val = updateIconFromRosterVal(roster);
                    flipRosterIcon = false;
                    if (val == null) {
                        return;
                    }
                }
                if (val instanceof String) {
                    String str = (String) val;
                    _icon = false;
                    _text = true;
                    setText(str);
                    updateIcon(null);
                    if (log.isDebugEnabled()) {
                        log.debug("String str= \"" + str + "\" str.trim().length()= " + str.trim().length());
                        log.debug("  maxWidth()= " + maxWidth() + ", maxHeight()= " + maxHeight());
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
                    setText(((jmri.IdTag)val).getDisplayName());
                    setIcon(null);
                } else if (val instanceof Reportable) {
                    _icon = false;
                    _text = true;
                    setText(((Reportable)val).toReportString());
                    setIcon(null);
                } else {
                    log.warn("can't display current value of {}, val= {} of Class ",
                            getNameString(), val, val.getClass().getName());
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

    protected Object updateIconFromRosterVal(RosterEntry roster) {
        re = roster;
        javax.swing.ImageIcon icon = jmri.InstanceManager.getDefault(RosterIconFactory.class).getIcon(roster);
        if (icon == null || icon.getIconWidth() == -1 || icon.getIconHeight() == -1) {
            //the IconPath is still at default so no icon set
            return roster.titleString();
        } else {
            NamedIcon rosterIcon = new NamedIcon(roster.getIconPath(), roster.getIconPath());
            _text = false;
            _icon = true;
            updateIcon(rosterIcon);

            if (flipRosterIcon) {
                flipIcon(NamedIcon.HORIZONTALFLIP);
            }
            jmri.InstanceManager.throttleManagerInstance().attachListener(re.getDccLocoAddress(), this);
            Object isForward = jmri.InstanceManager.throttleManagerInstance().getThrottleInfo(re.getDccLocoAddress(), jmri.Throttle.ISFORWARD);
            if (isForward != null) {
                if (!(Boolean) isForward) {
                    flipIcon(NamedIcon.HORIZONTALFLIP);
                }
            }
            return null;
        }
    }

    protected jmri.jmrit.roster.RosterEntry re = null;

    /*As the size of a memory label can change we want to adjust the position of the x,y
     if the width is fixed*/
    static final int LEFT = 0x00;
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

    public int getOriginalX() {
        return originalX;
    }

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
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameMemory"));
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
        makeIconEditorFrame(this, "Memory", true, null);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.memoryPickModelInstance());
        ActionListener addIconAction = (ActionEvent a) -> {
            editMemory();
        };
        _iconEditor.complete(addIconAction, false, true, true);
        _iconEditor.setSelection(getMemory());
    }

    void editMemory() {
        setMemory(_iconEditor.getTableSelection().getDisplayName());
        updateSize();
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }

    @Override
    public void dispose() {
        if (getMemory() != null) {
            getMemory().removePropertyChangeListener(this);
        }
        namedMemory = null;
        if (re != null) {
            jmri.InstanceManager.throttleManagerInstance().removeListener(re.getDccLocoAddress(), this);
            re = null;
        }
        super.dispose();
    }

    @Override
    public void doMouseClicked(java.awt.event.MouseEvent e) {
        if (e.getClickCount() == 2) { // double click?
            editMemoryValue();
        }
    }

    protected void editMemoryValue() {
        JTextField newMemory = new JTextField(20);
        if (getMemory().getValue() != null) {
            newMemory.setText(getMemory().getValue().toString());
        }
        Object[] options = {Bundle.getMessage("ButtonCancel"), Bundle.getMessage("ButtonOK"), newMemory};
        int retval = JOptionPane.showOptionDialog(this,
                "Edit Current Memory Value", namedMemory.getName(),
                0, JOptionPane.INFORMATION_MESSAGE, null,
                options, options[2]);

        if (retval != 1) {
            return;
        }
        setValue(newMemory.getText());
        updateSize();
    }

    //This is used by the LayoutEditor
    protected boolean updateBlockValue = false;

    public void updateBlockValueOnChange(boolean boo) {
        updateBlockValue = boo;
    }

    public boolean updateBlockValueOnChange() {
        return updateBlockValue;
    }

    protected boolean flipRosterIcon = false;

    protected void addRosterToIcon(RosterEntry roster) {
        Object[] options = {"Facing West",
            "Facing East",
            "Do Not Add"};
        int n = JOptionPane.showOptionDialog(this, // TODO I18N
                "Would you like to assign loco "
                + roster.titleString() + " to this location",
                "Assign Loco",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);
        if (n == 2) {
            return;
        }
        flipRosterIcon = (n == 0);
        if (getValue() == roster) {
            //No change in the loco but a change in direction facing might have occurred
            updateIconFromRosterVal(roster);
        } else {
            setValue(roster);
        }
    }

    protected Object getValue() {
        if (getMemory() == null) {
            return null;
        }
        return getMemory().getValue();
    }

    protected void setValue(Object val) {
        getMemory().setValue(val);
    }

    class TransferHandler extends javax.swing.TransferHandler {
        @Override
        public boolean canImport(JComponent c, DataFlavor[] transferFlavors) {
            for (DataFlavor flavor : transferFlavors) {
                if (RosterEntrySelection.rosterEntryFlavor.equals(flavor)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean importData(JComponent c, Transferable t) {
            try {
                ArrayList<RosterEntry> REs = RosterEntrySelection.getRosterEntries(t);
                for (RosterEntry roster : REs) {
                    addRosterToIcon(roster);
                }
            } catch (java.awt.datatransfer.UnsupportedFlavorException | java.io.IOException e) {
                log.error(e.getLocalizedMessage(), e);
            }
            return true;
        }

    }

    private final static Logger log = LoggerFactory.getLogger(MemoryIcon.class);

}
