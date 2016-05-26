package jmri.util;

import java.io.File;
import java.io.IOException;
import java.security.CodeSource;
import java.util.jar.JarFile;
import javax.annotation.Nonnull;
import jmri.beans.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support the {@link jmri.util.FileUtil } static API while providing
 * {@link java.beans.PropertyChangeSupport} for listening to changes in the
 * paths.
 *
 * @author Randall Wood (C) 2015
 */
public class FileUtilSupport extends Bean {

    /* User's home directory */
    private static final String homePath = System.getProperty("user.home") + File.separator; // NOI18N
    /*
     * Settable directories
     */
    /* JMRI program path, defaults to directory JMRI is executed from */
    private String programPath = null;
    /* path to jmri.jar */
    private String jarPath = null;
    /* path to the jython scripts directory */
    private String scriptsPath = null;
    /* path to the user's files directory */
    private String userFilesPath = null;
    /* path to the current profile */
    private String profilePath = null;
    // initialize logging
    private static final Logger log = LoggerFactory.getLogger(FileUtilSupport.class);
    // default instance
    volatile private static FileUtilSupport defaultInstance = null;

    /**
     * Get the user's home directory.
     *
     * @return User's home directory as a String
     */
    public String getHomePath() {
        return homePath;
    }

    /**
     * Get the user's files directory. If not set by the user, this is the same
     * as the profile path.
     *
     * @see #getProfilePath()
     * @return User's files directory as a String
     */
    public String getUserFilesPath() {
        return (this.userFilesPath != null) ? this.userFilesPath : this.getProfilePath();
    }

    /**
     * Set the user's files directory.
     *
     * @see #getUserFilesPath()
     * @param path The path to the user's files directory using system-specific separators
     */
    public void setUserFilesPath(String path) {
        String old = this.userFilesPath;
        if (path != null && !path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        this.userFilesPath = path;
        if ((old != null && !old.equals(path)) || (path != null && !path.equals(old))) {
            this.firePropertyChange(FileUtil.PREFERENCES, old, path);
        }
    }

    /**
     * Get the profile directory. If not set, this is the same as the
     * preferences path.
     *
     * @see #getPreferencesPath()
     * @return Profile directory as a String using system-specific separators
     */
    public String getProfilePath() {
        return (this.profilePath != null) ? this.profilePath : this.getPreferencesPath();
    }

    /**
     * Set the profile directory.
     *
     * @see #getProfilePath()
     * @param path The path to the profile directory using system-specific separators.
     */
    public void setProfilePath(String path) {
        String old = this.profilePath;
        if (path != null && !path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        this.profilePath = path;
        if ((old != null && !old.equals(path)) || (path != null && !path.equals(old))) {
            this.firePropertyChange(FileUtil.PROFILE, old, path);
        }
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
     * @return Path to the preferences directory using system-specific separators.
     */
    public String getPreferencesPath() {
        // return jmri.prefsdir property if present
        String jmriPrefsDir = System.getProperty("jmri.prefsdir", ""); // NOI18N
        if (!jmriPrefsDir.isEmpty() && !jmriPrefsDir.endsWith(File.separator)) {
            return jmriPrefsDir + File.separator;
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
        // logging here merely throws warnings since we call this method to setup logging
        // uncomment below to print OS default to console
        // System.out.println("preferencesPath defined as \"" + result + "\" based on os.name=\"" + SystemType.getOSName() + "\"");
        return result;
    }

    /**
     * Get the JMRI program directory.
     *
     * @return JMRI program directory as a String.
     */
    public String getProgramPath() {
        if (programPath == null) {
            this.setProgramPath("."); // NOI18N
        }
        return programPath;
    }

    /**
     * Set the JMRI program directory.
     *
     * Convenience method that calls
     * {@link FileUtil#setProgramPath(java.io.File)} with the passed in path.
     *
     */
    public void setProgramPath(String path) {
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
     */
    public void setProgramPath(File path) {
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
     * @return path to [user's file]/resources/  using system-specific separators
     */
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
     * Get the path to the scripts directory.
     *
     * @return the scriptsPath using system-specific separators
     */
    public String getScriptsPath() {
        if (scriptsPath != null) {
            return scriptsPath;
        }
        // scriptsPath not set by user, return default if it exists
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
     * @param path the scriptsPath to set
     */
    public void setScriptsPath(String path) {
        String old = this.scriptsPath;
        if (path != null && !path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        this.scriptsPath = path;
        if ((old != null && !old.equals(path)) || (path != null && !path.equals(old))) {
            this.firePropertyChange(FileUtil.SCRIPTS, old, path);
        }
    }

    /**
     * Get the JMRI distribution jar file.
     *
     * @return a {@link java.util.jar.JarFile} pointing to jmri.jar or null
     */
    public JarFile getJmriJarFile() {
        if (jarPath == null) {
            CodeSource sc = FileUtil.class.getProtectionDomain().getCodeSource();
            if (sc != null) {
                jarPath = sc.getLocation().toString();
                // 9 = length of jar:file:
                jarPath = jarPath.substring(9, jarPath.lastIndexOf("!"));
                log.debug("jmri.jar path is {}", jarPath);
            }
            if (jarPath == null) {
                log.error("Unable to locate jmri.jar");
                return null;
            }
        }
        try {
            return new JarFile(jarPath);
        } catch (IOException ex) {
            log.error("Unable to open jmri.jar", ex);
            return null;
        }
    }

    /**
     * Backup a file. The backup is in the same location as the original file,
     * has the extension <code>.bak</code> appended to the file name, and up to
     * four revisions are retained. The lowest numbered revision is the most
     * recent.
     *
     */
    public void backup(File file) throws IOException {
        this.rotate(file, 4, "bak");
    }

    /**
     * Rotate a file.
     *
     * @param file      The file to rotate
     * @param max       A positive integer
     * @param extension The extension to use for the rotations. If null or an
     *                  empty string, the rotation number is used as the
     *                  extension.
     * @throws IllegalArgumentException if max is less than one
     * @see #backup(java.io.File) 
     */
    public void rotate(@Nonnull File file, int max, String extension) throws IOException {
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
                FileUtil.copy(source, new File(dir, name + "." + i + extension));
            }
            i--;
        }
        FileUtil.copy(file, new File(dir, name + "." + i + extension));
    }

    public static FileUtilSupport getDefault() {
        if (FileUtilSupport.defaultInstance == null) {
            FileUtilSupport.defaultInstance = new FileUtilSupport();
        }
        return FileUtilSupport.defaultInstance;
    }
}
