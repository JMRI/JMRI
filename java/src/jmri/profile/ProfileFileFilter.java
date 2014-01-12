/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.profile;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author rhwood
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
