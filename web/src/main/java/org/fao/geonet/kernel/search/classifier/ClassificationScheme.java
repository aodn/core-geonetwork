package org.fao.geonet.kernel.search.classifier;

import org.fao.geonet.kernel.search.facet.CategoryHelper;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.kernel.search.keyword.KeywordRelation;

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
        
        if (!includedInClassificationScheme(term)) {
            return new ArrayList<CategoryPath>();
        }
        

        return buildCategoryPaths(term, getBroaderMatchTerms(term));
    }

    private List<CategoryPath> buildCategoryPaths(KeywordBean term,
            List<KeywordBean> broaderTerms) {
        List<CategoryPath> broaderTermCategoryPaths = buildCategoryPaths(broaderTerms);
        return addSubCategory(broaderTermCategoryPaths, term.getPreferredLabel(LANG_CODE));
    }

    private List<CategoryPath> buildCategoryPaths(List<KeywordBean> broaderTerms) {
        List<CategoryPath> result = new ArrayList<CategoryPath>();

        for (KeywordBean broaderTerm : broaderTerms) {
            result.addAll(buildCategoryPaths(broaderTerm));
        }
        return result;
    }

    private List<CategoryPath> buildCategoryPaths(KeywordBean term) {
        String termLabel = term.getPreferredLabel(LANG_CODE);

        if (hasBroaderTerms(term)) {
            return buildCategoryPaths(term, getBroaderTerms(term));
        } else {
            return buildSingleCategoryCategoryPathList(termLabel);
        }
    }

    private List<CategoryPath> buildSingleCategoryCategoryPathList(String categoryName) {
        List<CategoryPath> result = new ArrayList<CategoryPath>();
        result.add(new CategoryPath(categoryName));
        return result;
    }

    private List<CategoryPath> addSubCategory(
            List<CategoryPath> categoryPaths, String subCategoryName) {
        List<CategoryPath> result = new ArrayList<CategoryPath>();

        for (CategoryPath categoryPathToUpdate : categoryPaths) {
            CategoryPath newCategoryPath = CategoryHelper.addSubCategory(
                categoryPathToUpdate,
                subCategoryName
            );

            result.add(newCategoryPath);
        }
        
        return result;
    }

    private boolean hasBroaderTerms(KeywordBean term) {
        return getBroaderTerms(term).size() > 0;
    }

    private List<KeywordBean> getBroaderTerms(KeywordBean term) {
        return classificationThesaurus.getRelated(term.getUriCode(), KeywordRelation.NARROWER, LANG_CODE);
    }

    private boolean includedInClassificationScheme(KeywordBean term) {
        return getBroaderMatchTerms(term).size() > 0;
    }

    private List<KeywordBean> getBroaderMatchTerms(KeywordBean term) {
        return classificationThesaurus.getRelated(term.getUriCode(), KeywordRelation.NARROWER_MATCH, LANG_CODE);
    }

}
