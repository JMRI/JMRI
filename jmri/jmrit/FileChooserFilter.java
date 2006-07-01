package jmri.jmrit;

import java.io.*;

import com.sun.java.util.collections.*;

/**
 * <p> </p>
 * <p> </p>
 * <p> </p>
 * <p> </p>
 * @author Alex Shepherd
 * @version $Revision: 1.2 $
 */

public class FileChooserFilter extends javax.swing.filechooser.FileFilter {

  String mDescription ;
  HashSet allowedExtensions ;

  public FileChooserFilter( String pDescription ) {
    mDescription = pDescription ;
    allowedExtensions = new HashSet() ;
  }

  public void addExtension( String ext ){
    allowedExtensions.add( ext ) ;
  }

  private String getFileExtension(File f) {
    if(f != null) {
      String filename = f.getName();
      int i = filename.lastIndexOf( '.' );
      if( i > 0 && i < filename.length() - 1) {
        return filename.substring( i + 1 ).toLowerCase();
      };
    }
    return null;
  }

  public String getDescription(){
    return mDescription ;
  }

  public boolean accept( File f ){
    if(f != null) {
      if(f.isDirectory()) {
        return true;
      }
      String extension = getFileExtension( f );
      if(extension != null && allowedExtensions.contains( extension ) )
        return true;
    }
    return false;
  }
}
