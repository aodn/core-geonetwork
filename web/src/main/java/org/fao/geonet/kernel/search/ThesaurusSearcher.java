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

package org.fao.geonet.kernel.search;

import java.io.IOException;
import java.util.List;

import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.rdf.Query;
import org.fao.geonet.kernel.rdf.QueryBuilder;
import org.fao.geonet.kernel.rdf.Selectors;
import org.fao.geonet.kernel.rdf.Wheres;
import org.fao.geonet.kernel.search.keyword.KeywordRelation;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;

public class ThesaurusSearcher {
	private Thesaurus thesaurus;
	private IsoLanguagesMapper isoLanguageMapper;

	public ThesaurusSearcher(Thesaurus thesaurus, IsoLanguagesMapper isoLanguageMapper) {
		this.thesaurus = thesaurus;
		this.isoLanguageMapper = isoLanguageMapper;
	}

	public ThesaurusSearcher(Thesaurus thesaurus) {
		this(thesaurus, IsoLanguagesMapper.getInstance());
	}

	public KeywordBean searchById(String id, String... languages) throws IOException, MalformedQueryException,
			QueryEvaluationException, AccessDeniedException {
		Query<KeywordBean> query = QueryBuilder.keywordQueryBuilder(isoLanguageMapper, languages).where(Wheres.ID(id)).build();

		List<KeywordBean> keywords = query.execute(thesaurus);

		if(keywords.isEmpty()) {
			return null;
		} else {
			return keywords.get(0);
		}
	}

	public List<KeywordBean> searchForRelated(String id, KeywordRelation request, String... languages) throws Exception {
		Query<KeywordBean> query = QueryBuilder.keywordQueryBuilder(isoLanguageMapper, languages).select(Selectors.related(id,request), true).build();
		return query.execute(thesaurus);
	}
}
