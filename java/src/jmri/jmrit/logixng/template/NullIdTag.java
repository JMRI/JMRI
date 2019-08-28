package jmri.jmrit.logixng.template;

import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Reporter;
import jmri.implementation.AbstractIdTag;
import org.jdom2.Element;

/**
 * A null turnout.
 */
public class NullIdTag extends AbstractIdTag {

    /**
     * Create a new NullIdTag instance using only a system name.
     *
     * @param sys the system name for this bean; must not be null and must
     *            be unique within the layout
     */
    public NullIdTag(@Nonnull String sys) {
        super(sys);
    }

    @Override
    public void setState(int s) throws JmriException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getState() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setWhereLastSeen(Reporter reporter) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Element store(boolean storeState) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void load(Element e) {
        throw new UnsupportedOperationException("Not supported.");
    }

}
