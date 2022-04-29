package jmri.jmrit.logixng.util;

import java.util.Locale;
import java.util.Map;

import jmri.JmriException;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.implementation.AbstractBase;

/**
 * Implements a null Base
 * @author Daniel Bergqvist (C) 2022
 */
public class NullBase extends AbstractBase {

    public NullBase() throws BadSystemNameException {
        super("");
    }

    @Override
    public void setState(int s) throws JmriException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int getState() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String getBeanType() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String getShortDescription(Locale locale) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String getLongDescription(Locale locale) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Base getParent() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setParent(Base parent) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setup() {
        throw new UnsupportedOperationException("Not supported");
    }

}
