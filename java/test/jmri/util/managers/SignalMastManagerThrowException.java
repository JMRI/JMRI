/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jmri.util.managers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jmri.SignalMast;
import jmri.managers.DefaultSignalMastManager;

/**
 * This manager always throws an IllegalArgumentException from provide and get methods.
 * It is used for testing exception handling in tests.
 */
public class SignalMastManagerThrowException extends DefaultSignalMastManager {

    public SignalMastManagerThrowException() {
        super();
    }
    
    /** {@inheritDoc} */
    @Override
    public SignalMast provideSignalMast(@Nonnull String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public SignalMast getSignalMast(@Nonnull String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public SignalMast getBySystemName(@Nonnull String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public SignalMast getByUserName(String key) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
}
