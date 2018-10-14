package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a graphical representation of the DCC address, either long or short
 *
 * <p>
 * Expects one or more of the variables called:
 * <ul>
 * <li>Short Address
 * <li>Long Address
 * <li>Address Format (an Enum variable to select)
 * </ul>
 * and handles the cases where:
 * <ul>
 * <li>All three are present - the normal advanced decoder case
 * <li>Short Address is present and Long Address is not
 * <li>Long Address is present and Short Address is not
 * </ul>
 * At least one of Short Address and Long Address must be present!
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2012
 */
public class DccAddressPanel extends JPanel {

    JTextField val = new JTextField(6);

    VariableValue primaryAddr = null;
    VariableValue extendAddr = null;
    EnumVariableValue addMode = null;

    VariableTableModel variableModel = null;

    /**
     * Ctor using default label for the address.
     *
     * @param mod The current table of variables, used to locate the status
     *            information needed.
     */
    public DccAddressPanel(VariableTableModel mod) {
        this(mod, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("TextDccAddress"));
    }

    public DccAddressPanel(VariableTableModel mod, String label) {
        variableModel = mod;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // arrange for the field to be updated when any of the variables change
        java.beans.PropertyChangeListener dccNews = new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                updateDccAddress();
            }
        };

        // connect to variables
        primaryAddr = variableModel.findVar("Short Address");
        if (primaryAddr == null) {
            log.debug("DCC Address monitor did not find a Short Address variable");
        } else {
            primaryAddr.addPropertyChangeListener(dccNews);
        }

        extendAddr = variableModel.findVar("Long Address");
        if (extendAddr == null) {
            log.debug("DCC Address monitor did not find an Long Address variable");
        } else {
            extendAddr.addPropertyChangeListener(dccNews);
        }

        addMode = (EnumVariableValue) variableModel.findVar("Address Format");
        if (addMode == null) {
            log.debug("DCC Address monitor didnt find an Address Format variable");
        } else {
            addMode.addPropertyChangeListener(dccNews);
        }

        // show the selection
        if (addMode != null) {
            add(addMode.getNewRep("radiobuttons"));
        }

        // show address field
        add(new JLabel(label));
        val.setToolTipText(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("ToolTipDccAddress"));
        add(val);

        new DccAddressVarHandler(primaryAddr, extendAddr, addMode) {
            @Override
            protected void doPrimary() {
                // short address commonRep will be JTextField if variable, JLabel if constant
                JTextField f;
                if (primaryAddr.getCommonRep() instanceof JTextField) {
                    f = (JTextField) primaryAddr.getCommonRep();
                } else {
                    f = new JTextField();
                    f.setText(((JLabel) primaryAddr.getCommonRep()).getText());
                }
                val.setBackground(primaryAddr.getCommonRep().getBackground());
                val.setDocument(f.getDocument());
            }

            @Override
            protected void doExtended() {
                // long address commonRep will be JTextField if variable, JLabel if constant
                JTextField f;
                if (extendAddr.getCommonRep() instanceof JTextField) {
                    f = (JTextField) extendAddr.getCommonRep();
                } else {
                    f = new JTextField();
                    f.setText(((JLabel) extendAddr.getCommonRep()).getText());
                }
                val.setBackground(extendAddr.getCommonRep().getBackground());
                val.setDocument(f.getDocument());
            }
        };

        // start listening for changes to this value
        val.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new DccAddressVarHandler(primaryAddr, extendAddr, addMode) {
                    @Override
                    protected void doPrimary() {
                        // short address mode
                        primaryAddr.updatedTextField();
                        val.setBackground(primaryAddr.getCommonRep().getBackground());
                        if (log.isDebugEnabled()) {
                            log.debug("set color: " + primaryAddr.getCommonRep().getBackground());
                        }
                    }

                    @Override
                    protected void doExtended() {
                        // long address
                        extendAddr.updatedTextField();
                        val.setBackground(extendAddr.getCommonRep().getBackground());
                        if (log.isDebugEnabled()) {
                            log.debug("set color: " + extendAddr.getCommonRep().getBackground());
                        }
                    }
                };
            }
        });
        val.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (log.isDebugEnabled()) {
                    log.debug("focusGained");
                }
                enterField();
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (log.isDebugEnabled()) {
                    log.debug("focusLost");
                }
                exitField();
            }
        });

    }

    String oldContents = "";

    /**
     * Handle focus entering the address field by recording the contents.
     */
    void enterField() {
        oldContents = val.getText();
    }

    /**
     * Handle focus leaving the address field by checking to see if the contents
     * changed. We do this because we want to record that change even if it
     * hasn't been "entered" via return key et al.
     */
    void exitField() {
        if (!oldContents.equals(val.getText())) {
            new DccAddressVarHandler(primaryAddr, extendAddr, addMode) {
                @Override
                protected void doPrimary() {
                    // short address mode
                    primaryAddr.updatedTextField();
                    val.setBackground(primaryAddr.getCommonRep().getBackground());
                    if (log.isDebugEnabled()) {
                        log.debug("set color: " + primaryAddr.getCommonRep().getBackground());
                    }
                }

                @Override
                protected void doExtended() {
                    // long address
                    extendAddr.updatedTextField();
                    val.setBackground(extendAddr.getCommonRep().getBackground());
                    if (log.isDebugEnabled()) {
                        log.debug("set color: " + extendAddr.getCommonRep().getBackground());
                    }
                }
            };
        }
    }

    /**
     * Handle a (possible) update to the active DCC address, either because the
     * state changed or the address mode changed. Note that value changes of the
     * active address are directly reflected, so we don't have to do anything on
     * those, but we still go ahead and update the state color.
     */
    void updateDccAddress() {
        if (log.isDebugEnabled()) {
            log.debug("updateDccAddress: short " + (primaryAddr == null ? "<null>" : primaryAddr.getValueString())
                    + " long " + (extendAddr == null ? "<null>" : extendAddr.getValueString())
                    + " mode " + (addMode == null ? "<null>" : addMode.getValueString()));
        }
        new DccAddressVarHandler(primaryAddr, extendAddr, addMode) {
            @Override
            protected void doPrimary() {
                // short address commonRep will be JTextField if variable, JLabel if constant
                JTextField f;
                if (primaryAddr.getCommonRep() instanceof JTextField) {
                    f = (JTextField) primaryAddr.getCommonRep();
                } else {
                    f = new JTextField();
                    f.setText(((JLabel) primaryAddr.getCommonRep()).getText());
                }
                val.setBackground(primaryAddr.getCommonRep().getBackground());
                val.setDocument(f.getDocument());
                if (log.isDebugEnabled()) {
                    log.debug("set color: " + primaryAddr.getCommonRep().getBackground());
                }
            }

            @Override
            protected void doExtended() {
                // long address commonRep will be JTextField if variable, JLabel if constant
                JTextField f;
                if (extendAddr.getCommonRep() instanceof JTextField) {
                    f = (JTextField) extendAddr.getCommonRep();
                } else {
                    f = new JTextField();
                    f.setText(((JLabel) extendAddr.getCommonRep()).getText());
                }
                val.setBackground(extendAddr.getCommonRep().getBackground());
                val.setDocument(f.getDocument());
                if (log.isDebugEnabled()) {
                    log.debug("set color: " + extendAddr.getCommonRep().getBackground());
                }
            }
        };
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(DccAddressPanel.class);

}
