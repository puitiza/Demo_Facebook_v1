package pe.anthony.facebook.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.karan.churi.PermissionManager.PermissionManager;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import pe.anthony.facebook.R;
import pe.anthony.facebook.Util.PrefUtil;

public class LoginActivity extends AppCompatActivity {

    private LoginButton loginButton;    //Es el boton de login de facebook
    private CallbackManager callbackManager; //esto es propio y necesario de facebook
//    private SharedPreferences prefs;    //Esto es para guardar al usuario logeado
    private static final String TAG="facebook_login";//Esto solo sirve para el log, posteriormente se va a borrar

    //Esto parte de una libreria, para administrar mejor los permisos , solo funciona para android 6
    PermissionManager permissionManager;
    public static final String PERMISSION_NEVER="El permiso fue denegado si no quieres volver a ver el mensaje solo dale clic en nunca volver a recodar";
    ProgressDialog mDialog;
    PrefUtil session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//Compruebo la version de mi modelo de celular
          permissionManager = new PermissionManager(){
              @Override
              public void ifCancelledAndCanRequest(Activity activity) {
                  Toast.makeText(getApplicationContext(),PERMISSION_NEVER,Toast.LENGTH_LONG).show();
               permissionManager.checkAndRequestPermissions(LoginActivity.this);
              }
              @Override
              public void ifCancelledAndCannotRequest(Activity activity) {
              }
          };
          permissionManager.checkAndRequestPermissions(this);
        }
        session = new PrefUtil(LoginActivity.this);
//        prefs= getSharedPreferences("LOGIN_FACEBOOK", Context.MODE_PRIVATE);
        callbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button);
        //Aqui le damos los permisos para la foto y los demas datos del usuaario
        loginButton.setReadPermissions(Arrays.asList("public_profile","email","user_birthday","user_friends","email, publish_actions"));
        // "email, publish_actions" <-- Exactamente ese permiso es lo que se necesita para compartir contenido en la app
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {//Este metodo es cuando ya tenga una respuesta de exito e iniziaste sesion
//                loginButton.setVisibility(View.INVISIBLE);  //<-IMPORTANTE para que ya no me salga logout de facebook en el mismo activity
//                en caso de que quieres que no se muestre solo puedes hacer eso
                getUserDetails(loginResult);
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(),R.string.cancel_login,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(),R.string.error_login,Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void getUserDetails(LoginResult loginResult) {
        mDialog = new ProgressDialog(LoginActivity.this);
        mDialog.setMessage("Reciviendo los Datos");
        mDialog.show();
        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                mDialog.dismiss();
                //getting FB user data
                saveFacebookData(object);
                goMainActivity(object);
            }
        });

        //Estoy mandando los campos que voy a usar
        Bundle parameters = new Bundle();
        //Recuerda que del inicio de sesion el JSONobject solo tiene 6 campos porque solo haz pedido estos
        parameters.putString("fields","id,name,email,birthday,friends,picture.width(250).height(250)");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void saveFacebookData(JSONObject object) {

        try{
            String id = object.getString("id");
            URL profile_pic = null;
            try{
                profile_pic = new URL("https://graph.facebook.com/"+id+"/picture?width=250&height=250");
//              profile_pic = new URL("https://graph.facebook.com/" + id + "/picture?type=large"); --> Esto es para otro tamaño nada mas
                Log.i("profile_pic", profile_pic + "");
            }catch (MalformedURLException e){
                e.printStackTrace();
            }
/*          Antes mandaba como parametro muchas variables ahora que esta en una clase solo necesito mandar un solo parametro
            String iD = object.getString("id");
            String name = object.getString("name");
            String email = object.getString("email");
            String birhtday = object.getString("birthday");
            String friends = object.getJSONObject("friends").getJSONObject("summary").getString("total_count");
            saveFacebookUserInfo(iD,name,email,birhtday,friends,profile_pic.toString());
            session.saveFacebookUserInfo(iD,name,email,birhtday,friends,profile_pic.toString());*/

            session.saveFacebookUserInfo(object,profile_pic);
        }catch (Exception e){
            Log.d(TAG,"BUNDLE Exception : "+e.toString());
        }
    }

   /* public void saveFacebookUserInfo(String iD,String name, String email, String birthday,String friends, String profileURL){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("fb_id",iD);
        editor.putString("fb_name",name);
        editor.putString("fb_email",email);
        editor.putString("fb_birthday",birthday);
        editor.putString("fb_count_friends",friends);
        editor.putString("fb_profileURL",profileURL);
        editor.apply();//Recuerda que el apply es mejor que el .commit() porque el .apply() es Asincrono
    }*/

    private void goMainActivity(JSONObject object) {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
        //Estos flags son los que me permiten que el activity sea la unica pantala en ejecucion, esto se hace para evitar el problema de cuando estoy
//        en mi pantalla principal no se vaya a mi pantalla de login y salga directamente de la aplicacion
        intent.putExtra("userProfile",object.toString());
        startActivity(intent);
        finish();   //<-Esto es para cerrar esta activity y no se que haciendo el logout de facebook y  pase al siguiente activity
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Este es el metodo que van a llegar las solicitudes y que tenemos que rediriguir al callbackmanger
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionManager.checkResult(requestCode,permissions,grantResults);
    }
}