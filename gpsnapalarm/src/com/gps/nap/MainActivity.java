package com.gps.nap;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.gps.nap.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements 

OnMapClickListener,
OnMarkerDragListener,
OnMarkerClickListener,
OnItemClickListener,
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, LocationSource { 

	private NotificationManager notificationManager;
	private GoogleMap map;
	private OnLocationChangedListener mListener;
	private Marker markerRadio;
	private Marker markerPhone;
	private int radio;
	private Circle circle;
	private SharedPreferences pref;
	private Editor editor;
	private MediaPlayer mMediaPlayer;
	private Vibrator mVibrator;
	private Notification notification;
	private boolean twoLocations=false;//bloquear automover camara
	private boolean firstLocation;//hacer un zoom la primera vez que encuentra la localizacion
	private boolean alarmZone;
	private boolean home=true;
	 
	boolean mUpdatesRequested = false;
	private LocationClient mLocationClient;
	private LocationRequest mLocationRequest;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
    { 	 //Toast.makeText(this, "onCreated", Toast.LENGTH_SHORT).show();
         
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        
        pref = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);  
        editor = pref.edit();
        editor.putBoolean("alarma", false).commit();
        editor.putBoolean("activada", false).commit();
        editor.putBoolean("sonido", true).commit(); 
        editor.putBoolean("vibrar", true).commit();
        firstLocation=true;
     
        
        setUpMap();
        if (servicesConnected()) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(5000);//5 seg
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(1000);
        mUpdatesRequested = true;
        mLocationClient = new LocationClient(this, this, this);
        }
        
        AutoCompleteTextView editTextAddress = (AutoCompleteTextView)findViewById(R.id.autocomplete);
        editTextAddress.setAdapter(new AutoCompleteAdapter(this));
        editTextAddress.setOnItemClickListener(this);
        
       /*
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fadein);
        traslateLeft = AnimationUtils.loadAnimation(this, R.anim.traslateleft);
        ImageView lupa = (ImageView) findViewById(R.id.lupa);
        editTextAddress.startAnimation(traslateLeft);
        lupa.startAnimation(traslateLeft );
        */
       // Intent intent = new Intent(Intent.ACTION_VIEW);
        //intent.setData(Uri.parse("market://details?id=com.gps.nap"));
        //startActivity(intent);
    }
	@Override
	protected void onDestroy() 
	{// Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
	  super.onDestroy();
     //System.exit(0); //elimina procesos que quedan corriendo
	     
	}
    @Override
	public void onPause()
	{   //Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();  
    	if(alarmZone){
    	notificationLaunch(getString(R.string.weArrived),getString(R.string.app_name),getString(R.string.stopAlarm), 0);
    	}
    	else if((pref.getBoolean("alarma", false))&&(home)){
    		notificationLaunch(getString(R.string.goToSleep),getString(R.string.app_name),getString(R.string.alarmRunning), 1);
    		}
        super.onPause();
	}
    @Override
	public void onResume()
	{   //Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();
    	super.onResume();
    	
    	notificationManager.cancel(1);
        
    	LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
        	AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
    		myAlertDialog.setTitle(R.string.app_name);
			myAlertDialog.setMessage(R.string.enableGPS);
			myAlertDialog.setIcon(R.drawable.ic_launcher);
        	myAlertDialog.setNeutralButton(R.string.setActiveGPS, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
        		  startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 2);
        	  }});

        	myAlertDialog.show();
        }
		
	
	}
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
        switch(keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
            	if(pref.getBoolean("alarma", false)){
            		//notificationLaunch("La alarma ",getString(R.string.app_name),getString(R.string.alarmRunning), 1);
            		moveTaskToBack(true);
            		}
            	else{
            		notificationManager.cancelAll();
              	    notificationToast(getString(R.string.GPSnapClose), 2);
          		    System.exit(0);
            		
            		}
                return true;
            case KeyEvent.KEYCODE_SEARCH:
            	AutoCompleteTextView searchBar = (AutoCompleteTextView)findViewById(R.id.autocomplete);
            	if (searchBar.getVisibility()==0){searchBar.setVisibility(View.INVISIBLE);}
            	else {searchBar.setVisibility(View.VISIBLE);}
            	
            	
            	return true;	
            
            	
        }
        return false;
    }
    @Override
    public void onStart() {
        super.onStart();
        mLocationClient.connect();
    }
    //---------------------- M E N U -------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
       
		
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	mVibrator.vibrate(50);
	    
	    switch (item.getItemId()) {
	    case R.id.changemap:
	        changeMap();
	        return true;
	    case R.id.myLocation:
	        myLocation();
	        return true;
	    case R.id.myAlarmLocation:
	    	myAlarmlocation();
	    	return true;
	    case R.id.lanzarAcercaDe:
	    	lanzarAcercaDe();
	    	return true;
	    case R.id.allLocations:
	    	twoLocations();
	         return true; 
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	public boolean changeMap() 
	{  
	    switch(map.getMapType())
	    {
	    case 1:
	    	map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
	    	return true;
	    case 4:
	    	map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
	    	return true;
	    }
	return false;	
	}
	public void myLocation() 
	{    twoLocations=true;
	
	    
	       Location location = map.getMyLocation();
			 if(location!=null){
				 
				 map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
						 new LatLng(location.getLatitude(), location.getLongitude()), 
			        		18, 
			        		90,
			        		map.getCameraPosition().bearing)),5000,new CancelableCallback(){

			            @Override
			            public void onFinish()
			            {
			            	 twoLocations=false;
			            }

			            @Override
			            public void onCancel()
			            {
			            	 twoLocations=false; 
			            	
			            }
			        });
			       
				 //Toast.makeText(this, R.string.myLocation, Toast.LENGTH_SHORT).show();
				 }
			 else{ Toast.makeText(this, R.string.noLocation, Toast.LENGTH_SHORT).show();}		 
			
	
		 
	
	}
	public void myAlarmlocation() 
	{     twoLocations=true;
	 	 if (markerPhone!=null){   	 
	 		if(!pref.getBoolean("alarma", false)){
		 map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
				    markerPhone.getPosition(), 
	        		17, 
	        		0,
	        		map.getCameraPosition().bearing)),5000, null);
	 		}else{
	 			 map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
	 				    markerPhone.getPosition(), 
	 	        		17, 
	 	        		90,
	 	        		map.getCameraPosition().bearing)),5000, null);
	 		}
		 
	 	 }
	 	 else{
	 		Toast.makeText(this, R.string.noAlarm, Toast.LENGTH_LONG).show();
	 	 }
	}
	public void twoLocations() 
	{   twoLocations=true;
	    Location location = map.getMyLocation();
	 	
	    if(location==null){
	 		Toast.makeText(this, R.string.noLocation, Toast.LENGTH_SHORT).show();
	 	}
	 	else if(markerPhone==null){
	 		Toast.makeText(this, R.string.noAlarm, Toast.LENGTH_LONG).show();
	 	}
	 	else{   	 
	 		 
	 		 
	 		// Toast.makeText(this, String.valueOf(location.getSpeed()), Toast.LENGTH_SHORT).show();
	 		 LatLngBounds bounds = new LatLngBounds.Builder().include(new LatLng(location.getLatitude(),location.getLongitude())).include(markerPhone.getPosition()).build();
	 		 map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50),5000, null);
	 		
	 	}
	 	
	 	
	}
	//---------------------- M A P -------------------------------------------------------------------
	//---------------------- M A P ---------------------------------------------------------------------
	private void setUpMap() {
        if (map == null) 
        {
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            if (map != null) 
            {  	
            	map.setLocationSource(this);//register LocationSource 
    	    	map.setMyLocationEnabled(true);
    	     	map.setOnMarkerClickListener( this);
    	    	map.setOnMapClickListener(this);
    	    	map.setOnMarkerDragListener(this);
    	    	
    	    	map.getUiSettings().setZoomControlsEnabled(false);
    	    	map.getUiSettings().setCompassEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
    	    	map.getUiSettings().setAllGesturesEnabled(true);
    	    	//startGPS(3,0);
            }  
        }
	}        	//---------------------- G P S ---------------------------------------------------------------------

	//---------------------- M A R K E R  -------------------------------------------------------------------
	//-----------------------MARKER RELOJ---------------------------------------------------------------
 	public void createAlarmMarker(LatLng latLng) {
 		LatLng latLng2;
 		map.clear();
 		  if(!pref.getBoolean("alarma", false)){
 		 map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
 				latLng, 
 				map.getCameraPosition().zoom,
	        		0,
	        		map.getCameraPosition().bearing)),5000, null);   
 		  }
 		  else{
 			 map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
 	 				latLng, 
 	 				map.getCameraPosition().zoom, 
 		        		90,
 		        		map.getCameraPosition().bearing)),5000, null); 
 			  
 		  }
//marker phone
		MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        if(pref.getBoolean("alarma", false)){ 
        	if(pref.getBoolean("sonido", false)&&pref.getBoolean("vibrar", false)){markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.phone_red_a_v));}
        	else if(pref.getBoolean("sonido", false)){markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.phone_red_a));}
        	else{markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.phone_red_v));}
        } 
        else{ markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.phone_blue));}
        markerPhone =  map.addMarker(markerOptions);      
       
		//marker radio
        if(radio==0){ //if radio =0 means crete a new marker
        	Point point = map.getProjection().toScreenLocation(latLng);
        	point.set(point.x+100, point.y);
        	latLng2 = map.getProjection().fromScreenLocation(point);
        }
        else{
        	 latLng2=markerRadio.getPosition();	
        }
        if(!pref.getBoolean("alarma", false)){
        markerOptions.position(latLng2);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.x2));
        markerOptions.draggable(true);
        markerRadio =  map.addMarker(markerOptions);
        }
//marker circle        
       float[] results = new float[1]; 
       Location.distanceBetween(latLng.latitude, latLng.longitude, latLng2.latitude, latLng2.longitude, results);
       if(radio==0){radio=(int) results[0];}
       CircleOptions circleOptions = new CircleOptions();
       circleOptions.center(latLng);
       circleOptions.radius(radio);
       
       if(pref.getBoolean("alarma", false)){
    	   circleOptions.fillColor(0x40ff0000);
    	   circleOptions.strokeColor(0x40ff0000);
    	   circleOptions.strokeWidth(2);
    	   }
       else{
    	   circleOptions.fillColor(0x401B8EE0);
    	   circleOptions.strokeColor(0x401B8EE0);
    	   circleOptions.strokeWidth(3);
    	   }        
       
       circle =map.addCircle(circleOptions);	
	}
	@Override
	public void onMapClick(LatLng latLng) {
		mVibrator.vibrate(50);
		twoLocations=true;
        radio=0;
		if (pref.getBoolean("alarma", false)){stopAlarm(); }
		editor.putBoolean("alarma", false).commit();  
		createAlarmMarker(latLng);
	}
	@Override
	public boolean onMarkerClick(Marker arg0) {
		mVibrator.vibrate(50);
		if (arg0.equals(markerPhone)) {
			if(!pref.getBoolean("alarma", false)){
    			editor.putBoolean("alarma", true).commit();
    			createAlarmMarker(markerPhone.getPosition());
    			
    			
    			notificationToast(getString(R.string.alarmRunning), 2);
    			
    	    }
    		else {
    			//Toast.makeText(this, "Alarma Desactivada", Toast.LENGTH_LONG).show();
    			//toadCustom("Alarma Desactivada");
    			editor.putBoolean("alarma", false).commit();
    			editor.putBoolean("activada", false).commit();
    			createAlarmMarker(markerPhone.getPosition());
    			stopAlarm();
    		}
			
		
		}
	    return true;
	}
    @Override
    public void onMarkerDrag(Marker arg0) {
    	if (arg0.equals(markerRadio)) {
			float[] results = new float[1]; 
		    Location.distanceBetween(markerPhone.getPosition().latitude, markerPhone.getPosition().longitude, markerRadio.getPosition().latitude, markerRadio.getPosition().longitude, results);
            circle.setRadius((int) results[0]);
			markerRadio.hideInfoWindow();
		}
	}
	@Override
	public void onMarkerDragEnd(Marker arg0) {
		if (arg0.equals(markerRadio)) {
			float[] results = new float[1]; 
		    Location.distanceBetween(markerPhone.getPosition().latitude, markerPhone.getPosition().longitude, markerRadio.getPosition().latitude, markerRadio.getPosition().longitude, results);
            radio=(int) results[0];
		    circle.setRadius(radio);
			markerRadio.hideInfoWindow();

		}
	}
	@Override
	public void onMarkerDragStart(Marker arg0) {
		mVibrator.vibrate(50);
		if (arg0.equals(markerRadio)) {
			float[] results = new float[1]; 
		    Location.distanceBetween(markerPhone.getPosition().latitude, markerPhone.getPosition().longitude, markerRadio.getPosition().latitude, markerRadio.getPosition().longitude, results);
            circle.setRadius((int) results[0]);
			markerRadio.hideInfoWindow();
		}		
	}
	//----------------------- S O N I D O ---------------------------------------------------------------
	private void playSound(Context context, Uri alert) {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(context, alert);
            final AudioManager audioManager = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (IOException e) {
            System.out.println("OOPS");
        }
    }    
    public void stopAlarm()  
    {     
    	if(pref.getBoolean("sonido", false)&&mMediaPlayer!=null){ mMediaPlayer.stop(); }
    	if(pref.getBoolean("vibrar", false)&&mVibrator!=null){ mVibrator.cancel(); }
    	alarmZone=false;
    	notificationToast(getString(R.string.alarmNoRunning), 2);
		notificationManager.cancel(1);
		notificationManager.cancel(0);
 	      
    }
    //----------------------- L A N Z A R   M E N U ------------------------------------------------------
    public void lanzarAcercaDe(){
    	    home=false;
	        Intent i = new Intent(this, opciones.class);  
            startActivityForResult(i, 1);
	       
	      }
	    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
	    	if (requestCode==1 && resultCode==RESULT_OK) {
	    		home=true;
	    		if (markerPhone!=null)createAlarmMarker(markerPhone.getPosition());
	    	}
	    	
	    
	}

//-----------------------N O T I F I C A T I O N S ---------------------------------------------------
	@SuppressWarnings("deprecation")
	private void notificationLaunch(String charSecuence,String contentTitle,String contentText, int id){       
		    
		    notificationManager.cancelAll();
			notification = new Notification(R.drawable.aaa,charSecuence, System.currentTimeMillis());
			notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR |Notification.FLAG_SHOW_LIGHTS;   
			Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1234, intent, 0);
            
            notification.setLatestEventInfo(this, contentTitle, contentText, pendingIntent);
            notificationManager.notify(id, notification);	 
            
         
            
            
            
            
    }  
	@SuppressWarnings("deprecation")
	private void notificationToast(String tickerText, int id){       
		    notification = new Notification(R.drawable.ic_launcher, tickerText, System.currentTimeMillis());
            notification.flags = Notification.FLAG_AUTO_CANCEL|Notification.DEFAULT_SOUND;
            notification.setLatestEventInfo(this, "nada", "nada", PendingIntent.getActivity(this, 0, new Intent(), 0));
            notificationManager.notify(id, notification);	
            notificationManager.cancel(id);
    }
	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		twoLocations=true;
        Address address= (Address) adapterView.getItemAtPosition(position);
	    LatLng latlng = new LatLng (address.getLatitude(),address.getLongitude());
        map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
        		latlng, 
        		17, 
        		0,
        		map.getCameraPosition().bearing)),5000,new CancelableCallback(){

            @Override
            public void onFinish()
            {
            	 twoLocations=false;	

            }

            @Override
            public void onCancel()
            {
            	 twoLocations=false; 
            	
            }
        });
        
      
        MarkerOptions markerOptions =new MarkerOptions();
		markerOptions.position(latlng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_search)); 
        map.addMarker(markerOptions);
        
	}
	  

	//---------------------- G P S  -------------------------------------------------------------------
	public void onConnected(Bundle bundle) {
		//Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show();
	        if (mUpdatesRequested) {
	            startPeriodicUpdates();
	        }
	    }
	private void startPeriodicUpdates() {
		//Toast.makeText(this, "startPeriodicUpdates", Toast.LENGTH_SHORT).show();
		//onLocationChanged(mLocationClient.getLastLocation());    
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	        
	    }
	@Override
	public void onDisconnected() {
	       
	    }  
	      @Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
	    	 // Toast.makeText(this, "onConnectionFailed", Toast.LENGTH_SHORT).show();
	        if (connectionResult.hasResolution()) {
	            try {

	                // Start an Activity that tries to resolve the error
	                connectionResult.startResolutionForResult(
	                        this,
	                        9000);

	                /*
	                * Thrown if Google Play services canceled the original
	                * PendingIntent
	                */

	            } catch (IntentSender.SendIntentException e) {

	                // Log the error
	                e.printStackTrace();
	            }
	        } else {

	            // If no resolution is available, display a dialog to the user with the error.
	            showErrorDialog(connectionResult.getErrorCode());
	        }
	    }
	private void showErrorDialog(int errorCode) {
		//Toast.makeText(this, "showErrorDialog", Toast.LENGTH_SHORT).show();
	          // Get the error dialog from Google Play services
	          Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
	              errorCode,
	              this,
	              9000);

	          // If Google Play services can provide an error dialog
	          if (errorDialog != null) {

	              // Create a new DialogFragment in which to show the error dialog
	              ErrorDialogFragment errorFragment = new ErrorDialogFragment();

	              // Set the dialog in the DialogFragment
	              errorFragment.setDialog(errorDialog);

	              // Show the error dialog in the DialogFragment
	              errorFragment.show(getSupportFragmentManager(), "TAG");
	          }
	      }
	public static class ErrorDialogFragment extends DialogFragment {
		
	          // Global field to contain the error dialog
	          private Dialog mDialog;

	          /**
	           * Default constructor. Sets the dialog field to null
	           */
	          public ErrorDialogFragment() {
	              super();
	              mDialog = null;
	          }

	          /**
	           * Set the dialog to display
	           *
	           * @param dialog An error dialog
	           */
	          public void setDialog(Dialog dialog) {
	              mDialog = dialog;
	          }

	          /*
	           * This method must return a Dialog to the DialogFragment.
	           */
	          @Override
	          public Dialog onCreateDialog(Bundle savedInstanceState) {
	              return mDialog;
	          }
	      }
    @Override
    public void onLocationChanged(Location location) {
			//Toast.makeText(this, "locationchange", Toast.LENGTH_SHORT).show();
			
			if( mListener != null ){   	
				// Toast.makeText(this, ":O", Toast.LENGTH_SHORT).show();
		        mListener.onLocationChanged( location );
	            //--mover camara: si esta el foco  no esta en vista doble
		        LatLngBounds bounds = this.map.getProjection().getVisibleRegion().latLngBounds;
	            if(firstLocation){
	            	firstLocation=false;
	            	twoLocations=true;
	            	 map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
							 new LatLng(location.getLatitude(), location.getLongitude()), 
				        		17, 
				        		0,
				        		map.getCameraPosition().bearing)),5000,new CancelableCallback(){

				            @Override
				            public void onFinish()
				            {
				            	 twoLocations=false;
				            }

				            @Override
				            public void onCancel()
				            {
				            	 twoLocations=false; 
				            	
				            }
				        });
	            	}
		        
		        if(bounds.contains(new LatLng(location.getLatitude(), location.getLongitude()))&&!twoLocations){  
	            	
	            	map.animateCamera(	
	            			CameraUpdateFactory.newCameraPosition(
	            					new CameraPosition(
	            							new LatLng(location.getLatitude(),location.getLongitude()),
	            							map.getCameraPosition().zoom,
	            							map.getCameraPosition().tilt,
	            							location.getBearing()
	            							
	            				    )
	            			)
	            	);
	            
	            }
		        //--alarma activada?
		        if(pref.getBoolean("alarma", false)){	 
		        	float[] results = new float[1]; 
		            Location.distanceBetween(markerPhone.getPosition().latitude, markerPhone.getPosition().longitude, location.getLatitude(), location.getLongitude(), results);
		        	if((results[0]<radio)&&(!pref.getBoolean("activada", false)))
	                {   
		        		notificationLaunch(getString(R.string.weArrived),getString(R.string.app_name),getString(R.string.stopAlarm), 0);
		        		alarmZone=true;
	                    if(pref.getBoolean("sonido", false)){	playSound(this, Uri.parse(pref.getString("uri", "waca")));}
		        		if(pref.getBoolean("vibrar", false)){	mVibrator.vibrate(new long[] { 0,500,700 }, 0);}
		        		editor.putBoolean("activada", true).commit();
		        	}
		        	   
		         }
		       
		}
			
		}
    @Override
	public void activate(OnLocationChangedListener arg0) {
			
			// TODO Auto-generated method stub
			//Toast.makeText(this, "activate", Toast.LENGTH_LONG).show();
			mListener = arg0;
			
			
		}
	@Override
	public void deactivate() {
			// TODO Auto-generated method stub
			//Toast.makeText(this, "deactivate", Toast.LENGTH_LONG).show();
			mListener = null;
			
		}
    private boolean servicesConnected() { 
	        int resultCode =GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	        if (ConnectionResult.SUCCESS == resultCode) {
	            return true;
	        } else {
	        	Toast.makeText(this, "servicio NO disponible", Toast.LENGTH_LONG).show();
	            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
	            if (dialog != null) {
	                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
	                errorFragment.setDialog(dialog);
	                errorFragment.show(getSupportFragmentManager(), "TAG");
	            }
	            return false;
	        }
	    }
	   
	
}

