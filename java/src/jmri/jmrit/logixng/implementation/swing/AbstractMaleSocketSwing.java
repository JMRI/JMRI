package jmri.jmrit.logixng.implementation.swing;

import java.awt.GridBagConstraints;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.MaleSocket.ErrorHandlingType;
import jmri.jmrit.logixng.implementation.AbstractMaleSocket;
import jmri.jmrit.logixng.swing.AbstractSwingConfigurator;
import jmri.util.swing.JComboBoxUtil;

/**
 * Abstract class for SwingConfiguratorInterface
 */
public abstract class AbstractMaleSocketSwing extends AbstractSwingConfigurator {

    private JPanel panel;
    private final JLabel errorHandlingLabel = new JLabel(Bundle.getMessage("MaleSocket_ErrorHandlingLabel"));
    private final JLabel catchAbortExecutionLabel = new JLabel(Bundle.getMessage("MaleSocket_CatchAbortExecutionCheckBox"));
    private JComboBox<ErrorHandlingType> errorHandlingComboBox;
    private JCheckBox catchAbortExecutionCheckBox;
    private JPanel subPanel;
    
    
    protected JPanel getSubPanel(@CheckForNull Base object) {
        return null;
    }
    
    /** {@inheritDoc} */
    @Override
    public BaseManager<? extends NamedBean> getManager() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    /** {@inheritDoc} */
    @Override
    public final JPanel getConfigPanel(@Nonnull JPanel buttonPanel) throws IllegalArgumentException {
        createPanel(null, buttonPanel);
        return panel;
    }
    
    /** {@inheritDoc} */
    @Override
    public final JPanel getConfigPanel(@Nonnull Base object, @Nonnull JPanel buttonPanel) throws IllegalArgumentException {
        createPanel(object, buttonPanel);
        return panel;
    }
    
    protected final void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && (! (object instanceof AbstractMaleSocket))) {
            throw new IllegalArgumentException("object is not an AbstractMaleSocket: " + object.getClass().getName());
        }
        
        panel = new JPanel();
        
        AbstractMaleSocket maleSocket = (AbstractMaleSocket)object;
        
        panel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        panel.add(errorHandlingLabel, c);
        
        c.gridx = 1;
        errorHandlingComboBox = new JComboBox<>();
        for (ErrorHandlingType type : ErrorHandlingType.values()) {
            errorHandlingComboBox.addItem(type);
            if ((maleSocket != null) && (maleSocket.getErrorHandlingType() == type)) {
                errorHandlingComboBox.setSelectedItem(type);
            }
        }
        JComboBoxUtil.setupComboBoxMaxRows(errorHandlingComboBox);
        
        panel.add(errorHandlingComboBox, c);
        
        c.gridx = 0;
        c.gridy = 1;
        panel.add(catchAbortExecutionLabel, c);
        
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        catchAbortExecutionCheckBox = new JCheckBox();
        if (maleSocket != null) {
            catchAbortExecutionCheckBox.setSelected(maleSocket.getCatchAbortExecution());
        }
//        catchAbortExecutionCheckBox.setAlignmentX(0);
        catchAbortExecutionLabel.setLabelFor(catchAbortExecutionCheckBox);
        panel.add(catchAbortExecutionCheckBox, c);
        
        subPanel = getSubPanel(object);
        if (subPanel != null) {
            JPanel thisPanel = panel;
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(thisPanel);
            panel.add(subPanel);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public final boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        // Male sockets of this type is created by the system, not by the user
        throw new UnsupportedOperationException("Not supported");
    }
    
    /** {@inheritDoc} */
    @OverridingMethodsMustInvokeSuper
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof AbstractMaleSocket)) {
            throw new IllegalArgumentException("object is not an AbstractMaleSocket: " + object.getClass().getName());
        }
        
        AbstractMaleSocket maleSocket = (AbstractMaleSocket)object;
        maleSocket.setErrorHandlingType(errorHandlingComboBox.getItemAt(errorHandlingComboBox.getSelectedIndex()));
        maleSocket.setCatchAbortExecution(catchAbortExecutionCheckBox.isSelected());
    }
    
    /** {@inheritDoc} */
    @Override
    public String getExampleSystemName() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAutoSystemName() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    @Override
    public final void dispose() {
    }
    
    /*.*
     * Dispose the sub panel and remove all the listeners that this class may
     * have registered.
     *./
    public void disposeSubPanel() {
    }
*/    
}
