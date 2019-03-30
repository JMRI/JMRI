package jmri.util.prefs;

import java.io.File;
import java.io.IOException;
import jmri.profile.AuxiliaryConfiguration;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.util.FileUtil;
import jmri.util.node.NodeIdentity;

/**
 *
 * @author Randall Wood
 */
public abstract class AbstractConfigurationProvider {

    protected final Profile project;
    private boolean privateBackedUp = false;
    private boolean sharedBackedUp = false;

    public AbstractConfigurationProvider(Profile project) {
        this.project = project;
    }

    /**
     * Get the {@link jmri.profile.AuxiliaryConfiguration}.
     *
     * @return The AuxiliaryConfiguration.
     */
    protected abstract AuxiliaryConfiguration getConfiguration();

    protected abstract File getConfigurationFile(boolean shared);

    public File getConfigurationDirectory(boolean shared) {
        File dir;
        if (this.project == null) {
            dir = new File(FileUtil.getPreferencesPath(), "preferences"); // NOI18N
        } else {
            dir = new File(this.project.getPath(), Profile.PROFILE);
            if (!shared) {
                File nodeDir = new File(dir, NodeIdentity.storageIdentity());
                if (!nodeDir.exists()) {
                    try {
                        if (!ProfileUtils.copyPrivateContentToCurrentIdentity(project)) {
                            log.debug("Starting profile with new private configuration.");
                        }
                    } catch (IOException ex) {
                        log.debug("Copying existing private configuration failed.");
                    }
                }
                dir = new File(dir, NodeIdentity.storageIdentity());
            }
        }
        FileUtil.createDirectory(dir);
        return dir;
    }

    /**
     * @return the privateBackedUp
     */
    protected boolean isPrivateBackedUp() {
        return privateBackedUp;
    }

    /**
     * @param privateBackedUp the privateBackedUp to set
     */
    protected void setPrivateBackedUp(boolean privateBackedUp) {
        this.privateBackedUp = privateBackedUp;
    }

    /**
     * @return the sharedBackedUp
     */
    protected boolean isSharedBackedUp() {
        return sharedBackedUp;
    }

    /**
     * @param sharedBackedUp the sharedBackedUp to set
     */
    protected void setSharedBackedUp(boolean sharedBackedUp) {
        this.sharedBackedUp = sharedBackedUp;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractConfigurationProvider.class);
}
