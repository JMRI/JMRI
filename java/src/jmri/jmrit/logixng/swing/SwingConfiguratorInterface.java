package jmri.jmrit.logixng.swing;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JPanel;
import jmri.NamedBean.BadUserNameException;
import jmri.NamedBean.BadSystemNameException;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.MaleSocket;

/**
 * The parent interface for configuring classes with Swing.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface SwingConfiguratorInterface {

    /**
     * Get a configuration panel when a new object is to be created and we don't
     * have it yet.
     * This method initializes the panel with an empty configuration.
     * 
     * @param buttonPanel panel with the buttons
     * @return a panel that configures this object
     * @throws IllegalArgumentException if this class does not support the class
     * with the name given in parameter 'className'
     */
    public JPanel getConfigPanel(JPanel buttonPanel) throws IllegalArgumentException;
    
    /**
     * Get a configuration panel for an object.
     * This method initializes the panel with the configuration of the object.
     * 
     * @param object the object for which to return a configuration panel
     * @param buttonPanel panel with the buttons
     * @return a panel that configures this object
     */
    public JPanel getConfigPanel(@Nonnull Base object, JPanel buttonPanel) throws IllegalArgumentException;
    
    /**
     * Validate the form.
     * The parameter errorMessage is used to give the error message in case of
     * an error. The caller must ensure that errorMessage.length() is zero.
     * 
     * @param errorMessages the error messages in case of an error
     * @return true if data in the form is valid, false otherwise
     */
    public boolean validate(@Nonnull List<String> errorMessages);
//    public boolean validate(@Nonnull StringBuilder errorMessage);
    
    /**
     * Get an example of a system name
     * @return the system name
     */
    public String getExampleSystemName();
    
    /**
     * Create a new system name.
     * @return a new system name
     */
    public String getAutoSystemName();
    
    /**
     * Create a new object with the data entered.
     * This method must also register the object in its manager.
     * 
     * @param systemName system name
     * @param userName user name
     * @return a male socket for the new object
     */
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName)
            throws BadUserNameException, BadSystemNameException;
    
    /**
     * Updates the object with the data in the form.
     * 
     * @param object the object to update
     */
    public void updateObject(@Nonnull Base object);
    
    /**
     * Returns the name of the class that this class configures.
     *
     * @return the name of the class this class configures.
     */
    @Override
    public String toString();
    
    /**
     * Dispose the panel and remove all the listeners that this class may have
     * registered.
     */
    public void dispose();
    
}
