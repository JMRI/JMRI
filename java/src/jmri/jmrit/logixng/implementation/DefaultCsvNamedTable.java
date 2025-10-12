package jmri.jmrit.logixng.implementation;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * The default implementation of a NamedTable
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultCsvNamedTable extends AbstractNamedTable {

    private String _fileName;

    private CsvType _csvType;
    
    /**
     * Create a new named table.
     * @param sys the system name
     * @param user the user name or null if no user name
     * @param fileName the file name of the CSV table
     * @param data the data in the table. Note that this data is not copied to
     *        a new array but used by the table as is.
     * @param csvType the type of delimiter used for the file (comma or tab)
     * @throws BadUserNameException when needed
     * @throws BadSystemNameException when needed
     */
    public DefaultCsvNamedTable(
            @Nonnull String sys, @CheckForNull String user,
            @CheckForNull String fileName,
            @Nonnull Object[][] data,
            CsvType csvType)
            throws BadUserNameException, BadSystemNameException {
        super(sys,user,data);
        
        _fileName = fileName;
        _csvType = csvType;
    }
    @Override
    public boolean isCsvTypeSupported() {
        return true;
    }
    
    public String getFileName() {
        return _fileName;
    }

    public void setFileName(String fileName) {
        this._fileName = fileName;
    }

    @Override
    public void setCsvType(CsvType csvType) {
        _csvType = csvType;
    }

    @Override
    public CsvType getCsvType() {
        return _csvType;
    }

}
