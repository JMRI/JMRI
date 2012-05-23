package jmri.jmrit.display.layoutEditor;
/**
 * An icon to display a status of a Memory.<P>
 */
import jmri.jmrit.catalog.NamedIcon;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.AbstractAction;
import java.awt.event.ActionListener;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

// This is the same name as display.MemoryIcon, but a very
// separate class. That's not good. Unfortunately, it's too 
// hard to disentangle that now because it's resident in the
// panel file that have been written out, so we just annote 
// the fact.
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class MemoryIcon extends jmri.jmrit.display.MemoryIcon {

    String defaultText = " ";

    public MemoryIcon(String s, LayoutEditor panel) {
        super(s, panel);
        log.debug("MemoryIcon ctor= "+MemoryIcon.class.getName());
    }

    public void setText(String text) {
        if (text==null || text.length()==0) {
            super.setText(defaultText); 
        } else {
            super.setText(text); 
        }
    }
    
    LayoutBlock lBlock = null;
    
    public LayoutBlock getLayoutBlock(){
        return lBlock;
    }
    
    public void setLayoutBlock(LayoutBlock lb){
        lBlock = lb;
    }
    
    public void displayState() {
        log.debug("displayState");
    	if (getMemory() == null) {  // use default if not connected yet
            setText(defaultText);
    		updateSize();
    		return;
    	}
        if(re!=null){
            jmri.InstanceManager.throttleManagerInstance().removeListener(re.getDccLocoAddress(), this);
            re=null;
        }
		Object key = getMemory().getValue();
		if (key != null) {
            java.util.HashMap<String, NamedIcon> map = getMap();
		    if (map == null) {
		        // no map, attempt to show object directly
                Object val = key;
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
                    if (val.equals(""))
                        setText(defaultText);
                    else
                        setText((String) val);
                    setIcon(null);
                    _text = true;
                    _icon = false;
    		        updateSize();
                    return;
                } else if (val instanceof javax.swing.ImageIcon) {
                    setIcon((javax.swing.ImageIcon) val);
                    setText(null);
                    _text = false;
                    _icon = true;
    		        updateSize();
                    return;
                } else if (val instanceof Number) {
                    setText(val.toString());
                    setIcon(null);
                    _text = true;
                    _icon = false;
    		        updateSize();
                    return;
                } else log.warn("can't display current value of "+getNamedMemory().getName()+
                                ", val= "+val+" of Class "+val.getClass().getName());
		    } else {
		        // map exists, use it
			    NamedIcon newicon = map.get(key.toString());
			    if (newicon!=null) {
                    
                    setText(null);
				    super.setIcon(newicon);
                    _text = false;
                    _icon = true;
    		        updateSize();
				    return;
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
    
    JCheckBoxMenuItem  updateBlockItem = new JCheckBoxMenuItem("Update Block Details");
    
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable()) {
            popup.add(updateBlockItem);
            updateBlockItem.setSelected (updateBlockValueOnChange());
            updateBlockItem.addActionListener(new ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    updateBlockValueOnChange(updateBlockItem.isSelected());
                }
            });
            return super.showPopUp(popup);
        }  // end of selectable
        return false;
    }
    
    protected void editMemoryValue(){
        JTextField newMemory = new JTextField(20);
        if (getMemory().getValue()!=null)
            newMemory.setText(getMemory().getValue().toString());
        Object[] options = {"Cancel", "OK", newMemory};
        int retval = JOptionPane.showOptionDialog(this,
                                                  "Edit Current Memory Value", getNamedMemory().getName(),
                                                  0, JOptionPane.INFORMATION_MESSAGE, null,
                                                  options, options[2] );

        if (retval != 1) return;
        log.info(updateBlockValue);
        log.info(lBlock);
        if(updateBlockValue && lBlock!=null){
            lBlock.getBlock().setValue(newMemory.getText());
        } else {
            getMemory().setValue(newMemory.getText());
            updateSize();
        }
    }
    
    public void setMemory(String pName){
        super.setMemory(pName);
        lBlock = jmri.InstanceManager.layoutBlockManagerInstance().getBlockWithMemoryAssigned(getMemory());
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemoryIcon.class.getName());
}

