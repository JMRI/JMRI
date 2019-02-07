package jmri.jmrit.operations.setup;

import apps.Apps;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.util.swing.ExceptionContext;
import jmri.util.swing.ExceptionDisplayFrame;
import jmri.util.swing.UnexpectedExceptionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestoreDialog extends JDialog {

    private final static Logger log = LoggerFactory.getLogger(RestoreDialog.class
            .getName());

    private JPanel mainPanel;
    private JPanel contentPanel;
    private JRadioButton automaticBackupsRadioButton;
    private JRadioButton defaultBackupsRadioButton;
    private JComboBox<BackupSet> comboBox;
    private JButton restoreButton;
    //private JButton helpButton;

    private BackupBase backup;
    private String setName;
    private JLabel newLabelLabel;

    /**
     * Create the dialog.
     */
    public RestoreDialog() {
        initComponents();
    }

    private void initComponents() {
        setModalityType(ModalityType.DOCUMENT_MODAL);
        setModal(true);
        setTitle(Bundle.getMessage("RestoreDialog.this.title"));
        setBounds(100, 100, 378, 251);
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(null);
        setContentPane(mainPanel);
        {

            contentPanel = new JPanel();
            contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            mainPanel.add(contentPanel, BorderLayout.NORTH);

            {
                JPanel panel = new JPanel();
                panel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.setLayout(new FlowLayout(FlowLayout.LEFT));
                panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), // NOI18N
                Bundle.getMessage("RestoreDialog.label.text"), TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))); // NOI18N

                contentPanel.add(panel);
                ButtonGroup fromGroup = new ButtonGroup();

                {
                    automaticBackupsRadioButton = new JRadioButton(Bundle.getMessage("RestoreDialog.radioButton.autoBackup"));
                    automaticBackupsRadioButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            do_automaticBackupsRadioButton_actionPerformed(e);
                        }
                    });
                    panel.add(automaticBackupsRadioButton);
                    fromGroup.add(automaticBackupsRadioButton);
                }
                {
                    defaultBackupsRadioButton = new JRadioButton(Bundle.getMessage("RestoreDialog.radioButton.defaultBackup"));
                    defaultBackupsRadioButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            do_defaultBackupsRadioButton_actionPerformed(e);
                        }
                    });
                    panel.add(defaultBackupsRadioButton);
                    fromGroup.add(defaultBackupsRadioButton);
                }
            }

            {
                newLabelLabel = new JLabel(Bundle.getMessage("RestoreDialog.label2.text"));
                newLabelLabel.setBorder(new EmptyBorder(10, 0, 5, 0));
                contentPanel.add(newLabelLabel);
            }
            {
                comboBox = new JComboBox<>();
                comboBox.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent arg0) {
                        do_comboBox_itemStateChanged(arg0);
                    }
                });
                comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentPanel.add(comboBox);
            }
        }

        {
            JPanel buttonPane = new JPanel();
            FlowLayout fl_buttonPane = new FlowLayout(FlowLayout.CENTER);
            buttonPane.setLayout(fl_buttonPane);
            mainPanel.add(buttonPane, BorderLayout.SOUTH);
            {
                restoreButton = new JButton(Bundle.getMessage("RestoreDialog.retoreButton.text"));
                restoreButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        do_restoreButton_actionPerformed(e);
                    }
                });
                buttonPane.add(restoreButton);
            }
            {
                JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        do_cancelButton_actionPerformed(arg0);
                    }
                });
                buttonPane.add(cancelButton);
            }
        }

        // Start out with Default backups
        defaultBackupsRadioButton.doClick();
    }

    // Event handlers
    protected void do_automaticBackupsRadioButton_actionPerformed(ActionEvent e) {
        backup = new AutoBackup();
        loadComboBox();
    }

    protected void do_defaultBackupsRadioButton_actionPerformed(ActionEvent e) {
        backup = new DefaultBackup();
        loadComboBox();
    }

    protected void do_cancelButton_actionPerformed(ActionEvent arg0) {
        dispose();
    }

    protected void do_comboBox_itemStateChanged(ItemEvent arg0) {
        // If something has been selected, enable the Restore
        // button.
        // If we only have a deselect, then the button will remain disabled.
        // When changing between items in the list, the item being left is
        // deselected first, then the new item is selected.
        //
        // Not sure if there can be more than two states, so using the if
        // statement to be safe.
        int stateChange = arg0.getStateChange();

        if (stateChange == ItemEvent.SELECTED) {
            restoreButton.setEnabled(true);
        } else if (stateChange == ItemEvent.DESELECTED) {
            restoreButton.setEnabled(false);
        }

    }

    protected void do_helpButton_actionPerformed(ActionEvent arg0) {
        // Not implemented yet.
    }

    protected void do_restoreButton_actionPerformed(ActionEvent e) {
        log.debug("restore button activated");

        // check to see if files are dirty
        if (OperationsXml.areFilesDirty()) {
            if (JOptionPane.showConfirmDialog(this, Bundle.getMessage("OperationsFilesModified"),
                    Bundle.getMessage("SaveOperationFiles"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                OperationsXml.save();
            }
        }

        // first backup the users data in case they forgot
        try {
            AutoBackup auto = new AutoBackup();
            auto.autoBackup();

            // now delete the current operations files in case the restore isn't a full set of files
            backup.deleteOperationsFiles();

            setName = ((BackupSet) comboBox.getSelectedItem()).getSetName();

            // The restore method should probably be overloaded to accept a
            // BackupSet instead of a simple string. Later.
            backup.restoreFilesFromSetName(setName);

            // now deregister shut down task
            // If Trains window was opened, then task is active
            // otherwise it is normal to not have the task running
            InstanceManager.getDefault(OperationsManager.class).setShutDownTask(null);

            JOptionPane.showMessageDialog(this, Bundle.getMessage("YouMustRestartAfterRestore"),
                    Bundle.getMessage("RestoreSuccessful"), JOptionPane.INFORMATION_MESSAGE);
            dispose();

            Apps.handleRestart();
        } // These may need to be enhanced to show the backup store being used,
          // auto or default.
        catch (IOException ex) {
            ExceptionContext context = new ExceptionContext(ex, Bundle.getMessage("RestoreDialog.restoring")
                    + " " + setName, "Hint about checking valid names, etc."); // NOI18N
            new ExceptionDisplayFrame(context, this).setVisible(true);

        } catch (Exception ex) {
            log.error("Doing restore from " + setName, ex);

            UnexpectedExceptionContext context = new UnexpectedExceptionContext(ex,
                    Bundle.getMessage("RestoreDialog.restoring") + " " + setName);

            new ExceptionDisplayFrame(context, this).setVisible(true);
        }
    }

    /**
     * Adds the names of the backup sets that are available in either the
     * Automatic or the Default backup store.
     */
    private void loadComboBox() {
        // Get the Backup Sets from the currently selected backup store.
        // Called after the radio button selection has changed

        // Disable the Restore button in case there is nothing loaded into the ComboBox
        restoreButton.setEnabled(false);

        comboBox.removeAllItems();

        BackupSet[] sets = backup.getBackupSets();
        ComboBoxModel<BackupSet> model = new DefaultComboBoxModel<>(sets);

        // Clear any current selection so that the state change will fire when
        // we set a selection.
        model.setSelectedItem(null);

        comboBox.setModel(model);

        // Position at the last item, which is usually the most recent
        // This is ugly code, but it works....
        if (model.getSize() > 0) {
            comboBox.setSelectedIndex(model.getSize() - 1);
        }
    }

}
