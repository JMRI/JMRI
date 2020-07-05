package jmri.jmrit.logixng.implementation;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.InvokeOnGuiThread;
import jmri.NamedBean;
import jmri.jmrit.logixng.AnonymousTable;
import jmri.jmrit.logixng.NamedTable;
import jmri.jmrit.logixng.NamedTableManager;
import jmri.managers.AbstractManager;
import jmri.util.*;

/**
 * Class providing the basic logic of the NamedTable_Manager interface.
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
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
        if (systemName.matches(getSubSystemNamePrefix()+"(:AUTO:)?\\d+")) {
            return NameValidity.VALID;
        } else {
            return NameValidity.INVALID;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NamedTable newTable(String systemName, String userName, int numRows, int numColumns)
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
        // NamedTable does not exist, create a new NamedTable
        x = new DefaultNamedTable(systemName, userName, numRows, numColumns);
        // save in the maps
        register(x);
        
        // Keep track of the last created auto system name
        updateAutoNumber(systemName);
        
        return x;
    }
/*
    @Override
    public NamedTable createNamedTable(String userName, int numRows, int numColumns)
            throws IllegalArgumentException {
        return createNamedTable(getAutoSystemName(), userName, numRows, numColumns);
    }
*/    
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
    public NamedTable loadTableFromCSV(@Nonnull String text)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException {
        return DefaultNamedTable.loadTableFromCSV_Text(text);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public NamedTable loadTableFromCSV(@Nonnull File file)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException {
        return DefaultNamedTable.loadTableFromCSV_File(file);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public NamedTable loadTableFromCSV(
            @Nonnull File file,
            @Nonnull String sys, @CheckForNull String user)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException {
        return DefaultNamedTable.loadTableFromCSV_File(file, sys, user);
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultNamedTableManager.class);

}
