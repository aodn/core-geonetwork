package org.fao.geonet.kernel.search.classifier;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.search.keyword.KeywordRelation;

public class TermCategoryPathBuilder {
    private List<TermCategory> categories;
    private String langCode;

    public TermCategoryPathBuilder(KeywordBean term, String langCode) {
        this.langCode = langCode;
        categories = new ArrayList<TermCategory>();
        categories.add(new TermCategoryLeaf(term));
    }

    public TermCategoryPathBuilder addBroadMatchTerms(Thesaurus thesaurus) {
        List<TermCategory> result = new ArrayList<TermCategory>();

        for (TermCategory category: categories) {
            if (hasBroaderMatchTerms(thesaurus, category.getTerm())) {
                for (KeywordBean broaderTerm: getBroaderMatchTerms(thesaurus, category.getTerm())) {
                    result.add(new TermCategoryParent(broaderTerm, category));
                }
            } else {
                // ignore any categories which haven't been included in the lookup thesaurus
            }
        }

        categories = result;

        return this;
    }

    public TermCategoryPathBuilder addAllBroaderTerms(Thesaurus thesaurus) {
        while (haveBroaderTerms(thesaurus)) {
            addBroaderTerms(thesaurus);
        }

        return this;
    }

    public List<CategoryPath> buildCategoryPaths() {
        List<CategoryPath> categoryPaths = new ArrayList<CategoryPath>();

        for (TermCategory category: categories) {
            categoryPaths.add(category.getCategoryPath(langCode));
        }

        return categoryPaths;
    }

    private void addBroaderTerms(Thesaurus thesaurus) {
        List<TermCategory> result = new ArrayList<TermCategory>();

        for (TermCategory category: categories) {
            if (hasBroaderTerms(thesaurus, category.getTerm())) {
                for (KeywordBean broaderTerm: getBroaderTerms(thesaurus, category.getTerm())) {
                    result.add(new TermCategoryParent(broaderTerm, category));
                }
            } else {
                result.add(category);
            }
        }

        categories = result;
    }

    private boolean haveBroaderTerms(Thesaurus thesaurus) {
        for (TermCategory category: categories) {
            if (hasBroaderTerms(thesaurus, category.getTerm())) {
                return true;
            }
        }

        return false;
    }

    private boolean hasBroaderTerms(Thesaurus thesaurus, KeywordBean term) {
        return getBroaderTerms(thesaurus, term).size() > 0;
    }

    private List<KeywordBean> getBroaderTerms(Thesaurus thesaurus, KeywordBean term) {
        return thesaurus.getRelated(term.getUriCode(), KeywordRelation.NARROWER, langCode);
    }

    private boolean hasBroaderMatchTerms(Thesaurus thesaurus, KeywordBean term) {
        return getBroaderMatchTerms(thesaurus, term).size() > 0;
    }

    private List<KeywordBean> getBroaderMatchTerms(Thesaurus thesaurus, KeywordBean term) {
        return thesaurus.getRelated(term.getUriCode(), KeywordRelation.NARROWER_MATCH, langCode);
    }
}
