package au.org.emii.classifier;

import org.apache.log4j.Logger;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.kernel.search.classifier.Classifier;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to determine the category paths (facets) to be indexed for an AODN term given its prefLabel or atlLabel
 */

public class LabelClassifier implements Classifier {
    private static Logger logger = Logger.getLogger(LabelClassifier.class);

    private final AodnThesaurus vocabularyThesaurus; // vocabulary in which the term is defined
    private final AodnTermClassifier termClassifier; // classifier used to create CategoryPaths for a term

    public LabelClassifier(ThesaurusFinder thesaurusFinder, String vocabularyScheme, String classificationScheme) {
        vocabularyThesaurus = new AodnThesaurus(thesaurusFinder.getThesaurusByConceptScheme(vocabularyScheme));
        AodnThesaurus classificationThesaurus = new AodnThesaurus(thesaurusFinder.getThesaurusByConceptScheme(classificationScheme));
        termClassifier = new AodnTermClassifier(vocabularyThesaurus, classificationThesaurus);
    }

    @Override
    public List<CategoryPath> classify(String value) {
        AodnTerm term = vocabularyThesaurus.getTermWithLabel(value);

        if (term == null) {
            logger.error("Could not find '" + value + "' in " + vocabularyThesaurus.getThesaurusTitle());
            return new ArrayList<CategoryPath>();
        }

        return termClassifier.classify(term);
    }
}
