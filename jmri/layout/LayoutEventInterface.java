package jmri.layout;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version $Revision: 1.1 $
 */

public interface LayoutEventInterface
{
    public void addEventListener( LayoutEventListener pListener ) ;

    public void removeEventListener( LayoutEventListener pListener ) ;
}