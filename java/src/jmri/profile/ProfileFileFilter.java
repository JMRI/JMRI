package jmri.profile;

import java.io.File;
import javax.swing.filechooser.FileFilter;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * FileFilter for use in a JFileChooser.
 *
 * @author Randall Wood Copyright (C) 2013, 2014
 */
@API(status = EXPERIMENTAL)
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
