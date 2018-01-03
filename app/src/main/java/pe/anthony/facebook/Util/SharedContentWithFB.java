package pe.anthony.facebook.Util;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Esto es parte para compartir un contenido en facebook
 * Created by ANTHONY on 2/01/2018.
 */

public class SharedContentWithFB {
    Activity activity;
    ShareDialog shareDialog;

    int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private static String TAKE_PHOTO = "Toma una Foto";
    private static String CHOOSE_FROM_LIBRARY = "Elige de la galeria";
    private static String CANCEL = "Cancel";

    public SharedContentWithFB(Activity activity) {
        this.activity = activity;
        shareDialog = new ShareDialog(activity);
    }

    public void sharedContent(){
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

    public void selectImageToSharedFacebook() { //In this method, we will create a dialog box in which option to select image.
        final CharSequence[] items ={TAKE_PHOTO,CHOOSE_FROM_LIBRARY,CANCEL};
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Select profile Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if(items[i].equals(TAKE_PHOTO)){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    activity.startActivityForResult(intent,REQUEST_CAMERA);
//                  startActivityForResult(intent,REQUEST_CAMERA);  esto que esta comentado es para cuando va en un activity
                }else if(items[i].equals(CHOOSE_FROM_LIBRARY)){
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    activity.startActivityForResult(Intent.createChooser(intent,"Select File")
                            , SELECT_FILE);
//                    startActivityForResult(Intent.createChooser(intent,"Select File"), SELECT_FILE); esto que esta comentado es para cuando va en un activity
                }else if(items[i].equals(CANCEL)){
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void onResultToSelect( int requestCode, int resultCode, Intent data){
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    public void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = activity.managedQuery(selectedImageUri, projection, null, null, null);
//        Cursor cursor = managedQuery(selectedImageUri, projection, null, null, null); esto que esta comentado es para cuando va en un activity
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

    public void onCaptureImageResult(Intent data) {
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
}
