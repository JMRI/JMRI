package jmri.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;

import jmri.profile.Profile;
import jmri.profile.ProfileManager;

/**
 * Common utility methods for working with Files.
 * <p>
 * All methods in this class call the identical method from the default instance
 * of {@link FileUtilSupport}.
 *
 * @author Bob Jacobsen Copyright 2003, 2005, 2006
 * @author Randall Wood Copyright 2012, 2013, 2014, 2016, 2019
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
     * The portable file path component separator.
     */
    static public final char SEPARATOR = '/'; // NOI18N

    /**
     * The types of locations to use when falling back on default locations in
     * {@link #findURI(java.lang.String, jmri.util.FileUtil.Location, java.lang.String...)}.
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
    @Nonnull
    @CheckReturnValue
    static public File getFile(@Nonnull String path) throws FileNotFoundException {
        return FileUtilSupport.getDefault().getFile(path);
    }

    /**
     * Get the {@link java.io.File} that path refers to. Throws a
     * {@link java.io.FileNotFoundException} if the file cannot be found instead
     * of returning null (as File would). Use {@link #getURI(java.lang.String) }
     * or {@link #getURL(java.lang.String) } instead of this method if possible.
     *
     * @param profile the profile to use as a base
     * @param path    the path to find
     * @return {@link java.io.File} at path
     * @throws java.io.FileNotFoundException if path cannot be found
     * @see #getURI(java.lang.String)
     * @see #getURL(java.lang.String)
     */
    @Nonnull
    @CheckReturnValue
    static public File getFile(@CheckForNull Profile profile, @Nonnull String path) throws FileNotFoundException {
        return FileUtilSupport.getDefault().getFile(profile, path);
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
    @Nonnull
    @CheckReturnValue
    static public URI getURI(@Nonnull String path) throws FileNotFoundException {
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
    @Nonnull
    @CheckReturnValue
    static public URL getURL(@Nonnull String path) throws FileNotFoundException {
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
    @CheckForNull
    @CheckReturnValue
    static public URL getURL(@Nonnull URI uri) {
        return FileUtilSupport.getDefault().getURL(uri);
    }

    /**
     * Find all files matching the given name under the given root directory
     * within both the user and installed file locations.
     *
     * @param name the name of the file to find
     * @param root the relative path to a directory in either or both of the
     *             user or installed file locations; use a single period
     *             character to refer to the root of the user or installed file
     *             locations
     * @return a set of found files or an empty set if no matching files were
     *         found
     * @throws IllegalArgumentException if the name is not a relative path, is
     *                                  empty, or contains path separators; or
     *                                  if the root is not a relative path, is
     *                                  empty, or contains a parent directory
     *                                  (..)
     * @throws NullPointerException     if any parameter is null
     */
    @Nonnull
    @CheckReturnValue
    static public Set<File> findFiles(@Nonnull String name, @Nonnull String root) throws IllegalArgumentException {
        return FileUtilSupport.getDefault().findFiles(name, root);
    }

    /**
     * Find all files matching the given name under the given root directory
     * within the specified location.
     *
     * @param name     the name of the file to find
     * @param root     the relative path to a directory in either or both of the
     *                 user or installed file locations; use a single period
     *                 character to refer to the root of the location
     * @param location the location to search within
     * @return a set of found files or an empty set if no matching files were
     *         found
     * @throws IllegalArgumentException if the name is not a relative path, is
     *                                  empty, or contains path separators; if
     *                                  the root is not a relative path, is
     *                                  empty, or contains a parent directory
     *                                  (..); or if the location is
     *                                  {@link Location#NONE}
     * @throws NullPointerException     if any parameter is null
     */
    @Nonnull
    @CheckReturnValue
    static public Set<File> findFiles(@Nonnull String name, @Nonnull String root, @Nonnull Location location) {
        return FileUtilSupport.getDefault().findFiles(name, root, location);
    }

    /**
     * Get the resource file corresponding to a name. There are five cases:
     * <ul>
     * <li>Starts with "program:", treat the rest as a relative pathname below
     * the program directory</li>
     * <li>Starts with "preference:", treat the rest as a relative path below
     * the user's files directory</li>
     * <li>Starts with "settings:", treat the rest as a relative path below the
     * JMRI system preferences directory</li>
     * <li>Starts with "home:", treat the rest as a relative path below the
     * user.home directory</li>
     * <li>Starts with "profile:", treat the rest as a relative path below the
     * profile directory as specified in the
     * active{@link jmri.profile.Profile}</li>
     * <li>Starts with "scripts:", treat the rest as a relative path below the
     * scripts directory</li>
     * <li>Otherwise, treat the name as a relative path below the program
     * directory</li>
     * </ul>
     * In any case, absolute pathnames will work. Uses the Profile returned by
     * {@link ProfileManager#getActiveProfile()} as the base.
     *
     * @param pName the name, possibly starting with home:, profile:, program:,
     *              preference:, scripts:, or settings:
     * @return Absolute file name to use, or null. This will include
     *         system-specific file separators.
     * @since 2.7.2
     */
    @Nonnull
    @CheckReturnValue
    static public String getExternalFilename(@Nonnull String pName) {
        return FileUtilSupport.getDefault().getExternalFilename(pName);
    }

    /**
     * Get the resource file corresponding to a name. There are five cases:
     * <ul>
     * <li>Starts with "program:", treat the rest as a relative pathname below
     * the program directory</li>
     * <li>Starts with "preference:", treat the rest as a relative path below
     * the user's files directory</li>
     * <li>Starts with "settings:", treat the rest as a relative path below the
     * JMRI system preferences directory</li>
     * <li>Starts with "home:", treat the rest as a relative path below the
     * user.home directory</li>
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
     * @param profile the Profile to use as a base.
     * @param pName   the name, possibly starting with home:, profile:,
     *                program:, preference:, scripts:, or settings:
     * @return Absolute file name to use, or null. This will include
     *         system-specific file separators.
     * @since 4.17.3
     */
    @Nonnull
    @CheckReturnValue
    static public String getExternalFilename(@CheckForNull Profile profile, @Nonnull String pName) {
        return FileUtilSupport.getDefault().getExternalFilename(profile, pName);
    }

    /**
     * Convert a portable filename into an absolute filename, using
     * {@link ProfileManager#getActiveProfile()} as the base.
     *
     * @param path the portable filename
     * @return An absolute filename
     */
    @Nonnull
    @CheckReturnValue
    static public String getAbsoluteFilename(@Nonnull String path) {
        return FileUtilSupport.getDefault().getAbsoluteFilename(path);
    }

    /**
     * Convert a portable filename into an absolute filename.
     *
     * @param profile the profile to use the base
     * @param path    the portable filename
     * @return An absolute filename
     */
    @Nonnull
    @CheckReturnValue
    static public String getAbsoluteFilename(@CheckForNull Profile profile, @Nonnull String path) {
        return FileUtilSupport.getDefault().getAbsoluteFilename(profile, path);
    }

    /**
     * Convert a File object's path to our preferred storage form.
     * <p>
     * This is the inverse of {@link #getFile(String pName)}. Deprecated forms
     * are not created.
     *
     * @param file File at path to be represented
     * @return Filename for storage in a portable manner. This will include
     *         portable, not system-specific, file separators.
     * @since 2.7.2
     */
    @Nonnull
    @CheckReturnValue
    static public String getPortableFilename(@Nonnull File file) {
        return FileUtilSupport.getDefault().getPortableFilename(file);
    }

    /**
     * Convert a File object's path to our preferred storage form.
     * <p>
     * This is the inverse of {@link #getFile(String pName)}. Deprecated forms
     * are not created.
     * <p>
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
    @Nonnull
    @CheckReturnValue
    static public String getPortableFilename(@Nonnull File file, boolean ignoreUserFilesPath, boolean ignoreProfilePath) {
        return FileUtilSupport.getDefault().getPortableFilename(file, ignoreUserFilesPath, ignoreProfilePath);
    }

    /**
     * Convert a filename string to our preferred storage form.
     * <p>
     * This is the inverse of {@link #getExternalFilename(String pName)}.
     * Deprecated forms are not created.
     *
     * @param filename Filename to be represented
     * @return Filename for storage in a portable manner
     * @since 2.7.2
     */
    @Nonnull
    @CheckReturnValue
    static public String getPortableFilename(@Nonnull String filename) {
        return FileUtilSupport.getDefault().getPortableFilename(filename);
    }

    /**
     * Convert a filename string to our preferred storage form.
     * <p>
     * This is the inverse of {@link #getExternalFilename(String pName)}.
     * Deprecated forms are not created.
     * <p>
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
    @Nonnull
    @CheckReturnValue
    static public String getPortableFilename(@Nonnull String filename, boolean ignoreUserFilesPath, boolean ignoreProfilePath) {
        return FileUtilSupport.getDefault().getPortableFilename(filename, ignoreUserFilesPath, ignoreProfilePath);
    }

    /**
     * Convert a File object's path to our preferred storage form.
     * <p>
     * This is the inverse of {@link #getFile(String pName)}. Deprecated forms
     * are not created.
     *
     * @param profile Profile to use as a base
     * @param file    File at path to be represented
     * @return Filename for storage in a portable manner. This will include
     *         portable, not system-specific, file separators.
     * @since 4.17.3
     */
    @Nonnull
    @CheckReturnValue
    static public String getPortableFilename(@CheckForNull Profile profile, @Nonnull File file) {
        return FileUtilSupport.getDefault().getPortableFilename(profile, file);
    }

    /**
     * Convert a File object's path to our preferred storage form.
     * <p>
     * This is the inverse of {@link #getFile(String pName)}. Deprecated forms
     * are not created.
     * <p>
     * This method supports a specific use case concerning profiles and other
     * portable paths that are stored within the User files directory, which
     * will cause the {@link jmri.profile.ProfileManager} to write an incorrect
     * path for the current profile or
     * {@link apps.configurexml.FileLocationPaneXml} to write an incorrect path
     * for the Users file directory. In most cases, the use of
     * {@link #getPortableFilename(java.io.File)} is preferable.
     *
     * @param profile             Profile to use as a base
     * @param file                File at path to be represented
     * @param ignoreUserFilesPath true if paths in the User files path should be
     *                            stored as absolute paths, which is often not
     *                            desirable.
     * @param ignoreProfilePath   true if paths in the profile should be stored
     *                            as absolute paths, which is often not
     *                            desirable.
     * @return Storage format representation
     * @since 4.17.3
     */
    @Nonnull
    @CheckReturnValue
    static public String getPortableFilename(@CheckForNull Profile profile, @Nonnull File file, boolean ignoreUserFilesPath,
            boolean ignoreProfilePath) {
        return FileUtilSupport.getDefault().getPortableFilename(profile, file, ignoreUserFilesPath, ignoreProfilePath);
    }

    /**
     * Convert a filename string to our preferred storage form.
     * <p>
     * This is the inverse of {@link #getExternalFilename(String pName)}.
     * Deprecated forms are not created.
     *
     * @param profile  the Profile to use as a base
     * @param filename Filename to be represented
     * @return Filename for storage in a portable manner
     * @since 4.17.3
     */
    @Nonnull
    @CheckReturnValue
    static public String getPortableFilename(@CheckForNull Profile profile, @Nonnull String filename) {
        return FileUtilSupport.getDefault().getPortableFilename(profile, filename);
    }

    /**
     * Convert a filename string to our preferred storage form.
     * <p>
     * This is the inverse of {@link #getExternalFilename(String pName)}.
     * Deprecated forms are not created.
     * <p>
     * This method supports a specific use case concerning profiles and other
     * portable paths that are stored within the User files directory, which
     * will cause the {@link jmri.profile.ProfileManager} to write an incorrect
     * path for the current profile or
     * {@link apps.configurexml.FileLocationPaneXml} to write an incorrect path
     * for the Users file directory. In most cases, the use of
     * {@link #getPortableFilename(java.io.File)} is preferable.
     *
     * @param profile             the profile to use as a base
     * @param filename            Filename to be represented
     * @param ignoreUserFilesPath true if paths in the User files path should be
     *                            stored as absolute paths, which is often not
     *                            desirable.
     * @param ignoreProfilePath   true if paths in the profile path should be
     *                            stored as absolute paths, which is often not
     *                            desirable.
     * @return Storage format representation
     * @since 4.17.3
     */
    @Nonnull
    @CheckReturnValue
    static public String getPortableFilename(@CheckForNull Profile profile, @Nonnull String filename,
            boolean ignoreUserFilesPath, boolean ignoreProfilePath) {
        return FileUtilSupport.getDefault().getPortableFilename(profile, filename, ignoreUserFilesPath, ignoreProfilePath);
    }

    /**
     * Test if the given filename is a portable filename.
     *
     * @param filename the name to test
     * @return true if filename is portable
     */
    static public boolean isPortableFilename(@Nonnull String filename) {
        return FileUtilSupport.getDefault().isPortableFilename(filename);
    }

    /**
     * Get the user's home directory.
     *
     * @return User's home directory as a String
     */
    @Nonnull
    @CheckReturnValue
    static public String getHomePath() {
        return FileUtilSupport.getDefault().getHomePath();
    }

    /**
     * Get the user's files directory. If not set by the user, this is the same
     * as the profile path for the Profile specified by
     * {@link ProfileManager#getActiveProfile()}.
     *
     * @see #getProfilePath()
     * @return User's files directory as a String
     */
    @Nonnull
    @CheckReturnValue
    static public String getUserFilesPath() {
        return FileUtilSupport.getDefault().getUserFilesPath();
    }

    /**
     * Get the user's files directory. If not set by the user, this is the same
     * as the profile path.
     *
     * @param profile the profile to use as a base
     * @see #getProfilePath()
     * @return User's files directory as a String
     */
    @Nonnull
    @CheckReturnValue
    static public String getUserFilesPath(@CheckForNull Profile profile) {
        return FileUtilSupport.getDefault().getUserFilesPath(profile);
    }

    /**
     * Set the user's files directory.
     *
     * @see #getUserFilesPath()
     * @param profile The profile to use as a base
     * @param path    The path to the user's files directory
     */
    static public void setUserFilesPath(@CheckForNull Profile profile, @Nonnull String path) {
        FileUtilSupport.getDefault().setUserFilesPath(profile, path);
    }

    /**
     * Get the profile directory. Uses the Profile returned by
     * {@link ProfileManager#getActiveProfile()} as a base. If that is null,
     * gets the preferences path.
     *
     * @see #getPreferencesPath()
     * @return Profile directory
     */
    @Nonnull
    @CheckReturnValue
    static public String getProfilePath() {
        return FileUtilSupport.getDefault().getProfilePath();
    }

    /**
     * Get the profile directory. If the profile is null or has a null
     * directory, this is the same as the preferences path.
     *
     * @param profile the profile to use as a base
     * @see #getPreferencesPath()
     * @return Profile directory
     */
    @Nonnull
    @CheckReturnValue
    static public String getProfilePath(@CheckForNull Profile profile) {
        return FileUtilSupport.getDefault().getProfilePath(profile);
    }

    /**
     * Used to set the profile path, but now does nothing.
     *
     * @see #getProfilePath()
     * @param path The path to the profile directory
     * @deprecated since 4.17.3 without replacement
     */
    @Deprecated
    static public void setProfilePath(@CheckForNull String path) {
        // nothing to do
    }

    /**
     * Get the preferences directory. This directory is set based on the OS and
     * is not normally settable by the user.
     * <ul>
     * <li>On Microsoft Windows systems, this is {@code JMRI} in the User's home
     * directory.</li>
     * <li>On OS X systems, this is {@code Library/Preferences/JMRI} in the
     * User's home directory.</li>
     * <li>On Linux, Solaris, and other UNIXes, this is {@code .jmri} in the
     * User's home directory.</li>
     * <li>This can be overridden with by setting the {@code jmri.prefsdir} Java
     * property when starting JMRI.</li>
     * </ul>
     * Use {@link #getHomePath()} to get the User's home directory.
     *
     * @see #getHomePath()
     * @return Path to the preferences directory.
     */
    @Nonnull
    @CheckReturnValue
    static public String getPreferencesPath() {
        return FileUtilSupport.getDefault().getPreferencesPath();
    }

    /**
     * Get the JMRI program directory. If the program directory has not been
     * previously sets, first sets the program directory to the value specified
     * in the Java System property <code>jmri.path.program</code>, or
     * <code>.</code> if that property is not set.
     *
     * @return JMRI program directory as a String.
     */
    @Nonnull
    @CheckReturnValue
    static public String getProgramPath() {
        return FileUtilSupport.getDefault().getProgramPath();
    }

    /**
     * Set the JMRI program directory.
     * <p>
     * Convenience method that calls
     * {@link FileUtil#setProgramPath(java.io.File)} with the passed in path.
     *
     * @param path the path to the JMRI installation
     */
    static public void setProgramPath(@Nonnull String path) {
        FileUtilSupport.getDefault().setProgramPath(new File(path));
    }

    /**
     * Set the JMRI program directory.
     * <p>
     * If set, allows JMRI to be loaded from locations other than the directory
     * containing JMRI resources. This must be set very early in the process of
     * loading JMRI (prior to loading any other JMRI code) to be meaningfully
     * used.
     *
     * @param path the path to the JMRI installation
     */
    static public void setProgramPath(@Nonnull File path) {
        FileUtilSupport.getDefault().setProgramPath(path);
    }

    /**
     * Get the URL of a portable filename if it can be located using
     * {@link #findURL(java.lang.String)}
     *
     * @param path the path to find
     * @return URL of portable or absolute path
     */
    @Nonnull
    @CheckReturnValue
    static public URI findExternalFilename(@Nonnull String path) {
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
    static public InputStream findInputStream(@Nonnull String path) {
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
    static public InputStream findInputStream(@Nonnull String path, @Nonnull String... searchPaths) {
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
    static public InputStream findInputStream(@Nonnull String path, Location locations) {
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
    static public InputStream findInputStream(@Nonnull String path, Location locations, @Nonnull String... searchPaths) {
        return FileUtilSupport.getDefault().findInputStream(path, locations, searchPaths);
    }

    /**
     * Get the resources directory within the user's files directory.
     *
     * @return path to [user's file]/resources/
     */
    @Nonnull
    @CheckReturnValue
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
    static public URI findURI(@Nonnull String path) {
        return FileUtilSupport.getDefault().findURI(path);
    }

    /**
     * Search for a file or JAR resource by name and return the
     * {@link java.net.URI} for that file. Search order is defined by
     * {@link #findURI(java.lang.String, jmri.util.FileUtil.Location, java.lang.String...)}.
     * No limits are placed on search locations.
     * <p>
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
    static public URI findURI(@Nonnull String path, @Nonnull String... searchPaths) {
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
    static public URI findURI(@Nonnull String path, @Nonnull Location locations) {
        return FileUtilSupport.getDefault().findURI(path, locations);
    }

    /**
     * Search for a file or JAR resource by name and return the
     * {@link java.net.URI} for that file.
     * <p>
     * Search order is:
     * <ol>
     * <li>For any provided searchPaths, iterate over the searchPaths by
     * prepending each searchPath to the path and following the following search
     * order:<ol>
     * <li>As a {@link java.io.File} in the user preferences directory</li>
     * <li>As a File in the current working directory (usually, but not always
     * the JMRI distribution directory)</li>
     * <li>As a File in the JMRI distribution directory</li>
     * <li>As a resource in jmri.jar</li>
     * </ol></li>
     * <li>If the file or resource has not been found in the searchPaths, search
     * in the four locations listed without prepending any path</li>
     * <li>As a File with an absolute path</li>
     * </ol>
     * <p>
     * The <code>locations</code> parameter limits the above logic by limiting
     * the location searched.
     * <ol>
     * <li>{@link Location#ALL} will not place any limits on the search</li>
     * <li>{@link Location#NONE} effectively requires that <code>path</code> be
     * a portable pathname</li>
     * <li>{@link Location#INSTALLED} limits the search to the
     * {@link FileUtil#PROGRAM} directory and JARs in the class path</li>
     * <li>{@link Location#USER} limits the search to the
     * {@link FileUtil#PREFERENCES}, {@link FileUtil#PROFILE}, and
     * {@link FileUtil#SETTINGS} directories (in that order)</li>
     * </ol>
     *
     * @param path        The relative path of the file or resource
     * @param locations   The types of locations to limit the search to
     * @param searchPaths a list of paths to search for the path in
     * @return The URI or null
     * @see #findURI(java.lang.String)
     * @see #findURI(java.lang.String, jmri.util.FileUtil.Location)
     * @see #findURI(java.lang.String, java.lang.String...)
     */
    static public URI findURI(@Nonnull String path, @Nonnull Location locations, @Nonnull String... searchPaths) {
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
    static public URL findURL(@Nonnull String path) {
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
    static public URL findURL(@Nonnull String path, @Nonnull String... searchPaths) {
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
    static public URL findURL(@Nonnull String path, @Nonnull Location locations) {
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
    static public URL findURL(@Nonnull String path, @Nonnull Location locations, @Nonnull String... searchPaths) {
        return FileUtilSupport.getDefault().findURL(path, locations, searchPaths);
    }

    /**
     * Return the {@link java.net.URI} for a given URL
     *
     * @param url the URL
     * @return a URI or null if the conversion would have caused a
     *         {@link java.net.URISyntaxException}
     */
    static public URI urlToURI(@Nonnull URL url) {
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
    static public URL fileToURL(@Nonnull File file) {
        return FileUtilSupport.getDefault().fileToURL(file);
    }

    /**
     * Get the JMRI distribution jar file.
     *
     * @return the JAR file containing the JMRI library or null if not running
     *         from a JAR file
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
     * Get the path to the scripts directory using the Profile returned by
     * {@link ProfileManager#getActiveProfile()} as the base.
     *
     * @return the scriptsPath
     */
    @Nonnull
    @CheckReturnValue
    public static String getScriptsPath() {
        return FileUtilSupport.getDefault().getScriptsPath();
    }

    /**
     * Get the path to the scripts directory.
     *
     * @param profile the Profile to use as the base
     * @return the scriptsPath
     */
    @Nonnull
    @CheckReturnValue
    public static String getScriptsPath(@CheckForNull Profile profile) {
        return FileUtilSupport.getDefault().getScriptsPath(profile);
    }

    /**
     * Set the path to python scripts.
     *
     * @param profile the profile to set the path for
     * @param path    the scriptsPath to set
     */
    public static void setScriptsPath(@CheckForNull Profile profile, @CheckForNull String path) {
        FileUtilSupport.getDefault().setScriptsPath(profile, path);
    }

    /**
     * Read a text file into a String.
     *
     * @param file The text file.
     * @return The contents of the file.
     * @throws java.io.IOException if the file cannot be read
     */
    public static String readFile(@Nonnull File file) throws IOException {
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
    public static String readURL(@Nonnull URL url) throws IOException {
        return FileUtilSupport.getDefault().readURL(url);
    }

    /**
     * Replaces most non-alphanumeric characters in name with an underscore.
     *
     * @param name The filename to be sanitized.
     * @return The sanitized filename.
     */
    @Nonnull
    public static String sanitizeFilename(@Nonnull String name) {
        return FileUtilSupport.getDefault().sanitizeFilename(name);
    }

    /**
     * Create a directory if required. Any parent directories will also be
     * created.
     *
     * @param path directory to create
     */
    public static void createDirectory(@Nonnull String path) {
        FileUtilSupport.getDefault().createDirectory(path);
    }

    /**
     * Create a directory if required. Any parent directories will also be
     * created.
     *
     * @param dir directory to create
     */
    public static void createDirectory(@Nonnull File dir) {
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
    public static boolean delete(@Nonnull File path) {
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
    public static void copy(@Nonnull File source, @Nonnull File dest) throws IOException {
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
    public static void appendTextToFile(@Nonnull File file, @Nonnull String text) throws IOException {
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
    public static void backup(@Nonnull File file) throws IOException {
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
     * @throws java.io.IOException      if a backup cannot be created
     * @throws IllegalArgumentException if max is less than one
     * @see jmri.util.FileUtilSupport#rotate(java.io.File, int,
     * java.lang.String)
     * @see jmri.util.FileUtilSupport#backup(java.io.File)
     */
    public static void rotate(@Nonnull File file, int max, @CheckForNull String extension) throws IOException {
        FileUtilSupport.getDefault().rotate(file, max, extension);
    }

    /**
     * Get the default instance of FileUtilSupport.
     *
     * @return the default instance of FileUtilSupport
     */
    public static FileUtilSupport getDefault() {
        return FileUtilSupport.getDefault();
    }

    /**
     * PropertyChangeEvents for properties that are Profile-specific use a
     * Property to enclose both the Profile and the value of the property.
     */
    public static class Property implements Map.Entry {

        private final Profile key;
        private final String value;

        // package private
        Property(Profile key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Profile getKey() {
            return key;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            throw new UnsupportedOperationException("Immutable by design");
        }

    }

    /* Private default constructor to ensure it's not documented. */
    private FileUtil() {
    }
}
