package jmri.jmrit.operations.rollingstock.engines;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.RollingStockAttribute;
import jmri.jmrit.operations.rollingstock.RollingStockEditFrame;
import jmri.jmrit.operations.rollingstock.engines.tools.EngineAttributeEditFrame;
import jmri.jmrit.operations.setup.Control;

/**
 * Frame for user edit of engine
 *
 * @author Dan Boudreau Copyright (C) 2008, 2011, 2018
 */
public class EngineEditFrame extends RollingStockEditFrame {

    protected static final ResourceBundle rb = ResourceBundle
            .getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");

    EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);
    EngineManagerXml managerXml = InstanceManager.getDefault(EngineManagerXml.class);
    EngineModels engineModels = InstanceManager.getDefault(EngineModels.class);

    JButton editModelButton = new JButton(Bundle.getMessage("ButtonEdit"));

    JCheckBox bUnitCheckBox = new JCheckBox(Bundle.getMessage("BUnit"));

    JTextField hpTextField = new JTextField(8);

    EngineAttributeEditFrame engineAttributeEditFrame;

    public EngineEditFrame() {
        super(Bundle.getMessage("TitleEngineAdd"));
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Checks for null")
    @Override
    public void initComponents() {

        groupComboBox = InstanceManager.getDefault(EngineManager.class).getConsistComboBox();
        modelComboBox = engineModels.getComboBox();

        super.initComponents();

        addButton.setText(Bundle.getMessage("TitleEngineAdd"));

        // type options for engines
        addItem(pTypeOptions, bUnitCheckBox, 0, 1);

        // default check box selections
        bUnitCheckBox.setSelected(false);

        // load tool tips
        builtTextField.setToolTipText(Bundle.getMessage("buildDateTip"));
        editModelButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                new Object[]{Bundle.getMessage("Model").toLowerCase()}));
        editGroupButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                new Object[]{Bundle.getMessage("Consist").toLowerCase()}));
        bUnitCheckBox.setToolTipText(Bundle.getMessage("TipBoosterUnit"));

        deleteButton.setToolTipText(Bundle.getMessage("TipDeleteButton"));
        addButton.setToolTipText(Bundle.getMessage("TipAddButton"));
        saveButton.setToolTipText(Bundle.getMessage("TipSaveButton"));

        // row 3
        pModel.setLayout(new GridBagLayout());
        pModel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Model")));
        addItem(pModel, modelComboBox, 1, 0);
        addItem(pModel, editModelButton, 2, 0);
        pModel.setVisible(true);

        // row 12
        pHp.setLayout(new GridBagLayout());
        pHp.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Hp")));
        addItem(pHp, hpTextField, 0, 0);
        pHp.setVisible(true);

        pGroup.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Consist")));

        addEditButtonAction(editModelButton);

        addComboBoxAction(modelComboBox);
        modelComboBox.setSelectedIndex(0);

        addHelpMenu("package.jmri.jmrit.operations.Operations_LocomotivesAdd", true); // NOI18N
    }

    @Override
    protected ResourceBundle getRb() {
        return rb;
    }

    @Override
    protected RollingStockAttribute getTypeManager() {
        return InstanceManager.getDefault(EngineTypes.class);
    }

    @Override
    protected RollingStockAttribute getLengthManager() {
        return InstanceManager.getDefault(EngineLengths.class);
    }

    public void load(Engine engine) {
        if (!engineModels.containsName(engine.getModel())) {
            String msg = MessageFormat.format(Bundle.getMessage("modelNameNotExist"),
                    new Object[]{engine.getModel()});
            if (JOptionPane
                    .showConfirmDialog(this, msg, Bundle.getMessage("engineAddModel"),
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                engineModels.addName(engine.getModel());
            }
        }
        modelComboBox.setSelectedItem(engine.getModel());

        super.load(engine);

        pBlocking.setVisible(engine.getConsist() != null);
        blockingTextField.setEnabled(false); // don't allow user to modify, only see
        bUnitCheckBox.setSelected(engine.isBunit());
        hpTextField.setText(engine.getHp());
        groupComboBox.setSelectedItem(engine.getConsistName());

        setTitle(Bundle.getMessage("TitleEngineEdit"));
    }

    // combo boxes
    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == modelComboBox) {
            if (modelComboBox.getSelectedItem() != null) {
                String model = (String) modelComboBox.getSelectedItem();
                // load the default hp and length for the model selected
                hpTextField.setText(engineModels.getModelHorsepower(model));
                weightTonsTextField.setText(engineModels.getModelWeight(model));
                if (engineModels.getModelLength(model) != null && !engineModels.getModelLength(model).equals("")) {
                    lengthComboBox.setSelectedItem(engineModels.getModelLength(model));
                }
                if (engineModels.getModelType(model) != null && !engineModels.getModelType(model).equals("")) {
                    typeComboBox.setSelectedItem(engineModels.getModelType(model));
                }
            }
        }
        super.comboBoxActionPerformed(ae);
    }

    @Override
    protected boolean check(RollingStock engine) {
        // check to see if engine with road and number already exists
        Engine existingEngine =
                engineManager.getByRoadAndNumber((String) roadComboBox.getSelectedItem(), roadNumberTextField
                        .getText());
        if (existingEngine != null) {
            if (engine == null || !existingEngine.getId().equals(engine.getId())) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("engineExists"), Bundle
                        .getMessage("engineCanNotUpdate"), JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return super.check(engine);
    }

    @Override
    protected void save(boolean isSave) {
        super.save(engineManager, isSave);
        Engine engine = (Engine) _rs;

        engine.setBunit(bUnitCheckBox.isSelected());

        if (groupComboBox.getSelectedItem() != null) {
            if (groupComboBox.getSelectedItem().equals(EngineManager.NONE)) {
                engine.setConsist(null);
                if (engine.isBunit())
                    engine.setBlocking(Engine.B_UNIT_BLOCKING);
                else
                    engine.setBlocking(Engine.DEFAULT_BLOCKING_ORDER);
            } else if (!engine.getConsistName().equals(groupComboBox.getSelectedItem())) {
                engine.setConsist(engineManager.getConsistByName((String) groupComboBox.getSelectedItem()));
                if (engine.getConsist() != null) {
                    engine.setBlocking(engine.getConsist().getSize());
                    blockingTextField.setText(Integer.toString(engine.getBlocking()));
                }
            }
        }
        pBlocking.setVisible(engine.getConsist() != null);

        // confirm that horsepower is a number
        if (!hpTextField.getText().trim().isEmpty()) {
            try {
                Integer.parseInt(hpTextField.getText());
                engine.setHp(hpTextField.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("engineHorsepower"), Bundle
                        .getMessage("engineCanNotHp"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    protected void delete() {
        Engine engine = engineManager.getByRoadAndNumber((String) roadComboBox.getSelectedItem(), roadNumberTextField
                .getText());
        if (engine != null) {
            engineManager.deregister(engine);
        }
    }

    // edit buttons only one frame active at a time
    @Override
    public void buttonEditActionPerformed(java.awt.event.ActionEvent ae) {
        if (engineAttributeEditFrame != null) {
            engineAttributeEditFrame.dispose();
        }
        engineAttributeEditFrame = new EngineAttributeEditFrame();
        engineAttributeEditFrame.setLocationRelativeTo(this);
        engineAttributeEditFrame.addPropertyChangeListener(this);

        if (ae.getSource() == editRoadButton) {
            engineAttributeEditFrame.initComponents(EngineAttributeEditFrame.ROAD,
                    (String) roadComboBox.getSelectedItem());
        }
        if (ae.getSource() == editModelButton) {
            engineAttributeEditFrame.initComponents(EngineAttributeEditFrame.MODEL,
                    (String) modelComboBox.getSelectedItem());
        }
        if (ae.getSource() == editTypeButton) {
            engineAttributeEditFrame.initComponents(EngineAttributeEditFrame.TYPE,
                    (String) typeComboBox.getSelectedItem());
        }
        if (ae.getSource() == editLengthButton) {
            engineAttributeEditFrame.initComponents(EngineAttributeEditFrame.LENGTH,
                    (String) lengthComboBox.getSelectedItem());
        }
        if (ae.getSource() == editOwnerButton) {
            engineAttributeEditFrame.initComponents(EngineAttributeEditFrame.OWNER,
                    (String) ownerComboBox.getSelectedItem());
        }
        if (ae.getSource() == editGroupButton) {
            engineAttributeEditFrame.initComponents(EngineAttributeEditFrame.CONSIST,
                    (String) groupComboBox.getSelectedItem());
        }
    }

    @Override
    protected void addPropertyChangeListeners() {
        engineModels.addPropertyChangeListener(this);
        engineManager.addPropertyChangeListener(this);
        super.addPropertyChangeListeners();
    }

    @Override
    protected void removePropertyChangeListeners() {
        engineModels.removePropertyChangeListener(this);
        engineManager.removePropertyChangeListener(this);
        if (_rs != null) {
            _rs.removePropertyChangeListener(this);
        }
        super.removePropertyChangeListeners();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        super.propertyChange(e);

        if (e.getPropertyName().equals(EngineLengths.ENGINELENGTHS_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(EngineLengths.class).updateComboBox(lengthComboBox);
            if (_rs != null) {
                lengthComboBox.setSelectedItem(_rs.getLength());
            }
        }
        if (e.getPropertyName().equals(EngineModels.ENGINEMODELS_CHANGED_PROPERTY)) {
            engineModels.updateComboBox(modelComboBox);
            if (_rs != null) {
                modelComboBox.setSelectedItem(((Engine) _rs).getModel());
            }
        }
        if (e.getPropertyName().equals(EngineManager.CONSISTLISTLENGTH_CHANGED_PROPERTY)) {
            engineManager.updateConsistComboBox(groupComboBox);
            if (_rs != null) {
                groupComboBox.setSelectedItem(((Engine) _rs).getConsistName());
            }
        }
        if (e.getPropertyName().equals(EngineAttributeEditFrame.DISPOSE)) {
            engineAttributeEditFrame = null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EngineEditFrame.class);

}
