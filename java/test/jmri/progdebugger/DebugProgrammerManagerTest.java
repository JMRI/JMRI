package jmri.progdebugger;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import jmri.*;

import org.junit.jupiter.api.*;

/**
 * Test the DebugProgrammerManager class.
 *
 * @author Bob Jacobsen Copyright 2002
 */
public class DebugProgrammerManagerTest {

    /**
     * Service mode request returns a programmer
     */
    @Test
    public void testServiceModeRequest() {
        InstanceManager.setDefault(GlobalProgrammerManager.class,
                new DebugProgrammerManager());
        Programmer p = InstanceManager.getDefault(jmri.GlobalProgrammerManager.class)
                .getGlobalProgrammer();
        assertNotNull( p, "got service mode");
        assertInstanceOf(ProgDebugger.ProgDebuggerConfigurator.class,
            p.getConfigurator(), "correct type");
    }

    /**
     * Any service mode request gets the same object
     */
    @Test
    public void testServiceModeUnique() {
        InstanceManager.setDefault(GlobalProgrammerManager.class,
                new DebugProgrammerManager());
        Programmer p = InstanceManager.getDefault(jmri.GlobalProgrammerManager.class)
                .getGlobalProgrammer();
        assertSame( InstanceManager.getDefault( GlobalProgrammerManager.class).getGlobalProgrammer(),
            p, "same service mode programmer");
    }

    /**
     * ops mode request returns a programmer
     */
    @Test
    public void testOpsModeRequest() {
        InstanceManager.store(new DebugProgrammerManager(), AddressedProgrammerManager.class);
        Programmer p = InstanceManager.getDefault( AddressedProgrammerManager.class)
                .getAddressedProgrammer(true, 777);
        assertNotNull( p, "got ops mode");
        assertInstanceOf(ProgDebugger.ProgDebuggerConfigurator.class,
            p.getConfigurator(), "correct type");
    }

    /**
     * Any identical ops mode request gets the same object
     */
    @Test
    public void testOpsModeUnique() {
        InstanceManager.store(new DebugProgrammerManager(), AddressedProgrammerManager.class);
        Programmer p = InstanceManager.getDefault( AddressedProgrammerManager.class)
                .getAddressedProgrammer(true, 777);
        assertSame( InstanceManager.getDefault( AddressedProgrammerManager.class)
            .getAddressedProgrammer(true, 777), p, "same ops mode programmer");
    }

    /**
     * Any identical ops mode request gets the same object
     */
    @Test
    public void testOpsModeDistinct() {
        InstanceManager.store(new DebugProgrammerManager(), AddressedProgrammerManager.class);
        Programmer p = InstanceManager.getDefault(jmri.AddressedProgrammerManager.class)
                .getAddressedProgrammer(true, 777);
        assertNotSame( InstanceManager.getDefault(AddressedProgrammerManager.class)
            .getAddressedProgrammer(true, 888), p, "different ops mode programmer");
        assertSame( InstanceManager.getDefault( AddressedProgrammerManager.class)
            .getAddressedProgrammer(true, 777), p, "same ops mode programmer");
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown(){
        jmri.util.JUnitUtil.tearDown();
    }

}
