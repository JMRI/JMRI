package jmri.jmrit.logixng.implementation;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.NamedBean.BadUserNameException;
import jmri.NamedBean.BadSystemNameException;
import jmri.util.JmriCsvFormat;

/**
 * The default implementation of a NamedTable
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultCsvNamedTable extends AbstractNamedTable {

    private String _fileName;
    private JmriCsvFormat _csvFormat;
    
    /**
     * Create a new named table.
     * @param sys the system name
     * @param user the user name or null if no user name
     * @param fileName the file name of the CSV table
     * @param data the data in the table. Note that this data is not copied to
     *             an new array but used by the table as is.
     * @param csvFormat the format of the CSV text
     */
    public DefaultCsvNamedTable(
            @Nonnull String sys, @CheckForNull String user,
            @CheckForNull String fileName,
            @Nonnull Object[][] data,
            @Nonnull JmriCsvFormat csvFormat)
            throws BadUserNameException, BadSystemNameException {
        super(sys,user,data);
        
        _fileName = fileName;
        _csvFormat = csvFormat;
    }
    
    public String getFileName() {
        return _fileName;
    }
    
    public void setFileName(String fileName) {
        this._fileName = fileName;
    }
    
    @Nonnull
    public JmriCsvFormat getCSVFormat() {
        return _csvFormat;
    }
    
    public void setCSVFormat(@Nonnull JmriCsvFormat csvFormat) {
        this._csvFormat = csvFormat;
    }
    
}
