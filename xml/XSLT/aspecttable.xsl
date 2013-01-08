<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id$ -->

<!-- Stylesheet to convert a JMRI aspecttable file into displayable HTML    -->

<!-- Used by default when the file is displayed in a web browser            -->

<!-- This file is part of JMRI.  Copyright 2009-2011.                            -->
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
 
<xsl:stylesheet	version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:db="http://docbook.org/ns/docbook"
    >
      <xsl:param name="JmriCopyrightYear"/>

<!-- Need to instruct the XSLT processor to use HTML output rules.
     See http://www.w3.org/TR/xslt#output for more details
-->
<xsl:output method="html" encoding="ISO-8859-1"/>


<!-- Overide basic default template rule to 
     copy nodes to the output.  This lets e.g.
     XHTML be embedded in comments, etc, and
     be properly carried through. -->
<xsl:template match="*|/">
  <xsl:copy/><xsl:apply-templates/>
</xsl:template>

<!-- This primary template matches our root element in the input file.
     This will trigger the generation of the HTML skeleton document.
     In between we let the processor recursively process any contained
     elements, which is what the apply-templates instruction does.
     We also pick some stuff out explicitly in the head section using
     value-of instructions.
-->     
<xsl:template match='/'>

<html>
	<head>
		<title>JMRI &quot;<xsl:value-of select="aspecttable/name"/>&quot; Aspect Table</title>
	</head>
	
	<body>
		<h2>JMRI &quot;<xsl:value-of select="aspecttable/name"/>&quot; Aspect Table</h2>


<xsl:apply-templates select='aspecttable' />

<hr/>
This page was produced by <a href="http://jmri.org">JMRI</a>.
<P/>Copyright &#169; <xsl:value-of select="$JmriCopyrightYear" /> JMRI Community. 
<P/>JMRI, DecoderPro, PanelPro, SoundPro, SignalPro, DispatcherPro and associated logos are our trademarks.
<P/><A href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</A>
<P/>Site hosted by: <BR/>
<A href="http://sourceforge.net"><IMG src="http://sourceforge.net/sflogo.php?group_id=26788&amp;type=1" width="88" height="31" border="0" alt="SourceForge Logo"/> </A> 

	</body>
</html>

</xsl:template>

<!-- Overall table display -->
<xsl:template match="aspecttable">
    Date: <xsl:value-of select="date"/>
    <p/>
    <xsl:apply-templates select="reference"/>

    <!-- show the aspects -->
    <h2>Aspect Definitions</h2>
    <xsl:apply-templates select="aspects"/>

    <!-- link to appearances -->
    <hr/>
    <xsl:apply-templates select="appearancefiles"/>

    <!-- revision history -->
    <hr/>
    <xsl:apply-templates select="db:revhistory"/>

</xsl:template>

<!-- Display each aspect -->
<xsl:template match="aspect">
    <!-- create target for linking -->
    <xsl:element name="a"><xsl:attribute name="name"><xsl:value-of select="name"/></xsl:attribute></xsl:element>

    <!-- create section title -->
    <h3>
    <xsl:for-each select="rule">
        <xsl:value-of select="." />
        <xsl:text>: </xsl:text>
    </xsl:for-each>
    <xsl:value-of select="name"/>
    </h3>
    <!-- end create section title -->
    
    Indication: <xsl:value-of select="indication"/><br/>
    
    <!-- try to locate all images and show -->
    
    <!-- set the 'matchaspect' variable to the name of the aspect we're doing now -->
    <xsl:variable name="matchaspect"><xsl:value-of select="name" /></xsl:variable>

        <xsl:for-each select="/aspecttable/imagetypes/imagetype">
            <xsl:variable name="matchtype"><xsl:value-of select="@type" /></xsl:variable>
        
            <table><tr>
            <!-- index through all the files -->
            <!-- Bug: Firefox doesn't properly preserve document order here... -->
            <xsl:for-each select="document(/aspecttable/appearancefiles/appearancefile/@href)/appearancetable/appearances/appearance">
                <!-- looking at all aspects to find the one matching matchaspect -->
                <xsl:if test="aspectname = $matchaspect">
                    <!-- found, make image tag(s) -->
                    <xsl:for-each select="imagelink">

                        <xsl:if test="@type = $matchtype">

                              <td valign="bottom"><xsl:element name="img">
                                <xsl:attribute name="src">
                                    <xsl:value-of select="."/>
                                </xsl:attribute>
                              </xsl:element></td> 

                        </xsl:if>

                    </xsl:for-each>
                </xsl:if>
            </xsl:for-each>
            </tr></table>

        </xsl:for-each>

        <table><tr>
        <!-- Now repeat all that, except take unlabeled images -->
        <xsl:for-each select="document(/aspecttable/appearancefiles/appearancefile/@href)/appearancetable/appearances/appearance">
            <!-- looking at all aspects to find the one matching matchaspect -->
            <xsl:if test="aspectname = $matchaspect">
                <!-- found, make image tag(s) -->
                <xsl:for-each select="imagelink">

                    <xsl:if test='string-length(@type)=0'>

                          <td valign="bottom"><xsl:element name="img">
                            <xsl:attribute name="src">
                                <xsl:value-of select="."/>
                            </xsl:attribute>
                          </xsl:element></td> 

                    </xsl:if>

                </xsl:for-each>
            </xsl:if>
        </xsl:for-each>
        </tr></table>

<xsl:apply-templates/>

</xsl:template>

<!-- Some elements displayed specifically, don't show by default -->
<xsl:template match="name" />
<xsl:template match="title" />
<xsl:template match="rule" />
<xsl:template match="indication" />

<xsl:template match="reference" >
 Reference: <xsl:apply-templates/><br/>
</xsl:template>

<xsl:template match="speed" >
 Speed:  <xsl:apply-templates/><br/>
</xsl:template>

<xsl:template match="speed2" >
 Speed2: <xsl:apply-templates/><br/>
</xsl:template>

<xsl:template match="route" >
 Route:  <xsl:apply-templates/><br/>
</xsl:template>

<xsl:template match="description" >
 Description: <xsl:apply-templates/><br/>
</xsl:template>

<xsl:template match="comment" >
 Comment: <xsl:apply-templates/><br/>
</xsl:template>

<!-- Display imagelink as image -->
<xsl:template match="imagelink">
    <xsl:element name="img">
        <xsl:attribute name="src">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:element>
</xsl:template>

<xsl:template match="appearancefiles">
<!-- Display appearancefile section -->
<h2>Signal Mast Definition Files</h2>
<xsl:apply-templates select="appearancefile"/>
</xsl:template>

<!-- Display appearancefile elements as links to separate files -->
<xsl:template match="appearancefile">
    <xsl:element name="a">
        <xsl:attribute name="href">
            <xsl:value-of select="@href"/>
        </xsl:attribute>
        <xsl:value-of select="document(@href)/appearancetable/name" />
    </xsl:element>
    <br/>
    <!-- try to locate all images and show -->
    <table><tr>
    <!-- index through all the files -->
    <!-- Bug: Firefox doesn't properly preserve document order here... -->
    <xsl:for-each select="document(@href)/appearancetable/appearances/appearance/imagelink">
            <!-- found, make an image tag -->
            <td valign="bottom"><xsl:element name="img">
                <xsl:attribute name="src">
                    <xsl:value-of select="."/>
                </xsl:attribute>
            </xsl:element></td> 
    </xsl:for-each>
    </tr></table>
    <p/>
</xsl:template>

<!-- Display revision history -->
<xsl:include href="show-revhistory.xsl" />

  
</xsl:stylesheet>
