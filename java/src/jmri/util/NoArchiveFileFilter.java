package jmri.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FilenameUtils;

/**
 * File filter to suppress zip file archives.
 * <p>
 * Java 1.6's FileChooser gets slow when it encounters large zip files. This
 * filter skips them, while still allowing you to specify others, etc.
 *
 * @author Bob Jacobsen Copyright 2007 Made from a suggestion by John Plocher
 * @author Randall Wood Copyright 2020
 */
public class NoArchiveFileFilter extends FileFilter {

    private final String description;
    private final Set<String> extensions = new HashSet<>();

    /**
     * Create a filter that excludes {@code .zip} and {@code .jar} files.
     */
    public NoArchiveFileFilter() {
        this("Omit archive files");
    }

    /**
     * Create a filter that excludes {@code .zip} and {@code .jar} files.
     *
     * @param description The filter description presented in a file chooser
     */
    public NoArchiveFileFilter(String description) {
        this.description = description;
    }

    /**
     * Create a filter that filters for the specified extensions, excluding
     * {@code .zip} and {@code .jar} files.
     *
     * @param description The filter description presented in a file chooser
     * @param extensions  The extensions to accept; if no extensions are passed,
     *                    all files except archives are accepted; to accept a
     *                    limited set of extensions and files without an
     *                    extension, include an empty String as an extension
     */
    public NoArchiveFileFilter(String description, String... extensions) {
        this(description);
        Arrays.stream(extensions).filter(e -> (e != null)).forEach(e -> this.extensions.add(e));
    }

    @Override
    public boolean accept(java.io.File f) {
        String extension = FilenameUtils.getExtension(f.getName());
        if ("zip".equalsIgnoreCase(extension)
                || "jar".equalsIgnoreCase(extension)) {
            return false;
        }
        return extensions.isEmpty() || extensions.contains(extension);
    }

    @Override
    public String getDescription() {
        return description;
    }

}
