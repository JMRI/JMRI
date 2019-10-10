package jmri.implementation;

import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.managers.ProxyIdTagManager;

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
    protected String prefix = null;

    public AbstractIdTag(String systemName) {
        super(systemName);
    }

    public AbstractIdTag(String systemName, String userName) {
        super(systemName, userName);
    }

    @Override
    @Nonnull
    public String getTagID() {
        if(prefix == null) {
            try {
                prefix = findPrefix();
            } catch ( NullPointerException | BadSystemNameException e) {
                // if there isn't a ProxyIDTag Manager, assume the first D in the
                //  system name is the type letter.
                return mSystemName.substring(mSystemName.indexOf('D') + 1);

            }
        }
        return mSystemName.substring(prefix.length()+1);
    }

    private String findPrefix() {
        List<Manager<IdTag>> managerList = InstanceManager.getDefault(ProxyIdTagManager.class).getManagerList();
        for (Manager<IdTag> m : managerList) {
            if (m.getBeanBySystemName(mSystemName) != null) {
                return m.getSystemPrefix();
            }
        }
        throw new BadSystemNameException();
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

}
