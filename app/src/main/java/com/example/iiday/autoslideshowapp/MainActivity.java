package com.example.iiday.autoslideshowapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int IMAGE_NUM = 3;
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    Uri m_imageUris;
    boolean m_mode;
    int m_index;
    Button m_btnPlay;
    Button m_btnPrev;
    Button m_btnFwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_index = 0;
        m_mode = true;
        m_btnPrev = (Button)findViewById(R.id.buttonPre);
        m_btnPrev.setOnClickListener(this);
        m_btnFwd = (Button)findViewById(R.id.buttonFwd);
        m_btnFwd.setOnClickListener(this);
        m_btnPlay = (Button)findViewById(R.id.buttonPlay);
        m_btnPlay.setOnClickListener(this);

        // パーミッション確認不要
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if(!getContentsInfo()){
                Toast.makeText(this, "画像が足りません", Toast.LENGTH_LONG).show();
            }
            nextimage(0);
            return;
        }
        // パーミッション許可されてる場合
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            if(!getContentsInfo()) {
                Toast.makeText(this, "画像が足りません", Toast.LENGTH_LONG).show();
            }
            nextimage(0);
            return;
        }
        // パーミッション設定が必要
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int [] grantResults){
        if(requestCode != PERMISSIONS_REQUEST_CODE) {
            return;
        }
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "権限を設定してください", Toast.LENGTH_LONG).show();
            return;
        }
        if(!getContentsInfo()){
            Toast.makeText(this, "画像が足りません", Toast.LENGTH_LONG).show();
            return;
        }
        nextimage(0);
    }

    // 画像URIの取得
    private boolean getContentsInfo(){
        int i=0;

        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null
        );
        if(!cursor.moveToFirst()){
            cursor.close();
            return false;
        }
        for(i=0; i < IMAGE_NUM; i++){
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            m_imageUris[i] = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            if(!cursor.moveToNext()){
                break;
            }
        }
        cursor.close();
        if(i != IMAGE_NUM){
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v){
        // 再生/停止ボタン
        if(v.getId() == R.id.buttonPlay){
            if(m_mode){
                saisei();  // 再生
            }
            else{
                teishi();  // 停止
            }
        }
        // 進むボタン
        else if(v.getId() == R.id.buttonFwd){
            nextimage(1);
        }
        // 戻るボタン
        else if(v.getId() == R.id.buttonPre){
            nextimage(-1);
        }
    }
    // スライドショー再生処理
    private void saisei(){
        m_btnPlay.setText("停止");
        m_btnFwd.setEnabled(false);
        m_btnPrev.setEnabled(false);
        m_mode = false;
    }
    // スライドショー停止処理
    private void teishi(){
        m_btnPlay.setText("再生");
        m_btnFwd.setEnabled(true);
        m_btnPrev.setEnabled(true);
        m_mode = true;
    }
    // 画像変更処理
    private void nextimage(int next){
        m_index = m_index + next;
        if(m_index < 0){
            m_index = m_index + IMAGE_NUM;
        }
        else if(m_index >= IMAGE_NUM){
            m_index = m_index % IMAGE_NUM;
        }
        // 画像表示
        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageURI(m_imageUris[m_index]);
    }
}
