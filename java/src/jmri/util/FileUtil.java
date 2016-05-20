// FileUtil.java
package jmri.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Portable reference to items in the JMRI program directory.
     */
    static public final String PROGRAM = "program:"; // NOI18N
    /**
     * Portable reference to items in the JMRI user's preferences directory.
     */
    static public final String PREFERENCES = "preference:"; // NOI18N
    /**
     * Portable reference to the user's home directory.
     */
    static public final String HOME = "home:"; // NOI18N
    /**
     * Replaced with {@link #PROGRAM}.
     *
     * @see #PROGRAM
     * @deprecated
     */
    @Deprecated
    static public final String RESOURCE = "resource:"; // NOI18N
    @Deprecated
    /**
     * Replaced with {@link #PREFERENCES}.
     *
     * @see #PREFERENCES
     */
    static public final String FILE = "file:"; // NOI18N
    /**
     * The portable file path component separator.
     */
    static public final char SEPARATOR = '/'; // NOI18N
    /*
     * User's home directory
     */
    static private String homePath = System.getProperty("user.home") + File.separator; // NOI18N
    /*
     * Settable directories
     */
    /* JMRI program path, defaults to directory JMRI is executed from */
    static private String programPath = null;
    /* path to jmri.jar */
    static private String jarPath = null;
    /* path to the jython scripts directory */
    static private String scriptsPath = null;
    /* path to the user's files directory */
    static private String userFilesPath = null;
    // initialize logging
    static private Logger log = LoggerFactory.getLogger(FileUtil.class.getName());

    /**
     * Get the resource file corresponding to a name. There are five cases: <UL>
     * <LI> Starts with "resource:", treat the rest as a pathname relative to
     * the program directory (deprecated; see "program:" below) <LI> Starts with
     * "program:", treat the rest as a relative pathname below the program
     * directory <LI> Starts with "preference:", treat the rest as a relative
     * path below the preferences directory <LI> Starts with "home:", treat the
     * rest as a relative path below the user.home directory <LI> Starts with
     * "file:", treat the rest as a relative path below the resource directory
     * in the preferences directory (deprecated; see "preference:" above) <LI>
     * Otherwise, treat the name as a relative path below the program directory
     * </UL> In any case, absolute pathnames will work.
     *
     * @param pName The name string, possibly starting with program:,
     * preference:, home:, file: or resource:
     * @return Absolute or relative file name to use, or null.
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
            filename = FileUtil.getUserFilesPath() + filename;
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
            filename = FileUtil.getUserFilesPath() + "resources" + File.separator + filename; // NOI18N
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
                path = path.replaceFirst(PREFERENCES, Matcher.quoteReplacement(FileUtil.getUserFilesPath()));
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
        if (filename.startsWith(getUserFilesPath())) {
            return PREFERENCES + filename.substring(getUserFilesPath().length(), filename.length()).replace(File.separatorChar, SEPARATOR);
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
     * Get the user's files directory. If not set by the user, this is the same
     * as the preferences path.
     *
     * @see #getPreferencesPath()
     * @return User's files directory as a String
     */
    static public String getUserFilesPath() {
        return (FileUtil.userFilesPath != null) ? FileUtil.userFilesPath : FileUtil.getPreferencesPath();
    }

    /**
     * Set the user's files directory.
     *
     * @see #getUserFilesPath()
     * @param path The path to the user's files directory
     */
    static public void setUserFilesPath(String path) {
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        FileUtil.userFilesPath = path;
    }

    /**
     * Get the preferences directory. This directory is set based on the OS and
     * is not normally settable by the user. <ul><li>On Microsoft Windows
     * systems, this is JMRI in the User's home directory.</li> <li>On OS X
     * systems, this is Library/Preferences/JMRI in the User's home
     * directory.</li> <li>On Linux, Solaris, and othe UNIXes, this is .jmri in
     * the User's home directory.</li> <li>This can be overridden with by
     * setting the jmri.prefsdir Java property when starting JMRI.</li></ul> Use
     * {@link #getHomePath()} to get the User's home directory.
     *
     * @see #getHomePath()
     * @return Path to the preferences directory.
     */
    static public String getPreferencesPath() {
        // return jmri.prefsdir property if present
        String jmriPrefsDir = System.getProperty("jmri.prefsdir", ""); // NOI18N
        if (!jmriPrefsDir.isEmpty()) {
            return jmriPrefsDir + File.separator;
        }
        String result;
        switch (SystemType.getType()) {
            case SystemType.MACOSX:
                // Mac OS X
                result = FileUtil.getHomePath() + "Library" + File.separator + "Preferences" + File.separator + "JMRI" + File.separator; // NOI18N
                break;
            case SystemType.LINUX:
            case SystemType.UNIX:
                // Linux, so use an invisible file
                result = FileUtil.getHomePath() + ".jmri" + File.separator; // NOI18N
                break;
            case SystemType.WINDOWS:
            default:
                // Could be Windows, other
                result = FileUtil.getHomePath() + "JMRI" + File.separator; // NOI18N
                break;
        }
        if (log.isDebugEnabled()) {
            log.debug("preferencesPath defined as \"" + result + "\" based on os.name=\"" + SystemType.getOSName() + "\"");
        }
        return result;
    }

    /**
     * Get the JMRI program directory.
     *
     * @return JMRI program directory as a String.
     */
    static public String getProgramPath() {
        if (programPath == null) {
            FileUtil.setProgramPath("."); // NOI18N
        }
        return programPath;
    }

    /**
     * Set the JMRI program directory.
     *
     * Convenience method that calls
     * {@link FileUtil#setProgramPath(java.io.File)} with the passed in path.
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
        return FileUtil.findInputStream(path, new String[]{});
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
    static public InputStream findInputStream(String path, @NonNull String... searchPaths) {
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
     * Get the resources directory within the user's files directory.
     * 
     * @return path to [user's file]/resources/
     */
    static public String getUserResourcePath() {
        return FileUtil.getUserFilesPath() + "resources" + File.separator; // NOI18N
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
     * @see #findURL(java.lang.String, java.lang.String[])
     */
    static public URL findURL(String path) {
        return FileUtil.findURL(path, new String[]{});
    }

    /**
     * Search for a file or JAR resource by name and return the
     * {@link java.net.URL} for that file. <p> Search order is:<ol><li>For any
     * provided searchPaths, iterate over the searchPaths by prepending each
     * searchPath to the path and following the following search order:</li>
     * <ol><li>As a {@link java.io.File} in the user preferences directory</li>
     * <li>As a File in the current working directory (usually, but not always
     * the JMRI distribution directory)</li> <li>As a File in the JMRI
     * distribution directory</li> <li>As a resource in jmri.jar</li></ol>
     * <li>If the file or resource has not been found in the searchPaths, search
     * in the four locations listed without prepending any path</li></ol>
     *
     * @param path The relative path of the file or resource
     * @param searchPaths a list of paths to search for the path in
     * @return The URL or null
     * @see #findInputStream(java.lang.String)
     * @see #findInputStream(java.lang.String, java.lang.String[])
     * @see #findURL(java.lang.String)
     */
    static public URL findURL(String path, @NonNull String... searchPaths) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to find " + path + " in " + Arrays.toString(searchPaths));
        }
        URL resource;
        if (searchPaths != null) {
            for (String searchPath : searchPaths) {
                resource = FileUtil.findURL(searchPath + File.separator + path);
                if (resource != null) {
                    return resource;
                }
            }
        }
        try {
            // attempt to return path from preferences directory
            File file = new File(FileUtil.getUserFilesPath() + path);
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
            log.warn("Unable to get URL for " + path, ex);
            return null;
        }
        // return path if in jmri.jar or null
        resource = FileUtil.class.getClassLoader().getResource(path);
        if (resource == null && log.isDebugEnabled()) {
            log.debug("Unable to to get URL for " + path);
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
     * Return the {@link java.net.URL} for a given {@link java.io.File}. This
     * method catches a {@link java.net.MalformedURLException} and returns null
     * in its place, since we really do not expect a File object to ever give a
     * malformed URL. This method exists solely so implementing classes do not
     * need to catch that exception.
     *
     * @param file The File to convert.
     * @return a URL or null if the conversion would have caused a
     * MalformedURLException
     */
    static public URL fileToURL(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException ex) {
            log.error("Unable to get URL from file", ex);
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
        log.info("File path " + FileUtil.PREFERENCES + " is " + FileUtil.getUserFilesPath());
        log.info("File path " + FileUtil.HOME + " is " + FileUtil.getHomePath());
    }

    /**
     * Get the path to the scripts directory.
     *
     * @return the scriptsPath
     */
    public static String getScriptsPath() {
        if (scriptsPath != null) {
            return scriptsPath;
        }
        // scriptsPath not set by user, return default if it exists
        File file = new File(FileUtil.getProgramPath() + File.separator + "jython" + File.separator); // NOI18N
        if (file.exists()) {
            return file.getPath();
        }
        // if default does not exist, return user's files directory
        return FileUtil.getUserFilesPath();
    }

    /**
     * Set the path to python scripts.
     *
     * @param path the scriptsPath to set
     */
    public static void setScriptsPath(String path) {
        scriptsPath = path;
    }

    /**
     * Create a directory if required. Any parent directories will also be
     * created.
     *
     * @param path
     */
    public static void createDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            log.warn("Creating directory: " + path);
            if (!dir.mkdirs()) {
                log.error("Failed to create directory: " + path);
            }
        }
    }
}
