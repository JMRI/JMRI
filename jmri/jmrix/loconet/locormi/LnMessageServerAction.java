package jmri.jmrix.loconet.locormi;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version $Id: LnMessageServerAction.java,v 1.2 2002-03-28 02:25:24 jacobsen Exp $
 */

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;

public class LnMessageServerAction extends AbstractAction
{
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnMessageServerAction.class.getName());

  public LnMessageServerAction( String s )
  {
    super( s ) ;
  }

  public void actionPerformed( ActionEvent e)
  {
    try
    {
      LnMessageServer server = LnMessageServer.getInstance() ;
      server.enable();
    }
    catch( RemoteException ex )
    {
      log.warn( "LnMessageServerAction Exception: " + ex );
    }
  }
}
