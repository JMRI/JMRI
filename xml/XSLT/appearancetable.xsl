<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id$ -->

<!-- Stylesheet to convert a JMRI appearance table file into displayable HTML    -->

<!-- Used by default when the file is displayed in a web browser            -->

<!-- This file is part of JMRI.  Copyright 2009-2011.                       -->
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

<!-- Need to instruct the XSLT processor to use HTML output rules.
     See http://www.w3.org/TR/xslt#output for more details
-->
<xsl:output method="html" encoding="ISO-8859-1"/>

<!-- Overide basic default template rule to 
     copy nodes to the output.  This lets e.g.
     XHTML be embedded in comments, etc, and
     be properly carried through. -->
<xsl:template match="*|/">
  <xsl:copy-of select="."/>
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
		<title>JMRI &quot;<xsl:value-of select="document('http:aspects.xml', .)/aspecttable/name"/>
		                  <xsl:text>: </xsl:text>
		                  <xsl:value-of select="appearancetable/name"/>&quot; Appearance Table</title>
	</head>
	
	<body>
		<h2>JMRI &quot;<xsl:value-of select="document('http:aspects.xml', .)/aspecttable/name"/> 
		               <xsl:text>: </xsl:text>
		               <xsl:value-of select="appearancetable/name"/>&quot; Appearance Table</h2>


<xsl:apply-templates/>

<HR/>
This page was produced by <a href="http://jmri.org">JMRI</a>.
<P/>Copyright &#169; 1997 - 2012 JMRI Community. 
<P/>JMRI, DecoderPro, PanelPro, SoundPro, SignalPro, DispatcherPro and associated logos are our trademarks.
<P/><A href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</A>
<P/>Site hosted by: <BR/>
<A href="http://sourceforge.net"><IMG src="http://sourceforge.net/sflogo.php?group_id=26788&amp;type=1" width="88" height="31" border="0" alt="SourceForge Logo"/> </A> 

	</body>
</html>

</xsl:template>

<!-- Overall table display -->
<xsl:template match="appearancetable">
    For aspect table: 
    <a href="aspects.xml">
        <!-- The second argument provides the context (parent file) for the document() path; -->
        <!-- without it, the reference is relative to the stylesheet location -->
        <xsl:value-of select="document('http:aspects.xml', .)/aspecttable/name"/>
    </a>
    <p/>
    Name: <xsl:value-of select="name"/><p/>

    <xsl:apply-templates select="reference"/><p/>

    <xsl:apply-templates select="description"/><p/>

    <!-- show the appearances -->
    <xsl:apply-templates select="appearances"/>
	
	<xsl:apply-templates select="aspectMappings"/>
    
    <xsl:apply-templates select="specificappearances"/>

    <!-- revision history -->
    <hr/>
    <xsl:apply-templates select="db:revhistory"/>
</xsl:template>

<!-- Display each appearance -->
<xsl:template match="appearances"><xsl:apply-templates select="appearance"/></xsl:template>
<xsl:template match="appearance">
    <!-- set up to grab stuff from aspect.xml file -->
    <!-- set the 'matchaspect' variable to the name of the aspect we're doing now -->
    <xsl:variable name="matchaspect"><xsl:value-of select="aspectname" /></xsl:variable>

    <!-- start heading -->
    <h3>
    <!-- Compare to each aspect name in aspects.xml for matching rule -->
    <!-- The second argument provides the context (parent file) for the document() path; -->
    <!-- without it, the reference is relative to the stylesheet location -->
    <xsl:for-each select="document('http:aspects.xml', .)/aspecttable/aspects/aspect[name=$matchaspect]">
        <!-- looking at all aspects to find the one matching matchaspect -->
            <!-- now current node is match in aspects.xml -->
                        
            <!-- show title element if it exists -->
            <xsl:for-each select="rule">
                <xsl:value-of select="." />
                <xsl:text>: </xsl:text>
            </xsl:for-each>
    </xsl:for-each>

    <!-- name is also a link -->
        <!-- create target for linking -->
    <xsl:element name="a"><xsl:attribute name="href">aspects.xml#<xsl:value-of select="aspectname"/></xsl:attribute>
	<xsl:attribute name="name"><xsl:value-of select="aspectname"/></xsl:attribute>
    <xsl:value-of select="aspectname"/>
    </xsl:element>

    <!-- end heading -->
    </h3>
    <!-- end heading -->
    
    <!-- error if no match -->
    <xsl:if test="not(document('http:aspects.xml', .)/aspecttable/aspects/aspect[name=$matchaspect])">
        <em>Error: This appearance does not appear in the <a href="aspects.xml">aspect table</a>.
        Check spelling and upper/lower case.</em>
    </xsl:if>
    
    <!-- then compare to each aspect name in aspects.xml for match -->
    <!-- The second argument provides the context (parent file) for the document() path; -->
    <!-- without it, the reference is relative to the stylesheet location -->
    <xsl:for-each select="document('http:aspects.xml', .)/aspecttable/aspects/aspect[name=$matchaspect]">
            <!-- now current node is match in aspects.xml -->

            <!-- show title element if it exists -->
            <xsl:for-each select="title">
                <xsl:text>Title: </xsl:text>
                <xsl:value-of select="." />
                <br/>
            </xsl:for-each>

            <!-- show indication element if it exists -->
            <xsl:for-each select="indication">
                <xsl:text>Indication: </xsl:text>
                <xsl:value-of select="." />
                <br/>
            </xsl:for-each>
            
            <!-- show description element(s) if any -->
            <xsl:for-each select="description">
                <xsl:text>Description: </xsl:text>
                <xsl:apply-templates select="." />
                <br/>
            </xsl:for-each>
            
            <!-- show reference element(s) if any -->
            <xsl:for-each select="reference">
                <xsl:text>Aspect reference: </xsl:text>
                <xsl:apply-templates select="." />
                <br/>
            </xsl:for-each>
            
            <!-- show comment element(s) if any -->
            <xsl:for-each select="comment">
                <xsl:text>Comment: </xsl:text>
                <xsl:apply-templates select="." />
                <br/>
            </xsl:for-each>
            
            <xsl:for-each select="diverging">
                <xsl:text>Diverging flag set</xsl:text>
                <br/>
            </xsl:for-each>
            
            <p/>
    </xsl:for-each>

    <!-- display the aspect in a little table -->
    <table><tr><td>
    <!-- Put image to left if it exists -->
    <xsl:for-each select="imagelink">
        <xsl:element name="img">
            <xsl:attribute name="src">
                <xsl:value-of select="."/>
            </xsl:attribute>
        </xsl:element>
    </xsl:for-each>
    </td>
    <!-- show elements to right if they exist-->
    <td>
    <xsl:for-each select="show">
        Show: <xsl:value-of select="."/><br/>
    </xsl:for-each>
    
    </td></tr></table>
    <!-- show rest of element -->
    <xsl:apply-templates/>
    
</xsl:template>

<xsl:template match="aspectMappings">
  <br/>
  <b>Aspect Mapping</b>
  <!-- display aspect mappings in a small table -->
  <!-- header -->
  <table border="2" cellpadding="5">
    <tr>
	  <td>Advanced Aspect</td>
	  <td>Our Aspect</td>
	</tr>
  
    <!-- iterate through items -->
    <xsl:for-each select="aspectMapping">
	  <tr>
        <td>
		  <xsl:element name="a">
			<xsl:attribute name="href">#<xsl:value-of select="advancedAspect"/></xsl:attribute>
			<xsl:value-of select="advancedAspect"/>
		  </xsl:element>
		</td>
	    <td>
        <xsl:for-each select="ourAspect">
		  <xsl:element name="a">
			<xsl:attribute name="href">#<xsl:value-of select="."/></xsl:attribute>
			<xsl:value-of select="."/><br/>
		  </xsl:element>
        </xsl:for-each>
		</td>

      </tr>
    </xsl:for-each>
    <!-- close table -->
    </table>
    <br/>
</xsl:template>

<xsl:template match="specificappearances">
  <b>Specific Appearance Mapping</b>
  <!-- display aspect mappings in a small table -->
  <!-- header -->
  <table border="2" cellpadding="5">
    <tr>
	  <td>Appearance  </td>
	  <td>Mapped To  </td>
      <td>Alternative Image  </td>
	</tr>
  
    <!-- iterate through items -->
    <xsl:for-each select="danger">
	  <tr>
	    <td>
            Danger
		</td>
	    <td>
          <xsl:element name="a">
			<xsl:attribute name="href">#<xsl:value-of select="aspect"/></xsl:attribute>
			<xsl:value-of select="aspect"/>
		  </xsl:element>
          </td>
          <td>
            <xsl:for-each select="imagelink">
                <xsl:element name="img">
                    <xsl:attribute name="src">
                        <xsl:value-of select="."/>
                    </xsl:attribute>
                </xsl:element>
            </xsl:for-each>
		</td>
      </tr>
    </xsl:for-each>
    
    <xsl:for-each select="held">
	  <tr>
	    <td>
            Held
		</td>
	    <td>
          <xsl:element name="a">
			<xsl:attribute name="href">#<xsl:value-of select="aspect"/></xsl:attribute>
			<xsl:value-of select="aspect"/>
		  </xsl:element>
        </td>
	    <td>
            <xsl:for-each select="imagelink">
                <xsl:element name="img">
                    <xsl:attribute name="src">
                        <xsl:value-of select="."/>
                    </xsl:attribute>
                </xsl:element>
            </xsl:for-each>
		</td>
      </tr>
    </xsl:for-each>
    
    <xsl:for-each select="permissive">
	  <tr>
	    <td>
            Permissive
		</td>
	    <td>
          <xsl:element name="a">
			<xsl:attribute name="href">#<xsl:value-of select="aspect"/></xsl:attribute>
			<xsl:value-of select="aspect"/>
		  </xsl:element>
        </td>
	    <td>
            <xsl:for-each select="imagelink">
                <xsl:element name="img">
                    <xsl:attribute name="src">
                        <xsl:value-of select="."/>
                    </xsl:attribute>
                </xsl:element>
            </xsl:for-each>
		</td>
      </tr>
    </xsl:for-each>
    
    <xsl:for-each select="dark">
	  <tr>
	    <td>
            Dark
		</td>
	    <td>
          <xsl:element name="a">
			<xsl:attribute name="href">#<xsl:value-of select="aspect"/></xsl:attribute>
			<xsl:value-of select="aspect"/>
		  </xsl:element>
        </td>
	    <td>
            <xsl:for-each select="imagelink">
                <xsl:element name="img">
                    <xsl:attribute name="src">
                        <xsl:value-of select="."/>
                    </xsl:attribute>
                </xsl:element>
            </xsl:for-each>
		</td>
      </tr>
    </xsl:for-each>

    <!-- close table -->
    </table>
</xsl:template>

<!-- Ignore imagelink, already done -->
<xsl:template match="imagelink" />

<!-- already shown -->
<xsl:template match="aspectname"/>

<!-- Ignore show, already done -->
<xsl:template match="show" />

<xsl:template match="reference">
<p/>Appearance reference: <xsl:apply-templates/>
</xsl:template>

<!-- Display revision history -->
<xsl:include href="show-revhistory.xsl" />

</xsl:stylesheet>
