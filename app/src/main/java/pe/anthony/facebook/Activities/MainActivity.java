package pe.anthony.facebook.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import pe.anthony.facebook.R;
import pe.anthony.facebook.Util.PrefUtil;
import pe.anthony.facebook.Util.TargetPicasso;

public class MainActivity extends AppCompatActivity {

    private Button logoutButton,btn_sharedContent,btn_imageSharedFB;
    JSONObject response, profile_pic_data, profile_pic_url;
//    private SharedPreferences prefs;
    PrefUtil session;
    TargetPicasso targetPicasso;
//   Esto es parte para compartir un contenido en facebook
    int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    ShareDialog shareDialog;
    private static String TAKE_PHOTO = "Toma una Foto";
    private static String CHOOSE_FROM_LIBRARY = "Elige de la galeria";
    private static String CANCEL = "Cancel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        session = new PrefUtil(MainActivity.this);
//        prefs= getSharedPreferences("LOGIN_FACEBOOK", Context.MODE_PRIVATE);                    ///

        if(AccessToken.getCurrentAccessToken() == null){//NO hay session
            goLoginActivity();
        }else{//Aqui es para cuando ya tienes la sesion
            //Creas una instancia de la clase TargetPicasso
            targetPicasso = new TargetPicasso(getApplicationContext(),TargetPicasso.IMAGE_ADDRESS,TargetPicasso.IMAGE_NAME);

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
                shareDialog = new ShareDialog(this);  // initialize facebook shareDialog.
                btn_sharedContent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(isNetworkAvailable()){
                                sharedContent();
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
                            selectImageToSharedFacebook();
                        }else{
                            Toast.makeText(getApplicationContext(),R.string.error_sharedContent,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
//          ------->
        }
    }

    private void selectImageToSharedFacebook() { //In this method, we will create a dialog box in which option to select image.
        final CharSequence[] items ={TAKE_PHOTO,CHOOSE_FROM_LIBRARY,CANCEL};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Select profile Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if(items[i].equals(TAKE_PHOTO)){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent,REQUEST_CAMERA);
                }else if(items[i].equals(CHOOSE_FROM_LIBRARY)){
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent,"Select File")
                            , SELECT_FILE);
                }else if(items[i].equals(CANCEL)){
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void sharedContent(){
        if(ShareDialog.canShow(ShareLinkContent.class)){
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("Android Facebook Integration Demo")
                    .setImageUrl(Uri.parse("https://www.studytutorial.in/wp-content/uploads/2017/02/FacebookLoginButton-min-300x136.png"))
                    .setContentDescription("This tutorial explains how to integrate Facebook and Login through Android Application")
                    .setContentUrl(Uri.parse("https://www.studytutorial.in/android-facebook-integration-and-login-tutorial"))
                    .build();
            shareDialog.show(linkContent);
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
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = managedQuery(selectedImageUri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        String selectedImagePath = cursor.getString(column_index);
        Bitmap thumbnail;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImagePath, options);
        final int REQUIRED_SIZE = 200;
        int scale = 1;
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                && options.outHeight / scale / 2 >= REQUIRED_SIZE)
            scale *= 2;
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        thumbnail = BitmapFactory.decodeFile(selectedImagePath, options);
        ShareDialog(thumbnail);
    }

    public void ShareDialog(Bitmap imagePath){
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(imagePath)
                .setCaption("StudyTutorial")
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();
        shareDialog.show(content);
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ShareDialog(thumbnail);
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

//    public void clearSharedPreferences(){
//         prefs.edit().clear().apply();
//    }

//    public String getEmailUser(){
//        return prefs.getString("fb_email","No hay dato papu");
//    }
}
