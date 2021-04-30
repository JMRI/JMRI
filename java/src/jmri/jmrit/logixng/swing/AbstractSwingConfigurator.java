package jmri.jmrit.logixng.swing;

import javax.swing.JFrame;

/**
 * Abstract class for SwingConfiguratorInterface
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public abstract class AbstractSwingConfigurator implements SwingConfiguratorInterface {
    
    private JFrame _frame;
    
    /** {@inheritDoc} */
    @Override
    public void setFrame(JFrame frame) {
        _frame = frame;
    }
    
    /** {@inheritDoc} */
    @Override
    public JFrame getFrame() {
        return _frame;
    }
    
}
