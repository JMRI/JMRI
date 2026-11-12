package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Enumeration;
import java.util.Objects;

import jmri.InstanceManager;
import jmri.SignalSystem;
import jmri.SignalSystemManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Every aspect a BR-2003 appearance file shows is defined by the BR-2003
 * signal system, with a speed, so that a mast of any BR-2003 type has a
 * speed and description for each of its aspects.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class BR2003AspectsDefinedTest {

    @Test
    public void testEveryAppearanceAspectIsDefined() {
        SignalSystem system = Objects.requireNonNull(
                InstanceManager.getDefault(SignalSystemManager.class).getSystem("BR-2003"), "BR-2003 signal system");
        File[] files = new File("xml/signals/BR-2003").listFiles((d, n) -> n.startsWith("appearance-") && n.endsWith(".xml"));
        assertNotNull(files);
        assertTrue(files.length > 0, "BR-2003 has appearance files");
        int checked = 0;
        for (File f : files) {
            String mapName = f.getName().substring("appearance-".length(), f.getName().length() - ".xml".length());
            DefaultSignalAppearanceMap map = DefaultSignalAppearanceMap.getMap("BR-2003", mapName);
            for (Enumeration<String> e = map.getAspects(); e.hasMoreElements();) {
                String aspect = e.nextElement();
                assertTrue(system.checkAspect(aspect), f.getName() + " uses aspect " + aspect + ", not defined by BR-2003");
                assertNotNull(system.getProperty(aspect, "speed"), aspect + " has a speed");
                checked++;
            }
        }
        assertTrue(checked > 0, "aspects checked");
    }

    @Test
    public void testFeatherMastAspectsHaveNormalSpeed() {
        VirtualSignalMast feather = new VirtualSignalMast("IF$vsm:BR-2003:feather($0001)", "Junction indicator");
        for (String aspect : new String[]{"Zero", "One", "Two", "Three", "Four", "Five", "Six"}) {
            feather.setAspect(aspect);
            assertEquals("Normal", feather.getSpeed(), aspect);
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
