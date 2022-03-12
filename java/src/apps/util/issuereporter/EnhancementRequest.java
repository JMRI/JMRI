package apps.util.issuereporter;

import java.util.Locale;

/**
 *
 * @author rhwood
 */
public class EnhancementRequest extends IssueReport {

    public EnhancementRequest(String title, String body) {
        super(title, body);
    }
    
    @Override
    public void prepare() {
        title = Bundle.getMessage(Locale.US, "rfe.title", title);
        if (!tooLong) {
            body = Bundle.getMessage(Locale.US, "rfe.body", "", body, getSimpleContext(), this.getIssueFooter());
        } else {
            body = Bundle.getMessage("instructions.paste.414");
        }
    }
}
