<?xml version="1.0" encoding="iso-8859-1"?>

<!-- Stylesheet fragment to display a docbook:revhistory element            -->


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

<!-- Display revision history -->
<xsl:template match="db:revhistory" >
  <xsl:for-each select="db:revision">
    <xsl:for-each select="db:revnumber">Version <xsl:value-of select="."/></xsl:for-each>
    <xsl:for-each select="db:date"> of <xsl:value-of select="."/></xsl:for-each>
    <xsl:for-each select="db:authorinitials"> by <xsl:value-of select="."/></xsl:for-each>    
    <xsl:for-each select="db:revremark">: <xsl:value-of select="."/></xsl:for-each>
    <br/>
  </xsl:for-each>
</xsl:template>

</xsl:stylesheet>
