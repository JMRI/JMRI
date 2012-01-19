package jmri.configurexml;

/**
 * Memo class to remember errors encountered during 
 * loading
 *
 * @author Bob Jacobsen  Copyright (c) 2010
 * @version $Revision$
 */
    
public class ErrorMemo {
    public ErrorMemo(
            org.apache.log4j.Level level,
            XmlAdapter adapter,
            String operation,
            String description, 
            String systemName, 
            String userName, 
            Throwable exception)
    {
        this.level = level;
        this.adapter = adapter;
        this.operation = operation;
        this.description = description; 
        this.systemName = systemName;
        this.userName = userName;
        this.exception = exception;
    }
    
    public ErrorMemo(
            org.apache.log4j.Level level,
            XmlAdapter adapter,
            String operation,
            String description, 
            String systemName, 
            String userName, 
            Throwable exception,
            String title)
    {
        this(level, adapter, operation, description, systemName, userName, exception);
        this.title=title;
    }
    
    public org.apache.log4j.Level level;
    public XmlAdapter adapter;
    public String operation;
    public String description; 
    public String systemName;
    public String userName;
    public Throwable exception;
    public String title = "loading";
}
    

