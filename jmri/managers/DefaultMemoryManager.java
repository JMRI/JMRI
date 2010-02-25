// DefaultMemoryManager.java

package jmri.managers;

import jmri.*;
import jmri.implementation.DefaultMemory;

/**
 * Provide the concrete implementation for the Internal Memory Manager.
 *
 * @author			Bob Jacobsen Copyright (C) 2004
 * @version			$Revision: 1.4 $
 */
public class DefaultMemoryManager extends AbstractMemoryManager {

    public char systemLetter() { return 'I'; }

    protected Memory createNewMemory(String systemName, String userName){
        // we've decided to enforce that memory system
        // names start with IM by prepending if not present
        if (!systemName.startsWith("IM"))
            systemName = "IM"+systemName;
        return new DefaultMemory(systemName, userName);
    }
   
}

/* @(#)DefaultMemoryManager.java */
