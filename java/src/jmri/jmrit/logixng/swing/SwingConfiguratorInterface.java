package jmri.jmrit.logixng.swing;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.NamedBean;
import jmri.NamedBean.BadUserNameException;
import jmri.NamedBean.BadSystemNameException;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.BaseManager;
import jmri.jmrit.logixng.MaleSocket;

/**
 * The parent interface for configuring classes with Swing.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public interface SwingConfiguratorInterface {

    /**
     * Set the dialog of this SWI.
     *
     * @param dialog the dialog
     */
    public void setJDialog(JDialog dialog);

    /**
     * Set the dialog of this SWI.
     *
     * @return the dialog
     */
    public JDialog getJDialog();

    /**
     * Get the menu text for execute/evaluate.
     * @return the menu text
     */
    public String getExecuteEvaluateMenuText();

    /**
     * Execute or evaluate an item that this object configures.
     * @param object the object to execute or evaluate
     */
    public void executeEvaluate(@Nonnull Base object);

    /**
     * Get the manager that handles the beans for the new object.
     * This is used for validation of the system name for the bean that this
     * class creates.
     *
     * @return the manager
     */
    public BaseManager<? extends NamedBean> getManager();

    /**
     * Get a configuration panel when a new object is to be created and we don't
     * have it yet.This method initializes the panel with an empty configuration.
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
     * <P>
     * The parameter errorMessage is used to give the error message in case of
     * an error. If there are errors, the error messages is added to the list
     * errorMessage.
     *
     * @param errorMessages the error messages in case of an error
     * @return true if data in the form is valid, false otherwise
     */
    public boolean validate(@Nonnull List<String> errorMessages);

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
     * Create a new object with the data entered.This method must also register the object in its manager.
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
     * Is the SWI ready to be closed?
     * @return true if the SWI is ready to be closed, false otherwise.
     */
    public default boolean canClose() {
        return true;
    }

    /**
     * Dispose the panel and remove all the listeners that this class may have
     * registered.
     */
    public void dispose();


    /**
     * Parses the message and creates a list of components there the given
     * components are separated by JLabel components from the message.
     * @param message the message to be parsed
     * @param components the components
     * @return the components separated with JLabel components
     */
    public static List<JComponent> parseMessage(String message, JComponent[] components) {
        List<JComponent> componentsToReturn = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean parseNumber = false;

        for (int index=0; index < message.length(); index++) {
            int character = message.codePointAt(index);

            if (parseNumber) {
                if (character == '}') {
                    int number = Integer.parseInt(sb.toString());
                    componentsToReturn.add(components[number]);
                    sb = new StringBuilder();
                    parseNumber = false;
                } else if ((character >= '0') && (character <= '9')) {
                    sb.appendCodePoint(character);
                } else {
                    throw new IllegalArgumentException(
                            "left curly bracket must be followed by a digit but is followed by "
                                    + Arrays.toString(Character.toChars(character)));
                }
            } else {
                if (character == '{') {
                    parseNumber = true;
                    componentsToReturn.add(new JLabel(sb.toString()));
                    sb = new StringBuilder();
                } else {
                    sb.appendCodePoint(character);
                }
            }
        }

        if (sb.length() > 0) componentsToReturn.add(new JLabel(sb.toString()));

        return componentsToReturn;
    }

}
