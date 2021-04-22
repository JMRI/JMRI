package jmri.jmrit.logixng;

import java.util.*;

/**
 * Manager for initialization of LogixNG.
 * This manager has a list of LogixNGs that will be executed before all other
 * LogixNGs are executed.
 * 
 * @author Daniel Bergqvist   Copyright (C) 2021
 */
public interface LogixNG_InitializationManager {
    
    /**
     * Adds a LogixNG to the end of list.
     * @param logixNG the LogixNG
     */
    void add(LogixNG logixNG);
    
    /**
     * Removes a LogixNG from the list.
     * @param logixNG the LogixNG
     */
    void remove(LogixNG logixNG);
    
    /**
     * Moves the LogixNG up (higher priority)
     * @param logixNG the LogixNG
     */
    void moveUp(LogixNG logixNG);
    
    /**
     * Moves the LogixNG down (lower priority)
     * @param logixNG the LogixNG
     */
    void moveDown(LogixNG logixNG);
    
    /**
     * Returns an unmodifiable list of the initialization LogixNGs
     * @return the list
     */
    List<LogixNG> getList();
    
}
