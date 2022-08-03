package jmri.jmrit.operations.trains;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user edit of a train's road options
 *
 * @author Dan Boudreau Copyright (C) 2013, 2022
 * 
 */
public class TrainRoadOptionsFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    Train _train = null;

    JPanel pCarRoadControls = new JPanel();
    JPanel panelCarRoads = new JPanel();
    JScrollPane paneCarRoads = new JScrollPane(panelCarRoads);
    
    JPanel pLocoRoadControls = new JPanel();
    JPanel panelLocoRoads = new JPanel();
    JScrollPane paneLocoRoads = new JScrollPane(panelLocoRoads);

    // labels
    JLabel trainName = new JLabel();
    JLabel trainDescription = new JLabel();

    // major buttons
    JButton addCarRoadButton = new JButton(Bundle.getMessage("AddRoad"));
    JButton deleteCarRoadButton = new JButton(Bundle.getMessage("DeleteRoad"));
    JButton deleteCarAllRoadsButton = new JButton(Bundle.getMessage("DeleteAll"));
    JButton addLocoRoadButton = new JButton(Bundle.getMessage("AddRoad"));
    JButton deleteLocoRoadButton = new JButton(Bundle.getMessage("DeleteRoad"));
    JButton deleteLocoAllRoadsButton = new JButton(Bundle.getMessage("DeleteAll"));

    JButton saveTrainButton = new JButton(Bundle.getMessage("SaveTrain"));

    // radio buttons
    JRadioButton carRoadNameAll = new JRadioButton(Bundle.getMessage("AcceptAll"));
    JRadioButton carRoadNameInclude = new JRadioButton(Bundle.getMessage("AcceptOnly"));
    JRadioButton carRoadNameExclude = new JRadioButton(Bundle.getMessage("Exclude"));
    JRadioButton locoRoadNameAll = new JRadioButton(Bundle.getMessage("AcceptAll"));
    JRadioButton locoRoadNameInclude = new JRadioButton(Bundle.getMessage("AcceptOnly"));
    JRadioButton locoRoadNameExclude = new JRadioButton(Bundle.getMessage("Exclude"));

    // combo boxes
    JComboBox<String> comboBoxCarRoads = InstanceManager.getDefault(CarRoads.class).getComboBox();
    JComboBox<String> comboBoxLocoRoads = InstanceManager.getDefault(CarRoads.class).getComboBox();

    public static final String DISPOSE = "dispose"; // NOI18N

    public TrainRoadOptionsFrame() {
        super(Bundle.getMessage("MenuItemRoadOptions"));
    }

    public void initComponents(TrainEditFrame parent) {

        parent.setChildFrame(this);
        _train = parent._train;

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Layout the panel by rows
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.setMaximumSize(new Dimension(2000, 250));

        // Layout the panel by rows
        // row 1a
        JPanel pName = new JPanel();
        pName.setLayout(new GridBagLayout());
        pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Name")));
        addItem(pName, trainName, 0, 0);

        // row 1b
        JPanel pDesc = new JPanel();
        pDesc.setLayout(new GridBagLayout());
        pDesc.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Description")));
        addItem(pDesc, trainDescription, 0, 0);

        p1.add(pName);
        p1.add(pDesc);

        // row 3 Car Roads
        JPanel pCarRoad = new JPanel();
        pCarRoad.setLayout(new BoxLayout(pCarRoad, BoxLayout.Y_AXIS));
        JScrollPane paneCar = new JScrollPane(pCarRoad);
        paneCar.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("CarRoadsTrain")));
        paneCar.setMaximumSize(new Dimension(2000, 400));

        JPanel pCarRoadRadioButtons = new JPanel();
        pCarRoadRadioButtons.setLayout(new FlowLayout());

        pCarRoadRadioButtons.add(carRoadNameAll);
        pCarRoadRadioButtons.add(carRoadNameInclude);
        pCarRoadRadioButtons.add(carRoadNameExclude);

        pCarRoadControls.setLayout(new FlowLayout());

        pCarRoadControls.add(comboBoxCarRoads);
        pCarRoadControls.add(addCarRoadButton);
        pCarRoadControls.add(deleteCarRoadButton);
        pCarRoadControls.add(deleteCarAllRoadsButton);

        pCarRoadControls.setVisible(false);

        pCarRoad.add(pCarRoadRadioButtons);
        pCarRoad.add(pCarRoadControls);

        // row 4
        panelCarRoads.setLayout(new GridBagLayout());
        paneCarRoads.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Roads")));

        ButtonGroup carRoadGroup = new ButtonGroup();
        carRoadGroup.add(carRoadNameAll);
        carRoadGroup.add(carRoadNameInclude);
        carRoadGroup.add(carRoadNameExclude);
        
        // row 5 Engine Roads
        JPanel pLocoRoad = new JPanel();
        pLocoRoad.setLayout(new BoxLayout(pLocoRoad, BoxLayout.Y_AXIS));
        JScrollPane paneLoco = new JScrollPane(pLocoRoad);
        paneLoco.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("LocoRoadsTrain")));
        paneLoco.setMaximumSize(new Dimension(2000, 400));

        JPanel pLocoRoadRadioButtons = new JPanel();
        pLocoRoadRadioButtons.setLayout(new FlowLayout());

        pLocoRoadRadioButtons.add(locoRoadNameAll);
        pLocoRoadRadioButtons.add(locoRoadNameInclude);
        pLocoRoadRadioButtons.add(locoRoadNameExclude);

        pLocoRoadControls.setLayout(new FlowLayout());

        pLocoRoadControls.add(comboBoxLocoRoads);
        pLocoRoadControls.add(addLocoRoadButton);
        pLocoRoadControls.add(deleteLocoRoadButton);
        pLocoRoadControls.add(deleteLocoAllRoadsButton);

        pLocoRoadControls.setVisible(false);

        pLocoRoad.add(pLocoRoadRadioButtons);
        pLocoRoad.add(pLocoRoadControls);

        // row 4
        panelLocoRoads.setLayout(new GridBagLayout());
        paneLocoRoads.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Roads")));

        ButtonGroup locoRoadGroup = new ButtonGroup();
        locoRoadGroup.add(locoRoadNameAll);
        locoRoadGroup.add(locoRoadNameInclude);
        locoRoadGroup.add(locoRoadNameExclude);

        // row 12
        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new GridBagLayout());
        panelButtons.setBorder(BorderFactory.createTitledBorder(""));
        panelButtons.setMaximumSize(new Dimension(2000, 200));

        // row 13
        addItem(panelButtons, saveTrainButton, 0, 0);

        getContentPane().add(p1);
        getContentPane().add(paneCar);
        getContentPane().add(paneCarRoads);
        getContentPane().add(paneLoco);
        getContentPane().add(paneLocoRoads);
        getContentPane().add(panelButtons);

        // setup buttons
        addButtonAction(saveTrainButton);

        addButtonAction(deleteCarRoadButton);
        addButtonAction(deleteCarAllRoadsButton);
        addButtonAction(addCarRoadButton);

        addRadioButtonAction(carRoadNameAll);
        addRadioButtonAction(carRoadNameInclude);
        addRadioButtonAction(carRoadNameExclude);
        
        addButtonAction(deleteLocoRoadButton);
        addButtonAction(deleteLocoAllRoadsButton);
        addButtonAction(addLocoRoadButton);

        addRadioButtonAction(locoRoadNameAll);
        addRadioButtonAction(locoRoadNameInclude);
        addRadioButtonAction(locoRoadNameExclude);

        if (_train != null) {
            trainName.setText(_train.getName());
            trainDescription.setText(_train.getDescription());
            updateButtons(true);
            // listen for train changes
            _train.addPropertyChangeListener(this);
        } else {
            updateButtons(false);
        }
        addHelpMenu("package.jmri.jmrit.operations.Operations_TrainRoadOptions", true); // NOI18N
        updateRoadComboBoxes();
        updateCarRoadNames();
        updateLocoRoadNames();

        // get notified if car roads, roads, and owners gets modified
        InstanceManager.getDefault(CarTypes.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(CarRoads.class).addPropertyChangeListener(this);

        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight500));
    }

    // Save
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (_train != null) {
            if (ae.getSource() == saveTrainButton) {
                log.debug("train save button activated");
                saveTrain();
            }
            if (ae.getSource() == addCarRoadButton) {
                String roadName = (String) comboBoxCarRoads.getSelectedItem();
                if (_train.addCarRoadName(roadName)) {
                    updateCarRoadNames();
                }
                selectNextItemComboBox(comboBoxCarRoads);
            }
            if (ae.getSource() == deleteCarRoadButton) {
                String roadName = (String) comboBoxCarRoads.getSelectedItem();
                if (_train.deleteCarRoadName(roadName)) {
                    updateCarRoadNames();
                }
                selectNextItemComboBox(comboBoxCarRoads);
            }
            if (ae.getSource() == deleteCarAllRoadsButton) {
                deleteAllCarRoads();
            }
            if (ae.getSource() == addLocoRoadButton) {
                String roadName = (String) comboBoxLocoRoads.getSelectedItem();
                if (_train.addLocoRoadName(roadName)) {
                    updateLocoRoadNames();
                }
                selectNextItemComboBox(comboBoxLocoRoads);
            }
            if (ae.getSource() == deleteLocoRoadButton) {
                String roadName = (String) comboBoxLocoRoads.getSelectedItem();
                if (_train.deleteLocoRoadName(roadName)) {
                    updateLocoRoadNames();
                }
                selectNextItemComboBox(comboBoxLocoRoads);
            }
            if (ae.getSource() == deleteLocoAllRoadsButton) {
                deleteAllLocoRoads();
            }
        }
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("radio button activated");
        if (_train != null) {
            if (ae.getSource() == carRoadNameAll) {
                _train.setCarRoadOption(Train.ALL_LOADS);
                updateCarRoadNames();
            }
            if (ae.getSource() == carRoadNameInclude) {
                _train.setCarRoadOption(Train.INCLUDE_LOADS);
                updateCarRoadNames();
            }
            if (ae.getSource() == carRoadNameExclude) {
                _train.setCarRoadOption(Train.EXCLUDE_LOADS);
                updateCarRoadNames();
            }
            if (ae.getSource() == locoRoadNameAll) {
                _train.setLocoRoadOption(Train.ALL_LOADS);
                updateLocoRoadNames();
            }
            if (ae.getSource() == locoRoadNameInclude) {
                _train.setLocoRoadOption(Train.INCLUDE_LOADS);
                updateLocoRoadNames();
            }
            if (ae.getSource() == locoRoadNameExclude) {
                _train.setLocoRoadOption(Train.EXCLUDE_LOADS);
                updateLocoRoadNames();
            }
        }
    }

    protected void updateButtons(boolean enabled) {
        saveTrainButton.setEnabled(enabled);

        carRoadNameAll.setEnabled(enabled);
        carRoadNameInclude.setEnabled(enabled);
        carRoadNameExclude.setEnabled(enabled);
        
        locoRoadNameAll.setEnabled(enabled);
        locoRoadNameInclude.setEnabled(enabled);
        locoRoadNameExclude.setEnabled(enabled);
    }

    private static final int NUMBER_ROADS_PER_LINE = 6;

    private void updateCarRoadNames() {
        log.debug("Update car road names");
        panelCarRoads.removeAll();
        if (_train != null) {
            // set radio button
            carRoadNameAll.setSelected(_train.getCarRoadOption().equals(Train.ALL_LOADS));
            carRoadNameInclude.setSelected(_train.getCarRoadOption().equals(Train.INCLUDE_ROADS));
            carRoadNameExclude.setSelected(_train.getCarRoadOption().equals(Train.EXCLUDE_ROADS));

            pCarRoadControls.setVisible(!carRoadNameAll.isSelected());

            if (!carRoadNameAll.isSelected()) {
                int x = 0;
                int y = 0; // vertical position in panel

                for (String roadName : _train.getCarRoadNames()) {
                    JLabel road = new JLabel();
                    road.setText(roadName);
                    addItemTop(panelCarRoads, road, x++, y);
                    // limit the number of roads per line
                    if (x > NUMBER_ROADS_PER_LINE) {
                        y++;
                        x = 0;
                    }
                }
                revalidate();
            }
        } else {
            carRoadNameAll.setSelected(true);
        }
        panelCarRoads.repaint();
        panelCarRoads.revalidate();
    }
    
    private void updateLocoRoadNames() {
        log.debug("Update loco road names");
        panelLocoRoads.removeAll();
        if (_train != null) {
            // set radio button
            locoRoadNameAll.setSelected(_train.getLocoRoadOption().equals(Train.ALL_LOADS));
            locoRoadNameInclude.setSelected(_train.getLocoRoadOption().equals(Train.INCLUDE_ROADS));
            locoRoadNameExclude.setSelected(_train.getLocoRoadOption().equals(Train.EXCLUDE_ROADS));

            pLocoRoadControls.setVisible(!locoRoadNameAll.isSelected());

            if (!locoRoadNameAll.isSelected()) {
                int x = 0;
                int y = 0; // vertical position in panel

                for (String roadName : _train.getLocoRoadNames()) {
                    JLabel road = new JLabel();
                    road.setText(roadName);
                    addItemTop(panelLocoRoads, road, x++, y);
                    // limit the number of roads per line
                    if (x > NUMBER_ROADS_PER_LINE) {
                        y++;
                        x = 0;
                    }
                }
                revalidate();
            }
        } else {
            locoRoadNameAll.setSelected(true);
        }
        panelLocoRoads.repaint();
        panelLocoRoads.revalidate();
    }

    private void deleteAllCarRoads() {
        if (_train != null) {
            for (String road : _train.getCarRoadNames()) {
                _train.deleteCarRoadName(road);
            }
        }
        updateCarRoadNames();
    }
    
    private void deleteAllLocoRoads() {
        if (_train != null) {
            for (String road : _train.getLocoRoadNames()) {
                _train.deleteLocoRoadName(road);
            }
        }
        updateLocoRoadNames();
    }

    private void saveTrain() {
        OperationsXml.save();
        if (Setup.isCloseWindowOnSaveEnabled()) {
            dispose();
        }
    }

    private void updateRoadComboBoxes() {
        InstanceManager.getDefault(CarRoads.class).updateComboBox(comboBoxCarRoads);
        InstanceManager.getDefault(CarRoads.class).updateComboBox(comboBoxLocoRoads);
    }

    @Override
    public void dispose() {
        InstanceManager.getDefault(CarTypes.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarRoads.class).removePropertyChangeListener(this);
        if (_train != null) {
            _train.removePropertyChangeListener(this);
        }
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(CarRoads.CARROADS_CHANGED_PROPERTY)) {
            updateRoadComboBoxes();
            updateCarRoadNames();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrainRoadOptionsFrame.class);
}
