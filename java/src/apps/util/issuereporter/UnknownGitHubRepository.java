package apps.util.issuereporter;

import org.apiguardian.api.API;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood Copyright 2020
 */
@API(status = API.Status.INTERNAL)
@ServiceProvider(service = GitHubRepository.class)
public class UnknownGitHubRepository extends JmriGitHubRepository {

    @Override
    public int compareTo(GitHubRepository o) {
        return o == this ? 0 : 1;
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("unknown.repository.title");
    }
    
}
