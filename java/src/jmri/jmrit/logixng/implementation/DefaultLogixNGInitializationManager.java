package jmri.jmrit.logixng.implementation;

import java.util.*;

import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_InitializationManager;

/**
 * Class providing the basic logic of the LogixNG_InitializationManager interface.
 * 
 * @author Daniel Bergqvist   Copyright (C) 2021
 */
public class DefaultLogixNGInitializationManager implements LogixNG_InitializationManager {
    
    private final List<LogixNG> initLogixNGs = new ArrayList<>();
    
    @Override
    public void add(LogixNG logixNG) {
        initLogixNGs.add(logixNG);
    }
    
    @Override
    public void remove(LogixNG logixNG) {
        initLogixNGs.remove(logixNG);
    }
    
    @Override
    public void moveUp(LogixNG logixNG) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void moveDown(LogixNG logixNG) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public List<LogixNG> getList() {
        return Collections.unmodifiableList(initLogixNGs);
    }
    
}
