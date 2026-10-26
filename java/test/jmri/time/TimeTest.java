package jmri.time;

import java.time.*;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test time.
 *
 * @author Daniel Bergqvist (C) 2026
 */
public class TimeTest {

    // This class is currently empty.

    @Test
    public void testDate() {
        ZonedDateTime ldt = ZonedDateTime.parse(
                "Sat Jun 18 01:26:06 EDT 2016",
                TimeProvider.LEGACY_XML_DATETIME_FORMAT);

        System.out.format("DateTime: %s, %s%n", ldt.getZone().getId(), ldt.format(TimeProvider.LEGACY_XML_DATETIME_FORMAT));

/*
        if (1==1) return;

        ZonedDateTime zdt = LocalDateTime.MIN.atZone(ZoneId.of("EDT"));
//        ZonedDateTime zdt2 = LocalDateTime.MIN.atZone(ZoneId.);
        System.out.format("DateTime: %s, %s%n", zdt.getZone().getId(), zdt.format(TimeProvider.LEGACY_XML_DATETIME_FORMAT));
*/
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
