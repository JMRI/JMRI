package jmri.configurexml;

import java.util.HashMap;
import java.util.ServiceLoader;
import javax.annotation.Nonnull;
import jmri.InstanceManagerAutoDefault;

/**
 * Get class migrations for the {@link ConfigXmlManager}.
 *
 * @author Randall Wood Copyright 2017
 */
public class ClassMigrationManager implements InstanceManagerAutoDefault {

    private final HashMap<String, String> migrations = new HashMap<>();

    public ClassMigrationManager() {
        ServiceLoader<ClassMigration> loader = ServiceLoader.load(ClassMigration.class);
        loader.forEach((serviceProvider) -> {
            this.migrations.putAll(serviceProvider.getMigrations());
        });
        loader.reload(); // mark all providers as ready to be garbage collected
    }

    /**
     * Get the class name to be used for the given class name.
     *
     * @param className the class name to check for a migration against
     * @return the class name to use; either the new name to migrate to or the
     *         old name if no migration is required
     */
    @Nonnull
    public String getClassName(@Nonnull String className) {
        String migration = this.migrations.get(className);
        return migration != null ? migration : className;
    }

}
