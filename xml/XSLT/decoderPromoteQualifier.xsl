<?xml version="1.0" encoding="iso-8859-1"?>

<!-- Process a JMRI decoder file, moving qualifier elements to the top of the variable element -->
<!-- Thi sis for a JMRI 4.1.4 migration     -->
<!-- xmlint -format file.xml > file.xml.old -->
<!-- xsltproc decoderPromoteQualifier.xsl file.xml.old | xmllint -format - > file.xml.new      -->
<!-- diff file.xml file.xml.new -->

<!-- This file is part of JMRI.  Copyright 2009-2014.                       -->
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

<xsl:output method="xml" encoding="utf-8"/>

<!--specific template match for variable element with qualifier element(s) -->
    <xsl:template match="variable[qualifier]" priority="5">
      <xsl:copy>
        <xsl:apply-templates select="@*" />
        <xsl:text>
        </xsl:text>
        <xsl:apply-templates select="qualifier" />
        <xsl:text>
        </xsl:text>
        <xsl:apply-templates select="*[not(self::qualifier)]" />
      </xsl:copy>
    </xsl:template>

<!--Preserve processing instructions -->
    <xsl:template match="processing-instruction()">
      <xsl:copy>
        <xsl:apply-templates select="processing-instruction()"/>
      </xsl:copy>
       <xsl:text>
</xsl:text><!-- indent matters -->
    </xsl:template>

<!--Identity template copies content forward -->
    <xsl:template match="@*|node()">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:template>


</xsl:stylesheet>
