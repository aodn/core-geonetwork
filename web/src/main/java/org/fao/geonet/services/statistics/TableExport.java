package org.fao.geonet.services.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import jeeves.constants.Jeeves;
import jeeves.exceptions.BadParameterEx;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.IO;
import jeeves.utils.Log;
import jeeves.utils.Util;

import org.apache.commons.io.IOUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

/**
 * Jeeves service to export a Database table (based on its given name) into the configured file format (currently, only CSV supported, full
 * fields dump for the given table)
 * 
 * @author nicolas ribot
 */
public class TableExport extends NotInReadOnlyModeService {
    /** constant for CSV file export */
    public final static String CSV = "CSV";

    private String currentExportFormat;
    /** the full path to the application directory */
    private String appPath;
    /**
     * the separator for CSV format fixme: add a string quotation parameter
     */
    private String csvSep = ",";
    /** true to dump headers, false to dump only data */
    private boolean dumpHeader = true;

    /** List of tables that can be exported **/

    private List<String> allowedTablesToExport;

    // --------------------------------------------------------------------------
    // ---
    // --- Init
    // ---
    // --------------------------------------------------------------------------
    public void init(String appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);
        // this.currentExportFormat = params.getValue("exportType");
        this.csvSep = params.getValue("csvSeparator");
        this.dumpHeader = "true".equalsIgnoreCase(params.getValue("dumpHeader"));
        this.allowedTablesToExport = Arrays.asList(params.getValue("allowedTables").split(","));
        this.appPath = appPath;
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------
    /**
     * Physically dumps the given table, writing it to the App tmp folder, returning the URL of the file to get.
     */
    @Override
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        String tableToExport = Util.getParam(params, "tableToExport");

        if (tableToExport == null) {
            if (Log.isDebugEnabled(Geonet.SEARCH_LOGGER))
                Log.debug(Geonet.SEARCH_LOGGER, "Export Statistics table: no table name received from the client.");
        }

        if (!allowedTablesToExport.contains(tableToExport)) {
            throw new BadParameterEx("tableToExport", tableToExport);
        }

        // file to write
        File tableDumpFile = new File(appPath + File.separator + "images" + File.separator + "statTmp");
        IO.mkdirs(tableDumpFile, "Statistics temp directory");

        String dumpFileName = tableToExport + "_" + context.getUserSession().getUserId() + ".csv";
        tableDumpFile = new File(tableDumpFile.getAbsolutePath(), dumpFileName);
        if (Log.isDebugEnabled(Geonet.SEARCH_LOGGER))
            Log.debug(Geonet.SEARCH_LOGGER, "Export Statistics table: will dump CSV to file: " + tableDumpFile);

        // sql stuff
        String query = "select * from " + tableToExport;
        if (Log.isDebugEnabled(Geonet.SEARCH_LOGGER))
            Log.debug(Geonet.SEARCH_LOGGER, "Export Statistics table: query to get table:\n" + query);
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        // use connection by hand, to allow us to control the resultset and avoid Java Heap Space Exception
        Connection con = dbms.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        FileOutputStream fileOutputStream = null;
        BufferedWriter out = null;
        try {
            stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(query);
            fileOutputStream = new FileOutputStream(tableDumpFile);
            out = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));
            ResultSetMetaData rsMetaData = rs.getMetaData();

            if (this.dumpHeader) {
                StringBuilder headers = new StringBuilder();
                for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                    headers.append(rsMetaData.getColumnName(i)).append(this.csvSep);
                }
                // removes trailing separator
                headers.deleteCharAt(headers.length() - 1);
                out.write(headers.toString());
                out.newLine();
            }
            StringBuilder line = null;
            if (Log.isDebugEnabled(Geonet.SEARCH_LOGGER))
                Log.debug(Geonet.SEARCH_LOGGER, "Export Statistics table: headers written, writting data");
            while (rs.next()) {
                line = new StringBuilder();
                for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                    line.append(rs.getString(i)).append(this.csvSep);
                }
                line.deleteCharAt(line.length() - 1);
                out.write(line.toString());
                out.newLine();
            }
            out.flush();
            if (Log.isDebugEnabled(Geonet.SEARCH_LOGGER))
                Log.debug(Geonet.SEARCH_LOGGER, "data written");
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(fileOutputStream);
            IO.closeQuietly(rs);
            IO.closeQuietly(stmt);
        }
        // dbms.disconnect();
        if (Log.isDebugEnabled(Geonet.SEARCH_LOGGER))
            Log.debug(Geonet.SEARCH_LOGGER, "streams closed");

        Element elResp = new Element(Jeeves.Elem.RESPONSE);
        Element elFileUrl = new Element("fileURL").setText(context.getBaseUrl() + "/images/statTmp/" + dumpFileName);
        Element elExportedtable = new Element("exportedTable").setText(tableToExport);
        elResp.addContent(elFileUrl);
        elResp.addContent(elExportedtable);
        return elResp;
    }
}
