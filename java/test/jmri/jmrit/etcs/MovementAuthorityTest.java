package jmri.jmrit.etcs;

import java.util.ArrayList;
import java.util.List;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for MovementAuthority.
 * @author Steve Young Copyright (C) 2024
 */
public class MovementAuthorityTest {

    @Test
    public void testGetTrackSections() {

        TrackSection s = new TrackSection(10,10,3);
        List<TrackSection> list = new ArrayList<>();
        list.add(s);
        MovementAuthority ma = new MovementAuthority(list);
        Assertions.assertNotNull(ma);

        List<MovementAuthority> mas = new ArrayList<>();
        mas.add(ma);

        List<MovementAuthority> newMas = MovementAuthority.advanceForward(mas, 1);
        Assertions.assertEquals(1, newMas.size());
        MovementAuthority newMa = newMas.get(0);
        List<TrackSection> newTsList = newMa.getTrackSections();
        Assertions.assertEquals(1, newTsList.size());
        TrackSection newTs = newTsList.get(0);
        Assertions.assertEquals(10, newTs.getSpeed());
        Assertions.assertEquals(3, newTs.getGradient());
        Assertions.assertEquals(9, newTs.getLength());

        newMas = MovementAuthority.advanceForward(mas, 9);
        Assertions.assertEquals(1, newMas.size());
        newMa = newMas.get(0);
        newTsList = newMa.getTrackSections();
        Assertions.assertEquals(1, newTsList.size());
        newTs = newTsList.get(0);
        Assertions.assertEquals(10, newTs.getSpeed());
        Assertions.assertEquals(3, newTs.getGradient());
        Assertions.assertEquals(0, newTs.getLength());

        newMas = MovementAuthority.advanceForward(mas, 1);
        Assertions.assertEquals(0, newMas.size());

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
