package jmri.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.jar.JarFile;
import javax.annotation.Nonnull;

/**
 * Common utility methods for working with Files.
 * <p>
 * All methods in this class call the identical method from the default instance
 * of {@link FileUtilSupport}.</p>
 *
 * @author Bob Jacobsen Copyright 2003, 2005, 2006
 * @author Randall Wood Copyright 2012, 2013, 2014, 2016
 * @see FileUtilSupport
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
     * @param path the path to find
     * @return {@link java.io.File} at path
     * @throws java.io.FileNotFoundException if path cannot be found
     * @see #getURI(java.lang.String)
     * @see #getURL(java.lang.String)
     */
    static public File getFile(String path) throws FileNotFoundException {
        return FileUtilSupport.getDefault().getFile(path);
    }

    /**
     * Get the {@link java.io.File} that path refers to. Throws a
     * {@link java.io.FileNotFoundException} if the file cannot be found instead
     * of returning null (as File would).
     *
     * @param path the path to find
     * @return {@link java.io.File} at path
     * @throws java.io.FileNotFoundException if path cannot be found
     * @see #getFile(java.lang.String)
     * @see #getURL(java.lang.String)
     */
    static public URI getURI(String path) throws FileNotFoundException {
        return FileUtilSupport.getDefault().getURI(path);
    }

    /**
     * Get the {@link java.net.URL} that path refers to. Throws a
     * {@link java.io.FileNotFoundException} if the URL cannot be found instead
     * of returning null.
     *
     * @param path the path to find
     * @return {@link java.net.URL} at path
     * @throws java.io.FileNotFoundException if path cannot be found
     * @see #getFile(java.lang.String)
     * @see #getURI(java.lang.String)
     */
    static public URL getURL(String path) throws FileNotFoundException {
        return FileUtilSupport.getDefault().getURL(path);
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
        return FileUtilSupport.getDefault().getURL(uri);
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
     * @return Absolute file name to use, or null. This will include
     *         system-specific file separators.
     * @since 2.7.2
     */
    static public String getExternalFilename(String pName) {
        return FileUtilSupport.getDefault().getExternalFilename(pName);
    }

    /**
     * Convert a portable filename into an absolute filename.
     *
     * @param path the portable filename
     * @return An absolute filename
     */
    static public String getAbsoluteFilename(String path) {
        return FileUtilSupport.getDefault().getAbsoluteFilename(path);
    }

    /**
     * Convert a File object's path to our preferred storage form.
     *
     * This is the inverse of {@link #getFile(String pName)}. Deprecated forms
     * are not created.
     *
     * @param file File at path to be represented
     * @return Filename for storage in a portable manner. This will include
     *         portable, not system-specific, file separators.
     * @since 2.7.2
     */
    static public String getPortableFilename(File file) {
        return FileUtilSupport.getDefault().getPortableFilename(file);
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
        return FileUtilSupport.getDefault().getPortableFilename(file, ignoreUserFilesPath, ignoreProfilePath);
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
        return FileUtilSupport.getDefault().getPortableFilename(filename);
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
        return FileUtilSupport.getDefault().getPortableFilename(filename, ignoreUserFilesPath, ignoreProfilePath);
    }

    /**
     * Test if the given filename is a portable filename.
     *
     * Note that this method may return a false positive if the filename is a
     * file: URL.
     *
     * @param filename the name to test
     * @return true if filename is portable
     */
    static public boolean isPortableFilename(String filename) {
        return FileUtilSupport.getDefault().isPortableFilename(filename);
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
     * <li>On Microsoft Windows systems, this is JMRI in the User's home
     * directory.</li>
     * <li>On OS X systems, this is Library/Preferences/JMRI in the User's home
     * directory.</li>
     * <li>On Linux, Solaris, and othe UNIXes, this is .jmri in the User's home
     * directory.</li>
     * <li>This can be overridden with by setting the jmri.prefsdir Java
     * property when starting JMRI.</li>
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
     * @param path the path to the JMRI installation
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
     * @param path the path to the JMRI installation
     */
    static public void setProgramPath(File path) {
        FileUtilSupport.getDefault().setProgramPath(path);
    }

    /**
     * Get the URL of a portable filename if it can be located using
     * {@link #findURL(java.lang.String)}
     *
     * @param path the path to find
     * @return URL of portable or absolute path
     */
    static public URI findExternalFilename(String path) {
        return FileUtilSupport.getDefault().findExternalFilename(path);
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
        return FileUtilSupport.getDefault().findInputStream(path);
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
        return FileUtilSupport.getDefault().findInputStream(path, searchPaths);
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
        return FileUtilSupport.getDefault().findInputStream(path, locations);
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
        return FileUtilSupport.getDefault().findInputStream(path, locations, searchPaths);
    }

    /**
     * Get the resources directory within the user's files directory.
     *
     * @return path to [user's file]/resources/
     */
    static public String getUserResourcePath() {
        return FileUtilSupport.getDefault().getUserResourcePath();
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
        return FileUtilSupport.getDefault().findURI(path);
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
        return FileUtilSupport.getDefault().findURI(path, searchPaths);
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
        return FileUtilSupport.getDefault().findURI(path, locations);
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
        return FileUtilSupport.getDefault().findURI(path, locations, searchPaths);
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
        return FileUtilSupport.getDefault().findURL(path);
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
        return FileUtilSupport.getDefault().findURL(path, searchPaths);
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
        return FileUtilSupport.getDefault().findURL(path, locations);
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
        return FileUtilSupport.getDefault().findURL(path, locations, searchPaths);
    }

    /**
     * Return the {@link java.net.URI} for a given URL
     *
     * @param url the URL
     * @return a URI or null if the conversion would have caused a
     *         {@link java.net.URISyntaxException}
     */
    static public URI urlToURI(URL url) {
        return FileUtilSupport.getDefault().urlToURI(url);
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
        return FileUtilSupport.getDefault().fileToURL(file);
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
     * @throws java.io.IOException if the file cannot be read
     */
    public static String readFile(File file) throws IOException {
        return FileUtil.readURL(FileUtil.fileToURL(file));
    }

    /**
     * Read a text URL into a String. Would be significantly simpler with Java
     * 7. File is assumed to be encoded using UTF-8
     *
     * @param url The text URL.
     * @return The contents of the file.
     * @throws java.io.IOException if the URL cannot be read
     */
    public static String readURL(URL url) throws IOException {
        return FileUtilSupport.getDefault().readURL(url);
    }

    /**
     * Replaces most non-alphanumeric characters in name with an underscore.
     *
     * @param name The filename to be sanitized.
     * @return The sanitized filename.
     */
    public static String sanitizeFilename(String name) {
        return FileUtilSupport.getDefault().sanitizeFilename(name);
    }

    /**
     * Create a directory if required. Any parent directories will also be
     * created.
     *
     * @param path directory to create
     */
    public static void createDirectory(String path) {
        FileUtilSupport.getDefault().createDirectory(path);
    }

    /**
     * Create a directory if required. Any parent directories will also be
     * created.
     *
     * @param dir directory to create
     */
    public static void createDirectory(File dir) {
        FileUtilSupport.getDefault().createDirectory(dir);
    }

    /**
     * Recursively delete a path. It is recommended to use
     * {@link java.nio.file.Files#delete(java.nio.file.Path)} or
     * {@link java.nio.file.Files#deleteIfExists(java.nio.file.Path)} for files.
     *
     * @param path path to delete
     * @return true if path was deleted, false otherwise
     */
    public static boolean delete(File path) {
        return FileUtilSupport.getDefault().delete(path);
    }

    /**
     * Copy a file or directory. It is recommended to use
     * {@link java.nio.file.Files#copy(java.nio.file.Path, java.io.OutputStream)}
     * for files.
     *
     * @param source the file or directory to copy
     * @param dest   must be the file or directory, not the containing directory
     * @throws java.io.IOException if file cannot be copied
     */
    public static void copy(File source, File dest) throws IOException {
        FileUtilSupport.getDefault().copy(source, dest);
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
        FileUtilSupport.getDefault().appendTextToFile(file, text);
    }

    /**
     * Backup a file. The backup is in the same location as the original file,
     * has the extension <code>.bak</code> appended to the file name, and up to
     * four revisions are retained. The lowest numbered revision is the most
     * recent.
     *
     * @param file the file to backup
     * @throws java.io.IOException if a backup cannot be created
     * @see jmri.util.FileUtilSupport#backup(java.io.File)
     */
    public static void backup(File file) throws IOException {
        FileUtilSupport.getDefault().backup(file);
    }

    /**
     * Rotate a file and its backups, retaining only a set number of backups.
     *
     * @param file      the file to rotate
     * @param max       maximum number of backups to retain
     * @param extension The extension to use for the rotations. If null or an
     *                  empty string, the rotation number is used as the
     *                  extension.
     * @throws java.io.IOException if a backup cannot be created
     * @throws IllegalArgumentException if max is less than one
     * @see jmri.util.FileUtilSupport#rotate(java.io.File, int,
     * java.lang.String)
     * @see jmri.util.FileUtilSupport#backup(java.io.File) 
     */
    public static void rotate(File file, int max, String extension) throws IOException {
        FileUtilSupport.getDefault().rotate(file, max, extension);
    }

    /* Private default constructor to ensure it's not documented. */
    private FileUtil() {
    }
}
