<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Copyright (C) Bob Jacobsen 2007 All rights reserved -->
<!-- See the COPYING file for more information on licensing and appropriate use -->
<!-- $Id$ -->


<!-- This XSLT transform is used when a JMRI decoder definition -->
<!-- file is displayed by a web browser -->

<!-- This file is part of JMRI.  Copyright 2007-2016.                       -->
<!--                                                                        -->
<!-- JMRI is free software; you can redistribute it and/or modify it under  -->
<!-- the terms of version 2 of the GNU General Public License as published  -->
<!-- by the Free Software Foundation. See the "COPYING" file for a copy     -->
<!-- of this license.                                                       -->
<!--                                                                        -->
<!-- JMRI is distributed in the hope that it will be useful, but WITHOUT    -->
<!-- ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or  -->
<!-- FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License  -->
<!-- for more details.                                                      -->
 
<xsl:stylesheet	version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:docbook="http://docbook.org/ns/docbook">
      <xsl:param name="JmriCopyrightYear">
        2000 - 2016
      </xsl:param>

<!-- Need to instruct the XSLT processor to use HTML output rules.
     See http://www.w3.org/TR/xslt#output for more details
-->
<xsl:output method="html" encoding="ISO-8859-1"/>


<!-- This first template matches our root element in the input file.
     This will trigger the generation of the HTML skeleton document.
     In between we let the processor recursively process any contained
     elements, which is what the apply-templates instruction does.
     We also pick some stuff out explicitly in the head section using
     value-of instructions.
-->     

<xsl:template match='decoder-config'>

<html xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" xsl:version="1.0">
  <head>
  </head>
  <body style="font-family:Arial,helvetica,sans-serif;font-size:12pt;         background-color:#EEEEEE">
<!-- display family info -->
    <xsl:for-each select="decoder/family">
    Family: <xsl:value-of select="@name"/><br/>
    Manufacturer: <xsl:value-of select="@mfg"/>
    </xsl:for-each>
<!-- display old-style version comments -->
    <p><div style="background-color:gray;color:white;padding:4px"><span style="font-weight:bold;color:white">
    Definition versions:</span>
      <xsl:for-each select="comment()">
        <br/><xsl:value-of select="."/>
      </xsl:for-each>
<!-- display author info from old version tags -->
    
    <xsl:for-each select="version">
        <br/><span style="font-weight:bold;color:white">
        Author: <xsl:value-of select="@author"/>
        Version: <xsl:value-of select="@version"/>
        Updated: <xsl:value-of select="@lastUpdated"/>
        </span>
    </xsl:for-each>
    
<!-- display author info from new elements -->
    <xsl:for-each select="docbook:authorgroup/docbook:author/docbook:personname">   
        <br/><span style="font-weight:bold;color:white">
        Author: <xsl:value-of select="docbook:firstname"/> 
                <xsl:value-of select="docbook:surname"/>
        </span>
    </xsl:for-each>
    <xsl:for-each select="docbook:revhistory/docbook:revision">
        <br/><span style="font-weight:bold;color:white">
        Revision <xsl:value-of select="docbook:revnumber"/>: <xsl:value-of select="docbook:date"/> 
            (<xsl:value-of select="docbook:authorinitials"/>)
            <xsl:value-of select="docbook:revremark"/> 
        </span>
    </xsl:for-each>

    </div>    
    </p>


<p>
JMRI software, including this file, is distributed under license. That
license defines the terms under which you can use, modify and/or distribute
it.  Please see our 
<a href="http://jmri.org/Copyright.html">licensing page</a> 
for more information.
</p>
<hr/>
    
    Family version number low:
      <xsl:value-of select="decoder/family[last()]/@lowVersionID"/>
    
    high:
      <xsl:value-of select="decoder/family[last()]/@highVersionID"/>
      
  <!-- display model info -->
<xsl:for-each select="decoder/family/model">
<hr/>

    Model: <xsl:value-of select="@model"/><br/>
    Version number low: <xsl:value-of select="@lowVersionID"/>
    high: <xsl:value-of select="@highVersionID"/>

    <div style="margin: 40px;">
        <xsl:value-of select="@numOuts"/> outputs,
        <xsl:value-of select="@numFns"/> functions
        <br/>

        Form factor: "<xsl:value-of select="@formFactor"/>" (manufacturer's labeling)
        <br/>

        Conenctor: "<xsl:value-of select="@connector"/>"
        <br/>

        Length: <xsl:value-of select="size[last()]/@length"/> <xsl:value-of select="size[last()]/@units"/>
        Width:  <xsl:value-of select="size[last()]/@width"/> <xsl:value-of select="size[last()]/@units"/>
        Height: <xsl:value-of select="size[last()]/@height"/> <xsl:value-of select="size[last()]/@units"/>
        <br/>

        Max input voltage: <xsl:value-of select="@maxInputVolts"/>
        <br/>

        Max motor current: <xsl:value-of select="@maxMotorCurrent"/>
        <br/>

        Max total current: <xsl:value-of select="@maxTotalCurrent"/>
        <br/>

        NMRA warrant: <xsl:value-of select="@nmraWarrant"/>
        issued:
        <xsl:value-of select="@nmraWarrantStart"/>
        expires:
        <xsl:value-of select="@nmraWarrantEnd"/>
        <br/>
        
        Outputs<br/>
            <xsl:for-each select="output">
                <span style="margin: 80px;">
                "<xsl:value-of select="@name"/>"
                labeled
                  "<xsl:value-of select="@label"/>"
                connection type:
                  "<xsl:value-of select="@connection"/>"
                max current:
                  <xsl:value-of select="@maxcurrent"/>
                </span>
                <br/>
            </xsl:for-each>
    </div>
</xsl:for-each> <!-- end processing each model element-->
    

<HR/>
This page was produced by <a href="http://jmri.org">JMRI</a>.
<P/>Copyright &#169; <xsl:value-of select="$JmriCopyrightYear" /> JMRI Community. 
<P/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
<P/><A href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</A>
<P/>Site hosted by: <BR/>
<A href="http://sourceforge.net"><IMG src="http://sourceforge.net/sflogo.php?group_id=26788&amp;type=1" width="88" height="31" border="0" alt="SourceForge Logo"/> </A> 

  </body>
</html>

</xsl:template>
</xsl:stylesheet>
