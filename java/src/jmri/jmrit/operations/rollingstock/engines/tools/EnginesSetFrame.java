package jmri.jmrit.operations.rollingstock.engines.tools;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.gui.*;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for user to modify a group of engines on the layout
 *
 * @author Dan Boudreau Copyright (C) 2024
 */
public class EnginesSetFrame extends EngineSetFrame {

    EnginesTableModel _enginesTableModel;
    JTable _enginesTable;

    public EnginesSetFrame() {
        super();
    }

    // Ignore checkbox states
    private static boolean ignoreStatusCheckBoxSelected = true;
    private static boolean ignoreLocationCheckBoxSelected = true;
    private static boolean ignoreConsistCheckBoxSelected = true;
    private static boolean ignoreDestinationCheckBoxSelected = true;
    private static boolean ignoreTrainCheckBoxSelected = true;

    public void initComponents(JTable enginesTable) {
        _enginesTable = enginesTable;
        _enginesTableModel = (EnginesTableModel) enginesTable.getModel();

        super.initComponents("package.jmri.jmrit.operations.Operations_SetEngines");

        setTitle(Bundle.getMessage("TitleSetEngines"));
        // modify Save button text to "Apply";
        saveButton.setText(Bundle.getMessage("ButtonApply"));

        // show ignore checkboxes
        ignoreStatusCheckBox.setVisible(true);
        ignoreLocationCheckBox.setVisible(true);
        ignoreConsistCheckBox.setVisible(true);
        ignoreDestinationCheckBox.setVisible(true);
        ignoreTrainCheckBox.setVisible(true);
        ignoreAllButton.setVisible(true);

        // set the last state
        ignoreStatusCheckBox.setSelected(ignoreStatusCheckBoxSelected);
        ignoreLocationCheckBox.setSelected(ignoreLocationCheckBoxSelected);
        ignoreConsistCheckBox.setSelected(ignoreConsistCheckBoxSelected);
        ignoreDestinationCheckBox.setSelected(ignoreDestinationCheckBoxSelected);
        ignoreTrainCheckBox.setSelected(ignoreTrainCheckBoxSelected);

        // first engine in the list becomes the master
        int rows[] = _enginesTable.getSelectedRows();
        if (rows.length > 0) {
            Engine engine = _enginesTableModel.getEngineAtIndex(_enginesTable.convertRowIndexToModel(rows[0]));
            super.load(engine);
        } else {
            enableComponents(true);
            showMessageDialogWarning();
        }
    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        super.buttonActionPerformed(ae);
        if (ae.getSource() == ignoreAllButton) {
            ignoreAll(toggle);
        }
    }

    boolean toggle = false;

    protected void ignoreAll(boolean b) {
        ignoreStatusCheckBox.setSelected(!locationUnknownCheckBox.isSelected() & b);
        ignoreLocationCheckBox.setSelected(b);
        ignoreConsistCheckBox.setSelected(b);
        ignoreDestinationCheckBox.setSelected(b);
        ignoreTrainCheckBox.setSelected(b);
        enableComponents(!locationUnknownCheckBox.isSelected());
        toggle = !b;
    }

    @Override
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "GUI ease of use")
    protected boolean save() {
        // save ignore states
        ignoreStatusCheckBoxSelected = ignoreStatusCheckBox.isSelected();
        ignoreLocationCheckBoxSelected = ignoreLocationCheckBox.isSelected();
        ignoreConsistCheckBoxSelected = ignoreConsistCheckBox.isSelected();
        ignoreDestinationCheckBoxSelected = ignoreConsistCheckBox.isSelected();
        ignoreTrainCheckBoxSelected = ignoreTrainCheckBox.isSelected();

        // need to get selected engines before they are modified their location in the table can change
        List<Engine> engines = new ArrayList<Engine>();
        int rows[] = _enginesTable.getSelectedRows();
        for (int row : rows) {
            Engine engine = _enginesTableModel.getEngineAtIndex(_enginesTable.convertRowIndexToModel(row));
            log.debug("Adding selected engine {} to change list", engine.toString());
            engines.add(engine);
        }
        if (rows.length == 0) {
            showMessageDialogWarning();
            return false;
        } else if (engines.get(0) != _engine) {
            log.debug("Default engine isn't the first one selected");
            if (JmriJOptionPane.showConfirmDialog(this, Bundle
                    .getMessage("doYouWantToChange", engines.get(0).toString()),
                    Bundle
                            .getMessage("changeDefaultEngine"),
                    JmriJOptionPane.YES_NO_OPTION) == JmriJOptionPane.YES_OPTION) {
                super.load(engines.get(0)); // new default engine
                return false; // done, don't modify any of the engines selected
            }
        }

        // don't ask for to change engines in a consist when giving a selected group of engines a new consist name
        askConsistChange = false;
        
        // determine if all engines in every consist are selected
        for (Engine engine : engines) {
            if (engine.getConsist() != null) {
                for (Engine c : engine.getConsist().getEngines()) {
                    if (!engines.contains(c)) {
                        askConsistChange = true; // not all selected
                        break;
                    }
                }
            }
        }

        for (Engine engine : engines) {
            if (!super.change(engine)) {
                return false;
            } else if (engine.getConsist() != null && !ignoreConsistCheckBox.isSelected()) {
                askConsistChange = false; // changing consist name
            }
        }
        return false; // all good, but don't close window
    }
    
    private void showMessageDialogWarning() {
        JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("selectEngines"), Bundle
                .getMessage("engineNoneSelected"), JmriJOptionPane.WARNING_MESSAGE);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EnginesSetFrame.class);
}
