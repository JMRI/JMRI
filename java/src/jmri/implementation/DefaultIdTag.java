package jmri.implementation;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Reporter;
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

    @Override
    public final void setWhereLastSeen(Reporter r) {
        Reporter oldWhere = this.whereLastSeen;
        Date oldWhen = this.whenLastSeen;
        this.whereLastSeen = r;
        if (r != null) {
            this.whenLastSeen = InstanceManager.getDefault(IdTagManager.class).isFastClockUsed()
                    ? InstanceManager.getDefault(jmri.ClockControl.class).getTime()
                    : Calendar.getInstance().getTime();
        } else {
            this.whenLastSeen = null;
        }
        setCurrentState(r != null ? SEEN : UNSEEN);
        firePropertyChange("whereLastSeen", oldWhere, this.whereLastSeen); //NOI18N
        firePropertyChange("whenLastSeen", oldWhen, this.whenLastSeen);    //NOI18N
    }

    private void setCurrentState(int state) {
        try {
            setState(state);
        } catch (JmriException ex) {
            log.warn("Problem setting state of IdTag " + getSystemName());
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
        Element e = new Element("idtag"); //NOI18N
        // e.setAttribute("systemName", this.mSystemName); // not needed from 2.11.1
        e.addContent(new Element("systemName").addContent(this.mSystemName)); // NOI18N
        String uName = this.getUserName();
        if (uName != null && !uName.isEmpty()) {
            // e.setAttribute("userName", this.getUserName()); // not needed from 2.11.1
            e.addContent(new Element("userName").addContent(uName)); // NOI18N
        }
        String comment = this.getComment();
        if ((comment != null) && (!comment.isEmpty())) {
            e.addContent(new Element("comment").addContent(comment)); // NOI18N
        }
        Reporter whereLast = this.getWhereLastSeen();
        if (whereLast != null && storeState) {
            e.addContent(new Element("whereLastSeen").addContent(whereLast.getSystemName())); // NOI18N
        }
        if (this.getWhenLastSeen() != null && storeState) {
            e.addContent(new Element("whenLastSeen").addContent(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(this.getWhenLastSeen()))); //NOI18N
        }
        return e;
    }

    @Override
    public void load(Element e) {
        if (e.getName().equals("idtag")) { //NOI18N
            if (log.isDebugEnabled()) {
                log.debug("Load IdTag element for " + this.getSystemName());
            }
            if (e.getChild("userName") != null) //NOI18N
            {
                this.setUserName(e.getChild("userName").getText()); //NOI18N
            }
            if (e.getChild("comment") != null) //NOI18N
            {
                this.setComment(e.getChild("comment").getText()); //NOI18N
            }
            if (e.getChild("whereLastSeen") != null) { //NOI18N
                try {
                    Reporter r = InstanceManager.getDefault(jmri.ReporterManager.class)
                                    .provideReporter(e.getChild("whereLastSeen").getText()); //NOI18N
                    this.setWhereLastSeen(r);
                    this.whenLastSeen = null;
                } catch (IllegalArgumentException ex) {
                    log.warn("Failed to provide Turnout \"{}\" in load", e.getChild("whereLastSeen").getText());
                }
            }
            if (e.getChild("whenLastSeen") != null) { //NOI18N
                log.debug("When Last Seen: " + e.getChild("whenLastSeen").getText());
                try {
                    this.whenLastSeen = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).parse(e.getChild("whenLastSeen").getText()); //NOI18N
                } catch (ParseException ex) {
                    log.warn("Error parsing when last seen: " + ex);
                }
            }
        } else {
            log.error("Not an IdTag element: " + e.getName());
        }
    }

    private static final Logger log = LoggerFactory.getLogger(DefaultIdTag.class);

}
