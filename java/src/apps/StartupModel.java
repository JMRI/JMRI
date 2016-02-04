package apps;

/**
 * Startup object models all need to implement this interface. This allows the
 * {@link apps.StartupActionsManager} to handle lists of different model
 * classes.
 *
 * @author Randall Wood (C) 2015
 */
public interface StartupModel {

    public String getName();

    public void setName(String name);
}
