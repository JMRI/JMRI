package jmri.util.swing;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * Text by extension file filter. Matches files with extension {@literal .txt}.
 *
 * @author Dan Boudreau Copyright 2007
 * @author Randall Wood Copyright 2017
 */
public class TextFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        return f.isDirectory() || f.getName().matches(".*\\.txt");
    }

    @Override
    public String getDescription() {
        return "Text Documents (*.txt)";
    }

}
