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

import java.net.URL;
import java.util.List;

import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.test.ThesaurusDirectoryLoader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClassificationSchemeTest {

    private static final String VOCABULARY_SCHEME = "http://www.my.com/test_vocabulary";
    private static final String CLASSIFICATION_SCHEME = "http://www.my.com/test_classification";
    
    private static ThesaurusFinder thesaurusFinder;
    
    private Classifier classificationSchemeClassifier;

    @BeforeClass
    static public void loadThesauri() {
        URL thesauriDirectory = ClassificationScheme.class.getResource("/thesauri");
        thesaurusFinder = new ThesaurusDirectoryLoader(thesauriDirectory.getFile());
    }
    
    @Before
    public void setup() {
        classificationSchemeClassifier = new ClassificationScheme(thesaurusFinder, VOCABULARY_SCHEME, CLASSIFICATION_SCHEME);
    }

    @Test
    public void testClassifyHierarchyWithBroaderTerms() {
        List<CategoryPath> result = classificationSchemeClassifier.classify("http://www.my.com/#sea_surface_temperature");
        assertCategoryListEquals(result, "ocean/ocean temperature/sea surface temperature");
    }

    @Test
    public void testClassifyTermWithMultipleBroaderTerms() {
        List<CategoryPath> result = classificationSchemeClassifier.classify("http://www.my.com/#air_sea_flux");
        assertCategoryListEquals(result, "physical - water/air sea flux", "physical - air/air sea flux");
    }

    @Test
    public void testClassifyTermWithNoBroaderTerms() {
        List<CategoryPath> result = classificationSchemeClassifier.classify("http://www.my.com/#longitude");
        assertEquals(0, result.size());
    }

    @Test
    public void testClassifyTermDoesNotExist() {
        List<CategoryPath> result = classificationSchemeClassifier.classify("#http://www.my.com/#unknown_term");
        assertEquals(0, result.size());
    }

}
