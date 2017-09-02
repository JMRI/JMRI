package jmri.jmrit.vsdecoder.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of VSDecoderPreferencesPane
 *
 * @author	Paul Bender Copyright (C) 2015,2016
 */
public class VSDecoderPreferencesPaneTest {

    @Test
    public void testCtor() {
        VSDecoderPreferencesPane frame = new VSDecoderPreferencesPane();
        Assert.assertNotNull("exists", frame );
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
