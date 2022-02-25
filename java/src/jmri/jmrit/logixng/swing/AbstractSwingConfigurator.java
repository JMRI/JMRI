package jmri.jmrit.logixng.swing;

import javax.annotation.Nonnull;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import jmri.JmriException;
import jmri.jmrit.logixng.*;

/**
 * Abstract class for SwingConfiguratorInterface
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public abstract class AbstractSwingConfigurator implements SwingConfiguratorInterface {
    
    private JDialog _dialog;
    
    /** {@inheritDoc} */
    @Override
    public void setJDialog(JDialog dialog) {
        _dialog = dialog;
    }
    
    /** {@inheritDoc} */
    @Override
    public JDialog getJDialog() {
        return _dialog;
    }
    
    private void getSymbols(@Nonnull Base object, SymbolTable symbolTable) throws JmriException {
        if (object.getParent() != null) getSymbols(object.getParent(), symbolTable);
        
        if (object instanceof MaleSocket) {
            symbolTable.createSymbols(symbolTable, ((MaleSocket)object).getLocalVariables());
        }
    }
    
    public void getAllSymbols(@Nonnull Base object, SymbolTable symbolTable) {
        try {
            getSymbols(object.getParent(), symbolTable);
        } catch (JmriException e) {
            JOptionPane.showMessageDialog(null,
                    e.getLocalizedMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String getExecuteEvaluateMenuText() {
        throw new RuntimeException("Not supported. Class: " + this.getClass().getName());
    }
    
    /** {@inheritDoc} */
    @Override
    public void executeEvaluate(@Nonnull Base object) {
        throw new RuntimeException("Not supported");
    }
    
}
