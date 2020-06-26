package jmri.jmrit.logixng;

/**
 * Notifies the user that an error has occured.
 * 
 * @author Daniel Bergqvist 2020
 */
public interface ErrorNotifier {
    
    /**
     * Notifies about an error.
     * <P>
     * This method will not be called again on the same class unless the
     * manager
     * @param object the object that notifies about the error
     * @param msg the message
     * @param e the exception or null if no exception
     * @return true if notifications should be put on wait, false if
     * notifications may continue. Example of the later is if the ErrorNotifier
     * logs the message.
     */
    public boolean notifyError(Base object, String msg, Exception e);
    
    /**
     * Get the name of this ErrorNotifier.
     * @return the name
     */
    public String getName();
    
}
