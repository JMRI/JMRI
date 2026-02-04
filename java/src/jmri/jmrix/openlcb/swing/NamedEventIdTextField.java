package jmri.jmrix.openlcb.swing;

import java.awt.event.*;

import jmri.util.swing.*;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.openlcb.OlcbAddress;

import org.openlcb.swing.EventIdTextField;

/**
 * This is an entry field for Event IDs that also 
 * accepts event names
 *
 * @author Bob Jacobsen  (C) 2024
 */
public class NamedEventIdTextField extends OvertypeTextArea {
        
    public NamedEventIdTextField(CanSystemConnectionMemo memo) {
        this(23, memo);  // heuristically-found default size
    }

    public NamedEventIdTextField(int size, CanSystemConnectionMemo memo) {
        super(size);
        this.memo = memo;
        
        // set up semi-automatic tool tip
        addMouseListener(JmriMouseListener.adapt(new JmriMouseListener() {
            @Override
            public void mouseEntered(JmriMouseEvent arg0) {
                // Generate dynamic tooltip text here
                setToolTipText(makeToolTipText(getText()));
            }

            @Override public void mouseClicked(JmriMouseEvent e) {}
            @Override public void mouseExited(JmriMouseEvent arg0) {}
            @Override public void mousePressed(JmriMouseEvent arg0) {}
            @Override public void mouseReleased(JmriMouseEvent arg0) {}
        }));

        // Add a focus listener to update the tooltip dynamically
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                setToolTipText(makeToolTipText(getText()));
            }

            @Override public void focusLost(FocusEvent e) {}
        });

        EventIdTextField.configurePopUp(this);
        
    }

    final CanSystemConnectionMemo memo;
    
    protected String makeToolTipText(String fieldText) {
        var address = new OlcbAddress(fieldText, memo);
        if (address.isFromName()) {
            return fieldText+" is "+address.toDottedString();
        }
        return "Enter an event ID or event name";
    }    
    
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NamedEventIdTextField.class);
}
