package jmri.jmrit.logixng.implementation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.beans.*;
import java.io.PrintWriter;
import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.managers.AbstractManager;
import jmri.util.LoggingUtil;
import jmri.util.ThreadingUtil;

/**
 * Class providing the basic logic of the GlobalVariable_Manager interface.
 *
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2022
 */
public class DefaultGlobalVariableManager extends AbstractManager<GlobalVariable>
        implements GlobalVariableManager {


    public DefaultGlobalVariableManager() {
        // The GlobalVariablePreferences class may load plugins so we must ensure
        // it's loaded here.
        InstanceManager.getDefault(LogixNGPreferences.class);
    }

    @Override
    public int getXMLOrder() {
        return LOGIXNG_GLOBAL_VARIABLES;
    }

    @Override
    public char typeLetter() {
        return 'Q';
    }

    /**
     * Test if parameter is a properly formatted system name.
     *
     * @param systemName the system name
     * @return enum indicating current validity, which might be just as a prefix
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return LogixNG_Manager.validSystemNameFormat(
                getSubSystemNamePrefix(), systemName);
    }

    /**
     * Method to create a new GlobalVariable if the GlobalVariable does not exist.
     * <p>
     * Returns null if a GlobalVariable with the same systemName or userName
     * already exists, or if there is trouble creating a new GlobalVariable.
     */
    @Override
    public GlobalVariable createGlobalVariable(String systemName, String userName)
            throws IllegalArgumentException {

        // Check that GlobalVariable does not already exist
        GlobalVariable x;
        if (userName != null && !userName.isEmpty()) {
            x = getByUserName(userName);
            if (x != null) {
                return null;
            }
        }
        x = getBySystemName(systemName);
        if (x != null) {
            return null;
        }
        // Check if system name is valid
        if (this.validSystemNameFormat(systemName) != NameValidity.VALID) {
            throw new IllegalArgumentException("SystemName " + systemName + " is not in the correct format");
        }
        // GlobalVariable does not exist, create a new GlobalVariable
        x = new DefaultGlobalVariable(systemName, userName);
        // save in the maps
        register(x);

        // Keep track of the last created auto system name
        updateAutoNumber(systemName);

        return x;
    }

    @Override
    public GlobalVariable createGlobalVariable(String userName) throws IllegalArgumentException {
        return createGlobalVariable(getAutoSystemName(), userName);
    }

    @Override
    public GlobalVariable getGlobalVariable(String name) {
        GlobalVariable x = getByUserName(name);
        if (x != null) {
            return x;
        }
        return getBySystemName(name);
    }

    @Override
    public GlobalVariable getByUserName(String name) {
        return _tuser.get(name);
    }

    @Override
    public GlobalVariable getBySystemName(String name) {
        return _tsys.get(name);
    }

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameGlobalVariables" : "BeanNameGlobalVariable");
    }

    /** {@inheritDoc} */
    @Override
    public void deleteGlobalVariable(GlobalVariable x) {
        // delete the GlobalVariable
        deregister(x);
        x.dispose();
    }

    /** {@inheritDoc} */
    @Override
    public void printTree(Locale locale, PrintWriter writer, String indent) {
        for (GlobalVariable globalVariable : getNamedBeanSet()) {
            writer.append(String.format(
                    "Global variable: System name: %s, User name: %s, Initial value type: %s, Initial value data: %s",
                    globalVariable.getSystemName(), globalVariable.getUserName(),
                    globalVariable.getInitialValueType().toString(), globalVariable.getInitialValueData()));
            writer.println();
        }
        writer.println();
    }

    static volatile DefaultGlobalVariableManager _instance = null;

    @InvokeOnGuiThread  // this method is not thread safe
    public static DefaultGlobalVariableManager instance() {
        if (!ThreadingUtil.isGUIThread()) {
            LoggingUtil.warnOnce(log, "instance() called on wrong thread");
        }

        if (_instance == null) {
            _instance = new DefaultGlobalVariableManager();
        }
        return (_instance);
    }

    /** {@inheritDoc} */
    @Override
    public Class<GlobalVariable> getNamedBeanClass() {
        return GlobalVariable.class;
    }

    /**
     * Inform all registered listeners of a vetoable change.If the propertyName
     * is "CanDelete" ALL listeners with an interest in the bean will throw an
     * exception, which is recorded returned back to the invoking method, so
     * that it can be presented back to the user.However if a listener decides
     * that the bean can not be deleted then it should throw an exception with
     * a property name of "DoNotDelete", this is thrown back up to the user and
     * the delete process should be aborted.
     *
     * @param p   The programmatic name of the property that is to be changed.
     *            "CanDelete" will inquire with all listeners if the item can
     *            be deleted. "DoDelete" tells the listener to delete the item.
     * @param old The old value of the property.
     * @throws java.beans.PropertyVetoException If the recipients wishes the
     *                                          delete to be aborted (see above)
     */
    @OverridingMethodsMustInvokeSuper
    public void fireVetoableChange(String p, Object old) throws PropertyVetoException {
        PropertyChangeEvent evt = new PropertyChangeEvent(this, p, old, null);
        for (VetoableChangeListener vc : vetoableChangeSupport.getVetoableChangeListeners()) {
            vc.vetoableChange(evt);
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressFBWarnings(value = "OVERRIDING_METHODS_MUST_INVOKE_SUPER",
            justification = "Further investigation is needed to handle this correctly")
    public final void deleteBean(@Nonnull GlobalVariable globalVariable, @Nonnull String property) throws PropertyVetoException {
        // throws PropertyVetoException if vetoed
        fireVetoableChange(property, globalVariable);
        if ( PROPERTY_DO_DELETE.equals(property)) {
            deregister(globalVariable);
            globalVariable.dispose();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultGlobalVariableManager.class);

}
