package pe.anthony.facebook;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Button logoutButton;
    JSONObject response;
//    private SharedPreferences prefs;

    PrefUtil session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        session = new PrefUtil(MainActivity.this);
//        prefs= getSharedPreferences("LOGIN_FACEBOOK", Context.MODE_PRIVATE);                    ///

        if(AccessToken.getCurrentAccessToken() == null){//NO hay session
            goLoginActivity();
        }else{//Aqui es para cuando ya tienes la sesion

            logoutButton = findViewById(R.id.logout);
            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goLoginActivity();
                }
            });
            getData();
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
               URL profile_picture = new URL(session.getUserFB_profileUrl());
               Picasso.with(this).load(profile_picture.toString()).into(imgAvatar);
           }else{
               response = new JSONObject(jsonData);
               txt_name.setText(response.getString("name"));
               txtEmail.setText(response.getString("email"));
               txtCumple.setText(response.getString("birthday"));
               txtFriend.setText("Friends: "+response.getJSONObject("friends").getJSONObject("summary").getString("total_count"));

               URL profile_picture = new URL("https://graph.facebook.com/"+response.getString("id")+"/picture?width=250&height=250");
               Picasso.with(this).load(profile_picture.toString()).into(imgAvatar);
           }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void goLoginActivity() {
//        clearSharedPreferences();
        session.clearSharedPreferences();
        LoginManager.getInstance().logOut();//Esto es muy importante para salir de la session y regresar al login
        Intent intent = new Intent(this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

//    public void clearSharedPreferences(){
//         prefs.edit().clear().apply();
//    }

//    public String getEmailUser(){
//        return prefs.getString("fb_email","No hay dato papu");
//    }
}
