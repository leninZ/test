package com.gps.nap;

import com.gps.nap.R;

import android.app.Activity;

import android.content.Context;
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
import android.widget.Toast;


 
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
        //UI iniciar
        sonido = (CheckBox) findViewById(R.id.sonido);
        iconAlarm=(ImageView) findViewById(R.id.lupa);
        selectAlarm=(TextView) findViewById(R.id.selectAlarm);
        vibrar = (CheckBox) findViewById(R.id.vibrar);
        
        //Retorno a Main Activity
        Intent returnIntent = new Intent();
   	    setResult(RESULT_OK,returnIntent);     
        
   	    //persistencia de datos
        pref = getApplicationContext().getSharedPreferences("MyPref",  Context.MODE_PRIVATE);
        editor = pref.edit();
        
        //inicializar checkbox
        if(pref.getBoolean("sonido", false)){sonido.setChecked(true);}
        if(pref.getBoolean("vibrar", false)){vibrar.setChecked(true);}
        
        
        
       //seleccionar alarma|| la alarma ya seteada
        defaultAlarm=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE).toString();
        if(pref.getString("uri",defaultAlarm)==defaultAlarm){
        	selectAlarm.setText(getString(R.string.selectAlarmTittle), null); //selecciona tu alarma
        }
        else{
        	selectAlarm.setText(pref.getString("tittleAlarm", getString(R.string.selectAlarmTittle)), null); 
        }
        
       
    }
        
  
     @Override
	 public void onPause(){
    	super.onPause();
    	//Toast.makeText(this, "opciones:::pause", Toast.LENGTH_SHORT).show();
    }
    
	public void onResume(){  
		super.onResume();
		//Toast.makeText(this, "opciones:::resume", Toast.LENGTH_SHORT).show();
	}

	public void selectRingtone (View view){
		Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.selectAlarmPopup));
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(pref.getString("uri", defaultAlarm)));
		this.startActivityForResult(intent, 5);
	}
	public void selectSound (View view){
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
    public void selectVibrar (View view){
        if(vibrar.isChecked()){editor.putBoolean("vibrar", true).commit();}
        
        else{editor.putBoolean("vibrar", false).commit();
             if(!sonido.isChecked()){
               sonido.setChecked(true);editor.putBoolean("sonido", true).commit();
              selectAlarm.setVisibility(View.VISIBLE);
           iconAlarm.setVisibility(View.VISIBLE);
             }
        
        } 
    }
   
	@Override
	 protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent)
	 {
		//al volver de seleccionar alarma
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