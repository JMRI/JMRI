package jmri.jmrit.logixng.implementation;

import java.beans.*;
import java.io.PrintWriter;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.SymbolTable.VariableData;
import jmri.jmrit.logixng.actions.*;
import jmri.jmrit.logixng.implementation.swing.ErrorHandlingDialog;
import jmri.jmrit.logixng.implementation.swing.ErrorHandlingDialog_MultiLine;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.util.LoggingUtil;
import jmri.util.ThreadingUtil;

import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

/**
 * The abstract class that is the base class for all LogixNG classes that
 * implements the Base interface.
 *
 * @author Daniel Bergqvist 2020
 */
public abstract class AbstractMaleSocket implements MaleSocket {

    private final Base _object;
    private boolean _locked = false;
    private boolean _system = false;
    protected final List<VariableData> _localVariables = new ArrayList<>();
    private final BaseManager<? extends NamedBean> _manager;
    private Base _parent;
    private ErrorHandlingType _errorHandlingType = ErrorHandlingType.Default;
    private boolean _catchAbortExecution;
    private boolean _listen = true;     // By default, actions and expressions listen


    private static class ErrorHandlingModuleClass {

        private static final ErrorHandlingModuleClass INSTANCE = new ErrorHandlingModuleClass();

        private final LogixNG errorHandlingLogixNG;
        private final ConditionalNG errorHandlingConditionalNG;
        private final Module errorHandlingModule;
        private final Map<String, Object> _variablesWithValues;

        private ErrorHandlingModuleClass() {
            errorHandlingLogixNG = new DefaultLogixNG("IQ:JMRI:ErrorHandlingLogixNG", null);
            errorHandlingConditionalNG = new DefaultConditionalNG("IQC:JMRI:ErrorHandlingCondtionalNG", null, LogixNG_Thread.ERROR_HANDLING_LOGIXNG_THREAD);
            errorHandlingLogixNG.addConditionalNG(errorHandlingConditionalNG);

            Module tempErrorHandlingModule = InstanceManager.getDefault(ModuleManager.class).getBySystemName(LogixNG_Manager.ERROR_HANDLING_MODULE_NAME);
            if (tempErrorHandlingModule != null) {
                errorHandlingModule = tempErrorHandlingModule;
            } else {
                FemaleSocketManager.SocketType socketType = InstanceManager.getDefault(
                        FemaleSocketManager.class).getSocketTypeByType("DefaultFemaleDigitalActionSocket");
                errorHandlingModule = new DefaultModule(LogixNG_Manager.ERROR_HANDLING_MODULE_NAME, null, socketType, false, false);
                InstanceManager.getDefault(ModuleManager.class).register(errorHandlingModule);
            }


            DigitalMany many = new DigitalMany("IQDA:JMRI:ErrorHandlingAction", null);
            MaleSocket maleSocketMany = new DefaultMaleDigitalActionSocket(
                    InstanceManager.getDefault(DigitalActionManager.class), many);
            many.setParent(maleSocketMany);

            maleSocketMany.addLocalVariable("logixng", SymbolTable.InitialValueType.String, null);
            maleSocketMany.addLocalVariable("conditionalng", SymbolTable.InitialValueType.String, null);
            maleSocketMany.addLocalVariable("module", SymbolTable.InitialValueType.String, null);
            maleSocketMany.addLocalVariable("item", SymbolTable.InitialValueType.String, null);
            maleSocketMany.addLocalVariable("message", SymbolTable.InitialValueType.String, null);
            maleSocketMany.addLocalVariable("messageList", SymbolTable.InitialValueType.String, null);
            maleSocketMany.addLocalVariable("exception", SymbolTable.InitialValueType.String, null);

            try {
                errorHandlingConditionalNG.getFemaleSocket().connect(maleSocketMany);
            } catch (SocketAlreadyConnectedException e) {
                log.error("Exception when creating error handling LogixNG: ", e);
            }

            SetLocalVariables setLocalVariables = new SetLocalVariables("IQDA:JMRI:ErrorHandlingAction", null);
            MaleSocket maleSocketSetLocalVariables = new DefaultMaleDigitalActionSocket(
                    InstanceManager.getDefault(DigitalActionManager.class), setLocalVariables);
            setLocalVariables.setParent(maleSocketSetLocalVariables);
            _variablesWithValues = setLocalVariables.getMap();

            try {
                maleSocketMany.getChild(maleSocketMany.getChildCount()-1).connect(maleSocketSetLocalVariables);
            } catch (SocketAlreadyConnectedException e) {
                log.error("Exception when creating error handling LogixNG: ", e);
            }

            DigitalCallModule action = new DigitalCallModule("IQDA:JMRI:ErrorHandlingAction", null);
            action.getSelectNamedBean().setNamedBean(errorHandlingModule);
            action.addParameter("__logixng__", SymbolTable.InitialValueType.LocalVariable, "logixng", Module.ReturnValueType.None, null);
            action.addParameter("__conditionalng__", SymbolTable.InitialValueType.LocalVariable, "conditionalng", Module.ReturnValueType.None, null);
            action.addParameter("__module__", SymbolTable.InitialValueType.LocalVariable, "module", Module.ReturnValueType.None, null);
            action.addParameter("__item__", SymbolTable.InitialValueType.LocalVariable, "item", Module.ReturnValueType.None, null);
            action.addParameter("__message__", SymbolTable.InitialValueType.LocalVariable, "message", Module.ReturnValueType.None, null);
            action.addParameter("__messageList__", SymbolTable.InitialValueType.LocalVariable, "messageList", Module.ReturnValueType.None, null);
            action.addParameter("__exception__", SymbolTable.InitialValueType.LocalVariable, "exception", Module.ReturnValueType.None, null);
            MaleSocket maleSocket = new DefaultMaleDigitalActionSocket(InstanceManager.getDefault(DigitalActionManager.class), action);
            action.setParent(maleSocket);
            try {
                maleSocketMany.getChild(maleSocketMany.getChildCount()-1).connect(maleSocket);
            } catch (SocketAlreadyConnectedException e) {
                log.error("Exception when creating error handling LogixNG: ", e);
            }
            List<String> errors = new ArrayList<>();
            errorHandlingLogixNG.setParentForAllChildren(errors);
            if (!errors.isEmpty()) {
                for (String s : errors) {
                    log.error("Error: {}", s);
                }
            }
        }
    }


    public AbstractMaleSocket(BaseManager<? extends NamedBean> manager, Base object) {
        _manager = manager;
        _object = object;
    }

    public static FemaleSocket getErrorHandlingModuleSocket() {
        return ErrorHandlingModuleClass.INSTANCE.errorHandlingModule.getRootSocket();
    }

    public static boolean isErrorHandlingModuleEnabled() {
        // Does the error handling module exist?
        Module errorHandlingModule = InstanceManager.getDefault(ModuleManager.class)
                .getBySystemName(LogixNG_Manager.ERROR_HANDLING_MODULE_NAME);
        if (errorHandlingModule == null) {
            return false;
        }
        return errorHandlingModule.getRootSocket().isConnected();
    }

    /** {@inheritDoc} */
    @Override
    public final Base getObject() {
        return _object;
    }

    /** {@inheritDoc} */
    @Override
    public final Base getRoot() {
        return _object.getRoot();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLocked() {
        if (_object instanceof MaleSocket) {
            return ((MaleSocket)_object).isLocked();
        }
        return _locked;
    }

    /** {@inheritDoc} */
    @Override
    public void setLocked(boolean locked) {
        if (_object instanceof MaleSocket) {
            ((MaleSocket)_object).setLocked(locked);
        }
        _locked = locked;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSystem() {
        if (_object instanceof MaleSocket) {
            return ((MaleSocket)_object).isSystem();
        }
        return _system;
    }

    /** {@inheritDoc} */
    @Override
    public void setSystem(boolean system) {
        if (_object instanceof MaleSocket) {
            ((MaleSocket)_object).setSystem(system);
        }
        _system = system;
    }

    /** {@inheritDoc} */
    @Override
    public final Category getCategory() {
        return _object.getCategory();
    }

    @Override
    public final FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        return _object.getChild(index);
    }

    @Override
    public final int getChildCount() {
        return _object.getChildCount();
    }

    @Override
    public final String getShortDescription(Locale locale) {
        return _object.getShortDescription(locale);
    }

    @Override
    public final String getLongDescription(Locale locale) {
        String s = _object.getLongDescription(locale);
        if (!_listen) {
            s += " ::: " + Base.getNoListenString();
        }
        return s;
    }

    @Override
    public final String getUserName() {
        return _object.getUserName();
    }

    @Override
    public final void setUserName(String s) throws NamedBean.BadUserNameException {
        _object.setUserName(s);
    }

    @Override
    public final String getSystemName() {
        return _object.getSystemName();
    }

    @Override
    public final void addPropertyChangeListener(PropertyChangeListener l, String name, String listenerRef) {
        _object.addPropertyChangeListener(l, name, listenerRef);
    }

    @Override
    public final void addPropertyChangeListener(String propertyName, PropertyChangeListener l, String name, String listenerRef) {
        _object.addPropertyChangeListener(propertyName, l, name, listenerRef);
    }

    @Override
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        _object.addPropertyChangeListener(l);
    }

    @Override
    public final void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
        _object.addPropertyChangeListener(propertyName, l);
    }

    @Override
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        _object.removePropertyChangeListener(l);
    }

    @Override
    public final void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
        _object.removePropertyChangeListener(propertyName, l);
    }

    @Override
    public final void updateListenerRef(PropertyChangeListener l, String newName) {
        _object.updateListenerRef(l, newName);
    }

    @Override
    public final void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        _object.vetoableChange(evt);
    }

    @Override
    public final String getListenerRef(PropertyChangeListener l) {
        return _object.getListenerRef(l);
    }

    @Override
    public final ArrayList<String> getListenerRefs() {
        return _object.getListenerRefs();
    }

    @Override
    public final int getNumPropertyChangeListeners() {
        return _object.getNumPropertyChangeListeners();
    }

    @Override
    public final synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        return _object.getPropertyChangeListeners();
    }

    @Override
    public final synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return _object.getPropertyChangeListeners(propertyName);
    }

    @Override
    public final PropertyChangeListener[] getPropertyChangeListenersByReference(String name) {
        return _object.getPropertyChangeListenersByReference(name);
    }

    @Override
    public String getComment() {
        return _object.getComment();
    }

    @Override
    public void setComment(String comment) {
        _object.setComment(comment);
    }

    @Override
    public boolean getListen() {
        if (getObject() instanceof MaleSocket) {
            return ((MaleSocket)getObject()).getListen();
        }
        return _listen;
    }

    @Override
    public void setListen(boolean listen)
    {
        if (getObject() instanceof MaleSocket) {
            ((MaleSocket)getObject()).setListen(listen);
        }
        _listen = listen;
    }

    @Override
    public boolean getCatchAbortExecution() {
        return _catchAbortExecution;
    }

    @Override
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
    public final boolean setParentForAllChildren(List<String> errors) {
        boolean result = true;
        for (int i=0; i < getChildCount(); i++) {
            FemaleSocket femaleSocket = getChild(i);
            if (femaleSocket.isConnected()) {
                MaleSocket connectedSocket = femaleSocket.getConnectedSocket();
                connectedSocket.setParent(femaleSocket);
                result = result && connectedSocket.setParentForAllChildren(errors);
            }
        }
        return result;
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
        if (getObject() instanceof MaleSocket) {
            getObject().registerListeners();
        } else {
            if (_listen) {
                registerListenersForThisClass();
                for (int i=0; i < getChildCount(); i++) {
                    getChild(i).registerListeners();
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void unregisterListeners() {
        if (getObject() instanceof MaleSocket) {
            getObject().unregisterListeners();
        } else {
            unregisterListenersForThisClass();
            for (int i=0; i < getChildCount(); i++) {
                getChild(i).unregisterListeners();
            }
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
     * @param lineNumber the line number
     */
    protected void printTreeRow(
            PrintTreeSettings settings,
            Locale locale,
            PrintWriter writer,
            String currentIndent,
            MutableInt lineNumber) {

        if (!(getObject() instanceof AbstractMaleSocket)) {
            String comment = getComment();
            if (comment != null) {
                comment = comment.replaceAll("\\r\\n", "\\n");
                comment = comment.replaceAll("\\r", "\\n");
                for (String s : comment.split("\\n", 0)) {
                    if (settings._printLineNumbers) {
                        writer.append(String.format(PRINT_LINE_NUMBERS_FORMAT, lineNumber.addAndGet(1)));
                    }
                    writer.append(currentIndent);
                    writer.append("// ");
                    writer.append(s);
                    writer.println();
                }
            }
            if (settings._printLineNumbers) {
                writer.append(String.format(PRINT_LINE_NUMBERS_FORMAT, lineNumber.addAndGet(1)));
            }
            writer.append(currentIndent);
            writer.append(getLongDescription(locale));
            if (settings._printSystemNames) {
                writer.append(" ::: ");
                writer.append(this.getSystemName());
            }
            if (settings._printDisplayName) {
                writer.append(" ::: ");
                writer.append(Bundle.getMessage("LabelDisplayName"));
                writer.append(" ");
                writer.append(((NamedBean)this).getDisplayName(
                        NamedBean.DisplayOptions.USERNAME_SYSTEMNAME));
            } else if (!settings._hideUserName && getUserName() != null) {
                writer.append(" ::: ");
                writer.append(Bundle.getMessage("LabelUserName"));
                writer.append(" ");
                writer.append(getUserName());
            }

            if (settings._printErrorHandling) {
                writer.append(" ::: ");
                writer.append(getErrorHandlingType().toString());
            }
            if (!isEnabled()) {
                writer.append(" ::: ");
                writer.append(Bundle.getMessage("AbstractMaleSocket_Disabled"));
            }
            if (isLocked()) {
                writer.append(" ::: ");
                writer.append(Bundle.getMessage("AbstractMaleSocket_Locked"));
            }
            if (isSystem()) {
                writer.append(" ::: ");
                writer.append(Bundle.getMessage("AbstractMaleSocket_System"));
            }
            writer.println();
        }
    }

    protected void printLocalVariable(
            PrintTreeSettings settings,
            Locale locale,
            PrintWriter writer,
            String currentIndent,
            MutableInt lineNumber,
            VariableData localVariable) {

        if (settings._printLineNumbers) {
            writer.append(String.format(PRINT_LINE_NUMBERS_FORMAT, lineNumber.addAndGet(1)));
        }
        writer.append(currentIndent);
        writer.append("   ::: ");
        writer.append(Bundle.getMessage(
                locale,
                "PrintLocalVariable",
                localVariable._name,
                localVariable._initialValueType.toString(),
                localVariable._initialValueData));
        writer.println();
    }

    /** {@inheritDoc} */
    @Override
    public void printTree(
            PrintTreeSettings settings,
            PrintWriter writer,
            String indent,
            MutableInt lineNumber) {
        printTree(settings, Locale.getDefault(), writer, indent, "", lineNumber);
    }

    /** {@inheritDoc} */
    @Override
    public void printTree(
            PrintTreeSettings settings,
            Locale locale,
            PrintWriter writer,
            String indent,
            MutableInt lineNumber) {
        printTree(settings, locale, writer, indent, "", lineNumber);
    }

    /** {@inheritDoc} */
    @Override
    public void printTree(
            PrintTreeSettings settings,
            Locale locale,
            PrintWriter writer,
            String indent,
            String currentIndent,
            MutableInt lineNumber) {

        printTreeRow(settings, locale, writer, currentIndent, lineNumber);

        if (settings._printLocalVariables) {
            for (VariableData localVariable : _localVariables) {
                printLocalVariable(settings, locale, writer, currentIndent, lineNumber, localVariable);
            }
        }

        if (getObject() instanceof MaleSocket) {
            getObject().printTree(settings, locale, writer, indent, currentIndent, lineNumber);
        } else {
            for (int i=0; i < getChildCount(); i++) {
                getChild(i).printTree(settings, locale, writer, indent, currentIndent+indent, lineNumber);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value="SLF4J_SIGN_ONLY_FORMAT",
                                                        justification="Specific log message format")
    public void getUsageTree(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        if (!(getObject() instanceof AbstractMaleSocket)) {
            log.debug("*@ {} :: {}", level, this.getLongDescription());
            _object.getUsageDetail(level, bean, report, cdl);
        }

        if (getObject() instanceof MaleSocket) {
            getObject().getUsageTree(level, bean, report, cdl);
        } else {
            level++;
            for (int i=0; i < getChildCount(); i++) {
                getChild(i).getUsageTree(level, bean, report, cdl);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<jmri.NamedBeanUsageReport> report, NamedBean cdl) {
    }

    @Override
    public BaseManager<? extends NamedBean> getManager() {
        return _manager;
    }

    @Override
    public final Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames)
            throws JmriException {

        MaleSocket maleSocket = (MaleSocket)getObject().getDeepCopy(systemNames, userNames);

        maleSocket.setComment(this.getComment());
        if (maleSocket.getDebugConfig() != null) {
            maleSocket.setDebugConfig(maleSocket.getDebugConfig().getCopy());
        }
        maleSocket.setEnabledFlag(isEnabled());
        maleSocket.setListen(getListen());
        maleSocket.setErrorHandlingType(getErrorHandlingType());
        maleSocket.setLocked(isLocked());
        maleSocket.setSystem(false);    // If a system item is copied, the new item is not treated as system
        maleSocket.setCatchAbortExecution(getCatchAbortExecution());

        for (VariableData data : _localVariables) {
            maleSocket.addLocalVariable(data._name, data._initialValueType, data._initialValueData);
        }

        return maleSocket;
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
        if (getObject() instanceof MaleSocket) {
            return ((MaleSocket)getObject()).getErrorHandlingType();
        } else {
            return _errorHandlingType;
        }
    }

    @Override
    public void setErrorHandlingType(ErrorHandlingType errorHandlingType)
    {
        if (getObject() instanceof MaleSocket) {
            ((MaleSocket)getObject()).setErrorHandlingType(errorHandlingType);
        } else {
            _errorHandlingType = errorHandlingType;
        }
    }

    /**
     * Executes the error handling module.
     * @param  item           the item that had the error
     * @param  message        the error message
     * @param  messageList    a list of error messages
     * @param  e              the exception that has happened
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"SLF4J_SIGN_ONLY_FORMAT"})
            // justification="The message is on several lines")
    public void executeErrorHandlingModule(
            Base item,
            String message,
            List<String> messageList,
            Exception e) {

        // Don't call the error handling module if it doesnt exist.
        Module errorHandlingModule = InstanceManager.getDefault(ModuleManager.class)
                .getBySystemName(LogixNG_Manager.ERROR_HANDLING_MODULE_NAME);
        if (errorHandlingModule == null) {
            return;
        }

        // Don't call the error handling module if it hasn't any children.
        if (!ErrorHandlingModuleClass.INSTANCE.errorHandlingModule.getRootSocket().isConnected()) {
            return;
        }

        // Don't call the error handling module recursively. It would happen
        // if there is an error in the error handling module.
        if (item.getModule() == ErrorHandlingModuleClass.INSTANCE.errorHandlingModule) {
            log.warn("Exception in LogixNG error handling module. Can't execute error handling module recursively.");
            if (messageList != null) {
                for (String s : messageList) {
                    log.warn("   {}", s);
                }
            } else {
                log.warn("   {}", message);
            }
            log.warn("Exception: ", e);
        }

        ErrorHandlingModuleClass.INSTANCE._variablesWithValues.put("logixng", item.getLogixNG());
        ErrorHandlingModuleClass.INSTANCE._variablesWithValues.put("conditionalng", item.getConditionalNG());
        ErrorHandlingModuleClass.INSTANCE._variablesWithValues.put("module", item.getModule());
        ErrorHandlingModuleClass.INSTANCE._variablesWithValues.put("item", item);
        ErrorHandlingModuleClass.INSTANCE._variablesWithValues.put("message", message);
        ErrorHandlingModuleClass.INSTANCE._variablesWithValues.put("messageList", messageList);
        ErrorHandlingModuleClass.INSTANCE._variablesWithValues.put("exception", e);
        ErrorHandlingModuleClass.INSTANCE.errorHandlingConditionalNG.execute();
    }

    @Override
    public void handleError(Base item, String message, JmriException e, Logger log) throws JmriException {

        // Always throw AbortConditionalNGExecutionException exceptions
        if (!_catchAbortExecution && (e instanceof AbortConditionalNGExecutionException)) throw e;

        ErrorHandlingType errorHandlingType = getErrorHandlingType();
        if (errorHandlingType == ErrorHandlingType.Default) {
            errorHandlingType = InstanceManager.getDefault(LogixNGPreferences.class)
                    .getErrorHandlingType();
        }

        executeErrorHandlingModule(item, message, null, e);

        switch (errorHandlingType) {
            case ShowDialogBox:
                boolean abort = ThreadingUtil.runOnGUIwithReturn(() -> {
                    ErrorHandlingDialog dialog = new ErrorHandlingDialog();
                    return dialog.showDialog(item, message);
                });
                if (abort) throw new AbortConditionalNGExecutionException(this, e);
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
                throw new AbortConditionalNGExecutionException(this, e);

            case AbortWithoutError:
                throw new AbortConditionalNG_IgnoreException(this, e);

            default:
                throw e;
        }
    }

    @Override
    public void handleError(
            Base item,
            String message,
            List<String> messageList,
            JmriException e,
            Logger log)
            throws JmriException {

        ErrorHandlingType errorHandlingType = getErrorHandlingType();
        if (errorHandlingType == ErrorHandlingType.Default) {
            errorHandlingType = InstanceManager.getDefault(LogixNGPreferences.class)
                    .getErrorHandlingType();
        }

        executeErrorHandlingModule(item, message, messageList, e);

        switch (errorHandlingType) {
            case ShowDialogBox:
                boolean abort = ThreadingUtil.runOnGUIwithReturn(() -> {
                    ErrorHandlingDialog_MultiLine dialog = new ErrorHandlingDialog_MultiLine();
                    return dialog.showDialog(item, message, messageList);
                });
                if (abort) throw new AbortConditionalNGExecutionException(this, e);
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
                throw new AbortConditionalNGExecutionException(this, e);

            case AbortWithoutError:
                throw new AbortConditionalNG_IgnoreException(this, e);

            default:
                throw e;
        }
    }

    @Override
    public void handleError(Base item, String message, RuntimeException e, Logger log) throws JmriException {

        ErrorHandlingType errorHandlingType = getErrorHandlingType();
        if (errorHandlingType == ErrorHandlingType.Default) {
            errorHandlingType = InstanceManager.getDefault(LogixNGPreferences.class)
                    .getErrorHandlingType();
        }

        executeErrorHandlingModule(item, message, null, e);

        switch (errorHandlingType) {
            case ShowDialogBox:
                boolean abort = ThreadingUtil.runOnGUIwithReturn(() -> {
                    ErrorHandlingDialog dialog = new ErrorHandlingDialog();
                    return dialog.showDialog(item, message);
                });
                if (abort) throw new AbortConditionalNGExecutionException(this, e);
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
                throw new AbortConditionalNGExecutionException(this, e);

            case AbortWithoutError:
                throw new AbortConditionalNG_IgnoreException(this, e);

            default:
                throw e;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void getListenerRefsIncludingChildren(List<String> list) {
        list.addAll(getListenerRefs());
        for (int i=0; i < getChildCount(); i++) {
            getChild(i).getListenerRefsIncludingChildren(list);
        }
    }

    @Override
    public boolean hasChild(@Nonnull Base b) {
        return getObject() == b;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getObject().toString();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractMaleSocket.class);
}
