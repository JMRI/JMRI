package apps.startup;

import javax.annotation.Nonnull;

/**
 * Abstract startup action model.
 *
 * @author Randall Wood (c) 2016
 */
public abstract class AbstractStartupModel implements StartupModel {

    private String name;

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
        return this.name != null && !this.name.isEmpty();
    }
}
