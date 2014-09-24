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

import static org.fao.geonet.test.CategoryTestHelper.assertCategoryListEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.exceptions.TermNotFoundException;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.junit.Test;

public class BroaderTermTest {

    private static final String ENG = "eng";
    private static final String PARAMETER_THESAURUS = "http://geonetwork-opensource.org/parameters";

    @Test
    public void testWithTermWithBroaderTermWithBroaderTerm() {
        ThesaurusManager mockManager = mockThesaurusManagerWithTermWithBroaderTermWithBroaderTerm();

        BroaderTerm testBroaderTermClassifier = new BroaderTerm(mockManager, PARAMETER_THESAURUS);
        List<CategoryPath> testTermHierarchy = testBroaderTermClassifier.classify("#sea-surface-temperature");

        assertCategoryListEquals(testTermHierarchy, "ocean/ocean temperature/sea surface temperature");
    }

    @Test
    public void testWithTermWithTwoBroaderTerms() {
        ThesaurusManager mockManager = mockThesaurusManagerWithTermWithTwoBroaderTerms();

        BroaderTerm testBroaderTermClassifier = new BroaderTerm(mockManager, PARAMETER_THESAURUS);
        List<CategoryPath> testTermHierarchy = testBroaderTermClassifier.classify("#air-sea-flux");

        assertCategoryListEquals(testTermHierarchy, "physical - water/air sea flux", "physical - air/air sea flux");
    }

    @Test
    public void testWithUnknownTerm() {
        ThesaurusManager mockManager = mockThesaurusManagerWithUnkownTerm();

        BroaderTerm testBroaderTermClassifier = new BroaderTerm(mockManager, PARAMETER_THESAURUS);
        List<CategoryPath> testTermHierarchy = testBroaderTermClassifier.classify("#unkown-term");

        assertEquals(0, testTermHierarchy.size());
    }

    private ThesaurusManager mockThesaurusManagerWithUnkownTerm() {
        Thesaurus mockThesaurus = mock(Thesaurus.class);
        ThesaurusManager mockManager = mockManager(mockThesaurus);
        when(mockThesaurus.hasKeyword("#unkown-term")).thenReturn(false);
        when(mockThesaurus.getKeyword("#unkown-term", ENG)).thenThrow(new TermNotFoundException("term not found"));
        return mockManager;
    }

    private ThesaurusManager mockThesaurusManagerWithTermWithBroaderTermWithBroaderTerm() {
        Thesaurus mockThesaurus = mock(Thesaurus.class);
        
        KeywordBean mockKeywordBean = mockKeyword("#sea-surface-temperature", "sea surface temperature"); 
        KeywordBean mockKeywordBeanBroader = mockKeyword("#ocean-temperature", "ocean temperature");
        KeywordBean mockKeywordBeanBroadest = mockKeyword("#ocean", "ocean");

        addKeywordsToThesaurus(mockThesaurus, mockKeywordBean);
        addBroaderTermsToKeyword(mockThesaurus, mockKeywordBean, mockKeywordBeanBroader);
        addBroaderTermsToKeyword(mockThesaurus, mockKeywordBeanBroader, mockKeywordBeanBroadest);

        return mockManager(mockThesaurus);
    }

    private ThesaurusManager mockThesaurusManagerWithTermWithTwoBroaderTerms() {
        Thesaurus mockThesaurus = mock(Thesaurus.class);

        KeywordBean mockKeywordBean = mockKeyword("#air-sea-flux", "air sea flux");
        KeywordBean mockKeywordBeanBroader1 = mockKeyword("#physical-water", "physical - water");
        KeywordBean mockKeywordBeanBroader2 = mockKeyword("#physical-air", "physical - air");

        addKeywordsToThesaurus(mockThesaurus, mockKeywordBean, mockKeywordBeanBroader1, mockKeywordBeanBroader2);
        addBroaderTermsToKeyword(mockThesaurus, mockKeywordBean, mockKeywordBeanBroader1, mockKeywordBeanBroader2);

        return mockManager(mockThesaurus);
    }

    private void addBroaderTermsToKeyword(Thesaurus mockThesaurus, KeywordBean mockKeywordBean, KeywordBean... mockBroaderKeywordBeans) {
        String uri = mockKeywordBean.getUriCode();
        when(mockThesaurus.hasBroader(uri)).thenReturn(true);
        when(mockThesaurus.getBroader(uri, ENG)).thenReturn(Arrays.asList(mockBroaderKeywordBeans));
    }

    private void addKeywordsToThesaurus(Thesaurus mockThesaurus, KeywordBean... mockKeywordBeans) {
        for (KeywordBean mockKeywordBean: mockKeywordBeans) {
            when(mockThesaurus.hasKeyword(mockKeywordBean.getUriCode())).thenReturn(true);
            when(mockThesaurus.getKeyword(mockKeywordBean.getUriCode(), ENG)).thenReturn(mockKeywordBean);
        }
    }

    private KeywordBean mockKeyword(String uri, String label) {
        KeywordBean mockKeywordBean = mock(KeywordBean.class);
        when(mockKeywordBean.getPreferredLabel(ENG)).thenReturn(label);
        when(mockKeywordBean.getUriCode()).thenReturn(uri);
        return mockKeywordBean;
    }

    private ThesaurusManager mockManager(Thesaurus mockThesaurus) {
        ThesaurusManager mockManager = mock(ThesaurusManager.class);
        when(mockManager.getThesaurusByConceptScheme(PARAMETER_THESAURUS)).thenReturn(mockThesaurus);
        return mockManager;
    }

}
