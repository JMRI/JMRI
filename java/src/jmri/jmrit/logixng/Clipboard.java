package jmri.jmrit.logixng;

import java.util.List;

/**
 * The clipboard with actions and expressions
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public interface Clipboard {
    
    /**
     * Is the clipboard empty?
     * 
     * @return true if empty, false otherwise
     */
    public boolean isEmpty();
    
    /**
     * Add an item to the clipboard.
     * <P>
     * The last added item is on the top of the clipboard.
     * 
     * @param maleSocket the item to add on the clipboard
     */
    public boolean add(MaleSocket maleSocket, List<String> errors);
    
    /**
     * Get the top item on the clipboard and remove it from the clipboard.
     * <P>
     * The top item is the last item put on the clipboard
     * 
     * @return the top item
     */
    public MaleSocket fetchTopItem();
    
    /**
     * Get the top item on the clipboard without removing it from the clipboard.
     * <P>
     * The top item is the last item put on the clipboard
     * 
     * @return the top item
     */
    public MaleSocket getTopItem();
    
    /**
     * Get the female socket root of the clipboard tree.
     * 
     * @return the root female socket
     */
    public FemaleSocket getFemaleSocket();
    
    /**
     * Moves an item on the clipboard to the top of the clipboard.
     * <P>
     * If an item on the clipboard that is not the top item is cut, it's
     * placed as the top item on the clipboard until it's pasted elsewhere.
     * 
     * @param maleSocket the male socket to put on the top
     */
    public void moveItemToTop(MaleSocket maleSocket);
    
    /**
     * Setup this object and its children.
     * This method is used to lookup system names for child sockets.
     */
    public void setup();
    
}
