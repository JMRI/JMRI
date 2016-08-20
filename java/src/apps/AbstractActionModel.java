package apps;

/**
 * Provide services for invoking actions during configuration and startup.
 * <P>
 * The action classes and corresponding human-readable names are kept in the
 * apps.ActionListBundle properties file (which can be translated). They are
 * displayed in lexical order by human-readable name.
 * <P>
 * @author	Bob Jacobsen Copyright 2003, 2007, 2014
 * @see apps.startup.AbstractActionModelFactory
 * @deprecated since 4.5.1 use {@link apps.startup.AbstractActionModel} instead.
 */
@Deprecated
public abstract class AbstractActionModel extends apps.startup.AbstractActionModel {
}
