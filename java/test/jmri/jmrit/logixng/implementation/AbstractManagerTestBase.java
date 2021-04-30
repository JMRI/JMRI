/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.logixng.implementation;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;

import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertTrue(m == _manager);
    }
    
}
