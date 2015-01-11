package jmri.profile;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * FileFilter for use in a JFileChooser.
 *
 * @author Randall Wood Copyright (C) 2013, 2014
 */
public class ProfileFileFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        return (f.isDirectory());
    }

    @Override
    public String getDescription() {
        return Bundle.getMessage("fileFilterDescription");
    }
}
