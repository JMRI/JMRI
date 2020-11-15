package jmri.jmrit.logixng.implementation;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.util.LoggingUtil;

import org.slf4j.Logger;

/**
 * The abstract class that is the base class for all LogixNG classes that
 * implements the Base interface.
 */
public abstract class AbstractMaleSocket implements MaleSocket, InternalBase {

    protected final Map<String, SymbolTable.ParameterData> _localVariables = new HashMap<>();
    private final BaseManager<? extends NamedBean> _manager;
    private Base _parent;
    private ErrorHandlingType _errorHandlingType = ErrorHandlingType.LOG_ERROR;
    
    public AbstractMaleSocket(BaseManager<? extends NamedBean> manager) {
        _manager = manager;
    }
    
    @Override
    public void addLocalVariable(
            String name,
            SymbolTable.InitialValueType initialValueType,
            String initialValueData) {
        
        _localVariables.put(name,
                new DefaultSymbolTable.DefaultParameterData(
                        name,
                        initialValueType,
                        initialValueData,
                        SymbolTable.ReturnValueType.None,
                        null));
    }
    
    @Override
    public void removeLocalVariable(String name) {
        _localVariables.remove(name);
    }
    
    @Override
    public Collection<SymbolTable.ParameterData> getLocalVariables() {
        return _localVariables.values();
    }

    @Override
    public final Base getParent() {
        return _parent;
    }
    
    @Override
    public final void setParent(Base parent) {
        _parent = parent;
    }
    
    /** {@inheritDoc} */
    @Override
    public final void setParentForAllChildren() {
        for (int i=0; i < getChildCount(); i++) {
            FemaleSocket femaleSocket = getChild(i);
            femaleSocket.setParent(this);
            if (femaleSocket.isConnected()) {
                MaleSocket connectedSocket = femaleSocket.getConnectedSocket();
                connectedSocket.setParent(femaleSocket);
                connectedSocket.setParentForAllChildren();
            }
        }
    }
    
    /**
     * Register listeners if this object needs that.
     * <P>
     * Important: This method may be called more than once. Methods overriding
     * this method must ensure that listeners are not registered more than once.
     */
    abstract protected void registerListenersForThisClass();
    
    /**
     * Unregister listeners if this object needs that.
     * <P>
     * Important: This method may be called more than once. Methods overriding
     * this method must ensure that listeners are not unregistered more than once.
     */
    abstract protected void unregisterListenersForThisClass();
    
    /** {@inheritDoc} */
    @Override
    public final void registerListeners() {
        registerListenersForThisClass();
        for (int i=0; i < getChildCount(); i++) {
            ((InternalBase)getChild(i)).registerListeners();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public final void unregisterListeners() {
        unregisterListenersForThisClass();
        for (int i=0; i < getChildCount(); i++) {
            ((InternalBase)getChild(i)).unregisterListeners();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public final boolean isActive() {
        return isEnabled() && ((getParent() == null) || getParent().isActive());
    }
    
    protected void printTreeRow(Locale locale, PrintWriter writer, String currentIndent) {
        writer.append(currentIndent);
        writer.append(getLongDescription(locale));
        if (getComment() != null) {
            writer.append(" ::: ");
            writer.append(getComment());
        }
        writer.println();
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(PrintWriter writer, String indent) {
        printTree(Locale.getDefault(), writer, indent, "");
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(Locale locale, PrintWriter writer, String indent) {
        printTree(locale, writer, indent, "");
    }
    
    /**
     * Print the tree to a stream.
     * This method is the implementation of printTree(PrintStream, String)
     * 
     * @param writer the stream to print the tree to
     * @param indent the indentation of each level
     * @param currentIndent the current indentation
     */
    @Override
    public void printTree(Locale locale, PrintWriter writer, String indent, String currentIndent) {
        printTreeRow(locale, writer, currentIndent);
        
        for (int i=0; i < getChildCount(); i++) {
            getChild(i).printTree(locale, writer, indent, currentIndent+indent);
        }
    }
    
    @Override
    public BaseManager<? extends NamedBean> getManager() {
        return _manager;
    }
    
    /**
     * Disposes this object.
     * This must remove _all_ connections!
     */
    abstract protected void disposeMe();
    
    /** {@inheritDoc} */
    @Override
    public final void dispose() {
        for (int i=0; i < getChildCount(); i++) {
            getChild(i).dispose();
        }
        disposeMe();
    }
    
    @Override
    public ErrorHandlingType getErrorHandlingType() {
        return _errorHandlingType;
    }
    
    @Override
    public void setErrorHandlingType(ErrorHandlingType errorHandlingType)
    {
        _errorHandlingType = errorHandlingType;
    }
    
    public void handleError(Base item, String message, JmriException e, Logger log) throws JmriException {
        switch (_errorHandlingType) {
//            case SHOW_DIALOG_BOX:
//                InstanceManager.getDefault(ErrorHandlerManager.class)
//                        .notifyError(this, Bundle.getMessage("ExceptionSetValue", e), e);
//                break;
                
            case LOG_ERROR:
//                e.printStackTrace();
                log.error("item {}, {} thrown an exception: {}", item.toString(), getObject().toString(), e, e);
                break;
                
            case LOG_ERROR_ONCE:
//                e.printStackTrace();
                LoggingUtil.warnOnce(log, "item {}, {} thrown an exception: {}", item.toString(), getObject().toString(), e, e);
                break;
                
            case THROW:
                throw e;
                
            default:
                throw e;
        }
    }
    
    public void handleError(Base item, String message, RuntimeException e, Logger log) throws JmriException {
        switch (_errorHandlingType) {
//            case SHOW_DIALOG_BOX:
//                InstanceManager.getDefault(ErrorHandlerManager.class)
//                        .notifyError(this, Bundle.getMessage("ExceptionSetValue", e), e);
//                break;
                
            case LOG_ERROR:
//                e.printStackTrace();
                log.error("item {}, {} thrown an exception: {}", item.toString(), getObject().toString(), e, e);
                break;
                
            case LOG_ERROR_ONCE:
//                e.printStackTrace();
                LoggingUtil.warnOnce(log, "item {}, {} thrown an exception: {}", item.toString(), getObject().toString(), e, e);
                break;
                
            case THROW:
                throw e;
                
            default:
                throw e;
        }
    }
    
}
