package jmri.jmrit.operations.trains.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame to display which trains service certain car types
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
public class TrainsByCarTypeFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    TrainManager manager;
    static final String EMPTY = "            ";

    ArrayList<JCheckBox> trainList = new ArrayList<>();
    JPanel trainCheckBoxes = new JPanel();

    // panels
    JPanel pTrains;

    // major buttons
    JButton clearButton = new JButton(Bundle.getMessage("ClearAll"));
    JButton setButton = new JButton(Bundle.getMessage("SelectAll"));
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    // check boxes
    JCheckBox copyCheckBox = new JCheckBox(Bundle.getMessage("ButtonCopy"));

    // radio buttons
    // text field
    JLabel textCarType = new JLabel(EMPTY);

    // for padding out panel
    // combo boxes
    JComboBox<String> typeComboBox = InstanceManager.getDefault(CarTypes.class).getComboBox();

    public TrainsByCarTypeFrame() {
        super();
    }

    public void initComponents(String carType) {

        // load managers
        manager = InstanceManager.getDefault(TrainManager.class);

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the panels
        JPanel pCarType = new JPanel();
        pCarType.setLayout(new GridBagLayout());
        pCarType.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Type")));

        addItem(pCarType, typeComboBox, 0, 0);
        addItem(pCarType, copyCheckBox, 1, 0);
        addItem(pCarType, textCarType, 2, 0);
        typeComboBox.setSelectedItem(carType);
        copyCheckBox.setToolTipText(Bundle.getMessage("TipCopyCarType"));

        pTrains = new JPanel();
        pTrains.setLayout(new GridBagLayout());
        JScrollPane trainPane = new JScrollPane(pTrains);
        trainPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        trainPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Trains")));
        updateTrains();

        JPanel pButtons = new JPanel();
        pButtons.setLayout(new GridBagLayout());
        pButtons.setBorder(BorderFactory.createEtchedBorder());

        addItem(pButtons, clearButton, 0, 0);
        addItem(pButtons, setButton, 1, 0);
        addItem(pButtons, saveButton, 2, 0);

        getContentPane().add(pCarType);
        getContentPane().add(trainPane);
        getContentPane().add(pButtons);

        // setup combo box
        addComboBoxAction(typeComboBox);

        // setup buttons
        addButtonAction(setButton);
        addButtonAction(clearButton);
        addButtonAction(saveButton);

        // setup checkbox
        addCheckBoxAction(copyCheckBox);

        manager.addPropertyChangeListener(this);
        InstanceManager.getDefault(CarTypes.class).addPropertyChangeListener(this);

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(
                new PrintTrainsByCarTypesAction(Bundle.getMessage("MenuItemPrintByType"), false));
        toolMenu.add(new PrintTrainsByCarTypesAction(Bundle.getMessage("MenuItemPreviewByType"), true));
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_ModifyTrainsByCarType", true); // NOI18N

        setPreferredSize(null);
        pack();
        setMinimumSize(new Dimension(Control.panelWidth300, Control.panelHeight250));
        setTitle(Bundle.getMessage("TitleModifyTrains"));
        setVisible(true);
    }

    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("combo box action");
        updateTrains();
    }

    // Save, Delete, Add
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            save();
        }
        if (ae.getSource() == setButton) {
            selectCheckboxes(true);
        }
        if (ae.getSource() == clearButton) {
            selectCheckboxes(false);
        }
    }

    /**
     * Update the car types that trains and tracks service. Note that the
     * checkbox name is the id of the train or track.
     */
    private void save() {
        if (copyCheckBox.isSelected() &&
                JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("CopyCarType"),
                        new Object[]{typeComboBox.getSelectedItem(), textCarType.getText()}),
                        Bundle.getMessage("CopyCarTypeTitle"),
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        log.debug("Save {} trains", trainList.size());
        removePropertyChangeTrains();
        for (int i = 0; i < trainList.size(); i++) {
            JCheckBox cb = trainList.get(i);
            Train train = manager.getTrainById(cb.getName());
            if (cb.isSelected()) {
                train.addTypeName((String) typeComboBox.getSelectedItem());
            } else {
                train.deleteTypeName((String) typeComboBox.getSelectedItem());
            }
        }
        OperationsXml.save(); // save files
        updateTrains();
        if (Setup.isCloseWindowOnSaveEnabled()) {
            dispose();
        }
    }

    private void updateTrains() {
        log.debug("update trains");
        removePropertyChangeTrains();
        trainList.clear();
        int x = 0;
        pTrains.removeAll();
        String carType = (String) typeComboBox.getSelectedItem();
        if (copyCheckBox.isSelected()) {
            carType = textCarType.getText();
        }
        List<Train> trains = manager.getTrainsByNameList();
        for (Train train : trains) {
            train.addPropertyChangeListener(this);
            JCheckBox cb = new JCheckBox(train.getName());
            cb.setName(train.getId());
            cb.setToolTipText(MessageFormat.format(Bundle.getMessage("TipTrainCarType"), new Object[]{carType}));
            addCheckBoxAction(cb);
            trainList.add(cb);
            boolean trainAcceptsType = train.acceptsTypeName(carType);
            cb.setSelected(trainAcceptsType);
            addItemLeft(pTrains, cb, 0, x);
            JLabel description = new JLabel(train.getDescription());
            addItemLeft(pTrains, description, 1, x++);
        }
        pTrains.revalidate();
        repaint();
    }

    private void updateComboBox() {
        log.debug("update combobox");
        InstanceManager.getDefault(CarTypes.class).updateComboBox(typeComboBox);
    }

    private void selectCheckboxes(boolean b) {
        for (int i = 0; i < trainList.size(); i++) {
            trainList.get(i).setSelected(b);
        }
    }

    @Override
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        // copy checkbox?
        if (ae.getSource() == copyCheckBox) {
            if (copyCheckBox.isSelected()) {
                textCarType.setText((String) typeComboBox.getSelectedItem());
            } else {
                textCarType.setText(EMPTY);
                updateTrains();
            }
        } else {
            JCheckBox cb = (JCheckBox) ae.getSource();
            log.debug("Checkbox " + cb.getName() + " text: " + cb.getText());
            if (trainList.contains(cb)) {
                log.debug("Checkbox train " + cb.getText());
            } else {
                log.error("Error checkbox not found");
            }
        }
    }

    private void removePropertyChangeTrains() {
        if (trainList != null) {
            for (int i = 0; i < trainList.size(); i++) {
                // if object has been deleted, it's not here; ignore it
                Train train = manager.getTrainById(trainList.get(i).getName());
                if (train != null) {
                    train.removePropertyChangeListener(this);
                }
            }
        }
    }

    @Override
    public void dispose() {
        manager.removePropertyChangeListener(this);
        InstanceManager.getDefault(CarTypes.class).removePropertyChangeListener(this);
        removePropertyChangeTrains();
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.debug("Property change {} old: {} new: {}", e.getPropertyName(), e.getOldValue(), e.getNewValue());
        if (e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Train.TYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Train.NAME_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Train.DESCRIPTION_CHANGED_PROPERTY)) {
            updateTrains();
        }
        if (e.getPropertyName().equals(CarTypes.CARTYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY)) {
            updateComboBox();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrainsByCarTypeFrame.class);
}
