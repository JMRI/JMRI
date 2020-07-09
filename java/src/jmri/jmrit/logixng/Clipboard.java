package jmri.jmrit.logixng;

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
    public void add(MaleSocket maleSocket);
    
    /**
     * Get the top item on the clipboard and removes it from the clipboard.
     * <P>
     * The top item is the last item put on the clipboard
     * 
     * @return the top item
     */
    public MaleSocket getTopItem();
    
    /**
     * Get the root of the clipboard tree.
     * 
     * @return the root female socket
     */
    public FemaleSocket getRoot();
    
    /**
     * Moves an item on the clipboard to the top of the clipboard.
     * <P>
     * If an item on the clipboard that is not the top item is cut, it's
     * placed as the top item on the clipboard until it's pasted elsewhere.
     * 
     * @param maleSocket the male socket to put on the top
     */
    public void moveItemToTop(MaleSocket maleSocket);
    
}
