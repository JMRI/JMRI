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

/**
 * An icon to display a status of a Memory.<P>
 * <P>
 * The value of the memory can't be changed with this icon.
 *<P>
 * @author Bob Jacobsen  Copyright (c) 2004
 * @version $Revision: 1.29 $
 */

public class MemoryIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public MemoryIcon() {

        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/misc/X-red.gif",
                            "resources/icons/misc/X-red.gif"));
        setDisplayLevel(PanelEditor.LABELS);
        // have to do following explicitly, after the ctor
        resetDefaultIcon();
        icon = true;
        text = false;
    }

    public MemoryIcon(LayoutEditor panel) {
        // super ctor call to make sure this is an icon label
        super(new String("   "));
        setDisplayLevel(LayoutEditor.LABELS);
        // have to do following explicitly, after the ctor
        resetDefaultIcon();
        setPanel(panel);
        icon = false;
        text = true;
        updateSize();
    }

    private void resetDefaultIcon() {
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
        if (map==null) map = new java.util.HashMap<String,NamedIcon>();
	}
	
	NamedIcon defaultIcon = null;
    String defaultText = "  ";
    // the associated Memory object
    Memory memory = null;
    
    // the map of icons
    java.util.HashMap<String,NamedIcon> map = null;

    /**
     * Attached a named Memory to this display item
      * @param pName Used as a system/user name to lookup the Memory object
     */
     public void setMemory(String pName) {
         if (InstanceManager.memoryManagerInstance()!=null) {
             memory = InstanceManager.memoryManagerInstance().
                 provideMemory(pName);
             if (memory != null) {
                 setMemory(memory);
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
    public void setMemory(Memory m) {
        if (memory != null) {
            memory.removePropertyChangeListener(this);
        }
        memory = m;
        if (memory != null) {
            displayState();
            memory.addPropertyChangeListener(this);
            setProperToolTip();
        }
    }

    public Memory getMemory() { return memory; }
    
    public java.util.HashMap<String,NamedIcon> getMap() { return map; }

    // display icons

    public void addKeyAndIcon(NamedIcon icon, String keyValue) {
        if (map == null) setMap(); // initialize if needed
    	map.put(keyValue, icon);
    	// drop size cache
    	//height = -1;
    	//width = -1;
        displayState(); // in case changed
    }

    //private int height = -1;
    /**
     * This now uses the layout editor version below
     * This may be called during the superclass ctor, so before 
     * construction of this object is complete.  Be careful about that!
     */
    /*protected int maxHeight() {
        return ((javax.swing.JLabel)this).getMaximumSize().height;  // defer to superclass
    }*/
    
    //private int width = -1;
    /**
     * This now uses the layout editor version below
     * This may be called during the superclass ctor, so before 
     * construction of this object is complete.  Be careful about that!
     */
    /*protected int maxWidth() {
        return ((javax.swing.JLabel)this).getMaximumSize().width;  // defer to superclass
    }*/

    // update icon as state of Memory changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property change: "
                                            +e.getPropertyName()
                                            +" is now "+e.getNewValue());
	if (e.getPropertyName().equals("value")) {
            displayState();
        }
    }

    public void setProperToolTip() {
        setToolTipText(getNameString());
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
    protected void showPopUp(MouseEvent e) {
        if (!getEditable()) return;
        ours = this;
        popup = new JPopupMenu();
        
        popup.add(new JMenuItem(getNameString()));
        checkLocationEditable(popup, getNameString());
		
        if (icon) {
            popup.add(new AbstractAction(rb.getString("Rotate")) {
                public void actionPerformed(ActionEvent e) {
                    // rotate all the icons, a real PITA
                    java.util.Iterator<NamedIcon> iterator = map.values().iterator();
                    while (iterator.hasNext()) {
                        NamedIcon next = iterator.next();
                        next.setRotation(next.getRotation()+1, ours);
                    }
                    displayState();
                }
            });

            popup.add(new AbstractAction(rb.getString("Remove")) {
                public void actionPerformed(ActionEvent e) {
                    remove();
                    dispose();
                }
            });
            addFixedItem(popup);
        } else if (text) {
            //popup.add(makeFontSizeMenu());

           //popup.add(makeFontStyleMenu());

            //popup.add(makeFontColorMenu());
            //New Entry
            if (getFixedWidth()==0)
                popup.add("Width= Auto");
            else
                popup.add("Width= " + this.fixedWidth);

            if (getFixedHeight()==0)
                popup.add("Height= Auto");           
            else
                popup.add("Height= " + this.fixedHeight);

            if (((fixedHeight==0) || (fixedWidth==0))&&(text))
               popup.add("Margin= " + this.getMargin());
               
            if (getHidden()) popup.add(rb.getString("Hidden"));
            else popup.add(rb.getString("NotHidden"));

        popup.addSeparator();
            addTextEditEntry(popup, false);
            popup.add(makeTextJustificationMenu());
            popup.add(makeBackgroundFontColorMenu());
            
            popup.add(textBorderMenu(getNameString()));
            if ((getFixedWidth()==0)||(getFixedHeight()==0)){
                popup.add(new AbstractAction("Set Margin Size") {
                    public void actionPerformed(ActionEvent e) {
                        String name = getNameString();
                        marginSizeEdit(name);
                    }
                });
            }

            addFixedItem(popup);
            addShowTooltipItem(popup);
            
            popup.add(new AbstractAction(rb.getString("Remove")) {
                public void actionPerformed(ActionEvent e) {
                    remove();
                    dispose();
                }
            });
            
            popup.add(new AbstractAction("Set Fixed Size") {
            public void actionPerformed(ActionEvent e) {
                String name = getNameString();
                fixedSizeEdit(name);
            }
        });

        } else if (!text && !icon)
            log.warn("showPopUp when neither text nor icon true");

        popup.add(new AbstractAction(rb.getString("EditIcon")) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });

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
        popup.add(setHiddenMenu());

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Drive the current state of the display from the state of the
     * Memory.
     */
    void displayState() {
        log.debug("displayState");
    	if (memory == null) {  // use default if not connected yet
            setIcon(defaultIcon);
    		updateSize();
    		return;
    	}
		Object key = memory.getValue();
		if (key != null) {
		    if (map == null) {
		        // no map, attempt to show object directly
                Object val = memory.getValue();
                if (val instanceof String) {
                    if ((memory.getValue().equals("")) && (getLayoutPanel()!=null))
                        setText(defaultText);
                    else
                        setText((String) memory.getValue());

                    //setText((String) memory.getValue());
                    setIcon(null);
                    text = true;
                    icon = false;
    		        updateSize();
                    return;
                } else if (val instanceof javax.swing.ImageIcon) {
                    setIcon((javax.swing.ImageIcon) memory.getValue());
                    setText(null);
                    text = false;
                    icon = true;
    		        updateSize();
                    return;
                } else if (val instanceof Integer) {
                    setText(((Integer) memory.getValue()).toString());
                    setIcon(null);
                    text = true;
                    icon = false;
    		        updateSize();
                    return;
                } else if (val instanceof Float) {
                    setText(((Float) memory.getValue()).toString());
                    setIcon(null);
                    text = true;
                    icon = false;
    		        updateSize();
                    return;
                } else log.warn("can't display current value of "+memory.getSystemName()+
                                ", val= "+val);
		    } else {
		        // map exists, use it
			    NamedIcon newicon = map.get(key.toString());
			    if (newicon!=null) {
                    
                    setText(null);
				    super.setIcon(newicon);
                    text = false;
                    icon = true;
    		        updateSize();
				    return;
			    } else {
			        // no match, use default
		            setIcon(defaultIcon);
                    
                    setText(null);
                    text = false;
                    icon = true;
    		        updateSize();
			    }
		    }
		} else {
		    // If fall through to here, no Memory value, set icon to default.
            if (getLayoutPanel()!=null) {
                setIcon(null);
                setText(defaultText);
                text = true;
                icon = false;
            } else {
                setIcon(defaultIcon);
                setText(null);
                text = false;
                icon = true;
            }
    		updateSize();
        }
    }

    void edit() {
        if (_editorFrame != null) {
            _editorFrame.setLocationRelativeTo(null);
            _editorFrame.toFront();
            return;
        }
        _editor = new IconAdder();
        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                editMemory();
            }
        };
        makeAddIconFrame("EditMemory", "addMemValueToPanel", 
                                             "SelectMemory", _editor);
        _editor.setPickList(PickListModel.memoryPickModelInstance());
        _editor.complete(addIconAction, null, true, true);
        _editor.setSelection(memory);
    }
    void editMemory() {
        setMemory((Memory)_editor.getTableSelection());
        updateSize();
        _editorFrame.dispose();
        _editorFrame = null;
        _editor = null;
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
    private int fixedWidth=0;
    private int fixedHeight=0;
    
    public int getFixedWidth(){
        return fixedWidth;
    }
    
    public int getFixedHeight(){
        return fixedHeight;
    }
    
    public void setFixedSize(int width, int height){
        
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
    }
 
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
    protected int maxHeight() {
        if(isIcon()){
            return namedIcon.getIconHeight();
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
    protected int maxWidth() {
        if(isIcon()){
            return namedIcon.getIconWidth();
        }
        if ((getFixedWidth()==0) && (getMargin()==0))
            return ((javax.swing.JLabel)this).getMaximumSize().width;  // defer to superclass
        else if ((getFixedWidth()==0) && (getMargin()!=0))
            return ((javax.swing.JLabel)this).getMaximumSize().width+(getMargin()*2);
        //else if ((getFixedWidth()!=0) && (getMargin()!=0))
         //   return getFixedWidth();
        return getFixedWidth();
    }

        private ButtonGroup justButtonGroup;
    
    JMenu makeTextJustificationMenu() {
        JMenu justMenu = new JMenu("Justification");
        justButtonGroup = new ButtonGroup();
        addJustificationMenuEntry(justMenu, LEFT);
        addJustificationMenuEntry(justMenu, RIGHT);
        addJustificationMenuEntry(justMenu, CENTRE);
        return justMenu;
    }
    
    void addJustificationMenuEntry(JMenu menu, final int just) {
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
