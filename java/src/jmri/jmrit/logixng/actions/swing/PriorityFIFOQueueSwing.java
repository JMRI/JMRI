package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.PriorityFIFOQueue;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;

/**
 * Configures an PriorityFIFOQueue object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class PriorityFIFOQueueSwing extends AbstractDigitalActionSwing {

    private JTextField _numPrioritiesTextField;
    private int _numPriorities = 0;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        PriorityFIFOQueue action = (PriorityFIFOQueue) object;

        panel = new JPanel();

        _numPrioritiesTextField = new JTextField(4);
        _numPrioritiesTextField.setText("0");

        if (action != null) {
            _numPrioritiesTextField.setText(Integer.toString(action.getNumPriorities()));
        }

        JPanel timeField = new JPanel();
        JLabel timelabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("PriorityFIFOQueue_NumPrioritiesLabel")));
        timeField.add(timelabel);
        timeField.add(_numPrioritiesTextField);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(timeField);

        JComponent[] components = new JComponent[]{
            container};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("PriorityFIFOQueue_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        try {
            _numPriorities = Integer.parseInt(_numPrioritiesTextField.getText().trim());
        } catch (NumberFormatException ex) {
            errorMessages.add(Bundle.getMessage("PriorityFIFOQueue_ParseError", ex));
        }

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public String getAutoSystemName() {
        return InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        PriorityFIFOQueue action = new PriorityFIFOQueue(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof PriorityFIFOQueue)) {
            throw new IllegalArgumentException("object must be a PriorityFIFOQueue but is a: "+object.getClass().getName());
        }
        PriorityFIFOQueue action = (PriorityFIFOQueue) object;

        action.setNumPriorities(_numPriorities);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("PriorityFIFOQueue_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PriorityFIFOQueueSwing.class);

}
