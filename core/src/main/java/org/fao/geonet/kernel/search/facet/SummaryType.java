//===    Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===    United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===    and United Nations Environment Programme (UNEP)
//===
//===    This program is free software; you can redistribute it and/or modify
//===    it under the terms of the GNU General Public License as published by
//===    the Free Software Foundation; either version 2 of the License, or (at
//===    your option) any later version.
//===
//===    This program is distributed in the hope that it will be useful, but
//===    WITHOUT ANY WARRANTY; without even the implied warranty of
//===    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===    General Public License for more details.
//===
//===    You should have received a copy of the GNU General Public License
//===    along with this program; if not, write to the Free Software
//===    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===    Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===    Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.search.facet;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SummaryType {

    private String name;

    private List<ItemConfig> items;

    public SummaryType(String name, List<ItemConfig> items) {
        this.name = name;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public List<ItemConfig> getItems() {
        return items;
    }

    public Map<String, ItemConfig> getItemMap() {
        // Use linked hash map so that results in output are same as ordered in summary file
        Map<String, ItemConfig> result = new LinkedHashMap<String, ItemConfig>();

        for (ItemConfig item: items) {
            result.put(item.getDimension().getName(), item);
        }

        return result;
    }
}
