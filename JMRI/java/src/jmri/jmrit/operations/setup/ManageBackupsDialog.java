package jmri.jmrit.operations.setup;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ManageBackupsDialog extends JDialog {

    private final JPanel contentPanel = new JPanel();
    private JLabel selectBackupSetsLabel;
    private JButton selectAllButton;
    private JButton clearAllButton;
    private JScrollPane scrollPane;
    private JList<BackupSet> setList;

    private JButton deleteButton;
    // private JButton helpButton;

    private DefaultListModel<BackupSet> model;

    private AutoBackup backup;
    private Component horizontalGlue;
    private Component horizontalStrut;
    private Component horizontalStrut_1;
    private Component horizontalStrut_2;

    /**
     * Create the dialog.
     */
    public ManageBackupsDialog() {
        // For now we only support Autobackups, but this can be updated later if
        // needed.
        backup = new AutoBackup();

        initComponents();
    }

    private void initComponents() {
        setModalityType(ModalityType.DOCUMENT_MODAL);
        setModal(true);
        setTitle(Bundle.getMessage("ManageBackupsDialog.manageBackupSets"));
        setBounds(100, 100, 461, 431);
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setVgap(5);
        borderLayout.setHgap(5);
        getContentPane().setLayout(borderLayout);
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[]{0, 0};
        gbl_contentPanel.rowHeights = new int[]{0, 0, 0};
        gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_contentPanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        contentPanel.setLayout(gbl_contentPanel);
        {
            selectBackupSetsLabel = new JLabel(Bundle.getMessage("ManageBackupsDialog.instructionsLabel.text"));
            GridBagConstraints gbc_selectBackupSetsLabel = new GridBagConstraints();
            gbc_selectBackupSetsLabel.anchor = GridBagConstraints.WEST;
            gbc_selectBackupSetsLabel.insets = new Insets(0, 0, 5, 0);
            gbc_selectBackupSetsLabel.gridx = 0;
            gbc_selectBackupSetsLabel.gridy = 0;
            contentPanel.add(selectBackupSetsLabel, gbc_selectBackupSetsLabel);
        }
        {
            scrollPane = new JScrollPane();
            GridBagConstraints gbc_scrollPane = new GridBagConstraints();
            gbc_scrollPane.fill = GridBagConstraints.BOTH;
            gbc_scrollPane.gridx = 0;
            gbc_scrollPane.gridy = 1;
            contentPanel.add(scrollPane, gbc_scrollPane);
            {
                setList = new JList<BackupSet>();
                setList.setVisibleRowCount(20);

                model = new DefaultListModel<BackupSet>();

                // Load up the list control with the available BackupSets
                for (BackupSet bs : backup.getBackupSets()) {
                    model.addElement(bs);
                }

                setList.setModel(model);

                // Update button states based on if anything is selected in the
                // list.
                setList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        updateButtonStates();
                    }
                });
                scrollPane.setViewportView(setList);
            }
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setBorder(new EmptyBorder(5, 5, 5, 5));
            buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                selectAllButton = new JButton(Bundle.getMessage("ManageBackupsDialog.selectAllButton.text"));
                selectAllButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        do_selectAllButton_actionPerformed(e);
                    }
                });
                buttonPane.add(selectAllButton);
            }
            {
                horizontalStrut = Box.createHorizontalStrut(10);
                buttonPane.add(horizontalStrut);
            }
            {
                clearAllButton = new JButton(Bundle.getMessage("ManageBackupsDialog.clearAllButton.text"));
                clearAllButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        do_clearAllButton_actionPerformed(e);
                    }
                });
                buttonPane.add(clearAllButton);
            }
            {
                horizontalGlue = Box.createHorizontalGlue();
                buttonPane.add(horizontalGlue);
            }
            {
                deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
                deleteButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        do_deleteButton_actionPerformed(e);
                    }
                });
                deleteButton.setActionCommand("");
                buttonPane.add(deleteButton);
            }
            {
                horizontalStrut_1 = Box.createHorizontalStrut(10);
                buttonPane.add(horizontalStrut_1);
            }
            {
                JButton closeButton = new JButton(Bundle.getMessage("ButtonCancel"));
                closeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        do_cancelButton_actionPerformed(e);
                    }
                });
                closeButton.setActionCommand("Cancel"); // NOI18N
                getRootPane().setDefaultButton(closeButton);
                buttonPane.add(closeButton);
            }
            {
                horizontalStrut_2 = Box.createHorizontalStrut(10);
                buttonPane.add(horizontalStrut_2);
            }
            // {
            // helpButton = new JButton(Bundle.getMessage("BackupDialog.helpButton.text"));
            // helpButton.addActionListener(new ActionListener() {
            // public void actionPerformed(ActionEvent e) {
            // do_helpButton_actionPerformed(e);
            // }
            // });
            // helpButton.setEnabled(false);
            // buttonPane.add(helpButton);
            // }
        }

        updateButtonStates();
    }

    protected void do_cancelButton_actionPerformed(ActionEvent e) {
        dispose();
    }

    protected void do_clearAllButton_actionPerformed(ActionEvent e) {
        setList.clearSelection();
    }

    protected void do_deleteButton_actionPerformed(ActionEvent e) {
        // Here we get the selected items from the list
        List<BackupSet> selectedSets = setList.getSelectedValuesList();

        if (selectedSets.size() > 0) {
            // Make sure OK to delete backups
            int result = JOptionPane.showConfirmDialog(this, String.format(Bundle.getMessage("ManageBackupsDialog.aboutToDelete"), selectedSets.size()), 
                    Bundle.getMessage("ManageBackupsDialog.deletingBackupSets"), JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                for (BackupSet set : selectedSets) {
                    model.removeElement(set);

                    // For now, the BackupSet deletes the associated files, but
                    // we might want to move this into the BackupBase class just
                    // so that it knows what is happening.
                    set.delete();
                }
            }
        }
    }

    protected void do_helpButton_actionPerformed(ActionEvent e) {
        // Not implemented yet.
    }

    protected void do_selectAllButton_actionPerformed(ActionEvent e) {
        setList.setSelectionInterval(0, model.getSize() - 1);
    }

    private void updateButtonStates() {
        // Update the various button enabled states based on what is in the list
        // and what is selected.
        boolean notEmpty = !setList.isSelectionEmpty();

        deleteButton.setEnabled(notEmpty);
        clearAllButton.setEnabled(notEmpty);

        // Can only select if we have something to select!
        int count = model.size();
        selectAllButton.setEnabled(count > 0);
    }
}
