package pe.anthony.facebook.SplashScreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pe.anthony.facebook.Activities.LoginActivity;
import pe.anthony.facebook.Activities.MainActivity;
import pe.anthony.facebook.R;
import pe.anthony.facebook.Util.PrefUtil;

public class SplashActivity extends AppCompatActivity {

    PrefUtil session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new PrefUtil(SplashActivity.this);
        SystemClock.sleep(1000); //Esto es muy importante ya que de esa forma cada vez que se vulve a lanzar la activity al menos espera 1 segundo
        /*Esto es para una autentificacion solo con facebook
            if(AccessToken.getCurrentAccessToken() == null){//NO hay session
                goLoginActivity();
            }else{//Aqui es para cuando ya tienes la sesion
                goMainActivity();
            }
         */
//      Esto es cuando la autentificacion es por firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!= null){
            goMainActivity();
        }else{
            goLoginActivity();
        }
    }

    private void goMainActivity() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.right_go_in, R.anim.right_go_out); //Esto es una transicion hacia la right solo se necesita R.anim.right_go_in, R.anim.right_go_out
    }

    private void goLoginActivity() {
        session.clearSharedPreferences();
        LoginManager.getInstance().logOut();//Esto es muy importante para salir de la session y regresar al login
        FirebaseAuth.getInstance().signOut();//Esto es para poder cerrar sesion en firebase
        Intent intent = new Intent(this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.left_go_in, R.anim.left_go_out); //Esto es una transicion hacia la left solo se necesita R.anim.left_go_in, R.anim.left_go_out
    }
}
