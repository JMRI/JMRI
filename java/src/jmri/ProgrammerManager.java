package jmri;

/**
 * Mange access to available {@link Programmer} objects.
 * <p>
 * Programmers come in two types:
 * <ul>
 * <li>Global, previously "Service Mode" or on a programming track. Request
 * these from an instance of {@link GlobalProgrammerManager}.
 * <li>Addressed, previously "Ops Mode" also known as "programming on the main".
 * Request these from an instance of this interface.
 * </ul>
 * <p>
 * Some systems, notably SPROG hardware, have provided one programmer type, or
 * the other, depending on the system connection preference. This neccessitates 
 * closing and restarting JMRI to change the programmer types.
 * <p>
 * The ProgrammerManager interface allows for control of the programmer types
 * available without restarting and selecting a new connection preference.
 * 
 * @author Andrew crosland Copyright (C) 2020
 */
public interface ProgrammerManager {
    
    enum ProgrammerType {
        NONE,
        ADDRESSED,
        GLOBAL,        
        BOTH
    }

    /**
     * Get the current programmer type
     * 
     * @return the programmer type
     */
    public ProgrammerType getProgrammerType();
    
}
