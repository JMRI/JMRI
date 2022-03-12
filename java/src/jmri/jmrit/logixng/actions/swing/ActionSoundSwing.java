package jmri.jmrit.logixng.actions.swing;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionSound;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.FileUtil;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionSound object with a Swing JPanel.
 *
 * @author Daniel Bergqvist 2021
 */
public class ActionSoundSwing extends AbstractDigitalActionSwing {

    public static final int NUM_COLUMNS_TEXT_FIELDS = 20;

    private JTabbedPane _tabbedPaneOperationType;
    private JPanel _panelOperationTypeDirect;
    private JPanel _panelOperationTypeReference;
    private JPanel _panelOperationTypeLocalVariable;
    private JPanel _panelOperationTypeFormula;

    private JComboBox<ActionSound.Operation> _operationComboBox;
    private JTextField _soundOperationReferenceTextField;
    private JTextField _soundOperationLocalVariableTextField;
    private JTextField _soundOperationFormulaTextField;

    private JTabbedPane _tabbedPaneSoundType;
    private JPanel _panelSoundTypeDirect;
    private JPanel _panelSoundTypeReference;
    private JPanel _panelSoundTypeLocalVariable;
    private JPanel _panelSoundTypeFormula;

    private JFileChooser soundFileChooser;
    private JTextField _soundTextField;
    private JTextField _soundReferenceTextField;
    private JTextField _soundLocalVariableTextField;
    private JTextField _soundFormulaTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionSound action = (ActionSound)object;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel actionPanel = new JPanel();


        // Set up the tabbed pane for selecting the operation
        _tabbedPaneOperationType = new JTabbedPane();
        _panelOperationTypeDirect = new javax.swing.JPanel();
        _panelOperationTypeDirect.setLayout(new BoxLayout(_panelOperationTypeDirect, BoxLayout.Y_AXIS));
        _panelOperationTypeReference = new javax.swing.JPanel();
        _panelOperationTypeReference.setLayout(new BoxLayout(_panelOperationTypeReference, BoxLayout.Y_AXIS));
        _panelOperationTypeLocalVariable = new javax.swing.JPanel();
        _panelOperationTypeLocalVariable.setLayout(new BoxLayout(_panelOperationTypeLocalVariable, BoxLayout.Y_AXIS));
        _panelOperationTypeFormula = new javax.swing.JPanel();
        _panelOperationTypeFormula.setLayout(new BoxLayout(_panelOperationTypeFormula, BoxLayout.Y_AXIS));

        _tabbedPaneOperationType.addTab(NamedBeanAddressing.Direct.toString(), _panelOperationTypeDirect);
        _tabbedPaneOperationType.addTab(NamedBeanAddressing.Reference.toString(), _panelOperationTypeReference);
        _tabbedPaneOperationType.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelOperationTypeLocalVariable);
        _tabbedPaneOperationType.addTab(NamedBeanAddressing.Formula.toString(), _panelOperationTypeFormula);

        _operationComboBox = new JComboBox<>();
        for (ActionSound.Operation e : ActionSound.Operation.values()) {
            _operationComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_operationComboBox);
        _panelOperationTypeDirect.add(new JLabel(Bundle.getMessage("ActionSound_Operation")));
        _panelOperationTypeDirect.add(_operationComboBox);

        _soundOperationReferenceTextField = new JTextField();
        _soundOperationReferenceTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelOperationTypeReference.add(new JLabel(Bundle.getMessage("ActionSound_Operation")));
        _panelOperationTypeReference.add(_soundOperationReferenceTextField);

        _soundOperationLocalVariableTextField = new JTextField();
        _soundOperationLocalVariableTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelOperationTypeLocalVariable.add(new JLabel(Bundle.getMessage("ActionSound_Operation")));
        _panelOperationTypeLocalVariable.add(_soundOperationLocalVariableTextField);

        _soundOperationFormulaTextField = new JTextField();
        _soundOperationFormulaTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelOperationTypeFormula.add(new JLabel(Bundle.getMessage("ActionSound_Operation")));
        _panelOperationTypeFormula.add(_soundOperationFormulaTextField);


        // Set up the tabbed pane for selecting the appearance
        _tabbedPaneSoundType = new JTabbedPane();
        _panelSoundTypeDirect = new javax.swing.JPanel();
//        _panelSoundTypeDirect.setLayout(new BoxLayout(_panelSoundTypeDirect, BoxLayout.Y_AXIS));
        _panelSoundTypeReference = new javax.swing.JPanel();
        _panelSoundTypeReference.setLayout(new BoxLayout(_panelSoundTypeReference, BoxLayout.Y_AXIS));
        _panelSoundTypeLocalVariable = new javax.swing.JPanel();
        _panelSoundTypeLocalVariable.setLayout(new BoxLayout(_panelSoundTypeLocalVariable, BoxLayout.Y_AXIS));
        _panelSoundTypeFormula = new javax.swing.JPanel();
        _panelSoundTypeFormula.setLayout(new BoxLayout(_panelSoundTypeFormula, BoxLayout.Y_AXIS));

        _tabbedPaneSoundType.addTab(NamedBeanAddressing.Direct.toString(), _panelSoundTypeDirect);
        _tabbedPaneSoundType.addTab(NamedBeanAddressing.Reference.toString(), _panelSoundTypeReference);
        _tabbedPaneSoundType.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelSoundTypeLocalVariable);
        _tabbedPaneSoundType.addTab(NamedBeanAddressing.Formula.toString(), _panelSoundTypeFormula);

        JButton _actionSelectFileButton = new JButton("..."); // "File" replaced by ...
        _actionSelectFileButton.setMaximumSize(_actionSelectFileButton.getPreferredSize());
        _actionSelectFileButton.setToolTipText(Bundle.getMessage("FileButtonHint"));  // NOI18N
        _actionSelectFileButton.addActionListener((ActionEvent e) -> {
            soundFileChooser = new JFileChooser(System.getProperty("user.dir") // NOI18N
                    + java.io.File.separator + "resources" // NOI18N
                    + java.io.File.separator + "sounds");  // NOI18N
            soundFileChooser.setFileFilter(new FileNameExtensionFilter("wav sound files", "wav")); // NOI18N
            soundFileChooser.rescanCurrentDirectory();
            int retVal = soundFileChooser.showOpenDialog(null);
            // handle selection or cancel
            if (retVal == JFileChooser.APPROVE_OPTION) {
                // set selected file location
                try {
                    _soundTextField.setText(FileUtil.getPortableFilename(soundFileChooser.getSelectedFile().getCanonicalPath()));
                } catch (java.io.IOException ex) {
                    log.error("exception setting file location", ex);  // NOI18N
                    _soundTextField.setText("");
                }
            }
        });
        _panelSoundTypeDirect.add(_actionSelectFileButton);
        JPanel _soundTextPanel = new JPanel();
        _soundTextPanel.setLayout(new BoxLayout(_soundTextPanel, BoxLayout.Y_AXIS));
        _soundTextField = new JTextField(30);
        _soundTextPanel.add(new JLabel(Bundle.getMessage("ActionSound_Sound")));
        _soundTextPanel.add(_soundTextField);
        _panelSoundTypeDirect.add(_soundTextPanel);

        _soundReferenceTextField = new JTextField();
        _soundReferenceTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelSoundTypeReference.add(new JLabel(Bundle.getMessage("ActionSound_Sound")));
        _panelSoundTypeReference.add(_soundReferenceTextField);

        _soundLocalVariableTextField = new JTextField();
        _soundLocalVariableTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelSoundTypeLocalVariable.add(new JLabel(Bundle.getMessage("ActionSound_Sound")));
        _panelSoundTypeLocalVariable.add(_soundLocalVariableTextField);

        _soundFormulaTextField = new JTextField();
        _soundFormulaTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelSoundTypeFormula.add(new JLabel(Bundle.getMessage("ActionSound_Sound")));
        _panelSoundTypeFormula.add(_soundFormulaTextField);


        if (action != null) {
            switch (action.getOperationAddressing()) {
                case Direct: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeDirect); break;
                case Reference: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeReference); break;
                case LocalVariable: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeLocalVariable); break;
                case Formula: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getOperationAddressing().name());
            }
            _operationComboBox.setSelectedItem(action.getOperation());
            _soundOperationReferenceTextField.setText(action.getOperationReference());
            _soundOperationLocalVariableTextField.setText(action.getOperationLocalVariable());
            _soundOperationFormulaTextField.setText(action.getOperationFormula());

            switch (action.getSoundAddressing()) {
                case Direct: _tabbedPaneSoundType.setSelectedComponent(_panelSoundTypeDirect); break;
                case Reference: _tabbedPaneSoundType.setSelectedComponent(_panelSoundTypeReference); break;
                case LocalVariable: _tabbedPaneSoundType.setSelectedComponent(_panelSoundTypeLocalVariable); break;
                case Formula: _tabbedPaneSoundType.setSelectedComponent(_panelSoundTypeFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getSoundAddressing().name());
            }
            _soundTextField.setText(action.getSound());
            _soundReferenceTextField.setText(action.getSoundReference());
            _soundLocalVariableTextField.setText(action.getSoundLocalVariable());
            _soundFormulaTextField.setText(action.getSoundFormula());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneOperationType,
            _tabbedPaneSoundType
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionSound_Components"), components);

        for (JComponent c : componentList) actionPanel.add(c);
        panel.add(actionPanel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionSound action = new ActionSound("IQDA1", null);

        try {
            if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeReference) {
                action.setOperationReference(_soundOperationReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            action.setSoundFormula(_soundFormulaTextField.getText());
            if (_tabbedPaneSoundType.getSelectedComponent() == _panelSoundTypeDirect) {
                action.setSoundAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneSoundType.getSelectedComponent() == _panelSoundTypeReference) {
                action.setSoundAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneSoundType.getSelectedComponent() == _panelSoundTypeLocalVariable) {
                action.setSoundAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneSoundType.getSelectedComponent() == _panelSoundTypeFormula) {
                action.setSoundAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPaneSoundType has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionSound action = new ActionSound(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionSound)) {
            throw new IllegalArgumentException("object must be an ActionSound but is a: "+object.getClass().getName());
        }
        ActionSound action = (ActionSound)object;

        try {
            if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeDirect) {
                action.setOperationAddressing(NamedBeanAddressing.Direct);
                action.setOperation((ActionSound.Operation)_operationComboBox.getSelectedItem());
            } else if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeReference) {
                action.setOperationAddressing(NamedBeanAddressing.Reference);
                action.setOperationReference(_soundOperationReferenceTextField.getText());
            } else if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeLocalVariable) {
                action.setOperationAddressing(NamedBeanAddressing.LocalVariable);
                action.setOperationLocalVariable(_soundOperationLocalVariableTextField.getText());
            } else if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeFormula) {
                action.setOperationAddressing(NamedBeanAddressing.Formula);
                action.setOperationFormula(_soundOperationFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneOperationType has unknown selection");
            }

            if (_tabbedPaneSoundType.getSelectedComponent() == _panelSoundTypeDirect) {
                action.setSoundAddressing(NamedBeanAddressing.Direct);
                action.setSound(_soundTextField.getText());
            } else if (_tabbedPaneSoundType.getSelectedComponent() == _panelSoundTypeReference) {
                action.setSoundAddressing(NamedBeanAddressing.Reference);
                action.setSoundReference(_soundReferenceTextField.getText());
            } else if (_tabbedPaneSoundType.getSelectedComponent() == _panelSoundTypeLocalVariable) {
                action.setSoundAddressing(NamedBeanAddressing.LocalVariable);
                action.setSoundLocalVariable(_soundLocalVariableTextField.getText());
            } else if (_tabbedPaneSoundType.getSelectedComponent() == _panelSoundTypeFormula) {
                action.setSoundAddressing(NamedBeanAddressing.Formula);
                action.setSoundFormula(_soundFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneAspectType has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionSound_Short");
    }

    @Override
    public void dispose() {
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSoundSwing.class);

}
