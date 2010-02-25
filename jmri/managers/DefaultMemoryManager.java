// DefaultMemoryManager.java

package jmri.managers;

import jmri.*;
import jmri.implementation.DefaultMemory;

/**
 * Provide the concrete implementation for the Internal Memory Manager.
 *
 * @author			Bob Jacobsen Copyright (C) 2004
 * @version			$Revision: 1.3 $
 */
public class DefaultMemoryManager extends AbstractMemoryManager {

    public char systemLetter() { return 'I'; }

    protected Memory createNewMemory(String systemName, String userName){
        // we've decided to enforce that memory system
        // names start with IM
        if (!systemName.startsWith("IM"))
            throw new IllegalArgumentException("System name must start with IM");
        return new DefaultMemory(systemName, userName);
    }
   
}

/* @(#)DefaultMemoryManager.java */
