package jmri.jmrit.logixng.implementation;

import java.io.PrintWriter;
import java.util.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.implementation.AbstractNamedBean;
import jmri.jmrit.logixng.*;

import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

/**
 * The abstract class that is the base class for all LogixNG classes that
 * implements the Base interface.
 */
public abstract class AbstractBase
        extends AbstractNamedBean
        implements Base {

    private final Category _category;
    protected boolean _listenersAreRegistered = false;

    public AbstractBase(String sys) throws BadSystemNameException {
        super(sys);
        _category = Category.ITEM;
    }

    public AbstractBase(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _category = Category.ITEM;
    }

    public AbstractBase(String sys, Category category) throws BadSystemNameException {
        super(sys);
        _category = category;
    }

    public AbstractBase(String sys, String user, Category category)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _category = category;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return _category;
    }

    /** {@inheritDoc} */
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        // Default implementation is to throw UnsupportedOperationException.
        // Classes that have children must override this method.
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public int getChildCount() {
        // Default implementation is to return 0 children.
        // Classes that have children must override this method.
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public Base deepCopyChildren(Base original, Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        for (int i=0; i < original.getChildCount(); i++) {
            // Copy the name of the socket.
            // Ignore duplicate errors since these errors might happen temporary in this loop.
            getChild(i).setName(original.getChild(i).getName(), true);

            // Copy the child
            if (original.getChild(i).isConnected()) {
                Base childTree = original.getChild(i).getConnectedSocket().getDeepCopy(systemNames, userNames);
                getChild(i).connect((MaleSocket) childTree);
            }
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ConditionalNG getConditionalNG() {
        if (this instanceof ConditionalNG) return (ConditionalNG)this;
        if (getParent() == null) return null;
        return getParent().getConditionalNG();
    }

    /** {@inheritDoc} */
    @Override
    public final LogixNG getLogixNG() {
        if (this instanceof LogixNG) return (LogixNG)this;
        if (getParent() == null) return null;
        return getParent().getLogixNG();
    }

    /** {@inheritDoc} */
    @Override
    public final Base getRoot() {
        Base item = this;
        while (item.getParent() != null) {
            item = item.getParent();
        }
        return item;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean setParentForAllChildren(List<String> errors) {
        boolean result = true;
        for (int i=0; i < getChildCount(); i++) {
            FemaleSocket femaleSocket = getChild(i);
            femaleSocket.setParent(this);
            if (femaleSocket.isConnected()) {
                MaleSocket connectedSocket = femaleSocket.getConnectedSocket();
                if ((connectedSocket.getParent() != null)
                        && (connectedSocket.getParent() != femaleSocket)) {
                    errors.add(Bundle.getMessage("DuplicateParentMessage",
                            connectedSocket.getSystemName(),
                            connectedSocket.getParent().getSystemName(),
                            getSystemName()));
                    log.error("The child {} already has the parent {} so it cannot be added to {}",
                            connectedSocket.getSystemName(),
                            connectedSocket.getParent().getSystemName(),
                            getSystemName());
                    femaleSocket.disconnect();
                    result = false;
                } else {
                    connectedSocket.setParent(femaleSocket);
                    result = result && connectedSocket.setParentForAllChildren(errors);
                }
            }
        }
        return result;
    }

    /**
     * Register listeners if this object needs that.
     * <P>
     * Important: This method may be called more than once. Methods overriding
     * this method must ensure that listeners are not registered more than once.
     */
    protected void registerListenersForThisClass() {
        // Do nothing
    }

    /**
     * Unregister listeners if this object needs that.
     * <P>
     * Important: This method may be called more than once. Methods overriding
     * this method must ensure that listeners are not unregistered more than once.
     */
    protected void unregisterListenersForThisClass() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public final void registerListeners() {
        if (isActive()) {
            registerListenersForThisClass();
            for (int i=0; i < getChildCount(); i++) {
                getChild(i).registerListeners();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void unregisterListeners() {
        unregisterListenersForThisClass();
        for (int i=0; i < getChildCount(); i++) {
            getChild(i).unregisterListeners();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isActive() {
        return isEnabled() && ((getParent() == null) || getParent().isActive());
    }

    protected void printTreeRow(
            PrintTreeSettings settings,
            Locale locale,
            PrintWriter writer,
            String currentIndent,
            MutableInt lineNumber) {

        if (settings._printLineNumbers) {
            writer.append(String.format(PRINT_LINE_NUMBERS_FORMAT, lineNumber.addAndGet(1)));
        }
        writer.append(currentIndent);
        writer.append(getLongDescription(locale));
        writer.println();
    }

    /** {@inheritDoc} */
    @Override
    public void printTree(
            PrintTreeSettings settings,
            PrintWriter writer,
            String indent,
            MutableInt lineNumber) {

        printTree(settings, Locale.getDefault(), writer, indent, "", lineNumber);
    }

    /** {@inheritDoc} */
    @Override
    public void printTree(
            PrintTreeSettings settings,
            Locale locale,
            PrintWriter writer,
            String indent,
            MutableInt lineNumber) {

        printTree(settings, locale, writer, indent, "", lineNumber);
    }

    /** {@inheritDoc} */
    @Override
    public void printTree(
            PrintTreeSettings settings,
            Locale locale,
            PrintWriter writer,
            String indent,
            String currentIndent,
            MutableInt lineNumber) {

        printTreeRow(settings, locale, writer, currentIndent, lineNumber);

        for (int i=0; i < getChildCount(); i++) {
            getChild(i).printTree(settings, locale, writer, indent, currentIndent+indent, lineNumber);
        }
    }

    /** {@inheritDoc} */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value="SLF4J_SIGN_ONLY_FORMAT",
                                                        justification="Specific log message format")
    public void getUsageTree(int level, NamedBean bean, List<jmri.NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("## {} :: {}", level, this.getLongDescription());
        level++;
        for (int i=0; i < getChildCount(); i++) {
            getChild(i).getUsageTree(level, bean, report, cdl);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<jmri.NamedBeanUsageReport> report, NamedBean cdl) {
    }

    /**
     * {@inheritDoc}
     *
     * Do a string comparison.
     */
    @CheckReturnValue
    @Override
    public int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2, @Nonnull NamedBean n) {
        return suffix1.compareTo(suffix2);
    }

    /**
     * Dispose this class.
     * Listeners do not need to be unregistered by this method since they are
     * unregistered by dispose().
     */
    protected void disposeMe() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public final void dispose() {
        super.dispose();
        for (int i=0; i < getChildCount(); i++) {
            getChild(i).dispose();
        }
        unregisterListeners();
        disposeMe();
    }

    public void assertListenersAreNotRegistered(Logger log, String method) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException(method + " must not be called when listeners are registered");
            log.error("{} must not be called when listeners are registered", method, e);
            throw e;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void getListenerRefsIncludingChildren(List<String> list) {
        list.addAll(getListenerRefs());
        for (int i=0; i < getChildCount(); i++) {
            getChild(i).getListenerRefsIncludingChildren(list);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractBase.class);
}
