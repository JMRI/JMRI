<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet   version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:db="http://docbook.org/ns/docbook"
    xmlns:xi="http://www.w3.org/2001/XInclude"
    >
<xsl:output method="xml" encoding="utf-8"/>

<!-- for Digitrax_0SFX_Series8 -->

<!--  Variables ............................................................................. -->
<!--                  ............................................................................ -->
<xsl:template name="OneFunctionGroup">
    <xsl:param name="CVindex"/>
    <xsl:param name="index"/>

    <variable item="XF{$index} Enable" CV="{$CVindex}.230" mask="XXXXXXXV" default="1">
        <enumVal>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
        </enumVal>
        <label>      Enable:</label>
    </variable>

    <variable item="XF{$index} Invert" CV="{$CVindex}.230" mask="XVXXXXX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>      Invert:</label>
    </variable>

    <variable item="XF{$index} Control" CV="{$CVindex}.230" mask="XXXXVVX" default="0">
        <enumVal>
          <enumChoice choice="OR Merge"><choice>OR Merge</choice></enumChoice>
          <enumChoice choice="AND"><choice>AND</choice></enumChoice>
          <enumChoice choice="Lock"><choice>Lock</choice></enumChoice>
        </enumVal>
        <label>      Control:</label>
    </variable>

    <variable item="XF{$index} Op0-wht" CV="{$CVindex}.231" mask="XXXXXXXV" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op0-wht</label>
    </variable>

    <variable item="XF{$index} Op1-yel" CV="{$CVindex}.231" mask="XXXXXXVX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op1-yel</label>
    </variable>

    <variable item="XF{$index} Op2-grn" CV="{$CVindex}.231" mask="XXXXXVXX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op2-grn</label>
    </variable>

    <variable item="XF{$index} Op3-viol" CV="{$CVindex}.231" mask="XXXXVXXX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op3-viol</label>
    </variable>

    <variable item="XF{$index} Op4" CV="{$CVindex}.231" mask="XXXVXXXX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op4</label>
    </variable>

    <variable item="XF{$index} Op5" CV="{$CVindex}.231" mask="XXVXXXXX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
         </enumVal>
        <label>Op5</label>
   </variable>

    <variable item="XF{$index} Op6" CV="{$CVindex}.231" mask="XVXXXXXX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op6</label>
    </variable>

    <variable item="XF{$index} Op7" CV="{$CVindex}.231" mask="VXXXXXXX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op7</label>
    </variable>

    <variable item="XF{$index} Op8" CV="{$CVindex}.232" mask="XXXXXXXV" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op8</label>
    </variable>

    <variable item="XF{$index} Op9" CV="{$CVindex}.232" mask="XXXXXXVX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op9</label>
    </variable>

    <variable item="XF{$index} Op10" CV="{$CVindex}.232" mask="XXXXXVXX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op10</label>
    </variable>

    <variable item="XF{$index} Op11" CV="{$CVindex}.232" mask="XXXXVXXX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op11</label>
    </variable>

    <variable item="XF{$index} Op12" CV="{$CVindex}.232" mask="XXXVXXXX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op12</label>
    </variable>

    <variable item="XF{$index} Op13" CV="{$CVindex}.232" mask="XXVXXXXX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op13</label>
    </variable>

    <variable item="XF{$index} Op14" CV="{$CVindex}.232" mask="XVXXXXXX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op14</label>
    </variable>

    <variable item="XF{$index} Op15" CV="{$CVindex}.232" mask="VXXXXXXX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op15</label>
    </variable>

    <variable item="XF{$index} Op16" CV="{$CVindex}.232" mask="VXXXXXXX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op16</label>
    </variable>

    <variable item="XF{$index} Op17" CV="{$CVindex}.232" mask="VXXXXXXX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op17</label>
    </variable>

    <variable item="XF{$index} Op18" CV="{$CVindex}.232" mask="VXXXXXXX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op18</label>
    </variable>

    <variable item="XF{$index} Op19" CV="{$CVindex}.232" mask="VXXXXXXX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op19</label>
    </variable>

    <variable item="XF{$index} Op20" CV="{$CVindex}.232" mask="VXXXXXXX" default="0">
        <enumVal>
          <enumChoice choice="No"><choice>No</choice></enumChoice>
          <enumChoice choice="Yes"><choice>Yes</choice></enumChoice>
        </enumVal>
        <label>Op20</label>
    </variable>

    <variable item="XF{$index} Snd" CV="{$CVindex}.236" mask="XXVVVVVV" default="0">
        <enumVal>
          <enumChoice choice="0"/>
          <enumChoice choice="1"/>
          <enumChoice choice="2"/>
          <enumChoice choice="3"/>
          <enumChoice choice="4"/>
          <enumChoice choice="5"/>
          <enumChoice choice="6"/>
          <enumChoice choice="7"/>
          <enumChoice choice="8"/>
          <enumChoice choice="9"/>
          <enumChoice choice="10"/>
          <enumChoice choice="11"/>
          <enumChoice choice="12"/>
          <enumChoice choice="13"/>
          <enumChoice choice="14"/>
          <enumChoice choice="15"/>
          <enumChoice choice="16"/>
          <enumChoice choice="17"/>
          <enumChoice choice="18"/>
          <enumChoice choice="19"/>
          <enumChoice choice="20"/>
          <enumChoice choice="21"/>
          <enumChoice choice="22"/>
          <enumChoice choice="23"/>
          <enumChoice choice="24"/>
          <enumChoice choice="25"/>
          <enumChoice choice="26"/>
          <enumChoice choice="27"/>
          <enumChoice choice="28"/>
        </enumVal>
        <label>      Remap Sound:</label>
    </variable>
    
    <variable item="XF{$index} Effect" CV="{$CVindex}.234" mask="XXXXVVVV" default="0">
        <enumVal xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://jmri.org/xml/schema/decoder.xsd">
          <enumChoice choice="Normal function of lead">
            <choice>Normal function of lead</choice>
            <choice xml:lang="it">Funzione Normale</choice>
          </enumChoice>
          <enumChoice choice="Random flicker">
            <choice>Random flicker</choice>
            <choice xml:lang="it">Lampeggio Casuale</choice>
          </enumChoice>
          <enumChoice choice="Mars light">
            <choice>Mars light</choice>
            <choice xml:lang="it">Mars light</choice>
          </enumChoice>
          <enumChoice choice="Flashing head light">
            <choice>Flashing head light</choice>
            <choice xml:lang="it">Luce frontale Lampeggiante</choice>
          </enumChoice>
          <enumChoice choice="Single pulse strobe">
            <choice>Single pulse strobe</choice>
            <choice xml:lang="it">Strobe Singolo Impulso</choice>
          </enumChoice>
          <enumChoice choice="Double pulse strobe">
            <choice>Double pulse strobe</choice>
            <choice xml:lang="it">Strobe Doppio Impulso</choice>
          </enumChoice>
          <enumChoice choice="Rotary beacon simulation">
            <choice>Rotary beacon simulation</choice>
            <choice xml:lang="it">Simulazione Faro Rotante</choice>
          </enumChoice>
          <enumChoice choice="Gyralite">
            <choice>Gyralite</choice>
            <choice xml:lang="it">Gyralite</choice>
          </enumChoice>
          <enumChoice choice="Rule 17 dimmable headlight">
            <choice>Rule 17 dimmable headlight</choice>
            <choice xml:lang="it">Luce Frontale regolabile a Norma 17</choice>
          </enumChoice>
          <enumChoice choice="FRED end of train light">
            <choice>FRED end of train light</choice>
            <choice xml:lang="it">FRED Luce fine treno</choice>
          </enumChoice>
          <enumChoice choice="Right ditch light; when F2 on, flashes alternately">
            <choice>Right ditch light; when F2 on, flashes alternately</choice>
            <choice xml:lang="it">Luce di destra ditch, quando F2=on, lampeggia</choice>
          </enumChoice>
          <enumChoice choice="Left ditch light; when F2 on, flashes alternately">
            <choice>Left ditch light; when F2 on, flashes alternately</choice>
            <choice xml:lang="it">Luce di sinistra ditch, quando F2=on, lampeggia</choice>
          </enumChoice>
          <enumChoice choice="Pulse function (Series 6 only)">
            <choice>Pulse function (Series 6 only)</choice>
            <choice xml:lang="it">Impulsiva (solo series 6)</choice>
          </enumChoice>
          <enumChoice choice="Reserved (0x0D)">
            <choice>Reserved (0x0D)</choice>
            <choice xml:lang="it">Riservata (0x0D)</choice>
          </enumChoice>
          <enumChoice choice="Reserved (0x0E)">
            <choice>Reserved (0x0E)</choice>
            <choice xml:lang="it">Riservata (0x0E)</choice>
          </enumChoice>
          <enumChoice choice="Reserved (0x0F)">
            <choice>Reserved (0x0F)</choice>
            <choice xml:lang="it">Riservata (0x0F)</choice>
          </enumChoice>
        </enumVal>
        <label>   Effect:</label>
    </variable>

    <variable item="XF{$index} Qual" CV="{$CVindex}.235" mask="VVVVXXXX" default="0">
        <enumVal xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://jmri.org/xml/schema/decoder.xsd">
          <enumChoice value="0">
            <choice>On/OFF lead, controlled by Function output line maps</choice>
          </enumChoice>
          <enumChoice value="1">
            <choice>On/OFF lead, controlled by Function output line maps</choice>
          </enumChoice>
          <enumChoice value="2">
            <choice>FWD qualified</choice>
          </enumChoice>
          <enumChoice value="3">
            <choice>REV qualified</choice>
          </enumChoice>
          <enumChoice value="4">
            <choice>F0 qualified</choice>
          </enumChoice>
          <enumChoice value="5">
            <choice>F0 qualified</choice>
          </enumChoice>
          <enumChoice value="6">
            <choice>F0 and FWD qualified</choice>
          </enumChoice>
          <enumChoice value="7">
            <choice>F0 and REV qualified</choice>
          </enumChoice>
          <enumChoice value="10">
            <choice>SPD = 0, non directional qualify</choice>
          </enumChoice>
          <enumChoice value="11">
            <choice>SPD &gt; 0, non directional qualify</choice>
          </enumChoice>
        </enumVal>
        <label>  Qualifier:</label>
    </variable>

</xsl:template>

<xsl:template name="AllFunctionGroups">
  <xsl:param name="CVindex" select="1"/>
  <xsl:param name="index" select="1"/>

  <xsl:if test="29 >= $index">
    <xsl:call-template name="OneFunctionGroup">
      <xsl:with-param name="CVindex" select="$CVindex"/>
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>
    <!-- iterate until done -->
    <xsl:call-template name="AllFunctionGroups">
      <xsl:with-param name="CVindex" select="$CVindex +1"/>
      <xsl:with-param name="index" select="$index+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

    
<!-- Panes  ............................................................................. -->

<xsl:template name="OneFunctionPane">
    <xsl:param name="index"/>

      <griditem gridx="0" gridy="NEXT">
        <label text="XF {$index}     "/>
      </griditem>
      <griditem gridx="1" gridy="CURRENT">
        <column>
          <row>
            <display item="XF{$index} Enable" format="checkbox"/>
            <display item="XF{$index} Invert" format="checkbox"/>
            <display item="XF{$index} Control"/>
            <display item="XF{$index} Snd"/>
          </row>
          <row>
            <display item="XF{$index} Effect"/>
            <display item="XF{$index} Qual"/>
        </row>
        <row>
            <display item="XF{$index} Op0-wht" format="checkbox"/>
            <display item="XF{$index} Op1-yel" format="checkbox"/>
            <display item="XF{$index} Op2-grn" format="checkbox"/>
            <display item="XF{$index} Op3-viol" format="checkbox"/>
            <display item="XF{$index} Op4" format="checkbox"/>
            <display item="XF{$index} Op5" format="checkbox"/>
            <display item="XF{$index} Op6" format="checkbox"/>
            <display item="XF{$index} Op7" format="checkbox"/>
            <display item="XF{$index} Op8" format="checkbox"/>
            <display item="XF{$index} Op9" format="checkbox"/>
            <display item="XF{$index} Op10" format="checkbox"/>
            <display item="XF{$index} Op11" format="checkbox"/>
            <display item="XF{$index} Op12" format="checkbox"/>
            <display item="XF{$index} Op13" format="checkbox"/>
            <display item="XF{$index} Op14" format="checkbox"/>
            <display item="XF{$index} Op15" format="checkbox"/>
          </row>
        <separator/>
        </column>
      </griditem>

    
</xsl:template>

<xsl:template name="AllFunctionPanes">
  <xsl:param name="index" select="1"/>

  <xsl:if test="29 >= $index">
    <xsl:call-template name="OneFunctionPane">
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>
    <!-- iterate until done -->
    <xsl:call-template name="AllFunctionPanes">
      <xsl:with-param name="index" select="$index+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!--           ............................................................................. -->


<!--    END MATTER THAT CONTROLS INSERTION -->

<!-- install new variables at end of variables element-->
 <xsl:template match="variables">
   <variables>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="AllFunctionGroups"/>
   </variables>
 </xsl:template>

<!-- install new panes at end of decoder-config element-->
 <xsl:template match="decoder-config">
   <decoder-config>
   <xsl:apply-templates select="node()"/>
   <pane>
     <name>XF Functions</name>
     <column>
       <row>
        <label>
          <qualifier>
            <variableref>Product Number</variableref><!-- Software Version-->
            <relation>ge</relation>
            <value>5</value>
          </qualifier>
          <text>&lt;html&gt;&lt;b&gt;XF Decoder Detected&lt;/b&gt;&lt;/html&gt;</text>
        </label>        
        <label>
          <qualifier>
            <variableref>Product Number</variableref><!-- Software Version-->
            <relation>lt</relation>
            <value>5</value>
          </qualifier>
          <text>&lt;html&gt;&lt;b&gt;XF Decoder Not Detected, These Entries Not Active&lt;/b&gt;&lt;/html&gt;</text>
        </label>        
       </row>
       <separator/>
       <grid>
   <xsl:call-template name="AllFunctionPanes"/>
       </grid>
     </column>
   </pane>
   </decoder-config>
 </xsl:template>

<!--Identity template copies content forward -->
 <xsl:template match="@*|node()">
   <xsl:copy>
     <xsl:apply-templates select="@*|node()"/>
   </xsl:copy>
 </xsl:template>
</xsl:stylesheet>
