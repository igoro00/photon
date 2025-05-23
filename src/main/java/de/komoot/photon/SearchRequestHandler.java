package de.komoot.photon;

import de.komoot.photon.query.BadRequestException;
import de.komoot.photon.query.PhotonRequest;
import de.komoot.photon.query.PhotonRequestFactory;
import de.komoot.photon.searcher.*;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.RouteImpl;

import java.util.Arrays;
import java.util.List;

import static spark.Spark.halt;

/**
 * Webserver route for forward geocoding requests.
 */
public class SearchRequestHandler extends RouteImpl {
    private final PhotonRequestFactory photonRequestFactory;
    private final SearchHandler requestHandler;
    private final boolean supportGeometries;

    SearchRequestHandler(String path, SearchHandler dbHandler, String[] languages, String defaultLanguage, int maxResults, boolean supportGeometries) {
        super(path);
        List<String> supportedLanguages = Arrays.asList(languages);
        this.photonRequestFactory = new PhotonRequestFactory(supportedLanguages, defaultLanguage, maxResults, supportGeometries);
        this.requestHandler = dbHandler;
        this.supportGeometries = supportGeometries;
    }

    @Override
    public String handle(Request request, Response response) throws BadRequestException {
        PhotonRequest photonRequest = null;
        try {
            photonRequest = photonRequestFactory.create(request);
        } catch (BadRequestException e) {
            JSONObject json = new JSONObject();
            json.put("message", e.getMessage());
            throw halt(e.getHttpStatus(), json.toString());
        }

        if (!supportGeometries && photonRequest.getReturnGeometry()) {
            JSONObject json = new JSONObject();
            json.put("message", "You're explicitly requesting a geometry, but geometries are not imported!");
            throw halt(400, json.toString());
        }

        List<PhotonResult> results = requestHandler.search(photonRequest);

        // Further filtering
        results = new StreetDupesRemover(photonRequest.getLanguage()).execute(results);

        // Restrict to the requested limit.
        if (results.size() > photonRequest.getLimit()) {
            results = results.subList(0, photonRequest.getLimit());
        }

        String debugInfo = null;
        if (photonRequest.getDebug()) {
            debugInfo = requestHandler.dumpQuery(photonRequest);
        }

        return new GeocodeJsonFormatter(photonRequest.getDebug(), photonRequest.getLanguage(), photonRequest.getReturnGeometry()).convert(results, debugInfo);
    }
}