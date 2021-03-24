package jmri.jmrit.logixng;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * Manager for Expression
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public interface StringExpressionManager extends BaseManager<MaleStringExpressionSocket> {

    /**
     * Remember a NamedBean Object created outside the manager.
     * This method creates a MaleStringExpressionSocket for the action.
     *
     * @param expression the bean
     * @return the male socket for this expression
     * @throws IllegalArgumentException if the expression has an invalid system name
     */
    public MaleStringExpressionSocket registerExpression(@Nonnull StringExpressionBean expression)
            throws IllegalArgumentException;
    
    /**
     * Create a new system name for an Expression.
     * @return a new system name
     */
    public String getAutoSystemName();

    /**
     * Create a female socket for analog expressions
     * @param parent the parent that will own the new female socket
     * @param listener the listener for the female socket
     * @param socketName the name of the new socket
     * @return the new female socket
     */
    public FemaleStringExpressionSocket createFemaleSocket(
            Base parent, FemaleSocketListener listener, String socketName);
    
    /**
     * Get a set of classes that implements the DigitalAction interface.
     * 
     * @return a set of entries with category and class
     */
    public Map<Category, List<Class<? extends Base>>> getExpressionClasses();

    /*.*
     * Add an Expression.
     *
     * @param expression the expression to add
     * @throws IllegalArgumentException if the expression has an invalid system name
     */
//    public void addExpression(Expression expression)
//            throws IllegalArgumentException;

    /*.*
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
//    public Expression getExpression(String name);

//    public Expression getByUserName(String s);

//    public Expression getBySystemName(String s);

    /**
     * {@inheritDoc}
     * 
     * The sub system prefix for the StringExpressionManager is
     * {@link #getSystemNamePrefix() } and "SE";
     */
    @Override
    public default String getSubSystemNamePrefix() {
        return getSystemNamePrefix() + "SE";
    }

    /**
     * Delete StringExpression by removing it from the manager. The Expression must
     * first be deactivated so it stops processing.
     *
     * @param x the StringExpression to delete
     */
    public void deleteStringExpression(MaleStringExpressionSocket x);

}
