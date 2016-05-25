// ProgOpsModePane.java
package jmri.jmrit.progsupport;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import jmri.AddressedProgrammer;
import jmri.AddressedProgrammerManager;
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.ProgrammingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a JPanel to configure the ops programming mode.
 * <p>
 * Note that you should call the dispose() method when you're really done, so
 * that a ProgModePane object can disconnect its listeners.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public class ProgOpsModePane extends ProgModeSelector implements PropertyChangeListener, ActionListener {

    // GUI member declarations
    /**
     *
     */
    private static final long serialVersionUID = 165989491869394147L;
    ButtonGroup modeGroup = new ButtonGroup();
    HashMap<ProgrammingMode, JRadioButton> buttonMap = new HashMap<ProgrammingMode, JRadioButton>();
    JComboBox<AddressedProgrammerManager> progBox;
    ArrayList<JRadioButton> buttonPool = new ArrayList<JRadioButton>();

    JTextField mAddrField = new JTextField(4);
    String oldAddrText = "";

    JCheckBox mLongAddrCheck = new JCheckBox(Bundle.getMessage("LongAddress"));
    boolean oldLongAddr = false;
    AddressedProgrammer programmer = null;

    /**
     * Get the selected programmer
     */
    public Programmer getProgrammer() {
        if ((mLongAddrCheck.isSelected() == oldLongAddr) && mAddrField.getText().equals(oldAddrText)) {
            // hasn't changed
            return programmer;
        }

        // here values have changed, try to create a new one
        AddressedProgrammerManager pm = ((AddressedProgrammerManager) progBox.getSelectedItem());
        oldLongAddr = mLongAddrCheck.isSelected();
        oldAddrText = mAddrField.getText();

        if (pm != null) {
            int address = 3;
            try {
                address = Integer.parseInt(mAddrField.getText());
            } catch (java.lang.NumberFormatException e) {
                log.error("loco address \"{}\" not correct", mAddrField.getText());
                programmer = null;
            }
            boolean longAddr = mLongAddrCheck.isSelected();
            log.debug("ops programmer for address " + address
                    + ", long address " + longAddr);
            programmer = pm.getAddressedProgrammer(longAddr, address);
            log.debug("   programmer: {}", programmer);

            // whole point is to get mode...
            setProgrammerFromGui(programmer);
        } else {
            log.warn("request for ops mode programmer with no ProgrammerManager configured");
            programmer = null;
        }
        return programmer;
    }

    /**
     * Are any of the modes selected?
     *
     * @return true is any button is selected
     */
    public boolean isSelected() {
        for (JRadioButton button : buttonMap.values()) {
            if (button.isSelected()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param direction controls layout, either BoxLayout.X_AXIS or
     *                  BoxLayout.Y_AXIS
     */
    public ProgOpsModePane(int direction) {
        this(direction, new javax.swing.ButtonGroup());
    }

    /**
     * @param direction controls layout, either BoxLayout.X_AXIS or
     *                  BoxLayout.Y_AXIS
     */
    public ProgOpsModePane(int direction, javax.swing.ButtonGroup group) {
        modeGroup = group;

        // general GUI config
        setLayout(new BoxLayout(this, direction));

        // create the programmer display combo box
        java.util.Vector<AddressedProgrammerManager> v = new java.util.Vector<>();
        for (AddressedProgrammerManager pm : InstanceManager.getList(jmri.AddressedProgrammerManager.class)) {
            v.add(pm);
        }
        add(progBox = new JComboBox<>(v));
        // if only one, don't show
        if (progBox.getItemCount() < 2) {
            progBox.setVisible(false);
        }
        progBox.setSelectedItem(InstanceManager.getDefault(jmri.AddressedProgrammerManager.class)); // set default
        progBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // new programmer selection
                programmerSelected();
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.FlowLayout());
        panel.add(new JLabel(Bundle.getMessage("AddressLabel")));
        panel.add(mAddrField);
        add(panel);
        add(mLongAddrCheck);

        mAddrField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // new programmer selection
                programmerSelected(); // in case has valid address now
            }
        });
        mLongAddrCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // new programmer selection
                programmerSelected(); // in case has valid address now
            }
        });

        // and execute the setup for 1st time
        programmerSelected();
    }

    /**
     * reload the interface with the new programmers
     */
    void programmerSelected() {
        log.debug("programmerSelected starts with {} buttons", buttonPool.size());
        // hide buttons
        for (JRadioButton button : buttonPool) {
            button.setVisible(false);
        }

        // clear map
        buttonMap.clear();

        // require new programmer if possible
        oldAddrText = "";

        // configure buttons
        int index = 0;
        List<ProgrammingMode> modes;
        if (getProgrammer() != null) {
            modes = getProgrammer().getSupportedModes();
        } else {
            modes = ((AddressedProgrammerManager) progBox.getSelectedItem()).getDefaultModes();
        }
        log.debug("   has {} modes", modes.size());
        for (ProgrammingMode mode : modes) {
            JRadioButton button;
            // need a new button?
            if (index >= buttonPool.size()) {
                log.debug("   add button");
                button = new JRadioButton();
                buttonPool.add(button);
                modeGroup.add(button);
                button.addActionListener(this);
                add(button); // add to GUI
            }
            // configure next button in pool
            log.debug("   set for {}", mode.toString());
            button = buttonPool.get(index++);
            button.setVisible(true);
            modeGroup.add(button);
            button.setText(mode.toString());
            buttonMap.put(mode, button);
        }

        setGuiFromProgrammer();
    }

    /**
     * Listen to buttons for mode changes
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {
        // find selected button
        log.debug("Selected button: {}", e.getActionCommand());
        for (ProgrammingMode mode : buttonMap.keySet()) {
            if (mode.toString().equals(e.getActionCommand())) {
                log.debug("      set mode {} on {}", mode.toString(), getProgrammer());
                if (getProgrammer() != null) {
                    getProgrammer().setMode(mode);
                }
                return; // 1st match
            }
        }
    }

    void setProgrammerFromGui(Programmer programmer) {
        for (ProgrammingMode mode : buttonMap.keySet()) {
            if (buttonMap.get(mode).isSelected()) {
                programmer.setMode(mode);
            }
        }
    }

    /**
     * Listen to programmer for mode changes
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if ("Mode".equals(e.getPropertyName()) && getProgrammer().equals(e.getSource())) {
            // mode changed in programmer, change GUI here if needed
            if (isSelected()) {  // only change mode if we have a selected mode, in case some other selector with shared group has the selection
                setGuiFromProgrammer();
            }
        }
    }

    void setGuiFromProgrammer() {
        if (getProgrammer() == null) {
            // no mode selected
            for (JRadioButton button : buttonPool) {
                button.setSelected(false);
            }
            return;
        }

        ProgrammingMode mode = getProgrammer().getMode();
        JRadioButton button = buttonMap.get(mode);
        if (button == null) {
            log.error("setGuiFromProgrammer found mode \"{}\" that's not supported by the programmer", mode);
            return;
        }
        log.debug("  setting button for mode {}", mode);
        button.setSelected(true);
    }

    // no longer needed, disconnect if still connected
    public void dispose() {
    }

    private final static Logger log = LoggerFactory.getLogger(ProgOpsModePane.class.getName());

}
