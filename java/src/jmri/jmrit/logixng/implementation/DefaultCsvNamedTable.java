package jmri.jmrit.logixng.implementation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.NamedBean;
import jmri.NamedBean.BadUserNameException;
import jmri.NamedBean.BadSystemNameException;
import jmri.implementation.AbstractNamedBean;
import jmri.jmrit.logixng.AnonymousTable;
import jmri.jmrit.logixng.NamedTable;
import jmri.jmrit.logixng.NamedTableManager;

/**
 * The default implementation of a NamedTable
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultCsvNamedTable extends AbstractNamedTable {

    private String _fileName;
    
    /**
     * Create a new named table.
     * @param sys the system name
     * @param user the user name or null if no user name
     * @param fileName the file name of the CSV table
     * @param data the data in the table. Note that this data is not copied to
     * an new array but used by the table as is.
     */
    public DefaultCsvNamedTable(
            @Nonnull String sys, @CheckForNull String user,
            @CheckForNull String fileName,
            @Nonnull Object[][] data)
            throws BadUserNameException, BadSystemNameException {
        super(sys,user,data);
        
        _fileName = fileName;
    }
    
    public String getFileName() {
        return _fileName;
    }
    
    public void setFileName(String fileName) {
        this._fileName = fileName;
    }
    
}
