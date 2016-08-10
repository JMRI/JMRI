package apps;

/**
 * Startup object models all need to implement this interface. This allows the
 * {@link apps.StartupActionsManager} to handle lists of different model
 * classes.
 *
 * @author Randall Wood (C) 2015
 * @deprecated since 4.5.1; use {@link apps.startup.StartupModel} instead.
 */
@Deprecated
public interface StartupModel extends apps.startup.StartupModel {
}
