package jmri.jmrit.logixng.implementation;

import static jmri.NamedBean.UNKNOWN;

import java.io.PrintWriter;
import java.util.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.NamedBean;
import jmri.NamedBeanUsageReport;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.Module.Parameter;
import jmri.jmrit.logixng.ModuleManager;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.SymbolTable.VariableData;

/**
 * The default implementation of Module.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public class DefaultModule extends AbstractBase
        implements Module, FemaleSocketListener {


    private final FemaleSocketManager.SocketType _rootSocketType;
    private final FemaleSocket _femaleRootSocket;
    private String _socketSystemName = null;
    private final List<Parameter> _parameters = new ArrayList<>();
    private final List<VariableData> _localVariables = new ArrayList<>();
    private Lock _lock = Lock.NONE;
    private final Map<Thread, ConditionalNG> _currentConditionalNG = new HashMap<>();


    public DefaultModule(String sys, String user, FemaleSocketManager.SocketType socketType)
            throws BadUserNameException, BadSystemNameException  {

        super(sys, user);

        _rootSocketType = socketType;
        _femaleRootSocket = socketType.createSocket(this, this, "Root");

        // Listeners should never be enabled for a module
        _femaleRootSocket.setEnableListeners(false);

        // Do this test here to ensure all the tests are using correct system names
        Manager.NameValidity isNameValid = InstanceManager.getDefault(ModuleManager.class).validSystemNameFormat(mSystemName);
        if (isNameValid != Manager.NameValidity.VALID) {
            throw new IllegalArgumentException("system name is not valid");
        }
    }

    @Override
    public void setCurrentConditionalNG(ConditionalNG conditionalNG) {
        synchronized(this) {
            _currentConditionalNG.put(Thread.currentThread(), conditionalNG);
        }
    }

    @Override
    public ConditionalNG getConditionalNG() {
        synchronized(this) {
            return _currentConditionalNG.get(Thread.currentThread());
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
        return Bundle.getMessage("DefaultModule_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        StringBuilder sb = new StringBuilder(Bundle.getMessage("DefaultModule_Long", getDisplayName()));
        if (! _parameters.isEmpty()) {
            List<String> inParams = new ArrayList<>();
            List<String> outParams = new ArrayList<>();
            List<String> inOutParams = new ArrayList<>();

            for (Parameter p : _parameters) {
                if (p.isInput() && p.isOutput()) inOutParams.add(p.getName());
                else if (p.isInput()) inParams.add(p.getName());
                else if (p.isOutput()) outParams.add(p.getName());
                else throw new RuntimeException("Parameter "+p.getName()+" is neither in or out");
            }
            sb.append(" ::: ");

            boolean addComma = false;
            for (int i=0; i < inParams.size(); i++) {
                if (i==0) {
                    sb.append(Bundle.getMessage("DefaultModuleParamInput"));
                    addComma = true;
                }
                else sb.append(", ");
                sb.append(inParams.get(i));
            }

            if (addComma) sb.append(", ");
            addComma = false;

            for (int i=0; i < outParams.size(); i++) {
                if (i==0) {
                    sb.append(Bundle.getMessage("DefaultModuleParamOuput"));
                    addComma = true;
                }
                else sb.append(", ");
                sb.append(outParams.get(i));
            }

            if (addComma) sb.append(", ");

            for (int i=0; i < inOutParams.size(); i++) {
                if (i==0) sb.append(Bundle.getMessage("DefaultModuleParamInputOutput"));
                else sb.append(", ");
                sb.append(inOutParams.get(i));
            }
        }
        return sb.toString();
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
        return Category.OTHER;
    }

    @Override
    public boolean isExternal() {
        return false;
    }

    @Override
    public Lock getLock() {
        return _lock;
    }

    @Override
    public void setLock(Lock lock) {
        _lock = lock;
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
    public void printTree(PrintTreeSettings settings, PrintWriter writer, String indent) {
        printTree(settings, Locale.getDefault(), writer, indent, "");
    }

    /** {@inheritDoc} */
    @Override
    public void printTree(PrintTreeSettings settings, Locale locale, PrintWriter writer, String indent) {
        printTree(settings, locale, writer, indent, "");
    }

    /** {@inheritDoc} */
    @Override
    public void printTree(PrintTreeSettings settings, Locale locale, PrintWriter writer, String indent, String currentIndent) {
        printTreeRow(locale, writer, currentIndent);

        _femaleRootSocket.printTree(settings, locale, writer, indent, currentIndent+indent);
    }
/*
    @Override
    public void setRootSocketType(FemaleSocketManager.SocketType socketType) {
        if ((_femaleRootSocket != null) && _femaleRootSocket.isConnected()) throw new RuntimeException("Cannot set root socket when it's connected");

        _rootSocketType = socketType;
        _femaleRootSocket = socketType.createSocket(this, this, "Root");

        // Listeners should never be enabled for a module
        _femaleRootSocket.setEnableListeners(false);
    }
*/
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
        _parameters.add(new DefaultSymbolTable.DefaultParameter(name, isInput, isOutput));
    }

    @Override
    public void addParameter(Parameter parameter) {
        _parameters.add(parameter);
    }

//    @Override
//    public void removeParameter(String name) {
//        _parameters.remove(name);
//    }

    @Override
    public void addLocalVariable(
            String name,
            InitialValueType initialValueType,
            String initialValueData) {

        _localVariables.add(new VariableData(
                name,
                initialValueType,
                initialValueData));
    }

//    @Override
//    public void removeLocalVariable(String name) {
//        _localVariables.remove(name);
//    }

    @Override
    public List<Parameter> getParameters() {
        return _parameters;
    }

    @Override
    public List<VariableData> getLocalVariables() {
        return _localVariables;
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

    /** {@inheritDoc} */
    @Override
    public List<NamedBeanUsageReport> getUsageReport(NamedBean bean) {
        List<NamedBeanUsageReport> report = new ArrayList<>();
        if (bean != null) {
            getUsageTree(0, bean, report, null);
        }
        return report;
    }

    /** {@inheritDoc} */
    @Override
    public void getUsageTree(int level, NamedBean bean, List<jmri.NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("** {} :: {}", level, this.getClass().getName());
        level++;
        _femaleRootSocket.getUsageTree(level, bean, report, cdl);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultModule.class);

}
