package jmri.jmrit.logixng;

import javax.annotation.CheckForNull;

import jmri.JmriException;

/**
 * A generic female socket
 */
public interface FemaleGenericExpressionSocket
        extends FemaleSocket {

    public enum SocketType {
        DIGITAL("SocketTypeDigital"),
        ANALOG("SocketTypeAnalog"),
        STRING("SocketTypeString"),
        GENERIC("SocketTypeGeneric");
        
        private final String _bundleName;
        
        private SocketType(String bundleName) {
            _bundleName = bundleName;
        }
        
        @Override
        public String toString() {
            return Bundle.getMessage(_bundleName);
        }
        
    }
    
    /**
     * Get the current active socket.
     * @return the currently active socket or null if no socket is active
     */
    public FemaleSocket getCurrentActiveSocket();
    
    /**
     * Set the type of the socket.
     * 
     * @param socketType the type of socket.
     * @throws SocketAlreadyConnectedException if the socket is already
     * connected and if the new type doesn't match the currently connected
     * socket.
     */
    public void setSocketType(SocketType socketType)
            throws SocketAlreadyConnectedException;
    
    /**
     * Get the type of the socket.
     * 
     * @return the type of socket
     */
    public SocketType getSocketType();
    
    /**
     * Evaluate this expression.
     * <P>
     * The return value of the evaluation is converted to a boolean if necessary.
     * <P>
     * The parameter isCompleted is used if the expression should be evaluated
     * more than once. For example, the Count expression is not completed until
     * its child expression has been true and false a number of times.
     * 
     * @return the result of the evaluation
     * @throws jmri.JmriException when an exception occurs
     */
    public boolean evaluateBoolean() throws JmriException;
    
    /**
     * Evaluate this expression.
     * <P>
     * The return value of the evaluation is converted to a double if necessary.
     * 
     * @return the result of the evaluation. The male socket that holds this
     * expression throws an exception if this value is a Double.NaN or an
     * infinite number.
     * 
     * @throws jmri.JmriException when an exception occurs
     */
    public double evaluateDouble() throws JmriException;
    
    /**
     * Evaluate this expression.
     * <P>
     * The return value of the evaluation is converted to a String if necessary.
     * 
     * @return the result of the evaluation
     * @throws jmri.JmriException when an exception occurs
     */
    public String evaluateString() throws JmriException;
    
    /**
     * Evaluate this expression.
     * <P>
     * This method validates the expression without doing any convertation of
     * the return value.
     * <P>
     * The parameter isCompleted is used if the expression should be evaluated
     * more than once. For example, the Count expression is not completed until
     * its child expression has been true and false a number of times.
     * 
     * @return the result of the evaluation. This is of the same class as
     * parentValue.
     * @throws JmriException when an exception occurs
     */
    @CheckForNull
    public Object evaluateGeneric() throws JmriException;
    
}
