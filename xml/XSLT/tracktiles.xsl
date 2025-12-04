<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:db="http://docbook.org/ns/docbook"
    xmlns:tt="http://jmri.org/xml/tracktiles">
    
    <xsl:output method="html" encoding="UTF-8" indent="yes"/>
    
    <!-- Parameter for user language (default: en) -->
    <xsl:param name="userLang">en</xsl:param>
    
    <xsl:template match="/tt:tracktiles">
        <html>
            <head>
                <title>Track Tiles Catalog - <xsl:value-of select="tt:header/tt:vendor"/> <xsl:value-of select="tt:header/tt:family"/></title>
                <meta charset="UTF-8"/>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 20px;
                        background-color: #f5f5f5;
                    }
                    
                    h1 {
                        color: #333;
                        border-bottom: 2px solid #0066cc;
                        padding-bottom: 10px;
                    }
                    
                    .header-info {
                        background-color: white;
                        padding: 15px;
                        margin-bottom: 20px;
                        border-radius: 5px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    
                    .header-info p {
                        margin: 5px 0;
                    }
                    
                    .controls {
                        background-color: white;
                        padding: 15px;
                        margin-bottom: 20px;
                        border-radius: 5px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    
                    .filter-group {
                        display: inline-block;
                        margin-right: 20px;
                        margin-bottom: 10px;
                    }
                    
                    .filter-group label {
                        font-weight: bold;
                        margin-right: 5px;
                    }
                    
                    input[type="text"], select {
                        padding: 5px 10px;
                        border: 1px solid #ccc;
                        border-radius: 3px;
                        font-size: 14px;
                    }
                    
                    button {
                        padding: 6px 12px;
                        background-color: #0066cc;
                        color: white;
                        border: none;
                        border-radius: 3px;
                        cursor: pointer;
                        font-size: 14px;
                    }
                    
                    button:hover {
                        background-color: #0052a3;
                    }
                    
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        background-color: white;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    
                    th {
                        background-color: #0066cc;
                        color: white;
                        padding: 12px 8px;
                        text-align: left;
                        cursor: pointer;
                        user-select: none;
                        position: relative;
                    }
                    
                    th:hover {
                        background-color: #0052a3;
                    }
                    
                    th.sortable::after {
                        content: ' ⇅';
                        opacity: 0.5;
                    }
                    
                    th.sort-asc::after {
                        content: ' ▲';
                        opacity: 1;
                    }
                    
                    th.sort-desc::after {
                        content: ' ▼';
                        opacity: 1;
                    }
                    
                    td {
                        padding: 10px 8px;
                        border-bottom: 1px solid #e0e0e0;
                    }
                    
                    tr:hover {
                        background-color: #f9f9f9;
                    }
                    
                    tr.hidden {
                        display: none;
                    }
                    
                    .stats {
                        margin-top: 10px;
                        color: #666;
                        font-size: 14px;
                    }
                    
                    .geometry-details {
                        font-size: 12px;
                        color: #666;
                    }
                </style>
            </head>
            <body>
                <h1>Track Tiles Catalog</h1>
                
                <div class="header-info">
                    <p><strong>Vendor:</strong> <xsl:value-of select="tt:header/tt:vendor"/></p>
                    <p><strong>Family:</strong> <xsl:value-of select="tt:header/tt:family"/></p>
                    <p><strong>Scale:</strong> <xsl:value-of select="tt:header/tt:scale/tt:name"/> (1:<xsl:value-of select="tt:header/tt:scale/tt:ratio"/>)</p>
                    <p><strong>Gauge:</strong> <xsl:value-of select="tt:header/tt:gauge/tt:gaugename"/> - <xsl:value-of select="tt:header/tt:gauge/tt:gaugesize/@value"/><xsl:value-of select="tt:header/tt:gauge/tt:gaugesize/@unit"/></p>
                </div>
                
                <div class="controls">
                    <div class="filter-group">
                        <label for="searchBox">Search:</label>
                        <input type="text" id="searchBox" placeholder="Search all columns..." onkeyup="filterTable()"/>
                    </div>
                    
                    <div class="filter-group">
                        <label for="typeFilter">Type:</label>
                        <select id="typeFilter" onchange="filterTable()">
                            <option value="">All Types</option>
                            <option value="straight">Straight</option>
                            <option value="curved">Curved</option>
                            <option value="turnout">Turnout</option>
                            <option value="crossing">Crossing</option>
                            <option value="end">End/Bumper</option>
                        </select>
                    </div>
                    
                    <button onclick="clearFilters()">Clear Filters</button>
                    
                    <div class="stats">
                        Total tiles: <span id="totalCount"><xsl:value-of select="count(tt:tiles/tt:tile)"/></span> |
                        Showing: <span id="visibleCount"><xsl:value-of select="count(tt:tiles/tt:tile)"/></span>
                    </div>
                </div>
                
                <table id="tilesTable">
                    <thead>
                        <tr>
                            <th class="sortable" onclick="sortTable(0)">Vendor</th>
                            <th class="sortable" onclick="sortTable(1)">Family</th>
                            <th class="sortable" onclick="sortTable(2)">Gauge</th>
                            <th class="sortable" onclick="sortTable(3)">Type</th>
                            <th class="sortable" onclick="sortTable(4)">Part Code</th>
                            <th class="sortable" onclick="sortTable(5)">Caption</th>
                            <th>Geometry</th>
                        </tr>
                    </thead>
                    <tbody>
                        <xsl:apply-templates select="tt:tiles/tt:tile"/>
                    </tbody>
                </table>
                
                <script>
                    <![CDATA[
                    let sortDirection = {};
                    
                    function sortTable(columnIndex) {
                        const table = document.getElementById('tilesTable');
                        const tbody = table.tBodies[0];
                        const rows = Array.from(tbody.rows);
                        const headers = table.querySelectorAll('th');
                        
                        // Toggle sort direction
                        const currentDir = sortDirection[columnIndex] || 'asc';
                        const newDir = currentDir === 'asc' ? 'desc' : 'asc';
                        sortDirection[columnIndex] = newDir;
                        
                        // Update header classes
                        headers.forEach((header, idx) => {
                            header.classList.remove('sort-asc', 'sort-desc');
                            if (idx === columnIndex) {
                                header.classList.add(newDir === 'asc' ? 'sort-asc' : 'sort-desc');
                            }
                        });
                        
                        // Sort rows
                        rows.sort((a, b) => {
                            const aText = a.cells[columnIndex].textContent.trim();
                            const bText = b.cells[columnIndex].textContent.trim();
                            
                            // Try numeric comparison first
                            const aNum = parseFloat(aText);
                            const bNum = parseFloat(bText);
                            if (!isNaN(aNum) && !isNaN(bNum)) {
                                return newDir === 'asc' ? aNum - bNum : bNum - aNum;
                            }
                            
                            // String comparison
                            const comparison = aText.localeCompare(bText);
                            return newDir === 'asc' ? comparison : -comparison;
                        });
                        
                        // Reorder rows
                        rows.forEach(row => tbody.appendChild(row));
                    }
                    
                    function filterTable() {
                        const searchText = document.getElementById('searchBox').value.toLowerCase();
                        const typeFilter = document.getElementById('typeFilter').value.toLowerCase();
                        const table = document.getElementById('tilesTable');
                        const rows = table.tBodies[0].rows;
                        let visibleCount = 0;
                        
                        for (let i = 0; i < rows.length; i++) {
                            const row = rows[i];
                            let showRow = true;
                            
                            // Apply search filter
                            if (searchText) {
                                const rowText = row.textContent.toLowerCase();
                                if (!rowText.includes(searchText)) {
                                    showRow = false;
                                }
                            }
                            
                            // Apply type filter
                            if (typeFilter && showRow) {
                                const typeCell = row.cells[3].textContent.toLowerCase();
                                if (!typeCell.includes(typeFilter)) {
                                    showRow = false;
                                }
                            }
                            
                            row.classList.toggle('hidden', !showRow);
                            if (showRow) visibleCount++;
                        }
                        
                        document.getElementById('visibleCount').textContent = visibleCount;
                    }
                    
                    function clearFilters() {
                        document.getElementById('searchBox').value = '';
                        document.getElementById('typeFilter').value = '';
                        filterTable();
                    }
                    ]]>
                </script>
            </body>
        </html>
    </xsl:template>
    
    <!-- Template for each tile -->
    <xsl:template match="tt:tile">
        <tr>
            <!-- Vendor -->
            <td><xsl:value-of select="/tt:tracktiles/tt:header/tt:vendor"/></td>
            
            <!-- Family -->
            <td><xsl:value-of select="/tt:tracktiles/tt:header/tt:family"/></td>
            
            <!-- Gauge -->
            <td><xsl:value-of select="/tt:tracktiles/tt:header/tt:gauge/tt:gaugesize/@value"/><xsl:value-of select="/tt:tracktiles/tt:header/tt:gauge/tt:gaugesize/@unit"/></td>
            
            <!-- Type -->
            <td><xsl:value-of select="tt:jmritype"/></td>
            
            <!-- Part Code -->
            <td><xsl:value-of select="tt:partcode"/></td>
            
            <!-- Caption (localized) -->
            <td>
                <xsl:call-template name="getLocalizedCaption"/>
            </td>
            
            <!-- Geometry Details -->
            <td class="geometry-details">
                <xsl:call-template name="formatGeometry"/>
            </td>
        </tr>
    </xsl:template>
    
    <!-- Get localized caption based on user language -->
    <xsl:template name="getLocalizedCaption">
        <xsl:choose>
            <!-- Try to find l10n entry matching user language -->
            <xsl:when test="tt:l10n[@lang=$userLang]">
                <xsl:value-of select="tt:l10n[@lang=$userLang]"/>
            </xsl:when>
            <!-- Fall back to first l10n entry -->
            <xsl:when test="tt:l10n">
                <xsl:value-of select="tt:l10n[1]"/>
            </xsl:when>
            <!-- Synthesize from type and geometry if no l10n -->
            <xsl:otherwise>
                <xsl:call-template name="synthesizeCaption"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- Synthesize caption from type and geometry -->
    <xsl:template name="synthesizeCaption">
        <xsl:value-of select="tt:jmritype"/>
        <xsl:text> </xsl:text>
        <xsl:if test="tt:geometry/tt:straight">
            <xsl:value-of select="tt:geometry/tt:straight/@length"/>
            <xsl:value-of select="tt:geometry/tt:straight/@unit"/>
        </xsl:if>
        <xsl:if test="tt:geometry/tt:curved">
            <xsl:text>R</xsl:text>
            <xsl:value-of select="tt:geometry/tt:curved/@radius"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="tt:geometry/tt:curved/@arc"/>°
        </xsl:if>
    </xsl:template>
    
    <!-- Format geometry details -->
    <xsl:template name="formatGeometry">
        <xsl:choose>
            <!-- Straight track -->
            <xsl:when test="tt:geometry/tt:straight">
                <xsl:text>Length: </xsl:text>
                <xsl:value-of select="tt:geometry/tt:straight/@length"/>
                <xsl:value-of select="tt:geometry/tt:straight/@unit"/>
                <xsl:if test="tt:geometry/tt:straight/@function != 'none' and tt:geometry/tt:straight/@function">
                    <br/>Function: <xsl:value-of select="tt:geometry/tt:straight/@function"/>
                </xsl:if>
                <xsl:if test="tt:geometry/tt:straight/@contactType">
                    <br/>Contact: <xsl:value-of select="tt:geometry/tt:straight/@contactType"/>
                </xsl:if>
                <xsl:if test="tt:geometry/tt:straight/@powered = 'yes'">
                    <br/>Powered: yes
                </xsl:if>
                <xsl:if test="tt:geometry/tt:end">
                    <br/>[End/Bumper]
                </xsl:if>
            </xsl:when>
            
            <!-- Curved track -->
            <xsl:when test="tt:geometry/tt:curved">
                <xsl:text>Radius: </xsl:text>
                <xsl:value-of select="tt:geometry/tt:curved/@radius"/>
                <xsl:value-of select="tt:geometry/tt:curved/@radiusunit"/>
                <xsl:text>, Arc: </xsl:text>
                <xsl:value-of select="tt:geometry/tt:curved/@arc"/>
                <xsl:value-of select="tt:geometry/tt:curved/@arcunit"/>
                <xsl:if test="tt:geometry/tt:curved/@function != 'none' and tt:geometry/tt:curved/@function">
                    <br/>Function: <xsl:value-of select="tt:geometry/tt:curved/@function"/>
                </xsl:if>
                <xsl:if test="tt:geometry/tt:curved/@contactType">
                    <br/>Contact: <xsl:value-of select="tt:geometry/tt:curved/@contactType"/>
                </xsl:if>
                <xsl:if test="tt:geometry/tt:curved/@powered = 'yes'">
                    <br/>Powered: yes
                </xsl:if>
                <xsl:if test="tt:geometry/tt:end">
                    <br/>[End/Bumper]
                </xsl:if>
            </xsl:when>
            
            <!-- Turnout -->
            <xsl:when test="tt:geometry/tt:turnout">
                <xsl:for-each select="tt:geometry/tt:turnout/tt:path">
                    <xsl:value-of select="@direction"/>
                    <xsl:text> (</xsl:text>
                    <xsl:value-of select="@state"/>
                    <xsl:text>): </xsl:text>
                    <xsl:if test="@length">
                        <xsl:value-of select="@length"/>
                        <xsl:value-of select="@lengthunit"/>
                    </xsl:if>
                    <xsl:if test="@radius">
                        <xsl:text>R</xsl:text>
                        <xsl:value-of select="@radius"/>
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="@arc"/>°
                    </xsl:if>
                    <xsl:if test="position() != last()">
                        <br/>
                    </xsl:if>
                </xsl:for-each>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    
</xsl:stylesheet>
