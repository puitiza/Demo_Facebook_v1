package pe.anthony.facebook.Util;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;

import pe.anthony.facebook.Activities.LoginActivity;
import pe.anthony.facebook.Activities.MainActivity;
import pe.anthony.facebook.SharedPreferences.PrefUtil;

public class SplashActivity extends AppCompatActivity {

    PrefUtil session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new PrefUtil(SplashActivity.this);
        if(AccessToken.getCurrentAccessToken() == null){//NO hay session
            goLoginActivity();
        }else{//Aqui es para cuando ya tienes la sesion
            goMainActivity();
        }
    }

    private void goMainActivity() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void goLoginActivity() {
        session.clearSharedPreferences();
        LoginManager.getInstance().logOut();//Esto es muy importante para salir de la session y regresar al login
        Intent intent = new Intent(this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
