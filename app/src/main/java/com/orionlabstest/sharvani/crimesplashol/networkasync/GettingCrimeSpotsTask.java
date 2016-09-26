package com.orionlabstest.sharvani.crimesplashol.networkasync;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.orionlabstest.sharvani.crimesplashol.R;
import com.orionlabstest.sharvani.crimesplashol.models.LocationItem;
import com.orionlabstest.sharvani.crimesplashol.models.OwnIconRendered;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by Sharvani on 9/24/16.
 */
public class GettingCrimeSpotsTask extends AsyncTask<String, Void, JSONObject> {
    private Context context;
    private ClusterManager<LocationItem> mClusterManager;
    GoogleMap mMap;
    String district;
    int offset;
    Calendar cal = Calendar.getInstance();
    String thirtyDaysBack;
    String mGeoJsonUrl;
    private final static String mLogTag = "GeoJsonDemo";
    int[] rainbow;
    int[] crime_markers;
    int[] crime_markers_colors;
    HashMap<String, Integer> crimeMap = new HashMap();
    ArrayList<PolygonOptions> polylineOptionsList = new ArrayList<>();
    LinkedList<String> sortedDistrictList = new LinkedList<>();
    ArrayList<LocationItem> items = new ArrayList<>();

    public GettingCrimeSpotsTask(Context context, ClusterManager<LocationItem> clusterManager, GoogleMap mMap, String district, int offset) {
        this.context = context;
        mClusterManager = clusterManager;
        this.mMap = mMap;
        this.district = district;
        this.offset = offset;
        rainbow = context.getResources().getIntArray(R.array.rainbow);
        mClusterManager.setRenderer(new OwnIconRendered(context, mMap, mClusterManager));
        crime_markers = context.getResources().getIntArray(R.array.crime_markers);
        crime_markers_colors = context.getResources().getIntArray(R.array.crime_markers_colors);
        cal.add(Calendar.DATE, -30);
        thirtyDaysBack = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(cal.getTime());
        mGeoJsonUrl = context.getResources().getString(R.string.geojson);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mMap.clear();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        String[] polygons = context.getResources().getStringArray(R.array.points_array);
        ArrayList<LatLng> coordList = new ArrayList<LatLng>();
        int f = 0;
        for (String polygonArray : polygons) {
            String[] parts = polygonArray.split(",");
            for (String s : parts) {
                s = s.trim();
                String[] points = s.split(" ");
                double dbl = Double.parseDouble(points[1]);
                double dbl2 = Double.parseDouble(points[0]);
                coordList.add(new LatLng(dbl, dbl2));
            }
            PolygonOptions polylineOptions = new PolygonOptions();
            polylineOptions.addAll(coordList);
            coordList.clear();
            polylineOptions
                    .strokeWidth(4)
                    .strokeColor(Color.BLACK)
                    .fillColor(rainbow[f]);
            polylineOptionsList.add(polylineOptions);
            f++;
        }


        try {

            URL url;
            if (district.equals("")) {
                url = new URL(mGeoJsonUrl);
            } else if (offset >= 100) {
                url = new URL(mGeoJsonUrl + "?pddistrict=" + district + "&$limit=100&$offset=" + offset);

            } else {
                url = new URL(mGeoJsonUrl + "?pddistrict=" + district + "&$limit=100&$offset=0");
            }
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("X-App-Token", "JYVszizcAqLkNTvz10G1jbqpz");
            // Open a stream from the URL
            InputStream stream = url.openStream();
            String line;
            StringBuilder result = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            while ((line = reader.readLine()) != null) {
                // Read and save each line of the stream
                result.append(line);
            }

            // Close the stream
            reader.close();
            stream.close();


            JSONArray array;

            array = new JSONArray(result.toString());

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = (JSONObject) array.get(i);
                String latitude = obj.optString("y");
                String longitude = obj.optString("x");
                String date = obj.optString("date");
                String district = obj.optString("pddistrict");
                if (!crimeMap.containsKey(district)) {
                    crimeMap.put(district, 1);
                } else {
                    Integer integer = crimeMap.get(district);
                    crimeMap.put(district, integer + 1);
                }


                double lat = Double.parseDouble(latitude);
                double longi = Double.parseDouble(longitude);

                //API not responding to Date queries

               /* DateFormat formatter;
                Date date_format = new Date();

                formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                try {
                    date_format = formatter.parse(date);

                } catch (ParseException e) {
                    e.printStackTrace();
                }*/
                // if (date_format.after(formatter.parse(thirtyDaysBack))) {

                LocationItem offsetItem = new LocationItem(lat, longi);
                offsetItem.addDistrict(district);
                items.add(offsetItem);
                // }

            }


        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();


        } catch (IOException e) {
            e.printStackTrace();
            Log.e(mLogTag, "GeoJSON file could not be read");

        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        LatLng sfo = new LatLng(37.773972, -122.431297);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sfo, 10));
        sortMap();
        addMarkers();
        for (PolygonOptions opt : polylineOptionsList) {
            Polygon p = mMap.addPolygon(opt);
        }

    }

    private void addMarkers() {
        for (LocationItem item : items) {
            int position = sortedDistrictList.indexOf(item.getDistrict());
            if (position <= 6) {
                item.setHue(crime_markers[position]);
                item.setColor(crime_markers_colors[position]);
            } else {
                item.setHue(crime_markers[7]);
                item.setColor(crime_markers_colors[7]);

            }
            mClusterManager.addItem(item);

        }

    }

    private void sortMap() {
        Set<Map.Entry<String, Integer>> set = crimeMap.entrySet();
        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(
                set);
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        for (Map.Entry<String, Integer> entry : list) {
            sortedDistrictList.add(entry.getKey());
        }
    }


}
