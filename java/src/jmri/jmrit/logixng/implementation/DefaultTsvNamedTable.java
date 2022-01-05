package jmri.jmrit.logixng.implementation;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.NamedBean.BadUserNameException;
import jmri.NamedBean.BadSystemNameException;

/**
 * The default implementation of a NamedTable
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultTsvNamedTable extends AbstractNamedTable {

    private String _fileName;
    
    /**
     * Create a new named table.
     * @param sys the system name
     * @param user the user name or null if no user name
     * @param fileName the file name of the TSV file (TAB separated file, similar to a CSV file)
     * @param data the data in the table. Note that this data is not copied to
     * an new array but used by the table as is.
     */
    public DefaultTsvNamedTable(
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
