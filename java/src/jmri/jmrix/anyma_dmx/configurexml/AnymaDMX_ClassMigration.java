package jmri.jmrix.anyma_dmx.configurexml;

import java.util.HashMap;
import java.util.Map;
import jmri.configurexml.ClassMigration;
import org.openide.util.lookup.ServiceProvider;

/**
 * Anyma DMX class migrations for the
 * {@link jmri.configurexml.ConfigXmlManager} to use.
 *
 * @author Randall Wood Copyright 2017
 * @author George Warner Copyright (C) 2017
 * @since       4.9.6
 */
@ServiceProvider(service = ClassMigration.class)
public class AnymaDMX_ClassMigration implements ClassMigration {

    @Override
    public Map<String, String> getMigrations() {
        Map<String, String> migrations = new HashMap<>();
        migrations.put("jmri.jmrix.anyma_dmx.configurexml.ConnectionConfigXml", "jmri.jmrix.anyma_dmx.configurexml.AnymaDMX_ConnectionConfigXml");
        return migrations;
    }

}
