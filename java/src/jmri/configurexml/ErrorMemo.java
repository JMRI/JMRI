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
            XmlAdapter adapter,
            String operation,
            String description, 
            String systemName, 
            String userName, 
            Throwable exception)
    {
        this.adapter = adapter;
        this.operation = operation;
        this.description = description; 
        this.systemName = systemName;
        this.userName = userName;
        this.exception = exception;
    }
    
    public ErrorMemo(
            XmlAdapter adapter,
            String operation,
            String description, 
            String systemName, 
            String userName, 
            Throwable exception,
            String title)
    {
        this(adapter, operation, description, systemName, userName, exception);
        this.title=title;
    }
    
    public XmlAdapter adapter;
    public String operation;
    public String description; 
    public String systemName;
    public String userName;
    public Throwable exception;
    public String title = "loading";
}
    

