package jmri.jmrit.display;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.Memory;
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
 * @version $Revision$
 */

public class MemoryIcon extends PositionableLabel implements java.beans.PropertyChangeListener/*, DropTargetListener*/ {

	NamedIcon defaultIcon = null;
    // the associated Memory object
    //protected Memory memory = null;
    // the map of icons
    java.util.HashMap<String, NamedIcon> map = null;
    private NamedBeanHandle<Memory> namedMemory;
    
    public MemoryIcon(String s, Editor editor) {
        super(s, editor);
        resetDefaultIcon();
        //setIcon(defaultIcon);
        _namedIcon=defaultIcon;
        //updateSize();
        //By default all memory is left justified
        _popupUtil.setJustification(LEFT);
        this.setTransferHandler(new TransferHandler());
    }

    public MemoryIcon(NamedIcon s, Editor editor) {
        super(s, editor);
        setDisplayLevel(Editor.LABELS);
        defaultIcon = s;
        //updateSize();
        _popupUtil.setJustification(LEFT);
        log.debug("MemoryIcon ctor= "+MemoryIcon.class.getName());
        this.setTransferHandler(new TransferHandler());
    }
    
    public Positionable deepClone() {
        MemoryIcon pos = new MemoryIcon("", _editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        MemoryIcon pos = (MemoryIcon)p;
        pos.setMemory(namedMemory.getName());
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
    
	public void setDefaultIcon(NamedIcon n) {
        defaultIcon = n;
	}
	
	public NamedIcon getDefaultIcon() {
	    return defaultIcon;
	}
	
	private void setMap() {
        if (map==null) map = new java.util.HashMap<String, NamedIcon>();
	}

    /**
     * Attached a named Memory to this display item
      * @param pName Used as a system/user name to lookup the Memory object
     */
     public void setMemory(String pName) {
         if (InstanceManager.memoryManagerInstance()!=null) {
            Memory memory = InstanceManager.memoryManagerInstance().
                 provideMemory(pName);
             if (memory != null) {
                 setMemory(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, memory));
             } else {
                 log.error("Memory '"+pName+"' not available, icon won't see changes");
             }
         } else {
             log.error("No MemoryManager for this protocol, icon won't see changes");
         }
         updateSize();
     }

    /**
     * Attached a named Memory to this display item
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
    
    public NamedBeanHandle<Memory> getNamedMemory() { return namedMemory; }
    
    public Memory getMemory() {
        if (namedMemory==null) {
            return null;
        }
        return namedMemory.getBean();
    }
    
    public jmri.NamedBean getNamedBean(){
        return getMemory();
    }

    public java.util.HashMap<String, NamedIcon> getMap() { return map; }

    // display icons

    public void addKeyAndIcon(NamedIcon icon, String keyValue) {
        if (map == null) setMap(); // initialize if needed
    	map.put(keyValue, icon);
    	// drop size cache
    	//height = -1;
    	//width = -1;
        displayState(); // in case changed
    }

    // update icon as state of Memory changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property change: "
                                            +e.getPropertyName()
                                            +" is now "+e.getNewValue());
        if (e.getPropertyName().equals("value")) {
            displayState();
        }
        if(e.getSource() instanceof jmri.Throttle){
            if(e.getPropertyName().equals("IsForward")){
                Boolean boo = (Boolean)e.getNewValue();
                if(boo)
                    flipIcon(NamedIcon.NOFLIP);
                else
                    flipIcon(NamedIcon.HORIZONTALFLIP);
            }
        }
    }

    public String getNameString() {
        String name;
        if (namedMemory == null) name = Bundle.getMessage("NotConnected");
        else if (getMemory().getUserName()!=null)
            name = getMemory().getUserName()+" ("+getMemory().getSystemName()+")";
        else
            name = getMemory().getSystemName();
        return name;
    }


    public void setSelectable(boolean b) {selectable = b;}
    public boolean isSelectable() { return selectable;}
    boolean selectable = false;
    
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
            popup.add(new AbstractAction(Bundle.getMessage("OpenThrottle")) {
                public void actionPerformed(ActionEvent e) {
                    ThrottleFrame tf = ThrottleFrameManager.instance().createThrottleFrame();
                    tf.toFront();
                    tf.getAddressPanel().setRosterEntry(re);
                }
            });
            //don't like the idea of refering specifically to the layout block manager for this, but it has to be done if we are to allow the panel editor to also assign trains to block, when used with a layouteditor
            if((InstanceManager.sectionManagerInstance().getSystemNameList().size()) > 0 && jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).getBlockWithMemoryAssigned(getMemory())!=null){
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
                            popup.add(new AbstractAction(Bundle.getMessage("MenuRestartTrain")) {
                                public void actionPerformed(ActionEvent e) {
                                    at.allocateAFresh();
                                }
                            });
                        }
                    } else {
                        popup.add(new AbstractAction(Bundle.getMessage("MenuNewTrain")) {
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
    public boolean setTextEditMenu(JPopupMenu popup) {
        popup.add(new AbstractAction(Bundle.getMessage("EditMemoryValue")) {
            public void actionPerformed(ActionEvent e) {
                editMemoryValue();
            }
        });
        return true;
    }

    protected void flipIcon(int flip){
        _namedIcon.flip(flip, this);
        updateSize();
        repaint();
    }
    Color _saveColor;
    /**
     * Drive the current state of the display from the state of the
     * Memory.
     */
    public void displayState() {
        if (log.isDebugEnabled()) log.debug("displayState");
    	if (namedMemory == null) {  // use default if not connected yet
            setIcon(defaultIcon);
    		updateSize();
    		return;
    	}
        if(re!=null){
            jmri.InstanceManager.throttleManagerInstance().removeListener(re.getDccLocoAddress(), this);
            re=null;
        }
		Object key = getMemory().getValue();
        displayState(key);
    }
    protected void displayState(Object key){
		if (key != null) {
		    if (map == null) {
                Object val = key;
		        // no map, attempt to show object directly
                if (val instanceof jmri.jmrit.roster.RosterEntry){
                    jmri.jmrit.roster.RosterEntry roster = (jmri.jmrit.roster.RosterEntry) val;
                    val = updateIconFromRosterVal(roster);
                    flipRosterIcon = false;
                    if(val == null)
                        return;
                }
                if (val instanceof String) {
                    String str = (String)val;
                    _icon = false;
                    _text = true;
                    setText(str);
                    setIcon(null);
                    if (log.isDebugEnabled()) log.debug("String str= \""+str+"\" str.trim().length()= "+str.trim().length()+
                                                        ", maxWidth()= "+maxWidth()+", maxHeight()= "+maxHeight());
                    /*  MemoryIconTest says empty strings should show blank */
                    if (str.trim().length()==0) { 
                        if (getBackground().equals(_editor.getTargetPanel().getBackground())) {
                            _saveColor = getPopupUtility().getBackground();
                            if (_editor.getTargetPanel().getBackground().equals(Color.white)) {
                                getPopupUtility().setBackgroundColor(Color.gray);
                            } else {
                                getPopupUtility().setBackgroundColor(Color.white);
                            }
                        }
                    } else {
                        if (_saveColor!=null) {
                            getPopupUtility().setBackgroundColor(_saveColor);
                            _saveColor = null;
                        }
                    }
                    _editor.setAttributes(getPopupUtility(), this, false);
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
                } else log.warn("can't display current value of "+getNameString()+
                                ", val= "+val+" of Class "+val.getClass().getName());
		    } else {
		        // map exists, use it
			    NamedIcon newicon = map.get(key.toString());
			    if (newicon!=null) {
                    
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
            if (log.isDebugEnabled()) log.debug("object null");
            _icon = true;
            _text = false;
            setIcon(defaultIcon);
            setText(null);
        }
        updateSize();
    }
    
    protected Object updateIconFromRosterVal(RosterEntry roster){
        re=roster;
        javax.swing.ImageIcon icon = jmri.InstanceManager.rosterIconFactoryInstance().getIcon(roster);
        if(icon.getIconWidth()==-1 || icon.getIconHeight()==-1){
            //the IconPath is still at default so no icon set
            return roster.titleString();
        } else {
            NamedIcon rosterIcon = new NamedIcon(roster.getIconPath(), roster.getIconPath());
            _text = false;
            _icon = true;
            updateIcon(rosterIcon);
            
            if(flipRosterIcon){
                flipIcon(NamedIcon.HORIZONTALFLIP);
            }
            jmri.InstanceManager.throttleManagerInstance().attachListener(re.getDccLocoAddress(), this);
            Object isForward = jmri.InstanceManager.throttleManagerInstance().getThrottleInfo(re.getDccLocoAddress(), "IsForward");
            if(isForward!=null){
                if(!(Boolean)isForward)
                    flipIcon(NamedIcon.HORIZONTALFLIP);
            }
            return null;
        }
    }
    
    protected jmri.jmrit.roster.RosterEntry re = null;
    
    /*As the size of a memory label can change we want to adjust the position of the x,y
    if the width is fixed*/
    
    static final int LEFT   = 0x00;
    static final int RIGHT  = 0x02;
    static final int CENTRE = 0x04;
    
    public void updateSize() {
        if (_popupUtil.getFixedWidth()==0){
            //setSize(maxWidth(), maxHeight());
            switch (_popupUtil.getJustification()){
                case LEFT :     super.setLocation(getOriginalX(), getOriginalY());
                                break;
                case RIGHT :    super.setLocation(getOriginalX()-maxWidth(), getOriginalY());
                                break;
                case CENTRE :   super.setLocation(getOriginalX()-(maxWidth()/2), getOriginalY());
                                break;
            }
            setSize(maxWidth(), maxHeight());
        } else {
            super.updateSize();
            if(_icon){
                _namedIcon.reduceTo(maxWidthTrue(), maxHeightTrue(), 0.2);
            }
        }
    }
    
    /*Stores the original location of the memory, this is then used to calculate
    the position of the text dependant upon the justification*/
    private int originalX=0;
    private int originalY=0;
    
    public void setOriginalLocation(int x, int y){
        originalX=x;
        originalY=y;
        updateSize();
    }
    
    public int getOriginalX(){
        return originalX;
    }
     
    public int getOriginalY(){
        return originalY;
    }
    
    public void setLocation(int x, int y){
        if(_popupUtil.getFixedWidth()==0){
            setOriginalLocation(x,y);
        } else {
            super.setLocation(x,y);
        }
    }
    
    public boolean setEditIconMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("Memory"));
        popup.add(new AbstractAction(txt) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
        return true;
    }

    protected void edit() {
        makeIconEditorFrame(this, "Memory", true, null);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.memoryPickModelInstance());
        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                editMemory();
            }
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

    public void dispose() {
        if(getMemory()!=null)
            getMemory().removePropertyChangeListener(this);
        namedMemory = null;
        if(re!=null){
            jmri.InstanceManager.throttleManagerInstance().removeListener(re.getDccLocoAddress(), this);
            re=null;
        }
        super.dispose();
    }
    
    public void doMouseClicked(java.awt.event.MouseEvent e) {
        if (e.getClickCount() == 2) { // double click?
            editMemoryValue();
        }
    }
    
    protected void editMemoryValue(){
        JTextField newMemory = new JTextField(20);
        if (getMemory().getValue()!=null)
            newMemory.setText(getMemory().getValue().toString());
        Object[] options = {"Cancel", "OK", newMemory};
        int retval = JOptionPane.showOptionDialog(this,
                                                  "Edit Current Memory Value", namedMemory.getName(),
                                                  0, JOptionPane.INFORMATION_MESSAGE, null,
                                                  options, options[2] );

        if (retval != 1) return;
        setValue(newMemory.getText());
        updateSize();
    }
    
    //This is used by the LayoutEditor
    protected boolean updateBlockValue = false;
    
    public void updateBlockValueOnChange(boolean boo){
        updateBlockValue = boo;
    }
    
    public boolean updateBlockValueOnChange(){
        return updateBlockValue;
    }
    
    protected boolean flipRosterIcon = false;
    
    protected void addRosterToIcon(RosterEntry roster){
        Object[] options = {"Facing West",
                    "Facing East",
                    "Do Not Add"};
        int n = JOptionPane.showOptionDialog(this,
            "Would you like to assign loco "
            +  roster.titleString() + " to this location",
            "Assign Loco",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[2]);
        if(n==2)
            return;
        flipRosterIcon = false;
        if(n==0) {
            flipRosterIcon = true;
        }
        if(getValue()==roster){
            //No change in the loco but a change in direction facing might have occured
             updateIconFromRosterVal(roster);
        } else {
            setValue(roster);
        }
    }
    
    protected Object getValue(){
        if(getMemory()==null)
            return null;
        return getMemory().getValue();
    }
    
    protected void setValue(Object val){
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
            } catch(java.awt.datatransfer.UnsupportedFlavorException e){
                log.error(e.getLocalizedMessage(), e);
            } catch (java.io.IOException e){
                log.error(e.getLocalizedMessage(), e);
            }
            return true;
        }

    }

    static Logger log = LoggerFactory.getLogger(MemoryIcon.class.getName());
}
