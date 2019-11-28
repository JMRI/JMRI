package jmri.profile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.annotation.Nonnull;

/**
 * A JMRI application profile. Profiles allow a JMRI application to load
 * completely separate set of preferences at each launch without relying on host
 * OS-specific tricks to ensure this happens.
 *
 * It is recommended that profile directory names end in {@value #EXTENSION} so
 * that supporting iOS and macOS applications could potentially treat a JMRI
 * profile as a single file, instead of as a directory structure. This would
 * allow an application subject to mandatory security controls in iOS, and an
 * application sandbox on macOS to request permission from the user to access
 * the entire profile once, instead of needing to request permission to access
 * each file individually. This would also allow a profile to be opened by
 * double clicking on it, and to have a unique icon within the iOS Files app and
 * macOS Finder.
 * 
 * Note that JMRI itself is not currently capable of supporting opening a
 * profile by double clicking on it, even if other applications on the same
 * computer can.
 * 
 * @author Randall Wood Copyright (C) 2013, 2014, 2015, 2018
 */
public class Profile implements Comparable<Profile> {

    private String name;
    private String id;
    private File path;
    public static final String PROFILE = "profile"; // NOI18N
    protected static final String ID = "id"; // NOI18N
    protected static final String NAME = "name"; // NOI18N
    protected static final String PATH = "path"; // NOI18N
    public static final String PROPERTIES = "profile.properties"; // NOI18N
    public static final String CONFIG = "profile.xml"; // NOI18N
    public static final String SHARED_PROPERTIES = PROFILE + "/" + PROPERTIES; // NOI18N
    public static final String SHARED_CONFIG = PROFILE + "/" + CONFIG; // NOI18N
    /**
     * {@value #CONFIG_FILENAME} may be present in older profiles
     */
    public static final String CONFIG_FILENAME = "ProfileConfig.xml"; // NOI18N
    public static final String UI_CONFIG = "user-interface.xml"; // NOI18N
    public static final String SHARED_UI_CONFIG = PROFILE + "/" + UI_CONFIG; // NOI18N
    /**
     * {@value #UI_CONFIG_FILENAME} may be present in older profiles
     */
    public static final String UI_CONFIG_FILENAME = "UserPrefsProfileConfig.xml"; // NOI18N
    /**
     * The filename extension for JMRI profile directories. This is needed for
     * external applications on some operating systems to recognize JMRI
     * profiles.
     */
    public static final String EXTENSION = ".jmri"; // NOI18N

    /**
     * Create a Profile object given just a path to it. The Profile must exist
     * in storage on the computer.
     *
     * @param path The Profile's directory
     * @throws java.io.IOException If unable to read the Profile from path
     */
    public Profile(@Nonnull File path) throws IOException {
        this(path, true);
    }

    /**
     * Create a Profile object and a profile in storage. A Profile cannot exist
     * in storage on the computer at the path given. Since this is a new
     * profile, the id must match the last element in the path.
     * <p>
     * This is the only time the id can be set on a Profile, as the id becomes a
     * read-only property of the Profile. The {@link ProfileManager} will only
     * load a single profile with a given id.
     *
     * @param name Name of the profile. Will not be used to enforce uniqueness
     *             constraints.
     * @param id   Id of the profile. Will be prepended to a random String to
     *             enforce uniqueness constraints.
     * @param path Location to store the profile; {@value #EXTENSION} will be
     *             appended to this path if needed.
     * @throws java.io.IOException If unable to create the profile at path
     * @throws IllegalArgumentException If a profile already exists at or within path
     */
    public Profile(@Nonnull String name, @Nonnull String id, @Nonnull File path) throws IOException, IllegalArgumentException {
        File pathWithExt; // path with extension
        if (path.getName().endsWith(EXTENSION)) {
            pathWithExt = path;
        } else {
            pathWithExt = new File(path.getParentFile(), path.getName() + EXTENSION);
        }
        if (!pathWithExt.getName().equals(id + EXTENSION)) {
            throw new IllegalArgumentException(id + " " + path.getName() + " do not match"); // NOI18N
        }
        if (Profile.isProfile(path) || Profile.isProfile(pathWithExt)) {
            throw new IllegalArgumentException("A profile already exists at " + path); // NOI18N
        }
        if (Profile.containsProfile(path) || Profile.containsProfile(pathWithExt)) {
            throw new IllegalArgumentException(path + " contains a profile in a subdirectory."); // NOI18N
        }
        if (Profile.inProfile(path) || Profile.inProfile(pathWithExt)) {
            if (Profile.inProfile(path)) log.warn("Exception: Path {} is within an existing profile.", path, new Exception("traceback")); // NOI18N
            if (Profile.inProfile(pathWithExt)) log.warn("Exception: pathWithExt {} is within an existing profile.", pathWithExt, new Exception("traceback")); // NOI18N
            throw new IllegalArgumentException(path + " is within an existing profile."); // NOI18N
        }
        this.name = name;
        this.id = id + "." + ProfileManager.createUniqueId();
        this.path = pathWithExt;
        // use field, not local variables (path or pathWithExt) for paths below
        if (!this.path.exists() && !this.path.mkdirs()) {
            throw new IOException("Unable to create directory " + this.path); // NOI18N
        }
        if (!this.path.isDirectory()) {
            throw new IllegalArgumentException(path + " is not a directory"); // NOI18N
        }
        this.save();
        if (!Profile.isProfile(this.path)) {
            throw new IllegalArgumentException(path + " does not contain a profile.properties file"); // NOI18N
        }
    }

    /**
     * Create a Profile object given just a path to it. If isReadable is true,
     * the Profile must exist in storage on the computer. Generates a random id
     * for the profile.
     * <p>
     * This method exists purely to support subclasses.
     *
     * @param path       The Profile's directory
     * @param isReadable True if the profile has storage. See
     *                   {@link jmri.profile.NullProfile} for a Profile subclass
     *                   where this is not true.
     * @throws java.io.IOException If the profile's preferences cannot be read.
     */
    protected Profile(@Nonnull File path, boolean isReadable) throws IOException {
        this(path, ProfileManager.createUniqueId(), isReadable);
    }

    /**
     * Create a Profile object given just a path to it. If isReadable is true,
     * the Profile must exist in storage on the computer.
     * <p>
     * This method exists purely to support subclasses.
     *
     * @param path       The Profile's directory
     * @param id         The Profile's id
     * @param isReadable True if the profile has storage. See
     *                   {@link jmri.profile.NullProfile} for a Profile subclass
     *                   where this is not true.
     * @throws java.io.IOException If the profile's preferences cannot be read.
     */
    protected Profile(@Nonnull File path, @Nonnull String id, boolean isReadable) throws IOException {
        File pathWithExt; // path with extension
        if (path.getName().endsWith(EXTENSION)) {
            pathWithExt = path;
        } else {
            pathWithExt = new File(path.getParentFile(), path.getName() + EXTENSION);
        }
        // if path does not exist, but pathWithExt exists, use pathWithExt
        // to support a scenario where user adds .jmri extension to profile
        // directory outside of JMRI application
        if ((!path.exists() && pathWithExt.exists())) {
            this.path = pathWithExt;
        } else {
            this.path = path;
        }
        this.id = id;
        if (isReadable) {
            this.readProfile();
        }
    }

    protected final void save() throws IOException {
        ProfileProperties p = new ProfileProperties(this);
        p.put(NAME, this.name, true);
        p.put(ID, this.id, true);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name for this profile.
     * <p>
     * Overriding classing must use
     * {@link #setNameInConstructor(java.lang.String)} to set the name in a
     * constructor since this method passes this Profile object to an object
     * excepting a completely constructed Profile.
     *
     * @param name the new name
     */
    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        ProfileManager.getDefault().profileNameChange(this, oldName);
    }

    /**
     * Set the name for this profile while constructing the profile.
     * <p>
     * Overriding classing must use this method to set the name in a constructor
     * since {@link #setName(java.lang.String)} passes this Profile object to an
     * object expecting a completely constructed Profile.
     *
     * @param name the new name
     */
    protected final void setNameInConstructor(String name) {
        this.name = name;
    }

    /**
     * @return the id
     */
    @Nonnull
    public String getId() {
        return id;
    }

    /**
     * @return the path
     */
    public File getPath() {
        return path;
    }

    private void readProfile() throws IOException {
        ProfileProperties p = new ProfileProperties(this.path);
        this.id = p.get(ID, true);
        this.name = p.get(NAME, true);
        if (this.id == null) {
            this.readProfileXml();
            this.save();
        }
    }

    /**
     * @deprecated since 4.1.1; Remove sometime after the new profiles get
     * entrenched (JMRI 5.0, 6.0?)
     */
    @Deprecated
    private void readProfileXml() throws IOException {
        Properties p = new Properties();
        File f = new File(this.path, PROPERTIES);
        FileInputStream is = null;
        try {
            is = new FileInputStream(f);
            p.loadFromXML(is);
            is.close();
        } catch (IOException ex) {
            if (is != null) {
                is.close();
            }
            throw ex;
        }
        this.id = p.getProperty(ID);
        this.name = p.getProperty(NAME);
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    /**
     * {@inheritDoc}
     * This tests for equal ID values
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Profile other = (Profile) obj;
        return !((this.id == null) ? (other.id != null) : !this.id.equals(other.id));
    }

    /**
     * Test if the profile is complete. A profile is considered complete if it
     * can be instantiated using {@link #Profile(java.io.File)} and has a
     * profile.properties file within its "profile" directory.
     *
     * @return true if profile.properties exists where expected.
     */
    public boolean isComplete() {
        return (new File(this.getPath(), Profile.SHARED_PROPERTIES)).exists();
    }

    /**
     * Return the uniqueness portion of the Profile Id.
     * <p>
     * This portion of the Id is automatically generated when the profile is
     * created.
     *
     * @return An eight-character String of alphanumeric characters.
     */
    public String getUniqueId() {
        return this.id.substring(this.id.lastIndexOf(".") + 1); // NOI18N
    }

    /**
     * Test if the given path or subdirectories contains a Profile.
     *
     * @param path Path to test.
     * @return true if path or subdirectories contains a Profile.
     * @since 3.9.4
     */
    public static boolean containsProfile(File path) {
        if (path.isDirectory()) {
            if (Profile.isProfile(path)) {
                return true;
            } else {
                File[] files = path.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (Profile.containsProfile(file)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Test if the given path is within a directory that is a Profile.
     *
     * @param path Path to test.
     * @return true if path or parent directories is a Profile.
     * @since 3.9.4
     */
    public static boolean inProfile(File path) {
        if (path.getParentFile() != null) {
            if (Profile.isProfile(path.getParentFile())) {
                return true;
            }
            return Profile.inProfile(path.getParentFile());
        }
        return false;
    }

    /**
     * Test if the given path is a Profile.
     *
     * @param path Path to test.
     * @return true if path is a Profile.
     * @since 3.9.4
     */
    public static boolean isProfile(File path) {
        if (path.exists() && path.isDirectory()) {
            // version 2
            if ((new File(path, SHARED_PROPERTIES)).canRead()) {
                return true;
            }
            // version 1
            if ((new File(path, PROPERTIES)).canRead() && !path.getName().equals(PROFILE)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(Profile o) {
        if (this.equals(o)) {
            return 0;
        }
        String thisString = "" + this.getName() + this.getPath();
        String thatString = "" + o.getName() + o.getPath();
        return thisString.compareTo(thatString);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Profile.class);
}
