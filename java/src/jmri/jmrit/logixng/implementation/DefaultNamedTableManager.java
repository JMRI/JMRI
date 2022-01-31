package jmri.jmrit.logixng.implementation;

import java.beans.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Locale;

import javax.annotation.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.managers.AbstractManager;
import jmri.util.*;

/**
 * Class providing the basic logic of the NamedTable_Manager interface.
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2020
 */
public class DefaultNamedTableManager extends AbstractManager<NamedTable>
        implements NamedTableManager {

    DecimalFormat paddedNumber = new DecimalFormat("0000");

    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getXMLOrder() {
        return LOGIXNG_TABLES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char typeLetter() {
        return 'Q';
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public NamedTable newCSVTable(
            @Nonnull String systemName,
            @CheckForNull String userName,
            @Nonnull String fileName,
            @Nonnull JmriCsvFormat csvFormat)
            throws IllegalArgumentException {
        
        // Check that NamedTable does not already exist
        NamedTable x;
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
        try {
            // NamedTable does not exist, create a new NamedTable
            x = AbstractNamedTable.loadTableFromCSV_File(systemName, userName, fileName, csvFormat, true);
        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
            log.error("Cannot load table due to I/O error", ex);
            return null;
        }
        // save in the maps
        register(x);
        
        // Keep track of the last created auto system name
        updateAutoNumber(systemName);
        
        return x;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NamedTable newInternalTable(String systemName, String userName, int numRows, int numColumns)
            throws IllegalArgumentException {
        
        // Check that NamedTable does not already exist
        NamedTable x;
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
        // Table does not exist, create a new NamedTable
        x = new DefaultInternalNamedTable(systemName, userName, numRows, numColumns);
        // save in the maps
        register(x);
        
        // Keep track of the last created auto system name
        updateAutoNumber(systemName);
        
        return x;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnonymousTable newAnonymousTable(int numRows, int numColumns)
            throws IllegalArgumentException {
        
        // Check that NamedTable does not already exist
        // NamedTable does not exist, create a new NamedTable
        return new DefaultAnonymousTable(numRows, numColumns);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public NamedTable loadTableFromCSVData(
            @Nonnull String sys,
            @CheckForNull String user,
            @Nonnull String text,
            @Nonnull JmriCsvFormat csvFormat)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException {
        return AbstractNamedTable.loadTableFromCSV_Text(sys, user, text, csvFormat, true);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public NamedTable loadTableFromCSV(
            @Nonnull String sys, @CheckForNull String user,
            @Nonnull String fileName,
            @Nonnull JmriCsvFormat csvFormat)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException {
        return AbstractNamedTable.loadTableFromCSV_File(sys, user, fileName, csvFormat, true);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public NamedTable loadTableFromCSV(
            @Nonnull String sys, @CheckForNull String user,
            @Nonnull File file,
            @Nonnull JmriCsvFormat csvFormat)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException {
        return AbstractNamedTable.loadTableFromCSV_File(sys, user, file, csvFormat, true);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public NamedTable getNamedTable(String name) {
        NamedTable x = getByUserName(name);
        if (x != null) {
            return x;
        }
        return getBySystemName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NamedTable getByUserName(String name) {
        return _tuser.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NamedTable getBySystemName(String name) {
        return _tsys.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameNamedTables" : "BeanNameNamedTable");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteNamedTable(NamedTable x) {
        // delete the NamedTable
        deregister(x);
        x.dispose();
    }

    /** {@inheritDoc} */
    @Override
    public void printTree(PrintWriter writer, String indent) {
        printTree(Locale.getDefault(), writer, indent);
    }

    /** {@inheritDoc} */
    @Override
    public void printTree(Locale locale, PrintWriter writer, String indent) {
        for (NamedTable namedTable : getNamedBeanSet()) {
            if (namedTable instanceof DefaultCsvNamedTable) {
                DefaultCsvNamedTable csvTable = (DefaultCsvNamedTable)namedTable;
                writer.append(String.format(
                        "Named table: System name: %s, User name: %s, File name: %s, Num rows: %d, Num columns: %d",
                        csvTable.getSystemName(), csvTable.getUserName(),
                        csvTable.getFileName(), csvTable.numRows(), csvTable.numColumns()));
            } if (namedTable != null) {
                writer.append(String.format(
                        "Named table: System name: %s, User name: %s, Num rows: %d, Num columns: %d",
                        namedTable.getSystemName(), namedTable.getUserName(),
                        namedTable.numRows(), namedTable.numColumns()));
            } else {
                throw new NullPointerException("namedTable is null");
            }
            writer.println();
            writer.println();
        }
    }
    
    static volatile DefaultNamedTableManager _instance = null;

    @InvokeOnGuiThread  // this method is not thread safe
    static public DefaultNamedTableManager instance() {
        if (!ThreadingUtil.isGUIThread()) {
            LoggingUtil.warnOnce(log, "instance() called on wrong thread");
        }
        
        if (_instance == null) {
            _instance = new DefaultNamedTableManager();
        }
        return (_instance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<NamedTable> getNamedBeanClass() {
        return NamedTable.class;
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
    public final void deleteBean(@Nonnull NamedTable namedTable, @Nonnull String property) throws PropertyVetoException {
        // throws PropertyVetoException if vetoed
        fireVetoableChange(property, namedTable);
        if (property.equals("DoDelete")) { // NOI18N
            deregister(namedTable);
            namedTable.dispose();
        }
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultNamedTableManager.class);

}
