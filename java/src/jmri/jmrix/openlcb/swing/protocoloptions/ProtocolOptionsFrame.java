package jmri.jmrix.openlcb.swing.protocoloptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import apps.gui3.tabbedpreferences.TabbedPreferences;
import jmri.InstanceManager;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.profile.ProfileManager;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import static jmri.jmrix.openlcb.OlcbConfigurationManager.*;


/**
 * JmriFrame that allows the user to edit the OpenLCB protocol options.
 *
 * @author Balazs Racz, (C) 2018.
 */

public class ProtocolOptionsFrame extends JmriJFrame {
    final CanSystemConnectionMemo scm;

    public ProtocolOptionsFrame(CanSystemConnectionMemo scm) {
        super();
        this.scm = scm;
    }

    private Map<String, JPanel> protocolPanels = new HashMap<>();
    private JTabbedPane protocolTabs;
    private List<Runnable> saveCallbacks = new ArrayList<>();
    boolean anyChanged = false;

    private JPanel getProtocolTab(String protocolKey) {
        JPanel p = protocolPanels.get(protocolKey);
        if (p != null) return p;

        p = new JPanel(new GridBagLayout());
        p.setName(Bundle.getMessage("TabTitle" + protocolKey));
        protocolTabs.add(p);
        protocolPanels.put(protocolKey, p);

        // We create a first row with empty panels in order to ensure that the relative placement
        // choices later make sense.
        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridx = 0;
        c1.gridy = 0;
        p.add(new JPanel(), c1);

        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 1;
        c2.gridy = 0;
        p.add(new JPanel(), c2);

        return p;
    }

    private void addTextSetting(final String protocolKey, final String optionKey) {
        String pastValue = scm.getProtocolOption(protocolKey, optionKey);
        if (pastValue == null) pastValue = "";
        JPanel tab = getProtocolTab(protocolKey);

        addOptionLabel(protocolKey, optionKey, tab);

        final JTextField valueField = new JTextField(pastValue, 20);
        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridy = GridBagConstraints.RELATIVE;
        c2.gridx = 1;
        c2.anchor = GridBagConstraints.FIRST_LINE_START;
        c2.insets = new Insets(0, 0, 0, 3);

        tab.add(valueField, c2);

        try {
            String tip = Bundle.getMessage("ToolTip" + protocolKey + optionKey);
            valueField.setToolTipText(tip);
        } catch (MissingResourceException e) {
            // Ignore: no tool tip if bundle does not have it.
        }

        saveCallbacks.add(() -> {
            String v = scm.getProtocolOption(protocolKey, optionKey);
            String newV = valueField.getText();
            if (newV.equals(v)) return;
            scm.setProtocolOption(protocolKey, optionKey, newV);
            anyChanged = true;
        });
        valueField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                log.warn("Entry changed: " + protocolKey + " " + optionKey + " = " + valueField.getText());
            }
        });
    }

    private void addOptionLabel(String protocolKey, String optionKey, JPanel tab) {
        String labelText = Bundle.getMessage("Label" + protocolKey + optionKey);

        JLabel label = new JLabel(labelText);
        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridy = GridBagConstraints.RELATIVE;
        c1.gridx = 0;
        c1.anchor = GridBagConstraints.FIRST_LINE_END;
        c1.insets = new Insets(0, 0, 0, 3);

        tab.add(label, c1);
    }

    private static class ComboSelectionEntry {
        final String displayKey;
        final String selectionKey;

        private ComboSelectionEntry(String displayKey, String selectionKey) {
            this.displayKey = displayKey;
            this.selectionKey = selectionKey;
        }

        @Override
        public String toString() {
            return displayKey;
        }
    }

    private void addComboBoxSetting(String protocolKey, String optionKey, String[] choices, String defaultChoice) {
        JPanel tab = getProtocolTab(protocolKey);
        String pastValue = scm.getProtocolOption(protocolKey, optionKey);


        addOptionLabel(protocolKey, optionKey, tab);

        final JComboBox<ComboSelectionEntry> valueField = new JComboBox<>();
        int defaultNum = -1;
        int pastNum = -1;
        for (int i = 0; i < choices.length; ++i) {
            if (choices[i].equals(pastValue)) {
                pastNum = i;
            }
            if (choices[i].equals(defaultChoice)) {
                defaultNum = i;
            }
            String displayKey;
            try {
                displayKey = Bundle.getMessage("Selection" + protocolKey + optionKey + choices[i]);

            } catch (MissingResourceException e) {
                displayKey = choices[i];
            }
            valueField.addItem(new ComboSelectionEntry(displayKey, choices[i]));
        }
        if (pastNum >= 0) {
            valueField.setSelectedIndex(pastNum);
        } else if (pastValue == null) {
            valueField.setSelectedIndex(defaultNum);
        }

        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridy = GridBagConstraints.RELATIVE;
        c2.gridx = 1;
        c2.anchor = GridBagConstraints.FIRST_LINE_START;
        c2.insets = new Insets(0, 0, 0, 3);

        tab.add(valueField, c2);

        try {
            String tip = Bundle.getMessage("ToolTip" + protocolKey + optionKey);
            valueField.setToolTipText(tip);
        } catch (MissingResourceException e) {
            // Ignore: no tool tip if bundle does not have it.
        }

        saveCallbacks.add(() -> {
            String v = scm.getProtocolOption(protocolKey, optionKey);
            ComboSelectionEntry newO = (ComboSelectionEntry) valueField.getSelectedItem();
            if (newO == null) return;
            String newV = newO.selectionKey;
            if (newV.equals(v)) return;
            scm.setProtocolOption(protocolKey, optionKey, newV);
            anyChanged = true;
        });
    }

    @Override
    public void initComponents() {
        setTitle(Bundle.getMessage("WindowTitle", scm.getUserName()));

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        protocolTabs = new JTabbedPane();
        contentPane.add(protocolTabs);

        getProtocolTab(OPT_PROTOCOL_IDENT);
        addTextSetting(OPT_PROTOCOL_IDENT, OPT_IDENT_NODEID);
        addTextSetting(OPT_PROTOCOL_IDENT, OPT_IDENT_USERNAME);
        addTextSetting(OPT_PROTOCOL_IDENT, OPT_IDENT_DESCRIPTION);

        getProtocolTab(OPT_PROTOCOL_FASTCLOCK);
        addComboBoxSetting(OPT_PROTOCOL_FASTCLOCK, OPT_FASTCLOCK_ENABLE, new String[] {OPT_FASTCLOCK_ENABLE_OFF, OPT_FASTCLOCK_ENABLE_GENERATOR, OPT_FASTCLOCK_ENABLE_CONSUMER}, OPT_FASTCLOCK_ENABLE_OFF);
        addComboBoxSetting(OPT_PROTOCOL_FASTCLOCK, OPT_FASTCLOCK_ID, new String[]{OPT_FASTCLOCK_ID_DEFAULT, OPT_FASTCLOCK_ID_DEFAULT_RT, OPT_FASTCLOCK_ID_ALT_1, OPT_FASTCLOCK_ID_ALT_2, OPT_FASTCLOCK_ID_CUSTOM}, OPT_FASTCLOCK_ID_DEFAULT);
        addTextSetting(OPT_PROTOCOL_FASTCLOCK, OPT_FASTCLOCK_CUSTOM_ID);

        JPanel helpHintPanel = new JPanel();
        BoxLayout helpLayout = new BoxLayout(helpHintPanel, BoxLayout.X_AXIS);
        helpHintPanel.setLayout(helpLayout);
        contentPane.add(helpHintPanel);
        JLabel hintOnHelpLabel = new JLabel(Bundle.getMessage("HintOnHelp"));
        helpHintPanel.add(hintOnHelpLabel);

        JPanel bottomPanel = new JPanel();
        BoxLayout bottomLayout = new BoxLayout(bottomPanel, BoxLayout.X_AXIS);
        bottomPanel.setLayout(bottomLayout);
        contentPane.add(bottomPanel);

        JButton saveButton = new JButton(
                Bundle.getMessage("ButtonSave"),
                new ImageIcon(FileUtil.findURL("program:resources/icons/misc/gui3/SaveIcon.png",
                        FileUtil.Location.INSTALLED)));
        bottomPanel.add(saveButton);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                saveButtonClicked();
            }
        });

        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        bottomPanel.add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ProtocolOptionsFrame.this.dispatchEvent(new WindowEvent(ProtocolOptionsFrame
                        .this, WindowEvent.WINDOW_CLOSING));
            }
        });

        pack();
    }

    private void saveButtonClicked() {
        for (Runnable r : saveCallbacks) {
            r.run();
        }
        if (anyChanged) {
            // Save current profile's connection config xml.
            InstanceManager.getDefault(ConnectionConfigManager.class).savePreferences(
                    ProfileManager.getDefault().getActiveProfile());
            // This will pop up a restart message for the user.
            InstanceManager.getDefault(TabbedPreferences.class).savePressed(true);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ProtocolOptionsFrame.class);
}
