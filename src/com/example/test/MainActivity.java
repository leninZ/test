package com.example.test;
import java.io.IOException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.ShakeEventListener.OnShakeListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
LocationListener, 
LocationSource,
OnMapClickListener,
OnMarkerDragListener,
OnMarkerClickListener,
OnShakeListener{

	private NotificationManager notificationManager;
	private LocationManager locationManager;
	private GoogleMap mMap;
	private OnLocationChangedListener mListener;
	private Marker myMarker2;
	private Marker myMarker;
	private int radio;
	private Circle circle;
	private SharedPreferences pref;
	private Editor editor;
	private MediaPlayer mMediaPlayer;
	private Vibrator mVibrator;
	private Notification notification;
	
	private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
    { 	 
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();
        mSensorListener.setOnShakeListener(this);
        
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);  
        editor = pref.edit();
        editor.putBoolean("activado", false).commit(); 
        editor.putBoolean("sonido", true).commit(); 
        editor.putBoolean("vibrar", true).commit();
        editor.putString("uri",RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE).toString() ).commit();
	    startGPS();
        setUpMapIfNeeded();
    }
	@Override
	protected void onDestroy() 
	{ Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
	  super.onDestroy();
      notificationManager.cancelAll();
	  //finish();
      System.exit(0); //elimina procesos que quedan corriendo
	     
	}
    @Override
	public void onPause()
	{
		//if(locationManager != null){  locationManager.removeUpdates(this);}
    	mSensorManager.unregisterListener(mSensorListener);
		super.onPause();
	}
    public void onResume()
	{
		super.onResume();
		mSensorManager.registerListener(mSensorListener,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_UI);
		setUpMapIfNeeded();
      //  if(locationManager != null){   
        	//mMap.setMyLocationEnabled(true);
        	//startGPS();

        	//}
	}
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
        switch(keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
            	if(pref.getBoolean("activado", false)){moveTaskToBack(true);}
            	else{onDestroy();}
                return true;
            	
        }
        return false;
    }

    //---------------------- M E N U -------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.changemap:
	        changeMap();
	        return true;
	    case R.id.myLocation:
	        mylocation();
	        return true;
	    case R.id.myAlarmLocation:
	    	myAlarmlocation();
	    	return true;
	    case R.id.lanzarAcercaDe:
	    	lanzarAcercaDe();
	    	return true;
	  
	   
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	//---------------------- M A P ---------------------------------------------------------------------
	private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) 
        {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            CameraPosition cameraPosition = new CameraPosition(new LatLng(0,0),15,40,0); 
	    	mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
           
            if (mMap != null) 
            {
                setUpMap();
            }

            //This is how you register the LocationSource
           mMap.setLocationSource(this);
          
              
        }
    }
	private void setUpMap() 
	    {   //showNotification();
	    	mMap.setMyLocationEnabled(true);
	    	mMap.getUiSettings().setZoomControlsEnabled(true);
	    	mMap.getUiSettings().setCompassEnabled(false);
	    	mMap.getUiSettings().setMyLocationButtonEnabled(false);
	    	mMap.setOnMarkerClickListener( this);
	    	mMap.setOnMapClickListener(this);
	    	mMap.setOnMarkerDragListener(this);

	    }
	private void startGPS(){
			 if(locationManager != null)
		    {
		        boolean gpsIsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		        boolean networkIsEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		    	
		    	if(gpsIsEnabled)
		    	{
		    		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 1F, this);
		    		Toast.makeText(this, "gps activado", Toast.LENGTH_SHORT).show();
		
		    	}
		    	else if(networkIsEnabled)
		    	{
		    		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 1F, this);
		    		Toast.makeText(this, "network activado", Toast.LENGTH_SHORT).show();
		    	}
		    	else
		    	{
		    		//Show an error dialog that GPS is disabled...
		         }
		    }
		    else
		    {
		    	//Show some generic error dialog because something must have gone wrong with location manager.
		    }
			
		}

	//----------------------LOCATION LISTENER-----------------------------------------------------------
	@Override
	public void onLocationChanged(Location location) {
		 Toast.makeText(this, "location change", Toast.LENGTH_SHORT).show();
		 if( mListener != null )
	    {   	
	        mListener.onLocationChanged( location );
	        LatLngBounds bounds = this.mMap.getProjection().getVisibleRegion().latLngBounds;
	        if(!bounds.contains(new LatLng(location.getLatitude(), location.getLongitude())))
	        {    
	             mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));        
	        }
	        if(pref.getBoolean("activado", false)){	 
	            if((radio >0)&&(twoPointsDistance( myMarker.getPosition(),new LatLng(location.getLatitude(), location.getLongitude()))<radio))
	            {   notificationLaunch("LLegamos :)","Geo Alarma","Detener la Alarma", 0);
	        		if(pref.getBoolean("sonido", false)){	playSound(this, Uri.parse(pref.getString("uri", "waca")));}
	        		if(pref.getBoolean("vibrar", false)){	mVibrator.vibrate(new long[] { 0,500,700 }, 0);}}
	             }
	    }
		
	}
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	//----------------------LOCATION SOURCE-------------------------------------------------------------
	@Override
	public void activate(OnLocationChangedListener arg0) {
		// TODO Auto-generated method stub
		mListener = arg0;
		
	}
	@Override
	public void deactivate() {
		// TODO Auto-generated method stub
		mListener = null;
		
	}

	//----------------------SHAKE LISTENER--------------------------------------------------------------
    @Override
	public void onShake() {
			 //Toast.makeText(getBaseContext(), "Shake!", Toast.LENGTH_SHORT).show();
	    	mSensorManager.unregisterListener(mSensorListener);
	        if(myMarker!=null){onMarkerClick(myMarker);}
	        sleep(1);
	        mSensorManager.registerListener(mSensorListener,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_UI);
			
			
		}

	//-----------------------MARKER RELOJ---------------------------------------------------------------
	public void sleep(long seconds ){
		
		  try {
				Thread.currentThread();
				Thread.sleep(seconds*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
 	public void createAlarmMarker(LatLng latLng) {//Toast.makeText(this, "map click", Toast.LENGTH_SHORT).show();
		mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        
		MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        if(pref.getBoolean("activado", false)){ 
        	//markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.clock_alarm_red));
        	
        	if(pref.getBoolean("sonido", false)&&pref.getBoolean("vibrar", false)){markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.phone_red_a_v));}
        	else if(pref.getBoolean("sonido", false)){markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.phone_red_a));}
        	else{markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.phone_red_v));}
        	
        	}
        else{ markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.phone_blue));}
       
        myMarker =  mMap.addMarker(markerOptions);

        
        double distRight = this.mMap.getProjection().getVisibleRegion().farRight.longitude;
        LatLng latLng2=new LatLng(latLng.latitude,latLng.longitude+(distRight-latLng.longitude)/3);
        markerOptions.position(latLng2);
        
        if(!pref.getBoolean("activado", false)){
        	markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.x2));
        	markerOptions.draggable(true);
        	markerOptions.title("presionar y arrastrar");
        	myMarker2 =  mMap.addMarker(markerOptions);
        } 
       
        if(radio==0){radio=twoPointsDistance(latLng,latLng2);}
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radio);
        circleOptions.strokeColor(0xb01717);
        
        //circleOptions.fillColor(0x401BE022);//green
        if(pref.getBoolean("activado", false)){circleOptions.fillColor(0x40ff0000);}
        else{circleOptions.fillColor(0x401B8EE0);}
        
        //circleOptions.fillColor(0x40ff0000);//red
        
        circleOptions.strokeWidth(4);
        circle =mMap.addCircle(circleOptions);	
		
	}
	@Override
	public void onMapClick(LatLng latLng) {
		radio=0;
		if (!pref.getBoolean("activado", false)){createAlarmMarker(latLng);}
	}
    private int twoPointsDistance(LatLng latLng,LatLng latLng2) {
		double lat1=latLng.latitude;
		double lng1= latLng.longitude;
		double lat2=latLng2.latitude;
		double lng2=latLng2.longitude;
        double earthRadius = 3958.75;
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;
	    int meterConversion = 1609;
	    return (int)(dist * meterConversion);
	    
	 
	    
	}
	@Override
	public boolean onMarkerClick(Marker arg0) { 
		if (arg0.equals(myMarker)) {
			if(!pref.getBoolean("activado", false)){
    			//Toast.makeText(this, "Alarma Activada", Toast.LENGTH_LONG).show();
    			//toadCustom("Alarma Activada");
    			editor.putBoolean("activado", true).commit();
    			createAlarmMarker(myMarker.getPosition());
    			notificationLaunch("Alarma Activada","Geo Alarma","La Alarma esta activada", 1);
    			
    	    }
    		else {
    			//Toast.makeText(this, "Alarma Desactivada", Toast.LENGTH_LONG).show();
    			//toadCustom("Alarma Desactivada");
    			editor.putBoolean("activado", false).commit();
    			createAlarmMarker(myMarker.getPosition());
    			notificationToast("Alarma Desactivada", 2);
    			stopAlarm();
    		}
			
		
		}
	    return false;
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
       // mMap.clear();
        //radio=0;
 	    //notificationManager.cancelAll();
 	   // editor.putBoolean("activado", false).commit(); 
 	      
    }

    //----------------------- MarkerDrag Listener--------------------------------------------------------------------	
    @Override
    public void onMarkerDrag(Marker arg0) {
    	radio=twoPointsDistance(myMarker.getPosition(),myMarker2.getPosition());
		circle.setRadius(radio);
		
	}
	@Override
	public void onMarkerDragEnd(Marker arg0) {
		radio=twoPointsDistance(myMarker.getPosition(),myMarker2.getPosition());
		circle.setRadius(radio);
		
	}
	@Override
	public void onMarkerDragStart(Marker arg0) {
		radio=twoPointsDistance(myMarker.getPosition(),myMarker2.getPosition());
		circle.setRadius(radio);
		myMarker2.hideInfoWindow();
		
	}

    //----------------------- L A N Z A R   M E N U ------------------------------------------------------
    public void lanzarAcercaDe(){
    	
	        Intent i = new Intent(this, opciones.class);  
	        
	        startActivityForResult(i, 1234);
	       
	      }
	    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
	    	//Toast.makeText(this, resultCode, Toast.LENGTH_SHORT).show();
	    	if (requestCode==1234 && resultCode==RESULT_OK) {
	    		if (myMarker!=null)createAlarmMarker(myMarker.getPosition());
	    		if(pref.getBoolean("activado", false)){
	    			//Toast.makeText(this, "alarma activada", Toast.LENGTH_SHORT).show();
	    			//circle.setFillColor(0x40ff0000);//red
	    	    }
	    		else {
	    			//Toast.makeText(this, "alarma no activada", Toast.LENGTH_SHORT).show();
	    			//circle.setFillColor(0x401B8EE0);//blue
	    		}
	           
	    		
	    	}
	    	if (requestCode==5 && resultCode==RESULT_OK) {
	    		Uri alert = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
/*
	            if (uri != null)
	            {
	                this.chosenRingtone = uri.toString();
	            }
	            else
	            {
	                this.chosenRingtone = null;
	            }
	           
	*/  		
	    	}
	    
	}

//---------------------------F U N C I O N E S   M E N U--------------------------------------------------
	public void changeMap() 
	    {  
	    	if(mMap.getMapType()==4){mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);}
	    	else if(mMap.getMapType()==1){ mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);}
	    	
	    }
	public void mylocation() 
	{  
		 Location location = mMap.getMyLocation();	
		 if(location!=null){
		 CameraPosition cameraPosition = new CameraPosition(new LatLng(location.getLatitude(), location.getLongitude()),15,40,0);
		 mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		 }
	    
	}
	public void myAlarmlocation() 
	{  
	 	 if (myMarker!=null){   	 
		 CameraPosition cameraPosition = new CameraPosition( myMarker.getPosition(),15,40,0);
		 mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	 	 }
	}
	//-----------------------N O T I F I C A T I O N S ---------------------------------------------------
	@SuppressWarnings("deprecation")
	private void notificationLaunch(String charSecuence,String contentTitle,String contentText, int id)
    {       notificationManager.cancelAll();
			notification = new Notification(R.drawable.ic_launcher,charSecuence, System.currentTimeMillis());
			notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;   
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1234, intent, 0);
            notification.setLatestEventInfo(this, contentTitle, contentText, pendingIntent);
            notificationManager.notify(id, notification);	
    }  
	@SuppressWarnings("deprecation")
	private void notificationToast(String tickerText, int id)
    {       notificationManager.cancelAll();
			notification = new Notification(R.drawable.ic_launcher, "Alarma Desactivada", System.currentTimeMillis());
            notification.setLatestEventInfo(this, "nada", "nada", PendingIntent.getActivity(this, 0, new Intent(), 0));
            notificationManager.notify(id, notification);	
            notificationManager.cancelAll();
    }
    public void toadCustom(String title) {
        // Getting the main view group
        ViewGroup viewGroup = (ViewGroup) findViewById(R.layout.activity_main);

        // Creating a view object inflated by the layout toast_layout
        View view = View.inflate(getBaseContext(), R.layout.toast_layout, viewGroup);

        // Getting the reference to the TextView "msg" of the toast_layout
        TextView txtMessage = (TextView) view.findViewById(R.id.msg);

        // Setting a text for the TextView "msg" of the toast_layout
        txtMessage.setText(title);

        // Getting the reference to the ImageView "icon" of the toast_layout
        ImageView image = ( ImageView ) view.findViewById(R.id.icon);

        // Setting an image to the ImageView "icon" of the toast_layout
        image.setImageResource(R.drawable.ic_launcher);

        // Creating  a toast object
        Toast toast = new Toast(getBaseContext());

        // Setting toast object's view
        toast.setView(view);
        
        toast.setDuration(1); 
       // toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 10);

        // Displaying the toast message
        toast.show();
    }



}
