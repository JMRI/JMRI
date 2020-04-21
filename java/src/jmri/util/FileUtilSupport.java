package jmri.util;

import static jmri.util.FileUtil.HOME;
import static jmri.util.FileUtil.PREFERENCES;
import static jmri.util.FileUtil.PROFILE;
import static jmri.util.FileUtil.PROGRAM;
import static jmri.util.FileUtil.SCRIPTS;
import static jmri.util.FileUtil.SEPARATOR;
import static jmri.util.FileUtil.SETTINGS;

import com.sun.jna.platform.win32.KnownFolders;
import com.sun.jna.platform.win32.Shell32Util;
import com.sun.jna.platform.win32.ShlObj;
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
import java.security.CodeSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import javax.annotation.CheckReturnValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Version;
import jmri.beans.Bean;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.util.FileUtil.Location;
import jmri.util.FileUtil.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support the {@link jmri.util.FileUtil} static API while providing
 * {@link java.beans.PropertyChangeSupport} for listening to changes in the
 * paths. Also provides the underlying implementation of all FileUtil methods so
 * they can be exposed to scripts as an object methods instead of as static
 * methods of a class.
 *
 * @author Randall Wood (C) 2015, 2016, 2019
 */
public class FileUtilSupport extends Bean {

    /* User's home directory */
    private static final String HOME_PATH = System.getProperty("user.home") + File.separator; // NOI18N
    //
    // Settable directories
    //
    /* JMRI program path, defaults to directory JMRI is executed from */
    private String programPath = null;
    /* path to jmri.jar */
    private String jarPath = null;
    /* path to the jython scripts directory */
    private final HashMap<Profile, String> scriptsPaths = new HashMap<>();
    /* path to the user's files directory */
    private final HashMap<Profile, String> userFilesPaths = new HashMap<>();
    /* path to profiles in use */
    private final HashMap<Profile, String> profilePaths = new HashMap<>();

    // initialize logging
    private static final Logger log = LoggerFactory.getLogger(FileUtilSupport.class);
    // default instance
    volatile private static FileUtilSupport defaultInstance = null;

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
    public File getFile(@Nonnull String path) throws FileNotFoundException {
        return getFile(ProfileManager.getDefault().getActiveProfile(), path);
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
    public File getFile(@CheckForNull Profile profile, @Nonnull String path) throws FileNotFoundException {
        try {
            return new File(this.pathFromPortablePath(profile, path));
        } catch (NullPointerException ex) {
            throw new FileNotFoundException("Cannot find file at " + path);
        }
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
    public URI getURI(@Nonnull String path) throws FileNotFoundException {
        return this.getFile(path).toURI();
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
    public URL getURL(@Nonnull String path) throws FileNotFoundException {
        try {
            return this.getURI(path).toURL();
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
    @CheckForNull
    @CheckReturnValue
    public URL getURL(@Nonnull URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException | IllegalArgumentException ex) {
            log.warn("Unable to get URL from {}", uri);
            return null;
        } catch (NullPointerException ex) {
            log.warn("Unable to get URL from null object.", ex);
            return null;
        }
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
    public Set<File> findFiles(@Nonnull String name, @Nonnull String root) throws IllegalArgumentException {
        return this.findFiles(name, root, Location.ALL);
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
    public Set<File> findFiles(@Nonnull String name, @Nonnull String root, @Nonnull Location location) {
        Objects.requireNonNull(name, "name must be nonnull");
        Objects.requireNonNull(root, "root must be nonnull");
        Objects.requireNonNull(location, "location must be nonnull");
        if (location == Location.NONE) {
            throw new IllegalArgumentException("location must not be NONE");
        }
        if (root.isEmpty() || root.contains("..") || root.startsWith("/")) {
            throw new IllegalArgumentException("root is invalid");
        }
        if (name.isEmpty() || name.contains(File.pathSeparator) || name.contains("/")) {
            throw new IllegalArgumentException("name is invalid");
        }
        Set<File> files = new HashSet<>();
        if (location == Location.INSTALLED || location == Location.ALL) {
            files.addAll(this.findFiles(name, new File(this.findURI(PROGRAM + root, Location.NONE))));
        }
        if (location == Location.USER || location == Location.ALL) {
            try {
                files.addAll(this.findFiles(name, new File(this.findURI(PREFERENCES + root, Location.NONE))));
            } catch (NullPointerException ex) {
                // expected if path PREFERENCES + root does not exist
                log.trace("{} does not exist in {}", root, PREFERENCES);
            }
            try {
                files.addAll(this.findFiles(name, new File(this.findURI(PROFILE + root, Location.NONE))));
            } catch (NullPointerException ex) {
                // expected if path PROFILE + root does not exist
                log.trace("{} does not exist in {}", root, PROFILE);
            }
            try {
                files.addAll(this.findFiles(name, new File(this.findURI(SETTINGS + root, Location.NONE))));
            } catch (NullPointerException ex) {
                // expected if path SETTINGS + root does not exist
                log.trace("{} does not exist in {}", root, SETTINGS);
            }
        }
        return files;
    }

    private Set<File> findFiles(String name, File root) {
        Set<File> files = new HashSet<>();
        if (root.isDirectory()) {
            try {
                Files.walkFileTree(root.toPath(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(final Path dir,
                            final BasicFileAttributes attrs) throws IOException {

                        Path fn = dir.getFileName();
                        if (fn != null && name.equals(fn.toString())) {
                            files.add(dir.toFile().getCanonicalFile());
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(final Path file,
                            final BasicFileAttributes attrs) throws IOException {
                        // TODO: accept glob patterns
                        Path fn = file.getFileName();
                        if (fn != null && name.equals(fn.toString())) {
                            files.add(file.toFile().getCanonicalFile());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException ex) {
                log.warn("Exception while finding file {} in {}", name, root, ex);
            }
        }
        return files;
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
     * In any case, absolute pathnames will work. Uses the Profile returned by
     * {@link ProfileManager#getActiveProfile()} as the base.
     *
     * @param pName the name, possibly starting with file:, home:, profile:,
     *              program:, preference:, scripts:, settings, or resource:
     * @return Absolute file name to use. This will include system-specific file
     *         separators.
     * @since 2.7.2
     */
    @Nonnull
    @CheckReturnValue
    public String getExternalFilename(@Nonnull String pName) {
        return getExternalFilename(ProfileManager.getDefault().getActiveProfile(), pName);
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
     * @param profile the Profile to use as a base
     * @param pName   the name, possibly starting with file:, home:, profile:,
     *                program:, preference:, scripts:, settings, or resource:
     * @return Absolute file name to use. This will include system-specific file
     *         separators.
     * @since 4.17.3
     */
    @Nonnull
    @CheckReturnValue
    public String getExternalFilename(@CheckForNull Profile profile, @Nonnull String pName) {
        String filename = this.pathFromPortablePath(profile, pName);
        return (filename != null) ? filename : pName.replace(SEPARATOR, File.separatorChar);
    }

    /**
     * Convert a portable filename into an absolute filename, using
     * {@link jmri.profile.ProfileManager#getActiveProfile()} as the base.
     *
     * @param path the portable filename
     * @return An absolute filename
     */
    @Nonnull
    @CheckReturnValue
    public String getAbsoluteFilename(@Nonnull String path) {
        return this.getAbsoluteFilename(ProfileManager.getDefault().getActiveProfile(), path);
    }

    /**
     * Convert a portable filename into an absolute filename.
     *
     * @param profile the profile to use as the base
     * @param path    the portable filename
     * @return An absolute filename
     */
    @Nonnull
    @CheckReturnValue
    public String getAbsoluteFilename(@CheckForNull Profile profile, @Nonnull String path) {
        return this.pathFromPortablePath(profile, path);
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
    public String getPortableFilename(@Nonnull File file) {
        return this.getPortableFilename(ProfileManager.getDefault().getActiveProfile(), file, false, false);
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
    public String getPortableFilename(@Nonnull File file, boolean ignoreUserFilesPath, boolean ignoreProfilePath) {
        return getPortableFilename(ProfileManager.getDefault().getActiveProfile(), file, ignoreUserFilesPath, ignoreProfilePath);
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
    public String getPortableFilename(@Nonnull String filename) {
        return getPortableFilename(ProfileManager.getDefault().getActiveProfile(), filename, false, false);
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
    public String getPortableFilename(@Nonnull String filename, boolean ignoreUserFilesPath, boolean ignoreProfilePath) {
        if (this.isPortableFilename(filename)) {
            // if this already contains prefix, run through conversion to normalize
            return getPortableFilename(getExternalFilename(filename), ignoreUserFilesPath, ignoreProfilePath);
        } else {
            // treat as pure filename
            return getPortableFilename(new File(filename), ignoreUserFilesPath, ignoreProfilePath);
        }
    }

    /**
     * Convert a File object's path to our preferred storage form.
     * <p>
     * This is the inverse of {@link #getFile(String pName)}. Deprecated forms
     * are not created.
     *
     * @param profile Profile to use as base
     * @param file    File at path to be represented
     * @return Filename for storage in a portable manner. This will include
     *         portable, not system-specific, file separators.
     * @since 4.17.3
     */
    @Nonnull
    @CheckReturnValue
    public String getPortableFilename(@CheckForNull Profile profile, @Nonnull File file) {
        return this.getPortableFilename(profile, file, false, false);
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
     * @param profile             Profile to use as base
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
    public String getPortableFilename(@CheckForNull Profile profile, @Nonnull File file, boolean ignoreUserFilesPath, boolean ignoreProfilePath) {
        // compare full path name to see if same as preferences
        String filename = file.getAbsolutePath();

        // append separator if file is a directory
        if (file.isDirectory()) {
            filename = filename + File.separator;
        }

        if (filename == null) {
            throw new IllegalArgumentException("File \"" + file + "\" has a null absolute path which is not allowed");
        }

        // compare full path name to see if same as preferences
        if (!ignoreUserFilesPath) {
            if (filename.startsWith(getUserFilesPath(profile))) {
                return PREFERENCES
                        + filename.substring(getUserFilesPath(profile).length(), filename.length()).replace(File.separatorChar,
                                SEPARATOR);
            }
        }

        if (!ignoreProfilePath) {
            // compare full path name to see if same as profile
            if (filename.startsWith(getProfilePath(profile))) {
                return PROFILE
                        + filename.substring(getProfilePath(profile).length(), filename.length()).replace(File.separatorChar,
                                SEPARATOR);
            }
        }

        // compare full path name to see if same as settings
        if (filename.startsWith(getPreferencesPath())) {
            return SETTINGS
                    + filename.substring(getPreferencesPath().length(), filename.length()).replace(File.separatorChar,
                            SEPARATOR);
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
            if (filename.startsWith(getScriptsPath(profile)) && !filename.equals(getScriptsPath(profile))) {
                return SCRIPTS
                        + filename.substring(getScriptsPath(profile).length(), filename.length()).replace(File.separatorChar,
                                SEPARATOR);
            }
        }

        // now check for relative to program dir
        if (filename.startsWith(getProgramPath())) {
            return PROGRAM
                    + filename.substring(getProgramPath().length(), filename.length()).replace(File.separatorChar,
                            SEPARATOR);
        }

        // compare full path name to see if same as home directory
        // do this last, in case preferences or program dir are in home directory
        if (filename.startsWith(getHomePath())) {
            return HOME
                    + filename.substring(getHomePath().length(), filename.length()).replace(File.separatorChar,
                            SEPARATOR);
        }

        return filename.replace(File.separatorChar, SEPARATOR); // absolute, and doesn't match; not really portable...
    }

    /**
     * Convert a filename string to our preferred storage form.
     * <p>
     * This is the inverse of {@link #getExternalFilename(String pName)}.
     * Deprecated forms are not created.
     *
     * @param profile  Profile to use as base
     * @param filename Filename to be represented
     * @return Filename for storage in a portable manner
     * @since 4.17.3
     */
    @Nonnull
    @CheckReturnValue
    public String getPortableFilename(@CheckForNull Profile profile, @Nonnull String filename) {
        return getPortableFilename(profile, filename, false, false);
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
     * @param profile             Profile to use as base
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
    public String getPortableFilename(@CheckForNull Profile profile, @Nonnull String filename, boolean ignoreUserFilesPath,
            boolean ignoreProfilePath) {
        if (isPortableFilename(filename)) {
            // if this already contains prefix, run through conversion to normalize
            return getPortableFilename(profile, getExternalFilename(filename), ignoreUserFilesPath, ignoreProfilePath);
        } else {
            // treat as pure filename
            return getPortableFilename(profile, new File(filename), ignoreUserFilesPath, ignoreProfilePath);
        }
    }

    /**
     * Test if the given filename is a portable filename.
     * <p>
     * Note that this method may return a false positive if the filename is a
     * file: URL.
     *
     * @param filename the name to test
     * @return true if filename is portable
     */
    public boolean isPortableFilename(@Nonnull String filename) {
        return (filename.startsWith(PROGRAM)
                || filename.startsWith(HOME)
                || filename.startsWith(PREFERENCES)
                || filename.startsWith(SCRIPTS)
                || filename.startsWith(PROFILE)
                || filename.startsWith(SETTINGS));
    }

    /**
     * Get the user's home directory.
     *
     * @return User's home directory as a String
     */
    @Nonnull
    @CheckReturnValue
    public String getHomePath() {
        return HOME_PATH;
    }

    /**
     * Get the user's files directory. If not set by the user, this is the same
     * as the profile path returned by
     * {@link ProfileManager#getActiveProfile()}. Note that if the profile path
     * has been set to null, that returns the preferences directory, see
     * {@link #getProfilePath()}.
     *
     * @see #getProfilePath()
     * @return User's files directory
     */
    @Nonnull
    @CheckReturnValue
    public String getUserFilesPath() {
        return getUserFilesPath(ProfileManager.getDefault().getActiveProfile());
    }

    /**
     * Get the user's files directory. If not set by the user, this is the same
     * as the profile path. Note that if the profile path has been set to null,
     * that returns the preferences directory, see {@link #getProfilePath()}.
     *
     * @param profile the profile to use
     * @see #getProfilePath()
     * @return User's files directory
     */
    @Nonnull
    @CheckReturnValue
    public String getUserFilesPath(@CheckForNull Profile profile) {
        String path = userFilesPaths.get(profile);
        return path != null ? path : getProfilePath(profile);
    }

    /**
     * Set the user's files directory.
     *
     * @see #getUserFilesPath()
     * @param profile the profile to set the user's files directory for
     * @param path    The path to the user's files directory using
     *                system-specific separators
     */
    public void setUserFilesPath(@CheckForNull Profile profile, @Nonnull String path) {
        String old = userFilesPaths.get(profile);
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        userFilesPaths.put(profile, path);
        if ((old != null && !old.equals(path)) || (!path.equals(old))) {
            this.firePropertyChange(FileUtil.PREFERENCES, new Property(profile, old), new Property(profile, path));
        }
    }

    /**
     * Get the profile directory. If not set, provide the preferences path.
     *
     * @param profile the Profile to use as a base
     * @see #getPreferencesPath()
     * @return Profile directory using system-specific separators
     */
    @Nonnull
    @CheckReturnValue
    public String getProfilePath(@CheckForNull Profile profile) {
        String path = profilePaths.get(profile);
        if (path == null) {
            File f = profile != null ? profile.getPath() : null;
            if (f != null) {
                path = f.getAbsolutePath();
                if (!path.endsWith(File.separator)) {
                    path = path + File.separator;
                }
                profilePaths.put(profile, path);
            }
        }
        return (path != null) ? path : this.getPreferencesPath();
    }

    /**
     * Get the profile directory. If not set, provide the preferences path. Uses
     * the Profile returned by {@link ProfileManager#getActiveProfile()} as a
     * base.
     *
     * @see #getPreferencesPath()
     * @return Profile directory using system-specific separators
     */
    @Nonnull
    @CheckReturnValue
    public String getProfilePath() {
        return getProfilePath(ProfileManager.getDefault().getActiveProfile());
    }

    /**
     * Used to set the profile path, but now does nothing.
     *
     * @see #getProfilePath()
     * @param path The path to the profile directory using system-specific
     *             separators. If null, this will cause
     *             {@link #getProfilePath()} to provide the preferences
     *             directory via {@link #getPreferencesPath()}.
     * @deprecated since 4.17.3 without replacement
     */
    @Deprecated
    public void setProfilePath(@CheckForNull String path) {
        // does nothing
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
     * @return Path to the preferences directory using system-specific
     *         separators.
     */
    @Nonnull
    @CheckReturnValue
    public String getPreferencesPath() {
        // return jmri.prefsdir property if present
        String jmriPrefsDir = System.getProperty("jmri.prefsdir", ""); // NOI18N
        if (!jmriPrefsDir.isEmpty()) {
            try {
                return new File(jmriPrefsDir).getCanonicalPath() + File.separator;
            } catch (IOException ex) {
                // use System.err because logging at this point will fail
                // since this method is called to setup logging
                System.err.println("Unable to locate settings dir \"" + jmriPrefsDir + "\"");
                if (!jmriPrefsDir.endsWith(File.separator)) {
                    return jmriPrefsDir + File.separator;
                }
            }
        }
        String result;
        switch (SystemType.getType()) {
            case SystemType.MACOSX:
                // Mac OS X
                result = this.getHomePath() + "Library" + File.separator + "Preferences" + File.separator + "JMRI" + File.separator; // NOI18N
                break;
            case SystemType.LINUX:
            case SystemType.UNIX:
                // Linux, so use an invisible file
                result = this.getHomePath() + ".jmri" + File.separator; // NOI18N
                break;
            case SystemType.WINDOWS:
            default:
                // Could be Windows, other
                result = this.getHomePath() + "JMRI" + File.separator; // NOI18N
                break;
        }
        // logging here merely throws warnings since we call this method to set up logging
        // uncomment below to print OS default to console
        // System.out.println("preferencesPath defined as \"" + result + "\" based on os.name=\"" + SystemType.getOSName() + "\"");
        return result;
    }

    /**
     * Get the JMRI cache location, ensuring its existence.
     * <p>
     * This is <strong>not</strong> part of the {@link jmri.util.FileUtil} API
     * since it should generally be accessed using
     * {@link jmri.profile.ProfileUtils#getCacheDirectory(jmri.profile.Profile, java.lang.Class)}.
     * <p>
     * Uses the following locations (where [version] is from
     * {@link jmri.Version#getCanonicalVersion()}):
     * <dl>
     * <dt>System Property (if set)</dt><dd>value of
     * <em>jmri_default_cachedir</em></dd>
     * <dt>macOS</dt><dd>~/Library/Caches/JMRI/[version]</dd>
     * <dt>Windows</dt><dd>%Local AppData%/JMRI/[version]</dd>
     * <dt>UNIX/Linux/POSIX</dt><dd>${XDG_CACHE_HOME}/JMRI/[version] or
     * $HOME/.cache/JMRI/[version]</dd>
     * <dt>Fallback</dt><dd>JMRI portable path
     * <em>setting:cache/[version]</em></dd>
     * </dl>
     *
     * @return the cache directory for this version of JMRI
     */
    @Nonnull
    public File getCacheDirectory() {
        File cache;
        String property = System.getProperty("jmri_default_cachedir");
        if (property != null) {
            cache = new File(property);
        } else {
            switch (SystemType.getType()) {
                case SystemType.MACOSX:
                    cache = new File(new File(this.getHomePath(), "Library/Caches/JMRI"), Version.getCanonicalVersion());
                    break;
                case SystemType.LINUX:
                case SystemType.UNIX:
                    property = System.getenv("XDG_CACHE_HOME");
                    if (property != null) {
                        cache = new File(new File(property, "JMRI"), Version.getCanonicalVersion());
                    } else {
                        cache = new File(new File(this.getHomePath(), ".cache/JMRI"), Version.getCanonicalVersion());
                    }
                    break;
                case SystemType.WINDOWS:
                    try {
                        cache = new File(new File(Shell32Util.getKnownFolderPath(KnownFolders.FOLDERID_LocalAppData), "JMRI/cache"), Version.getCanonicalVersion());
                    } catch (UnsatisfiedLinkError er) {
                        // Needed only on Windows XP
                        cache = new File(new File(Shell32Util.getFolderPath(ShlObj.CSIDL_LOCAL_APPDATA), "JMRI/cache"), Version.getCanonicalVersion());
                    }
                    break;
                default:
                    // fallback
                    cache = new File(new File(this.getPreferencesPath(), "cache"), Version.getCanonicalVersion());
                    break;
            }
        }
        this.createDirectory(cache);
        return cache;
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
    public String getProgramPath() {
        if (programPath == null) {
            this.setProgramPath(System.getProperty("jmri.path.program", ".")); // NOI18N
        }
        return programPath;
    }

    /**
     * Set the JMRI program directory.
     * <p>
     * Convenience method that calls {@link #setProgramPath(java.io.File)} with
     * the passed in path.
     *
     * @param path the path to the JMRI installation
     */
    public void setProgramPath(@Nonnull String path) {
        this.setProgramPath(new File(path));
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
    public void setProgramPath(@Nonnull File path) {
        String old = this.programPath;
        try {
            this.programPath = (path).getCanonicalPath() + File.separator;
        } catch (IOException ex) {
            log.error("Unable to get JMRI program directory.", ex);
        }
        if ((old != null && !old.equals(this.programPath))
                || (this.programPath != null && !this.programPath.equals(old))) {
            this.firePropertyChange(FileUtil.PROGRAM, old, this.programPath);
        }
    }

    /**
     * Get the resources directory within the user's files directory.
     *
     * @return path to [user's file]/resources/ using system-specific separators
     */
    @Nonnull
    @CheckReturnValue
    public String getUserResourcePath() {
        return this.getUserFilesPath() + "resources" + File.separator; // NOI18N
    }

    /**
     * Log all paths at the INFO level.
     */
    public void logFilePaths() {
        log.info("File path {} is {}", FileUtil.PROGRAM, this.getProgramPath());
        log.info("File path {} is {}", FileUtil.PREFERENCES, this.getUserFilesPath());
        log.info("File path {} is {}", FileUtil.PROFILE, this.getProfilePath());
        log.info("File path {} is {}", FileUtil.SETTINGS, this.getPreferencesPath());
        log.info("File path {} is {}", FileUtil.HOME, this.getHomePath());
        log.info("File path {} is {}", FileUtil.SCRIPTS, this.getScriptsPath());
    }

    /**
     * Get the path to the scripts directory. If not set previously with
     * {@link #setScriptsPath}, this is the "jython" subdirectory in the program
     * directory. Uses the Profile returned by
     * {@link ProfileManager#getActiveProfile()} as the base.
     *
     * @return the scripts directory using system-specific separators
     */
    @Nonnull
    @CheckReturnValue
    public String getScriptsPath() {
        return getScriptsPath(ProfileManager.getDefault().getActiveProfile());
    }

    /**
     * Get the path to the scripts directory. If not set previously with
     * {@link #setScriptsPath}, this is the "jython" subdirectory in the program
     * directory.
     *
     * @param profile the Profile to use as the base
     * @return the path to scripts directory using system-specific separators
     */
    @Nonnull
    @CheckReturnValue
    public String getScriptsPath(@CheckForNull Profile profile) {
        String path = scriptsPaths.get(profile);
        if (path != null) {
            return path;
        }
        // scripts directory not set by user, return default if it exists
        File file = new File(this.getProgramPath() + File.separator + "jython" + File.separator); // NOI18N
        if (file.exists() && file.isDirectory()) {
            return file.getPath() + File.separator;
        }
        // if default does not exist, return user's files directory
        return this.getUserFilesPath();
    }

    /**
     * Set the path to python scripts.
     *
     * @param profile the profile to use as a base
     * @param path    the scriptsPaths to set; null resets to the default,
     *                defined in {@link #getScriptsPath()}
     */
    public void setScriptsPath(@CheckForNull Profile profile, @CheckForNull String path) {
        String old = scriptsPaths.get(profile);
        if (path != null && !path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        scriptsPaths.put(profile, path);
        if ((old != null && !old.equals(path)) || (path != null && !path.equals(old))) {
            this.firePropertyChange(FileUtil.SCRIPTS, new Property(profile, old), new Property(profile, path));
        }
    }

    /**
     * Get the URL of a portable filename if it can be located using
     * {@link #findURI(java.lang.String)}
     *
     * @param path the path to find
     * @return URL of portable or absolute path
     */
    @Nonnull
    @CheckReturnValue
    public URI findExternalFilename(@Nonnull String path) {
        log.debug("Finding external path {}", path);
        if (this.isPortableFilename(path)) {
            int index = path.indexOf(":") + 1;
            String location = path.substring(0, index);
            path = path.substring(index);
            log.debug("Finding {} and {}", location, path);
            switch (location) {
                case FileUtil.PROGRAM:
                    return this.findURI(path, FileUtil.Location.INSTALLED);
                case FileUtil.PREFERENCES:
                    return this.findURI(path, FileUtil.Location.USER);
                case FileUtil.PROFILE:
                case FileUtil.SETTINGS:
                case FileUtil.SCRIPTS:
                case FileUtil.HOME:
                    return this.findURI(this.getExternalFilename(location + path));
                default:
                    break;
            }
        }
        return this.findURI(path, Location.ALL);
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
    public InputStream findInputStream(@Nonnull String path) {
        return this.findInputStream(path, new String[]{});
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
    public InputStream findInputStream(@Nonnull String path, @Nonnull String... searchPaths) {
        return this.findInputStream(path, Location.ALL, searchPaths);
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
    public InputStream findInputStream(@Nonnull String path, @Nonnull Location locations) {
        return this.findInputStream(path, locations, new String[]{});
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
    public InputStream findInputStream(@Nonnull String path, @Nonnull Location locations, @Nonnull String... searchPaths) {
        URL file = this.findURL(path, locations, searchPaths);
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
    public URI findURI(@Nonnull String path) {
        return this.findURI(path, new String[]{});
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
    public URI findURI(@Nonnull String path, @Nonnull String... searchPaths) {
        return this.findURI(path, Location.ALL, searchPaths);
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
    public URI findURI(@Nonnull String path, @Nonnull Location locations) {
        return this.findURI(path, locations, new String[]{});
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
    public URI findURI(@Nonnull String path, @Nonnull Location locations, @Nonnull String... searchPaths) {
        if (log.isDebugEnabled()) { // avoid the Arrays.toString call unless debugging
            log.debug("Attempting to find {} in {}", path, Arrays.toString(searchPaths));
        }
        if (this.isPortableFilename(path)) {
            try {
                return this.findExternalFilename(path);
            } catch (NullPointerException ex) {
                // do nothing
            }
        }
        URI resource = null;
        for (String searchPath : searchPaths) {
            resource = this.findURI(searchPath + File.separator + path);
            if (resource != null) {
                return resource;
            }
        }
        File file;
        if (locations == Location.ALL || locations == Location.USER) {
            // attempt to return path from preferences directory
            file = new File(this.getUserFilesPath(), path);
            if (file.exists()) {
                return file.toURI();
            }
            // attempt to return path from profile directory
            file = new File(this.getProfilePath(), path);
            if (file.exists()) {
                return file.toURI();
            }
            // attempt to return path from preferences directory
            file = new File(this.getPreferencesPath(), path);
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
            file = new File(this.getProgramPath() + path);
            if (file.exists()) {
                return file.toURI();
            }
        }
        if (locations == Location.ALL || locations == Location.INSTALLED) {
            // return path if in jmri.jar or null
            // The ClassLoader needs paths to use /
            path = path.replace(File.separatorChar, '/');
            URL url = FileUtilSupport.class.getClassLoader().getResource(path);
            if (url == null) {
                url = FileUtilSupport.class.getResource(path);
                if (url == null) {
                    log.debug("{} not found in classpath", path);
                }
            }
            try {
                resource = (url != null) ? url.toURI() : null;
            } catch (URISyntaxException ex) {
                log.warn("Unable to get URI for {}", path, ex);
            }
        }
        // if a resource has not been found and path is absolute and exists
        // return it
        if (resource == null) {
            file = new File(path);
            if (file.isAbsolute() && file.exists()) {
                return file.toURI();
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
    public URL findURL(@Nonnull String path) {
        return this.findURL(path, new String[]{});
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
    public URL findURL(@Nonnull String path, @Nonnull String... searchPaths) {
        return this.findURL(path, Location.ALL, searchPaths);
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
    public URL findURL(@Nonnull String path, Location locations) {
        return this.findURL(path, locations, new String[]{});
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
     * {@link FileUtil#PROGRAM} directory and JARs in the class
     * path</li><li>{@link Location#USER} limits the search to the
     * {@link FileUtil#PROFILE} directory</li></ol>
     *
     * @param path        The relative path of the file or resource
     * @param locations   The types of locations to limit the search to
     * @param searchPaths a list of paths to search for the path in
     * @return The URL or null
     * @see #findURL(java.lang.String)
     * @see #findURL(java.lang.String, jmri.util.FileUtil.Location)
     * @see #findURL(java.lang.String, java.lang.String...)
     */
    public URL findURL(@Nonnull String path, @Nonnull Location locations, @Nonnull String... searchPaths) {
        URI file = this.findURI(path, locations, searchPaths);
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
     * @param url the URL
     * @return a URI or null if the conversion would have caused a
     *         {@link java.net.URISyntaxException}
     */
    public URI urlToURI(@Nonnull URL url) {
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
    public URL fileToURL(@Nonnull File file) {
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
     * @return the JAR file containing the JMRI library or null if not running
     *         from a JAR file
     */
    public JarFile getJmriJarFile() {
        if (jarPath == null) {
            CodeSource sc = FileUtilSupport.class.getProtectionDomain().getCodeSource();
            if (sc != null) {
                jarPath = sc.getLocation().toString();
                if (jarPath.startsWith("jar:file:")) {
                    // 9 = length of jar:file:
                    jarPath = jarPath.substring(9, jarPath.lastIndexOf("!"));
                } else {
                    log.info("Running from classes not in jar file.");
                    jarPath = ""; // set to empty String to bypass search
                    return null;
                }
                log.debug("jmri.jar path is {}", jarPath);
            }
            if (jarPath == null) {
                log.error("Unable to locate jmri.jar");
                jarPath = ""; // set to empty String to bypass search
                return null;
            }
        }
        if (!jarPath.isEmpty()) {
            try {
                return new JarFile(jarPath);
            } catch (IOException ex) {
                log.error("Unable to open jmri.jar", ex);
                return null;
            }
        }
        return null;
    }

    /**
     * Read a text file into a String.
     *
     * @param file The text file.
     * @return The contents of the file.
     * @throws java.io.IOException if the file cannot be read
     */
    public String readFile(@Nonnull File file) throws IOException {
        return this.readURL(this.fileToURL(file));
    }

    /**
     * Read a text URL into a String.
     *
     * @param url The text URL.
     * @return The contents of the file.
     * @throws java.io.IOException if the URL cannot be read
     */
    public String readURL(@Nonnull URL url) throws IOException {
        try {
            try (InputStreamReader in = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
                    BufferedReader reader = new BufferedReader(in)) {
                return reader.lines().collect(Collectors.joining("\n")); // NOI18N
            }
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
    @Nonnull
    public String sanitizeFilename(@Nonnull String name) {
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
     * @param path directory to create
     */
    public void createDirectory(@Nonnull String path) {
        this.createDirectory(new File(path));
    }

    /**
     * Create a directory if required. Any parent directories will also be
     * created.
     *
     * @param dir directory to create
     */
    public void createDirectory(@Nonnull File dir) {
        if (!dir.exists()) {
            log.debug("Creating directory: {}", dir);
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
     * @param path path to delete
     * @return true if path was deleted, false otherwise
     */
    public boolean delete(@Nonnull File path) {
        if (path.isDirectory()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    this.delete(file);
                }
            }
        }
        return path.delete();
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
    public void copy(@Nonnull File source, @Nonnull File dest) throws IOException {
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
    public void appendTextToFile(@Nonnull File file, @Nonnull String text) throws IOException {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            pw.println(text);
        }
    }

    /**
     * Backup a file. The backup is in the same location as the original file,
     * has the extension <code>.bak</code> appended to the file name, and up to
     * four revisions are retained. The lowest numbered revision is the most
     * recent.
     *
     * @param file the file to backup
     * @throws java.io.IOException if a backup cannot be created
     */
    public void backup(@Nonnull File file) throws IOException {
        this.rotate(file, 4, "bak");
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
     * @see #backup(java.io.File)
     */
    public void rotate(@Nonnull File file, int max, @CheckForNull String extension) throws IOException {
        if (max < 1) {
            throw new IllegalArgumentException();
        }
        String name = file.getName();
        if (extension != null) {
            if (extension.length() > 0 && !extension.startsWith(".")) {
                extension = "." + extension;
            }
        } else {
            extension = "";
        }
        File dir = file.getParentFile();
        File source;
        int i = max;
        while (i > 1) {
            source = new File(dir, name + "." + (i - 1) + extension);
            if (source.exists()) {
                this.copy(source, new File(dir, name + "." + i + extension));
            }
            i--;
        }
        this.copy(file, new File(dir, name + "." + i + extension));
    }

    /**
     * Get the default instance of a FileUtilSupport object.
     * <p>
     * Unlike most implementations of getDefault(), this does not return an
     * object held by {@link jmri.InstanceManager} due to the need for this
     * default instance to be available prior to the creation of an
     * InstanceManager.
     *
     * @return the default FileUtilSupport instance, creating it if necessary
     */
    public static FileUtilSupport getDefault() {
        if (FileUtilSupport.defaultInstance == null) {
            FileUtilSupport.defaultInstance = new FileUtilSupport();
        }
        return FileUtilSupport.defaultInstance;
    }

    /**
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
     *             profile:, program:, preference:, scripts:, settings, or
     *             resource:
     * @return Canonical path to use, or null if one cannot be found.
     * @since 2.7.2
     */
    private String pathFromPortablePath(@CheckForNull Profile profile, @Nonnull String path) {
        if (path.startsWith(PROGRAM)) {
            if (new File(path.substring(PROGRAM.length())).isAbsolute()) {
                path = path.substring(PROGRAM.length());
            } else {
                path = path.replaceFirst(PROGRAM, Matcher.quoteReplacement(this.getProgramPath()));
            }
        } else if (path.startsWith(PREFERENCES)) {
            if (new File(path.substring(PREFERENCES.length())).isAbsolute()) {
                path = path.substring(PREFERENCES.length());
            } else {
                path = path.replaceFirst(PREFERENCES, Matcher.quoteReplacement(this.getUserFilesPath(profile)));
            }
        } else if (path.startsWith(PROFILE)) {
            if (new File(path.substring(PROFILE.length())).isAbsolute()) {
                path = path.substring(PROFILE.length());
            } else {
                path = path.replaceFirst(PROFILE, Matcher.quoteReplacement(this.getProfilePath(profile)));
            }
        } else if (path.startsWith(SCRIPTS)) {
            if (new File(path.substring(SCRIPTS.length())).isAbsolute()) {
                path = path.substring(SCRIPTS.length());
            } else {
                path = path.replaceFirst(SCRIPTS, Matcher.quoteReplacement(this.getScriptsPath(profile)));
            }
        } else if (path.startsWith(SETTINGS)) {
            if (new File(path.substring(SETTINGS.length())).isAbsolute()) {
                path = path.substring(SETTINGS.length());
            } else {
                path = path.replaceFirst(SETTINGS, Matcher.quoteReplacement(this.getPreferencesPath()));
            }
        } else if (path.startsWith(HOME)) {
            if (new File(path.substring(HOME.length())).isAbsolute()) {
                path = path.substring(HOME.length());
            } else {
                path = path.replaceFirst(HOME, Matcher.quoteReplacement(this.getHomePath()));
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

}
