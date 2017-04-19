package edu.temple.gymminder.geofence;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import edu.temple.gymminder.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class GeofenceFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public GeofenceFragment() {
        // Required empty public constructor
    }

    private static final int DEFAULT_RADIUS = 100;
    private static final int PLACES_ACTIVITY_RESULT = 96;
    private GoogleApiClient mGoogleApiClient;

    private EditText mLatitudeEditText;
    private EditText mLongitudeEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_geofence, container, false);
        mLatitudeEditText = (EditText) v.findViewById(R.id.latitudeEditText);
        mLongitudeEditText = (EditText) v.findViewById(R.id.longitudeEditText);
        v.findViewById(R.id.getLocationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivityForResult(new PlacePicker.IntentBuilder().build(getActivity()),
                            PLACES_ACTIVITY_RESULT);
                } catch (GooglePlayServicesRepairableException
                        | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
        v.findViewById(R.id.finishGeofenceButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mGoogleApiClient.isConnected()) return;
                String latitude = mLatitudeEditText.getText().toString();
                String longitude = mLongitudeEditText.getText().toString();
                String radius = ((EditText) v.findViewById(R.id.radiusEditText)).getText().toString();
                if(latitude.length()<1 || longitude.length()<1){
                    Toast.makeText(getContext(), "Please enter valid LatLng data", Toast.LENGTH_SHORT).show();
                    return;
                }
                radius = radius.length() > 0 ? radius : "100";
                try {
                    startGeofencing(Double.parseDouble(latitude), Double.parseDouble(longitude),
                            Float.parseFloat(radius));
                } catch (NumberFormatException e){
                    Toast.makeText(getContext(), "Please enter valid LatLng data", Toast.LENGTH_SHORT).show();
                }
            }
        });
        requestPermissions(new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        connectToPlayServices();
        return v;
    }

    private void connectToPlayServices(){
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    private void startGeofencing(double latitude, double longitude, float radius) {
        Intent intent = new Intent(getContext(), GeofenceStarterService.class);
        intent.putExtra(GeofenceStarterService.LATITUDE_EXTRA, latitude);
        intent.putExtra(GeofenceStarterService.LONGITUDE_EXTRA, longitude);
        intent.putExtra(GeofenceStarterService.RADIUS_EXTRA, radius);
        getActivity().startService(intent);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==PLACES_ACTIVITY_RESULT){
            if(resultCode== Activity.RESULT_OK){
                LatLng latLng = PlacePicker.getPlace(getContext(), data).getLatLng();
                startGeofencing(latLng.latitude, latLng.longitude, DEFAULT_RADIUS);
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("Google API", "Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("Google API", "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("Google API", "Failed to connect");
    }

}
