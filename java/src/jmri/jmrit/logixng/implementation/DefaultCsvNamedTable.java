package jmri.jmrit.logixng.implementation;

import java.io.File;
import java.io.FileNotFoundException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * The default implementation of a NamedTable
 *
 * @author Daniel Bergqvist 2018
 */
public class DefaultCsvNamedTable extends AbstractNamedTable {

    private String _fileName;
    private boolean _fileHasSystemUserName;

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

    /**
     * Create a new named table.
     * @param sys the system name
     * @param user the user name or null if no user name
     * @param fileName the file name of the CSV table
     * @param fileHasSystemUserName true if the file has system name and user name
     * @param data the data in the table. Note that this data is not copied to
     *        a new array but used by the table as is.
     * @param csvType the type of delimiter used for the file (comma or tab)
     * @throws BadUserNameException when needed
     * @throws BadSystemNameException when needed
     */
    public DefaultCsvNamedTable(
            @Nonnull String sys, @CheckForNull String user,
            @CheckForNull String fileName,
            boolean fileHasSystemUserName,
            @Nonnull Object[][] data,
            CsvType csvType)
            throws BadUserNameException, BadSystemNameException {
        super(sys,user,data);

        _fileName = fileName;
        _fileHasSystemUserName = fileHasSystemUserName;
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

    @Override
    public void storeTableAsCSV() throws FileNotFoundException {
        storeTableAsCSV(new File(_fileName), _fileHasSystemUserName);
    }

}
