package pe.anthony.facebook.Util;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

/**
 * Created by ANTHONY on 15/12/2017.
 */
//Esta clase fue creada con el proposito de guardar la informacion del usuario una vez registrado en el movil
public class PrefUtil {

    SharedPreferences preferences;   // Shared Preferences variable
    SharedPreferences.Editor editor;   //editor for shared preference

    private String PREFERENCES ="login_FBsession";  //Nombre del archivo del preferences
    private String ID = "fb_id"; //key for iD_userFB
    private String NAME = "fb_name"; //key for name_userFB
    private String EMAIL = "fb_email";//Key for email_userFB
    private String BIRTHDAY = "fb_birthday"; //Key for birthday_userFB
    private String COUNT_FRIENDS = "fb_count_friends";  //Key for count_friends_userFB
    private String PROFILEURL = "fb_profileURL";  //Key for profileURL_userFB

    public PrefUtil(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void saveAccessToken(String token){
//      SharedPreferences.Editor editor = preferences.edit(); pondria esta linea a todos los metodos pero como ya lo hemos puesto en el constructor
//      No es necesario
        editor.putString("fb_access_token",token);
        editor.apply();
    }

    public String getToken(){return preferences.getString("fb_access_token",null);}


   /* public void saveFacebookUserInfo(String iD,String name, String email, String birthday,String friends, String profileURL){
        editor.putString(ID,iD);
        editor.putString(NAME,name);
        editor.putString(EMAIL,email);
        editor.putString(BIRTHDAY,birthday);
        editor.putString(COUNT_FRIENDS,friends);
        editor.putString(PROFILEURL,profileURL);
        editor.apply(); //Recuerda que el apply es mejor que el .commit() porque el .apply() es Asincrono
    }*/

    public void clearSharedPreferences(){
        editor.clear();
        editor.apply();
    }

    /*Esto es para retornar la informacion del usuario*/
    public String getUserFB_id(){return preferences.getString(ID,"No tiene ID");}
    public String getUserFB_name(){return preferences.getString(NAME,"No tiene NAME");}
    public String getUserFB_email(){return preferences.getString(EMAIL,"No tiene EMAIL");}
    public String getUserFB_birthday(){return preferences.getString(BIRTHDAY,"No tiene BIRTHDAY");}
    public String getUserFB_countFriends(){return preferences.getString(COUNT_FRIENDS,"No tiene FRIENDS");}
    public String getUserFB_profileUrl(){return preferences.getString(PROFILEURL,"No tiene PROFILEURL");}

    public void saveFacebookUserInfo(JSONObject object, URL profile_picture) {
        try {
            String iD = object.getString("id");
            String name = object.getString("name");
            String email = object.getString("email");
            String birthday = object.getString("birthday");
            String friends = object.getJSONObject("friends").getJSONObject("summary").getString("total_count");

            editor.putString(ID,iD);
            editor.putString(NAME,name);
            editor.putString(EMAIL,email);
            editor.putString(BIRTHDAY,birthday);
            editor.putString(COUNT_FRIENDS,friends);
            editor.putString(PROFILEURL,profile_picture.toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
