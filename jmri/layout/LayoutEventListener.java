package jmri.layout;

import java.util.EventListener;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version $Revision: 1.1 $
 */

public interface LayoutEventListener extends EventListener
{
    public void message( LayoutEventData pLayoutEvent ) ;
}