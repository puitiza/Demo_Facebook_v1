package pe.anthony.facebook.Util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Esta clase fue creada para saber si es la primera vez que se lanza la app o si ya esta registrada en el movil del usuario
 * Created by ANTHONY on 5/01/2018.
 */

public class FirstRunDetected {
    SharedPreferences preferences;   // Shared Preferences variable
    SharedPreferences.Editor editor;   //editor for shared preference
    private String PREFERENCES ="FB_RunFirst";  //Nombre del archivo del preferences
    private  boolean firstRun;

    public FirstRunDetected(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE);
        editor = preferences.edit();
        firstRun = preferences.getBoolean("firstRun", true);//Aca dice por defecto esta true
    }

    public boolean loadFirstRun(){
        return firstRun;
    }

    public void saveFirstRun(){
        editor.putBoolean("firstRun",false);
        editor.commit();
    }
}
