<?xml version="1.0" encoding="iso-8859-1"?>
<!-- Copyright (C) Bob Jacobsen 2007 All rights reserved -->
<!-- See the COPYING file for more information on licensing and appropriate use -->

<!-- This XSLT transform is used when a JMRI decoder definition -->
<!-- file is displayed by a web browser -->

<!-- This file is part of JMRI.  Copyright 2007-2018.                       -->
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
 
<xsl:stylesheet	version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- Define the copyright year for the output page
     In batch work via running Ant, this is defined
     via the build.xml file
-->
<xsl:param name="JmriCopyrightYear" select="1997-2019" />

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
  <body style="font-family:Arial,helvetica,sans-serif;font-size:12pt;         background-color:#EEEEEE">
<!-- display family info -->
    <xsl:for-each select="decoder-config/decoder/family">
    Family: <xsl:value-of select="@name"/><br/>
    Manufacturer: <xsl:value-of select="@mfg"/>
    </xsl:for-each>
<!-- display copyright comments -->
    <p>
      <xsl:for-each select="comment()">
        <br/>
        <xsl:value-of select="."/>
      </xsl:for-each>
    </p>
<!-- display author info -->
    <xsl:for-each select="decoder-config/version">
      <div style="background-color:gray;color:white;padding:4px"><span style="font-weight:bold;color:white">
          Author: <xsl:value-of select="@author"/>
        </span>
          
        Version: <xsl:value-of select="@version"/>
        Updated: <xsl:value-of select="@lastUpdated"/></div>
    </xsl:for-each>
    <p>
If you want to submit an update to any of this information,
change the form and hit the "Enter" button at the bottom.
You can also add a new model in the spaces at the bottom.
</p><p>
JMRI software, including this file, is distributed under license. That
license defines the terms under which you can use, modify and/or distribute
it.  Please see our 
<a href="http://jmri.org/Copyright.html">licensing page</a> 
for more information.
</p>
<hr/>
    <form method="post" action="/cgi-bin/updateDecoder.cgi">
    <!-- store the mfg andfamily name for later retrieval -->
    <xsl:for-each select="decoder-config/decoder/family"><!-- unique -->
    
      <xsl:element name="input">
          <xsl:attribute name="type">hidden</xsl:attribute>
          <xsl:attribute name="name">Manufacturer</xsl:attribute>
          <xsl:attribute name="value"><xsl:value-of select="@mfg"/></xsl:attribute>
      </xsl:element>
      <xsl:element name="input">
          <xsl:attribute name="type">hidden</xsl:attribute>
          <xsl:attribute name="name">Family</xsl:attribute>
          <xsl:attribute name="value"><xsl:value-of select="@name"/></xsl:attribute>
      </xsl:element>
    
    </xsl:for-each><!-- display version number -->
    
    Family version number low:
      <xsl:element name="input">
        <xsl:attribute name="type">int</xsl:attribute>
        <xsl:attribute name="name">family-version-low</xsl:attribute>
        <xsl:attribute name="size">3</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="decoder-config/decoder/family[last()]/@lowVersionID"/></xsl:attribute>
      </xsl:element>
    
    high:
      <xsl:element name="input">
        <xsl:attribute name="type">int</xsl:attribute>
        <xsl:attribute name="name">family-version-high</xsl:attribute>
        <xsl:attribute name="size">3</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="decoder-config/decoder/family[last()]/@highVersionID"/></xsl:attribute>
      </xsl:element>
      
  <!-- display model info -->
  <xsl:for-each select="decoder-config/decoder/family/model">
      <xsl:element name="hr"/><p>

      <xsl:element name="a">
        <xsl:attribute name="name"><xsl:value-of select="@model"/></xsl:attribute>
      </xsl:element>

      Model: <xsl:value-of select="@model"/><br/>
      Version number low:
      <xsl:element name="input">
        <xsl:attribute name="type">int</xsl:attribute>
        <xsl:attribute name="name"><xsl:value-of select="@model"/>-version-low</xsl:attribute>
        <xsl:attribute name="size">3</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="@lowVersionID"/></xsl:attribute>
      </xsl:element>
      high:
      <xsl:element name="input"><xsl:attribute name="type">int</xsl:attribute>
        <xsl:attribute name="name"><xsl:value-of select="@model"/>-version-high</xsl:attribute>
        <xsl:attribute name="size">3</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="@highVersionID"/></xsl:attribute>
      </xsl:element>

      <br/>
      
      <xsl:element name="input">
        <xsl:attribute name="type">int</xsl:attribute>
        <xsl:attribute name="name"><xsl:value-of select="@model"/>-Outs</xsl:attribute>
        <xsl:attribute name="size">3</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="@numOuts"/></xsl:attribute>
      </xsl:element>
      outputs,
      <xsl:element name="input">
        <xsl:attribute name="type">int</xsl:attribute>
        <xsl:attribute name="name"><xsl:value-of select="@model"/>-Fns</xsl:attribute>
        <xsl:attribute name="size">3</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="@numFns"/></xsl:attribute>
      </xsl:element>
      functions
      
      <br/>
      
      <xsl:element name="input">
        <xsl:attribute name="type">text</xsl:attribute>
        <xsl:attribute name="name"><xsl:value-of select="@model"/>-formFactor</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="@formFactor"/></xsl:attribute>
      </xsl:element>
      form factor (manufacturer's labeling)
      
      <br/>
      
      <xsl:element name="select"><xsl:attribute name="name"><xsl:value-of select="@model"/>-connector</xsl:attribute><xsl:element name="option"><xsl:if test="@connector = '9pin'"><xsl:attribute name="selected"/></xsl:if>
        9pin
        </xsl:element><xsl:element name="option"><xsl:if test="@connector = 'NMRAsmall'"><xsl:attribute name="selected"/></xsl:if>
        NMRAsmall
        </xsl:element><xsl:element name="option"><xsl:if test="@connector = 'NMRAmedium'"><xsl:attribute name="selected"/></xsl:if>
        NMRAmedium
        </xsl:element><xsl:element name="option"><xsl:if test="@connector = 'NMRAlarge'"><xsl:attribute name="selected"/></xsl:if>
        NMRAlarge
        </xsl:element><xsl:element name="option"><xsl:if test="@connector = 'other'"><xsl:attribute name="selected"/></xsl:if>
        other
        </xsl:element><xsl:element name="option"><xsl:if test="@connector = 'unspecified'"><xsl:attribute name="selected"/></xsl:if>
        unspecified
        </xsl:element></xsl:element>
      connector
      
      <br/>

      Length:
      <xsl:element name="input">
        <xsl:attribute name="type">float</xsl:attribute>
        <xsl:attribute name="name"><xsl:value-of select="@model"/>-length</xsl:attribute>
        <xsl:attribute name="size">10</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="size[last()]/@length"/></xsl:attribute>
      </xsl:element>
      Width:
      <xsl:element name="input">
        <xsl:attribute name="type">float</xsl:attribute>
        <xsl:attribute name="name"><xsl:value-of select="@model"/>-width</xsl:attribute>
        <xsl:attribute name="size">10</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="size[last()]/@width"/></xsl:attribute>
      </xsl:element>
      Height: 
      <xsl:element name="input">
        <xsl:attribute name="type">float</xsl:attribute>
        <xsl:attribute name="name"><xsl:value-of select="@model"/>-height</xsl:attribute>
        <xsl:attribute name="size">10</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="size[last()]/@height"/>
        </xsl:attribute>
      </xsl:element>
      <xsl:text> </xsl:text>
      <xsl:element name="select">
        <xsl:attribute name="name">
        <xsl:value-of select="@model"/>-units</xsl:attribute>
          <xsl:element name="option"><xsl:if test="size[last()]/@units = 'inches'"><xsl:attribute name="selected"/></xsl:if>
        inches
          </xsl:element><xsl:element name="option"><xsl:if test="size[last()]/@units = 'cm'"><xsl:attribute name="selected"/></xsl:if>
        cm
          </xsl:element><xsl:element name="option"><xsl:if test="size[last()]/@units = 'mm'"><xsl:attribute name="selected"/></xsl:if>
        mm
          </xsl:element>
      </xsl:element>
        
      <br/>

      Max input voltage:
      <xsl:element name="input">
        <xsl:attribute name="type">text</xsl:attribute>
        <xsl:attribute name="name"><xsl:value-of select="@model"/>-maxInputVolts</xsl:attribute>
        <xsl:attribute name="size">15</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="@maxInputVolts"/></xsl:attribute>
      </xsl:element>
      
      <br/>

      Max motor current:
      <xsl:element name="input">
        <xsl:attribute name="type">text</xsl:attribute>
        <xsl:attribute name="name"><xsl:value-of select="@model"/>-maxMotorCurrent</xsl:attribute>
        <xsl:attribute name="size">15</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="@maxMotorCurrent"/></xsl:attribute>
      </xsl:element>
      
      <br/>

      Max total current:
      <xsl:element name="input">
        <xsl:attribute name="type">text</xsl:attribute>
        <xsl:attribute name="name"><xsl:value-of select="@model"/>-maxTotalCurrent</xsl:attribute>
        <xsl:attribute name="size">15</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="@maxTotalCurrent"/></xsl:attribute>
      </xsl:element>
      
      <br/>

      NMRA warrant
      <xsl:element name="input">
        <xsl:attribute name="type">checkbox</xsl:attribute>
        <xsl:attribute name="name"><xsl:value-of select="@model"/>-nmraWarrant</xsl:attribute>
        <xsl:if test="@nmraWarrant = 'yes'">
            <xsl:attribute name="checked">checked</xsl:attribute>
        </xsl:if>
      </xsl:element>
      
      issued:
      <xsl:element name="input"><xsl:attribute name="type">date</xsl:attribute><xsl:attribute name="name"><xsl:value-of select="@model"/>-nmraWarrantStart</xsl:attribute><xsl:attribute name="value"><xsl:value-of select="@nmraWarrantStart"/></xsl:attribute></xsl:element>
      expires:
      <xsl:element name="input"><xsl:attribute name="type">date</xsl:attribute><xsl:attribute name="name"><xsl:value-of select="@model"/>-nmraWarrantEnd</xsl:attribute><xsl:attribute name="value"><xsl:value-of select="@nmraWarrantEnd"/></xsl:attribute></xsl:element>
      <br/>

        <xsl:for-each select="output">
            Output <xsl:value-of select="@name"/>
            labeled
              <xsl:element name="input"><xsl:attribute name="type">text</xsl:attribute><xsl:attribute name="name"><xsl:value-of select="../@model"/>-<xsl:value-of select="@name"/>-output-label</xsl:attribute><xsl:attribute name="size">10</xsl:attribute><xsl:attribute name="value"><xsl:value-of select="@label"/></xsl:attribute></xsl:element>
            connection:
              <xsl:element name="input"><xsl:attribute name="type">text</xsl:attribute><xsl:attribute name="name"><xsl:value-of select="../@model"/>-<xsl:value-of select="@name"/>-output-connection</xsl:attribute><xsl:attribute name="size">10</xsl:attribute><xsl:attribute name="value"><xsl:value-of select="@connection"/></xsl:attribute></xsl:element>
            max current:
              <xsl:element name="input"><xsl:attribute name="type">text</xsl:attribute><xsl:attribute name="name"><xsl:value-of select="../@model"/>-<xsl:value-of select="@name"/>-output-maxcurrent</xsl:attribute><xsl:attribute name="size">10</xsl:attribute><xsl:attribute name="value"><xsl:value-of select="@maxcurrent"/></xsl:attribute></xsl:element>
            <br/>
        </xsl:for-each>

    </p>
    </xsl:for-each> <!-- end processing each model element-->
    
    <!-- manually add entries for new model -->
    <p><hr/><hr/>
      Add New Model: 
      <xsl:element name="input">
        <xsl:attribute name="type">test</xsl:attribute>
        <xsl:attribute name="name">new-model-name</xsl:attribute>
        <xsl:attribute name="size">25</xsl:attribute>
      </xsl:element>
      
      <br/>
      
      Version number low:
      <xsl:element name="input">
        <xsl:attribute name="type">int</xsl:attribute>
        <xsl:attribute name="name">new-version-low</xsl:attribute>
        <xsl:attribute name="size">3</xsl:attribute>
      </xsl:element>
      high:
      <xsl:element name="input"><xsl:attribute name="type">int</xsl:attribute>
        <xsl:attribute name="name">new-version-high</xsl:attribute>
        <xsl:attribute name="size">3</xsl:attribute>
      </xsl:element>

      <br/>
      
      <xsl:element name="input">
        <xsl:attribute name="type">int</xsl:attribute>
        <xsl:attribute name="name">new-Outs</xsl:attribute>
        <xsl:attribute name="size">3</xsl:attribute>
      </xsl:element>
      outputs,
      <xsl:element name="input">
        <xsl:attribute name="type">int</xsl:attribute>
        <xsl:attribute name="name">new-Fns</xsl:attribute>
        <xsl:attribute name="size">3</xsl:attribute>
      </xsl:element>
      functions
      
      <br/>
      
      <xsl:element name="input">
        <xsl:attribute name="type">text</xsl:attribute>
        <xsl:attribute name="name">new-formFactor</xsl:attribute>
      </xsl:element>
      form factor (manufacturer's labeling)
      
      <br/>
      
      <xsl:element name="select">
        <xsl:attribute name="name">new-connector</xsl:attribute>
        <xsl:element name="option">9pin</xsl:element>
        <xsl:element name="option">NMRAsmall</xsl:element>
        <xsl:element name="option">NMRAmedium</xsl:element>
        <xsl:element name="option">NMRAlarge</xsl:element>
        <xsl:element name="option">other</xsl:element>
        <xsl:element name="option">unspecified</xsl:element>
      </xsl:element>
      connector
      
      <br/>

      Length:
      <xsl:element name="input">
        <xsl:attribute name="type">float</xsl:attribute>
        <xsl:attribute name="name">new-length</xsl:attribute>
        <xsl:attribute name="size">10</xsl:attribute>
      </xsl:element>
      Width:
      <xsl:element name="input">
        <xsl:attribute name="type">float</xsl:attribute>
        <xsl:attribute name="name">new-width</xsl:attribute>
        <xsl:attribute name="size">10</xsl:attribute>
      </xsl:element>
      Height: 
      <xsl:element name="input">
        <xsl:attribute name="type">float</xsl:attribute>
        <xsl:attribute name="name">new-height</xsl:attribute>
        <xsl:attribute name="size">10</xsl:attribute>
      </xsl:element>
      <xsl:text> </xsl:text>
      <xsl:element name="select">
        <xsl:attribute name="name">new-units</xsl:attribute>
        <xsl:element name="option">inches</xsl:element>
        <xsl:element name="option">cm</xsl:element>
        <xsl:element name="option">mm</xsl:element>
      </xsl:element>
        
      <br/>

      Max input voltage:
      <xsl:element name="input">
        <xsl:attribute name="type">text</xsl:attribute>
        <xsl:attribute name="name">new-maxInputVolts</xsl:attribute>
        <xsl:attribute name="size">15</xsl:attribute>
      </xsl:element>
      
      <br/>

      Max motor current:
      <xsl:element name="input">
        <xsl:attribute name="type">text</xsl:attribute>
        <xsl:attribute name="name">new-maxMotorCurrent</xsl:attribute>
        <xsl:attribute name="size">15</xsl:attribute>
      </xsl:element>
      
      <br/>

      Max total current:
      <xsl:element name="input">
        <xsl:attribute name="type">text</xsl:attribute>
        <xsl:attribute name="name">new-maxTotalCurrent</xsl:attribute>
        <xsl:attribute name="size">15</xsl:attribute>
      </xsl:element>
      
      <br/>

      NMRA warrant
      <xsl:element name="input">
        <xsl:attribute name="type">checkbox</xsl:attribute>
        <xsl:attribute name="name">new-nmraWarrant</xsl:attribute>
      </xsl:element>
      
      expires:
      <xsl:element name="input">
        <xsl:attribute name="type">date</xsl:attribute>
        <xsl:attribute name="name">end-nmraWarrantEnd</xsl:attribute>
      </xsl:element>
      <br/>


    </p>
    
    <xsl:element name="hr"/>
    <input type="submit" value="Enter"/>
    </form>

<hr/>
This page was produced by <a href="http://jmri.org">JMRI</a>.
<p/>Copyright &#169; <xsl:value-of select="$JmriCopyrightYear" /> JMRI Community.
<p/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
<p/><a href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</a>
<p/>Site hosted by: <br/>
      <a href="http://www.tagadab.com/">
          <img src="https://www.tagadab.com/sites/default/files/logo-tagadab-nostrap.png" height="28" width="103" border="0" alt="Tagadab logo"/></a>
  </body>
</html>

</xsl:template>
</xsl:stylesheet>
