package com.orionlabstest.sharvani.crimesplashol.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.maps.android.clustering.ClusterManager;
import com.orionlabstest.sharvani.crimesplashol.R;
import com.orionlabstest.sharvani.crimesplashol.models.LocationItem;
import com.orionlabstest.sharvani.crimesplashol.networkasync.GettingCrimeSpotsTask;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ClusterManager<LocationItem> mClusterManager;
    SearchView searchView;
    LinearLayout paginationLayout;
    Button prevBtn,nextBtn;
    int next=0;
    String district;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setActionBar();
        setUIHandles();
        setListeners();

    }

    private void setUIHandles() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setQueryHint(getResources().getString(R.string.searchText));
        paginationLayout= (LinearLayout) findViewById(R.id.pagination);
        prevBtn= (Button) findViewById(R.id.prev);
        nextBtn= (Button) findViewById(R.id.next);
    }

    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.mipmap.icon);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    private void setListeners() {
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevBtn.setVisibility(View.VISIBLE);
                next=next+100;
             makeNetWorkCall(district,next);
                Toast.makeText(getApplicationContext(),"Loading next 100 . . . ",Toast.LENGTH_SHORT).show();
            }
        });
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            next=next-100;
                makeNetWorkCall(district,next);
                Toast.makeText(getApplicationContext(),"Loading Previous 100 . . . ",Toast.LENGTH_SHORT).show();

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(getBaseContext(), query,
                        Toast.LENGTH_SHORT).show();
                searchView.onActionViewCollapsed();
                district=query.trim().toUpperCase();
                makeNetWorkCall(district,0);
                paginationLayout.setVisibility(View.VISIBLE);
                prevBtn.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                makeNetWorkCall("",0);
                return true;

            default:
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
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
        makeNetWorkCall("",0);
    }

    public void makeNetWorkCall(String district,int offset){
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mClusterManager = new ClusterManager<LocationItem>(getApplicationContext(), mMap);
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        GettingCrimeSpotsTask task = new GettingCrimeSpotsTask(this, mClusterManager, mMap,district,offset);
        task.execute();
    }
}
