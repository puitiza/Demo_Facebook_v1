package pe.anthony.facebook.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.karan.churi.PermissionManager.PermissionManager;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;
import java.net.URL;

import pe.anthony.facebook.R;
import pe.anthony.facebook.Util.PrefUtil;
import pe.anthony.facebook.Util.SharedContentWithFB;
import pe.anthony.facebook.Util.TargetPicasso;

public class MainActivity extends AppCompatActivity {

    private Button logoutButton,btn_sharedContent,btn_imageSharedFB;
    JSONObject response, profile_pic_data, profile_pic_url;
//    private SharedPreferences prefs;
    PrefUtil session;
    TargetPicasso targetPicasso;
    SharedContentWithFB shared;

    private final static int  ALLOW_WRITE_EXTERNAL_STORAGE = 10;

    PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        session = new PrefUtil(MainActivity.this);
//        prefs= getSharedPreferences("LOGIN_FACEBOOK", Context.MODE_PRIVATE);
        if(AccessToken.getCurrentAccessToken() == null){//NO hay session
            goLoginActivity();
        }else{//Aqui es para cuando ya tienes la sesion
            //Creas una instancia de la clase TargetPicasso
            targetPicasso = new TargetPicasso(getApplicationContext(),TargetPicasso.IMAGE_ADDRESS,TargetPicasso.IMAGE_NAME);
            shared = new SharedContentWithFB(this);
//          <---Esto es para el login de FB
                logoutButton = findViewById(R.id.logout);
                logoutButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        goLoginActivity();
                    }
                });
                getData();
//            ----->

//          <--Esto es para compartir contenido en FB
                btn_sharedContent = findViewById(R.id.button_shareContent);
                btn_sharedContent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(isNetworkAvailable()){
                                shared.sharedContent();
                            }else{
                                Toast.makeText(getApplicationContext(),R.string.error_sharedContent,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
//          ------>

//          <----Esto es para compartir una imagen en facebook
                btn_imageSharedFB = findViewById(R.id.btn_imageShare);
                btn_imageSharedFB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isNetworkAvailable()){
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                                    permissionManager = new PermissionManager(){
                                        @Override
                                        public void ifCancelledAndCanRequest(Activity activity) {
                                            Toast.makeText(getApplicationContext(),LoginActivity.PERMISSION_NEVER,Toast.LENGTH_LONG).show();
                                            permissionManager.checkAndRequestPermissions(MainActivity.this);
                                        }
                                        @Override
                                        public void ifCancelledAndCannotRequest(Activity activity) {
                                        }
                                    };
                                    permissionManager.checkAndRequestPermissions(MainActivity.this);
                                }
                            }
                            shared.selectImageToSharedFacebook();
                        }else{
                            Toast.makeText(getApplicationContext(),R.string.error_sharedContent,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
//          ------->
        }
    }

    private void getData() {
        Intent intent = getIntent();
        String jsonData = intent.getStringExtra("userProfile");
        TextView txt_name = findViewById(R.id.txt_name);
        TextView txtEmail= findViewById(R.id.txt_Email);
        TextView txtCumple= findViewById(R.id.txt_Cumple);
        TextView txtFriend= findViewById(R.id.txt_Friend);
        ImageView imgAvatar = findViewById(R.id.imgAvatar);
        try {
           if(jsonData == null){ //Es porque has destruido la actividad y quieres volver a cargar asi que ya tienes los datos solo llamalos
//               String email = getEmailUser();
               txt_name.setText(session.getUserFB_name());
               txtEmail.setText(session.getUserFB_email());
               txtCumple.setText(session.getUserFB_birthday());
               txtFriend.setText("Friends"+session.getUserFB_countFriends());
                if(isNetworkAvailable()){
                    URL profile_picture = new URL(session.getUserFB_profileUrl());
                    Picasso.with(this).load(profile_picture.toString()).into(imgAvatar);
                    //Bueno como tengo conexion entonces estoy guardando la foto de perfil de la ultima imagen que tiene como usuario de facebook
                    targetPicasso.deleteImagePicassoTarget(getApplicationContext());
                    Picasso.with(this).load(profile_picture.toString()).into(targetPicasso.piccassoImageTarget());
                }else{
                    // Esto es para cargar la imagen que ha sido guardada para no volver a traerla de internet
                    File myImageFile = targetPicasso.loadImagePicassoTarget(getApplicationContext());
                    Picasso.with(this).load(myImageFile).into(imgAvatar);
                }

           }else{
               response = new JSONObject(jsonData);
               txt_name.setText(response.getString("name"));
               txtEmail.setText(response.getString("email"));
               txtCumple.setText(response.getString("birthday"));
               txtFriend.setText("Friends: "+response.getJSONObject("friends").getJSONObject("summary").getString("total_count"));

               URL profile_picture = new URL("https://graph.facebook.com/"+response.getString("id")+"/picture?width=250&height=250");
               Picasso.with(this).load(profile_picture.toString()).into(imgAvatar);

               //guardo la imagen para asi no volver a llamarla si no tienes conexion a internet
               Picasso.with(this).load(profile_picture.toString()).into(targetPicasso.piccassoImageTarget());
           }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(final int requestCode,final int resultCode,final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        shared.onResultToSelect(requestCode, resultCode, data);
    }

    private void goLoginActivity() {
//        clearSharedPreferences();
        session.clearSharedPreferences();
        targetPicasso.deleteImagePicassoTarget(getApplicationContext());   //Este metodo es para borrar el archivo de la imagen
        LoginManager.getInstance().logOut();//Esto es muy importante para salir de la session y regresar al login
        Intent intent = new Intent(this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /*
    * Este metodo me retorna true si hay una conexion de otra forma me retorna false
    * */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionManager.checkResult(requestCode,permissions,grantResults);
    }

//    public void clearSharedPreferences(){
//         prefs.edit().clear().apply();
//    }

//    public String getEmailUser(){
//        return prefs.getString("fb_email","No hay dato papu");
//    }
}
