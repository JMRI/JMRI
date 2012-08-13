package jmri.jmrit;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.BoxLayout;

import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.InstanceManager;
import jmri.ThrottleManager;

import java.util.ResourceBundle;
import java.util.ArrayList;

import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Tool for selecting short/long address for DCC throttles.
 *
 * This is made more complex because we want it to appear easier.
 * Some DCC systems allow addresses like 112 to be either long (extended)
 * or short; others default to one or the other.
 * <P>
 * When locked (the default), the short/long selection
 * is forced to stay in synch with what's available from the 
 * current ThrottleManager.  If unlocked, this can differ if
 * it's been explicity specified via the GUI  (e.g. you can call
 * 63 a long address even if the DCC system can't actually do it
 * right now). This is useful in decoder programming, for example,
 * where you might be configuring a loco to run somewhere else.
 *
 * @author     Bob Jacobsen   Copyright (C) 2005
 * @version    $Revision$
 */
public class DccLocoAddressSelector extends JPanel
{

    JComboBox box = null;
    JTextField text = new JTextField();
    
    public DccLocoAddressSelector() {
        super();
        if (rb == null) rb = ResourceBundle.getBundle("jmri.jmrit.DccLocoAddressSelectorBundle");
        if ((InstanceManager.throttleManagerInstance() !=null) 
                && !InstanceManager.throttleManagerInstance().addressTypeUnique()){
//            int[] addressTypes = InstanceManager.throttleManagerInstance().getAddressIntTypes();
            box = new JComboBox(InstanceManager.throttleManagerInstance().getAddressTypes());
            //box = new JComboBox(InstanceManager.throttleManagerInstance().getAddressTypes());
        } else {
            box = new JComboBox(
                new String[]{rb.getString("ComboItemShort"),
                             rb.getString("ComboItemLong")});
        }
        box.insertItemAt(rb.getString("ComboItemNone"), 0);
        box.setSelectedIndex(0);
        text = new JTextField();
        text.setColumns(4);
        text.setToolTipText(rb.getString("TooltipTextFieldEnabled"));
        box.setToolTipText(rb.getString("TooltipComboBoxEnabled"));
    }
    
    public void setLocked(boolean l) {
        locked = l;
    }
    public boolean getLocked(boolean l) { return locked; }
    private boolean locked = true;
    
    private boolean boxUsed = false;
    private boolean textUsed = false;
    private boolean panelUsed = false;
    
    /*
     * Get the currently selected DCC address.
     * <P>
     * This is the primary output of this class.
     * @return DccLocoAddress object containing GUI choices, or null if no entries in GUI
     */
    public DccLocoAddress getAddress() {
        // no object if no address
        if (text.getText().equals("")) return null;
        
        int num = Integer.parseInt(text.getText());
        setMode(num);
        int protocol = LocoAddress.DCC;
        if(InstanceManager.throttleManagerInstance()!=null){
            protocol = InstanceManager.throttleManagerInstance().getProtocolFromString((String)box.getSelectedItem());
        }
        return new DccLocoAddress(num,protocol);
    }

    public void setAddress(DccLocoAddress a) {
        if (a!=null) {
            text.setText(""+a.getNumber());
            if(InstanceManager.throttleManagerInstance()!=null){
                box.setSelectedItem(InstanceManager.throttleManagerInstance().getAddressTypeString(a.getProtocol()));
                //protocol = InstanceManager.throttleManagerInstance().getProtocolFromString((String)box.getSelectedItem());
            }
        }
    }
    
    public void setVariableSize(boolean s) { varFontSize = s; }
    boolean varFontSize = false;
    
    /*
     * Put back to original state, clearing GUI
     */
     
    public void reset() {
        box.setSelectedIndex(0);
        text.setText("");
    }
    
    /* Get a JPanel containing the combined selector.
     *
     * <P>
     * Because Swing only allows a component to be inserted in one
     * container, this can only be done once
     */
    public JPanel getCombinedJPanel() { 
        if (panelUsed) {
            log.error("getCombinedPanel invoked after panel already requested");
            return null;
        }
        if (textUsed) {
            log.error("getCombinedPanel invoked after text already requested");
            return null;
        }
        if (boxUsed) {
            log.error("getCombinedPanel invoked after text already requested");
            return null;
        }
        panelUsed = true;
        
        if (varFontSize) text.setFont(new Font("", Font.PLAIN, 32));
         
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(text);
        if (!locked || 
              ( (InstanceManager.throttleManagerInstance() !=null) 
                    && !InstanceManager.throttleManagerInstance().addressTypeUnique()
              )
           )
            p.add(box);
        
         p.addComponentListener(
                 new ComponentAdapter()
         {
             public void componentResized(ComponentEvent e)
             {
                 changeFontSizes();
             }
         });

        return p;
    }     

    /** The longest 4 character string. Used for resizing. */
    private static final String LONGEST_STRING = "MMMM";

    /**
     * A resizing has occurred, so determine the optimum font size
     * for the localAddressField.
     */
    private void changeFontSizes()
    {
        if (!varFontSize) return;
        double fieldWidth = text.getSize().width;
        int stringWidth = text.getFontMetrics(text.getFont()).
                          stringWidth(LONGEST_STRING)+8;
        int fontSize = text.getFont().getSize();
        if (stringWidth > fieldWidth) // component has shrunk.
        {
            while ( (stringWidth > fieldWidth) && (fontSize>12) )
            {
                fontSize -= 2;
                Font f = new Font("", Font.PLAIN, fontSize);
                text.setFont(f);
                stringWidth = text.getFontMetrics(text.getFont()).
                              stringWidth(LONGEST_STRING)+8;
            }
        }
        else // component has grown
        {
            while ( (fieldWidth - stringWidth > 10) && (fontSize<48) )
            {
                fontSize += 2;
                Font f = new Font("", Font.PLAIN, fontSize);
                text.setFont(f);
                stringWidth = text.getFontMetrics(text.getFont()).
                              stringWidth(LONGEST_STRING)+8;
            }
        }
    }
    
    /*
     * Provide a common setEnable call for the GUI components in the
     * selector
     */
    public void setEnabled(boolean e) {
        text.setEditable(e);
        text.setEnabled(e);
        box.setEnabled(e);
        if (e) {
            text.setToolTipText(rb.getString("TooltipTextFieldEnabled"));
            box.setToolTipText(rb.getString("TooltipComboBoxEnabled"));
       } else {
            text.setToolTipText(rb.getString("TooltipTextFieldDisabled"));
            box.setToolTipText(rb.getString("TooltipComboBoxDisabled"));
       } 
    }
    
    /*
     * Get the text field for entering the number as a separate
     * component.  
     * <P>
     * Because Swing only allows a component to be inserted in one
     * container, this can only be done once
     */
    public JTextField getTextField() { 
        if (textUsed) {
            reportError("getTextField invoked after text already requested");
            return null;
        }
        textUsed = true;
        return text;
    }     
    
    void reportError(String msg) {
        log.error(msg, new Exception("traceback"));
    }
    
    /*
     * Get the selector box for picking long/short as a separate
     * component.
     * Because Swing only allows a component to be inserted in one
     * container, this can only be done once
     */
    public JComboBox getSelector() { 
        if (boxUsed) {
            log.error("getSelector invoked after text already requested");
            return null;
        }
        boxUsed = true;
        return box;
    }     
    
    //This is only required for DCC when selecting between short and long address
    protected void setMode(int address) {
    
        // IAre we locked, and is there a throttle manager?
        ThrottleManager tf = InstanceManager.throttleManagerInstance();
        if (locked && tf != null) {
            boolean dcclongshort = false;
            for(int i: tf.getAddressIntTypes()){
                if(i==LocoAddress.DCC_SHORT){
                    for(int j:tf.getAddressIntTypes()){
                        if(j==LocoAddress.DCC_LONG){
                            dcclongshort=true;
                        }
                    }
                }
            }

            //Only check if the throttle manager specifically supports dcc long and dcc short
            if(dcclongshort){
                // yes, lets make some checks of required modes

                // if it has to be long, handle that
                if (tf.canBeLongAddress(address) && !tf.canBeShortAddress(address)) {
                    box.setSelectedItem(tf.getAddressTypeString(LocoAddress.DCC_LONG));
                    return;
                }

                // if it has to be short, handle that
                if (!tf.canBeLongAddress(address) && tf.canBeShortAddress(address)) {
                   box.setSelectedItem(tf.getAddressTypeString(LocoAddress.DCC_SHORT));
                    return;
                }
            }
        }
        
        // done checking for required modes
                
        // now we're in the "could be either" place; leave selection if possible
        switch (box.getSelectedIndex()) {
            case 0:
                { // well, now we've got a problem; no clue, so guess short or even set to the first available protocol
                    box.setSelectedIndex(1);
                    return;
                }
            case 1:
            case 2:
            default:
        }
    }
    
    static ResourceBundle rb = null;
        
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DccLocoAddressSelector.class.getName());
}