package jmri.util;

/**
 * File filter to suppress zip file archives.
 * <p>
 * Java 1.6's FileChooser gets slow when it encounters large zip files. This
 * filter skips them, while still allowing you to specify others, etc.
 *
 * @author Bob Jacobsen Copyright 2007 Made from a suggestion by John Plocher
 */
public class NoArchiveFileFilter extends jmri.util.FileChooserFilter {

    public NoArchiveFileFilter() {
        super("Omit archive files");
    }

    public NoArchiveFileFilter(String description) {
        super(description);
    }

    @Override
    public boolean accept(java.io.File f) {
        if (f.getName().endsWith(".zip")) {
            return false;
        }
        if (f.getName().endsWith(".jar")) {
            return false;
        } else {
            return super.accept(f);
        }
    }

}
