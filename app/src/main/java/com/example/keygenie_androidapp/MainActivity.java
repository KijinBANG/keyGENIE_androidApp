package com.example.keygenie_androidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    //객체를 만들어놓고, 쓸 수 있게 해 주는 것
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //버튼 클릭 이벤트 핸들러 작성
        Button galleryBtn = findViewById(R.id.galleryBtn);
        //클릭 이벤트 처리
        galleryBtn.setOnClickListener(view -> {
            //새로운 Activity 호출 - 'Intent 사용이유' - 화면과 데이터를 같이 넘겨주기 위함!
            Intent i = new Intent(MainActivity.this, GalleryActivity.class);
            startActivity(i);
        });
        Button cameraBtn = findViewById(R.id.cameraBtn);
        cameraBtn.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(i);
        });
    }
}