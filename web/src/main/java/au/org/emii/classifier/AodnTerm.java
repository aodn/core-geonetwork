package au.org.emii.classifier;

/**
 * An AODN vocab term
 */

public class AodnTerm {
    private String uri;
    private String prefLabel;
    private String altLabel;
    private String displayLabel;

    public AodnTerm(String uri, String prefLabel, String altLabel, String displayLabel) {
        this.uri = uri;
        this.prefLabel = prefLabel;
        this.altLabel = altLabel;
        this.displayLabel = displayLabel;
    }

    public String getUri() {
        return uri;
    }

    public String getPrefLabel() {
        return prefLabel;
    }

    public String getAltLabel() {
        return altLabel;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    public String getCategoryLabel() {
        if (displayLabel != null) {
            return displayLabel;
        } else if (altLabel != null) {
            return altLabel;
        } else {
            return prefLabel;
        }
    }
}
