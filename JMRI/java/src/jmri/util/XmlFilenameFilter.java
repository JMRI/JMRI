package jmri.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Filter for filenames ending in <em>.xml</em>.
 * 
 * @author Randall Wood (C) 2013, 2017
 */
public class XmlFilenameFilter implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".xml"); // NOI18N
    }

}
