package jmri.jmrit.logixng;

import java.io.PrintWriter;
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
     * Deletes a LogixNG from the list.
     * @param logixNG the LogixNG
     */
    void delete(LogixNG logixNG);
    
    /**
     * Deletes a LogixNG from the list.
     * @param index the index of the LogixNG to delete
     */
    void delete(int index);
    
    /**
     * Moves the LogixNG up (higher priority)
     * @param index the index of the LogixNG to move up
     */
    void moveUp(int index);
    
    /**
     * Moves the LogixNG down (lower priority)
     * @param index the index of the LogixNG to move down
     */
    void moveDown(int index);
    
    /**
     * Returns an unmodifiable list of the initialization LogixNGs
     * @return the list
     */
    List<LogixNG> getList();
    
    /**
     * Print the tree to a stream.
     * 
     * @param locale The locale to be used
     * @param writer the stream to print the tree to
     * @param indent the indentation of each level
     */
    public void printTree(Locale locale, PrintWriter writer, String indent);
    
}
