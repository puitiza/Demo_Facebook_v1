package pe.anthony.facebook.Util;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Esta clase fue creada para guardar la imagen en la memoria del celular y asi tener la imagen incluso cuando no hay internet
 * Created by ANTHONY on 17/12/2017.
 */

public class TargetPicasso {
    public static String IMAGE_ADDRESS= "imageDir";
    public static String IMAGE_NAME= "profileFB_picture.jpeg";

    Context context;
    String imageDir = null;
    String imageName = null;

    public TargetPicasso(Context context, String imageDir, String imageName) {
        this.context = context;
        this.imageDir = imageDir;
        this.imageName = imageName;
    }
    public Target piccassoImageTarget(){
        ContextWrapper cw = new ContextWrapper(context);
        final File directory = cw.getDir(imageDir, Context.MODE_PRIVATE); // path to /data/data/yourapp/app_imageDir
        return new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final File myImageFile = new File(directory, imageName); // Create image file
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(myImageFile);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.i("image", "image saved to >>>" + myImageFile.getAbsolutePath());

                    }
                }).start();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable != null) {}
            }
        };
    }

    public File loadImagePicassoTarget(Context context){
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(IMAGE_ADDRESS, Context.MODE_PRIVATE);
        File myImageFile = new File(directory, IMAGE_NAME);
        return myImageFile;
    }

    public void deleteImagePicassoTarget(Context context){
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(IMAGE_ADDRESS, Context.MODE_PRIVATE);
        File myImageFile = new File(directory, IMAGE_NAME);
        if (myImageFile.delete()) Log.i("imagen","image on the disk deleted successfully!");
    }

}
