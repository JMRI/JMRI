package jmri.jmrix.can.cbus.eventtable;

import java.io.File;
import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;

/**
 * Class to provide access to the EventTableData.xml file.
 * @author Steve Young Copyright (C) 2019
 */
public class CbusEventTableXmlFile extends XmlFile {

    public static String getDefaultFileName() {
        return getFileLocation() + getFileName();
    }

    public File getFile(boolean store) {
        // Verify that directory:cbus exists
        FileUtil.createDirectory(getFileLocation());

        File file = findFile(getDefaultFileName());
        if (file == null && store) {
            file = new File(getDefaultFileName());
        }
        return file;
    }

    public static String getFileName() {
        return "EventTableData.xml";  // NOI18N
    }

    /**
     * Absolute path to location of TimeTable files.
     *
     * @return path to location
     */
    public static String getFileLocation() {
        return FileUtil.getUserFilesPath() + "cbus" + File.separator;  // NOI18N
    }
}
