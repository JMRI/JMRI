<?xml version="1.0" encoding="iso-8859-1"?>

<!-- Stylesheet to convert a JMRI manufacturer ID list  -->
<!-- into an HTML page on decoder ID -->

<!-- Used by default when the nmra_mfg_list.xml file is displayed in a web browser-->

<!-- This file is part of JMRI.  Copyright 2007-2020.                       -->
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
     via the build.xml file. We build it by concatenation
     because XPath will evaluate '1997 - 2017' to '20'.
-->
<xsl:param name="JmriCopyrightYear" select="concat('1997','-','2021')" />

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
<xsl:template match='mfgList'>

<html>
	<head>
		<title>NMRA Manufacturer IDs</title>
	</head>
	
	<body>
		<h2>NMRA Manufacturer IDs</h2>

  These tables are made from
  <a href="https:/jmri.org/index.shtml">JMRI's</a>
  local cache of the 
  <a href="http://nmra.org">NMRA</a>'s 
  <a href="https://www.nmra.org/sites/default/files/standards/sandrp/pdf/appendix_a_s-9.2.2.pdf">Manufacturer ID definitions</a>.
  The content is the NMRAs, the formatting is JMRI's. 
  <br/>
  (And if you're curious why we make that distinction, there's 
  <a href="https://www.jmri.org/k/summary.shtml">more info here</a>)

  <!-- the following blocks are identical except for title and sort element -->
  
  <h3>By Manufacturer Name</h3>
  <table border="1">
  <tr><th>Name</th><th>ID number</th></tr>
  <xsl:for-each select="manufacturer">
    <xsl:sort select="@mfg" /> <!-- by name -->
    <xsl:if test="not( @mfg = 'NMRA' )" >
      <xsl:element name="a">
          <xsl:attribute name="name"><xsl:value-of select="@mfgID"/></xsl:attribute>
          <xsl:attribute name="id"><xsl:value-of select="@mfgID"/></xsl:attribute>
      </xsl:element>
      <tr><td><xsl:value-of select="@mfg"/></td><td align="right"><xsl:value-of select="@mfgID"/></td></tr>
    </xsl:if>
  </xsl:for-each>
  </table>

  <h3>By Manufacturer Number</h3>
  <table border="1">
  <tr><th>Name</th><th>ID number</th></tr>
  <xsl:for-each select="manufacturer">
    <xsl:sort select="@mfgID" data-type="number"/> <!-- by number -->
    <xsl:if test="not( @mfg = 'NMRA' )" >
      <xsl:element name="a">
          <xsl:attribute name="name"><xsl:value-of select="@mfgID"/></xsl:attribute>
          <xsl:attribute name="id"><xsl:value-of select="@mfgID"/></xsl:attribute>
      </xsl:element>
      <tr><td><xsl:value-of select="@mfg"/></td><td align="right"><xsl:value-of select="@mfgID"/></td></tr>
    </xsl:if>
  </xsl:for-each>
  </table>

<hr/>
This page was produced by <a href="http://jmri.org">JMRI</a>.
<p/>Copyright &#169; <xsl:value-of select="$JmriCopyrightYear" /> JMRI Community.

<p/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
<p/><A href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</A>
	</body>
</html>

</xsl:template>


</xsl:stylesheet>
