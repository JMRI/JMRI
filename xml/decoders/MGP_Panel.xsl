<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet	version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:db="http://docbook.org/ns/docbook"
    xmlns:xi="http://www.w3.org/2001/XInclude"
    >
<xsl:output method="xml" encoding="utf-8"/>


<!--for MGP Panel -->

<!-- * * * * *  Variables * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * INPUT * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!--
-->
<xsl:template name="InputVar">
    <xsl:param name="CV1"/>
    <xsl:param name="index"/>

    <variable item="Input {$index} Type" CV="{$CV1}" mask="VVVVVVVV" default="0">
        <enumVal>
			<enumChoice choice="none" value='0'>
                            <choice>none</choice>
                            <choice xml:lang="cs">nic</choice>
                        </enumChoice>   
			<enumChoice choice="Switch Toggle" value='1'>
                            <choice>Switch Toggle</choice>
                            <choice xml:lang="cs">Přepnutí přepínače</choice>
                        </enumChoice>   
			<enumChoice choice="Switch Thrown" value='2'>
                            <choice>Switch Thrown</choice>
                            <choice xml:lang="cs">Přepínač Do odbočky</choice>
                        </enumChoice>   
			<enumChoice choice="Switch Closed" value='3'>
                            <choice>Switch Closed</choice>
                            <choice xml:lang="cs">Přepínač Přímo</choice>
                        </enumChoice>   
			<enumChoice choice="Switch Closed/Thrown" value='4'>
                            <choice>Switch Closed/Thrown</choice>
                            <choice xml:lang="cs">Přepínač Přímo/Do odbočky</choice>
                        </enumChoice>   
			<enumChoice choice="Status Toggle" value='5'>
                            <choice>Status Toggle</choice>
                            <choice xml:lang="cs">Stav přepnuto</choice>
                        </enumChoice>   
			<enumChoice choice="Status Occupied" value='6'>
                            <choice>Status Occupied</choice>
                            <choice xml:lang="cs">Stav Obsazeno</choice>
                        </enumChoice>   
			<enumChoice choice="Status Free" value='7'>
                            <choice>Status Free</choice>
                            <choice xml:lang="cs">Stav Volno</choice>
                        </enumChoice>   
			<enumChoice choice="Status Free/Occupied" value='8'>
                            <choice>Status Free/Occupied</choice>
                            <choice xml:lang="cs">Stav Volno/Obsazeno</choice>
                        </enumChoice>   
			<enumChoice choice="Interrogate" value='9'>
                            <choice>Interrogate</choice>
                            <choice xml:lang="cs">Dotázat se</choice>
                        </enumChoice>   
			<enumChoice choice="Combo Address" value='10'>
                            <choice>Combo Address</choice>
                            <choice xml:lang="cs">Kombinace adresa</choice>
                        </enumChoice>   
			<enumChoice choice="Switch Status Toggle" value='11'>
                            <choice>Switch Status Toggle</choice>
                            <choice xml:lang="cs">Stav přepínače přepnuto</choice>
                        </enumChoice>   
			<enumChoice choice="Switch Status Thrown" value='12'>
                            <choice>Switch Status Thrown</choice>
                            <choice xml:lang="cs">Stav přepínače Do odbočky</choice>
                        </enumChoice>   
			<enumChoice choice="Switch Status Thrown" value='13'>
                            <choice>Switch Status Closed</choice>
                            <choice xml:lang="cs">Stav přepínače Přímo</choice>
                        </enumChoice>   
			<enumChoice choice="Switch Status Closed/Thrown" value='14'>
                            <choice>Switch Status Closed/Thrown</choice>
                            <choice xml:lang="cs">Stav přepínače Přímo/Do odbočky</choice>
                        </enumChoice>   
			<enumChoice choice="Send Track Status ON" value='15'>
                            <choice>Send Track Status ON</choice>
                            <choice xml:lang="cs">Odeslat Stav napájení Zapnuto</choice>
                        </enumChoice>   
			<enumChoice choice="Send Track Status OFF" value='16'>
                            <choice>Send Track Status ON</choice>
                            <choice xml:lang="cs">Odeslat Stav napájení Vypnuto</choice>
                        </enumChoice>   
        </enumVal>
    </variable>
	<variable item="Input {$index} Address" CV="{$CV1 +1}" default="0">
        <splitVal highCV="{$CV1 +2}" upperMask="XXXXVVVV"/>
    </variable>
	<variable item="Input {$index} Lock" CV="{$CV1 +2}" mask="XXXVXXXX">
		<enumVal>
            <enumChoice choice="Not in use" value="0">
                <choice>Not in use</choice>
                <choice xml:lang="cs">Nepoužito</choice>
            </enumChoice>
            <enumChoice choice="Use the general lock" value="1">
                <choice>Use the general lock</choice>
                <choice xml:lang="cs">Použit všeobecný zámek</choice>
            </enumChoice>
		</enumVal>
	</variable>
</xsl:template>

<xsl:template name="AllInputVars">
  <xsl:param name="CV1" select="30"/>
  <xsl:param name="index" select="1"/>

  <xsl:if test="16 >= $index">
    <xsl:call-template name="InputVar">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>

    <xsl:call-template name="AllInputVars">
      <xsl:with-param name="CV1" select="$CV1 +3"/>
      <xsl:with-param name="index" select="$index+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!--  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- ***** Input Pane  ********************************************************************** -->
<xsl:template name="InputInPane">
	<xsl:param name="index" select="1"/>

	<xsl:if test="16 >= $index">

		<label><text>&#160;</text></label>

		<label>
                    <text>Input <xsl:value-of select="$index"/></text>
                    <text xml:lang="cs">Vstup <xsl:value-of select="$index"/></text>
                </label>
        <display item="Input {$index} Type">
			<label>Type</label>
			<label xml:lang="cs">Typ</label>
		</display>
		<display item="Input {$index} Address">
			<label>Address</label>
			<label xml:lang="cs">Adresa</label>
		</display>
		<display item="Input {$index} Lock">
			<label>Lock</label>
			<label xml:lang="cs">Zámek</label>
		</display>

		<xsl:call-template name="InputInPane">
			<xsl:with-param name="index" select="$index+1"/>
		</xsl:call-template>
		
  </xsl:if>
</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='InputPane']">
	<pane>
	<name>Input</name>
	<name xml:lang="cs">Vstupy</name>
    <column>
		<xsl:call-template name="InputInPane">
			<xsl:with-param name="index" select="1"/>
		</xsl:call-template>
    </column>
   </pane>
</xsl:template>
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * LED   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!--
-->
<xsl:template name="LedVar">
    <xsl:param name="CV1"/>
    <xsl:param name="index"/>

    <variable item="LED {$index} Type" CV="{$CV1}" mask="VVVVVVVV" default="0">
        <enumVal>
            <enumChoice choice="none" value='0'>
                <choice>none</choice>
                <choice xml:lang="cs">nic</choice>
            </enumChoice>   
            <enumChoice choice="Switch Thrown" value='1'>
                <choice>Switch Thrown</choice>
                <choice xml:lang="cs">Výhybka Do odbočky</choice>
            </enumChoice>   
            <enumChoice choice="Switch Closed" value='2'>
                <choice>Switch Closed</choice>
                <choice xml:lang="cs">Výhybka Přímo</choice>
            </enumChoice>   
            <enumChoice choice="Status Occupied" value='3'>
                <choice>Status Occupied</choice>
                <choice xml:lang="cs">Stav Obsazeno</choice>
            </enumChoice>   
            <enumChoice choice="Status Free" value='4'>
                <choice>Status Free</choice>
                <choice xml:lang="cs">Stav Volno</choice>
            </enumChoice>   
            <enumChoice choice="Switch Known state" value='5'>
                <choice>Switch Known state</choice>
                <choice xml:lang="cs">Výhybka Známý stav</choice>
            </enumChoice>   
            <enumChoice choice="Switch Unknown state" value='6'>
                <choice>Switch Unknown state</choice>
                <choice xml:lang="cs">Výhybka Neznámý stav</choice>
            </enumChoice>   
            <enumChoice choice="On" value='9'>
                <choice>On</choice>
                <choice xml:lang="cs">Zapnuto</choice>
            </enumChoice>   
            <enumChoice choice="Signal Stop" value='10'>
                <choice>Signal Stop</choice>
                <choice xml:lang="cs">Návěst Stůj</choice>
            </enumChoice>   
            <enumChoice choice="Signal Go" value='11'>
                <choice>Signal Go</choice>
                <choice xml:lang="cs">Návěst Volno</choice>
            </enumChoice>   
            <enumChoice choice="Signal Go Slow" value='12'>
                <choice>Signal Go Slow</choice>
                <choice xml:lang="cs">Návěst Pomalu</choice>
            </enumChoice>   
            <enumChoice choice="Combo active" value='15'>
                <choice>Combo active</choice>
                <choice xml:lang="cs">Kombinované tlačítko stisknuto</choice>
            </enumChoice>   
        </enumVal>
    </variable>
    <variable item="LED {$index} Group" CV="{$CV1}" mask="VVVXXXXX" default="0">
        <decVal />
    </variable>
	<variable item="LED {$index} Address" CV="{$CV1 +1}" default="0">
        <splitVal highCV="{$CV1 +2}" upperMask="XXXXVVVV"/>
    </variable>
</xsl:template>

<xsl:template name="x64LedVars">
  <xsl:param name="CV1" select="104"/>
  <xsl:param name="index" select="1"/>

  <xsl:if test="64 >= $index">
    <xsl:call-template name="LedVar">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>

    <xsl:call-template name="x64LedVars">
      <xsl:with-param name="CV1" select="$CV1 +3"/>
      <xsl:with-param name="index" select="$index+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template name="AllLedVars">

    <xsl:call-template name="x64LedVars">
      <xsl:with-param name="CV1" select="104"/>
      <xsl:with-param name="index" select="1"/>
    </xsl:call-template>

</xsl:template>

<!--  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- ***** LED Pane  ********************************************************************** -->
<xsl:template name="LedInPane">
	<xsl:param name="index" select="1"/>

	<xsl:if test="64 >= $index">

		<label><text>&#160;</text></label>

		<label><text>LED <xsl:value-of select="$index"/></text></label>
        <display item="LED {$index} Type">
			<label>Type</label>
            <label xml:lang="cs">Typ</label>
		</display>
		<display item="LED {$index} Address">
			<label>Address</label>
            <label xml:lang="cs">Adresa</label>
		</display>
        <display item="LED {$index} Group">
            <label>Group</label>
            <label xml:lang="cs">Skupina</label>
        </display>

		<xsl:call-template name="LedInPane">
			<xsl:with-param name="index" select="$index+1"/>
		</xsl:call-template>
		
  </xsl:if>
</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='LedPane']">
	<pane>
	<name>LED</name>
    <column>
		<xsl:call-template name="LedInPane">
			<xsl:with-param name="index" select="1"/>
		</xsl:call-template>
    </column>
   </pane>
</xsl:template>
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * LED Dependency  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!--
-->
<xsl:template name="LedDepVar">
    <xsl:param name="CV1"/>
    <xsl:param name="index"/>

    <variable item="Dependency {$index} On" CV="{$CV1}" mask="XXVVVVVV" default="0">
    	<enumVal>
            <enumChoice choice="Off" value='0'>
                <choice>Off</choice>
                <choice xml:lang="cs">Vypnuto</choice>
            </enumChoice>   
   	<enumChoice choice="LED 1" value='1'/>
   	<enumChoice choice="LED 2" value='2'/>
   	<enumChoice choice="LED 3" value='3'/>
   	<enumChoice choice="LED 4" value='4'/>
   	<enumChoice choice="LED 5" value='5'/>
   	<enumChoice choice="LED 6" value='6'/>
   	<enumChoice choice="LED 7" value='7'/>
   	<enumChoice choice="LED 8" value='8'/>
   	<enumChoice choice="LED 9" value='9'/>
   	<enumChoice choice="LED 10" value='10'/>
   	<enumChoice choice="LED 11" value='11'/>
   	<enumChoice choice="LED 12" value='12'/>
   	<enumChoice choice="LED 13" value='13'/>
   	<enumChoice choice="LED 14" value='14'/>
   	<enumChoice choice="LED 15" value='15'/>
   	<enumChoice choice="LED 16" value='16'/>
   	<enumChoice choice="LED 17" value='17'/>
   	<enumChoice choice="LED 18" value='18'/>
   	<enumChoice choice="LED 19" value='19'/>
   	<enumChoice choice="LED 20" value='20'/>
   	<enumChoice choice="LED 21" value='21'/>
   	<enumChoice choice="LED 22" value='22'/>
   	<enumChoice choice="LED 23" value='23'/>
   	<enumChoice choice="LED 24" value='24'/>
   	<enumChoice choice="LED 25" value='25'/>
   	<enumChoice choice="LED 26" value='26'/>
   	<enumChoice choice="LED 27" value='27'/>
   	<enumChoice choice="LED 28" value='28'/>
   	<enumChoice choice="LED 29" value='29'/>
   	<enumChoice choice="LED 30" value='30'/>
   	<enumChoice choice="LED 31" value='31'/>
   	<enumChoice choice="LED 32" value='32'/>
   	<enumChoice choice="LED 33" value='33'/>
   	<enumChoice choice="LED 34" value='34'/>
   	<enumChoice choice="LED 35" value='35'/>
   	<enumChoice choice="LED 36" value='36'/>
   	<enumChoice choice="LED 37" value='37'/>
   	<enumChoice choice="LED 38" value='38'/>
   	<enumChoice choice="LED 39" value='39'/>
   	<enumChoice choice="LED 40" value='40'/>
   	<enumChoice choice="LED 41" value='41'/>
   	<enumChoice choice="LED 42" value='42'/>
   	<enumChoice choice="LED 43" value='43'/>
   	<enumChoice choice="LED 44" value='44'/>
   	<enumChoice choice="LED 45" value='45'/>
   	<enumChoice choice="LED 46" value='46'/>
   	<enumChoice choice="LED 47" value='47'/>
   	<enumChoice choice="LED 48" value='48'/>
   	<enumChoice choice="LED 49" value='49'/>
   	<enumChoice choice="LED 50" value='50'/>
   	<enumChoice choice="LED 51" value='51'/>
   	<enumChoice choice="LED 52" value='52'/>
   	<enumChoice choice="LED 53" value='53'/>
   	<enumChoice choice="LED 54" value='54'/>
   	<enumChoice choice="LED 55" value='55'/>
   	<enumChoice choice="LED 56" value='56'/>
   	<enumChoice choice="LED 57" value='57'/>
   	<enumChoice choice="LED 58" value='58'/>
   	<enumChoice choice="LED 59" value='59'/>
   	<enumChoice choice="LED 60" value='60'/>
   	<enumChoice choice="LED 61" value='61'/>
   	<enumChoice choice="LED 62" value='62'/>
   	<enumChoice choice="LED 63" value='63'/>
	</enumVal>
    </variable>
    <variable item="Dependency {$index} State" CV="{$CV1}" mask="XVXXXXXX" default="0">
    	<enumVal>
  	<enumChoice choice="Off" value='0'>
            <choice>Off</choice>
            <choice xml:lang="cs">Vypnuto</choice>
        </enumChoice>   
   	<enumChoice choice="On" value='1'>
            <choice>On</choice>
            <choice xml:lang="cs">Zapnuto</choice>
        </enumChoice>   
	</enumVal>
    </variable>
    <variable item="Dependency {$index} Type" CV="{$CV1}" mask="VXXXXXXX" default="0">
        <enumVal>
	<enumChoice choice="LED" value='0'>
            <choice>LED</choice>
            <choice xml:lang="cs">LED</choice>
        </enumChoice>   
	<enumChoice choice="LED Group" value='1'>
            <choice>LED Group</choice>
            <choice xml:lang="cs">Skupina LED</choice>
        </enumChoice>   
        </enumVal>
    </variable>
</xsl:template>

<xsl:template name="x64LedDepVars">
  <xsl:param name="CV1" select="296"/>
  <xsl:param name="index" select="1"/>

  <xsl:if test="64 >= $index">
    <xsl:call-template name="LedDepVar">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>

    <xsl:call-template name="x64LedDepVars">
      <xsl:with-param name="CV1" select="$CV1+1"/>
      <xsl:with-param name="index" select="$index+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template name="AllLedDepVars">

    <xsl:call-template name="x64LedDepVars">
      <xsl:with-param name="CV1" select="296"/>
      <xsl:with-param name="index" select="1"/>
    </xsl:call-template>

</xsl:template>

<!--  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- ***** LED Dependency Pane  ********************************************************************** -->
<xsl:template name="LedDepInPane">
	<xsl:param name="index" select="1"/>

	<xsl:if test="64 >= $index">

		<label><text>&#160;</text></label>

		<label><text>LED <xsl:value-of select="$index"/></text></label>
        	<display item="Dependency {$index} On">
			<label>On</label>
			<label xml:lang="cs">Na</label>
		</display>
        	<display item="Dependency {$index} State">
			<label>State</label>
			<label xml:lang="cs">Stav</label>
		</display>
        	<display item="Dependency {$index} Type">
			<label>Type</label>
			<label xml:lang="cs">Typ</label>
		</display>

		<xsl:call-template name="LedDepInPane">
			<xsl:with-param name="index" select="$index+1"/>
		</xsl:call-template>

		
  	</xsl:if>
</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='LedDepPane']">
	<pane>
	<name>LED Dependency</name>
	<name xml:lang="cs">Závislost LED</name>
    <column>
		<xsl:call-template name="LedDepInPane">
			<xsl:with-param name="index" select="1"/>
		</xsl:call-template>
    </column>
   </pane>
</xsl:template>
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * LED Intensity * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!--
-->
<xsl:template name="LedItsVar">
    <xsl:param name="CV1"/>
    <xsl:param name="index"/>

    <variable item="LED Intensity Group {$index}" CV="{$CV1}" default="100">
        <decVal min="0" max="100"/>
    </variable>
    
</xsl:template>

<xsl:template name="AllLedItsVars">
  <xsl:param name="CV1" select="90"/>
  <xsl:param name="index" select="0"/>

    <variable item="LED max intensity" CV="100">
        <decVal />
        <label>LED max intensity</label>
        <label xml:lang="cs">Maximální intenzita LED</label>
    </variable>

  <xsl:if test="7 >= $index">
    <xsl:call-template name="LedItsVar">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>

    <xsl:call-template name="AllLedItsVars">
      <xsl:with-param name="CV1" select="$CV1 +1"/>
      <xsl:with-param name="index" select="$index +1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!--  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- ***** LED Intensity Pane  *********************************************************************** -->
<xsl:template name="LedItsInPane">
    <xsl:param name="index" select="0"/>

    <xsl:if test="7 >= $index">

        <display item="LED Intensity Group {$index}">
            <label>LED Group <xsl:value-of select="$index"/></label>
            <label xml:lang="cs">LED skupina <xsl:value-of select="$index"/></label>
        </display>

        <xsl:call-template name="LedItsInPane">
            <xsl:with-param name="index" select="$index+1"/>
        </xsl:call-template>
    </xsl:if>
</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='LedItsPane']">
    <pane>
    <name>LED Intensity Group</name>
    <name xml:lang="cs">Intenzita LED skupiny</name>
    
    <column>
    
        <label><text>&#160;</text></label>
        <display item="LED max intensity"></display>
        <label><text>&#160;</text></label>
        
        <xsl:call-template name="LedItsInPane">
            <xsl:with-param name="index" select="0"/>
        </xsl:call-template>
    </column>
    </pane>
</xsl:template>
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * LED Groups  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!--
-->
<xsl:template name="LedGrpVar">
    <xsl:param name="CV1"/>
    <xsl:param name="index"/>

    <variable item="LED 1 Group {$index}" CV="{$CV1}"  mask="XXVVVVVV" default="0">
        <decVal />
    </variable>
    <variable item="LED 2 Group {$index}" CV="{$CV1}"  mask="VVXXXXXX" default="0">
        <splitVal highCV="{$CV1 +1}" upperMask="XXXXVVVV"/>
    </variable>
    <variable item="LED 3 Group {$index}" CV="{$CV1 +1}"  mask="VVVVXXXX" default="0">
        <splitVal highCV="{$CV1 +2}" upperMask="XXXXXXVV"/>
    </variable>
    <variable item="LED 4 Group {$index}" CV="{$CV1 +2}"  mask="VVVVVVXX" default="0">
        <decVal />
    </variable>
    
</xsl:template>

<xsl:template name="AllLedGrpVars">
    <xsl:param name="CV1" select="564"/>
    <xsl:param name="index" select="1"/>

    <xsl:if test="12 >= $index">
        <xsl:call-template name="LedGrpVar">
            <xsl:with-param name="CV1" select="$CV1"/>
            <xsl:with-param name="index" select="$index"/>
        </xsl:call-template>

        <xsl:call-template name="AllLedGrpVars">
            <xsl:with-param name="CV1" select="$CV1 +3"/>
            <xsl:with-param name="index" select="$index +1"/>
        </xsl:call-template>
    </xsl:if>
</xsl:template>

<!--  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- ***** LED Groups Pane  ************************************************************************** -->
<xsl:template name="LedGrpInPane">
    <xsl:param name="index" select="1"/>

    <xsl:if test="12 >= $index">

        <label><text>&#160;</text></label>

        <label>
            <text>LED Group <xsl:value-of select="$index"/></text>
            <text xml:lang="cs">Skupina LED <xsl:value-of select="$index"/></text>
        </label>

        <display item="LED 1 Group {$index}">
            <label>LED 1st</label>
            <label xml:lang="cs">1. LED</label>
        </display>
        <display item="LED 2 Group {$index}">
            <label>LED 2nd</label>
            <label xml:lang="cs">2. LED</label>
        </display>
        <display item="LED 3 Group {$index}">
            <label>LED 3rd</label>
            <label xml:lang="cs">3. LED</label>
        </display>
        <display item="LED 4 Group {$index}">
            <label>LED 4th</label>
            <label xml:lang="cs">4. LED</label>
        </display>

        <xsl:call-template name="LedGrpInPane">
            <xsl:with-param name="index" select="$index+1"/>
        </xsl:call-template>
    </xsl:if>
</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='LedGrpPane']">
    <pane>
    <name>LED Groups</name>
    <name xml:lang="cs">Skupiny LED</name>
    
    <column>
    
        <xsl:call-template name="LedGrpInPane">
            <xsl:with-param name="index" select="1"/>
        </xsl:call-template>
    </column>
    </pane>
</xsl:template>
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Direction   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!--
-->
<xsl:template name="DirVar">
    <xsl:param name="CV1"/>
    <xsl:param name="index"/>

    <variable item="Direction {$index} Address A" CV="{$CV1}" default="0">
        <splitVal highCV="{$CV1 +1}" upperMask="XXXXVVVV"/>
    </variable>
    <variable item="Direction {$index} Address B" CV="{$CV1 +2}" default="0">
        <splitVal highCV="{$CV1 +3}" upperMask="XXXXVVVV"/>
    </variable>
</xsl:template>

<xsl:template name="x8DirVars">
  <xsl:param name="CV1" select="363"/>
  <xsl:param name="index" select="1"/>

  <xsl:if test="8 >= $index">
    <xsl:call-template name="DirVar">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>

    <xsl:call-template name="x8DirVars">
      <xsl:with-param name="CV1" select="$CV1 +4"/>
      <xsl:with-param name="index" select="$index +1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template name="AllDirVars">

    <variable item="Direction Start Address" CV="360" default="0">
        <splitVal highCV="361" upperMask="XXXXVVVV"/>
    </variable>
    <variable item="Direction Delay Free" CV="362" default="0">
        <decVal />
    </variable>
    
    <xsl:call-template name="x8DirVars">
      <xsl:with-param name="CV1" select="363"/>
      <xsl:with-param name="index" select="1"/>
    </xsl:call-template>

</xsl:template>

<!--  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- ***** Direction Pane  ********************************************************************** -->
<xsl:template name="DirInPane">
    <xsl:param name="index" select="1"/>

        <xsl:if test="8 >= $index">

            <label><text>&#160;</text></label>

            <label>
                <text>Direction Dependency <xsl:value-of select="$index"/></text>
                <text xml:lang="cs">Směrová závislost <xsl:value-of select="$index"/></text>
            </label>
            
            <display item="Direction {$index} Address A">
                <label>Address A</label>
                <label xml:lang="cs">Adresa A</label>
            </display>
            <display item="Direction {$index} Address B">
                <label>Address B</label>
                <label xml:lang="cs">Adresa B</label>
            </display>

            <xsl:call-template name="DirInPane">
                <xsl:with-param name="index" select="$index+1"/>
            </xsl:call-template>
		
        </xsl:if>
</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='DirPane']">
	<pane>
	<name>Direction Dependency</name>
	<name xml:lang="cs">Směrová závislost</name>
    <column>
        <label><text>&#160;</text></label>
        <display item="Direction Start Address">
            <label>Start Address</label>
            <label xml:lang="cs">Počáteční adresa</label>
        </display>
        <display item="Direction Delay Free">
            <label>Delay Free (*0.1 sec)</label>
            <label xml:lang="cs">Zpoždění na neobsazeno (×0,1 s)</label>
        </display>
        <label><text>&#160;</text></label>
            
        <xsl:call-template name="DirInPane">
            <xsl:with-param name="index" select="1"/>
        </xsl:call-template>
    </column>
   </pane>
</xsl:template>

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Routes variable * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<xsl:template name="RouteSwVar">
    <xsl:param name="CV1"/>
    <xsl:param name="routeIndex"/>
    <xsl:param name="swIndex"/>

	<xsl:if test="6 >= $swIndex">
	
		<variable CV="{$CV1}" item="Route {$routeIndex}, Switch {$swIndex}, Address" default="0">
		<splitVal highCV="{$CV1+1}" upperMask="XXXXXVVV"/>
		</variable>	  
		<variable item="Route {$routeIndex}, Switch {$swIndex}, Direction" mask="XXXVXXXX" CV="{$CV1+1}">
			<enumVal>
        			<enumChoice choice="Thrown" value="0">
                                    <choice>Thrown</choice>
                                    <choice xml:lang="cs">Do odbočky</choice>
                                </enumChoice>   
                		<enumChoice choice="Closed" value="1">
                                    <choice>Closed</choice>
                                    <choice xml:lang="cs">Přímo</choice>
                                </enumChoice>   
			</enumVal>
		</variable>

		<xsl:call-template name="RouteSwVar">
			<xsl:with-param name="CV1" select="$CV1 +2"/>
			<xsl:with-param name="routeIndex" select="$routeIndex"/>
			<xsl:with-param name="swIndex" select="$swIndex+1"/>
		</xsl:call-template>

	</xsl:if>
</xsl:template>

<xsl:template name="x8RouteVars">
	<xsl:param name="CV1" select="402"/>
    <xsl:param name="routeIndex" select="1"/>

	<xsl:if test="12 >= $routeIndex">
  
		<variable CV="{$CV1}" item="Route {$routeIndex}, Active" mask="XXXXXXXV" default="0">
			<enumVal>
                <enumChoice choice="Not used" value="0">
                    <choice>Not used</choice>
                    <choice xml:lang="cs">Nepoužito</choice>
                </enumChoice>   
                <enumChoice choice="Used as Switch Route" value="1">
                    <choice>Used as Switch Route</choice>
                    <choice xml:lang="cs">Použito jako Přepínač trasy</choice>
                </enumChoice>   
                <enumChoice choice="Used as Occupancy Group" value="2">
                    <choice>Used as Occupancy Group</choice>
                    <choice xml:lang="cs">Použito jako Skupina obsazení</choice>
                </enumChoice>   
                <enumChoice choice="Used as Switch Group" value="3">
                    <choice>Used as Switch Group</choice>
                    <choice xml:lang="cs">Použito jako Skupina výhybek</choice>
                </enumChoice>   
			</enumVal>
		</variable>	  

		<xsl:call-template name="RouteSwVar">
			<xsl:with-param name="CV1" select="$CV1+1"/>
			<xsl:with-param name="routeIndex" select="$routeIndex"/>
			<xsl:with-param name="swIndex" select="1"/>
		</xsl:call-template>

		<xsl:call-template name="x8RouteVars">
			<xsl:with-param name="CV1" select="$CV1+13"/>
			<xsl:with-param name="routeIndex" select="$routeIndex+1"/>
		</xsl:call-template>

	</xsl:if>
</xsl:template>

<xsl:template name="AllRouteVars">
	<xsl:param name="CV1" select="400"/>
 
	<variable CV="{$CV1}" item="Route Start Address" default="0">
		<splitVal highCV="{$CV1+1}" upperMask="XXXXXVVV"/>
                <label>Route Start Address</label>
                <label xml:lang="cs">Počáteční adresa trasy</label>
	</variable>	  
            <variable item="Delay in Routes" CV="{$CV1+1}" mask="VVVVXXXX" default="0">
                <decVal />
                <label>Delay in Routes (0-15 msec)</label>
                <label xml:lang="cs">Zpoždění v trasách (0–15 ms)</label>
            </variable>

	<xsl:call-template name="x8RouteVars">
		<xsl:with-param name="CV1" select="402"/>
		<xsl:with-param name="routeIndex" select="1"/>
	</xsl:call-template>

</xsl:template>

<!--  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- ***** Routes Pane  ********************************************************************** -->
<xsl:template name="RoutesSwInPane">
    <xsl:param name="routeIndex"/>
    <xsl:param name="swIndex"/>

	<xsl:if test="6 >= $swIndex">
	
        <display item="Route {$routeIndex}, Switch {$swIndex}, Address">
			<label>Switch <xsl:value-of select="$swIndex"/>, Address  </label>
			<label xml:lang="cs">Přepínač <xsl:value-of select="$swIndex"/>, Adresa  </label>
		</display>
        <display item="Route {$routeIndex}, Switch {$swIndex}, Direction">
			<label>Switch <xsl:value-of select="$swIndex"/>, Direction</label>
			<label xml:lang="cs">Přepínač <xsl:value-of select="$swIndex"/>, Směr  </label>
		</display>

		<xsl:call-template name="RoutesSwInPane">
			<xsl:with-param name="routeIndex" select="$routeIndex"/>
			<xsl:with-param name="swIndex" select="$swIndex+1"/>
		</xsl:call-template>

	</xsl:if>
</xsl:template>

<xsl:template name="RoutesInPane">
    <xsl:param name="routeIndex" select="1"/>

	<xsl:if test="12 >= $routeIndex">
  
		<label><text>&#160;</text></label>
		<label>
                    <text>Route <xsl:value-of select="$routeIndex"/></text>
                    <text xml:lang="cs">Trasa <xsl:value-of select="$routeIndex"/></text>
                </label>
        <display item="Route {$routeIndex}, Active">
			<label>Route <xsl:value-of select="$routeIndex"/> Active</label>
			<label xml:lang="cs">Trasa <xsl:value-of select="$routeIndex"/> Aktivní</label>
		</display>

		<xsl:call-template name="RoutesSwInPane">
			<xsl:with-param name="routeIndex" select="$routeIndex"/>
			<xsl:with-param name="swIndex" select="1"/>
		</xsl:call-template>

		<xsl:call-template name="RoutesInPane">
			<xsl:with-param name="routeIndex" select="$routeIndex+1"/>
		</xsl:call-template>

	</xsl:if>
</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='RoutesPane']">
	<pane>
	<name>Routes</name>
	<name xml:lang="cs">Trasy</name>
    <column>
		<label><text>&#160;</text></label>
        <display item="Route Start Address">
		</display>
		<label><text>&#160;</text></label>

		<xsl:call-template name="RoutesInPane">
		  <xsl:with-param name="routeIndex" select="1"/>
		</xsl:call-template>
    </column>
   </pane>
</xsl:template>
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Button Combos * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!--
-->
<xsl:template name="ButtonComboVar">
    <xsl:param name="CV1"/>
    <xsl:param name="index"/>

    <variable item="Combo {$index} Active" CV="{$CV1}" mask="XXXXXXXV" default="0">
        <enumVal>
		<enumChoice choice="No" value="0">
                    <choice>No</choice>
                    <choice xml:lang="cs">Ne</choice>
                </enumChoice>   
		<enumChoice choice="Yes" value="1">
                    <choice>Yes</choice>
                    <choice xml:lang="cs">Ano</choice>
                </enumChoice>   
        </enumVal>
    </variable>
    <variable item="Combo {$index} Button or Address" CV="{$CV1}" mask="XXXXXXVX" default="0">
        <enumVal>
            <enumChoice choice="Button" value='0'>
                <choice>Button number</choice>
                <choice xml:lang="cs">Číslo tlačítka</choice>
            </enumChoice>   
            <enumChoice choice="Address" value='1'>
                <choice>External Address</choice>
                <choice xml:lang="cs">Externí adresa</choice>
            </enumChoice>   
        </enumVal>
    </variable>
	<variable item="Combo {$index} Button A" CV="{$CV1 +1}"  mask="VVVVVVVV" default="0">
        <splitVal highCV="{$CV1 +2}" upperMask="XXXXVVVV"/>
    </variable>	
	<variable item="Combo {$index} Button B" CV="{$CV1 +3}"  mask="VVVVVVVV" default="0">
        <splitVal highCV="{$CV1 +4}" upperMask="XXXXVVVV"/>
    </variable>		
	
    <variable item="Combo {$index} Order Type 1" CV="{$CV1+5}" mask="XXXXVVVV" default="0">
        <enumVal>
			<enumChoice choice="Not used" value='0'>
                            <choice>Not used</choice>
                            <choice xml:lang="cs">Nepoužito</choice>
                        </enumChoice>   
			<enumChoice choice="Switch Thrown" value='1'>
                            <choice>Switch Thrown</choice>
                            <choice xml:lang="cs">Výhybka Do odbočky</choice>
                        </enumChoice>   
			<enumChoice choice="Switch Closed" value='2'>
                            <choice>Switch Closed</choice>
                            <choice xml:lang="cs">Výhybka Přímo</choice>
                        </enumChoice>   
			<enumChoice choice="Status Free" value='3'>
                            <choice>Status Free</choice>
                            <choice xml:lang="cs">Stav Volno</choice>
                        </enumChoice>   
			<enumChoice choice="Status Occupied" value='4'>
                            <choice>Status Occupied</choice>
                            <choice xml:lang="cs">Stav Obsazeno</choice>
                        </enumChoice>   
        </enumVal>
    </variable>
	
 	<variable item="Combo {$index} Order Address 1" CV="{$CV1 +5}" mask="VVVVXXXX" default="0">
        <splitVal highCV="{$CV1 +6}" upperMask="VVVVVVVV"/>
    </variable>
	
<!-- New way to use splitval
 	<variable item="Combo {$index} Order Address 1" CV="{$CV1 +5}, {$CV1 +6}" mask="VVVVXXXX VVVVVVVV" default="0">
        <splitVal/>
    </variable>  -->
	
    <variable item="Combo {$index} Order Type 2" CV="{$CV1+7}" mask="XXXXVVVV" default="0">
        <enumVal>
			<enumChoice choice="Not used" value='0'>
                            <choice>Not used</choice>
                            <choice xml:lang="cs">Nepoužito</choice>
                        </enumChoice>   
			<enumChoice choice="Switch Thrown" value='1'>
                            <choice>Switch Thrown</choice>
                            <choice xml:lang="cs">Výhybka Do odbočky</choice>
                        </enumChoice>   
			<enumChoice choice="Switch Closed" value='2'>
                            <choice>Switch Closed</choice>
                            <choice xml:lang="cs">Výhybka Přímo</choice>
                        </enumChoice>   
			<enumChoice choice="Status Free" value='3'>
                            <choice>Status Free</choice>
                            <choice xml:lang="cs">Stav Volno</choice>
                        </enumChoice>   
			<enumChoice choice="Status Occupied" value='4'>
                            <choice>Status Occupied</choice>
                            <choice xml:lang="cs">Stav Obsazeno</choice>
                        </enumChoice>   
        </enumVal>
    </variable>
	<variable item="Combo {$index} Order Address 2" CV="{$CV1 +7}" mask="VVVVXXXX" default="0">
        <splitVal highCV="{$CV1 +8}" upperMask="VVVVVVVV"/>
    </variable>
	
</xsl:template>

<xsl:template name="x16ButtonComboVars">
  <xsl:param name="CV1" select="604"/>
  <xsl:param name="index" select="1"/>

  <xsl:if test="24 >= $index">
    <xsl:call-template name="ButtonComboVar">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>

    <xsl:call-template name="x16ButtonComboVars">
      <xsl:with-param name="CV1" select="$CV1 +9"/>
      <xsl:with-param name="index" select="$index+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template name="AllButtonComboVars">
	<xsl:param name="CV1" select="600"/>

	<variable item="Direction dependant" CV="{$CV1}" mask="XXXXXXXV" default="0">
		<enumVal>
                <enumChoice choice="No" value="0">
                    <choice>No</choice>
                    <choice xml:lang="cs">Ne</choice>
                </enumChoice>   
                <enumChoice choice="Yes" value="1">
                    <choice>Yes</choice>
                    <choice xml:lang="cs">Ano</choice>
                </enumChoice>   
		</enumVal>
            <label>Direction dependant</label>
            <label xml:lang="cs">Směrová závislost</label>
	</variable>
	<variable item="Max time between (0-15s)" CV="{$CV1}" mask="XXXVVVVX" default="0">
		<decVal />
            <label>Max time between (0-15s)</label>
            <label xml:lang="cs">Maximální čas mezi (0–15 s)</label>
	</variable>
	<xsl:call-template name="x16ButtonComboVars">
		<xsl:with-param name="CV1" select="604"/>
		<xsl:with-param name="index" select="1"/>
	</xsl:call-template>

</xsl:template>

<!--  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- ***** Button Combo Pane  ********************************************************************** -->
<xsl:template name="ComboInPane">
	<xsl:param name="index" select="1"/>

	<xsl:if test="24 >= $index">

	<label><text>&#160;</text></label>

	<label>
            <text>Combo <xsl:value-of select="$index"/></text>
            <text xml:lang="cs">Kombinace <xsl:value-of select="$index"/></text>
        </label>
        <display item="Combo {$index} Active">
            <label>Active</label>
            <label xml:lang="cs">Aktivní</label>
	</display>
        <display item="Combo {$index} Button or Address">
			<label>Button or Address</label>
            <label xml:lang="cs">Tlačítko nebo adresa</label>
		</display>
		<display item="Combo {$index} Button A">
			<label>Button A</label>
            <label xml:lang="cs">Tlačítko nebo adresa A</label>
		</display>
		<display item="Combo {$index} Button B">
			<label>Button B</label>
            <label xml:lang="cs">Tlačítko nebo adresa B</label>
		</display>
		<display item="Combo {$index} Order Address 1">
		<label>Order 1 Address</label>
            <label xml:lang="cs">Příkaz 1 Adresa</label>
		</display>
		<display item="Combo {$index} Order Type 1">
			<label>Order 1 Type</label>
            <label xml:lang="cs">Příkaz 1 Typ</label>
		</display>
				<display item="Combo {$index} Order Address 2">
			<label>Order 2 Address</label>
            <label xml:lang="cs">Příkaz 2 Adresa</label>
		</display>
		<display item="Combo {$index} Order Type 2">
			<label>Order 2 Type</label>
            <label xml:lang="cs">Příkaz 2 Typ</label>
		</display>

		<xsl:call-template name="ComboInPane">
			<xsl:with-param name="index" select="$index+1"/>
		</xsl:call-template>
		
  </xsl:if>
</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='ComboPane']">
	<pane>
	<name>Combination Buttons</name>
	<name xml:lang="cs">Kombinace tlačítek</name>
    <column>

		<label><text>&#160;</text></label>
		<display item="Direction dependant">
		</display>
		<display item="Max time between (0-15s)">
		</display>

		<xsl:call-template name="ComboInPane">
			<xsl:with-param name="index" select="1"/>
		</xsl:call-template>

		<label><text>&#160;</text></label>

    </column>
   </pane>
</xsl:template>
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->



<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Run All   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- - - - MATCH - - - -->
<!-- install new variables at end of variables element-->
 <xsl:template match="variables">
   <variables>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="AllInputVars"/>
     <xsl:call-template name="AllLedVars"/>
     <xsl:call-template name="AllLedDepVars"/>
     <xsl:call-template name="AllLedItsVars"/>
     <xsl:call-template name="AllLedGrpVars"/>
     <xsl:call-template name="AllDirVars"/>
     <xsl:call-template name="AllRouteVars"/>
	 <xsl:call-template name="AllButtonComboVars"/>
   </variables>
 </xsl:template>


<!--Identity template copies content forward -->
<!-- - - - MATCH - - - -->
 <xsl:template match="@*|node()">
   <xsl:copy>
     <xsl:apply-templates select="@*|node()"/>
   </xsl:copy>
 </xsl:template>
 
</xsl:stylesheet>
