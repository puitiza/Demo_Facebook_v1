package pe.anthony.facebook.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import pe.anthony.facebook.R;
import pe.anthony.facebook.Util.FirstRunDetected;
import pe.anthony.facebook.Util.PrefUtil;

public class LoginActivity extends AppCompatActivity {

    private LoginButton loginButton;    //Es el boton de login de facebook
    private CallbackManager callbackManager; //esto es propio y necesario de facebook

    private static final String TAG="facebook_login";//Esto solo sirve para el log

    ProgressDialog mDialog;
    PrefUtil session;

    //saber si se ha corrido la primera vez
    FirstRunDetected firstRun;
    private static final int  Permission_All = 1 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firstRun = new FirstRunDetected(LoginActivity.this);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ //Compruebo la version de mi modelo de celular si es mayour que la del API 23
            if(firstRun.loadFirstRun()){
                Log.i("onCreate: ","first time" );
                firstRun.saveFirstRun();
//                Si quieres agrega mas permiso a la cadena te recomiendo maximo 5
                String[] Permissions ={Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION};
                if(!hasPermissions(LoginActivity.this,Permissions)){
                    ActivityCompat.requestPermissions(LoginActivity.this,Permissions,Permission_All);
                }
            }
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
            session.saveFacebookUserInfo(object,profile_pic);
        }catch (Exception e){
            Log.d(TAG,"BUNDLE Exception : "+e.toString());
        }
    }

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
        switch (requestCode){
            case Permission_All:
                boolean allPermissionsGranted = true;
                if(grantResults.length>0){
                    for(int grantResult: grantResults){
                        if(grantResult != PackageManager.PERMISSION_GRANTED){
                            allPermissionsGranted = false;
                            break;
                        }
                    }
                }
                if (allPermissionsGranted) {
                    // Permission Granted
                    Toast.makeText(getApplicationContext(),"Los permisos fueron permitido",Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Facebook");
                    builder.setMessage("Se requieren todos los permisos para que la aplicación funcione correctamente" +
                            ", por favor activarlos desde la configuración de su disositivo");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                break;

            default: super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions!=null){
            for (String permission :permissions) {
                if(ActivityCompat.checkSelfPermission(context,permission)!= PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }
}