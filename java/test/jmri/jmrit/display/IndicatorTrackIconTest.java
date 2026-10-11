package jmri.jmrit.display;

import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of IndicatorTrackIcon
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class IndicatorTrackIconTest extends PositionableIconTest {

    @Test
    @Override
    @DisabledIfHeadless
    public void testCtor() {
        Assertions.assertNotNull( p, "IndicatorTrackIcon Constructor");
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // creates editor
        p = new IndicatorTrackIcon(editor);
    }

}
