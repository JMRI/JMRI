package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.jmrit.catalog.NamedIcon;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import javax.swing.JTextField;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;

import jmri.NamedBeanHandle;

/**
 * An icon to display a status of a Memory.<P>
 * <P>
 * The value of the memory can't be changed with this icon.
 *<P>
 * @author Bob Jacobsen  Copyright (c) 2004
 * @version $Revision$
 */

public class MemoryIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

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
    }

    public MemoryIcon(NamedIcon s, Editor editor) {
        super(s, editor);
        setDisplayLevel(Editor.LABELS);
        defaultIcon = s;
        //updateSize();
        _popupUtil.setJustification(LEFT);
        log.debug("MemoryIcon ctor= "+MemoryIcon.class.getName());

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
        if (namedMemory == null) name = rb.getString("NotConnected");
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
                        getMemory().setValue(key);
                    }
                });
            }
            return true;
        }  // end of selectable
        return false;
    }

    /**
    * Text edits cannot be done to Memory text - override
    */    
    public boolean setTextEditMenu(JPopupMenu popup) {
        popup.add(new AbstractAction(rb.getString("EditMemoryValue")) {
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
		if (key != null) {
		    if (map == null) {
                Object val = key;
		        // no map, attempt to show object directly
                if (val instanceof jmri.jmrit.roster.RosterEntry){
                    jmri.jmrit.roster.RosterEntry roster = (jmri.jmrit.roster.RosterEntry) val;
                    javax.swing.ImageIcon icon = jmri.InstanceManager.rosterIconFactoryInstance().getIcon(roster);
                    if(icon.getIconWidth()==-1 || icon.getIconHeight()==-1){
                        //the IconPath is still at default so no icon set
                        val = roster.titleString();
                    } else {
                        NamedIcon rosterIcon = new NamedIcon(roster.getIconPath(), roster.getIconPath());
                        _text = false;
                        _icon = true;
                        updateIcon(rosterIcon);
                        rosterIcon.reduceTo(maxWidth(), maxHeight(), 0.2);
                        re=roster;
                        jmri.InstanceManager.throttleManagerInstance().attachListener(re.getDccLocoAddress(), this);
                        Object isForward = jmri.InstanceManager.throttleManagerInstance().getThrottleInfo(re.getDccLocoAddress(), "IsForward");
                        if(isForward!=null){
                            if(!(Boolean)isForward)
                                flipIcon(NamedIcon.HORIZONTALFLIP);
                        }
                        return;
                    }
                }
                if (val instanceof String) {
                    String str = (String)val;
                    setText(str);
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
                    setIcon(null);
                    _icon = false;
                    _text = true;
                } else if (val instanceof javax.swing.ImageIcon) {
                    setIcon((javax.swing.ImageIcon) val);
                    setText(null);
                    _icon = true;
                    _text = false;
                } else if (val instanceof Number) {
                    setText(val.toString());
                    setIcon(null);
                    _icon = false;
                    _text = true;
                } else log.warn("can't display current value of "+namedMemory.getName()+
                                ", val= "+val+" of Class "+val.getClass().getName());
		    } else {
		        // map exists, use it
			    NamedIcon newicon = map.get(key.toString());
			    if (newicon!=null) {
                    
                    setText(null);
				    super.setIcon(newicon);
			    } else {
			        // no match, use default
		            setIcon(defaultIcon);                    
                    setText(null);
                    _icon = true;
                    _text = false;
			    }
		    }
		} else {
            if (log.isDebugEnabled()) log.debug("memory null");
            setIcon(defaultIcon);
            setText(null);
            _icon = true;
            _text = false;
        }
        updateSize();
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
        String txt = java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString("Memory"));
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
    
    private void editMemoryValue(){
        JTextField newMemory = new JTextField(20);
        if (getMemory().getValue()!=null)
            newMemory.setText(getMemory().getValue().toString());
        Object[] options = {"Cancel", "OK", newMemory};
        int retval = JOptionPane.showOptionDialog(this,
                                                  "Edit Current Memory Value", namedMemory.getName(),
                                                  0, JOptionPane.INFORMATION_MESSAGE, null,
                                                  options, options[2] );

        if (retval != 1) return;
        getMemory().setValue(newMemory.getText());
        updateSize();
    }
    
    protected boolean updateBlockValue = false;
    
    public void updateBlockValueOnChange(boolean boo){
        updateBlockValue = boo;
    }

    public boolean updateBlockValueOnChange(){
        return updateBlockValue;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemoryIcon.class.getName());
}
