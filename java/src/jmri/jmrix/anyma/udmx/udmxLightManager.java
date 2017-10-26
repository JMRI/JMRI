package jmri.jmrix.anyma.udmx;

import static jmri.Manager.NameValidity.VALID;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import jmri.Light;
import jmri.LightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement Light manager - Specific to anyma udmx
 * <P>
 * System names are "XLnnn", where nnn is the light number without padding.
 *
 * @author George Warner Copyright (C) 2017
 */
public class udmxLightManager implements LightManager {

    /**
     * {@inheritDoc}
     */
    @CheckReturnValue
    @Nonnull
    public String getSystemPrefix() {
        log.info("*getSystemPrefix()");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @CheckReturnValue
    public char typeLetter() {
        log.info("*typeLetter()");
        return '\n';
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    public String makeSystemName(@Nonnull String s) {
        log.info("*makeSystemName('{}')", s);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @CheckReturnValue
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        log.info("*validSystemNameFormat('{}')", systemName);
        return VALID;
    }

    /**
     * {@inheritDoc}
     */
    @CheckReturnValue
    @Nonnull
    public String[] getSystemNameArray() {
        log.info("*getSystemNameArray()");
        return new String[]{};
    }

    /**
     * {@inheritDoc}
     */
    @CheckReturnValue
    @Nonnull
    public List<Light> getNamedBeanList() {
        log.info("*getNamedBeanList()");
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @CheckReturnValue
    @CheckForNull
    public Light getBeanBySystemName(@Nonnull String systemName) {
        log.info("*getBeanBySystemName('{}')", systemName);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @CheckReturnValue
    @CheckForNull
    public Light getBeanByUserName(@Nonnull String userName) {
        log.info("*getBeanByUserName('{}')", userName);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @CheckReturnValue
    @CheckForNull
    public Light getNamedBean(@Nonnull String name) {
        log.info("*getNamedBean('{}')", name);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void addPropertyChangeListener(@CheckForNull PropertyChangeListener l) {
        log.info("*addPropertyChangeListener('{}')", l);
    }

    /**
     * {@inheritDoc}
     */
    public void removePropertyChangeListener(@CheckForNull PropertyChangeListener l) {
        log.info("*removePropertyChangeListener('{}')", l);
    }

    /**
     * {@inheritDoc}
     */
    public void addVetoableChangeListener(@CheckForNull VetoableChangeListener l) {
        log.info("*addVetoableChangeListener('{}')", l);
    }

    /**
     * {@inheritDoc}
     */
    public void removeVetoableChangeListener(@CheckForNull VetoableChangeListener l) {
        log.info("*removeVetoableChangeListener('{}')", l);
    }

    /**
     * {@inheritDoc}
     */
    public void deleteBean(@Nonnull Light n, @Nonnull String property) throws java.beans.PropertyVetoException {
        log.info("*deleteBean({}, '{}')", n, property);
    }

    /**
     * {@inheritDoc}
     */
    public void register(@Nonnull Light n) {
        log.info("*register({})", n);
    }

    /**
     * {@inheritDoc}
     */
    public void deregister(@Nonnull Light n) {
        log.info("*deregister({})", n);
    }

    /**
     * {@inheritDoc}
     */
    @CheckReturnValue
    public int getXMLOrder() {
        log.info("*getXMLOrder()");
        return LIGHTS;
    }

    /**
     * {@inheritDoc}
     */
    @CheckReturnValue
    @Nonnull
    public String getBeanTypeHandled() {
        log.info("*getBeanTypeHandled()");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @CheckReturnValue
    @CheckForNull
    public Light getLight(@Nonnull String name) {
        log.info("*getLight('{}')", name);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    public Light provideLight(@Nonnull String name) {
        log.info("*provideLight('{}')", name);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        log.info("*dispose()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Light newLight(String systemName, String userName) {
        log.info("*newLight('{}', '{}')", systemName, userName);
        List<String> strings = parse(systemName);
        if (!strings.isEmpty()) {
            String lastElement = strings.get(strings.size() - 1);
            // clear the last element
            strings.remove(lastElement);
            String allButLast = String.join("", strings);
            int addr = Integer.parseInt(lastElement);
            Light l = new udmxLight(allButLast, addr);
            l.setUserName(userName);
            return l;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        log.info("*allowMultipleAdditions('{}')", systemName);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @CheckReturnValue
    @CheckForNull
    public Light getByUserName(@Nonnull String s) {
        log.info("*getByUserName('{}')", s);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @CheckReturnValue
    @CheckForNull
    public Light getBySystemName(@Nonnull String s) {
        log.info("*getBySystemName('{}')", s);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @CheckReturnValue
    public boolean validSystemNameConfig(@Nonnull String systemName) {
        log.info("*validSystemNameConfig('{}')", systemName);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @CheckReturnValue
    @Nonnull
    public String normalizeSystemName(@Nonnull String systemName) {
        log.info("*normalizeSystemName('{}')", systemName);
        return systemName;
    }

    /**
     * {@inheritDoc}
     */
    @CheckReturnValue
    @Nonnull
    public String convertSystemNameToAlternate(@Nonnull String systemName) {
        log.info("*convertSystemNameToAlternate('{}')", systemName);
        return systemName;
    }

    /**
     * {@inheritDoc}
     */
    @CheckReturnValue
    @Nonnull
    @Override
    public List<String> getSystemNameList() {
        log.info("*getSystemNameList()");
        return new ArrayList<String>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activateAllLights() {
        log.info("*allowMultipleAdditions()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsVariableLights(@Nonnull String systemName) {
        log.info("*supportsVariableLights('{}')", systemName);
        return true;
    }

    /*=================*\
    |* private methods *|
    \*=================*/
    private static final Pattern VALID_PATTERN = Pattern.compile("[0-9]+|[A-Z]+");

    private List<String> parse(String toParse) {
        List<String> chunks = new LinkedList<String>();
        Matcher matcher = VALID_PATTERN.matcher(toParse);
        while (matcher.find()) {
            chunks.add(matcher.group());
        }
        return chunks;
    }

    private final static Logger log = LoggerFactory.getLogger(udmxLightManager.class);
}
