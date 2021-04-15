package jmri.jmrit.logixng;

import java.util.List;

import jmri.NamedBean;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.SymbolTable.VariableData;

/**
 * A LogixNG male socket.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface MaleSocket extends Debugable {

    public enum ErrorHandlingType {
        
        Default(Bundle.getMessage("ErrorHandling_Default")),
        ShowDialogBox(Bundle.getMessage("ErrorHandling_ShowDialogBox")),
        LogError(Bundle.getMessage("ErrorHandling_LogError")),
        LogErrorOnce(Bundle.getMessage("ErrorHandling_LogErrorOnce")),
        ThrowException(Bundle.getMessage("ErrorHandling_ThrowException")),
        AbortExecution(Bundle.getMessage("ErrorHandling_AbortExecution"));
        
        private final String _description;
        
        private ErrorHandlingType(String description) {
            _description = description;
        }
        
        @Override
        public String toString() {
            return _description;
        }
    }
    
    /**
     * Set whenether this male socket is enabled or disabled.
     * <P>
     * This method must call registerListeners() / unregisterListeners().
     * 
     * @param enable true if this male socket should be enabled, false otherwise
     */
    public void setEnabled(boolean enable);
    
    /**
     * Set whenether this male socket is enabled or disabled, without activating
     * the male socket. This is used when loading the xml file.
     * <P>
     * This method must call registerListeners() / unregisterListeners().
     * 
     * @param enable true if this male socket should be enabled, false otherwise
     */
    public void setEnabledFlag(boolean enable);
    
    /**
     * Determines whether this male socket is enabled.
     * 
     * @return true if the male socket is enabled, false otherwise
     */
    @Override
    public boolean isEnabled();
    
    public boolean getListen();
    
    public void setListen(boolean listen);
    
    public void addLocalVariable(
            String name,
            InitialValueType initialValueType,
            String initialValueData);
    
    public void addLocalVariable(VariableData variableData);
    
    public void clearLocalVariables();
    
    public List<VariableData> getLocalVariables();
    
    /**
     * Get the error handling type for this socket.
     * @return the error handling type
     */
    public ErrorHandlingType getErrorHandlingType();
    
    /**
     * Set the error handling type for this socket.
     * @param errorHandlingType the error handling type
     */
    public void setErrorHandlingType(ErrorHandlingType errorHandlingType);
    
    /**
     * Get the object that this male socket holds.
     * This method is used when the object is going to be configured.
     * 
     * @return the object this male socket holds
     */
    public Base getObject();

    /**
     * Get the manager that stores this socket.
     * This method is used when the object is going to be configured.
     * 
     * @return the manager
     */
    public BaseManager<? extends NamedBean> getManager();

    /** {@inheritDoc} */
    @Override
    default public void setup() {
        getObject().setup();
    }

    /**
     * Find a male socket of a particular type.
     * Male sockets can be stacked and this method travels thru the stacked
     * male sockets to find the desired male socket.
     * @param clazz the type of the male socket we are looking for
     * @return the found male socket or null if not found
     */
    public default MaleSocket find(Class clazz) {
        
        if (! MaleSocket.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("clazz is not a MaleSocket");
        }
        
        Base item = this;
        
        while ((item instanceof MaleSocket) && !clazz.isInstance(item)) {
            item = item.getParent();
        }
        
        if (clazz.isInstance(item)) return (MaleSocket)item;
        else return null;
    }

}
