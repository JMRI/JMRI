#!/usr/bin/env python3
"""Generate standalone HTML catalog from tracktiles XML files"""

import xml.etree.ElementTree as ET
import json

def extract_tiles(xml_file):
    """Extract tile data from XML file"""
    tree = ET.parse(xml_file)
    root = tree.getroot()
    
    vendor = root.find('header/vendor').text
    family = root.find('header/family').text
    gauge = root.find('header/gauge/gaugesize').get('value')
    gauge_unit = root.find('header/gauge/gaugesize').get('unit')
    
    tiles = []
    for tile in root.findall('tiles/tile'):
        partcode = tile.find('partcode').text
        jmritype = tile.find('jmritype').text
        
        # Extract localization
        l10n = {}
        for l in tile.findall('l10n'):
            l10n[l.get('lang')] = l.text
        
        # Extract geometry
        geom = tile.find('geometry')
        geometry = {}
        if geom is not None:
            for child in geom:
                geometry[child.tag] = dict(child.attrib)
                if child.text:
                    geometry[child.tag]['text'] = child.text
        
        tiles.append({
            'vendor': vendor,
            'family': family,
            'gauge': f"{gauge} {gauge_unit}",
            'partcode': partcode,
            'jmritype': jmritype,
            'l10n': l10n,
            'geometry': geometry
        })
    
    return tiles

def format_geometry(geom):
    """Format geometry data as readable string"""
    parts = []
    for geom_type, attrs in geom.items():
        if geom_type == 'straight':
            length = attrs.get('length', '')
            unit = attrs.get('lengthunit', 'mm')
            function = attrs.get('function', 'none')
            powered = attrs.get('powered', 'no')
            contact = attrs.get('contactType', '')
            
            parts.append(f"Straight: {length}{unit}")
            if function != 'none':
                parts.append(f"({function}")
                if contact:
                    parts.append(f", {contact}")
                if powered == 'yes':
                    parts.append(f", powered")
                parts.append(")")
                
        elif geom_type == 'curved':
            radius = attrs.get('radius', '')
            arc = attrs.get('arc', '')
            runit = attrs.get('radiusunit', 'mm')
            aunit = attrs.get('arcunit', 'deg')
            function = attrs.get('function', 'none')
            
            parts.append(f"Curved: R{radius}{runit}, {arc}{aunit}")
            if function != 'none':
                parts.append(f"({function})")
                
        elif geom_type == 'end':
            length = attrs.get('length', '')
            unit = attrs.get('lengthunit', 'mm')
            parts.append(f"Bumper: {length}{unit}")
            
        elif geom_type == 'turnout':
            paths = attrs.get('paths', '')
            parts.append(f"Turnout: {paths} paths")
            
        elif geom_type == 'crossing':
            angle = attrs.get('angle', '')
            unit = attrs.get('angleunit', 'deg')
            parts.append(f"Crossing: {angle}{unit}")
    
    return ' | '.join(parts)

def get_caption(l10n, lang='en'):
    """Get caption in preferred language"""
    if lang in l10n:
        return l10n[lang]
    elif 'en' in l10n:
        return l10n['en']
    elif len(l10n) > 0:
        return list(l10n.values())[0]
    return ''

# Extract data from both files
print("Extracting Märklin C-Track data...")
maerklin_tiles = extract_tiles('xml/tracktiles/Maerklin - H0 - C-Track.xml')

print("Extracting Piko A-Track data...")
piko_tiles = extract_tiles('xml/tracktiles/Piko - H0 - A-Track.xml')

all_tiles = maerklin_tiles + piko_tiles

# Generate HTML
html = '''<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Track Tiles Catalog</title>
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
        
        .stats {
            margin-top: 10px;
            color: #666;
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
        }
        
        th:hover {
            background-color: #0052a3;
        }
        
        th.sortable::after {
            content: " ⇅";
            opacity: 0.5;
        }
        
        th.sort-asc::after {
            content: " ▲";
            opacity: 1;
        }
        
        th.sort-desc::after {
            content: " ▼";
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
        
        .no-results {
            text-align: center;
            padding: 40px;
            color: #999;
            font-style: italic;
        }
    </style>
</head>
<body>
    <h1>Track Tiles Catalog</h1>
    
    <div class="controls">
        <div class="filter-group">
            <label for="search">Search:</label>
            <input type="text" id="search" placeholder="Type to filter...">
        </div>
        
        <div class="filter-group">
            <label for="typeFilter">Type:</label>
            <select id="typeFilter">
                <option value="">All</option>
                <option value="straight">Straight</option>
                <option value="curved">Curved</option>
                <option value="turnout">Turnout</option>
                <option value="crossing">Crossing</option>
                <option value="end">Bumper/End</option>
            </select>
        </div>
        
        <div class="filter-group">
            <label for="vendorFilter">Vendor:</label>
            <select id="vendorFilter">
                <option value="">All</option>
                <option value="Märklin">Märklin</option>
                <option value="Piko">Piko</option>
            </select>
        </div>
        
        <button onclick="clearFilters()">Clear Filters</button>
        
        <div class="stats">
            <span id="visibleCount">0</span> of <span id="totalCount">0</span> tiles displayed
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
        <tbody id="tilesBody">
        </tbody>
    </table>
    
    <div id="noResults" class="no-results" style="display: none;">
        No tiles match your search criteria.
    </div>
    
    <script>
        const tilesData = ''' + json.dumps(all_tiles, indent=8) + ''';
        
        let currentSort = { column: -1, direction: 'asc' };
        
        function renderTable() {
            const tbody = document.getElementById('tilesBody');
            tbody.innerHTML = '';
            
            tilesData.forEach(tile => {
                const row = tbody.insertRow();
                row.insertCell(0).textContent = tile.vendor;
                row.insertCell(1).textContent = tile.family;
                row.insertCell(2).textContent = tile.gauge;
                row.insertCell(3).textContent = tile.jmritype;
                row.insertCell(4).textContent = tile.partcode;
                row.insertCell(5).textContent = getCaption(tile.l10n);
                row.insertCell(6).textContent = formatGeometry(tile.geometry);
            });
            
            updateStats();
        }
        
        function getCaption(l10n) {
            return l10n.en || l10n.de || l10n.es || Object.values(l10n)[0] || '';
        }
        
        function formatGeometry(geom) {
            const parts = [];
            for (const [type, attrs] of Object.entries(geom)) {
                if (type === 'straight') {
                    let str = `Straight: ${attrs.length}${attrs.lengthunit || 'mm'}`;
                    if (attrs.function && attrs.function !== 'none') {
                        str += ` (${attrs.function}`;
                        if (attrs.contactType) str += `, ${attrs.contactType}`;
                        if (attrs.powered === 'yes') str += ', powered';
                        str += ')';
                    }
                    parts.push(str);
                } else if (type === 'curved') {
                    let str = `Curved: R${attrs.radius}${attrs.radiusunit || 'mm'}, ${attrs.arc}${attrs.arcunit || 'deg'}`;
                    if (attrs.function && attrs.function !== 'none') {
                        str += ` (${attrs.function})`;
                    }
                    parts.push(str);
                } else if (type === 'end') {
                    parts.push(`Bumper: ${attrs.length}${attrs.lengthunit || 'mm'}`);
                } else if (type === 'turnout') {
                    parts.push(`Turnout: ${attrs.paths} paths`);
                } else if (type === 'crossing') {
                    parts.push(`Crossing: ${attrs.angle}${attrs.angleunit || 'deg'}`);
                }
            }
            return parts.join(' | ');
        }
        
        function sortTable(columnIndex) {
            const table = document.getElementById('tilesTable');
            const tbody = table.querySelector('tbody');
            const rows = Array.from(tbody.rows);
            
            // Update sort direction
            if (currentSort.column === columnIndex) {
                currentSort.direction = currentSort.direction === 'asc' ? 'desc' : 'asc';
            } else {
                currentSort.column = columnIndex;
                currentSort.direction = 'asc';
            }
            
            // Remove sort classes from all headers
            table.querySelectorAll('th').forEach(th => {
                th.classList.remove('sort-asc', 'sort-desc');
            });
            
            // Add sort class to current header
            const th = table.querySelectorAll('th')[columnIndex];
            th.classList.add(currentSort.direction === 'asc' ? 'sort-asc' : 'sort-desc');
            
            // Sort rows
            rows.sort((a, b) => {
                let aVal = a.cells[columnIndex].textContent;
                let bVal = b.cells[columnIndex].textContent;
                
                // Try numeric comparison
                const aNum = parseFloat(aVal);
                const bNum = parseFloat(bVal);
                if (!isNaN(aNum) && !isNaN(bNum)) {
                    return currentSort.direction === 'asc' ? aNum - bNum : bNum - aNum;
                }
                
                // String comparison
                if (currentSort.direction === 'asc') {
                    return aVal.localeCompare(bVal);
                } else {
                    return bVal.localeCompare(aVal);
                }
            });
            
            // Re-append rows
            rows.forEach(row => tbody.appendChild(row));
        }
        
        function applyFilters() {
            const searchTerm = document.getElementById('search').value.toLowerCase();
            const typeFilter = document.getElementById('typeFilter').value.toLowerCase();
            const vendorFilter = document.getElementById('vendorFilter').value;
            
            const tbody = document.getElementById('tilesBody');
            const rows = tbody.rows;
            
            for (let row of rows) {
                const vendor = row.cells[0].textContent;
                const type = row.cells[3].textContent.toLowerCase();
                const rowText = row.textContent.toLowerCase();
                
                const matchesSearch = searchTerm === '' || rowText.includes(searchTerm);
                const matchesType = typeFilter === '' || type === typeFilter;
                const matchesVendor = vendorFilter === '' || vendor === vendorFilter;
                
                if (matchesSearch && matchesType && matchesVendor) {
                    row.classList.remove('hidden');
                } else {
                    row.classList.add('hidden');
                }
            }
            
            updateStats();
        }
        
        function clearFilters() {
            document.getElementById('search').value = '';
            document.getElementById('typeFilter').value = '';
            document.getElementById('vendorFilter').value = '';
            applyFilters();
        }
        
        function updateStats() {
            const tbody = document.getElementById('tilesBody');
            const total = tbody.rows.length;
            const visible = Array.from(tbody.rows).filter(row => !row.classList.contains('hidden')).length;
            
            document.getElementById('totalCount').textContent = total;
            document.getElementById('visibleCount').textContent = visible;
            
            const noResults = document.getElementById('noResults');
            if (visible === 0 && total > 0) {
                noResults.style.display = 'block';
                document.getElementById('tilesTable').style.display = 'none';
            } else {
                noResults.style.display = 'none';
                document.getElementById('tilesTable').style.display = 'table';
            }
        }
        
        // Event listeners
        document.getElementById('search').addEventListener('input', applyFilters);
        document.getElementById('typeFilter').addEventListener('change', applyFilters);
        document.getElementById('vendorFilter').addEventListener('change', applyFilters);
        
        // Initial render
        renderTable();
    </script>
</body>
</html>'''

# Write HTML file
output_file = 'xml/tracktiles/tracktiles-catalog.html'
with open(output_file, 'w', encoding='utf-8') as f:
    f.write(html)

print(f"\nHTML catalog generated: {output_file}")
print(f"Total tiles: {len(all_tiles)} (Märklin: {len(maerklin_tiles)}, Piko: {len(piko_tiles)})")
