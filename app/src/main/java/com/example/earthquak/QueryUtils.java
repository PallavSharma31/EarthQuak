package com.example.earthquak;

import android.text.TextUtils;
import android.util.Log;

import com.example.earthquak.Earthquake;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Helper methods related to requesting and receiving earthquake data from USGS.
 */
public final class QueryUtils {


    /** Tag for the log messages */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();


    private QueryUtils() {
    }


    /**
     * Query the USGS dataset and return a list of {@link Earthquake} objects.
     */
    public static List<Earthquake> fetchEarthquakeData(String requestUrl) {

       //create URL object
        URL url=createUrl(requestUrl);

        //perform a http request to the url and recieve a JSON response back
        String jsonResponse=null;

        try {
            jsonResponse=makeHttpRequest(url);
        }
        catch (IOException e) {
          Log.e(LOG_TAG,"Problem making the HTTP request.",e);
        }
        //Extract the relevant field from the JSON response and create a list of earthquakes
        List<Earthquake> earthquakes=extractFeaturesFromJson(jsonResponse);

        // Return the list of earthquakes
        return earthquakes;
    }
    private static URL createUrl(String stringUrl) {
        URL url=null;
        try {
            url=new URL(stringUrl);
        }
        catch (MalformedURLException e) {
            Log.e(LOG_TAG,"Problem building the Url");
        }
        return url;
    }
//    make a HTTP request to the given URL and return a String as the responese

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse="";
        //if the url is null then return early
        if(url==null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection=null;
        InputStream inputStream=null;
        try {
            urlConnection= (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* millisecond */);
            urlConnection.setConnectTimeout(15000 /* millisecond */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //if the request was successful (response code 200),
            //the read the input stream and parse the response
            if(urlConnection.getResponseCode()==200) {
                inputStream=urlConnection.getInputStream();
                jsonResponse=readFromStream(inputStream);
                //the input stream is in the form of bits so we need to conver it into string
            }
            else {
                Log.e(LOG_TAG,"Error response cone: "+urlConnection.getResponseCode());
            }
        } catch (IOException e ){
            Log.e(LOG_TAG,"Problem retriving the earthquake Json result.",e);
        }
        finally {
            if(urlConnection!=null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output=new StringBuilder();
        if(inputStream!=null) {
            InputStreamReader inputStreamReader=new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader=new BufferedReader(inputStreamReader);
            String line=reader.readLine();
            while (line!=null) {
                output.append(line);
                line=reader.readLine();
            }
        }
        return output.toString();
    }
    /**
     * Return a list of {@link Earthquake} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<Earthquake> extractFeaturesFromJson(String earthquakeJSON) {
        //if the JSON string is empty or null,then return early.
        if(TextUtils.isEmpty(earthquakeJSON)) {
            return null;
        }

        //create a empty ArrayList that we can start adding earthquak to
        List<Earthquake> earthquakes=new ArrayList<>();


        //Try to parse the JSON response string.if there a problem with the way the JSON
        //is formatted ,a JSONException object will be thrown.
        //catch the exception so the app doesn't crash,and print the erro message to the logs.

        try {
            //create a JSON object from the JSON respnse string
            JSONObject baseJsonResponse=new JSONObject(earthquakeJSON);

            //Extract the JSoNArray associated with the key called features
            //which represent the list of the features(or earthQuake)
            JSONArray earthquakeArray=baseJsonResponse.getJSONArray("features");

            //For each earthquake in the earthquakeArray,create a earthquake objec
            for(int i=0;i<earthquakeArray.length();i++) {

                //Get a single earthquake at the position i within the list of earthquake
                JSONObject currentEarthquake =earthquakeArray.getJSONObject(i);

                // For a given earthquake, extract the JSONObject associated with the
                // key called "properties", which represents a list of all properties
                // for that earthquake.
                JSONObject properties=currentEarthquake.getJSONObject("properties");

                //extrac the value for the key called "mag"
                double magnitude=properties.getDouble("mag");

                //extract the value for the key called "place"
                String location=properties.getString("place");

                //extract the value for the key called time
                long time=properties.getLong("time");

                //extrac the value for the key called "url"
                String url=properties.getString("url");

                // Create a new {@link Earthquake} object with the magnitude, location, time,
                // and url from the JSON response.
                Earthquake earthquake = new Earthquake(magnitude, location, time, url);

                // Add the new {@link Earthquake} to the list of earthquakes.
                earthquakes.add(earthquake);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }
        //return the list of the earthquakes
        return  earthquakes;
    }
}