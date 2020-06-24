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
     * @param msg 
     */
    public void notifyError(String msg);
    
    /**
     * Get a description of this ErrorNotifier.
     * @return the description
     */
    public String getDescription();
    
    public enum ErrorNotifierResponse {
        OK(Bundle.getMessage("ErrorNotifierResponse_OK")),
        MUTE(Bundle.getMessage("ErrorNotifierResponse_OK"));
        
        private String _message;
        
        private ErrorNotifierResponse(String message) {
            _message = message;
        }
        
        @Override
        public String toString() {
            return _message;
        }
        
    }
}
