package com.example.test;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    private ArrayList<String> resultList;
    private static final String LOG_TAG = "waca";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String PLACES_API_BASE2 = "https://maps.googleapis.com/maps/api/geocode";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyByEWZEQxPfCFMwekHfoFShHWNIq3NwVEc";
    
    public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }
    
    @Override
    public int getCount() {
    	if (resultList != null){
        return resultList.size();
    	}
    	return 0;
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    resultList = autocomplete(constraint.toString());
                    
                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }
    private ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;
        
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);//https://maps.googleapis.com/maps/api/place/autocomplete/json
            sb.append("?sensor=false&key=" + API_KEY);
            sb.append("&components=country:cl");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API (autocomplete)", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
           // Log.d(LOG_TAG, jsonObj.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        
        return resultList;
    }
    public LatLng getLanLong(String input) {
    	 LatLng latlng= null;
        
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/geocode/json");
            
           
            sb.append("?address=" + URLEncoder.encode(input.replaceAll(" ", "+"), "utf8"));
            sb.append("&sensor=true&oe=utf-8");
            Log.d(LOG_TAG, sb.toString());
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return latlng;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API (getlanlong)", e);
            return latlng;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());            
            JSONArray a = jsonObj.getJSONArray("results");
            JSONObject b = a.getJSONObject(0);
            JSONObject c = b.getJSONObject("geometry");
            JSONObject d = c.getJSONObject("location");

            latlng= new LatLng (Double.parseDouble(d.getString("lat")),Double.parseDouble(d.getString("lng")));
            
            
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        
      
	   return latlng;
    }

	

}