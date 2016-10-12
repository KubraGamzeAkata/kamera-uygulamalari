package com.example.dell.kamera_uygulamalari;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class MainActivity extends Activity {

    // Activity istek kodları
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    //Galeri butonu için oluşturuyoruz.
    Button b;
    // Çekilen videoları ve fotoğrafları saklamak için dizin adını belirtiyoruz
    private static final String IMAGE_DIRECTORY_NAME = "KGA";

    private Uri fileUri; // Görüntü/video saklamak için dosya url

    private ImageView imgPreview;
    private VideoView videoPreview;
    private Button btnCapturePicture, btnRecordVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b=(Button)findViewById(R.id.btnGallery);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        imgPreview = (ImageView) findViewById(R.id.image_view);
        videoPreview = (VideoView) findViewById(R.id.video_view);

        btnCapturePicture = (Button) findViewById(R.id.btnPicture);
        btnRecordVideo = (Button) findViewById(R.id.btnVideo);

        //Görüntü yakalamak için buton olayı
        btnCapturePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Görüntü yakalama
                captureImage();
            }
        });

        //Video kaydetmek için buton olayı
        btnRecordVideo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Video kayıt
                recordVideo();
            }
        });

        // Kamera kullanılabilirliğini kontrol etme
        if (!isDeviceSupportCamera()) {
            Toast.makeText(getApplicationContext(),
                    "Üzgünüm! Cihazınızın kamera desteği yok",
                    Toast.LENGTH_LONG).show();
            // Eğer cihazınızda kamera yoksa uygulama kapanacak
            finish();
        }
    }
    //Galeri butonu için seçim menüsü oluşturuyoruz.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Menü inflate; bu eğer varsa eylem çubuğu için seçenekler ekler.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    private void selectImage() {

        final CharSequence[] options = { "Galeri","İptal" };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("KGA Kamera!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Galeri"))
                {
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                }
                else if (options[item].equals("İptal")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    //Cihazın kamera donanımına sahip olup olmadığını denetlemek için kontrol ediyoruz
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // Bu cihaz kameraya sahip
            return true;
        } else {
            // Bu cihazda kamera yok
            return false;
        }
    }

    // KGA kamera uygulaması görüntü yakalama ve video kayıt isteklerini başlatma olayı
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        // Görüntü yakalama amacını başlatıyoruz
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    //   Kameradan döndükten sonra boş olacak burada url dosya depolama işlemi yaparız
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Bu ekran yönlendirme boş olacak  paket dosya url kaydetme işlemi
        // Değişiklikler
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //Dosya için url adresi alıyoruz
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    // Video kayıt
    private void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

        // Video kalitesini ayarlıyoruz
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // Resim dosyasını ayarlıyoruz.

        // Video kaydetme işlemini başlatıyoruz
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    // Alıcı faaliyeti sonucu yöntemi kamera kapandıktan sonra çağrılır
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Görüntü yakalama
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Görüntü başarıyla yakalandı
                // Görüntü görümünde göstermek
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // Kullanıcı görüntü yakalamayı iptal etti
                Toast.makeText(getApplicationContext(),
                        "Kullanıcı görüntüyü iptal etti", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // Görüntü yakalama için başarısız
                Toast.makeText(getApplicationContext(),
                        "Üzgünüm! Görüntü yakalamak için başarısız oldu", Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Video başarıyla kaydedil
                // Kaydedilen video önizlemesi
                previewVideo();
            } else if (resultCode == RESULT_CANCELED) {
                // Kullanıcı kaydı iptal etti
                Toast.makeText(getApplicationContext(),
                        "Kullanıcı video kaydını iptal etti", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // Video kaydetmek için başarısız
                Toast.makeText(getApplicationContext(),
                        "Üzgünüm! Video kaydetmek için başarısız oldu", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
    // ImageView için bir yoldan ekran görüntüsü

    private void previewCapturedImage() {
        try {
            // Video önizlemesini gizle
            videoPreview.setVisibility(View.GONE);

            imgPreview.setVisibility(View.VISIBLE);

            // Bitmap fabrikası
            BitmapFactory.Options options = new BitmapFactory.Options();

            // Küçülme görüntü olarak atar OutOfMemory özel durum için daha büyük   görüntüler
            options.inSampleSize = 8;
            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);
            imgPreview.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    // Kaydedilan video önizleniyor
    private void previewVideo() {
        try {
            // Görüntü önizlemesini gizle
            imgPreview.setVisibility(View.GONE);
            videoPreview.setVisibility(View.VISIBLE);
            videoPreview.setVideoPath(fileUri.getPath());

            // Oynatmaya başlayın
            videoPreview.start();

            //Medya Denetleyicisi oluşturuyoruz.
            videoPreview.setMediaController(new MediaController(this));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Yardımcı Yöntemler
    // Görüntü ve video saklamak için dosya Uri oluşturma

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    // Görüntü ve video döndürme

    private static File getOutputMediaFile(int type) {

        // Harici Sdcard konumu
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Sdcard yoksa depolama dizini oluştur
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oluşturma başarısız oldu"
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Bir medya dosyası adı oluşturun
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}