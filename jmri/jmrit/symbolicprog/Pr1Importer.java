// Pr1Importer.java

package jmri.jmrit.symbolicprog;

import java.util.*;
import java.io.FileInputStream;
import java.io.*;

/**
 * Import CV values from a "PR1" file written by PR1DOS or PR1WIN
 *
 * @author			Alex Shepherd   Copyright (C) 2003
 * @version			$Revision: 1.1 $
 */
public class Pr1Importer {
    private static final String versionKey = "Version" ;
    Properties  m_CVs ;
    boolean     m_packedValues = false ;
    
    public Pr1Importer( String fileName ) throws IOException {
        m_CVs = new Properties() ;
        FileInputStream file = new FileInputStream(fileName);
        m_CVs.load( file );
        
        // First check to see if the file contains a Version=x entry and if it
        // does assume it is a PR1WIN file that has packed values
        if( m_CVs.containsKey( versionKey ) ) {
            if (m_CVs.get(versionKey).equals("0"))
                m_packedValues = true;
            
            else
                throw new IOException("Unsupported PR1 File Version");
        }
        
        // Have a look at the values and see if there are any entries with values
        // greater out of the range 0..255. If they are found then also assume PR1WIN
        else {
            Set cvSet = m_CVs.entrySet();
            Iterator cvIterator = cvSet.iterator() ;
            
            while( cvIterator.hasNext() ) {
                Map.Entry cvEntry = (Map.Entry) cvIterator.next() ;
                String cvKey = (String)cvEntry.getKey() ;
                if( cvKey.startsWith( "CV" ) )
                    {
                        String cvValue = (String)cvEntry.getValue() ;
                        int cvIntValue = Integer.parseInt( cvValue ) ;
                        if( ( cvIntValue < 0 ) || ( cvIntValue > 255 ) )
                            {
                                m_packedValues = true;
                                return ;
                            }
                    }
            }
        }
    }
    
    public int getCV( int cvNumber ) throws Exception {
        int result ;
        
        if( m_packedValues ) {
            String cvKey = "CV" + ((cvNumber / 4) + 1) ;
            String cvValueStr = m_CVs.getProperty( cvKey ) ;
            if( cvValueStr == null )
                throw new Exception( "CV not found" ) ;
            
            int shiftBits = ((cvNumber - 1) % 4 ) * 8 ;
            long cvValue = Long.parseLong( cvValueStr ) ;
            
            if( cvValue < 0 ) {
                result = (int)(((cvValue + 0x7FFFFFFF) >> shiftBits ) % 256 ) ;
                if( shiftBits > 16 )
                    result += 127 ;
            } else
                result = (int)((cvValue >> shiftBits ) % 256 ) ;
        } else {
            String cvKey = "CV" + cvNumber ;
            String cvValueStr = m_CVs.getProperty(cvKey);
            if( cvValueStr == null )
                throw new Exception( "CV not found" ) ;
            
            result = Integer.parseInt( cvValueStr ) ;
        }
        
        return result ;
    }
    
    public static void main(String[] args) {
        try {
            //      String fileName = "Y:/ModelRail/pr1dos/Gp071586.dec" ;
            //      String fileName = "Y:/ModelRail/pr1dos/a1.dec" ;
            //      String fileName = "Y:/ModelRail/pr1dos/GP382158.DEC" ;
            //      String fileName = "Y:/ModelRail/pr1dos/Gp071586.dec" ;
            //      String fileName = "Y:/ModelRail/pr1dos/sw9_58.dec" ;
            //      String fileName = "Y:/ModelRail/pr1dos/sw9_7405.dec" ;
            String fileName = "Y:/ModelRail/pr1dos/alex.dec" ;
            Pr1Importer cvList = new Pr1Importer( fileName );
            
            System.out.println( "File: " + fileName ) ;
            System.out.println( "CV#, Hex Int, Dec Int, Hex Byte, Dec Byte" ) ;
            for( int cvIndex = 1; cvIndex <= 512; cvIndex++ )  {
                try {
                    int cvValue = cvList.getCV(cvIndex);
                    System.out.println("CV" + cvIndex + " " + Integer.toHexString(cvValue) +
                                       ", " + cvValue);
                } catch (Exception ex1) {}
            }
        } catch (IOException ex) {}
    }
}
