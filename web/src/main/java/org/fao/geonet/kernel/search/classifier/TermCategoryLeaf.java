package org.fao.geonet.kernel.search.classifier;

import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.kernel.KeywordBean;

public class TermCategoryLeaf implements TermCategory {

    private KeywordBean term;

    public TermCategoryLeaf(KeywordBean term) {
        this.term = term;
    }

    @Override
    public KeywordBean getTerm() {
        return term;
    }

    @Override
    public CategoryPath getCategoryPath(String langCode) {
        return new CategoryPath(term.getPreferredLabel(langCode));
    }

}
