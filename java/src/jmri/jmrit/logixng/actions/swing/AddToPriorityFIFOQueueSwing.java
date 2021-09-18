package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AddToPriorityFIFOQueue;
import jmri.jmrit.logixng.actions.PriorityFIFOQueue;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an AddPriorityFIFOQueue object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class AddToPriorityFIFOQueueSwing extends AbstractDigitalActionSwing {

    private JComboBox<String> _priorityFIFOQueueComboBox;
    private JTextField _priorityTextField;
    private int _priority = 0;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        AddToPriorityFIFOQueue action = (AddToPriorityFIFOQueue) object;

        panel = new JPanel();

        _priorityFIFOQueueComboBox = new JComboBox<>();
        _priorityFIFOQueueComboBox.addItem("");
        for (Base bean : InstanceManager.getDefault(DigitalActionManager.class).getNamedBeanSet()) {
            while (bean instanceof MaleSocket) {
                bean = ((MaleSocket)bean).getObject();
            }
            if (bean instanceof PriorityFIFOQueue) {
                PriorityFIFOQueue priorityFIFOQueue = (PriorityFIFOQueue)bean;
                _priorityFIFOQueueComboBox.addItem(priorityFIFOQueue.getDisplayName());
                if (action != null) {
                    NamedBeanHandle<PriorityFIFOQueue> handle = action.getPriorityFIFOQueue();
                    if ((handle != null) && (handle.getName().equals(priorityFIFOQueue.getDisplayName()))) {
                        _priorityFIFOQueueComboBox.setSelectedItem(priorityFIFOQueue.getDisplayName());
                    }
                }
            }
        }
        JComboBoxUtil.setupComboBoxMaxRows(_priorityFIFOQueueComboBox);
        
        _priorityTextField = new JTextField(4);
        _priorityTextField.setText("0");

        if (action != null) {
            _priorityTextField.setText(Integer.toString(action.getPriority()));
        }

        JPanel priorityField = new JPanel();
//        JLabel timelabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("AddToPriorityFIFOQueue_PriorityLabel")));
//        priorityField.add(timelabel);
        priorityField.add(_priorityTextField);

//        JPanel container = new JPanel();
//        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
//        container.add(priorityField);

        JComponent[] components = new JComponent[]{
            priorityField,
            _priorityFIFOQueueComboBox,
//            container
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("AddToPriorityFIFOQueue_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        try {
            _priority = Integer.parseInt(_priorityTextField.getText().trim());
        } catch (NumberFormatException ex) {
            errorMessages.add(Bundle.getMessage("AddToPriorityFIFOQueue_ParseError", ex));
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
        AddToPriorityFIFOQueue action = new AddToPriorityFIFOQueue(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof AddToPriorityFIFOQueue)) {
            throw new IllegalArgumentException("object must be an AddToPriorityFIFOQueue but is a: "+object.getClass().getName());
        }
        
        AddToPriorityFIFOQueue action = (AddToPriorityFIFOQueue) object;
        
        String priorityFIFOQueueName = _priorityFIFOQueueComboBox.getItemAt(_priorityFIFOQueueComboBox.getSelectedIndex());
        if (priorityFIFOQueueName.isEmpty()) action.removePriorityFIFOQueue();
        else action.setPriorityFIFOQueue(priorityFIFOQueueName);
        
        action.setPriority(_priority);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("AddToPriorityFIFOQueue_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PriorityFIFOQueueSwing.class);

}
