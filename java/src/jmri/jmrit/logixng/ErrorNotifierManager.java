package jmri.jmrit.logixng;

import java.util.Set;

/**
 * Manager for ErrorNotifier implementations.
 * <P>
 * Several ErrorNotifier classes may be registered, if for example, the user
 * wants the notification both as a dialog on the screen and on a web page
 * using json message.
 * 
 * @author Daniel Bergqvist 2020
 */
public interface ErrorNotifierManager {
    
    /**
     * Notifies the ErrorNotifiers.
     * <P>
     * Note that the manager must ensure that ErrorNotifiers that hasn't
     * responded, or responded with "mute", shouldn't get more notifiers.
     * 
     * @param object the object that notifies about the error
     * @param msg the message
     * @param e the exception or null if no exception
     */
    public void notifyError(Base object, String msg, Exception e);
    
    public void registerErrorNotifier(ErrorNotifier errorNotifier);
    
    public void unregisterErrorNotifier(ErrorNotifier errorNotifier);
    
    public void clearErrorNotifiers();
    
    public Set<ErrorNotifier> getErrorNotifiers();
    
    /**
     * Response from the ErrorNotifier that it's OK to send more error messages
     * to this ErrorNotifier.
     * <P>
     * The ErrorNotifier can be un-muted by calling responseOK() for the
     * ErrorNotifier.
     * 
     * @param errorNotifier the ErrorNotifier that this is a response for
     */
    public void responseOK(ErrorNotifier errorNotifier);
    
    /**
     * Response from the ErrorNotifier that no more error messages should
     * be notified to this ErrorNotifier.
     * <P>
     * The ErrorNotifier can be un-muted by calling responseOK() for the
     * ErrorNotifier.
     * 
     * @param errorNotifier the ErrorNotifier that this is a response for
     */
    public void responseMute(ErrorNotifier errorNotifier);
    
    
    public enum State {
        /**
         * Ready for notifing the user
         */
        READY,
        
        /**
         * Waiting for the user to respond to the message. No new notifications
         * should be done until state is READY again.
         */
        WAITING,
        
        /**
         * The ErrorNotifier is muted and will not receive any more notfications
         * until it's un.muted.
         */
        MUTED,
    }
    
}
