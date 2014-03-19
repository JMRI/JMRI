package jmri.profile;

import java.io.File;
import java.util.Arrays;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class ProfileFileView extends FileView {

    private static final Logger log = LoggerFactory.getLogger(ProfileFileView.class);

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
        try {
            if (f.isDirectory() && f.canRead() && Arrays.asList(f.list()).contains(Profile.PROPERTIES)) {
                return false;
            }
        } catch (NullPointerException ex) {
            // this is most likely caused by virtual folders like Networks in Windows 7
            log.debug("Unable to list contents of {}", f.getPath());
        }
        return true;
    }

    @Override
    public Icon getIcon(File f) {
        if (!isTraversable(f)) {
            return new ImageIcon(FileUtil.getExternalFilename(FileUtil.PROGRAM + "resources/jmri16x16.gif")); // NOI18N
        }
        return null;
    }
}
