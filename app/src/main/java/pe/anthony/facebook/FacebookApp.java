package pe.anthony.facebook;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

/**
 * Created by ANTHONY on 14/12/2017.
 */

/*     Para que el proyecto tenga implementado el login de facebook es necesario crear una clase como esta con el nombre del paquete
     En mi caso se llama FacebookApp , te recomiendo que pongas el nombre de tu applicacion y al final agregues app
     luego extends Application y luego solo implementa esto
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        En el onCreate*/
public class FacebookApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }
//    Luego de implemntar esto solo tienes que agregar en el manifest esto  android:name=".FacebookApp"
}
