// NoArchiveFileFilter.java

package jmri.util;

/**
 * File filter to suppress zip file archives.
 *<P>
 * Java 1.6's FileChooser gets slow when it encounters
 * large zip files.  This filter skips them.
 *
 * @author Bob Jacobsen  Copyright 2007
 * Made from a suggestion by John Plocher
 * @version $Revision: 1.1 $
 */

class NoArchiveFileFilter extends javax.swing.filechooser.FileFilter {
       public NoArchiveFileFilter() {
       }

       public boolean accept(java.io.File f) {
         return !f.getName().endsWith(".zip");
       }

       public String getDescription() {
         return "No archive files";
       }
}
