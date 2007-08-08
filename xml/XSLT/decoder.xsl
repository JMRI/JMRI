<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Edited with XML Spy v2007 (http://www.altova.com) -->
<html xsl:version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml">
  <body style="font-family:Arial,helvetica,sans-serif;font-size:12pt;
        background-color:#EEEEEE">
        
    <!-- display family info -->
    <xsl:for-each select="decoder-config/decoder/family">
    Family: <xsl:value-of select="@name"/>
    Manufacturer: <xsl:value-of select="@mfg"/>
    
    </xsl:for-each>

    <!-- display author info -->
    <xsl:for-each select="decoder-config/version">
      <div style="background-color:gray;color:white;padding:4px">
        <span style="font-weight:bold;color:white">
          Author: <xsl:value-of select="@author"/>
        </span>
          
        Version: <xsl:value-of select="@version"/>
        Updated: <xsl:value-of select="@lastUpdated"/>
      </div>
    </xsl:for-each>

    <!-- display model info -->
    <xsl:for-each select="decoder-config/decoder/family/model">
        <p>
          Model: <xsl:value-of select="@model"/>
                <br/>
            <xsl:value-of select="@numOuts"/> outputs, <xsl:value-of select="@numFns"/> functions
            <br/>
            <xsl:value-of select="@formFactor"/> form factor (manufacturer's labeling)
            <br/>
            <xsl:value-of select="@connector"/> connector
            <br/>
            Max motor current: <xsl:value-of select="@maxMotorCurrent"/>
            <br/>

            <xsl:for-each select="size">
                Length: <xsl:value-of select="@length"/>
                Width: <xsl:value-of select="@width"/>
                Height: <xsl:value-of select="@height"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="@units"/>
            </xsl:for-each>
            <xsl:for-each select="output">
                <br/>
                Output <xsl:value-of select="@name"/>
                labeled <xsl:value-of select="@label"/>
                connection: <xsl:value-of select="@connection"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="@units"/>
            </xsl:for-each>
        </p>          

    </xsl:for-each>
    
  </body>
</html>