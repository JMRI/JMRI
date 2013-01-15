// FileUtil.java
package jmri.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import jmri.jmrit.XmlFile;
import org.apache.log4j.Logger;

/**
 * Common utility methods for working with Files. <P> We needed a place to
 * refactor common File-processing idioms in JMRI code, so this class was
 * created. It's more of a library of procedures than a real class, as (so far)
 * all of the operations have needed no state information.
 *
 * @author Bob Jacobsen Copyright 2003, 2005, 2006
 * @author Randall Wood Copyright 2012, 2013
 * @version $Revision$
 */
public class FileUtil {

    /**
     * Portable reference to items in the JMRI program directory
     */
    static public final String PROGRAM = "program:";
    /**
     * Portable reference to items in the JMRI user's preferences directory
     */
    static public final String PREFERENCES = "preference:";
    /**
     * Portable reference to the user's home directory
     */
    static public final String HOME = "home:";
    @Deprecated
    static public final String RESOURCE = "resource:";
    @Deprecated
    static public final String FILE = "file:";
    /**
     * The portable file path component separator
     */
    static public final char SEPARATOR = '/';
    /*
     * JMRI program path, defaults to directory JMRI is executed from
     */
    static private String programPath = null;
    /*
     * User's home directory
     */
    static private String homePath = System.getProperty("user.home") + File.separator;
    /*
     * path to jmri.jar
     */
    static private String jarPath = null;

    /**
     * Find the resource file corresponding to a name. There are five cases:
     * <UL> <LI> Starts with "resource:", treat the rest as a pathname relative
     * to the program directory (deprecated; see "program:" below) <LI> Starts
     * with "program:", treat the rest as a relative pathname below the program
     * directory <LI> Starts with "preference:", treat the rest as a relative
     * path below the preferences directory <LI> Starts with "home:", treat the
     * rest as a relative path below the user.home directory <LI> Starts with
     * "file:", treat the rest as a relative path below the resource directory
     * in the preferences directory (deprecated; see "preference:" above) <LI>
     * Otherwise, treat the name as a relative path below the program directory
     * </UL> In any case, absolute pathnames will work.
     *
     * @param pName The name string, possibly starting with file: or resource:
     * @return Absolute file name to use, or null.
     * @since 2.7.2
     */
    static public String getExternalFilename(String pName) {
        if (pName == null || pName.length() == 0) {
            return null;
        }
        if (pName.startsWith(RESOURCE)) {
            // convert to relative filename 
            String temp = pName.substring(RESOURCE.length());
            if ((new File(temp)).isAbsolute()) {
                return temp.replace(SEPARATOR, File.separatorChar);
            } else {
                return temp.replace(SEPARATOR, File.separatorChar);
            }
        } else if (pName.startsWith(PROGRAM)) {
            // both relative and absolute are just returned
            return pName.substring(PROGRAM.length()).replace(SEPARATOR, File.separatorChar);
        } else if (pName.startsWith(PREFERENCES)) {
            String filename = pName.substring(PREFERENCES.length());

            // Check for absolute path name
            if ((new File(filename)).isAbsolute()) {
                if (log.isDebugEnabled()) {
                    log.debug("Load from absolute path: " + filename);
                }
                return filename.replace(SEPARATOR, File.separatorChar);
            }
            // assume this is a relative path from the
            // preferences directory
            filename = XmlFile.userFileLocationDefault() + filename;
            if (log.isDebugEnabled()) {
                log.debug("load from user preferences file: " + filename);
            }
            return filename.replace(SEPARATOR, File.separatorChar);
        } else if (pName.startsWith(FILE)) {
            String filename = pName.substring(FILE.length());

            // historically, absolute path names could be stored 
            // in the 'file' format.  Check for those, and
            // accept them if present
            if ((new File(filename)).isAbsolute()) {
                if (log.isDebugEnabled()) {
                    log.debug("Load from absolute path: " + filename);
                }
                return filename.replace(SEPARATOR, File.separatorChar);
            }
            // assume this is a relative path from the
            // preferences directory
            filename = FileUtil.getPreferencesPath() + "resources" + File.separator + filename;
            if (log.isDebugEnabled()) {
                log.debug("load from user preferences file: " + filename);
            }
            return filename.replace(SEPARATOR, File.separatorChar);
        } else if (pName.startsWith(HOME)) {
            String filename = pName.substring(HOME.length());

            // Check for absolute path name
            if ((new File(filename)).isAbsolute()) {
                if (log.isDebugEnabled()) {
                    log.debug("Load from absolute path: " + filename);
                }
                return filename.replace(SEPARATOR, File.separatorChar);
            }
            // assume this is a relative path from the
            // user.home directory
            filename = FileUtil.getHomePath() + filename;
            if (log.isDebugEnabled()) {
                log.debug("load from user preferences file: " + filename);
            }
            return filename.replace(SEPARATOR, File.separatorChar);
        } else {
            // must just be a (hopefully) valid name
            return pName.replace(SEPARATOR, File.separatorChar);
        }
    }

    /**
     * Convert a portable filename into an absolute filename
     *
     * @param path
     * @return An absolute filename
     */
    static public String getAbsoluteFilename(String path) {
        if (path == null || path.length() == 0) {
            return null;
        }
        if (path.startsWith(PROGRAM)) {
            if (new File(path.substring(PROGRAM.length())).isAbsolute()) {
                path = path.substring(PROGRAM.length());
            } else {
                path = path.replaceFirst(PROGRAM, Matcher.quoteReplacement(FileUtil.getProgramPath()));
            }
        } else if (path.startsWith(PREFERENCES)) {
            if (new File(path.substring(PREFERENCES.length())).isAbsolute()) {
                path = path.substring(PREFERENCES.length());
            } else {
                path = path.replaceFirst(PREFERENCES, Matcher.quoteReplacement(FileUtil.getPreferencesPath()));
            }
        } else if (path.startsWith(HOME)) {
            if (new File(path.substring(HOME.length())).isAbsolute()) {
                path = path.substring(HOME.length());
            } else {
                path = path.replaceFirst(HOME, Matcher.quoteReplacement(FileUtil.getHomePath()));
            }
        } else if (path.startsWith(RESOURCE) || path.startsWith(FILE)) {
            return getAbsoluteFilename(getPortableFilename(getExternalFilename(path)));
        } else if (!new File(path).isAbsolute()) {
            return null;
        }
        try {
            // if path cannot be converted into a canonical path, return null
            if (log.isDebugEnabled()) {
                log.debug("Using " + path);
            }
            return new File(path.replace(SEPARATOR, File.separatorChar)).getCanonicalPath();
        } catch (IOException ex) {
            log.warn("Can not convert " + path + " into a usable filename.", ex);
            return null;
        }
    }

    /**
     * Convert a File object to our preferred storage form.
     *
     * This is the inverse of {@link #getExternalFilename(String pName)}.
     * Deprecated forms are not created.
     *
     * @param file File to be represented
     * @since 2.7.2
     */
    static public String getPortableFilename(File file) {
        // compare full path name to see if same as preferences
        String filename = file.getAbsolutePath();

        // compare full path name to see if same as preferences
        if (filename.startsWith(getPreferencesPath())) {
            return PREFERENCES + filename.substring(getPreferencesPath().length(), filename.length()).replace(File.separatorChar, SEPARATOR);
        }

        // now check for relative to program dir
        if (filename.startsWith(getProgramPath())) {
            return PROGRAM + filename.substring(getProgramPath().length(), filename.length()).replace(File.separatorChar, SEPARATOR);
        }

        // compare full path name to see if same as home directory
        // do this last, in case preferences or program dir are in home directory
        if (filename.startsWith(getHomePath())) {
            return HOME + filename.substring(getHomePath().length(), filename.length()).replace(File.separatorChar, SEPARATOR);
        }

        return filename.replace(File.separatorChar, SEPARATOR);   // absolute, and doesn't match; not really portable...
    }

    /**
     * Convert a filename string to our preferred storage form.
     *
     * This is the inverse of {@link #getExternalFilename(String pName)}.
     * Deprecated forms are not created.
     *
     * @param filename Filename to be represented
     * @since 2.7.2
     */
    static public String getPortableFilename(String filename) {
        // if this already contains prefix, run through conversion to normalize
        if (filename.startsWith(FILE)
                || filename.startsWith(RESOURCE)
                || filename.startsWith(PROGRAM)
                || filename.startsWith(HOME)
                || filename.startsWith(PREFERENCES)) {
            return getPortableFilename(getExternalFilename(filename));
        } else {
            // treat as pure filename
            return getPortableFilename(new File(filename));
        }
    }

    /**
     * Get the user's home directory.
     *
     * @return User's home directory as a String
     */
    static public String getHomePath() {
        return homePath;
    }

    /**
     * Get the JMRI preferences directory.
     *
     * @return JMRI preferences directory as a String
     */
    static public String getPreferencesPath() {
        return XmlFile.userFileLocationDefault();
    }

    /**
     * Get the JMRI program directory.
     *
     * @return JMRI program directory as a String.
     */
    static public String getProgramPath() {
        if (programPath == null) {
            FileUtil.setProgramPath(".");
        }
        return programPath;
    }

    /**
     * Set the JMRI program directory.
     *
     * Convenience method that calls {@link FileUtil#setProgramPath(java.io.File)
     * with the passed in path.
     *
     * @param path
     */
    static public void setProgramPath(String path) {
        FileUtil.setProgramPath(new File(path));
    }

    /**
     * Set the JMRI program directory.
     *
     * If set, allows JMRI to be loaded from locations other than the directory
     * containing JMRI resources. This must be set very early in the process of
     * loading JMRI (prior to loading any other JMRI code) to be meaningfully
     * used.
     *
     * @param path
     */
    static public void setProgramPath(File path) {
        try {
            programPath = (path).getCanonicalPath() + File.separator;
        } catch (IOException ex) {
            log.error("Unable to get JMRI program directory.", ex);
        }
    }

    /**
     * Get the URL of a portable filename if it can be located using
     * {@link #findURL(java.lang.String)}
     *
     * @param path
     * @return
     */
    static public URL findExternalFilename(String path) {
        return FileUtil.findURL(FileUtil.getExternalFilename(path));
    }

    /**
     * Search for a file or JAR resource by name and return the
     * {@link java.io.InputStream} for that file. Search order is defined by
     * {@link #findURL(java.lang.String, java.lang.String[])}.
     *
     * @param path The relative path of the file or resource.
     * @return InputStream or null.
     * @see #findInputStream(java.lang.String, java.lang.String[]) 
     * @see #findURL(java.lang.String) 
     * @see #findURL(java.lang.String, java.lang.String[]) 
     */
    static public InputStream findInputStream(String path) {
        return FileUtil.findInputStream(path, (String[]) null);
    }

    /**
     * Search for a file or JAR resource by name and return the
     * {@link java.io.InputStream} for that file. Search order is defined by
     * {@link #findURL(java.lang.String, java.lang.String[])}.
     *
     * @param path The relative path of the file or resource.
     * @param searchPaths a list of paths to search for the path in
     * @return InputStream or null.
     * @see #findInputStream(java.lang.String, java.lang.String[]) 
     * @see #findURL(java.lang.String) 
     * @see #findURL(java.lang.String, java.lang.String[]) 
     */
    static public InputStream findInputStream(String path, String... searchPaths) {
        URL file = FileUtil.findURL(path, searchPaths);
        if (file != null) {
            try {
                return file.openStream();
            } catch (IOException ex) {
                log.error(ex.getLocalizedMessage(), ex);
            }
        }
        return null;
    }

    /**
     * Search for a file or JAR resource by name and return the
     * {@link java.net.URL} for that file. Search order is defined by
     * {@link #findURL(java.lang.String, java.lang.String[])}.
     *
     * @param path The relative path of the file or resource.
     * @return The URL or null.
     * @see #findInputStream(java.lang.String) 
     * @see #findInputStream(java.lang.String, java.lang.String[]) 
     * @see #findURL(java.lang.String, java.lang.String[]);
     */
    static public URL findURL(String path) {
        return FileUtil.findURL(path, (String[]) null);
    }

    /**
     * Search for a file or JAR resource by name and return the
     * {@link java.net.URL} for that file. <p> Search order is:<ol> <li>As a
     * {@link java.io.File} in the user preferences directory</li> <li>As a File
     * in the current working directory (usually, but not always the JMRI
     * distribution directory)</li> <li>As a File in the JMRI distribution
     * directory</li> <li>As a resource in jmri.jar</li> <li>For any provided
     * searchPaths, iterate over the searchPaths by prepending each searchPath
     * to the path and following the above search order.</li></ol>
     *
     * @param path The relative path of the file or resource
     * @param searchPaths a list of paths to search for the path in
     * @return The URL or null
     * @see #findInputStream(java.lang.String) 
     * @see #findInputStream(java.lang.String, java.lang.String[]) 
     * @see #findURL(java.lang.String) 
     */
    static public URL findURL(String path, String... searchPaths) {
        try {
            // attempt to return path from preferences directory
            File file = new File(FileUtil.getPreferencesPath() + path);
            if (file.exists()) {
                return file.toURI().toURL();
            }
            // attempt to return path from current working directory
            file = new File(path);
            if (file.exists()) {
                return file.toURI().toURL();
            }
            // attempt to return path from JMRI distribution directory
            file = new File(FileUtil.getProgramPath() + path);
            if (file.exists()) {
                return file.toURI().toURL();
            }
        } catch (MalformedURLException ex) {
            log.warn("Unable to get URL for " + path);
            return null;
        }
        // return path if in jmri.jar or null
        URL resource = FileUtil.class.getClassLoader().getResource(path);
        if (resource == null) {
            for (String searchPath : searchPaths) {
                resource = FileUtil.findURL(searchPath + File.separator + path);
                if (resource != null) {
                    return resource;
                }
            }
        }
        return resource;
    }

    /**
     * Return the {@link java.net.URI} for a given URL
     *
     * @param url
     * @return a URI or null if the conversion would have caused a
     * {@link java.net.URISyntaxException}
     */
    static public URI urlToURI(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException ex) {
            log.error("Unable to get URI from URL", ex);
            return null;
        }
    }

    /**
     * Get the JMRI distribution jar file.
     *
     * @return a {@link java.util.jar.JarFile} pointing to jmri.jar or null
     */
    static public JarFile jmriJarFile() {
        if (jarPath == null) {
            CodeSource sc = FileUtil.class.getProtectionDomain().getCodeSource();
            if (sc != null) {
                jarPath = sc.getLocation().toString();
                // 9 = length of jar:file:
                jarPath = jarPath.substring(9, jarPath.lastIndexOf("!"));
                log.debug("jmri.jar path is " + jarPath);
            }
        }
        try {
            return new JarFile(jarPath);
        } catch (IOException ex) {
            log.error("Unable to open jmri.jar", ex);
            return null;
        }
    }

    static public void logFilePaths() {
        log.info("File path " + FileUtil.PROGRAM + " is " + FileUtil.getProgramPath());
        log.info("File path " + FileUtil.PREFERENCES + " is " + FileUtil.getPreferencesPath());
        log.info("File path " + FileUtil.HOME + " is " + FileUtil.getHomePath());
    }
    // initialize logging
    static private Logger log = Logger.getLogger(FileUtil.class.getName());
}