package jmri.util;

import java.io.File;
import java.util.HashSet;

/**
 * Allow selection of possible files by their extension.
 *
 * <p>
 * As a convenience, if no extension(s) are specified, all files pass.
 * <p>
 * Except in that case, files without extensions fail.
 *
 * @author Alex Shepherd
 */
public class FileChooserFilter extends javax.swing.filechooser.FileFilter {

    String mDescription;
    HashSet<String> allowedExtensions;

    public FileChooserFilter(String pDescription) {
        mDescription = pDescription;
        allowedExtensions = new HashSet<String>();
    }

    public void addExtension(String ext) {
        allowedExtensions.add(ext.toLowerCase());
    }

    public static String getFileExtension(File f) {
        if (f != null) {
            String filename = f.getName();
            int i = filename.lastIndexOf('.');
            if (i > 0 && i < filename.length() - 1) {
                return filename.substring(i + 1).toLowerCase();
            }
        }
        return null;
    }

    @Override
    public String getDescription() {
        return mDescription;
    }

    @Override
    public boolean accept(File f) {
        if (allowedExtensions.isEmpty()) {
            return true;
        }
        if (f != null) {
            if (f.isDirectory()) {
                return true;
            }
            String extension = getFileExtension(f);
            if (extension == null) {
                return false;
            }
            if (allowedExtensions.contains(extension)) {
                return true;
            }
        }
        return false;
    }
}
