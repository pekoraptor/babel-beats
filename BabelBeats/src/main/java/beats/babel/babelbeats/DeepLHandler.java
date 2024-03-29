package beats.babel.babelbeats;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.deepl.api.*;

public class DeepLHandler {
    private final String authKey;
    private final String url;

    public DeepLHandler(){
//        read authKey and api url from json file
        JSONExtractor je = new JSONExtractor();
        String[] keys = new String[]{"authKey", "url"};
        String[] values = je.readFromFile("src/main/resources/deeplCredentials.json", keys);
        this.authKey = values[0];
        this.url = values[1];
    }

    public List<String> translate(List<String> text, String targetLang, String srcLang){
//        construct api call parameters
//        String parameters = "{\"text\": [\"" + text + "\"], \"target_lang\":\"" + targetLang + "\", \"source_lang\": \"" + srcLang + "\", \"split_sentences\": \"1\", \"preserve_formatting\": true, \"context\": \"text of a song\"}";
//        String parameters = "{'text': ['" + text + "'], 'target_lang':'" + targetLang + "', 'source_lang': '" + srcLang + "', 'split_sentences': '1', 'preserve_formatting': true, 'context': 'text of a song'}";

//        String parameters = "{\"text\": [\"" + text + "\"], 'target_lang': '" + targetLang + "', 'source_lang': '" + srcLang + "', \"split_sentences\": \"1\", 'preserve_formatting': true, 'context': 'text of a song'}";

//        create http client and build http request
//        var client = HttpClient.newHttpClient();
//        var builder = HttpRequest.newBuilder(
//                URI.create(url));
//        builder.header("Authorization", "DeepL-Auth-Key " + authKey);
//        builder.header("Content-Type", "application/json");
//        builder.POST(HttpRequest.BodyPublishers.ofString(parameters));
//        HttpRequest request = builder.build();
        targetLang = Objects.equals(targetLang, "en") ? "en-US" : targetLang;
//        srcLang = Objects.equals(srcLang, "en") ? "en-US" : srcLang;

        TextTranslationOptions tto = new TextTranslationOptions();
        Translator translator = new Translator(authKey);
        tto.setContext("text of a song");
        tto.setPreserveFormatting(true);
        tto.setSentenceSplittingMode(SentenceSplittingMode.All);
        try {
            List<TextResult> results = translator.translateText(text, srcLang, targetLang, tto);
            List<String> translated = new ArrayList<>();
            for (TextResult result : results) {
                translated.add(result.getText());
            }
            return translated;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

//        try {
////            finally call api
//            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//            String responseJson =  response.body();
//
////            extract translated text from the api response and return it
//            JSONExtractor je = new JSONExtractor();
//            JSONArray translations = je.extractList(responseJson, "translations");
//            JSONObject translation = translations.getJSONObject(0);
//            return translation.getString("text");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
    }
}
