package jmri.jmrix.pi.configurexml;

import java.util.HashMap;
import java.util.Map;
import jmri.configurexml.ClassMigration;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default class migrations for the {@link jmri.configurexml.ConfigXmlManager}
 * to use.
 *
 * @author Randall Wood Copyright 2017
 */
@ServiceProvider(service = ClassMigration.class)
public class RaspberryPiClassMigration implements ClassMigration {

    @Override
    public Map<String, String> getMigrations() {
        Map<String, String> migrations = new HashMap<>();
        migrations.put("jmri.jmrix.pi.ConnectionConfigXML", "jmri.jmrix.pi.RaspberryPiConnectionConfigXML");
        return migrations;
    }

}
