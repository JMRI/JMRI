// MailMessage.java

package jmri.util.javamail;

/** 
 * Send an email message.
 * <p>
 * Based on JavaMail 1.4.1's msgsend example,
 * converted from command line form to 
 * callable form.
 *
 * Probably needs to set the "Return-Path" header to SF 
 * itself, so that SF will accept directly-sent email
 *
 * Has a temp value for server name
 * 
 * Check for //! comments
 *
 * @author Bob Jacobsen    Copyright 2008, 2009
 * @version $Revision$
 * 
 */

/*
 * @(#)msgsend.java 1.20 07/07/06
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.apache.log4j.Logger;
import java.util.Properties;
import java.util.Date;

import javax.mail.*;
import javax.mail.internet.*;

/*
 * Demo app that shows how to construct and send an RFC822
 * (singlepart) message.
 *
 * XXX - allow more than one recipient on the command line
 *
 * @author Max Spivak
 * @author Bill Shannon
 */

public class MailMessage {

    // first two required 
    String to;
    String mailhost;
    String subject;
    
    String from = "jmri.problem.report@gmail.com";  //! null
    String cc = null;
    String bcc = null;
    String url = null;  // "store-url"
    String mailer = "JMRI";
    String file = "default.lcf"; //! null;  // "attach-file"
    String protocol = null;
    String host = null;
    
    String user = "jmri.problem.report@gmail.com";
    String password = "Hgytht756%gfA@f9";
    
    String record = null;   // name of folder in which to record mail
    
    public MailMessage(String to, String mailhost, String subject) {
        this.to = to;
        this.mailhost = mailhost;
        this.subject = subject;
    }
    
    public void setFrom(String from) {
        this.from = from;
    }

    public void setUser(String userName) {
        this.user = userName;
    }

    public void setPassword(String passWord) {
        this.password = passWord;
    }
    
    Session session;
    Message msg;
    MimeMultipart mp;
    
    public void prepare() {        
        try {
            Properties props = System.getProperties();
            // XXX - could use Session.getTransport() and Transport.connect()
            // XXX - assume we're using SMTP
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.starttls.enable","true");
            if (mailhost != null) {
                props.put("mail.smtp.host", mailhost);
            }
            props.put("mail.smtp.auth", "true");
            
            Authenticator auth = new SMTPAuthenticator();
            
            // Get a Session object
            session = Session.getInstance(props, auth);
            if (log.isDebugEnabled())
                session.setDebug(true);
            
            // construct the message
            msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            
            msg.setRecipients(Message.RecipientType.TO,
                              InternetAddress.parse(to, false));
            if (cc != null)
                msg.setRecipients(Message.RecipientType.CC,
                                  InternetAddress.parse(cc, false));
            if (bcc != null)
                msg.setRecipients(Message.RecipientType.BCC,
                                  InternetAddress.parse(bcc, false));
            
            msg.setSubject(subject);
            
            // We need a multipart message to hold attachment.
            mp = new MimeMultipart();

        } catch (Exception e) {
            log.warn("Exception in prepare", e);
        }
    }
    
    public void setText(String text) {        
        try {
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText(text);
            mp.addBodyPart(mbp1);
        } catch (Exception e) {
            log.warn("Exception in setText", e);
        }
    }
             
    public void setFileAttachment(String file) {        
        try {
            // Attach the specified file.
            MimeBodyPart mbp2 = new MimeBodyPart();
            mbp2.attachFile(file);
            mp.addBodyPart(mbp2);
                
        } catch (Exception e) {
            log.error("Exception in setAttachment", e);
        }
    }
            
    public void send() throws Exception {        
        msg.setContent(mp);

        msg.setHeader("X-Mailer", mailer);
        msg.setSentDate(new Date());
        
        // send the thing off
        Transport.send(msg);
        
        log.debug("Mail was sent successfully.");        
    }

    public void saveCopy() {        
        try {
            // Keep a copy, if requested.
            
            if (record != null) {
                // Get a Store object
                Store store = null;
                if (url != null) {
                    URLName urln = new URLName(url);
                    store = session.getStore(urln);
                    store.connect();
                } else {
                    if (protocol != null)       
                        store = session.getStore(protocol);
                    else
                        store = session.getStore();
                    
                    // Connect
                    if (host != null || user != null || password != null)
                        store.connect(host, user, password);
                    else
                        store.connect();
                }
                
                // Get record Folder.  Create if it does not exist.
                Folder folder = store.getFolder(record);
                if (folder == null) {
                    log.error("Can't get record folder!");
                } else {
                	if (!folder.exists())
                		folder.create(Folder.HOLDS_MESSAGES);

                	Message[] msgs = new Message[1];
                	msgs[0] = msg;
                	folder.appendMessages(msgs);

                	log.info("Mail was recorded successfully.");
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	/**
	 * SimpleAuthenticator is used to do simple authentication when the SMTP
	 * server requires it.
	 */
	private class SMTPAuthenticator extends javax.mail.Authenticator {
	
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(user, password);
		}
	}
    
    static Logger log = Logger.getLogger(MailMessage.class.getName());
}
