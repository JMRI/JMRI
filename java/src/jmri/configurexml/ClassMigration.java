package jmri.configurexml;

import java.util.Map;
import javax.annotation.Nonnull;

/**
 * Interface for service provider instances that provide a class migration path
 * that the {@link ConfigXmlManager} must be made aware of.
 *
 * @author Randall Wood Copyright 2017
 */
/*
 * Note this is deliberately not advertised in help/html/doc/Technical/plugins.shtml
 * since it enables the overriding of JMRI behaviors with a plugin instead of the
 * extension of JMRI with a plugin
 */
public interface ClassMigration {

    /**
     * Get the map of migrated classes, where to the class to be migrated from
     * is the key, and its replacement is the value.
     *
     * @return the map of migrations, or an empty map if none are provided.
     */
    @Nonnull
    public Map<String, String> getMigrations();

}
