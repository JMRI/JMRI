package jmri.jmrit.logixng.implementation;

import java.awt.GraphicsEnvironment;
import java.beans.*;
import java.io.PrintWriter;
import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.swing.JOptionPane;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Base.PrintTreeSettings;
import jmri.jmrit.logixng.Module;
import jmri.managers.AbstractManager;
import jmri.util.LoggingUtil;
import jmri.util.ThreadingUtil;

import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Class providing the basic logic of the LogixNG_Manager interface.
 *
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public class DefaultLogixNGManager extends AbstractManager<LogixNG>
        implements LogixNG_Manager {


    private final Map<String, Manager<? extends MaleSocket>> _managers = new HashMap<>();
    private final Clipboard _clipboard = new DefaultClipboard();
    private boolean _isActive = false;


    public DefaultLogixNGManager() {
        // The LogixNGPreferences class may load plugins so we must ensure
        // it's loaded here.
        InstanceManager.getDefault(LogixNGPreferences.class);
    }

    @Override
    public int getXMLOrder() {
        return LOGIXNGS;
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
//        if (systemName.matches(getSubSystemNamePrefix()+"(:AUTO:)?\\d+")) {
//            return NameValidity.VALID;
//        } else {
//            return NameValidity.INVALID;
//        }
    }

    /**
     * Method to create a new LogixNG if the LogixNG does not exist.
     * <p>
     * Returns null if
     * a Logix with the same systemName or userName already exists, or if there
     * is trouble creating a new LogixNG.
     */
    @Override
    public LogixNG createLogixNG(String systemName, String userName)
            throws IllegalArgumentException {

        // Check that LogixNG does not already exist
        LogixNG x;
        if (userName != null && !userName.equals("")) {
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
        // LogixNG does not exist, create a new LogixNG
        x = new DefaultLogixNG(systemName, userName);
        // save in the maps
        register(x);

        // Keep track of the last created auto system name
        updateAutoNumber(systemName);

        return x;
    }

    @Override
    public LogixNG createLogixNG(String userName) throws IllegalArgumentException {
        return createLogixNG(getAutoSystemName(), userName);
    }

    @Override
    public LogixNG getLogixNG(String name) {
        LogixNG x = getByUserName(name);
        if (x != null) {
            return x;
        }
        return getBySystemName(name);
    }

    @Override
    public LogixNG getByUserName(String name) {
        return _tuser.get(name);
    }

    @Override
    public LogixNG getBySystemName(String name) {
        return _tsys.get(name);
    }

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameLogixNGs" : "BeanNameLogixNG");
    }

    /** {@inheritDoc} */
    @Override
    public void setupAllLogixNGs() {
        List<String> errors = new ArrayList<>();
        boolean result = true;
        for (LogixNG logixNG : _tsys.values()) {
            logixNG.setup();
            result = result && logixNG.setParentForAllChildren(errors);
        }
        for (Module module : InstanceManager.getDefault(ModuleManager.class).getNamedBeanSet()) {
            module.setup();
            result = result && module.setParentForAllChildren(errors);
        }
        _clipboard.setup();
        if (errors.size() > 0) {
            messageDialog("SetupErrorsTitle", errors, null);
        }
        checkItemsHaveParents();
    }

    /**
     * Display LogixNG setup errors when not running in headless mode.
     * @param titleKey The bundle key for the dialog title.
     * @param messages A ArrayList of messages that have been localized.
     * @param helpKey The bundle key for additional information about the errors
     */
    private void messageDialog(String titleKey, List<String> messages, String helpKey) {
        if (!GraphicsEnvironment.isHeadless() && !Boolean.getBoolean("jmri.test.no-dialogs")) {
            StringBuilder sb = new StringBuilder("<html>");
            messages.forEach(msg -> {
                sb.append(msg);
                sb.append("<br>");
            });
            if (helpKey != null) {
                sb.append("<br>");
                sb.append(Bundle.getMessage(helpKey));
            }
            sb.append("/<html>");
            JOptionPane.showMessageDialog(null,
                    sb.toString(),
                    Bundle.getMessage(titleKey),
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void checkItemsHaveParents(SortedSet<? extends MaleSocket> set, List<MaleSocket> beansWithoutParentList) {
        for (MaleSocket bean : set) {
            if (((Base)bean).getParent() == null) beansWithoutParentList.add(bean);
        }
    }

    private void checkItemsHaveParents() {
        List<MaleSocket> beansWithoutParentList = new ArrayList<>();
        checkItemsHaveParents(InstanceManager.getDefault(AnalogActionManager.class).getNamedBeanSet(), beansWithoutParentList);
        checkItemsHaveParents(InstanceManager.getDefault(DigitalActionManager.class).getNamedBeanSet(), beansWithoutParentList);
        checkItemsHaveParents(InstanceManager.getDefault(DigitalBooleanActionManager.class).getNamedBeanSet(), beansWithoutParentList);
        checkItemsHaveParents(InstanceManager.getDefault(StringActionManager.class).getNamedBeanSet(), beansWithoutParentList);
        checkItemsHaveParents(InstanceManager.getDefault(AnalogExpressionManager.class).getNamedBeanSet(), beansWithoutParentList);
        checkItemsHaveParents(InstanceManager.getDefault(DigitalExpressionManager.class).getNamedBeanSet(), beansWithoutParentList);
        checkItemsHaveParents(InstanceManager.getDefault(StringExpressionManager.class).getNamedBeanSet(), beansWithoutParentList);

        if (!beansWithoutParentList.isEmpty()) {
            List<String> errors = new ArrayList<>();
            List<String> msgs = new ArrayList<>();
            for (Base b : beansWithoutParentList) {
                b.setup();
                b.setParentForAllChildren(errors);
            }
            for (Base b : beansWithoutParentList) {
                if (b.getParent() == null) {
                    log.error("Item has no parent: {}, {}, {}",
                            b.getSystemName(),
                            b.getUserName(),
                            b.getLongDescription());
                    msgs.add(Bundle.getMessage("NoParentMessage",
                            b.getSystemName(),
                            b.getUserName(),
                            b.getLongDescription()));

                    for (int i=0; i < b.getChildCount(); i++) {
                        if (b.getChild(i).isConnected()) {
                            log.error("    Child: {}, {}, {}",
                                    b.getChild(i).getConnectedSocket().getSystemName(),
                                    b.getChild(i).getConnectedSocket().getUserName(),
                                    b.getChild(i).getConnectedSocket().getLongDescription());
                        }
                    }
                    log.error("                                                                 ");
                    List<String> cliperrors = new ArrayList<String>();
                    _clipboard.add((MaleSocket) b, cliperrors);
                }
            }
            messageDialog("ParentErrorsTitle", msgs, "NoParentHelp");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void activateAllLogixNGs() {
        activateAllLogixNGs(true, true);
    }

    /** {@inheritDoc} */
    @Override
    public void activateAllLogixNGs(boolean runDelayed, boolean runOnSeparateThread) {

        _isActive = true;

        // This may take a long time so it must not be done on the GUI thread.
        // Therefore we create a new thread for this task.
        Runnable runnable = () -> {
            Set<LogixNG> activeLogixNGs = new HashSet<>();

            // Activate and execute the initialization LogixNGs first.
            List<LogixNG> initLogixNGs =
                    InstanceManager.getDefault(LogixNG_InitializationManager.class)
                            .getList();

            for (LogixNG logixNG : initLogixNGs) {
                if (logixNG.isActive()) {
                    logixNG.registerListeners();
                    logixNG.execute(false);
                    activeLogixNGs.add(logixNG);
                } else {
                    logixNG.unregisterListeners();
                }
            }

            // Activate and execute all the rest of the LogixNGs.
            _tsys.values().stream()
                    .sorted()
                    .filter((logixNG) -> !(activeLogixNGs.contains(logixNG)))
                    .forEachOrdered((logixNG) -> {

                if (logixNG.isActive()) {
                    logixNG.registerListeners();
                    logixNG.execute();
                } else {
                    logixNG.unregisterListeners();
                }
            });
        };

        if (runOnSeparateThread) new Thread(runnable).start();
        else runnable.run();
    }

    /** {@inheritDoc} */
    @Override
    public void deActivateAllLogixNGs() {
        for (LogixNG logixNG : _tsys.values()) {
            logixNG.unregisterListeners();
        }
        _isActive = false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isActive() {
        return _isActive;
    }

    /** {@inheritDoc} */
    @Override
    public void deleteLogixNG(LogixNG x) {
        // delete the LogixNG
        deregister(x);
        x.dispose();
    }

    /** {@inheritDoc} */
    @Override
    public void setLoadDisabled(boolean s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void printTree(
            PrintTreeSettings settings,
            PrintWriter writer,
            String indent,
            MutableInt lineNumber) {

        printTree(settings, Locale.getDefault(), writer, indent, lineNumber);
    }

    /** {@inheritDoc} */
    @Override
    public void printTree(
            PrintTreeSettings settings,
            Locale locale,
            PrintWriter writer,
            String indent,
            MutableInt lineNumber) {

        for (LogixNG logixNG : getNamedBeanSet()) {
            logixNG.printTree(settings, locale, writer, indent, "", lineNumber);
            writer.println();
        }
        InstanceManager.getDefault(ModuleManager.class).printTree(settings, locale, writer, indent, lineNumber);
        InstanceManager.getDefault(NamedTableManager.class).printTree(locale, writer, indent);
        InstanceManager.getDefault(LogixNG_InitializationManager.class).printTree(locale, writer, indent);
    }


    static volatile DefaultLogixNGManager _instance = null;

    @InvokeOnGuiThread  // this method is not thread safe
    static public DefaultLogixNGManager instance() {
        if (!ThreadingUtil.isGUIThread()) {
            LoggingUtil.warnOnce(log, "instance() called on wrong thread");
        }

        if (_instance == null) {
            _instance = new DefaultLogixNGManager();
        }
        return (_instance);
    }

    /** {@inheritDoc} */
    @Override
    public Class<LogixNG> getNamedBeanClass() {
        return LogixNG.class;
    }

    /** {@inheritDoc} */
    @Override
    public Clipboard getClipboard() {
        return _clipboard;
    }

    /** {@inheritDoc} */
    @Override
    public void registerManager(Manager<? extends MaleSocket> manager) {
        _managers.put(manager.getClass().getName(), manager);
    }

    /** {@inheritDoc} */
    @Override
    public Manager<? extends MaleSocket> getManager(String className) {
        return _managers.get(className);
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
//    @OverridingMethodsMustInvokeSuper
    public final void deleteBean(@Nonnull LogixNG logixNG, @Nonnull String property) throws PropertyVetoException {
        for (int i=0; i < logixNG.getNumConditionalNGs(); i++) {
            ConditionalNG child = logixNG.getConditionalNG(i);
            InstanceManager.getDefault(ConditionalNG_Manager.class).deleteBean(child, property);
        }
        
        // throws PropertyVetoException if vetoed
        fireVetoableChange(property, logixNG);
        if (property.equals("DoDelete")) { // NOI18N
            deregister(logixNG);
            logixNG.dispose();
        }
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultLogixNGManager.class);

}
