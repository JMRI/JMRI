<?xml version="1.0" encoding="UTF-8"?>
<!--
  **************************************************************
  ** Antdoc v1.0
  **
  ** Written by Eric Burke (burke_e@ociweb.com)
  **
  ** Uses XSLT to generate HTML summary reports of Ant build
  ** files.
  ***********************************************************-->
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" 
      doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" 
      doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" 
      indent="yes" encoding="UTF-8"/>

  <!-- global variable: the project name -->
  <xsl:variable name="projectName" select="/project/@name"/>
  <xsl:template match="/">
    <html xmlns="http://www.w3.org/1999/xhtml" >
      <head>
        <title>Ant Project Summary - 
            <xsl:value-of select="$projectName"/></title>
      </head>
      <body>
        <h1>Ant Project Summary</h1>
        <xsl:apply-templates select="project"/>
      </body>
    </html>
  </xsl:template>

  <!--
  ***************************************************************
  ** "project" template
  ************************************************************-->
  <xsl:template match="project">
    <!-- show the project summary table, listing basic info
         such as name, default target, and base directory -->
    <table border="1" cellpadding="4" cellspacing="0">
      <tr><th colspan="2"><h3>Project Summary</h3></th></tr>
      <tr>
        <td>Project Name:</td>
        <td><xsl:value-of select="$projectName"/></td>
      </tr>
      <tr>
        <td>Default Target:</td>
        <td><xsl:value-of select="@default"/></td>
      </tr>
      <tr>
        <td>Base Directory:</td>
        <td><xsl:value-of select="@basedir"/></td>
      </tr>
    </table>

     <p/>
     
    <!-- Show a table of all targets -->
    <table border="1" cellpadding="4" cellspacing="0">
      <tr><th colspan="3"><h3>List of Targets</h3></th></tr>
      <tr>
        <th>Name</th>
        <th>Dependencies</th>
        <th>Description</th>
      </tr>
      <xsl:apply-templates select="target" mode="tableRow">
        <xsl:sort select="count(@description)" order="descending"/>
        <xsl:sort select="@name"/>
      </xsl:apply-templates>
    </table>
    <p/>

    <p/>
    <xsl:call-template name="globalProperties"/>

    <!-- show all target dependencies as a tree -->
    <h3>Target Dependency Tree</h3>
    <xsl:apply-templates select="target[not(@depends)]" mode="tree">
      <xsl:sort select="@name"/>
    </xsl:apply-templates>
    <p/>

  </xsl:template>

  <!--
  ***************************************************************
  ** Create a table of all global properties.
  ************************************************************-->
  <xsl:template name="globalProperties">
    <xsl:if test="property">
      <table border="1" cellpadding="4" cellspacing="0">
        <tr><th colspan="2"><h3>Global Properties</h3></th></tr>
        <tr>
          <th>Name</th>
          <th>Value</th>
        </tr>
        <xsl:apply-templates select="property" mode="tableRow">
          <xsl:sort select="@name"/>
        </xsl:apply-templates>
      </table>
    </xsl:if>
  </xsl:template>

  <!--
  ***************************************************************
  ** Show an individual property in a table row.
  ************************************************************-->
  <xsl:template match="property[@name]" mode="tableRow">
    <tr>
      <td><xsl:value-of select="@name"/></td>
      <td>
        <xsl:choose>
          <xsl:when test="not(@value)">
            <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@value"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
    </tr>
  </xsl:template>

  <!--
  ***************************************************************
  ** "target" template, mode=tableRow
  ** Print a target name and its list of dependencies in a 
  ** table row.
  ************************************************************-->
  <xsl:template match="target" mode="tableRow">
    <tr valign="top">
      <td><xsl:value-of select="@name"/></td>
      <td>
        <xsl:choose>
          <xsl:when test="@depends">
            <xsl:call-template name="parseDepends">
              <xsl:with-param name="depends" select="@depends"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>-</xsl:otherwise>
        </xsl:choose>
      </td>
      <td>
        <xsl:if test="@description">
          <xsl:value-of select="@description"/>
        </xsl:if>
        <xsl:if test="not(@description)">
          <xsl:text>-</xsl:text>
        </xsl:if>
      </td>
    </tr>
  </xsl:template>

  <!--
  ***************************************************************
  ** "parseDepends" template
  ** Tokenizes and prints a comma separated list of dependencies.
  ** The first token is printed, and the remaining tokens are
  ** recursively passed to this template.
  ************************************************************-->
  <xsl:template name="parseDepends">
    <!-- this parameter contains the list of dependencies -->
    <xsl:param name="depends"/>

    <!-- grab everything before the first comma,
         or the entire string if there are no commas -->
    <xsl:variable name="firstToken">
      <xsl:choose>
        <xsl:when test="contains($depends, ',')">
          <xsl:value-of 
            select="normalize-space(substring-before($depends, ','))"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="normalize-space($depends)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="remainingTokens" 
      select="normalize-space(substring-after($depends, ','))"/>

    <!-- output the first dependency -->
    <xsl:value-of select="$firstToken"/>

    <!-- recursively invoke this template with the remainder
         of the comma separated list -->
    <xsl:if test="$remainingTokens">
      <xsl:text>, </xsl:text>
      <xsl:call-template name="parseDepends">
        <xsl:with-param name="depends" select="$remainingTokens"/>
      </xsl:call-template>
    </xsl:if>

  </xsl:template>

  <!--
  ***************************************************************
  ** This template will begin a recursive process that forms a
  ** dependency graph of all targets.
  ************************************************************-->
  <xsl:template match="target" mode="tree">
    <xsl:param name="indentLevel" select="'0'"/>
    <xsl:variable name="curName" select="@name"/>
    <div style="text-indent: {$indentLevel}em;">
      <xsl:value-of select="$curName"/>

      <!-- if the 'depends' attribute is present, show the
           list of dependencies -->
      <xsl:if test="@depends">
        <xsl:text> (depends on </xsl:text>
        <xsl:call-template name="parseDepends">
          <xsl:with-param name="depends" select="@depends"/>
        </xsl:call-template>
        <xsl:text>)</xsl:text>
      </xsl:if>
    </div>

    <!-- set up the indentation -->
    <xsl:variable name="nextLevel" select="$indentLevel+1"/>

    <!-- search all other <target> elements that have "depends"
         attributes -->
    <xsl:for-each select="../target[@depends]">

      <!-- Take the comma-separated list of dependencies and
           "clean it up". See the comments for the "fixDependency"
           template -->
      <xsl:variable name="correctedDependency">
        <xsl:call-template name="fixDependency">
          <xsl:with-param name="depends" select="@depends"/>
        </xsl:call-template>
      </xsl:variable>

      <!-- Now the dependency list is pipe (|) delimited, making
           it easier to reliably search for substrings. Recursively
           instantiate this template for all targets that depend
           on the current target -->
      <xsl:if test="contains($correctedDependency,concat('|',$curName,'|'))">
        <xsl:apply-templates select="." mode="tree">
          <xsl:with-param name="indentLevel" select="$nextLevel"/>
        </xsl:apply-templates>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <!--
  ***************************************************************
  ** This template takes a comma-separated list of dependencies
  ** and converts all commas to pipe (|) characters. It also
  ** removes all spaces. For instance:
  **
  ** Input: depends="a, b,c "
  ** Ouput: |a|b|c|
  **
  ** The resulting text is much easier to parse with XSLT.
  ************************************************************-->
  <xsl:template name="fixDependency">
    <xsl:param name="depends"/>

    <!-- grab everything before the first comma,
         or the entire string if there are no commas -->
    <xsl:variable name="firstToken">
      <xsl:choose>
        <xsl:when test="contains($depends, ',')">
          <xsl:value-of 
            select="normalize-space(substring-before($depends, ','))"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="normalize-space($depends)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- define a variable that contains everything after the
         first comma -->
    <xsl:variable name="remainingTokens" 
      select="normalize-space(substring-after($depends, ','))"/>

    <xsl:text>|</xsl:text>
    <xsl:value-of select="$firstToken"/>
    <xsl:choose>
      <xsl:when test="$remainingTokens">
        <xsl:call-template name="fixDependency">
          <xsl:with-param name="depends" select="$remainingTokens"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>|</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
