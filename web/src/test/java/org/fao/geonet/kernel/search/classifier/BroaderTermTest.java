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

package org.fao.geonet.kernel.search.classifier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.fao.geonet.exceptions.TermNotFoundException;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.junit.Test;

public class BroaderTermTest {

    private static final String TEST_LANG = "eng";
    private static final String TEST_CONFIG_SCHEME = "http://geonetwork-opensource.org/regions";
    private static final String TEST_VALUE = "#term";

    @Test
    public void testClassifyHierarchyWithBroaderTerms() {
        ThesaurusManager mockManager = mockThesaurusWithBroaderTerms();

        BroaderTerm testBroaderTermClassifier = new BroaderTerm(mockManager, TEST_CONFIG_SCHEME);
        List<String> testTermHierarchy = testBroaderTermClassifier.classify(TEST_VALUE);

        assertEquals(testTermHierarchy.size(), 3);
        assertEquals(testTermHierarchy.get(0), "ocean"); 
        assertEquals(testTermHierarchy.get(1), "ocean temperature");
        assertEquals(testTermHierarchy.get(2), "sea surface temperature");
    }

    @Test(expected=TermNotFoundException.class)
    public void testClassifyTermDoesNotExist() {
        ThesaurusManager mockManager = mockTermNotFoundThesaurus();

        BroaderTerm testBroaderTermClassifier = new BroaderTerm(mockManager, TEST_CONFIG_SCHEME);
        testBroaderTermClassifier.classify(TEST_VALUE);
    }

    private ThesaurusManager mockTermNotFoundThesaurus() {
        Thesaurus mockThesaurus = mock(Thesaurus.class);
        ThesaurusManager mockManager = mockManager(mockThesaurus);
        when(mockThesaurus.getKeyword(TEST_VALUE, TEST_LANG)).thenThrow(new TermNotFoundException("term not found"));
        return mockManager;
    }

    private ThesaurusManager mockThesaurusWithBroaderTerms() {
        Thesaurus mockThesaurus = mock(Thesaurus.class);
        KeywordBean mockKeywordBean = mockKeyword("#1", "sea surface temperature"); 
        KeywordBean mockKeywordBeanBroader = mockKeyword("#2", "ocean temperature");
        KeywordBean mockKeywordBeanBroadest = mockKeyword("", "ocean");

        when(mockThesaurus.getKeyword("#term", TEST_LANG)).thenReturn(mockKeywordBean);
        when(mockThesaurus.getKeyword("#1", TEST_LANG)).thenReturn(mockKeywordBeanBroader);
        when(mockThesaurus.getKeyword("#2", TEST_LANG)).thenReturn(mockKeywordBeanBroadest);

        return mockManager(mockThesaurus);
    }

    private ThesaurusManager mockManager(Thesaurus mockThesaurus) {
        ThesaurusManager mockManager = mock(ThesaurusManager.class);
        when(mockManager.getThesaurusByConceptScheme(TEST_CONFIG_SCHEME)).thenReturn(mockThesaurus);
        return mockManager;
    }

    private KeywordBean mockKeyword(String theBroaderTerm, String label) {
        KeywordBean mockKeywordBean = mock(KeywordBean.class);
        when(mockKeywordBean.getBroaderRelationship()).thenReturn(theBroaderTerm);
        when(mockKeywordBean.hasBroader()).thenReturn(!theBroaderTerm.equals(""));
        when(mockKeywordBean.getPreferredLabel(TEST_LANG)).thenReturn(label);
        return mockKeywordBean;
    }
}
