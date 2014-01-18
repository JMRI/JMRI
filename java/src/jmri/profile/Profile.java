package jmri.profile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * A JMRI application profile. Profiles allow a JMRI application to load
 * completely separate set of preferences at each launch without relying on host
 * OS-specific tricks to ensure this happens.
 *
 * @author rhwood Copyright (C) 2013
 */
public class Profile {

    private String name;
    private String id;
    private File path;
    protected static final String ID = "id"; // NOI18N
    protected static final String NAME = "name"; // NOI18N
    protected static final String PATH = "path"; // NOI18N
    protected static final String PROPERTIES = "profile.properties"; // NOI18N
    public static final String CONFIG_FILENAME = "ProfileConfig.xml"; // NOI18N

    /**
     * Create a Profile object given just a path to it. The Profile must exist
     * in storage on the computer.
     *
     * @param path The Profile's directory
     * @throws IOException
     */
    public Profile(File path) throws IOException {
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
     * @param name
     * @param id
     * @param path
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public Profile(String name, String id, File path) throws IOException, IllegalArgumentException {
        if (!path.getName().equals(id)) {
            throw new IllegalArgumentException(id + " " + path.getName() + " do not match"); // NOI18N
        }
        if ((new File(path, PROPERTIES)).canRead()) {
            throw new IllegalArgumentException("A profile already exists at " + path); // NOI18N
        }
        this.name = name;
        this.id = id + "." + ProfileManager.createUniqueId();
        this.path = path;
        if (path.mkdirs()) {
            this.save();
        }
        if (!path.isDirectory()) {
            throw new IllegalArgumentException(path + " is not a directory"); // NOI18N
        }
        if (!(new File(path, PROPERTIES)).canRead()) {
            throw new IllegalArgumentException(path + " does not contain a profile.properties file"); // NOI18N
        }
    }

    /**
     * Create a Profile object given just a path to it. If isReadable is true,
     * the Profile must exist in storage on the computer.
     *
     * This method exists purely to support subclasses.
     *
     * @param path The Profile's directory
     * @param isReadable
     * @throws IOException
     */
    protected Profile(File path, boolean isReadable) throws IOException {
        this.path = path;
        if (isReadable) {
            this.readProfile();
        }
    }

    private void save() throws IOException {
        Properties p = new Properties();
        File f = new File(this.path, PROPERTIES);
        FileOutputStream os = null;

        p.setProperty(NAME, this.name);
        p.setProperty(ID, this.id);
        if (!f.exists() && !f.createNewFile()) {
            throw new IOException("Unable to create file at " + f.getAbsolutePath()); // NOI18N
        }
        try {
            os = new FileOutputStream(f);
            p.storeToXML(os, "JMRI Profile"); // NOI18N
            os.close();
        } catch (IOException ex) {
            if (os != null) {
                os.close();
            }
            throw ex;
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) throws IOException {
        this.name = name;
        this.save();
    }

    /**
     * @return the id
     */
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
     * ProfileConfig.xml file within it's private directory.
     *
     * @return true if ProfileConfig.xml exists where expected.
     */
    public boolean isComplete() {
        return (new File(this.getPath(), Profile.CONFIG_FILENAME)).exists();
    }

    /**
     * Return the uniqueness portion of the Profile Id.
     *
     * This portion of the Id is automatically generated when the profile is
     * created.
     *
     * @return An eight-character String of alphanumeric characters.
     */
    public String getUniqueId() {
        return this.id.substring(this.id.lastIndexOf(".") + 1); // NOI18N
    }
}
