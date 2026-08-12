package jmri.jmrit.operations.trains.manualtrainbuilder.gui;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.manualtrainbuilder.TrainManualBuild;
import jmri.jmrit.operations.trains.manualtrainbuilder.TrainManualBuildManager;
import jmri.swing.JTablePersistenceManager;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for user edit of a manual build
 *
 * @author Dan Boudreau Copyright (C) 2026
 */
public class TrainManualBuildEditFrame extends OperationsFrame {

    TrainManualBuildTableModel manualBuildModel = new TrainManualBuildTableModel();
    JTable manualBuildTable = new JTable(manualBuildModel);

    TrainManualBuildManager manualBuildManager;
    TrainManualBuild _manualBuild = null;

    // major buttons
    JButton addButton = new JButton(Bundle.getMessage("AddCar"));
    JButton saveManualBuildButton = new JButton(Bundle.getMessage("SaveManualBuild"));
    JButton deleteManualBuildButton = new JButton(Bundle.getMessage("DeleteManualBuild"));

    // radio buttons
    JRadioButton addLocAtTop = new JRadioButton(Bundle.getMessage("Top"));
    JRadioButton addLocAtMiddle = new JRadioButton(Bundle.getMessage("Middle"));
    JRadioButton addLocAtBottom = new JRadioButton(Bundle.getMessage("Bottom"));

    // text field
    JTextField commentTextField = new JTextField(45);

    public TrainManualBuildEditFrame(String trainId) {
        super(Bundle.getMessage("TitleManualBuild"));

        // load managers
        manualBuildManager = InstanceManager.getDefault(TrainManualBuildManager.class);

        // creates a new one or returns one that already exists
        _manualBuild = manualBuildManager.newManualBuild(trainId);

        // Set up the jtable in a Scroll Pane..
        JScrollPane manualBuildPane = new JScrollPane(manualBuildTable);
        manualBuildPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        manualBuildModel.initTable(this, manualBuildTable, _manualBuild);
        commentTextField.setText(_manualBuild.getComment());

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Layout the panel by rows
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));

        JScrollPane p1Pane = new JScrollPane(p1);
        p1Pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        p1Pane.setMaximumSize(new Dimension(2000, 200));
        p1Pane.setMinimumSize(new Dimension(200, 40));

        // row 1a name
        JPanel pName = new JPanel();
        pName.setLayout(new GridBagLayout());
        pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Name")));
        addItem(pName, new JLabel(_manualBuild.getTrainName()), 0, 0);

        // row 1b comment
        JPanel pC = new JPanel();
        pC.setLayout(new GridBagLayout());
        pC.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
        addItem(pC, commentTextField, 0, 0);

        p1.add(pName);
        p1.add(pC);

        // row 2
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        p2.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("AddCar")));
        addItem(p2, addButton, 1, 1);
        addItem(p2, addLocAtTop, 2, 1);
        addItem(p2, addLocAtMiddle, 3, 1);
        addItem(p2, addLocAtBottom, 4, 1);
        ButtonGroup group = new ButtonGroup();
        group.add(addLocAtTop);
        group.add(addLocAtMiddle);
        group.add(addLocAtBottom);
        addLocAtBottom.setSelected(true);

        p2.setMaximumSize(new Dimension(2000, 200));

        // row 3 buttons
        JPanel pB = new JPanel();
        pB.setLayout(new GridBagLayout());
        pB.setBorder(BorderFactory.createTitledBorder(""));
        pB.setMaximumSize(new Dimension(2000, 200));

        addItem(pB, deleteManualBuildButton, 0, 0);
        addItem(pB, saveManualBuildButton, 1, 0);

        getContentPane().add(p1Pane);
        getContentPane().add(manualBuildPane);
        getContentPane().add(p2);
        getContentPane().add(pB);

        // set up buttons
        addButtonAction(addButton);
        addButtonAction(deleteManualBuildButton);
        addButtonAction(saveManualBuildButton);

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_ManualBuilds", true); // NOI18N

        // set frame size
        initMinimumSize(new Dimension(Control.panelWidth700, Control.panelHeight400));
    }

    // Save, Delete, Add
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == addButton) {
            addNewManualBuildItem();
        }
        if (ae.getSource() == saveManualBuildButton) {
            log.debug("manualBuild save button activated");
            saveManualBuild();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
        if (ae.getSource() == deleteManualBuildButton) {
            log.debug("manualBuild delete button activated");
            if (JmriJOptionPane.showConfirmDialog(this,
                    Bundle.getMessage("DoYouWantToDeleteManualBuild"),
                    Bundle.getMessage("DeleteManualBuild?"),
                    JmriJOptionPane.YES_NO_OPTION) != JmriJOptionPane.YES_OPTION) {
                return;
            }
            manualBuildManager.deregister(_manualBuild);
            _manualBuild = null;
            
            OperationsXml.save();
            dispose();
        }
    }

    private void addNewManualBuildItem() {
        // add item to this manual build
        if (addLocAtTop.isSelected()) {
            _manualBuild.addItem(0);
        } else if (addLocAtMiddle.isSelected()) {
            if (manualBuildTable.getSelectedRow() >= 0) {
                int row = manualBuildTable.getSelectedRow();
                log.debug("Selected row: {}", row);
                _manualBuild.addItem(row);
                // we need to reselect the table since the content has changed
                manualBuildTable.getSelectionModel().setSelectionInterval(row, row);
            } else {
                _manualBuild.addItem(_manualBuild.getSize() / 2);
            }
        } else {
            _manualBuild.addItem();
        }
    }

    private void saveManualBuild() {
        _manualBuild.setComment(commentTextField.getText());

        if (manualBuildTable.isEditing()) {
            log.debug("manualBuild table edit true");
            manualBuildTable.getCellEditor().stopCellEditing();
            manualBuildTable.clearSelection();
        }
        // save manual build
        OperationsXml.save();
    }

    @Override
    public void dispose() {
        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent(tpm -> {
            tpm.stopPersisting(manualBuildTable);
        });
        manualBuildModel.dispose();
        super.dispose();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainManualBuildEditFrame.class);
}
