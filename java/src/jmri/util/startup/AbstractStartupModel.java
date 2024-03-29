package jmri.util.startup;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Abstract startup action model.
 *
 * @author Randall Wood (c) 2016
 */
public abstract class AbstractStartupModel implements StartupModel {

    private String name;
    private boolean enabled = true;
    private final List<Exception> exceptions = new ArrayList<>();

    protected AbstractStartupModel() {
        this.name = null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    @Nonnull
    public String toString() {
        String string = this.getName();
        if (string == null) {
            return super.toString();
        }
        return string;
    }

    /**
     * {@inheritDoc}
     *
     * The default behavior is to return true if {@link #getName()} returns a
     * non-null, non-empty String.
     *
     * @return true if valid; false otherwise
     */
    @Override
    public boolean isValid() {
        String s = this.getName();
        return s != null && !s.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(boolean value) {
        enabled = value;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public List<Exception> getExceptions() {
        return new ArrayList<>(this.exceptions);
    }

    @Override
    public void addException(@Nonnull Exception exception) {
        this.exceptions.add(exception);
    }
}
