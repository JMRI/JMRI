package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.jmrit.catalog.NamedIcon;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import java.awt.event.MouseEvent;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import java.util.ResourceBundle;


/**
 * An icon to display a status of a Memory.<P>
 * <P>
 * The value of the memory can't be changed with this icon.
 * <P>
 * This module is derived with only a few changes from MemoryIcon.java by 
 *   Bob Jacobsen Copyright (c) 2004. A name change was needed to work around 
 *   the hard dependence on PanelEditor in MemoryIconXml.java, without risking 
 *   compromising existing PanelEditor panels. 
 * <P>
 * Another difference from MemoryIcon.java, is that this defaults to a text 
 *   instead of the red X icon displayed when Panel Editor is loaded. If the
 *   user needs to "find" the MemoryIcon, putting text into the Memory Table
 *   is suggested.
 * <P>
 * This module has been modified (from MemoryIcon.java) to use a resource
 *	 bundle for its user-seen text, like other LayoutEditor modules.
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @version $Revision: 1.11 $
 */

public class LayoutMemoryIcon extends LayoutPositionableLabel implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.LayoutEditorBundle");

    public LayoutMemoryIcon() {
        // super ctor call to make sure this defaults to a text label
        super (new String("  "));                    
        setDisplayLevel(LayoutEditor.LABELS);
        // have to do following explicitly, after the ctor
        resetDefaultIcon();
		text = true;
		icon = false;
        updateSize();
    }

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
            setSize(fixedWidth, fixedHeight);
            //setMargin(0);
        } else if ((width!=0) && (height==0)){
            setSize(fixedWidth, maxHeight());
            //setMargin(0);
        } else if ((width==0) && (height!=0)){
            setSize(maxWidth(), fixedHeight);
            //setMargin(0);
        } else {
            //setLocation(getX(), getY());
            setSize(maxWidth(), maxHeight());
        }
        //displayState();
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
                displayState();
                memory.addPropertyChangeListener(this);
                setProperToolTip();
            } else {
                log.error("Memory '"+pName+"' not available, icon won't see changes");
            }
        } else {
            log.error("No MemoryManager for this protocol, icon won't see changes");
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
     * This may be called during the superclass ctor, so before 
     * construction of this object is complete.  Be careful about that!
     */
    protected int maxHeight() {
        if ((getFixedHeight()==0) && (getMargin()==0))
            return ((javax.swing.JLabel)this).getMaximumSize().height;  // defer to superclass
        else if ((getFixedHeight()==0) && (getMargin()!=0))
            return ((javax.swing.JLabel)this).getMaximumSize().height+(getMargin()*2);
        return getFixedHeight();
        
    }
    
    //private int width = -1;
    /**
     * This may be called during the superclass ctor, so before 
     * construction of this object is complete.  Be careful about that!
     */
    protected int maxWidth() {
        if ((getFixedWidth()==0) && (getMargin()==0))
            return ((javax.swing.JLabel)this).getMaximumSize().width;  // defer to superclass
        else if ((getFixedWidth()==0) && (getMargin()!=0))
            return ((javax.swing.JLabel)this).getMaximumSize().width+(getMargin()*2);
        return getFixedWidth();
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

    public void setProperToolTip() {
        setToolTipText(getNameString());
    }

    String getNameString() {
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
		popup.add("x= " + this.getX());
		popup.add("y= " + this.getY());

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

        popup.addSeparator();
        
        popup.add(new AbstractAction(rb.getString("SetXY")) {
				public void actionPerformed(ActionEvent e) {
					String name = getNameString();
					displayCoordinateEdit(name);
				}
			});
        
        popup.add(new AbstractAction("Set Fixed Size") {
            public void actionPerformed(ActionEvent e) {
                String name = getNameString();
                fixedSizeEdit(name);
            }
        });
        

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
        
        } else if (text) {
            popup.add(makeTextJustificationMenu());
            
            popup.add(makeFontSizeMenu());

            popup.add(makeFontStyleMenu());

            popup.add(makeFontColorMenu());
            
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

        } else
            log.warn("showPopUp when neither text nor icon true");

        if (selectable) {
            popup.add(new JSeparator());
    
            java.util.Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next().toString();
                //String value = map.get(key).getName();
                popup.add(new AbstractAction(key) {
                    public void actionPerformed(ActionEvent e) {
                        String key = e.getActionCommand();
                        memory.setValue(key);
                    }
                });
            }
        }  // end of selectable

        popup.show(e.getComponent(), e.getX(), e.getY());
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
    
    /*JMenu textBorderMenu() {
        JMenu borderMenu = new JMenu("Border Menu");
        borderMenu.add("Border Size= " + getBorderSize());
        borderMenu.add(new AbstractAction("Set Border Size") {
				public void actionPerformed(ActionEvent e) {
					String name = getNameString();
					displayBorderEdit(name);
				}
			});
        
        borderMenu.add(makeBorderColorMenu());
        return borderMenu;
    }*/

    
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
                    if (memory.getValue().equals(""))
                        setText(defaultText);
                    else
                        setText((String) memory.getValue());
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
                } else log.warn("can't display current value of "+memory.getSystemName());
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
		    setIcon(null);
            setText(defaultText);
            text = true;
            icon = false;
    		updateSize();
        }
    }

    public void updateSize() {

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
    
    /**
     * Clicks are ignored
     * @param e
     */
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

    public void dispose() {
        memory.removePropertyChangeListener(this);
        memory = null;
        super.dispose();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutMemoryIcon.class.getName());
}
