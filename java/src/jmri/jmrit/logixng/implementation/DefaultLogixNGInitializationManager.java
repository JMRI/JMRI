package jmri.jmrit.logixng.implementation;

import java.io.PrintWriter;
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
    
    /** {@inheritDoc} */
    @Override
    public void add(LogixNG logixNG) {
        initLogixNGs.add(logixNG);
    }
    
    /** {@inheritDoc} */
    @Override
    public void delete(LogixNG logixNG) {
        initLogixNGs.remove(logixNG);
    }
    
    /** {@inheritDoc} */
    @Override
    public void delete(int index) {
        initLogixNGs.remove(index);
    }
    
    /** {@inheritDoc} */
    @Override
    public void moveUp(int index) {
        if ((index == 0) || (index >= initLogixNGs.size())) return;
        LogixNG logixNG = initLogixNGs.remove(index);
        initLogixNGs.add(index-1, logixNG);
    }
    
    /** {@inheritDoc} */
    @Override
    public void moveDown(int index) {
        if (index+1 >= initLogixNGs.size()) return;
        LogixNG logixNG = initLogixNGs.remove(index);
        initLogixNGs.add(index+1, logixNG);
    }
    
    /** {@inheritDoc} */
    @Override
    public List<LogixNG> getList() {
        return Collections.unmodifiableList(initLogixNGs);
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(Locale locale, PrintWriter writer, String indent) {
        writer.println("LogixNG initialization table:");
        for (LogixNG logixNG : initLogixNGs) {
            writer.append(indent);
            writer.append(logixNG.getSystemName());
            writer.append(", ");
            writer.append(logixNG.getUserName());
            writer.println();
        }
        writer.println();
    }
    
}
