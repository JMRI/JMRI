package jmri.jmrit;

import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyListener;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.LocoAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool for selecting short/long address for DCC throttles.
 *
 * This is made more complex because we want it to appear easier. Some DCC
 * systems allow addresses like 112 to be either long (extended) or short;
 * others default to one or the other.
 * <p>
 * When locked (the default), the short/long selection is forced to stay in
 * synch with what's available from the current ThrottleManager. If unlocked,
 * this can differ if it's been explicity specified via the GUI (e.g. you can
 * call 63 a long address even if the DCC system can't actually do it right
 * now). This is useful in decoder programming, for example, where you might be
 * configuring a loco to run somewhere else.
 *
 * @author Bob Jacobsen Copyright (C) 2005
 */
public class DccLocoAddressSelector extends JPanel {

    JComboBox<String> box = null;
    JTextField text = new JTextField();

    public DccLocoAddressSelector() {
        super();
        if ((InstanceManager.getNullableDefault(jmri.ThrottleManager.class) != null)
                && !InstanceManager.throttleManagerInstance().addressTypeUnique()) {
            configureBox(InstanceManager.throttleManagerInstance().getAddressTypes());
        } else {
            configureBox(
                    new String[]{LocoAddress.Protocol.DCC_SHORT.getPeopleName(),
                        LocoAddress.Protocol.DCC_LONG.getPeopleName()});
        }
    }

    public DccLocoAddressSelector(String[] protocols) {
        super();
        configureBox(protocols);
    }

    void configureBox(String[] protocols) {
        box = new JComboBox<String>(protocols);
        box.setSelectedIndex(0);
        text = new JTextField();
        text.setColumns(4);
        text.setToolTipText(rb.getString("TooltipTextFieldEnabled"));
        box.setToolTipText(rb.getString("TooltipComboBoxEnabled"));

    }

    public void setLocked(boolean l) {
        locked = l;
    }

    public boolean getLocked(boolean l) {
        return locked;
    }
    private boolean locked = true;

    private boolean boxUsed = false;
    private boolean textUsed = false;
    private boolean panelUsed = false;

    /*
     * Get the currently selected DCC address.
     * <p>
     * This is the primary output of this class.
     * @return DccLocoAddress object containing GUI choices, or null if no entries in GUI
     */
    public DccLocoAddress getAddress() {
        // no object if no address
        if (text.getText().equals("")) {
            return null;
        }

        // ask the Throttle Manager to handle this!
        LocoAddress.Protocol protocol;
        if (InstanceManager.getNullableDefault(jmri.ThrottleManager.class) != null) {
            protocol = InstanceManager.throttleManagerInstance().getProtocolFromString((String) box.getSelectedItem());
            return (DccLocoAddress) InstanceManager.throttleManagerInstance().getAddress(text.getText(), protocol);
        }

        // nothing, construct a default
        int num = Integer.parseInt(text.getText());
        protocol = LocoAddress.Protocol.getByPeopleName((String) box.getSelectedItem());
        return new DccLocoAddress(num, protocol);
    }

    public void setAddress(DccLocoAddress a) {
        if (a != null) {
            if (a instanceof jmri.jmrix.openlcb.OpenLcbLocoAddress) {
                // now special case, should be refactored
                jmri.jmrix.openlcb.OpenLcbLocoAddress oa = (jmri.jmrix.openlcb.OpenLcbLocoAddress) a;
                text.setText(oa.getNode().toString());
                box.setSelectedItem(jmri.LocoAddress.Protocol.OPENLCB.getPeopleName());
            } else {
                text.setText("" + a.getNumber());
                if (InstanceManager.getNullableDefault(jmri.ThrottleManager.class) != null) {
                    box.setSelectedItem(InstanceManager.throttleManagerInstance().getAddressTypeString(a.getProtocol()));
                } else {
                    box.setSelectedItem(a.getProtocol().getPeopleName());
                }
            }
        }
    }

    public void setVariableSize(boolean s) {
        varFontSize = s;
    }
    boolean varFontSize = false;

    /*
     * Put back to original state, clearing GUI
     */
    public void reset() {
        box.setSelectedIndex(0);
        text.setText("");
    }

    /* Get a JPanel containing the combined selector.
     * <p>
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

        if (varFontSize) {
            text.setFont(new Font("", Font.PLAIN, 32));
        }

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(text);
        if (!locked
                || ((InstanceManager.getNullableDefault(jmri.ThrottleManager.class) != null)
                && !InstanceManager.throttleManagerInstance().addressTypeUnique())) {
            p.add(box);
        }

        p.addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        changeFontSizes();
                    }
                });

        return p;
    }

    /**
     * The longest 4 character string. Used for resizing.
     */
    private static final String LONGEST_STRING = "MMMM";

    /**
     * A resizing has occurred, so determine the optimum font size for the
     * localAddressField.
     */
    private void changeFontSizes() {
        if (!varFontSize) {
            return;
        }
        double fieldWidth = text.getSize().width;
        int stringWidth = text.getFontMetrics(text.getFont()).
                stringWidth(LONGEST_STRING) + 8;
        int fontSize = text.getFont().getSize();
        if (stringWidth > fieldWidth) // component has shrunk.
        {
            while ((stringWidth > fieldWidth) && (fontSize > 12)) {
                fontSize -= 2;
                Font f = new Font("", Font.PLAIN, fontSize);
                text.setFont(f);
                stringWidth = text.getFontMetrics(text.getFont()).
                        stringWidth(LONGEST_STRING) + 8;
            }
        } else // component has grown
        {
            while ((fieldWidth - stringWidth > 10) && (fontSize < 48)) {
                fontSize += 2;
                Font f = new Font("", Font.PLAIN, fontSize);
                text.setFont(f);
                stringWidth = text.getFontMetrics(text.getFont()).
                        stringWidth(LONGEST_STRING) + 8;
            }
        }
    }

    /*
     * Provide a common setEnable call for the GUI components in the
     * selector
     */
    @Override
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

    public void setEnabledProtocol(boolean e) {
        box.setEnabled(e);
        if (e) {
            box.setToolTipText(rb.getString("TooltipComboBoxEnabled"));
        } else {
            box.setToolTipText(rb.getString("TooltipComboBoxDisabled"));
        }
    }

    /*
     * Get the text field for entering the number as a separate
     * component.  
     * <p>
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
    public JComboBox<String> getSelector() {
        if (boxUsed) {
            log.error("getSelector invoked after text already requested");
            return null;
        }
        boxUsed = true;
        return box;
    }

    /*
     * Override the addKeyListener method in JPanel so that we can set the
     * text box as the object listening for keystrokes
     */
    @Override
    public void addKeyListener(KeyListener l){
       super.addKeyListener(l);
       text.addKeyListener(l);
    }

    final static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.DccLocoAddressSelectorBundle");

    private final static Logger log = LoggerFactory.getLogger(DccLocoAddressSelector.class);
}
