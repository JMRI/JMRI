<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Copyright (C) Bob Jacobsen 2007 All rights reserved -->
<!-- See the COPYING file for more information on licensing and appropriate use -->
<!-- $Id: decoder.xsl,v 1.8 2007-08-21 07:49:43 jacobsen Exp $ -->
<!-- This XSLT transform is used when a JMRI decoder definition -->
<!-- file is displayed by a web browser -->
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
</p>
    <form method="post" action="/cgi-bin/updateDecoder.cgi">
    <!-- store the family name for later retrieval -->
    <xsl:for-each select="decoder-config/decoder/family"><!-- unique -->
    
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
        <xsl:attribute name="size">3</xsl:attribute><xsl:attribute name="value">
        <xsl:value-of select="@formFactor"/></xsl:attribute>
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
        <xsl:if test="@nmraWarrant = 'true'">
            <xsl:attribute name="checked">checked</xsl:attribute>
        </xsl:if>
      </xsl:element>
      
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
    
    <xsl:element name="hr"/>
    <input type="submit" value="Enter"/>
    </form>
  </body>
</html>
