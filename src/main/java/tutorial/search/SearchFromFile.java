package tutorial.search;


import org.apache.lucene.queryparser.classic.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;
import tutorial.QueryStruct;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class SearchFromFile {
    public static void main(String[] args) throws IOException, ParseException {


        File file = new File(args[0]);
        InputStream instream = new FileInputStream(file);
        Scanner s = new Scanner(instream).useDelimiter("\\A");
        String fileText = s.hasNext() ? s.next() : "";
        JSONObject obj = new JSONObject(fileText);
        Map<String, String> fields = getFields(obj);
        List<QueryStruct> queries = getQueries(obj.getJSONArray("queries"));
        ImprovedSearcher improvedSearcher = new ImprovedSearcher(fields.get("index"), fields.get("scorer"), Integer.parseInt(fields.get("requested")));
        for(QueryStruct query: queries) {
            improvedSearcher.search(query);
        }
    }

    static public Map<String, String> getFields(JSONObject jsonObject) {
        Map<String, String> map = new HashMap<>();
        map.put("index", jsonObject.getString("index"));
        map.put("requested", String.valueOf(jsonObject.getInt("requested")));
        map.put("scorer", jsonObject.getString("scorer"));

        return map;
    }

    static public List<QueryStruct> getQueries(JSONArray queriesJson) {
        List<QueryStruct> queries = new ArrayList<>();
        for (int i = 0; i < queriesJson.length(); i++) {
            JSONObject jsonObject = queriesJson.getJSONObject(i);
            String query = jsonObject.getString("text");
            if(query.contains("#combine(")){
                query = query.replace("#combine(","");
                query = query.substring(0,query.length()-1);
            }
            QueryStruct queryStruct = new QueryStruct(jsonObject.getString("number"),query);
            queries.add(queryStruct);
        }
        return queries;
    }
}
