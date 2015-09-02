package org.fao.geonet.kernel.search.classifier;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusFinder;

public class ClassificationScheme implements Classifier {

    private final static String LANG_CODE = "eng";

    private Thesaurus vocabularyThesaurus;
    private Thesaurus classificationThesaurus;

    public ClassificationScheme(ThesaurusFinder thesaurusFinder, String vocabularyScheme, String classificationScheme) {
        this.vocabularyThesaurus = thesaurusFinder.getThesaurusByConceptScheme(vocabularyScheme);
        this.classificationThesaurus = thesaurusFinder.getThesaurusByConceptScheme(classificationScheme);
    }

    @Override
    public List<CategoryPath> classify(String uri) {
        if (!vocabularyThesaurus.hasKeyword(uri)) {
            return new ArrayList<CategoryPath>();
        }

        KeywordBean term = vocabularyThesaurus.getKeyword(uri, LANG_CODE);
        TermCategoryPathBuilder builder = new TermCategoryPathBuilder(term, LANG_CODE);

        builder.addAllBroaderTerms(vocabularyThesaurus)
               .addBroadMatchTerms(classificationThesaurus)
               .addAllBroaderTerms(classificationThesaurus);

        return builder.buildCategoryPaths();
    }
}
