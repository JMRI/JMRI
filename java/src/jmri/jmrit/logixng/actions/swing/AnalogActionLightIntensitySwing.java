package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AnalogActionLightIntensity;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;

/**
 * Configures an AnalogActionLightIntensity object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class AnalogActionLightIntensitySwing extends AbstractAnalogActionSwing {

    private LogixNG_SelectNamedBeanSwing<VariableLight> _selectNamedBeanSwing;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        AnalogActionLightIntensity action = (AnalogActionLightIntensity)object;

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(VariableLightManager.class), getJDialog(), this);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel _tabbedPaneNamedBean;
        if (action != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
        }

        panel.add(_tabbedPaneNamedBean);

        panel.add(javax.swing.Box.createVerticalStrut(10));

        JPanel labelPanel = new JPanel();
        labelPanel.add(new JLabel(Bundle.getMessage("AnalogActionLightIntensity_Descr")));
        panel.add(labelPanel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        AnalogActionLightIntensity action = new AnalogActionLightIntensity("IQAA1", null);

        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);
        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        AnalogActionLightIntensity action = new AnalogActionLightIntensity(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(AnalogActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof AnalogActionLightIntensity)) {
            throw new IllegalArgumentException("object must be an AnalogActionLightIntensity but is a: "+object.getClass().getName());
        }
        AnalogActionLightIntensity action = (AnalogActionLightIntensity)object;
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("AnalogActionLightIntensity_Short");
    }

    @Override
    public void dispose() {
    }

}
