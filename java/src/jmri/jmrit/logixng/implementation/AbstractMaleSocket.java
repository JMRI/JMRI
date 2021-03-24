package jmri.jmrit.logixng.implementation;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.SymbolTable.VariableData;
import jmri.jmrit.logixng.implementation.swing.ErrorHandlingDialog;
import jmri.util.LoggingUtil;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;

/**
 * The abstract class that is the base class for all LogixNG classes that
 * implements the Base interface.
 * 
 * @author Daniel Bergqvist 2020
 */
public abstract class AbstractMaleSocket implements MaleSocket {

    protected final List<VariableData> _localVariables = new ArrayList<>();
    private final BaseManager<? extends NamedBean> _manager;
    private Base _parent;
    private ErrorHandlingType _errorHandlingType = ErrorHandlingType.LogError;
    private boolean _catchAbortExecution;
    
    public AbstractMaleSocket(BaseManager<? extends NamedBean> manager) {
        _manager = manager;
    }
    
    public boolean getCatchAbortExecution() {
        return _catchAbortExecution;
    }
    
    public void setCatchAbortExecution(boolean catchAbortExecution)
    {
        _catchAbortExecution = catchAbortExecution;
    }
    
    @Override
    public void addLocalVariable(
            String name,
            SymbolTable.InitialValueType initialValueType,
            String initialValueData) {
        
        if (getObject() instanceof MaleSocket) {
            ((MaleSocket)getObject()).addLocalVariable(name, initialValueType, initialValueData);
        } else {
            _localVariables.add(new VariableData(name, initialValueType, initialValueData));
        }
    }
    
    @Override
    public void addLocalVariable(VariableData variableData) {
        
        if (getObject() instanceof MaleSocket) {
            ((MaleSocket)getObject()).addLocalVariable(variableData);
        } else {
            _localVariables.add(variableData);
        }
    }
    
    @Override
    public void clearLocalVariables() {
        if (getObject() instanceof MaleSocket) {
            ((MaleSocket)getObject()).clearLocalVariables();
        } else {
            _localVariables.clear();
        }
    }
    
    @Override
    public List<VariableData> getLocalVariables() {
        if (getObject() instanceof MaleSocket) {
            return ((MaleSocket)getObject()).getLocalVariables();
        } else {
            return _localVariables;
        }
    }
    
    @Override
    public Base getParent() {
        return _parent;
    }
    
    @Override
    public void setParent(Base parent) {
        _parent = parent;
    }
    
    @Override
    public final ConditionalNG getConditionalNG() {
        if (getParent() == null) return null;
        return getParent().getConditionalNG();
    }
    
    @Override
    public final LogixNG getLogixNG() {
        if (getParent() == null) return null;
        return getParent().getLogixNG();
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
            getChild(i).registerListeners();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public final void unregisterListeners() {
        unregisterListenersForThisClass();
        for (int i=0; i < getChildCount(); i++) {
            getChild(i).unregisterListeners();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public final boolean isActive() {
        return isEnabled() && ((getParent() == null) || getParent().isActive());
    }
    
    /**
     * Print this row.
     * If getObject() doesn't return an AbstractMaleSocket, print this row.
     * <P>
     * If a male socket that extends AbstractMaleSocket wants to print
     * something here, it needs to override this method.
     * <P>
     * The reason this method doesn't print if getObject() returns an
     * AbstractMaleSocket is to protect so it doesn't print itself twice if
     * it's embedding an other AbstractMaleSocket. An example of this is the
     * AbstractDebuggerMaleSocket which embeds other male sockets.
     * 
     * @param settings settings for what to print
     * @param locale The locale to be used
     * @param writer the stream to print the tree to
     * @param currentIndent the current indentation
     */
    protected void printTreeRow(PrintTreeSettings settings, Locale locale, PrintWriter writer, String currentIndent) {
        if (!(getObject() instanceof AbstractMaleSocket)) {
            writer.append(currentIndent);
            writer.append(getLongDescription(locale));
            if (getUserName() != null) {
                writer.append(" ::: ");
                writer.append(Bundle.getMessage("LabelUserName"));
                writer.append(" ");
                writer.append(getUserName());
            }
            if (getComment() != null) {
                writer.append(" ::: ");
                writer.append(getComment());
            }
            
            if (settings._printErrorHandling) {
                writer.append(" ::: ");
                writer.append(getErrorHandlingType().toString());
            }
            if (!isEnabled()) {
                writer.append(" ::: ");
                writer.append(Bundle.getMessage("Disabled"));
            }
            writer.println();
        }
    }
    
    protected void printLocalVariable(
            Locale locale,
            PrintWriter writer,
            String currentIndent,
            VariableData localVariable) {
        
        writer.append(currentIndent);
        writer.append("   ::: ");
        writer.append(Bundle.getMessage(
                locale,
                "PrintLocalVariable",
                localVariable._name,
                localVariable._initalValueType.toString(),
                localVariable._initialValueData));
        writer.println();
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(PrintTreeSettings settings, PrintWriter writer, String indent) {
        printTree(settings, Locale.getDefault(), writer, indent, "");
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(PrintTreeSettings settings, Locale locale, PrintWriter writer, String indent) {
        printTree(settings, locale, writer, indent, "");
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
    public void printTree(PrintTreeSettings settings, Locale locale, PrintWriter writer, String indent, String currentIndent) {
        printTreeRow(settings, locale, writer, currentIndent);
        
        if (settings._printLocalVariables) {
            for (VariableData localVariable : _localVariables) {
                printLocalVariable(locale, writer, currentIndent, localVariable);
            }
        }
        
        if (getObject() instanceof MaleSocket) {
            getObject().printTree(settings, locale, writer, indent, currentIndent);
        } else {
            for (int i=0; i < getChildCount(); i++) {
                getChild(i).printTree(settings, locale, writer, indent, currentIndent+indent);
            }
        }
    }
    
    @Override
    public BaseManager<? extends NamedBean> getManager() {
        return _manager;
    }
    
    @Override
    public final Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames)
            throws JmriException {
        return getObject().getDeepCopy(systemNames, userNames);
    }

    @Override
    public final Base deepCopyChildren(Base original, Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        getObject().deepCopyChildren(original, systemNames, userNames);
        return this;
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
        
        // Always throw AbortConditionalNGExecutionException exceptions
        if (!_catchAbortExecution && (e instanceof AbortConditionalNGExecutionException)) throw e;
        
        ErrorHandlingType errorHandlingType = _errorHandlingType;
        if (errorHandlingType == ErrorHandlingType.Default) {
            errorHandlingType = InstanceManager.getDefault(LogixNGPreferences.class)
                    .getErrorHandlingType();
        }
        
        switch (errorHandlingType) {
            case ShowDialogBox:
                boolean abort = ThreadingUtil.runOnGUIwithReturn(() -> {
                    ErrorHandlingDialog dialog = new ErrorHandlingDialog();
                    return dialog.showDialog(item, message);
                });
                if (abort) throw new AbortConditionalNGExecutionException();
                break;
                
            case LogError:
                log.error("item {}, {} thrown an exception: {}", item.toString(), getObject().toString(), e, e);
                break;
                
            case LogErrorOnce:
                LoggingUtil.warnOnce(log, "item {}, {} thrown an exception: {}", item.toString(), getObject().toString(), e, e);
                break;
                
            case ThrowException:
                throw e;
                
            case AbortExecution:
                log.error("item {}, {} thrown an exception: {}", item.toString(), getObject().toString(), e, e);
                throw new AbortConditionalNGExecutionException(e);
                
            default:
                throw e;
        }
    }
    
    public void handleError(Base item, String message, RuntimeException e, Logger log) throws JmriException {
        
        ErrorHandlingType errorHandlingType = _errorHandlingType;
        if (errorHandlingType == ErrorHandlingType.Default) {
            errorHandlingType = InstanceManager.getDefault(LogixNGPreferences.class)
                    .getErrorHandlingType();
        }
        
        switch (errorHandlingType) {
            case ShowDialogBox:
                boolean abort = ThreadingUtil.runOnGUIwithReturn(() -> {
                    ErrorHandlingDialog dialog = new ErrorHandlingDialog();
                    return dialog.showDialog(item, message);
                });
                if (abort) throw new AbortConditionalNGExecutionException();
                break;
                
            case LogError:
//                e.printStackTrace();
                log.error("item {}, {} thrown an exception: {}", item.toString(), getObject().toString(), e, e);
                break;
                
            case LogErrorOnce:
//                e.printStackTrace();
                LoggingUtil.warnOnce(log, "item {}, {} thrown an exception: {}", item.toString(), getObject().toString(), e, e);
                break;
                
            case ThrowException:
                throw e;
                
            case AbortExecution:
                throw new AbortConditionalNGExecutionException(e);
                
            default:
                throw e;
        }
    }
    
}
