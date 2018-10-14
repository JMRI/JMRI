package jmri.jmrix.loconet.ds64;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import jmri.util.swing.ValidatedTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a swing object, for use by the Ds64TabbedPanel tool, which allows 
 * display and configuration of turnout number and position.
 * <p>
 * Turnout numbering is the same as seen on a Digitrax throttle display; Tools 
 * using values from objects of this type must provide the appropriate transform
 * to create turnout numbering which is suitable for use within LocoNet messaging.
 *
 * @author B. Milhaupt Copyright (C) 2011, 2012, 2013, 2014, 2015, 2017
 */
public class SimpleTurnoutStateEntry extends SimpleTurnout {
    private JPanel entryPanel = null;

    /**
     * Tracks the "user-friendly" turnout address.
     * <p>
     * Turnout numbering is the same as seen on a Digitrax throttle display; Tools 
     * using values from objects of this type must provide the appropriate transform
     * to create turnout numbering which is suitable for use within LocoNet messaging.
     */
    public ValidatedTextField addressField = null;

    /**
     * Tracks whether the associated turnout is "thrown".
     */
    public JRadioButton thrownRadioButton;

    /**
     * Tracks whether the associated turnout is "closed".
     */
    public JRadioButton closedRadioButton;

    /**
     * Tracks whether the object is in-use or not, as seen in some aspects of
     * DS64 configuration.
     */
    public JRadioButton unusedRadioButton;

    /**
     * Constructor used when the current address and position are not known.  It
     * is assumed that the turnout address is 1, that the turnout is "closed", and
     * that the turnout is "valid".
     * <p>
     * Turnout numbering is the same as seen on a Digitrax throttle display; Tools 
     * using values from objects of this type must provide the appropriate transform
     * to create turnout numbering which is suitable for use within LocoNet messaging.
     */
    public SimpleTurnoutStateEntry() {
        this(1,true);
    }

    /**
     * Constructor used when the current address and position are known.  Turnout
     * "validity" is assumed to be "valid".
     * <p>
     * Turnout numbering is the same as seen on a Digitrax throttle display; Tools 
     * using values from objects of this type must provide the appropriate transform
     * to create turnout numbering which is suitable for use within LocoNet messaging.
     * 
     * @param address turnout address
     * @param isClosed  true if turnout is closed, else false
     */
    public SimpleTurnoutStateEntry(Integer address, boolean isClosed) {
        super(address,isClosed);
        thrownRadioButton = new JRadioButton(Bundle.getMessage("TurnoutStateThrown"));
        closedRadioButton = new JRadioButton(Bundle.getMessage("TurnoutStateClosed"));
        unusedRadioButton = null;
        addressField = new ValidatedTextField(5, true, 1, 2048, Bundle.getMessage("ErrorTextAddressInvalid"));
        entryPanel = null;
        addressField.setText(Integer.toString(address));
        setAddressLastQueriedValue(address);
        if (isClosed == true) {
            thrownRadioButton.setSelected(false);
            closedRadioButton.setSelected(true);
        }
        else {
            thrownRadioButton.setSelected(true);
            closedRadioButton.setSelected(false);
        }
        thrownRadioButton.addFocusListener(new java.awt.event.FocusListener() {

            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                // eat this focus change event.
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (thrownRadioButton.isSelected()) {
                    setIsClosed(false);
                }
            }
        });

        closedRadioButton.addFocusListener(new java.awt.event.FocusListener() {

            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                // eat this focus change event.
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (closedRadioButton.isSelected()) {
                    setIsClosed(true);
                }
            }
        });
    }
    
    /**
     * Constructor used when the current address, position, and "validity" 
     * state are known.
     * <p>
     * Turnout numbering is the same as seen on a Digitrax throttle display; Tools 
     * using values from objects of this type must provide the appropriate transform
     * to create turnout numbering which is suitable for use within LocoNet messaging.
     * 
     * @param address turnout address
     * @param closed  true if turnout is closed, else false
     * @param unused  true if turnout is unused, else false
     */
    public SimpleTurnoutStateEntry(Integer address, boolean closed, boolean unused) {
        super(address, closed, unused);
        thrownRadioButton = new JRadioButton(Bundle.getMessage("TurnoutStateThrown"));
        closedRadioButton = new JRadioButton(Bundle.getMessage("TurnoutStateClosed"));
        unusedRadioButton = new JRadioButton(Bundle.getMessage("RadioButtonTextUnused"));
        addressField = new ValidatedTextField(5, true, 1, 2048, Bundle.getMessage("ErrorTextAddressInvalid"));
        entryPanel = null;
        if (unused) {
            addressField.setText("");
        } else {
            addressField.setText(Integer.toString(address));
        }
        setAddressLastQueriedValue(address);
        if (closed == true) {
            thrownRadioButton.setSelected(false);
            unusedRadioButton.setSelected(false);
            closedRadioButton.setSelected(true);
        }
        else {
            thrownRadioButton.setSelected(true);
            unusedRadioButton.setSelected(false);
            closedRadioButton.setSelected(false);
        }
        
        thrownRadioButton.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                // eat this focus change event.
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (thrownRadioButton.isSelected()) {
                    setIsClosed(false);
                }
            }
        });

        closedRadioButton.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                // eat this focus change event.
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (closedRadioButton.isSelected()) {
                    setIsClosed(true);
                }
            }
        });

        unusedRadioButton.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                // eat this focus change event.
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (unusedRadioButton.isSelected()) {
                    setIsUnused();
                    addressField.setText("");
                }
            }
        });

    }
    
    /**
     *
     * @return the JPanel related to this object
     */
    public JPanel getEntryPanel() { return this.entryPanel; }

    /**
     * Creates a GUI Panel for managing the address and position of a turnout, 
     * as used in configuring the turnout address of a DS64 output.
     *
     * @param label a text string to be displayed in the JPanel with the turnout 
     *              address and position 
     * @return a JPanel containing the label, the turnout address text field, and 
     *              position GUI elements
     */
    public JPanel createEntryPanel(String label) {
        entryPanel = new JPanel();
        entryPanel.setLayout(new FlowLayout());
        entryPanel.add(new JLabel(label));
        entryPanel.add(addressField);

        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2,BoxLayout.X_AXIS));
        ButtonGroup g = new ButtonGroup();
        
        if (addressField.getText().length() == 0) {
            closedRadioButton.setSelected(false);
            thrownRadioButton.setSelected(false);
            if (unusedRadioButton != null) {
                unusedRadioButton.setSelected(true);
            }
            addressField.setText("");
        } else if (Integer.parseInt(addressField.getText()) == 2048) {
            closedRadioButton.setSelected(false);
            thrownRadioButton.setSelected(false);
            if (unusedRadioButton != null) {
                unusedRadioButton.setSelected(true);
            }
            addressField.setText("");
        }
        else {
            if (unusedRadioButton != null) {
                unusedRadioButton.setSelected(false);
            }
            closedRadioButton.setSelected(getIsClosed());
            thrownRadioButton.setSelected(!getIsClosed());
        }

        g.add(thrownRadioButton);
        g.add(closedRadioButton);
        p2.add(thrownRadioButton);
        p2.add(closedRadioButton);
        if (unusedRadioButton != null) {
            g.add(unusedRadioButton);
            p2.add(unusedRadioButton);
        } 
        entryPanel.add(p2);
        return entryPanel;
    }

    /**
     * Retrieve the GUI element which holds a turnout address
     * @return turnout address
     */
    public ValidatedTextField getAddressField() {
        return addressField;
    }

    @Override
    public void setAddress(Integer addr) {
        log.debug("simpleturnoutstateentry - setaddress {}", addr);
        super.setAddress(addr);
        if (isValid()) {
            addressField.setText(String.valueOf(addr));
        }
        else {
            addressField.setText("");
        }
        addressField.updateUI();
    }
    
    /**
     * Establish the most recent value known to be found in the hardware.  
     * Value is used to help determine colorization of the swing GUI text field.
     * 
     * @param addr Turnout address
     */
    final public void setAddressLastQueriedValue(Integer addr) {
        addressField.setLastQueriedValue(String.valueOf(addr));
    }

    @Override
    public void setIsClosed(boolean isclosed) {
        super.setIsClosed(isclosed);
        closedRadioButton.setSelected(getIsClosed());
        thrownRadioButton.setSelected(!getIsClosed());
        closedRadioButton.updateUI();
        thrownRadioButton.updateUI();
        if (unusedRadioButton != null) {
            unusedRadioButton.setSelected(false);
            unusedRadioButton.updateUI();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SimpleTurnoutStateEntry.class);

}
