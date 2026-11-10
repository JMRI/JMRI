package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import jmri.Block;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.throttle.ThrottleFrameManager;
import jmri.jmrit.throttle.interfaces.ThrottleControllerUI;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.swing.JmriMouseEvent;

/**
 * An icon to display the value contained within a Block.
 *
 * @author Bob Jacobsen Copyright (c) 2004
 */
public class BlockContentsIcon extends MemoryIcon {

    private NamedIcon defaultIcon = null;
    private NamedBeanHandle<Block> namedBlock;

    public BlockContentsIcon(String s, Editor editor) {
        super(s, editor);
        BlockContentsIcon.this.resetDefaultIcon();
        _namedIcon = defaultIcon;
        //By default all text objects are left justified
        _popupUtil.setJustification(LEFT);
        this.setTransferHandler(new TransferHandler());
    }

    public BlockContentsIcon(NamedIcon s, Editor editor) {
        super(s, editor);
        setDisplayLevel(Editor.LABELS);
        defaultIcon = s;
        _popupUtil.setJustification(LEFT);
        log.debug("BlockContentsIcon ctor= {}", BlockContentsIcon.class.getName());
        this.setTransferHandler(new TransferHandler());
    }

    @Override
    @Nonnull
    public Positionable deepClone() {
        BlockContentsIcon pos = new BlockContentsIcon("", _editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(BlockContentsIcon pos) {
        pos.setBlock(namedBlock);
        pos.setOriginalLocation(getOriginalX(), getOriginalY());
        if (map != null) {
            for (Map.Entry<String, NamedIcon> entry : map.entrySet()) {
                String url = entry.getValue().getName();
                pos.addKeyAndIcon(NamedIcon.getIconByName(url), entry.getKey());
            }
        }
        return super.finishClone(pos);
    }

    @Override
    public void resetDefaultIcon() {
        defaultIcon = new NamedIcon("resources/icons/misc/X-red.gif",
                "resources/icons/misc/X-red.gif");
    }

    /**
     * Attach a named Block to this display item.
     *
     * @param pName Used as a system/user name to lookup the Block object
     */
    public void setBlock(String pName) {
        if (InstanceManager.getNullableDefault(jmri.BlockManager.class) != null) {
            Block block = InstanceManager.getDefault(jmri.BlockManager.class).
                    provideBlock(pName);
            setBlock(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, block));
        } else {
            log.error("No Block Manager for this protocol, icon won't see changes");
        }
        updateSize();
    }

    /**
     * Attach a named Block to this display item.
     *
     * @param m The Block object
     */
    public void setBlock(NamedBeanHandle<Block> m) {
        if (namedBlock != null) {
            getBlock().removePropertyChangeListener(this);
        }
        namedBlock = m;
        if (namedBlock != null) {
            getBlock().addPropertyChangeListener(this, namedBlock.getName(), "Block Icon");
            displayState();
            setName(namedBlock.getName());
        }
    }

    public NamedBeanHandle<Block> getNamedBlock() {
        return namedBlock;
    }

    public Block getBlock() {
        if (namedBlock == null) {
            return null;
        }
        return namedBlock.getBean();
    }

    @Override
    public jmri.NamedBean getNamedBean() {
        return getBlock();
    }

    @Override
    public java.util.HashMap<String, NamedIcon> getMap() {
        return map;
    }

    @Override
    @Nonnull
    public String getTypeString() {
        return Bundle.getMessage("PositionableType_BlockContentsIcon");
    }

    @Override
    @Nonnull
    public String getNameString() {
        String name;
        if (namedBlock == null) {
            name = Bundle.getMessage("NotConnected");
        } else {
            name = getBlock().getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
        }
        return name;
    }

    /**
     * {@inheritDoc}
     * <p>
     * In operating mode, after the Dispatcher items for the train the icon
     * shows, a last item opens the Block dialog: the trains the Block holds,
     * why, and what has happened there.
     */
    @Override
    public boolean showPopUp(JPopupMenu popup) {
        boolean added = showTrainPopUp(popup);
        Block block = getBlock();
        if (!isEditable() && block != null) {
            if (popup.getComponentCount() > 0) {
                popup.addSeparator();
            }
            popup.add(new AbstractAction(jmri.jmrit.trainrecord.swing.BlockDialog.menuItemText(block)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    jmri.jmrit.trainrecord.swing.BlockDialog.open(block, !isValueEditDisabled());
                }
            });
            return true;
        }
        return added;
    }

    private boolean showTrainPopUp(JPopupMenu popup) {
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
        } // end of selectable
        // This is a little different
        // jmri.jmrit.dispatcher.DispatcherFrame.class is AutoCreate so getNullableDefault creates it 
        // if it doesnt exist. So we look at the count of instances.
        final jmri.jmrit.dispatcher.DispatcherFrame df;
        if (jmri.InstanceManager.getList(jmri.jmrit.dispatcher.DispatcherFrame.class).isEmpty()) {
            df = null;
        } else {
            df = jmri.InstanceManager.getNullableDefault(jmri.jmrit.dispatcher.DispatcherFrame.class);
        }
        // the train the menu is for, as the Block shows it
        final Object train = trainForMenu();
        final jmri.jmrit.roster.RosterEntry entry =
                train instanceof jmri.jmrit.roster.RosterEntry ? (jmri.jmrit.roster.RosterEntry) train : null;
        final jmri.jmrit.dispatcher.ActiveTrain at;
        if (df != null) {
            if (entry != null) {
                at = df.getActiveTrainForRoster(entry);
            } else {
                at = df.getActiveTrainForName(train == null ? null : train.toString());
            }
        } else {
            at = null;
        }
        if (at != null && df != null) {
            // we have active train, with or without auto train with or without roster entry
            if (at.getAutoActiveTrain() != null ) {
                if( re == null ) {
                    popup.add(new AbstractAction("Open Throttle") {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            ThrottleControllerUI tf = InstanceManager.getDefault(ThrottleFrameManager.class).createThrottleFrame();
                            tf.toFront();
                            tf.setAddress(at.getAutoActiveTrain().getDccAddress());
                        }
                    });
                } else {
                    popup.add(new AbstractAction("Open Throttle") {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            ThrottleControllerUI tf = InstanceManager.getDefault(ThrottleFrameManager.class).createThrottleFrame();
                            tf.toFront();
                            tf.setAddress(at.getAutoActiveTrain().getDccAddress());
                        }
                    });
                }
            }
            popup.add(new AbstractAction(Bundle.getMessage("MenuTerminateTrain")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    df.terminateActiveTrain(at, true, false);
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
                popup.add(new AbstractAction("Restart") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        at.allocateAFresh();
                    }
                });
            }
            if (isEditable()) {
                popup.add(new JSeparator());
            }
            return true;
        } else if (entry != null) {
            // No active train, but have a roster, therefore a throttle can be created
            popup.add(new AbstractAction("Open Throttle") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ThrottleControllerUI tf = InstanceManager.getDefault(ThrottleFrameManager.class).createThrottleFrame();
                    tf.toFront();
                    tf.setRosterEntry(entry);
                }
            });
            // if dispatcher exists we can create a new train.
            if (df != null) {
                popup.add(new AbstractAction(Bundle.getMessage("MenuNewTrain")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!df.getNewTrainActive()) {
                            df.getActiveTrainFrame().initiateTrain(e, entry, getBlock());
                            df.setNewTrainActive(true);
                        } else {
                            df.getActiveTrainFrame().showActivateFrame(entry);
                        }
                    }

                });
            }
            if (isEditable()) {
                popup.add(new JSeparator());
            }
            return true;
        }
        return false;
    }

    /**
     * Text edits cannot be done to Block text - override.
     */
    @Override
    public boolean setTextEditMenu(JPopupMenu popup) {
        popup.add(new AbstractAction(Bundle.getMessage("EditBlockValue")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                editBlockValue();
            }
        });
        return true;
    }

    /**
     * Drive the current state of the display from the state of the Block Value.
     */
    @Override
    public void displayState() {
        log.debug("displayState");
        if (namedBlock == null) {  // use default if not connected yet
            setIcon(defaultIcon);
            updateSize();
            return;
        }
        if (re != null) {
            jmri.InstanceManager.throttleManagerInstance().removeListener(re.getDccLocoAddress(), this);
            re = null;
        }
        Object value = getBlock().getValue();
        Object allocatedValue = getBlock().getAllocatedValue();
        if (value == null && allocatedValue != null && getMap() == null) {
            // the train the Block is allocated to, not yet in it
            displayState(allocatedText(allocatedValue));
        } else {
            // the train in the Block or, shown by an icon map, the train it is allocated to
            displayState(getBlock().getDisplayValue());
        }
    }

    /**
     * Get the text that shows the train a Block is allocated to, so that it
     * can be told apart from a train in the Block: its name in italics, after
     * an arrow. As HTML, so that the label's own font, which the panel file
     * keeps, is left alone.
     *
     * @param allocatedValue the Block's allocated value
     * @return the text to show
     */
    @Nonnull
    static String allocatedText(@Nonnull Object allocatedValue) {
        String name;
        if (allocatedValue instanceof jmri.jmrit.roster.RosterEntry) {
            name = ((jmri.jmrit.roster.RosterEntry) allocatedValue).titleString();
        } else if (allocatedValue instanceof jmri.IdTag) {
            name = ((jmri.IdTag) allocatedValue).getDisplayName();
        } else if (allocatedValue instanceof jmri.Reportable) {
            name = ((jmri.Reportable) allocatedValue).toReportString();
        } else {
            name = allocatedValue.toString();
        }
        // escape only what HTML needs, so names with accents show as they are
        String escaped = name.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<html><i>" + Bundle.getMessage("AllocatedValueShown", escaped) + "</i></html>";
    }

    /**
     * Get the train the popup menu's Dispatcher items are for: the train the
     * icon shows. In a Block the train is allocated to but hasn't reached,
     * that is the Block's allocated value, as Dispatcher set it (the train's
     * name or its roster entry), not the marked text the icon shows for it.
     *
     * @return the roster entry, the train's name, or null if the icon shows
     *         no train
     */
    Object trainForMenu() {
        Block b = getBlock();
        Object allocated = b == null ? null : b.getAllocatedValue();
        if (b != null && allocated != null && b.getValue() == null && getMap() == null) {
            return allocated instanceof jmri.jmrit.roster.RosterEntry ? allocated : allocated.toString();
        }
        if (re != null) {
            return re;
        }
        return getText();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        super.propertyChange(e);
        if (Block.PROPERTY_ALLOCATED_VALUE.equals(e.getPropertyName())) {
            displayState();
        }
    }

    @Override
    public boolean setEditIconMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameBlock"));
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
        makeIconEditorFrame(this, "Block", true, null); // NOI18N
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.blockPickModelInstance());
        ActionListener addIconAction = a -> editBlock();
        _iconEditor.complete(addIconAction, false, true, true);
        _iconEditor.setSelection(getBlock());
    }

    void editBlock() {
        setBlock(_iconEditor.getTableSelection().getDisplayName());
        updateSize();
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }

    @Override
    public void dispose() {
        if (getBlock() != null) {
            getBlock().removePropertyChangeListener(this);
        }
        namedBlock = null;
        if (re != null) {
            jmri.InstanceManager.throttleManagerInstance().removeListener(re.getDccLocoAddress(), this);
            re = null;
        }
        super.dispose();
    }

    @Override
    public void doMouseClicked(JmriMouseEvent e) {
        if (e.getClickCount() == 2) { // double click?
            if (!getEditor().isEditable() && isValueEditDisabled()) {
                log.debug("Double click block value edit disabled");
                return;
            }
            editBlockValue();
        }
    }

    protected void editBlockValue() {

        String reval = (String)JmriJOptionPane.showInputDialog(this,
                                     Bundle.getMessage("EditCurrentBlockValue", namedBlock.getName()),
                                     getBlock().getValue());

        setValue(reval);
        updateSize();
    }

    @Override
    protected Object getValue() {
        if (getBlock() == null) {
            return null;
        }
        return getBlock().getValue();
    }

    @Override
    protected void setValue(Object val) {
        getBlock().setValue(val);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BlockContentsIcon.class);

}
