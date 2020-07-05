package jmri.util;

import java.io.File;
import java.io.FilenameFilter;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Filter for filenames ending in <em>.xml</em>.
 * 
 * @author Randall Wood (C) 2013, 2017
 */
@API(status = EXPERIMENTAL)
public class XmlFilenameFilter implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".xml"); // NOI18N
    }

}
