package jmri.jmrit.logixng.implementation;

import static org.junit.jupiter.api.Assertions.assertSame;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;

import org.junit.jupiter.api.Test;

/**
 * Test managers
 * 
 * @author Daniel Bergqvist 2020
 */
public abstract class AbstractManagerTestBase {
    
    protected Manager<? extends MaleSocket> _manager;
    
    @Test
    public void testManagerIsRegistered() {
        Manager<? extends MaleSocket> m =
                InstanceManager.getDefault(LogixNG_Manager.class)
                        .getManager(_manager.getClass().getName());
        assertSame( m, _manager);
    }
    
}
