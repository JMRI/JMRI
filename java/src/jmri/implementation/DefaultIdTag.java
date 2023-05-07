package jmri.implementation;

import com.fasterxml.jackson.databind.util.StdDateFormat;

import java.text.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import javax.annotation.CheckForNull;

import jmri.*;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of the {@link jmri.IdTag} interface for the Internal
 * system.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class DefaultIdTag extends AbstractIdTag {

    private int currentState = UNKNOWN;

    public DefaultIdTag(String systemName) {
        super(systemName);
        setWhereLastSeen(null);
    }

    public DefaultIdTag(String systemName, String userName) {
        super(systemName, userName);
        setWhereLastSeen(null);
    }

    public final static String PROPERTY_WHEN_LAST_SEEN = "whenLastSeen";
    public final static String PROPERTY_WHERE_LAST_SEEN = "whereLastSeen";

    @Override
    public int compareTo(NamedBean n2) {
        Objects.requireNonNull(n2);
        String o1 = this.getSystemName();
        String o2 = n2.getSystemName();
        int p1len = Manager.getSystemPrefixLength(o1);
        int p2len = Manager.getSystemPrefixLength(o2);
        int comp = o1.substring(0, p1len).compareTo(o2.substring(0, p2len));
        if (comp != 0) 
            return comp;
        comp = o1.compareTo(o2);
        return comp;
    }

    @Override
    public final void setWhereLastSeen(@CheckForNull Reporter r) {
        Reporter oldWhere = this.whereLastSeen;
        Date oldWhen = this.whenLastSeen;
        this.whereLastSeen = r;
        if (r != null) {
            this.whenLastSeen = getDateNow();
        } else {
            this.whenLastSeen = null;
        }
        setCurrentState(r != null ? SEEN : UNSEEN);
        firePropertyChange(PROPERTY_WHERE_LAST_SEEN, oldWhere, this.whereLastSeen);
        firePropertyChange(PROPERTY_WHEN_LAST_SEEN, oldWhen, this.whenLastSeen);
    }

    private Date getDateNow() {
        return InstanceManager.getDefault(IdTagManager.class).isFastClockUsed()
            ? InstanceManager.getDefault(ClockControl.class).getTime()
            : Calendar.getInstance().getTime();
    }

    private void setCurrentState(int state) {
        try {
            setState(state);
        } catch (JmriException ex) {
            log.warn("Problem setting state of IdTag {} {}", getSystemName(),ex.getMessage());
        }
    }

    @Override
    public void setState(int s) throws JmriException {
        this.currentState = s;
    }

    @Override
    public int getState() {
        return this.currentState;
    }

    @Override
    public Element store(boolean storeState) {
        Element e = new Element("idtag"); // NOI18N
        e.addContent(new Element("systemName").addContent(this.mSystemName)); // NOI18N
        String uName = this.getUserName();
        if (uName != null && !uName.isEmpty()) {
            e.addContent(new Element("userName").addContent(uName)); // NOI18N
        }
        String comment = this.getComment();
        if ((comment != null) && (!comment.isEmpty())) {
            e.addContent(new Element("comment").addContent(comment)); // NOI18N
        }
        Reporter whereLast = this.getWhereLastSeen();
        if (whereLast != null && storeState) {
            e.addContent(new Element(PROPERTY_WHERE_LAST_SEEN).addContent(whereLast.getSystemName()));
        }
        if (this.getWhenLastSeen() != null && storeState) {
            e.addContent(new Element(PROPERTY_WHEN_LAST_SEEN).addContent(new StdDateFormat().format(this.getWhenLastSeen())));
        }
        return e;
    }

    /**
     * Load an idtag xml element.
     * whenLastSeen formats accepted JMRI 5.3.6 include
     * yyyy-MM-dd'T'HH:mm:ss.SSSX
     * yyyy-MM-dd'T'HH:mm:ss.SSS
     * EEE, dd MMM yyyy HH:mm:ss zzz
     * 
     * @param e element to load.
     */
    @Override
    public void load(Element e) {
        if (e.getName().equals("idtag")) { // NOI18N
            log.debug("Load IdTag element for {}", this.getSystemName());
            if (e.getChild("userName") != null) { // NOI18N
                this.setUserName(e.getChild("userName").getText()); // NOI18N
            }
            if (e.getChild("comment") != null) { // NOI18N
                this.setComment(e.getChild("comment").getText()); // NOI18N
            }
            if (e.getChild(PROPERTY_WHERE_LAST_SEEN) != null) {
                try {
                    Reporter r = InstanceManager.getDefault(ReporterManager.class)
                                    .provideReporter(e.getChild(PROPERTY_WHERE_LAST_SEEN).getText());
                    this.setWhereLastSeen(r);
                    this.whenLastSeen = null;
                } catch (IllegalArgumentException ex) {
                    log.warn("Failed to provide Reporter \"{}\" in load of \"{}\"", e.getChild(PROPERTY_WHERE_LAST_SEEN).getText(), getDisplayName());
                }
            }
            if (e.getChild(PROPERTY_WHEN_LAST_SEEN) != null) {
                String lastSeenText = e.getChildText(PROPERTY_WHEN_LAST_SEEN);
                log.debug("Loading {} When Last Seen: {}", getDisplayName(), lastSeenText);
                try { // parse using ISO 8601 date format
                    this.whenLastSeen = new StdDateFormat().parse(lastSeenText);
                } catch (ParseException ex) {
                    log.debug("ParseException in whenLastSeen ISO attempt: \"{}\"", lastSeenText, ex);
                    // next, try parse using how it was saved by JMRI < 5.3.5
                    try {
                        this.whenLastSeen = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).parse(lastSeenText);
                    } catch (ParseException ex2) {
                        log.warn("During load of IdTag \"{}\" {}", getDisplayName(), ex.getMessage());
                    }
                }
            }
        } else {
            log.error("Not an IdTag element: \"{}\" for Tag \"{}\"", e.getName(), this.getDisplayName());
        }
    }

    private static final Logger log = LoggerFactory.getLogger(DefaultIdTag.class);

}
