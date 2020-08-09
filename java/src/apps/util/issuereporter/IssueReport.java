package apps.util.issuereporter;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.beans.Bean;

import org.apiguardian.api.API;

/**
 * Common code for the various types of problem reports.
 *
 * @author Randall Wood Copyright 2020
 */
@API(status = API.Status.INTERNAL, consumers = "apps.util.issuereporter.*")
public abstract class IssueReport extends Bean {

    protected String title;
    protected String body;
    protected boolean tooLong = false;

    // package protected
    IssueReport(String title, String body) {
        this.title = title;
        this.body = body;
    }

    /**
     * Submit a report.
     *
     * @param repository the GitHub repository to submit to
     * @return the URI to submit the report to
     * @throws URISyntaxException      if unable to create URI for issue
     * @throws IOException             if unable to connect to GitHub
     * @throws IssueReport414Exception if report is too long
     */
    @Nonnull
    public URI submit(GitHubRepository repository) throws URISyntaxException, IOException, IssueReport414Exception {
        prepare();
        URI uri = new URI(String.format("https://github.com/%s/%s/issues/new?title=%s;body=%s",
                repository.getOwner(),
                repository.getName(),
                URLEncoder.encode(title, StandardCharsets.UTF_8.toString()),
                URLEncoder.encode(body, StandardCharsets.UTF_8.toString())));
        HttpURLConnection request = (HttpURLConnection) uri.toURL().openConnection();
        request.setRequestMethod("GET");
        request.connect();
        if (request.getResponseCode() != 200) {
            tooLong = true;
            throw new IssueReport414Exception();
        }
        return uri;
    }

    /**
     * Prepare a report.
     */
    protected abstract void prepare();

    @Nonnull
    public List<File> getAttachments() {
        return new ArrayList<>();
    }

    /**
     * Get the simple context (JMRI version, Java version, and OS)
     *
     * @return the context
     */
    @Nonnull
    public String getSimpleContext() {
        return Bundle.getMessage(Locale.US, "issue.context",
                Application.getApplicationName(),
                Version.name(),
                System.getProperty("java.vm.name"),
                System.getProperty("java.vm.vendor"),
                System.getProperty("java.vm.version"),
                System.getProperty("java.vm.info"),
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"));
    }

    @Nonnull
    public String getIssueFooter() {
        return Bundle.getMessage(Locale.US, "issue.footer");
    }

    @Nonnull
    public String getBody() {
        return body;
    }
}
