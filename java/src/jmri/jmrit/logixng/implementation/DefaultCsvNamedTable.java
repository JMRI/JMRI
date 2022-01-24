package jmri.jmrit.logixng.implementation;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.NamedBean.BadUserNameException;
import jmri.NamedBean.BadSystemNameException;

import org.apache.commons.csv.CSVFormat;

/**
 * The default implementation of a NamedTable
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultCsvNamedTable extends AbstractNamedTable {

    private String _fileName;
    private CSVFormat _csvFormat;
    private CSVFormat.Predefined _predefinedCsvFormat;
    
    /**
     * Create a new named table.
     * @param sys the system name
     * @param user the user name or null if no user name
     * @param fileName the file name of the CSV table
     * @param data the data in the table. Note that this data is not copied to
     * @param csvFormat the format of the CSV text
     * @param predefinedCsvFormat the format of the CSV text
     * an new array but used by the table as is.
     */
    public DefaultCsvNamedTable(
            @Nonnull String sys, @CheckForNull String user,
            @CheckForNull String fileName,
            @Nonnull Object[][] data,
            @CheckForNull CSVFormat csvFormat,
            @CheckForNull CSVFormat.Predefined predefinedCsvFormat)
            throws BadUserNameException, BadSystemNameException {
        super(sys,user,data);
        
        _fileName = fileName;
        _csvFormat = csvFormat;
        _predefinedCsvFormat = predefinedCsvFormat;
    }
    
    public String getFileName() {
        return _fileName;
    }
    
    public void setFileName(String fileName) {
        this._fileName = fileName;
    }
    
    public CSVFormat getCSVFormat() {
        return _csvFormat;
    }
    
    public void setCSVFormat(@CheckForNull CSVFormat csvFormat) {
        this._csvFormat = csvFormat;
    }
    
    public CSVFormat.Predefined getPredefinedCSVFormat() {
        return _predefinedCsvFormat;
    }
    
    public void setPredefinedCSVFormat(@CheckForNull CSVFormat.Predefined csvFormat) {
        this._predefinedCsvFormat = csvFormat;
    }
    
}
