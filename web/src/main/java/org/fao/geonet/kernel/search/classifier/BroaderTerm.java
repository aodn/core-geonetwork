//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.search.classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;

public class BroaderTerm implements Classifier {
	
	private Thesaurus thesaurus;
	private final static String LANG_CODE = "eng";

	public BroaderTerm(ThesaurusManager thesaurusManager, String conceptScheme) {
		thesaurus = thesaurusManager.getThesaurusByConceptScheme(conceptScheme);
	}
	
	@Override
	public List<String> classify(String value) {
		List<String> termHierarchy = getClassifications(value);
		Collections.reverse(termHierarchy);
		
		return termHierarchy;
	}
	
	public List<String> getClassifications(String termUri) {
        List<String> termHierarchy = new ArrayList<String>();
		
		KeywordBean term = thesaurus.getKeyword(termUri, LANG_CODE);
		termHierarchy.add(term.getPreferredLabel(LANG_CODE));
		
		if (term.hasBroader()) {
			termHierarchy.addAll(getClassifications(term.getBroaderRelationship()));
		}
		
		return termHierarchy;
	}
}
