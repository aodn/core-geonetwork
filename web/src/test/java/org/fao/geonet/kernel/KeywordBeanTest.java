//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.kernel;

import static org.junit.Assert.*;

import org.junit.Test;

public class KeywordBeanTest {

    @Test
    public void testHasBroaderRelationship() {
        KeywordBean testKeywordBean = new KeywordBean();
        testKeywordBean.setBroaderRelationship("broaderTerm");
        assertTrue(testKeywordBean.hasBroader());
    }

    @Test
    public void testHasNoBroaderRelationship() {
        KeywordBean testKeywordBean = new KeywordBean();
        testKeywordBean.setBroaderRelationship(null);
        assertFalse(testKeywordBean.hasBroader());
    }
}
