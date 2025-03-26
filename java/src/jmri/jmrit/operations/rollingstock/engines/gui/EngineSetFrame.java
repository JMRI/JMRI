package jmri.jmrit.operations.rollingstock.engines.gui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.RollingStockSetFrame;
import jmri.jmrit.operations.rollingstock.engines.*;
import jmri.jmrit.operations.rollingstock.engines.tools.EngineAttributeEditFrame;
import jmri.jmrit.operations.setup.Control;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for user to place engine on the layout
 *
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2024
 */
public class EngineSetFrame extends RollingStockSetFrame<Engine> {

    protected static final ResourceBundle rb = ResourceBundle
            .getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");

    EngineManager manager = InstanceManager.getDefault(EngineManager.class);
    EngineManagerXml managerXml = InstanceManager.getDefault(EngineManagerXml.class);
    ConsistManager consistManager = InstanceManager.getDefault(ConsistManager.class);

    protected JComboBox<String> consistComboBox = consistManager.getComboBox();
    public JCheckBox ignoreConsistCheckBox = new JCheckBox(Bundle.getMessage("Ignore"));
    protected JButton editConsistButton = new JButton(Bundle.getMessage("ButtonEdit"));

    protected boolean askConsistChange = true;

    public Engine _engine;

    private String _help = "package.jmri.jmrit.operations.Operations_LocomotivesSet";

    public EngineSetFrame() {
        super(Bundle.getMessage("TitleEngineSet"));
    }

    public void initComponents(String help) {
        _help = help;
        initComponents();
    }

    @Override
    public void initComponents() {
        super.initComponents();

        // build menu
        addHelpMenu(_help, true); // NOI18N

        // disable location unknown, final destination fields
        locationUnknownCheckBox.setVisible(false);
        pFinalDestination.setVisible(false);
        autoTrainCheckBox.setVisible(false);

        pOptional.setLayout(new BoxLayout(pOptional, BoxLayout.Y_AXIS));

        // add Consist fields
        JPanel pConsist = new JPanel();
        pConsist.setLayout(new GridBagLayout());
        pConsist.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Consist")));
        addItemLeft(pConsist, ignoreConsistCheckBox, 1, 0);
        consistComboBox.setName("consistComboBox"); // NOI18N for UI Test
        addItem(pConsist, consistComboBox, 2, 0);
        addItem(pConsist, editConsistButton, 3, 0);
        pOptional.add(pConsist);

        // don't show ignore checkboxes
        ignoreConsistCheckBox.setVisible(false);

        addButtonAction(editConsistButton);
        addCheckBoxAction(ignoreConsistCheckBox);

        consistManager.addPropertyChangeListener(this);

        // tool tips
        outOfServiceCheckBox.setToolTipText(getRb().getString("TipLocoOutOfService"));

        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight400));
    }

    public void load(Engine engine) {
        _engine = engine;
        updateConsistComboBox();
        super.load(engine);
    }

    @Override
    protected ResourceBundle getRb() {
        return rb;
    }

    @Override
    protected void enableComponents(boolean enabled) {
        super.enableComponents(enabled);
        ignoreConsistCheckBox.setEnabled(enabled);
        consistComboBox.setEnabled(!ignoreConsistCheckBox.isSelected() && enabled);
        editConsistButton.setEnabled(!ignoreConsistCheckBox.isSelected() && enabled && _engine != null);
    }

    EngineAttributeEditFrame eaef;

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        super.buttonActionPerformed(ae);
        if (ae.getSource() == editConsistButton) {
            if (eaef != null) {
                eaef.dispose();
            }
            eaef = new EngineAttributeEditFrame();
            eaef.initComponents(EngineAttributeEditFrame.CONSIST, (String) consistComboBox.getSelectedItem());
        }
    }

    @Override
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        super.checkBoxActionPerformed(ae);
        if (ae.getSource() == ignoreConsistCheckBox) {
            consistComboBox.setEnabled(!ignoreConsistCheckBox.isSelected());
            editConsistButton.setEnabled(!ignoreConsistCheckBox.isSelected());
        }
    }

    protected void updateConsistComboBox() {
        consistManager.updateComboBox(consistComboBox);
        if (_engine != null) {
            consistComboBox.setSelectedItem(_engine.getConsistName());
        }
    }

    @Override
    protected boolean save() {
        if (change(_engine)) {
            OperationsXml.save();
            return true;
        }
        return false;
    }

    protected boolean change(Engine engine) {
        // consist
        if (consistComboBox.getSelectedItem() != null) {
            if (consistComboBox.getSelectedItem().equals(ConsistManager.NONE)) {
                engine.setConsist(null);
                engine.setBlocking(Engine.DEFAULT_BLOCKING_ORDER);
            } else if (!engine.getConsistName().equals(consistComboBox.getSelectedItem())) {
                engine.setConsist(consistManager.getConsistByName((String) consistComboBox.getSelectedItem()));
                if (engine.getConsist() != null) {
                    engine.setBlocking(engine.getConsist().getSize());
                }
            }
        }
        if (!super.change(engine)) {
            return false;
        }

        // check for train change
        checkTrain(engine);

        // is this engine part of a consist?
        if (askConsistChange && _engine.getConsist() != null) {
            if (JmriJOptionPane.showConfirmDialog(this, Bundle.getMessage("engineInConsist"),
                    Bundle.getMessage("enginePartConsist"),
                    JmriJOptionPane.YES_NO_OPTION) == JmriJOptionPane.YES_OPTION) {
                // convert cars list to rolling stock list
                List<Engine> list = _engine.getConsist().getEngines();
                if (!updateGroup(list)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void dispose() {
        if (eaef != null) {
            eaef.dispose();
        }
        consistManager.removePropertyChangeListener(this);
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        super.propertyChange(e);
        if (e.getPropertyName().equals(ConsistManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateConsistComboBox();
        }
    }

    //    private final static Logger log = LoggerFactory.getLogger(EngineSetFrame.class);
}
