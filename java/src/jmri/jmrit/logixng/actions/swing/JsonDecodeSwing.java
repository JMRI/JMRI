package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.JsonDecode;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;

/**
 * Configures an JsonDecode object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2024
 */
public class JsonDecodeSwing extends AbstractDigitalActionSwing {

    private JTextField _jsonLocalVariableTextField;
    private JTextField _resultLocalVariableTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        JsonDecode action = (JsonDecode)object;

        panel = new JPanel();

        _jsonLocalVariableTextField = new JTextField(20);
        _resultLocalVariableTextField = new JTextField(20);

        if (action != null) {
            if (action.getResultLocalVariable() != null) {
                _jsonLocalVariableTextField.setText(action.getJsonLocalVariable());
            }
            if (action.getResultLocalVariable() != null) {
                _resultLocalVariableTextField.setText(action.getResultLocalVariable());
            }
        }

        JComponent[] components = new JComponent[]{
            _jsonLocalVariableTextField,
            _resultLocalVariableTextField
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("JsonDecode_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        JsonDecode action = new JsonDecode(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof JsonDecode)) {
            throw new IllegalArgumentException("object must be an JsonDecode but is a: "+object.getClass().getName());
        }
        JsonDecode action = (JsonDecode)object;

        action.setJsonLocalVariable(_jsonLocalVariableTextField.getText());
        action.setResultLocalVariable(_resultLocalVariableTextField.getText());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("JsonDecode_Short");
    }

    @Override
    public void dispose() {
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonDecodeSwing.class);

}
