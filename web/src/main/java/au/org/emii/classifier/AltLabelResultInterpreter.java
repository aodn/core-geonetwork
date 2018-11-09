package au.org.emii.classifier;

import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.rdf.ResultInterpreter;
import org.openrdf.sesame.query.QueryResultsTable;

import static au.org.emii.classifier.QueryResultsHelper.getColumnValue;

public class AltLabelResultInterpreter extends ResultInterpreter<String> {
    @Override
    public String createFromRow(Thesaurus thesaurus, QueryResultsTable resultTable, int rowIndex) {
        return getColumnValue(resultTable, rowIndex, "altLabel");
    }
}
