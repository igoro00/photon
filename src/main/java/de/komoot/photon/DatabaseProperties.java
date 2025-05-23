package de.komoot.photon;

import java.util.*;

/**
 * Class collecting database global properties.
 *
 * The server is responsible for making the data persistent in the Photon database.
 */
public class DatabaseProperties {
    private String[] languages;
    private Date importDate;
    private boolean supportStructuredQueries;
    private boolean supportGeometries;

    public DatabaseProperties(String[] languages, Date importDate, boolean supportStructuredQueries, boolean supportGeometries) {
        this.languages = languages;
        this.importDate = importDate;
        this.supportStructuredQueries = supportStructuredQueries;
        this.supportGeometries = supportGeometries;
    }

    public DatabaseProperties() {

    }


    /**
     * Return the list of languages for which the database is configured.
     * If no list was set, then the default is returned.
     *
     * @return List of supported languages.
     */
    public String[] getLanguages() {
        if (languages == null) {
            return new String[]{"en", "de", "fr", "it"};
        }

        return languages;
    }

    /**
     * Replace the language list with the given list.
     *
     * @param languages Array of two-letter language codes.
     *
     * @return This object for function chaining.
     */
    public DatabaseProperties setLanguages(String[] languages) {
        this.languages = languages;
        return this;
    }

    /**
     * Set language list to the intersection between the existing list and the given list.
     *
     * The final list will use the same order as the given list.
     *
     * @param languageList Comma-separated list of two-letter language codes.
     */
    public void restrictLanguages(String[] languageList) {
        if (languages == null) {
            // Special case for versions that did not yet have a language list set
            // in the database: Use the given list as is.
            languages = languageList;
        } else {
            Set<String> currentLanguageSet = new HashSet<>(Arrays.asList(languages));
            List<String> newLanguageList = new ArrayList<>();

            for (String lang : languageList) {
                if (currentLanguageSet.contains(lang)) {
                    newLanguageList.add(lang);
                }
            }

            if (newLanguageList.isEmpty()) {
                throw new UsageException("Language list '" + Arrays.toString(languageList) +
                        "' not compatible with languages in database(" + Arrays.toString(languages) + ")");
            }

            languages = newLanguageList.toArray(new String[]{});
        }
    }

    public Date getImportDate() {
        return this.importDate;
    }

    public DatabaseProperties setImportDate(Date importDate) {
        this.importDate = importDate;
        return this;
    }

    public boolean getSupportStructuredQueries() {
        return supportStructuredQueries;
    }

    public DatabaseProperties setSupportStructuredQueries(boolean supportStructuredQueries) {
        this.supportStructuredQueries = supportStructuredQueries;
        return this;
    }

    public boolean getSupportGeometries() {
        return supportGeometries;
    }

    public DatabaseProperties setSupportGeometries(boolean supportGeometries) {
        this.supportGeometries = supportGeometries;
        return this;
    }

    @Override
    public String toString() {
        return "DatabaseProperties{" +
                "languages=" + Arrays.toString(languages) +
                ", importDate=" + importDate +
                ", supportStructuredQueries=" + supportStructuredQueries +
                ", supportGeometries=" + supportGeometries +
                '}';
    }
}
