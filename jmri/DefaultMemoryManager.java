// DefaultMemoryManager.java

package jmri;

/**
 * Provide the concrete implementation for the Internal Memory Manager.
 *
 * @author			Bob Jacobsen Copyright (C) 2004
 * @version			$Revision: 1.1 $
 */
public class DefaultMemoryManager extends AbstractMemoryManager {

   public char systemLetter() { return 'I'; }

   protected Memory createNewMemory(String systemName, String userName){
   	return new DefaultMemory(systemName, userName);
   }

   public static DefaultMemoryManager instance() {
   	if (_instance == null) _instance = new DefaultMemoryManager();
   	return _instance;
   }
   private static DefaultMemoryManager _instance;
   
}

/* @(#)DefaultMemoryManager.java */
