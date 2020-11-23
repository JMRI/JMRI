package jmri.jmrit.logixng.implementation;

import static jmri.NamedBean.UNKNOWN;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.Module.Parameter;
import jmri.jmrit.logixng.Module.ParameterData;
import jmri.jmrit.logixng.ModuleManager;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable.DefaultVariableData;

/**
 * The default implementation of LogixNG.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class DefaultModule extends AbstractBase
        implements Module, FemaleSocketListener {
    
    
    private FemaleSocketManager.SocketType _rootSocketType;
    private FemaleSocket _femaleRootSocket;
    private String _socketSystemName = null;
    private final Map<String, Parameter> _parameters = new HashMap<>();
    private final Map<String, ParameterData> _localVariables = new HashMap<>();
    
    
    public DefaultModule(String sys, String user) throws BadUserNameException, BadSystemNameException  {
        super(sys, user);
        
        // Do this test here to ensure all the tests are using correct system names
        Manager.NameValidity isNameValid = InstanceManager.getDefault(ModuleManager.class).validSystemNameFormat(mSystemName);
        if (isNameValid != Manager.NameValidity.VALID) {
            throw new IllegalArgumentException("system name is not valid");
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Base getParent() {
        return null;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setParent(Base parent) {
        throw new UnsupportedOperationException("A Module cannot have a parent");
    }
    
    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameModule");
    }

    @Override
    public void setState(int s) throws JmriException {
        log.warn("Unexpected call to setState in DefaultModule.");  // NOI18N
    }

    @Override
    public int getState() {
        log.warn("Unexpected call to getState in DefaultModule.");  // NOI18N
        return UNKNOWN;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return "Module";
    }

    @Override
    public String getLongDescription(Locale locale) {
        return "Module: "+getDisplayName();
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        if (index != 0) {
            throw new IllegalArgumentException(
                    String.format("index has invalid value: %d", index));
        }
        
        return _femaleRootSocket;
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    @Override
    public Category getCategory() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean isExternal() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Lock getLock() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setLock(Lock lock) {
        throw new UnsupportedOperationException("Not supported.");
    }
/*
    protected void printTreeRow(Locale locale, PrintWriter writer, String currentIndent) {
        writer.append(currentIndent);
        writer.append(getLongDescription(locale));
        writer.println();
    }
*/    
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
    
    /** {@inheritDoc} */
    @Override
    public void printTree(Locale locale, PrintWriter writer, String indent, String currentIndent) {
        printTreeRow(locale, writer, currentIndent);

        _femaleRootSocket.printTree(locale, writer, indent, currentIndent+indent);
    }
    
    @Override
    public void setRootSocketType(FemaleSocketManager.SocketType socketType) {
        if ((_femaleRootSocket != null) && _femaleRootSocket.isConnected()) throw new RuntimeException("Cannot set root socket when it's connected");
        
        _rootSocketType = socketType;
        _femaleRootSocket = socketType.createSocket(this, this, mSystemName);
        
        // Listeners should never be enabled for a module
        _femaleRootSocket.setEnableListeners(false);
    }
    
    @Override
    public FemaleSocketManager.SocketType getRootSocketType() {
        return _rootSocketType;
    }
    
    @Override
    public FemaleSocket getRootSocket() {
        return _femaleRootSocket;
    }
    
    @Override
    public void addParameter(String name, boolean isInput, boolean isOutput) {
        _parameters.put(name, new DefaultSymbolTable.DefaultParameter(name, isInput, isOutput));
    }
    
    @Override
    public void removeParameter(String name) {
        _parameters.remove(name);
    }
    
    @Override
    public void addLocalVariable(
            String name,
            SymbolTable.InitialValueType initialValueType,
            String initialValueData) {
        
        _localVariables.put(name,
                new DefaultParameterData(
                        name,
                        initialValueType,
                        initialValueData,
                        ReturnValueType.None,
                        null));
    }
    
    @Override
    public void removeLocalVariable(String name) {
        _localVariables.remove(name);
    }
    
    @Override
    public Collection<Parameter> getParameters() {
        return _parameters.values();
    }
    
    @Override
    public Collection<ParameterData> getLocalVariables() {
        return _localVariables.values();
    }
    
    @Override
    public void connected(FemaleSocket socket) {
        _socketSystemName = socket.getConnectedSocket().getSystemName();
    }
    
    @Override
    public void disconnected(FemaleSocket socket) {
        _socketSystemName = null;
    }
    
    public void setSocketSystemName(String systemName) {
        if ((systemName == null) || (!systemName.equals(_socketSystemName))) {
            _femaleRootSocket.disconnect();
        }
        _socketSystemName = systemName;
    }
    
    public String getSocketSystemName() {
        return _socketSystemName;
    }
    
    /** {@inheritDoc} */
    @Override
    final public void setup() {
        if (!_femaleRootSocket.isConnected()
                || !_femaleRootSocket.getConnectedSocket().getSystemName()
                        .equals(_socketSystemName)) {
            
            _femaleRootSocket.disconnect();
            
            if (_socketSystemName != null) {
                try {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(_socketSystemName);
                    if (maleSocket != null) {
                        _femaleRootSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("digital action is not found: " + _socketSystemName);
                    }
                } catch (SocketAlreadyConnectedException ex) {
                    // This shouldn't happen and is a runtime error if it does.
                    throw new RuntimeException("socket is already connected");
                }
            }
        } else {
            _femaleRootSocket.setup();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    final public void disposeMe() {
        _femaleRootSocket.dispose();
    }

    @Override
    protected void registerListenersForThisClass() {
        // Do nothing. A module never listen on anything.
    }

    @Override
    protected void unregisterListenersForThisClass() {
        // Do nothing. A module never listen on anything.
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public static class DefaultParameterData extends DefaultVariableData implements ParameterData {
        
        public ReturnValueType _returnValueType;
        public String _returnValueData;
        
        public DefaultParameterData(
                String name,
                InitialValueType initalValueType,
                String initialValueData,
                ReturnValueType returnValueType,
                String returnValueData) {
            
            super(name, initalValueType, initialValueData);
            
            _returnValueType = returnValueType;
            _returnValueData = returnValueData;
        }
        
        /** {@inheritDoc} */
        @Override
        public ReturnValueType getReturnValueType() {
            return _returnValueType;
        }
        
        /** {@inheritDoc} */
        @Override
        public String getReturnValueData() {
            return _returnValueData;
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultModule.class);
    
}
