package pe.anthony.facebook.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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
    JSONObject response;
    PrefUtil session;
    TargetPicasso targetPicasso;
    SharedContentWithFB shared;

    private final static int  ALLOW_WRITE_EXTERNAL_STORAGE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        session = new PrefUtil(MainActivity.this);
        if(AccessToken.getCurrentAccessToken() == null){//NO hay session
            goLoginActivity();
        }else{//Aqui es para cuando ya tienes la sesion
//            Creas una instancia de la clase TargetPicasso
            targetPicasso = new TargetPicasso(getApplicationContext(),TargetPicasso.IMAGE_ADDRESS,TargetPicasso.IMAGE_NAME);
//            Creas una instancia de la clase SharedContentWithFB , para compartir contenido de FB
            shared = new SharedContentWithFB(this);
//          <---Esto es para el login de FB
                logoutButton = findViewById(R.id.logout);
                logoutButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        goLoginActivity();
                    }
                });
//           ----->
            getData();

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
                                    if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},ALLOW_WRITE_EXTERNAL_STORAGE);
                                    }else{
                                        startApplicationDetailsActivity(getPackageName());
                                    }
                                }else{//Tienes el permiso
                                    shared.selectImageToSharedFacebook();
                                }
                            }else{
                                shared.selectImageToSharedFacebook();
                            }
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

    private void startApplicationDetailsActivity(String packageName) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setComponent(intent.resolveActivity(this.getPackageManager()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case ALLOW_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    Toast.makeText(getApplicationContext(),"Permiso permitido",Toast.LENGTH_SHORT).show();
                    shared.selectImageToSharedFacebook();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Permiso Denegado", Toast.LENGTH_SHORT).show();
                }
                break;
            default: super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
