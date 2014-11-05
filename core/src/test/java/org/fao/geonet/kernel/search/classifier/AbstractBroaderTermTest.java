//===   Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.kernel.search.classifier;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.openrdf.sesame.config.ConfigurationException;
import org.springframework.core.io.ClassPathResource;

public abstract class AbstractBroaderTermTest {

    private final IsoLanguagesMapper isoLangMapper = new IsoLanguagesMapper() {
        {
            _isoLanguagesMap639.put("en", "eng");
        }
    };

    protected ThesaurusManager mockThesaurusManagerWith(String fileName) throws IOException, ConfigurationException {
        File thesaurusFile = new ClassPathResource(fileName, this.getClass()).getFile();
        Thesaurus thesaurus = loadThesaurusFile(isoLangMapper, thesaurusFile);
        ThesaurusManager mockManager = mock(ThesaurusManager.class);
        when(mockManager.getThesaurusByConceptScheme(anyString())).thenReturn(thesaurus);
        return mockManager;
    }

    private Thesaurus loadThesaurusFile(IsoLanguagesMapper isoLanguagesMapper, File thesaurusFile)
      throws ConfigurationException {
        Thesaurus thesaurus = new Thesaurus(isoLanguagesMapper, thesaurusFile.getName(), "external", "theme", thesaurusFile, "http://dummy.org/geonetwork");
        thesaurus.initRepository();
        return thesaurus;
    }

}
