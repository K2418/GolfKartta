package com.example.kaupp.golfmap;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    protected JSONArray kentat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //Fetch Json data
        FetchDataTask task = new FetchDataTask();
        task.execute("http://student.labranet.jamk.fi/~K2418/ttms0500-harkat/h05/kentat/data/kentat.json");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        //Set my custom infoWindow to map
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //Focus map on JKL
        LatLng ICT = new LatLng(62.2416223, 25.7597309);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ICT, 6));

    }

    // Custom info window because all info wasn't shown on original implentation
    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyInfoWindowAdapter(){
            myContentsView = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoContents(Marker marker) {

            TextView tvTitle = ((TextView)myContentsView.findViewById(R.id.title));
            tvTitle.setText(marker.getTitle());
            TextView tvSnippet = ((TextView)myContentsView.findViewById(R.id.snippet));
            tvSnippet.setText(marker.getSnippet());

            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

    }

    //Adds markers to map according to info sent
    public void SetMarker(LatLng coords, String title, String snip, String type){

        BitmapDescriptor icon;
        Log.v("Type",type);

        if(type.equals("Kulta")){
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
        }
        else if(type.equals("Etu")){
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        }
        else if(type.equals("Kulta/Etu")){
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE);
        }
        else{
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        }

        final Marker golfField = mMap.addMarker(new MarkerOptions()
                .position(coords)
                .title(title)
                .snippet(snip)
                .icon(icon));
    }

    class FetchDataTask extends AsyncTask<String, Void, JSONObject> {
        //Background loading JSON from url sent to class
        @Override
        protected JSONObject doInBackground(String... urls) {
            HttpURLConnection urlConnection = null;
            JSONObject json = null;
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                json = new JSONObject(stringBuilder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }
            return json;
        }
        // After loading is complete, send info to Gmaps to set markers
        protected void onPostExecute(JSONObject json) {
            try {
                kentat = json.getJSONArray("kentat");
                for (int i = 0; i < kentat.length(); i++){
                    JSONObject kentta = kentat.getJSONObject(i);
                    double lat = kentta.getDouble("lat");
                    double lon = kentta.getDouble("lng");
                    String title = kentta.getString("Kentta");
                    String snippet = "Address: " + kentta.getString("Osoite") + "\n" +
                            "Phone: " + kentta.getString("Puhelin") + "\n" +
                            "email: " + kentta.getString("Sahkoposti") + "\n" +
                            "WWW: " + kentta.getString("Webbi");
                    LatLng kenttaCoord = new LatLng(lat, lon);
                    String type = kentta.getString("Tyyppi");

                    SetMarker(kenttaCoord, title, snippet, type);
                }
            } catch (JSONException e) {
                Log.e("JSON", "Error getting data.");
            }
        }
    }
}
