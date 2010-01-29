package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.jmrit.catalog.NamedIcon;
//import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

//New imports used in layout editor
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import jmri.util.NamedBeanHandle;

/**
 * An icon to display a status of a Memory.<P>
 * <P>
 * The value of the memory can't be changed with this icon.
 *<P>
 * @author Bob Jacobsen  Copyright (c) 2004
 * @version $Revision: 1.38 $
 */

public class MemoryIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

	NamedIcon defaultIcon = null;
    // the associated Memory object
    Memory memory = null;
    // the map of icons
    java.util.HashMap<String, NamedIcon> map = null;
    private NamedBeanHandle<Memory> namedMemory;
    
    public MemoryIcon(String s, Editor editor) {
        super(s, editor);
        setDisplayLevel(Editor.MEMORIES);
        _icon = true;   // also iconic
        updateSize();
    }

    public MemoryIcon(NamedIcon s, Editor editor) {
        super(s, editor);
        setDisplayLevel(Editor.LABELS);
        _text = true;   // also has text from memory.
        updateSize();
    }

    protected void resetDefaultIcon() {
        defaultIcon = new NamedIcon("resources/icons/misc/X-red.gif",
                            "resources/icons/misc/X-red.gif");
    }
    
	public void setDefaultIcon(NamedIcon n) {
        defaultIcon = n;
        displayState(); // in case changed
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
             memory = InstanceManager.memoryManagerInstance().
                 provideMemory(pName);
             if (memory != null) {
                 setMemory(new NamedBeanHandle<Memory>(pName, memory));
             } else {
                 log.error("Memory '"+pName+"' not available, icon won't see changes");
             }
         } else {
             log.error("No MemoryManager for this protocol, icon won't see changes");
         }
     }

    /**
     * Attached a named Memory to this display item
     * @param m The Memory object
     */
    public void setMemory(NamedBeanHandle<Memory> m) {
        if (memory != null) {
            memory.removePropertyChangeListener(this);
        }
        memory = InstanceManager.memoryManagerInstance().provideMemory(m.getName());
        if (memory != null) {
            memory.addPropertyChangeListener(this);
            displayState();
            namedMemory = m;
        }
    }

    public NamedBeanHandle<Memory> getMemory() { return namedMemory; }
    
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
    }

    public String getNameString() {
        String name;
        if (memory == null) name = rb.getString("NotConnected");
        else if (memory.getUserName()!=null)
            name = memory.getUserName()+" ("+memory.getSystemName()+")";
        else
            name = memory.getSystemName();
        return name;
    }


    public void setSelectable(boolean b) {selectable = b;}
    public boolean isSelectable() { return selectable;}
    boolean selectable = false;
    
    /**
     * Pop-up displays the Memory name, allows you to remove the icon.
     *<P>
     * Rotate is not supported for text-holding memories
     *<p>
     * Because this class can change between icon and text forms, 
     * we recreate the popup object each time.
     */
    public void showPopUp(JPopupMenu popup) {
        if (selectable) {
            popup.add(new JSeparator());
    
            java.util.Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next().toString();
                //String value = ((NamedIcon)map.get(key)).getName();
                popup.add(new AbstractAction(key) {
                    public void actionPerformed(ActionEvent e) {
                        String key = e.getActionCommand();
                        memory.setValue(key);
                    }
                });
            }
        }  // end of selectable
    }

    /**
    * Rotate othogonally cannot be done to Label text - override
    */
    public void setRotateOrthogonalMenu(JPopupMenu popup) {
    }
    /**
    * Rotatations cannot be done to Label text - override
    */    
    public void setRotateMenu(JPopupMenu popup) {
    }
    /**
    * Image scaling cannot be done to Label text - override
    */    
    public void setScaleMenu(JPopupMenu popup) {
    }
    /**
    * Text edits cannot be done to Memory text - override
    */    
    public void setTextEditMenu(JPopupMenu popup) {
    }

    /**
     * Drive the current state of the display from the state of the
     * Memory.
     */
    protected void displayState() {
        if (log.isDebugEnabled()) log.debug("displayState");
    	if (memory == null) {  // use default if not connected yet
            setIcon(defaultIcon);
    		updateSize();
    		return;
    	}
		Object key = memory.getValue();
		if (key != null) {
		    if (map == null) {
		        // no map, attempt to show object directly
                Object val = key;
                if (val instanceof String) {
                    setText((String) val);
                    setIcon(null);
                    //_text = true;
                    //_icon = false;
                } else if (val instanceof javax.swing.ImageIcon) {
                    setIcon((javax.swing.ImageIcon) val);
                    setText(null);
                    //_text = false;
                    //_icon = true;
                } else if (val instanceof Number) {
                    setText(val.toString());
                    setIcon(null);
                    //_text = true;
                    //_icon = false;
                } else log.warn("can't display current value of "+memory.getSystemName()+
                                ", val= "+val+" of Class "+val.getClass().getName());
		    } else {
		        // map exists, use it
			    NamedIcon newicon = map.get(key.toString());
			    if (newicon!=null) {
                    
                    setText(null);
				    super.setIcon(newicon);
                    //_text = false;
                    //_icon = true;
			    } else {
			        // no match, use default
		            setIcon(defaultIcon);
                    
                    setText(null);
                    //_text = false;
                    //_icon = true;
			    }
		    }
		} else {
            // use LayoutEditor's conventions
            setIcon(null);
            setText(" ");
            //_text = true;
            //_icon = false;
            /* former PanelEditor's conventions
            setIcon(defaultIcon);
            setText(null);
            _text = false;
            _icon = true;
            */
        }
        updateSize();
    }

    protected void edit() {
        if (showIconEditorFrame(this)) {
            return;
        }
        _iconEditor = new IconAdder();
        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                editMemory();
            }
        };
        _iconEditorFrame = makeAddIconFrame("EditMemory", "addMemValueToPanel", 
                                             "SelectMemory", _iconEditor, this);
        _iconEditor.setPickList(PickListModel.memoryPickModelInstance());
        _iconEditor.complete(addIconAction, null, true, true);
        _iconEditor.setSelection(memory);
    }
    void editMemory() {
        setMemory(_iconEditor.getTableSelection().getDisplayName());
        updateSize();
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }

    /*public void updateSize() {
    	//height = -1;
    	//width = -1;
        super.updateSize();
    }*/
    
    public void dispose() {
        memory.removePropertyChangeListener(this);
        memory = null;
        super.dispose();
    }
    
    //Imports from the old Layout Memory Icon Code.
    
    private int originalX=0;
    private int originalY=0;
    
    static final int LEFT   = 0x00;
    static final int RIGHT  = 0x02;
    static final int CENTRE = 0x04;
    
    private int justification=LEFT; //Default is always left    
    
    public void setJustification(int just){
        justification=just;
        setJustification();
    }
    
    public void setJustification(String just){
        if (just.equals("right"))
            justification=RIGHT;
        else if (just.equals("centre"))
            justification=CENTRE;
        else
            justification=LEFT;
        setJustification();
    }
    
    private void setJustification(){
        if (getFixedWidth()==0){
            switch (justification){
                case RIGHT :    setOriginalLocation(this.getX()+this.maxWidth(), this.getY());
                                break;
                case CENTRE :   setOriginalLocation(this.getX()+(this.maxWidth()/2), this.getY());
                                break;
            }
            this.setHorizontalAlignment(JLabel.CENTER);
            updateSize();
        }
        else{
            switch (justification){
                case LEFT :     this.setHorizontalAlignment(JLabel.LEFT);
                                break;
                case RIGHT :    this.setHorizontalAlignment(JLabel.RIGHT);
                                break;
                case CENTRE :   this.setHorizontalAlignment(JLabel.CENTER);
                                break;
                default     :   this.setHorizontalAlignment(JLabel.CENTER);
            }
        }
    }
    
    public void setOriginalLocation(int x, int y){
        originalX=x;
        originalY=y;
        updateSize();
    }
    
    public int getJustification(){
        return justification;
    }
    
    public int getOriginalX(){
        return originalX;
    }
    
    public int getOriginalY(){
        return originalY;
    }
    /*private int fixedWidth=0;
    private int fixedHeight=0;
    
    public int getFixedWidth(){
        return fixedWidth;
    }
    
    public int getFixedHeight(){
        return fixedHeight;
    }*/
    
    /*public void setFixedSize(int width, int height){
        
        fixedWidth=width;
        fixedHeight=height;
        if ((width!=0) && (height!=0)){
            setJustification();
            setSize(fixedWidth, fixedHeight);
        } else if ((width!=0) && (height==0)){
            setJustification();
            setSize(fixedWidth, maxHeight());
        } else if ((width==0) && (height!=0)){
            setSize(maxWidth(), fixedHeight);
        } else {
            setSize(maxWidth(), maxHeight());
        }
    }*/
 
    public void setLocation(int x, int y){

        super.setLocation(x,y);

        if (getFixedWidth()==0){
            switch (justification){
                case RIGHT :    setOriginalLocation(this.getX()+this.maxWidth(), this.getY());
                                break;
                case CENTRE :   setOriginalLocation(this.getX()+(this.maxWidth()/2), this.getY());
                                break;
            }
        }
    }
    
    /**
     * This may be called during the superclass ctor, so before 
     * construction of this object is complete.  Be careful about that!
     */
    public int maxHeight() {
        if(isIcon() && _namedIcon!=null){
            return _namedIcon.getIconHeight();
        }
        if ((getFixedHeight()==0) && (getMargin()==0))
            return ((javax.swing.JLabel)this).getMaximumSize().height;  // defer to superclass
        else if ((getFixedHeight()==0) && (getMargin()!=0))
            return ((javax.swing.JLabel)this).getMaximumSize().height+(getMargin()*2);
        //else if ((getFixedHeight()!=0) && (getMargin()!=0))
        return getFixedHeight();
        //return getFixedHeight()+(getBorderSize()*2);
    }
    
    //private int width = -1;
    /**
     * This may be called during the superclass ctor, so before 
     * construction of this object is complete.  Be careful about that!
     */
    public int maxWidth() {
        if(isIcon() && _namedIcon!=null){
            return _namedIcon.getIconWidth();
        }
        if ((getFixedWidth()==0) && (getMargin()==0))
            return ((javax.swing.JLabel)this).getMaximumSize().width;  // defer to superclass
        else if ((getFixedWidth()==0) && (getMargin()!=0))
            return ((javax.swing.JLabel)this).getMaximumSize().width+(getMargin()*2);
        //else if ((getFixedWidth()!=0) && (getMargin()!=0))
         //   return getFixedWidth();
        return getFixedWidth();
    }

    JMenu makeTextJustificationMenu() {
        JMenu justMenu = new JMenu("Justification");
        addJustificationMenuEntry(justMenu, LEFT);
        addJustificationMenuEntry(justMenu, RIGHT);
        addJustificationMenuEntry(justMenu, CENTRE);
        return justMenu;
    }
    
    void addJustificationMenuEntry(JMenu menu, final int just) {
        ButtonGroup justButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem r;
        switch(just){
            case LEFT :     r = new JRadioButtonMenuItem("LEFT");
                            break;
            case RIGHT:     r = new JRadioButtonMenuItem("RIGHT");
                            break;
            case CENTRE:    r = new JRadioButtonMenuItem("CENTRE");
                            break;
            default :       r = new JRadioButtonMenuItem("LEFT");
        }
        r.addActionListener(new ActionListener() {
            //final int justification = just;
            public void actionPerformed(ActionEvent e) { setJustification(just); }
        });
        justButtonGroup.add(r);
        if (justification == just) r.setSelected(true);
        else r.setSelected(false);
        menu.add(r);
    }
    
    public void updateSize() {
        if(isIcon()){
            setSize(this.maxWidth(), this.maxHeight());
        } else {
            if (getFixedWidth()==0){
                switch (justification){
                    case RIGHT :    super.setLocation(this.getOriginalX()-this.maxWidth(), this.getOriginalY());
                                    break;
                    case CENTRE :   super.setLocation(this.getOriginalX()-(this.maxWidth()/2), this.getOriginalY());
                                    break;
                }
            }
            this.setSize(this.maxWidth(), this.maxHeight());
        }
    }
    
    public void mouseClicked(java.awt.event.MouseEvent e) {
        if (e.getClickCount() == 2){ 
            editMemoryValue();
        }
    }
    
    private void editMemoryValue(){
        JTextField _newMemory = new JTextField(20);
        if (memory.getValue()!=null)
            _newMemory.setText(memory.getValue().toString());
        Object[] options = {"Cancel", "OK", _newMemory};
        int retval = JOptionPane.showOptionDialog(null,
                                                  "Edit Current Memory Value", memory.getSystemName(),
                                                  0, JOptionPane.INFORMATION_MESSAGE, null,
                                                  options, options[2] );

        if (retval != 1) return;
        memory.setValue(_newMemory.getText());
    
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemoryIcon.class.getName());
}
