package jmri.jmrit.logixng.implementation;

import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    protected boolean _listenersAreRegistered = false;

    public AbstractBase(String sys) throws BadSystemNameException {
        super(sys);
    }

    public AbstractBase(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    /** {@inheritDoc} */
    @Override
    public Base deepCopyChildren(Base original, Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        for (int i=0; i < original.getChildCount(); i++) {
            // Copy the name of the socket
            getChild(i).setName(original.getChild(i).getName());

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
    public final void setParentForAllChildren() {
        for (int i=0; i < getChildCount(); i++) {
            FemaleSocket femaleSocket = getChild(i);
            femaleSocket.setParent(this);
            if (femaleSocket.isConnected()) {
                MaleSocket connectedSocket = femaleSocket.getConnectedSocket();
                connectedSocket.setParent(femaleSocket);
                connectedSocket.setParentForAllChildren();
            }
        }
    }

    /**
     * Register listeners if this object needs that.
     * <P>
     * Important: This method may be called more than once. Methods overriding
     * this method must ensure that listeners are not registered more than once.
     */
    abstract protected void registerListenersForThisClass();

    /**
     * Unregister listeners if this object needs that.
     * <P>
     * Important: This method may be called more than once. Methods overriding
     * this method must ensure that listeners are not unregistered more than once.
     */
    abstract protected void unregisterListenersForThisClass();

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
    abstract protected void disposeMe();

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

    protected void assertListenersAreNotRegistered(Logger log, String method) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException(method + " must not be called when listeners are registered");
            log.error(method + " must not be called when listeners are registered", e);
            throw e;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractBase.class);
}
