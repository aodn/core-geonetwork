package org.fao.geonet.kernel.search;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.DocIdBitSet;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;

import java.io.IOException;
import java.util.BitSet;
import java.util.Collections;
import java.util.Set;

public class CollectionAvailabilityFilter extends Filter {
    private Query _query;
    private Set<String> _fieldsToLoad;
    private GeonetContext _geonetContext = null;

    public CollectionAvailabilityFilter(Query query, ServiceContext serviceContext) {
        this._query = query;
        _fieldsToLoad = Collections.singleton("_uuid");

        if (serviceContext != null)
            this._geonetContext = (GeonetContext) serviceContext.getHandlerContext(Geonet.CONTEXT_NAME);
    }

    @Override
    public DocIdSet getDocIdSet(AtomicReaderContext context, Bits acceptDocs) throws IOException {
        final BitSet bits = new BitSet(context.reader().maxDoc());

        new IndexSearcher(context.reader()).search(_query, new Collector() {

            private int docBase;
            private IndexReader reader;

            @Override
            public void setScorer(Scorer scorer) throws IOException {
            }

            @Override
            public void setNextReader(AtomicReaderContext context) throws IOException {
                this.docBase = context.docBase;
                this.reader = context.reader();
            }

            @Override
            public void collect(int doc) throws IOException {
                Document document;
                try {
                    document = reader.document(docBase + doc, _fieldsToLoad);
                    String uuid = document.get("_uuid");

                    if (_geonetContext != null && _geonetContext.getLinkMonitor() != null &&
                        ! _geonetContext.getLinkMonitor().isHealthy(uuid)) {
                        Log.debug(this.getClass().getSimpleName(), String.format("'%s' is unavailable - omitting from search results", uuid));
                    } else {
                        bits.set(docBase + doc);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public boolean acceptsDocsOutOfOrder() {
                return false;
            }
        });

        return new DocIdBitSet(bits);
    }
}
