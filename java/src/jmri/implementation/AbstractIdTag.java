package jmri.implementation;

import java.util.Date;
import java.util.Set;
import javax.annotation.Nonnull;
import jmri.IdTag;
import jmri.Reportable;
import jmri.Reporter;

/**
 * Abstract implementation of {@link jmri.IdTag} containing code common to all
 * concrete implementations. This implementation implements {@link jmri.Reportable}.
 *
 * @author  Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public abstract class AbstractIdTag extends AbstractNamedBean implements IdTag, Reportable  {

    protected Reporter whereLastSeen = null;

    protected Date whenLastSeen = null;

    public AbstractIdTag(String systemName) {
        super(systemName);
    }

    public AbstractIdTag(String systemName, String userName) {
        super(systemName, userName);
    }

    @Override
    @Nonnull
    public String getTagID() {
        // TODO: Convert this to allow for >1 char system name length
        // Or, is this really necessary as it will always be 'I'nternal???
        return this.mSystemName.substring(2);
    }

    @Override
    public Reporter getWhereLastSeen() {
        return this.whereLastSeen;
    }

    @Override
    public Date getWhenLastSeen() {
        if (this.whenLastSeen == null) {
            return null;
        } else {
            return (Date) this.whenLastSeen.clone();  // Date is mutable, so return copy
        }
    }

    /**
     * The IDTag version of toReportString returns a string consisting
     * of the user name (if defined) or Tag ID followed by the associated
     * list of property values.
     */
    @Override
    public String toReportString() {
        String userName = getUserName();
        StringBuilder sb = new StringBuilder();
        if(userName == null || userName.isEmpty()){
           sb.append(getTagID());
        } else {
          sb.append(userName);
        }

        // check to see if any properties have been added
        Set keySet = getPropertyKeys();
        // we have properties, so append the values to the
        // end of the report, seperated by spaces.
        for( Object s : keySet) {
            sb.append(" ");
            sb.append(getProperty((String)s));
        }
        return sb.toString();
    }

    @Override
    @Nonnull
    public String getBeanType() {
        return Bundle.getMessage("BeanNameReporter");
    }

//    private static final Logger log = LoggerFactory.getLogger(AbstractIdTag.class);
}
