package jmri.jmrit.display;

import jmri.jmrit.logixng.LogixNG;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

public class LogixNGDeleteDialog extends JDialog {

    private JList<CheckableItem> itemList;
    private DefaultListModel<CheckableItem> listModel;
    private JCheckBox selectAllCheckBox;
    private JCheckBox disableCheckBox;
    private JButton okButton;
    private JButton cancelButton;
    private List<CheckableItem> selectedItems;
    private boolean isSelectAllChecked = false; // Track the state of the "Select All" checkbox

    public LogixNGDeleteDialog(Frame owner, List<LogixNG> items) {
        super(owner, "Disposition Inline Logix NGs", true); // true for modal dialog
        initComponents(items);
        layoutComponents();
        addEventHandlers();
        selectedItems = new ArrayList<>();
    }

    private void initComponents(List<LogixNG> items) {
        listModel = new DefaultListModel<>();
        for (LogixNG item : items) {
            listModel.addElement(new CheckableItem(item));
        }
        itemList = new JList<>(listModel);
        itemList.setCellRenderer(new CheckboxListCellRenderer()); // Use custom renderer
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Or MULTIPLE_INTERVAL_SELECTION as needed

        selectAllCheckBox = new JCheckBox("<html>Selected Logix NGs will be deleted<br/>Others converted to normal Logix NGs</html>");
        selectAllCheckBox.setFocusable(false); // Remove focus rectangle

        disableCheckBox = new JCheckBox("Disable converted Logix NGs");
        disableCheckBox.setFocusable(false); // Remove focus rectangle

        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        getRootPane().setDefaultButton(okButton); // Make Enter key activate OK button
    }

    private void layoutComponents() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());

        // Header panel for the "Select All" checkbox
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        headerPanel.add(selectAllCheckBox);
        contentPanel.add(headerPanel, BorderLayout.NORTH);

        //scrollpane for the list
        JScrollPane scrollPane = new JScrollPane(itemList);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel for OK and Cancel buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(disableCheckBox, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        contentPanel.add(southPanel, BorderLayout.SOUTH);

        setContentPane(contentPanel);
        pack();
        setLocationRelativeTo(getOwner()); // Center relative to owner
    }

    private void addEventHandlers() {
        selectAllCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                isSelectAllChecked = e.getStateChange() == ItemEvent.SELECTED; // update the flag
                for (int i = 0; i < listModel.getSize(); i++) {
                    listModel.getElementAt(i).setSelected(isSelectAllChecked);
                }
                itemList.repaint(); // Refresh the list to show changes
            }
        });

        itemList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) { // Only handle the final event
                    int selectedIndex = itemList.getSelectedIndex();
                    if (selectedIndex != -1) {
                        CheckableItem selectedItem = listModel.getElementAt(selectedIndex);
                        selectedItem.setSelected(!selectedItem.isSelected()); // Toggle
                        listModel.setElementAt(selectedItem, selectedIndex); // Update the model
                        itemList.repaint(); // Ensure the checkbox is updated

                        // Update "Select All" checkbox
                        updateSelectAllCheckbox();
                    }
                }
            }
        });

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedItems.clear(); // Clear previous selection
                for (int i = 0; i < listModel.getSize(); i++) {
                    CheckableItem item = listModel.getElementAt(i);
                    if (item.isSelected()) {
                        selectedItems.add(item);
                    }
                }
                setVisible(false); // Close the dialog
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedItems.clear();
                setVisible(false); // Close the dialog
            }
        });
    }
    /**
     * Updates the "Select All" checkbox based on the current state of the items in the list.
     */
    private void updateSelectAllCheckbox() {
        boolean allSelected = true;
        for (int i = 0; i < listModel.getSize(); i++) {
            if (!listModel.getElementAt(i).isSelected()) {
                allSelected = false;
                break;
            }
        }
        selectAllCheckBox.setSelected(allSelected);
        isSelectAllChecked = allSelected;
    }

    public List<LogixNG> getSelectedItems() {
        ArrayList<LogixNG> selections = new ArrayList<>();
        for (CheckableItem item : selectedItems) {
            selections.add(item.getLogixNG());
        }
        return selections;
    }

    public boolean isDisableLogixNG() {
        return disableCheckBox.isSelected();
    }

    // Inner class for items in the list
    public static class CheckableItem {
        private LogixNG logixNG;
        private boolean isSelected;

        public CheckableItem(LogixNG logixNG) {
            this.logixNG = logixNG;
            this.isSelected = false;
        }

        public LogixNG getLogixNG() {
            return logixNG;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        @Override
        public String toString() {
            return logixNG.getDisplayName();
        }
    }

    // Inner class for custom cell renderer
    private static class CheckboxListCellRenderer extends JPanel implements ListCellRenderer<CheckableItem> {
        private JCheckBox checkBox;

        public CheckboxListCellRenderer() {
            setLayout(new BorderLayout());
            checkBox = new JCheckBox();
            checkBox.setMargin(new Insets(0, 0, 0, 0));
            add(checkBox, BorderLayout.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends CheckableItem> list, CheckableItem value, int index, boolean isSelected, boolean cellHasFocus) {
            checkBox.setText(value.getLogixNG().getDisplayName());
            checkBox.setSelected(value.isSelected());
            checkBox.setEnabled(list.isEnabled());
            checkBox.setFont(list.getFont());

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            return this;
        }
    }
}

