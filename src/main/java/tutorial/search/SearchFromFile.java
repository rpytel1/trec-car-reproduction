package tutorial.search;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SearchFromFile {
    public static void main(String[] args) throws FileNotFoundException {


        File file = new File("/Users/rafalpytel/Desktop/TU Delft/Q3&Q4/Information Retriveal/project/trec-car-reproduction/test200_queries.json");
        InputStream instream = new FileInputStream(file);
        Scanner s = new Scanner(instream).useDelimiter("\\A");
        String fileText = s.hasNext() ? s.next() : "";
        JSONObject obj = new JSONObject(fileText);
        Map<String, String> fields = getFields(obj);

    }
    static public Map<String, String> getFields(JSONObject jsonObject){
        Map<String,String> map = new HashMap<>();
        map.put("index",jsonObject.getString("index"));
        map.put("requested",String.valueOf(jsonObject.getInt("requested")));
        map.put("scorer",jsonObject.getString("scorer"));

        return map;
    }
}
