package apps.util.issuereporter;

import javax.annotation.Nonnull;

import jmri.spi.JmriServiceProviderInterface;

import org.apiguardian.api.API;

/**
 *
 * @author Randall Wood
 */
@API(status = API.Status.EXPERIMENTAL)
public interface GitHubRepository extends Comparable<GitHubRepository>, JmriServiceProviderInterface {

    @Override
    public default int compareTo(GitHubRepository o) {
        return getTitle().compareTo(o.getTitle());
    }

    /**
     * Get the repository name.
     *
     * @return the name
     */
    @Nonnull
    public String getName();

    /**
     * Get the repository owner.
     *
     * @return the owner
     */
    @Nonnull
    public String getOwner();

    /**
     * Get the title to display to a user. Override if this is not the same as
     * {@link #getName()}.
     *
     * @return the title
     */
    @Nonnull
    public default String getTitle() {
        return getName();
    }
}
