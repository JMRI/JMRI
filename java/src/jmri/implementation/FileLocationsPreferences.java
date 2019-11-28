package jmri.implementation;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.prefs.Preferences;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.spi.PreferencesManager;
import jmri.util.FileUtil;
import jmri.util.prefs.AbstractPreferencesManager;
import jmri.util.prefs.InitializationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood (C) 2015
 */
@ServiceProvider(service = PreferencesManager.class)
public class FileLocationsPreferences extends AbstractPreferencesManager {

    public static final String USER_FILES = "user-files"; // NOI18N
    public static final String SCRIPTS = "scripts"; // NOI18N

    @Override
    public void initialize(Profile profile) throws InitializationException {
        if (!this.isInitialized(profile)) {
            Preferences shared = ProfileUtils.getPreferences(profile, this.getClass(), true);
            Preferences perNode = ProfileUtils.getPreferences(profile, this.getClass(), false);
            String userFiles = shared.get(USER_FILES, FileUtil.PROFILE);
            if (!userFiles.startsWith(FileUtil.PROFILE)) {
                userFiles = perNode.get(USER_FILES, userFiles);
            }
            FileUtil.setUserFilesPath(profile, FileUtil.getAbsoluteFilename(profile, userFiles));
            String scripts = shared.get(SCRIPTS, FileUtil.PROFILE);
            if (!scripts.startsWith(FileUtil.PROFILE) && !scripts.startsWith(FileUtil.PROGRAM)) {
                scripts = perNode.get(SCRIPTS, scripts);
            }
            FileUtil.setScriptsPath(profile, FileUtil.getAbsoluteFilename(profile, scripts));
            this.setInitialized(profile, true);
            try {
                if (!FileUtil.getFile(profile, userFiles).isDirectory()) {
                    String message = "UserFilesIsNotDir"; // NOI18N
                    userFiles = FileUtil.getAbsoluteFilename(profile, userFiles);
                    throw new InitializationException(Bundle.getMessage(Locale.ENGLISH, message, userFiles), Bundle.getMessage(message, userFiles));
                }
            } catch (FileNotFoundException ex) {
                String message = "UserFilesDoesNotExist"; // NOI18N
                userFiles = FileUtil.getAbsoluteFilename(profile, userFiles);
                throw new InitializationException(Bundle.getMessage(Locale.ENGLISH, message, userFiles), Bundle.getMessage(message, userFiles));
            }
            try {
                if (!FileUtil.getFile(profile, scripts).isDirectory()) {
                    String message = "ScriptsIsNotDir"; // NOI18N
                    scripts = FileUtil.getAbsoluteFilename(profile, scripts);
                    throw new InitializationException(Bundle.getMessage(Locale.ENGLISH, message, scripts), Bundle.getMessage(message, scripts));
                }
            } catch (FileNotFoundException ex) {
                String message = "ScriptsDoesNotExist"; // NOI18N
                scripts = FileUtil.getAbsoluteFilename(profile, scripts);
                throw new InitializationException(Bundle.getMessage(Locale.ENGLISH, message, scripts), Bundle.getMessage(message, scripts));
            }
        }
    }

    @Override
    public Set<Class<? extends PreferencesManager>> getRequires() {
        return new HashSet<>();
    }

    @Override
    public void savePreferences(Profile profile) {
        Preferences shared = ProfileUtils.getPreferences(profile, this.getClass(), true);
        Preferences perNode = ProfileUtils.getPreferences(profile, this.getClass(), false);
        shared.put(USER_FILES, FileUtil.getPortableFilename(profile, FileUtil.getUserFilesPath(profile), true, false));
        shared.put(SCRIPTS, FileUtil.getPortableFilename(profile, FileUtil.getScriptsPath(profile)));
        perNode.put(USER_FILES, FileUtil.getPortableFilename(profile, FileUtil.getUserFilesPath(profile), true, false));
        perNode.put(SCRIPTS, FileUtil.getPortableFilename(profile, FileUtil.getScriptsPath(profile)));
    }

}
