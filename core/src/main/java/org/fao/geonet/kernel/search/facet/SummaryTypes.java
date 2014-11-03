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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummaryTypes {

    private List<SummaryType>  summaryTypes;

    public SummaryTypes(List<SummaryType> summaryTypes) {
        this.summaryTypes = summaryTypes;
    }

    public List<SummaryType> getSummaryTypes() {
        return new ArrayList<SummaryType>(summaryTypes);
    }

    public Map<String, Map<String, ItemConfig>> getSummaryTypeMap() {
        Map<String, Map<String, ItemConfig>> result = new HashMap<String, Map<String, ItemConfig>>();

        for (SummaryType summaryType : summaryTypes) {
            result.put(summaryType.getName(), summaryType.getItemMap());
        }

        return result;
    }
}
