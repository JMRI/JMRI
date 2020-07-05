package jmri.configurexml;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default class migrations for the {@link ConfigXmlManager} to use.
 *
 * @author Randall Wood Copyright 2017
 */
@ServiceProvider(service = ClassMigration.class)
@API(status = EXPERIMENTAL)
public class DefaultClassMigration implements ClassMigration {

    @Override
    public Map<String, String> getMigrations() {
        Map<String, String> migrations = new HashMap<>();
        ResourceBundle bundle = ResourceBundle.getBundle("jmri.configurexml.ClassMigration");
        bundle.keySet().forEach((key) -> {
            migrations.put(key, bundle.getString(key));
        });
        return migrations;
    }

}
