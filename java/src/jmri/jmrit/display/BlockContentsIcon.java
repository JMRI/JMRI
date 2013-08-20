package jmri.jmrit.display;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.Block;
import jmri.jmrit.catalog.NamedIcon;
//import javax.swing.TransferHandler;
import java.awt.datatransfer.Transferable;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JComponent;
import java.awt.datatransfer.DataFlavor;

import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.throttle.ThrottleFrameManager;

import java.util.ArrayList;

import javax.swing.JTextField;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import jmri.util.datatransfer.RosterEntrySelection;

import jmri.NamedBeanHandle;

/**
 * An icon to display a status of a Memory.<P>
 * <P>
 * The value of the memory can't be changed with this icon.
 *<P>
 * @author Bob Jacobsen  Copyright (c) 2004
 * @version $Revision: 23861 $
 */

public class BlockContentsIcon extends MemoryIcon implements java.beans.PropertyChangeListener/*, DropTargetListener*/ {

	NamedIcon defaultIcon = null;
    // the associated Memory object
    //protected Memory memory = null;
    // the map of icons
    java.util.HashMap<String, NamedIcon> map = null;
    private NamedBeanHandle<Block> namedBlock;
    
    public BlockContentsIcon(String s, Editor editor) {
        super(s, editor);
        resetDefaultIcon();
        //setIcon(defaultIcon);
        _namedIcon=defaultIcon;
        //updateSize();
        //By default all memory is left justified
        _popupUtil.setJustification(LEFT);
        this.setTransferHandler(new TransferHandler());
    }
    
    public BlockContentsIcon(NamedIcon s, Editor editor) {
        super(s, editor);
        setDisplayLevel(Editor.LABELS);
        defaultIcon = s;
        //updateSize();
        _popupUtil.setJustification(LEFT);
        log.debug("BlockContentsIcon ctor= "+BlockContentsIcon.class.getName());
        this.setTransferHandler(new TransferHandler());
    }
    
    public Positionable deepClone() {
        BlockContentsIcon pos = new BlockContentsIcon("", _editor);
        return finishClone(pos);
    }
    
    public Positionable finishClone(Positionable p) {
        BlockContentsIcon pos = (BlockContentsIcon)p;
        pos.setBlock(namedBlock.getName());
        pos.setOriginalLocation(getOriginalX(), getOriginalY());
        if (map!=null) {
		    java.util.Iterator<String> iterator = map.keySet().iterator();
    	    while (iterator.hasNext()) {
    		    String key = iterator.next().toString();
    		    String url = map.get(key).getName();
                pos.addKeyAndIcon(NamedIcon.getIconByName(url), key);         
    	    }
        }
        return super.finishClone(pos);
    }
    
    public void resetDefaultIcon() {
        defaultIcon = new NamedIcon("resources/icons/misc/X-red.gif",
                            "resources/icons/misc/X-red.gif");
    }
    
    /**
     * Attached a named Memory to this display item
      * @param pName Used as a system/user name to lookup the Memory object
     */
     public void setBlock(String pName) {
         if (InstanceManager.memoryManagerInstance()!=null) {
            Block block = InstanceManager.blockManagerInstance().
                 provideBlock(pName);
             if (block != null) {
                 setBlock(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, block));
             } else {
                 log.error("Block '"+pName+"' not available, icon won't see changes");
             }
         } else {
             log.error("No Block Manager for this protocol, icon won't see changes");
         }
         updateSize();
     }

    /**
     * Attached a named Block to this display item
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
    
    public NamedBeanHandle<Block> getNamedBlock() { return namedBlock; }
    
    public Block getBlock() {
        if (namedBlock==null) {
            return null;
        }
        return namedBlock.getBean();
    }
    
    public jmri.NamedBean getNamedBean(){
        return getBlock();
    }
    
    public java.util.HashMap<String, NamedIcon> getMap() { return map; }
    
    public String getNameString() {
        String name;
        if (namedBlock == null) name = Bundle.getMessage("NotConnected");
        else if (getBlock().getUserName()!=null)
            name = getBlock().getUserName()+" ("+getBlock().getSystemName()+")";
        else
            name = getBlock().getSystemName();
        return name;
    }
    
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable() && selectable) {
            popup.add(new JSeparator());
    
            java.util.Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next().toString();
                //String value = ((NamedIcon)map.get(key)).getName();
                popup.add(new AbstractAction(key) {
                    public void actionPerformed(ActionEvent e) {
                        String key = e.getActionCommand();
                        setValue(key);
                    }
                });
            }
            return true;
        }  // end of selectable
        if(re!=null){
            popup.add(new AbstractAction("Open Throttle") {
                public void actionPerformed(ActionEvent e) {
                    ThrottleFrame tf = ThrottleFrameManager.instance().createThrottleFrame();
                    tf.toFront();
                    tf.getAddressPanel().setRosterEntry(re);
                }
            });
            
            final jmri.jmrit.dispatcher.DispatcherFrame df = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame.class);
            if(df!=null){
                final jmri.jmrit.dispatcher.ActiveTrain at = df.getActiveTrainForRoster(re);
                if(at!=null){
                    popup.add(new AbstractAction(Bundle.getMessage("MenuTerminateTrain")) {
                        public void actionPerformed(ActionEvent e) {
                            df.terminateActiveTrain(at);
                        }
                    });
                    popup.add(new AbstractAction(Bundle.getMessage("MenuAllocateExtra")) {
                        public void actionPerformed(ActionEvent e) {
                            //Just brings up the standard allocate extra frame, this could be expanded in the future 
                            //As a point and click operation.
                            df.allocateExtraSection(e, at);
                        }
                    });
                    if(at.getStatus()==jmri.jmrit.dispatcher.ActiveTrain.DONE){
                        popup.add(new AbstractAction("Restart") {
                            public void actionPerformed(ActionEvent e) {
                                at.allocateAFresh();
                            }
                        });
                    }
                } else {
                    popup.add(new AbstractAction(Bundle.getMessage("MenuNewTrain")) {
                        public void actionPerformed(ActionEvent e) {
                            if (!df.getNewTrainActive()) {
                                df.getActiveTrainFrame().initiateTrain(e, re, getBlock());
                                df.setNewTrainActive(true);
                            } else {
                                df.getActiveTrainFrame().showActivateFrame(re);
                            }
                        }
                    
                    });
                }
            }
            return true;
        }
        return false;
    }

    /**
    * Text edits cannot be done to Memory text - override
    */    
    public boolean setTextEditMenu(JPopupMenu popup) {
        popup.add(new AbstractAction(Bundle.getMessage("EditMemoryValue")) {
            public void actionPerformed(ActionEvent e) {
                editBlockValue();
            }
        });
        return true;
    }

    /**
     * Drive the current state of the display from the state of the
     * Memory.
     */
    public void displayState() {
        if (log.isDebugEnabled()) log.debug("displayState");
    	if (namedBlock == null) {  // use default if not connected yet
            setIcon(defaultIcon);
    		updateSize();
    		return;
    	}
        if(re!=null){
            jmri.InstanceManager.throttleManagerInstance().removeListener(re.getDccLocoAddress(), this);
            re=null;
        }
		Object key = getBlock().getValue();
        displayState(key);
    }
    
    public boolean setEditIconMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("Block"));
        popup.add(new AbstractAction(txt) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
        return true;
    }

    protected void edit() {
        makeIconEditorFrame(this, "Block", true, null);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.blockPickModelInstance());
        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                editBlock();
            }
        };
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

    public void dispose() {
        if(getBlock()!=null)
            getBlock().removePropertyChangeListener(this);
        namedBlock = null;
        if(re!=null){
            jmri.InstanceManager.throttleManagerInstance().removeListener(re.getDccLocoAddress(), this);
            re=null;
        }
        super.dispose();
    }
    
    public void doMouseClicked(java.awt.event.MouseEvent e) {
        if (e.getClickCount() == 2) { // double click?
            editBlockValue();
        }
    }
    
    protected void editBlockValue(){
        JTextField newBlock = new JTextField(20);
        if (getBlock().getValue()!=null)
            newBlock.setText(getBlock().getValue().toString());
        Object[] options = {"Cancel", "OK", newBlock};
        int retval = JOptionPane.showOptionDialog(this,
                                                  "Edit Current Block Value", namedBlock.getName(),
                                                  0, JOptionPane.INFORMATION_MESSAGE, null,
                                                  options, options[2] );

        if (retval != 1) return;
        setValue(newBlock.getText());
        updateSize();
    }
    
    protected Object getValue(){
        if(getBlock()==null)
            return null;
        return getBlock().getValue();
    }
    
    protected void setValue(Object val){
        getBlock().setValue(val);
    }
    
    static Logger log = LoggerFactory.getLogger(BlockContentsIcon.class.getName());
}
