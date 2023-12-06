package beats.babel.babelbeats;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.net.URI;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class GeniusHandler {
    private static String accessToken;

    public GeniusHandler() {
        if (accessToken == null) {
            JSONExtractor JSONe = new JSONExtractor();
            String[] keys = new String[]{"accessToken"};
            String[] credentials = JSONe.readFromFile("src/main/resources/geniusCredentials.json", keys);
            accessToken = credentials[0];
        }
    }

    public String getLyrics(String query)
    {
        String search = searchGenius(query);
        JSONObject results = getResults(search);
        String url = getUrl(results.toString());
        WebScraper scrape = new WebScraper(6000);
        return formatText(scrape.scrapeLyrics(url), '[', ']', 2);
    }

    private String searchGenius(String query)
    {
        try
        {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String uri = "https://api.genius.com/search?q=" + encodedQuery;
            String[] header = new String[]{"Authorization"};
            String[] headerValues = new String[]{"Bearer " + accessToken};
            return sendHTTPRequest(uri, header, headerValues, "GET", "");
        }
        catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
            return null;
        }
    }

    private String formatText(String text, char startSign, char stopSign, int maxEndl) {
        int newLineCount = 0;
        StringBuilder formattedText = new StringBuilder();
        boolean removeCycle = false;
        for (char character : text.toCharArray()) {
            if (!removeCycle) {
                if (character == startSign)
                    removeCycle = true;
                if (character == '\n')
                    newLineCount += 1;
                else
                    newLineCount = 0;
                if (newLineCount < maxEndl && !removeCycle)
                    formattedText.append(character);
            }
            else if(character == stopSign) {
                removeCycle = false;
            }
        }
        return formattedText.toString();
    }

    private JSONObject getResults(String JSON_string)
    {
        JSONObject file = new JSONObject(JSON_string);
        JSONObject response = file.getJSONObject("response");
        JSONArray hits =response.getJSONArray("hits");
        return hits.getJSONObject(0);
    }

    private String getUrl(String JSON_file)
    {
        JSONObject file = new JSONObject(JSON_file);
        JSONObject result = file.getJSONObject("result");
        return result.getString( "url");
    }


    private String searchSong(String id)
    {
            String uri = "https://api.genius.com/songs/" + id;
            String[] header = new String[]{"Authorization"};
            String[] headerValues = new String[]{"Bearer " + accessToken};
            return sendHTTPRequest(uri, header, headerValues, "GET", "");

    }
    private String sendHTTPRequest(String url, String[] headerName, String[] headerValue, String type, String parameters) {
        var client = HttpClient.newHttpClient();

        var builder = HttpRequest.newBuilder(
                URI.create(url));
        for (int i = 0; i < headerName.length; i++){
            builder.header(headerName[i], headerValue[i]);
        }
        if (Objects.equals(type, "POST")) {
            builder.POST(HttpRequest.BodyPublishers.ofString(parameters));
        }
        if (Objects.equals(type, "PUT")){
            builder.PUT(HttpRequest.BodyPublishers.ofString(parameters));
        }
        if (Objects.equals(type, "GET")){
            builder.GET();
        }
        HttpRequest request = builder.build();

        try{
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void getLyricsToFile(String query, String language, boolean formatName){
        String lyrics = getLyrics(query);
        JSONObject jo = new JSONObject();

        if(formatName)
            jo.put("title", query.replace(" ", "_"));
        else
            jo.put("title", query);

        jo.put("language", language);
        jo.put("lyrics", lyrics);
        try{
            JSONTokener tokener = new JSONTokener(new FileReader("src/main/resources/lyrics/songs_data.json"));
            JSONArray jsonArray = new JSONArray(tokener);
            jsonArray.put(jo);
            try (FileWriter fileWriter = new FileWriter("src/main/resources/lyrics/songs_data.json")) {
                fileWriter.write(jsonArray.toString(2));
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
