// FileUtil.java
package jmri.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utility methods for working with Files.
 * <P>
 * We needed a place to refactor common File-processing idioms in JMRI code, so
 * this class was created. It's more of a library of procedures than a real
 * class, as (so far) all of the operations have needed no state information.
 *
 * @author Bob Jacobsen Copyright 2003, 2005, 2006
 * @author Randall Wood Copyright 2012, 2013, 2014
 * @version $Revision$
 */
public final class FileUtil {

    /**
     * Portable reference to items in the JMRI program directory.
     */
    static public final String PROGRAM = "program:"; // NOI18N
    /**
     * Portable reference to the JMRI user's files and preferences directory.
     */
    static public final String PREFERENCES = "preference:"; // NOI18N
    /**
     * Portable reference to the JMRI applications preferences directory.
     */
    static public final String SETTINGS = "settings:"; // NOI18N
    /**
     * Portable reference to the user's home directory.
     */
    static public final String HOME = "home:"; // NOI18N
    /**
     * Portable reference to the current profile directory.
     */
    static public final String PROFILE = "profile:"; // NOI18N
    /**
     * Portable reference to the current scripts directory.
     */
    static public final String SCRIPTS = "scripts:"; // NOI18N
    /**
     * Replaced with {@link #PROGRAM}.
     *
     * @see #PROGRAM
     * @deprecated
     */
    @Deprecated
    static public final String RESOURCE = "resource:"; // NOI18N
    /**
     * Replaced with {@link #PREFERENCES}.
     *
     * @see #PREFERENCES
     * @deprecated
     */
    @Deprecated
    static public final String FILE = "file:"; // NOI18N
    /**
     * The portable file path component separator.
     */
    static public final char SEPARATOR = '/'; // NOI18N
    // initialize logging
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class.getName());

    /**
     * The types of locations to use when falling back on default locations in {@link #findURL(java.lang.String, java.lang.String...)
     * }.
     */
    static public enum Location {

        INSTALLED, USER, ALL, NONE
    }

    /**
     * Get the {@link java.io.File} that path refers to. Throws a
     * {@link java.io.FileNotFoundException} if the file cannot be found instead
     * of returning null (as File would). Use {@link #getURI(java.lang.String) }
     * or {@link #getURL(java.lang.String) } instead of this method if possible.
     *
     * @return {@link java.io.File} at path
     * @throws java.io.FileNotFoundException
     * @see #getURI(java.lang.String)
     * @see #getURL(java.lang.String)
     */
    static public File getFile(String path) throws FileNotFoundException {
        try {
            return new File(FileUtil.pathFromPortablePath(path));
        } catch (NullPointerException ex) {
            throw new FileNotFoundException("Cannot find file at " + path);
        }
    }

    /**
     * Get the {@link java.io.File} that path refers to. Throws a
     * {@link java.io.FileNotFoundException} if the file cannot be found instead
     * of returning null (as File would).
     *
     * @return {@link java.io.File} at path
     * @throws java.io.FileNotFoundException
     * @see #getFile(java.lang.String)
     * @see #getURL(java.lang.String)
     */
    static public URI getURI(String path) throws FileNotFoundException {
        return FileUtil.getFile(path).toURI();
    }

    /**
     * Get the {@link java.net.URL} that path refers to. Throws a
     * {@link java.io.FileNotFoundException} if the URL cannot be found instead
     * of returning null.
     *
     * @return {@link java.net.URL} at path
     * @throws FileNotFoundException
     * @see #getFile(java.lang.String)
     * @see #getURI(java.lang.String)
     */
    static public URL getURL(String path) throws FileNotFoundException {
        try {
            return FileUtil.getURI(path).toURL();
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("Cannot create URL for file at " + path);
        }
    }

    /**
     * Convenience method to get the {@link java.net.URL} from a
     * {@link java.net.URI}. Logs errors and returns null if any exceptions are
     * thrown by the conversion.
     *
     * @param uri The URI to convert.
     * @return URL or null if any errors exist.
     */
    static public URL getURL(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException | IllegalArgumentException ex) {
            log.warn("Unable to get URL from {}", uri.toString());
            return null;
        } catch (NullPointerException ex) {
            log.warn("Unable to get URL from null object.", ex);
            return null;
        }
    }
    /*
     * Get the canonical path for a portable path. There are nine cases:
     * <ul>
     * <li>Starts with "resource:", treat the rest as a pathname relative to the
     * program directory (deprecated; see "program:" below)</li>
     * <li>Starts with "program:", treat the rest as a relative pathname below
     * the program directory</li>
     * <li>Starts with "preference:", treat the rest as a relative path below
     * the user's files directory</li>
     * <li>Starts with "settings:", treat the rest as a relative path below the
     * JMRI system preferences directory</li>
     * <li>Starts with "home:", treat the rest as a relative path below the
     * user.home directory</li>
     * <li>Starts with "file:", treat the rest as a relative path below the
     * resource directory in the preferences directory (deprecated; see
     * "preference:" above)</li>
     * <li>Starts with "profile:", treat the rest as a relative path below the
     * profile directory as specified in the
     * active{@link jmri.profile.Profile}</li>
     * <li>Starts with "scripts:", treat the rest as a relative path below the
     * scripts directory</li>
     * <li>Otherwise, treat the name as a relative path below the program
     * directory</li>
     * </ul>
     * In any case, absolute pathnames will work.
     *
     * @param path The name string, possibly starting with file:, home:,
     * profile:, program:, preference:, scripts:, settings, or resource:
     * @return Canonical path to use, or null if one cannot be found.
     * @since 2.7.2
     */

    static private String pathFromPortablePath(@Nonnull String path) {
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
        } else if (path.startsWith(PROFILE)) {
            if (new File(path.substring(PROFILE.length())).isAbsolute()) {
                path = path.substring(PROFILE.length());
            } else {
                path = path.replaceFirst(PROFILE, Matcher.quoteReplacement(FileUtil.getProfilePath()));
            }
        } else if (path.startsWith(SCRIPTS)) {
            if (new File(path.substring(SCRIPTS.length())).isAbsolute()) {
                path = path.substring(SCRIPTS.length());
            } else {
                path = path.replaceFirst(SCRIPTS, Matcher.quoteReplacement(FileUtil.getScriptsPath()));
            }
        } else if (path.startsWith(SETTINGS)) {
            if (new File(path.substring(SETTINGS.length())).isAbsolute()) {
                path = path.substring(SETTINGS.length());
            } else {
                path = path.replaceFirst(SETTINGS, Matcher.quoteReplacement(FileUtil.getPreferencesPath()));
            }
        } else if (path.startsWith(HOME)) {
            if (new File(path.substring(HOME.length())).isAbsolute()) {
                path = path.substring(HOME.length());
            } else {
                path = path.replaceFirst(HOME, Matcher.quoteReplacement(FileUtil.getHomePath()));
            }
        } else if (path.startsWith(RESOURCE)) {
            if (new File(path.substring(RESOURCE.length())).isAbsolute()) {
                path = path.substring(RESOURCE.length());
            } else {
                path = path.replaceFirst(RESOURCE, Matcher.quoteReplacement(FileUtil.getProgramPath()));
            }
        } else if (path.startsWith(FILE)) {
            if (new File(path.substring(FILE.length())).isAbsolute()) {
                path = path.substring(FILE.length());
            } else {
                path = path.replaceFirst(FILE, Matcher.quoteReplacement(FileUtil.getUserFilesPath() + "resources" + File.separator));
            }
        } else if (!new File(path).isAbsolute()) {
            return null;
        }
        try {
            // if path cannot be converted into a canonical path, return null
            log.debug("Using {}", path);
            return new File(path.replace(SEPARATOR, File.separatorChar)).getCanonicalPath();
        } catch (IOException ex) {
            log.warn("Cannot convert {} into a usable filename.", path, ex);
            return null;
        }
    }

    /**
     * Get the resource file corresponding to a name. There are five cases:
     * <ul>
     * <li>Starts with "resource:", treat the rest as a pathname relative to the
     * program directory (deprecated; see "program:" below)</li>
     * <li>Starts with "program:", treat the rest as a relative pathname below
     * the program directory</li>
     * <li>Starts with "preference:", treat the rest as a relative path below
     * the user's files directory</li>
     * <li>Starts with "settings:", treat the rest as a relative path below the
     * JMRI system preferences directory</li>
     * <li>Starts with "home:", treat the rest as a relative path below the
     * user.home directory</li>
     * <li>Starts with "file:", treat the rest as a relative path below the
     * resource directory in the preferences directory (deprecated; see
     * "preference:" above)</li>
     * <li>Starts with "profile:", treat the rest as a relative path below the
     * profile directory as specified in the
     * active{@link jmri.profile.Profile}</li>
     * <li>Starts with "scripts:", treat the rest as a relative path below the
     * scripts directory</li>
     * <li>Otherwise, treat the name as a relative path below the program
     * directory</li>
     * </ul>
     * In any case, absolute pathnames will work.
     *
     * @param pName The name string, possibly starting with file:, home:,
     *              profile:, program:, preference:, scripts:, settings, or
     *              resource:
     * @return Absolute file name to use, or null. This will include system-specific file separators.
     * @since 2.7.2
     */
    static public String getExternalFilename(String pName) {
        String filename = FileUtil.pathFromPortablePath(pName);
        return (filename != null) ? filename : pName.replace(SEPARATOR, File.separatorChar);
    }

    /**
     * Convert a portable filename into an absolute filename.
     *
     * @return An absolute filename
     */
    static public String getAbsoluteFilename(String path) {
        return FileUtil.pathFromPortablePath(path);
    }

    /**
     * Convert a File object's path to our preferred storage form.
     *
     * This is the inverse of {@link #getFile(String pName)}. Deprecated forms
     * are not created.
     *
     * @param file File at path to be represented
     * @return Filename for storage in a portable manner. This will include portable, not system-specific, file separators.
     * @since 2.7.2
     */
    static public String getPortableFilename(File file) {
        return FileUtil.getPortableFilename(file, false, false);
    }

    /**
     * Convert a File object's path to our preferred storage form.
     *
     * This is the inverse of {@link #getFile(String pName)}. Deprecated forms
     * are not created.
     *
     * This method supports a specific use case concerning profiles and other
     * portable paths that are stored within the User files directory, which
     * will cause the {@link jmri.profile.ProfileManager} to write an incorrect
     * path for the current profile or
     * {@link apps.configurexml.FileLocationPaneXml} to write an incorrect path
     * for the Users file directory. In most cases, the use of
     * {@link #getPortableFilename(java.io.File)} is preferable.
     *
     * @param file                File at path to be represented
     * @param ignoreUserFilesPath true if paths in the User files path should be
     *                            stored as absolute paths, which is often not
     *                            desirable.
     * @param ignoreProfilePath   true if paths in the profile should be stored
     *                            as absolute paths, which is often not
     *                            desirable.
     * @return Storage format representation
     * @since 3.5.5
     */
    static public String getPortableFilename(File file, boolean ignoreUserFilesPath, boolean ignoreProfilePath) {
        // compare full path name to see if same as preferences
        String filename = file.getAbsolutePath();

        // append separator if file is a directory
        if (file.isDirectory()) {
            filename = filename + File.separator;
        }

        // compare full path name to see if same as preferences
        if (!ignoreUserFilesPath) {
            if (filename.startsWith(getUserFilesPath())) {
                return PREFERENCES + filename.substring(getUserFilesPath().length(), filename.length()).replace(File.separatorChar, SEPARATOR);
            }
        }

        if (!ignoreProfilePath) {
            // compare full path name to see if same as profile
            if (filename.startsWith(getProfilePath())) {
                return PROFILE + filename.substring(getProfilePath().length(), filename.length()).replace(File.separatorChar, SEPARATOR);
            }
        }

        // compare full path name to see if same as settings
        if (filename.startsWith(getPreferencesPath())) {
            return SETTINGS + filename.substring(getPreferencesPath().length(), filename.length()).replace(File.separatorChar, SEPARATOR);
        }

        if (!ignoreUserFilesPath) {
            /*
             * The tests for any portatable path that could be within the
             * UserFiles locations needs to be within this block. This prevents
             * the UserFiles or Profile path from being set to another portable
             * path that is user settable.
             *
             * Note that this test should be after the UserFiles, Profile, and
             * Preferences tests.
             */
            // check for relative to scripts dir
            if (filename.startsWith(getScriptsPath()) && !filename.equals(getScriptsPath())) {
                return SCRIPTS + filename.substring(getScriptsPath().length(), filename.length()).replace(File.separatorChar, SEPARATOR);
            }
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
     * @return Filename for storage in a portable manner
     * @since 2.7.2
     */
    static public String getPortableFilename(String filename) {
        return FileUtil.getPortableFilename(filename, false, false);
    }

    /**
     * Convert a filename string to our preferred storage form.
     *
     * This is the inverse of {@link #getExternalFilename(String pName)}.
     * Deprecated forms are not created.
     *
     * This method supports a specific use case concerning profiles and other
     * portable paths that are stored within the User files directory, which
     * will cause the {@link jmri.profile.ProfileManager} to write an incorrect
     * path for the current profile or
     * {@link apps.configurexml.FileLocationPaneXml} to write an incorrect path
     * for the Users file directory. In most cases, the use of
     * {@link #getPortableFilename(java.io.File)} is preferable.
     *
     * @param filename            Filename to be represented
     * @param ignoreUserFilesPath true if paths in the User files path should be
     *                            stored as absolute paths, which is often not
     *                            desirable.
     * @param ignoreProfilePath   true if paths in the profile path should be
     *                            stored as absolute paths, which is often not
     *                            desirable.
     * @return Storage format representation
     * @since 3.5.5
     */
    static public String getPortableFilename(String filename, boolean ignoreUserFilesPath, boolean ignoreProfilePath) {
        if (FileUtil.isPortableFilename(filename)) {
            // if this already contains prefix, run through conversion to normalize
            return getPortableFilename(getExternalFilename(filename), ignoreUserFilesPath, ignoreProfilePath);
        } else {
            // treat as pure filename
            return getPortableFilename(new File(filename), ignoreUserFilesPath, ignoreProfilePath);
        }
    }

    /**
     * Test if the given filename is a portable filename.
     *
     * Note that this method may return a false positive if the filename is a
     * file: URL.
     *
     * @return true if filename is portable
     */
    static public boolean isPortableFilename(String filename) {
        return (filename.startsWith(PROGRAM)
                || filename.startsWith(HOME)
                || filename.startsWith(PREFERENCES)
                || filename.startsWith(SCRIPTS)
                || filename.startsWith(PROFILE)
                || filename.startsWith(SETTINGS)
                || filename.startsWith(FILE)
                || filename.startsWith(RESOURCE));
    }

    /**
     * Get the user's home directory.
     *
     * @return User's home directory as a String
     */
    static public String getHomePath() {
        return FileUtilSupport.getDefault().getHomePath();
    }

    /**
     * Get the user's files directory. If not set by the user, this is the same
     * as the profile path.
     *
     * @see #getProfilePath()
     * @return User's files directory as a String
     */
    static public String getUserFilesPath() {
        return FileUtilSupport.getDefault().getUserFilesPath();
    }

    /**
     * Set the user's files directory.
     *
     * @see #getUserFilesPath()
     * @param path The path to the user's files directory
     */
    static public void setUserFilesPath(String path) {
        FileUtilSupport.getDefault().setUserFilesPath(path);
    }

    /**
     * Get the profile directory. If not set, this is the same as the
     * preferences path.
     *
     * @see #getPreferencesPath()
     * @return Profile directory as a String
     */
    static public String getProfilePath() {
        return FileUtilSupport.getDefault().getProfilePath();
    }

    /**
     * Set the profile directory.
     *
     * @see #getProfilePath()
     * @param path The path to the profile directory
     */
    static public void setProfilePath(String path) {
        FileUtilSupport.getDefault().setProfilePath(path);
    }

    /**
     * Get the preferences directory. This directory is set based on the OS and
     * is not normally settable by the user. 
     * <ul>
     * <li>On Microsoft Windows
     * systems, this is JMRI in the User's home directory.</li>
     * <li>On OS X
     * systems, this is Library/Preferences/JMRI in the User's home
     * directory.</li> 
     * <li>On Linux, Solaris, and othe UNIXes, this is .jmri in
     * the User's home directory.</li> 
     * <li>This can be overridden with by
     * setting the jmri.prefsdir Java property when starting JMRI.</li>
     * </ul>
     * Use {@link #getHomePath()} to get the User's home directory.
     *
     * @see #getHomePath()
     * @return Path to the preferences directory.
     */
    static public String getPreferencesPath() {
        return FileUtilSupport.getDefault().getPreferencesPath();
    }

    /**
     * Get the JMRI program directory.
     *
     * @return JMRI program directory as a String.
     */
    static public String getProgramPath() {
        return FileUtilSupport.getDefault().getProgramPath();
    }

    /**
     * Set the JMRI program directory.
     *
     * Convenience method that calls
     * {@link FileUtil#setProgramPath(java.io.File)} with the passed in path.
     *
     */
    static public void setProgramPath(String path) {
        FileUtilSupport.getDefault().setProgramPath(new File(path));
    }

    /**
     * Set the JMRI program directory.
     *
     * If set, allows JMRI to be loaded from locations other than the directory
     * containing JMRI resources. This must be set very early in the process of
     * loading JMRI (prior to loading any other JMRI code) to be meaningfully
     * used.
     *
     */
    static public void setProgramPath(File path) {
        FileUtilSupport.getDefault().setProgramPath(path);
    }

    /**
     * Get the URL of a portable filename if it can be located using
     * {@link #findURL(java.lang.String)}
     *
     * @return URL of portable or absolute path
     */
    static public URI findExternalFilename(String path) {
        log.debug("Finding external path {}", path);
        if (FileUtil.isPortableFilename(path)) {
            int index = path.indexOf(":") + 1;
            String location = path.substring(0, index);
            path = path.substring(index);
            log.debug("Finding {} and {}", location, path);
            switch (location) {
                case FileUtil.PROGRAM:
                case FileUtil.RESOURCE:
                    return FileUtil.findURI(path, Location.INSTALLED);
                case FileUtil.PREFERENCES:
                case FileUtil.FILE:
                    return FileUtil.findURI(path, Location.USER);
                case FileUtil.PROFILE:
                case FileUtil.SETTINGS:
                case FileUtil.SCRIPTS:
                case FileUtil.HOME:
                    return FileUtil.findURI(FileUtil.getExternalFilename(location + path));
                default:
                    break;
            }
        }
        return FileUtil.findURI(path, Location.ALL);
    }

    /**
     * Search for a file or JAR resource by name and return the
     * {@link java.io.InputStream} for that file. Search order is defined by
     * {@link #findURL(java.lang.String, jmri.util.FileUtil.Location, java.lang.String...) }.
     * No limits are placed on search locations.
     *
     * @param path The relative path of the file or resource
     * @return InputStream or null.
     * @see #findInputStream(java.lang.String, java.lang.String...)
     * @see #findInputStream(java.lang.String, jmri.util.FileUtil.Location,
     * java.lang.String...)
     * @see #findURL(java.lang.String)
     * @see #findURL(java.lang.String, java.lang.String...)
     * @see #findURL(java.lang.String, jmri.util.FileUtil.Location,
     * java.lang.String...)
     */
    static public InputStream findInputStream(String path) {
        return FileUtil.findInputStream(path, new String[]{});
    }

    /**
     * Search for a file or JAR resource by name and return the
     * {@link java.io.InputStream} for that file. Search order is defined by
     * {@link #findURL(java.lang.String, jmri.util.FileUtil.Location, java.lang.String...) }.
     * No limits are placed on search locations.
     *
     * @param path        The relative path of the file or resource
     * @param searchPaths a list of paths to search for the path in
     * @return InputStream or null.
     * @see #findInputStream(java.lang.String)
     * @see #findInputStream(java.lang.String, jmri.util.FileUtil.Location,
     * java.lang.String...)
     */
    static public InputStream findInputStream(String path, @Nonnull String... searchPaths) {
        return FileUtil.findInputStream(path, Location.ALL, searchPaths);
    }

    /**
     * Search for a file or JAR resource by name and return the
     * {@link java.io.InputStream} for that file. Search order is defined by
     * {@link #findURL(java.lang.String, jmri.util.FileUtil.Location, java.lang.String...) }.
     *
     * @param path      The relative path of the file or resource
     * @param locations The type of locations to limit the search to
     * @return InputStream or null.
     * @see #findInputStream(java.lang.String)
     * @see #findInputStream(java.lang.String, jmri.util.FileUtil.Location,
     * java.lang.String...)
     */
    static public InputStream findInputStream(String path, Location locations) {
        return FileUtil.findInputStream(path, locations, new String[]{});
    }

    /**
     * Search for a file or JAR resource by name and return the
     * {@link java.io.InputStream} for that file. Search order is defined by
     * {@link #findURL(java.lang.String, jmri.util.FileUtil.Location, java.lang.String...) }.
     *
     * @param path        The relative path of the file or resource
     * @param locations   The type of locations to limit the search to
     * @param searchPaths a list of paths to search for the path in
     * @return InputStream or null.
     * @see #findInputStream(java.lang.String)
     * @see #findInputStream(java.lang.String, java.lang.String...)
     */
    static public InputStream findInputStream(String path, Location locations, @Nonnull String... searchPaths) {
        URL file = FileUtil.findURL(path, locations, searchPaths);
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
     * {@link java.net.URI} for that file. Search order is defined by
     * {@link #findURI(java.lang.String, jmri.util.FileUtil.Location, java.lang.String...)}.
     * No limits are placed on search locations.
     *
     * @param path The relative path of the file or resource.
     * @return The URI or null.
     * @see #findURI(java.lang.String, java.lang.String...)
     * @see #findURI(java.lang.String, jmri.util.FileUtil.Location)
     * @see #findURI(java.lang.String, jmri.util.FileUtil.Location,
     * java.lang.String...)
     */
    static public URI findURI(String path) {
        return FileUtil.findURI(path, new String[]{});
    }

    /**
     * Search for a file or JAR resource by name and return the
     * {@link java.net.URI} for that file. Search order is defined by
     * {@link #findURI(java.lang.String, jmri.util.FileUtil.Location, java.lang.String...)}.
     * No limits are placed on search locations.
     *
     * Note that if the file for path is not found in one of the searchPaths,
     * all standard locations are also be searched through to find the file. If
     * you need to limit the locations where the file can be found use
     * {@link #findURI(java.lang.String, jmri.util.FileUtil.Location, java.lang.String...)}.
     *
     * @param path        The relative path of the file or resource
     * @param searchPaths a list of paths to search for the path in
     * @return The URI or null
     * @see #findURI(java.lang.String)
     * @see #findURI(java.lang.String, jmri.util.FileUtil.Location)
     * @see #findURI(java.lang.String, jmri.util.FileUtil.Location,
     * java.lang.String...)
     */
    static public URI findURI(String path, @Nonnull String... searchPaths) {
        return FileUtil.findURI(path, Location.ALL, searchPaths);
    }

    /**
     * Search for a file or JAR resource by name and return the
     * {@link java.net.URI} for that file. Search order is defined by
     * {@link #findURI(java.lang.String, jmri.util.FileUtil.Location, java.lang.String...)}.
     *
     * @param path      The relative path of the file or resource
     * @param locations The types of locations to limit the search to
     * @return The URI or null
     * @see #findURI(java.lang.String)
     * @see #findURI(java.lang.String, java.lang.String...)
     * @see #findURI(java.lang.String, jmri.util.FileUtil.Location,
     * java.lang.String...)
     */
    static public URI findURI(String path, Location locations) {
        return FileUtil.findURI(path, locations, new String[]{});
    }

    /**
     * Search for a file or JAR resource by name and return the
     * {@link java.net.URI} for that file.
     * <p>
     * Search order is:
     * <ol><li>For any provided searchPaths, iterate over the searchPaths by
     * prepending each searchPath to the path and following the following search
     * order:
     * <ol><li>As a {@link java.io.File} in the user preferences directory</li>
     * <li>As a File in the current working directory (usually, but not always
     * the JMRI distribution directory)</li> <li>As a File in the JMRI
     * distribution directory</li> <li>As a resource in jmri.jar</li></ol></li>
     * <li>If the file or resource has not been found in the searchPaths, search
     * in the four locations listed without prepending any path</li></ol>
     * <p>
     * The <code>locations</code> parameter limits the above logic by limiting
     * the location searched.
     * <ol><li>{@link Location#ALL} will not place any limits on the
     * search</li><li>{@link Location#NONE} effectively requires that
     * <code>path</code> be a portable
     * pathname</li><li>{@link Location#INSTALLED} limits the search to the
     * {@link #PROGRAM} directory and JARs in the class
     * path</li><li>{@link Location#USER} limits the search to the
     * {@link #PROFILE} directory</li></ol>
     *
     * @param path        The relative path of the file or resource
     * @param locations   The types of locations to limit the search to
     * @param searchPaths a list of paths to search for the path in
     * @return The URI or null
     * @see #findURI(java.lang.String)
     * @see #findURI(java.lang.String, jmri.util.FileUtil.Location)
     * @see #findURI(java.lang.String, java.lang.String...)
     */
    static public URI findURI(String path, Location locations, @Nonnull String... searchPaths) {
        if (log.isDebugEnabled()) { // avoid the Arrays.toString call unless debugging
            log.debug("Attempting to find {} in {}", path, Arrays.toString(searchPaths));
        }
        if (FileUtil.isPortableFilename(path)) {
            try {
                return FileUtil.findExternalFilename(path);
            } catch (NullPointerException ex) {
                // do nothing
            }
        }
        URI resource = null;
        for (String searchPath : searchPaths) {
            resource = FileUtil.findURI(searchPath + File.separator + path);
            if (resource != null) {
                return resource;
            }
        }
        File file;
        if (locations == Location.ALL || locations == Location.USER) {
            // attempt to return path from preferences directory
            file = new File(FileUtil.getUserFilesPath() + path);
            if (file.exists()) {
                return file.toURI();
            }
        }
        if (locations == Location.ALL || locations == Location.INSTALLED) {
            // attempt to return path from current working directory
            file = new File(path);
            if (file.exists()) {
                return file.toURI();
            }
            // attempt to return path from JMRI distribution directory
            file = new File(FileUtil.getProgramPath() + path);
            if (file.exists()) {
                return file.toURI();
            }
        }
        if (locations == Location.ALL || locations == Location.INSTALLED) {
            // return path if in jmri.jar or null
            // The ClassLoader needs paths to use /
            path = path.replace(File.separatorChar, '/');
            URL url = FileUtil.class.getClassLoader().getResource(path);
            if (url == null) {
                url = FileUtil.class.getResource(path);
                if (url == null) {
                    log.debug("{} not found in classpath", path);
                }
            }
            try {
                resource = (url != null) ? url.toURI() : null;
            } catch (URISyntaxException ex) {
                log.warn("Unable to get URI for {}", path, ex);
                return null;
            }
        }
        return resource;
    }

    /**
     * Search for a file or JAR resource by name and return the
     * {@link java.net.URL} for that file. Search order is defined by
     * {@link #findURL(java.lang.String, jmri.util.FileUtil.Location, java.lang.String...)}.
     * No limits are placed on search locations.
     *
     * @param path The relative path of the file or resource.
     * @return The URL or null.
     * @see #findURL(java.lang.String, java.lang.String...)
     * @see #findURL(java.lang.String, jmri.util.FileUtil.Location)
     * @see #findURL(java.lang.String, jmri.util.FileUtil.Location,
     * java.lang.String...)
     */
    static public URL findURL(String path) {
        return FileUtil.findURL(path, new String[]{});
    }

    /**
     * Search for a file or JAR resource by name and return the
     * {@link java.net.URL} for that file. Search order is defined by
     * {@link #findURL(java.lang.String, jmri.util.FileUtil.Location, java.lang.String...)}.
     * No limits are placed on search locations.
     *
     * @param path        The relative path of the file or resource
     * @param searchPaths a list of paths to search for the path in
     * @return The URL or null
     * @see #findURL(java.lang.String)
     * @see #findURL(java.lang.String, jmri.util.FileUtil.Location)
     * @see #findURL(java.lang.String, jmri.util.FileUtil.Location,
     * java.lang.String...)
     */
    static public URL findURL(String path, @Nonnull String... searchPaths) {
        return FileUtil.findURL(path, Location.ALL, searchPaths);
    }

    /**
     * Search for a file or JAR resource by name and return the
     * {@link java.net.URL} for that file. Search order is defined by
     * {@link #findURL(java.lang.String, jmri.util.FileUtil.Location, java.lang.String...)}.
     *
     * @param path      The relative path of the file or resource
     * @param locations The types of locations to limit the search to
     * @return The URL or null
     * @see #findURL(java.lang.String)
     * @see #findURL(java.lang.String, java.lang.String...)
     * @see #findURL(java.lang.String, jmri.util.FileUtil.Location,
     * java.lang.String...)
     */
    static public URL findURL(String path, Location locations) {
        return FileUtil.findURL(path, locations, new String[]{});
    }

    /**
     * Search for a file or JAR resource by name and return the
     * {@link java.net.URL} for that file.
     * <p>
     * Search order is:
     * <ol><li>For any provided searchPaths, iterate over the searchPaths by
     * prepending each searchPath to the path and following the following search
     * order:
     * <ol><li>As a {@link java.io.File} in the user preferences directory</li>
     * <li>As a File in the current working directory (usually, but not always
     * the JMRI distribution directory)</li> <li>As a File in the JMRI
     * distribution directory</li> <li>As a resource in jmri.jar</li></ol></li>
     * <li>If the file or resource has not been found in the searchPaths, search
     * in the four locations listed without prepending any path</li></ol>
     * <p>
     * The <code>locations</code> parameter limits the above logic by limiting
     * the location searched.
     * <ol><li>{@link Location#ALL} will not place any limits on the
     * search</li><li>{@link Location#NONE} effectively requires that
     * <code>path</code> be a portable
     * pathname</li><li>{@link Location#INSTALLED} limits the search to the
     * {@link #PROGRAM} directory and JARs in the class
     * path</li><li>{@link Location#USER} limits the search to the
     * {@link #PROFILE} directory</li></ol>
     *
     * @param path        The relative path of the file or resource
     * @param locations   The types of locations to limit the search to
     * @param searchPaths a list of paths to search for the path in
     * @return The URL or null
     * @see #findURL(java.lang.String)
     * @see #findURL(java.lang.String, jmri.util.FileUtil.Location)
     * @see #findURL(java.lang.String, java.lang.String...)
     */
    static public URL findURL(String path, Location locations, @Nonnull String... searchPaths) {
        URI file = FileUtil.findURI(path, locations, searchPaths);
        if (file != null) {
            try {
                return file.toURL();
            } catch (MalformedURLException ex) {
                log.error(ex.getLocalizedMessage(), ex);
            }
        }
        return null;
    }

    /**
     * Return the {@link java.net.URI} for a given URL
     *
     * @return a URI or null if the conversion would have caused a
     *         {@link java.net.URISyntaxException}
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
     *         MalformedURLException
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
        return FileUtilSupport.getDefault().getJmriJarFile();
    }

    /**
     * Log all paths at the INFO level.
     */
    static public void logFilePaths() {
        FileUtilSupport.getDefault().logFilePaths();
    }

    /**
     * Get the path to the scripts directory.
     *
     * @return the scriptsPath
     */
    public static String getScriptsPath() {
        return FileUtilSupport.getDefault().getScriptsPath();
    }

    /**
     * Set the path to python scripts.
     *
     * @param path the scriptsPath to set
     */
    public static void setScriptsPath(String path) {
        FileUtilSupport.getDefault().setScriptsPath(path);
    }

    /**
     * Read a text file into a String.
     *
     * @param file The text file.
     * @return The contents of the file.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String readFile(File file) throws IOException {
        return FileUtil.readURL(FileUtil.fileToURL(file));
    }

    /**
     * Read a text URL into a String. Would be significantly simpler with Java 7.
     * File is assumed to be encoded using UTF-8
     *
     * @param url The text URL.
     * @return The contents of the file.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String readURL(URL url) throws IOException {
        try {
            StringBuilder builder;
            try (InputStreamReader in = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
                    BufferedReader reader = new BufferedReader(in)) {
                builder = new StringBuilder();
                String aux;
                while ((aux = reader.readLine()) != null) {
                    builder.append(aux);
                }
            }
            return builder.toString();
        } catch (NullPointerException ex) {
            return null;
        }
    }

    /**
     * Replaces most non-alphanumeric characters in name with an underscore.
     *
     * @param name The filename to be sanitized.
     * @return The sanitized filename.
     */
    public static String sanitizeFilename(String name) {
        name = name.trim().replaceAll(" ", "_").replaceAll("[.]+", ".");
        StringBuilder filename = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (c == '.' || Character.isJavaIdentifierPart(c)) {
                filename.append(c);
            }
        }
        return filename.toString();
    }

    /**
     * Create a directory if required. Any parent directories will also be
     * created.
     *
     */
    public static void createDirectory(String path) {
        FileUtil.createDirectory(new File(path));
    }

    /**
     * Create a directory if required. Any parent directories will also be
     * created.
     *
     */
    public static void createDirectory(File dir) {
        if (!dir.exists()) {
            log.info("Creating directory: {}", dir);
            if (!dir.mkdirs()) {
                log.error("Failed to create directory: {}", dir);
            }
        }
    }
    
    /**
     * Recursively delete a path. It is recommended to use
     * {@link java.nio.file.Files#delete(java.nio.file.Path)} or
     * {@link java.nio.file.Files#deleteIfExists(java.nio.file.Path)} for files.
     *
     * @return true if path was deleted, false otherwise
     */
    @SuppressFBWarnings(value="NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification="listFiles() is documented to return null only if isDirectory() is false")
    public static boolean delete(File path) {
        if (path.isDirectory()) {
            for (File file : path.listFiles()) {
                FileUtil.delete(file);
            }
        }
        return path.delete();
    }

    /**
     * Copy a file or directory. It is recommended to use
     * {@link java.nio.file.Files#copy(java.nio.file.Path, java.io.OutputStream)}
     * for files.
     *
     * @param dest   must be the file, not the destination directory.
     * @throws IOException
     */
    public static void copy(File source, File dest) throws IOException {
        if (!source.exists()) {
            log.error("Attempting to copy non-existant file: {}", source);
            return;
        }
        if (!dest.exists()) {
            if (source.isDirectory()) {
                boolean ok = dest.mkdirs();
                if (!ok) {
                    throw new IOException("Could not use mkdirs to create destination directory");
                }
            } else {
                boolean ok = dest.createNewFile();
                if (!ok) {
                    throw new IOException("Could not create destination file");
                }
            }
        }
        Path srcPath = source.toPath();
        Path dstPath = dest.toPath();
        if (source.isDirectory()) {
            Files.walkFileTree(srcPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(final Path dir,
                        final BasicFileAttributes attrs) throws IOException {
                    Files.createDirectories(dstPath.resolve(srcPath.relativize(dir)));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path file,
                        final BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, dstPath.resolve(srcPath.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Simple helper method to just append a text string to the end of the given
     * filename. The file will be created if it does not exist.
     *
     * @param file File to append text to
     * @param text Text to append
     * @throws java.io.IOException if file cannot be written to
     */
    public static void appendTextToFile(File file, String text) throws IOException {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            pw.println(text);
        }
    }

    /**
     * Backup a file.
     *
     * @throws java.io.IOException 
     * @see jmri.util.FileUtilSupport#backup(java.io.File) 
     */
    public static void backup(File file) throws IOException {
        FileUtilSupport.getDefault().backup(file);
    }
    
    /**
     * Rotate a file
     * @param extension 
     * @throws java.io.IOException 
     * @see jmri.util.FileUtilSupport#rotate(java.io.File, int, java.lang.String) 
     * @see backup
     */
    public static void rotate(File file, int max, String extension) throws IOException {
        FileUtilSupport.getDefault().rotate(file, max, extension);
    }
    
    /* Private default constructor to ensure it's not documented. */
    private FileUtil() {
    }
}
