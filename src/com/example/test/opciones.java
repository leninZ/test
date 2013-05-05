package com.example.test;

import android.app.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

import android.widget.ImageView;
import android.widget.TextView;


 
public class opciones extends Activity  {

	private CheckBox  sonido,vibrar;


    private SharedPreferences pref;
	private Editor editor;

	private TextView selectAlarm;
	private ImageView iconAlarm;

	private String defaultAlarm;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opciones);
        Intent returnIntent = new Intent();
   	    setResult(RESULT_OK,returnIntent);     

        pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        editor = pref.edit();

        
        sonido = (CheckBox) findViewById(R.id.sonido);
        iconAlarm=(ImageView) findViewById(R.id.imageView1);
        selectAlarm=(TextView) findViewById(R.id.selectAlarm);
        vibrar = (CheckBox) findViewById(R.id.vibrar);
       
        defaultAlarm=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE).toString();
        if(pref.getString("uri",defaultAlarm)==defaultAlarm){
        	selectAlarm.setText(getString(R.string.selectAlarmTittle), null); 
        }
        else{
        	selectAlarm.setText(pref.getString("tittleAlarm", getString(R.string.selectAlarmTittle)), null); 
        }
        if(pref.getBoolean("sonido", false)){sonido.setChecked(true);}
        if(pref.getBoolean("vibrar", false)){vibrar.setChecked(true);}

        sonido.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	if(sonido.isChecked()){
            		editor.putBoolean("sonido", true).commit();
            		/*show select alarm*/
            		selectAlarm.setVisibility(View.VISIBLE);
            		iconAlarm.setVisibility(View.VISIBLE);
            		
            	}
            	
            	else{editor.putBoolean("sonido", false).commit();
            	     /*hide select alarm*/
            	     selectAlarm.setVisibility(View.INVISIBLE);
        		     iconAlarm.setVisibility(View.INVISIBLE);
        		     
            	     if(!vibrar.isChecked()){
            	    	 vibrar.setChecked(true);editor.putBoolean("vibrar", true).commit();
            	     }
            	
            	}   
            }
        });
        vibrar.setOnClickListener(new OnClickListener() {
        	
        	             @Override
        	               public void onClick(View v) {
        	                 if(vibrar.isChecked()){editor.putBoolean("vibrar", true).commit();}
        	                 
        	                 else{editor.putBoolean("vibrar", false).commit();
        	                      if(!sonido.isChecked()){
        	                        sonido.setChecked(true);editor.putBoolean("sonido", true).commit();
        	                       selectAlarm.setVisibility(View.VISIBLE);
        	                    iconAlarm.setVisibility(View.VISIBLE);
        	                      }
        	                 
        	                 }   
        	               }
        	        });

        
  	 
    }
    public void onPause(){
    	//Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
    	super.onPause();
    }

	public void selectRingtone (View view){
		Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.selectAlarmPopup));
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(pref.getString("uri", defaultAlarm)));
		this.startActivityForResult(intent, 5);
	}
	@Override
	 protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent)
	 {
	     if (resultCode == Activity.RESULT_OK && requestCode == 5)
	     {
	          Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
	          

	          if (uri != null)
	          {
	             // this.chosenRingtone = uri.toString();
	        	  Ringtone r=RingtoneManager.getRingtone(this, uri);

	        	  String ringToneName=r.getTitle(this);
	        	  editor.putString("uri", uri.toString()).commit();
	        	  editor.putString("tittleAlarm",ringToneName ).commit();
	        	  selectAlarm.setText(pref.getString("tittleAlarm", "Seleccionar Alarma"), null);
	              //Toast.makeText(this, ringToneName, Toast.LENGTH_SHORT).show();
	          }
	          else
	          {
	              //this.chosenRingtone = null;
	          }
	      }            
	  }
}