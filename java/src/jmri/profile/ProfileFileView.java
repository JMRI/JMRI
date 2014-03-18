package jmri.profile;

import java.io.File;
import java.util.Arrays;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;
import jmri.util.FileUtil;

/**
 *
 * @author rhwood
 */
public class ProfileFileView extends FileView {

    @Override
    public String getDescription(File f) {
        if (!this.isTraversable(f)) {
            return Bundle.getMessage("FileViewDescription", f.getName());
        } else {
            return null;
        }
    }

    @Override
    public Boolean isTraversable(File f) {
        if (f.isDirectory() && f.canRead() && Arrays.asList(f.list()).contains(Profile.PROPERTIES)) {
            return false;
        }
        return null;
    }

    @Override
    public Icon getIcon(File f) {
        if (f.isDirectory() && f.canRead() && Arrays.asList(f.list()).contains(Profile.PROPERTIES)) {
            return new ImageIcon(FileUtil.getExternalFilename(FileUtil.PROGRAM + "resources/jmri16x16.gif")); // NOI18N
        }
        return null;
    }
}
