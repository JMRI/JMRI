package jmri.jmrix.openlcb.swing;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.event.*;

import javax.swing.*;

import jmri.util.swing.*;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.openlcb.OlcbAddress;

import org.openlcb.EventID;
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

        var popup = EventIdTextField.createPopupMenu(this);
        // add a custom copy operator to capture the numerical value
        JMenuItem menuItem = new JMenuItem("Copy Numerical Event ID");
        popup.add(menuItem);
        var self = this;
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = new OlcbAddress(self.getText(), memo).toDottedString();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        self.selectAll();
                    }
                });
                StringSelection eventToCopy = new StringSelection(s);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(eventToCopy, new ClipboardOwner() {
                    @Override
                    public void lostOwnership(Clipboard clipboard, Transferable transferable) {
                    }
                });
            }
        });
        this.setComponentPopupMenu(popup);
    }

    final CanSystemConnectionMemo memo;
    
    protected String makeToolTipText(String fieldText) {
        var address = new OlcbAddress(fieldText, memo);
        if (address.isFromName()) {
            return fieldText+" is "+address.toDottedString();
        }
        return "Enter an event ID or event name";
    }    
    
    public EventID getEventID() {
        return new EventID(new OlcbAddress(getText(), memo).toDottedString());
    }
    
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NamedEventIdTextField.class);
}
