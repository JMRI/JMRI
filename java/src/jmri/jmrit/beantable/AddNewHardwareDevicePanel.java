package jmri.jmrit.beantable;

import com.alexandriasoftware.swing.Validation;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import jmri.Manager;
import jmri.swing.ManagerComboBox;
import jmri.swing.SystemNameValidator;

/**
 * JPanel to create a new hardware-backed NamedBean.
 *
 * @author Bob Jacobsen Copyright (C) 2009
 * @author Pete Cressman Copyright (C) 2010
 */
public class AddNewHardwareDevicePanel extends jmri.util.swing.JmriPanel {

    /**
     * Create the panel.
     *
     * @param sysAddress text field for the system name or address
     * @param sysAddressValidator validation control for sysAddress
     * @param userName text field for the optional user name
     * @param prefixBox selector for the connection for the NamedBean
     * @param endRange selector for range to create multiple NamedBeans
     * @param addRange check box to allow creation of multiple NamedBeans
     * @param addButton button to trigger creation of NamedBean
     * @param cancelListener listener to attach to the cancel button
     * @param rangeListener listener that controls if addRange is enabled
     * @param statusBar area where status messages can be presented
     */
    public AddNewHardwareDevicePanel(@Nonnull JTextField sysAddress,
            @Nonnull SystemNameValidator sysAddressValidator,
            @Nonnull JTextField userName,
            @Nonnull ManagerComboBox<?> prefixBox,
            JSpinner endRange,
            JCheckBox addRange,
            @Nonnull JButton addButton,
            @Nonnull ActionListener cancelListener,
            @Nonnull ActionListener rangeListener,
            @Nonnull JLabel statusBar) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.statusBar = statusBar;
        _endRange = endRange;
        _range = addRange;
        // directly using the addButton from the table action allows to disable it from there
        // as long until a valid address is entered
        JPanel p;
        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        p.add(sysNameLabel, c);
        c.gridx = 0;
        c.gridy = 1;
        p.add(sysAddressLabel, c);
        sysAddressLabel.setLabelFor(sysAddress);
        c.gridy = 2;
        p.add(userNameLabel, c);
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL; // text field will expand
        c.gridy = 0;
        p.add(prefixBox, c);
        c.gridx = 3;
        p.add(addRange, c);
        c.gridx = 2;
        c.gridy = 1;
        p.add(sysAddress, c);
        sysAddress.setToolTipText(Bundle.getMessage("HardwareAddressToolTip")); // overridden in calling class
        c.gridx = 3;
        p.add(finishLabel, c);
        c.gridx = 4;
        p.add(endRange, c);
        c.gridx = 2;
        c.gridy = 2;
        p.add(userName, c);
        userName.setToolTipText(Bundle.getMessage("UserNameToolTip")); // fixed general instruction
        add(p);

        finishLabel.setEnabled(false);
        _endRange.setEnabled(false);

        // add status bar above buttons
        JPanel panelStatus = new JPanel();
        panelStatus.setLayout(new FlowLayout());
        statusBar.setFont(statusBar.getFont().deriveFont(0.9f * sysAddressLabel.getFont().getSize())); // a bit smaller
        statusBar.setForeground(Color.gray);
        panelStatus.add(statusBar);
        add(panelStatus);

        // cancel + add buttons at bottom of window
        JPanel panelBottom = new JPanel();
        panelBottom.setLayout(new FlowLayout(FlowLayout.TRAILING));

        panelBottom.add(cancel);
        cancel.addActionListener(cancelListener);

        panelBottom.add(addButton);

        add(panelBottom);

        addRange.addItemListener((ItemEvent e) -> {
            rangeState();
        });
        prefixBox.addActionListener(rangeListener);
        sysAddress.setInputVerifier(sysAddressValidator);
        if (prefixBox.getSelectedItem() == null) {
            prefixBox.setSelectedIndex(0);
        }
        prefixBox.addActionListener((evt) -> {
            Manager<?> manager = prefixBox.getSelectedItem();
            if (manager != null) {
                sysAddress.setText("");     // Reset saved text before switching managers
                sysAddressValidator.setManager(manager);
            }
        });
        sysAddressValidator.addPropertyChangeListener("validation", (evt) -> { // NOI18N
            Validation validation = sysAddressValidator.getValidation();
            Validation.Type type = validation.getType();
            addButton.setEnabled(type != Validation.Type.WARNING && type != Validation.Type.DANGER);
            setStatusBarText(validation.getMessage());
        });
        sysAddressValidator.setManager(prefixBox.getSelectedItem());
        sysAddressValidator.verify(sysAddress);
    }

    public void addLabels(String labelSystemName, String labelUserName) {
        sysAddressLabel.setText(labelSystemName);
        userNameLabel.setText(labelUserName);
    }

    private void rangeState() {
        if (_range.isSelected()) {
            finishLabel.setEnabled(true);
            _endRange.setEnabled(true);
        } else {
            finishLabel.setEnabled(false);
            _endRange.setEnabled(false);
        }
    }

    /**
     * Set the status bar text; if the input is multi-line HTML, get just the
     * first line, assuming lines are separated with {@code <br>}.
     *
     * @param message the message to set
     */
    public void setStatusBarText(@CheckForNull String message) {
        if (message == null) {
            statusBar.setText("");
        } else {
            message = message.trim();
            if (message.startsWith("<html>") && message.contains("<br>")) {
                message = message.substring(0, message.indexOf("<br>"));
                if (!message.endsWith("</html>")) {
                    message = message + "</html>";
                }
            }
            statusBar.setText(message);
        }
    }

    JButton cancel = new JButton(Bundle.getMessage("ButtonClose")); // when Apply has been clicked at least once, this is not Revert/Cancel
    JSpinner _endRange;
    JCheckBox _range;
    JLabel sysNameLabel = new JLabel(Bundle.getMessage("SystemConnectionLabel"));
    JLabel sysAddressLabel = new JLabel(Bundle.getMessage("LabelHardwareAddress"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    JLabel finishLabel = new JLabel(Bundle.getMessage("LabelNumberToAdd"));
    JLabel statusBar;

}
