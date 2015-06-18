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
import org.fao.geonet.kernel.search.keyword.KeywordRelation;
import org.junit.Test;

public class BroaderTermTest {

    private static final String TEST_LANG = "eng";
    private static final String TEST_CONFIG_SCHEME = "http://geonetwork-opensource.org/regions";
    private static final String TEST_VALUE = "#term";

    @Test
    public void testClassifyHierarchyWithBroaderTerms() {
        ThesaurusManager mockManager = mockThesaurusWithBroaderTerms();

        BroaderTerm testBroaderTermClassifier = new BroaderTerm(mockManager, TEST_CONFIG_SCHEME);
        List<CategoryPath> testTermHierarchy = testBroaderTermClassifier.classify(TEST_VALUE);

        assertCategoryListEquals(testTermHierarchy, "ocean/ocean temperature/sea surface temperature");
    }

    @Test
    public void testClassifyTermWithMultipleBroaderTerms() {
        ThesaurusManager mockManager = mockThesaurusWithMultipleBroaderTerms();

        BroaderTerm testBroaderTermClassifier = new BroaderTerm(mockManager, TEST_CONFIG_SCHEME);
        List<CategoryPath> testTermHierarchy = testBroaderTermClassifier.classify(TEST_VALUE);

        assertCategoryListEquals(testTermHierarchy, "physical - water/air sea flux", "physical - air/air sea flux");
    }

    @Test
    public void testClassifyTermDoesNotExist() {
        ThesaurusManager mockManager = mockTermNotFoundThesaurus();

        BroaderTerm testBroaderTermClassifier = new BroaderTerm(mockManager, TEST_CONFIG_SCHEME);
        List<CategoryPath> testTermHierarchy = testBroaderTermClassifier.classify(TEST_VALUE);

        assertEquals(0, testTermHierarchy.size());
    }

    private ThesaurusManager mockTermNotFoundThesaurus() {
        Thesaurus mockThesaurus = mock(Thesaurus.class);
        ThesaurusManager mockManager = mockManager(mockThesaurus);
        when(mockThesaurus.hasKeyword("#term")).thenReturn(false);
        when(mockThesaurus.getKeyword(TEST_VALUE, TEST_LANG)).thenThrow(new TermNotFoundException("term not found"));
        return mockManager;
    }

    private ThesaurusManager mockThesaurusWithBroaderTerms() {
        Thesaurus mockThesaurus = mock(Thesaurus.class);
        KeywordBean mockKeywordBean = mockKeyword("#term", "sea surface temperature", true); 
        KeywordBean mockKeywordBeanBroader = mockKeyword("#1", "ocean temperature", true);
        KeywordBean mockKeywordBeanBroadest = mockKeyword("#2", "ocean", false);

        when(mockThesaurus.hasKeyword("#term")).thenReturn(true);
        when(mockThesaurus.getKeyword("#term", TEST_LANG)).thenReturn(mockKeywordBean);
        when(mockThesaurus.getRelated("#term", KeywordRelation.NARROWER, TEST_LANG)).thenReturn(Arrays.asList(mockKeywordBeanBroader));
        when(mockThesaurus.getRelated("#1", KeywordRelation.NARROWER, TEST_LANG)).thenReturn(Arrays.asList(mockKeywordBeanBroadest));

        return mockManager(mockThesaurus);
    }

    private ThesaurusManager mockThesaurusWithMultipleBroaderTerms() {
        Thesaurus mockThesaurus = mock(Thesaurus.class);
        KeywordBean mockKeywordBean = mockKeyword("#term", "air sea flux", true); 
        KeywordBean mockKeywordBeanBroader1 = mockKeyword("#1", "physical - water", false);
        KeywordBean mockKeywordBeanBroader2 = mockKeyword("#2", "physical - air", false);

        when(mockThesaurus.hasKeyword("#term")).thenReturn(true);
        when(mockThesaurus.getKeyword("#term", TEST_LANG)).thenReturn(mockKeywordBean);
        when(mockThesaurus.getRelated("#term", KeywordRelation.NARROWER, TEST_LANG)).thenReturn(Arrays.asList(mockKeywordBeanBroader1, mockKeywordBeanBroader2));

        return mockManager(mockThesaurus);
	}

    private ThesaurusManager mockManager(Thesaurus mockThesaurus) {
        ThesaurusManager mockManager = mock(ThesaurusManager.class);
        when(mockManager.getThesaurusByConceptScheme(TEST_CONFIG_SCHEME)).thenReturn(mockThesaurus);
        return mockManager;
    }

    private KeywordBean mockKeyword(String uri, String label, boolean hasBroader) {
        KeywordBean mockKeywordBean = mock(KeywordBean.class);
        when(mockKeywordBean.hasBroader()).thenReturn(hasBroader);
        when(mockKeywordBean.getPreferredLabel(TEST_LANG)).thenReturn(label);
        when(mockKeywordBean.getUriCode()).thenReturn(uri);
        return mockKeywordBean;
    }
}
