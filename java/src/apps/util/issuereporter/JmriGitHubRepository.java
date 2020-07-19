package apps.util.issuereporter;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = GitHubRepository.class)
public class JmriGitHubRepository implements GitHubRepository {

    @Override
    public String getName() {
        return "JMRI";
    }

    @Override
    public String getOwner() {
        return "JMRI";
    }
}
