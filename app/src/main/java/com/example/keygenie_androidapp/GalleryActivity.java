package com.example.keygenie_androidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class GalleryActivity extends AppCompatActivity {
    //필요한 변수 선언 - 로그 출력 시 사용하는 태그
    public static final String TAG = "[IC]GalleryActivity";
    //갤러리 이미지를 부르고 돌아올 때 어떤 작업을 수행했는지 확인하기 위한 코드
    public static final int GALLERY_IMAGE_REQUEST_CODE = 1;
    //분류 모델
    private ClassifierWithModel cls;

    //UI요소
    private ImageView imageView;
    private TextView textView;
    private Button selectBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        //초기화 코드
        selectBtn = findViewById(R.id.selectBtn);
        selectBtn.setOnClickListener(v -> getImageFromGallery());

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);

        cls = new ClassifierWithModel(this);
        try {
            cls.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //startActivityForResult를 호출해서 새로운 Activity가 출력되고 출력된 Activity가 화면에서 사라지면 호출되는 메소드 재정의
    //겔러리에서 사진을 선택하고 나면 호출되는 메소드
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //상위 클래스의 메소드 호출
        super.onActivityResult(requestCode, resultCode, data);
        //화면에 출력할 겔러리에서 O.K. 버튼을 누른 것이라면,
        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_IMAGE_REQUEST_CODE) {
            //선택된 이미지가 없다면 좋료
            if (data == null) {
                return;
            }
            //선택된 이미지 불러오기
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                //운영체제 버전에 따른 코드
                if (Build.VERSION.SDK_INT >= 29) {
                    Uri fileUri = data.getData();
                    ContentResolver resolver = getContentResolver();
                    InputStream inputStream = resolver.openInputStream(fileUri);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
                            selectedImage);
                }
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to read Image", ioe);
            }
            //읽어온 이미지가 있다면
            if (bitmap != null) {
                //추론
                Pair<String, Float> output = cls.classify(bitmap);
                //추론 결과를 출력
                String resultStr = String.format(Locale.ENGLISH,
                        "class : %s, prob : %.2f%%",
                        output.first, output.second * 100);
                textView.setText(resultStr);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    //갤러리를 화면에 출력하는 메소드를 생성
    private void getImageFromGallery() {
        //겔러리 앱
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
        //겔러리 화면을 출력하는데 구분하기 위한 숫자를 같이 대입 (콜백함수를 매개변수로!)
        startActivityForResult(intent, GALLERY_IMAGE_REQUEST_CODE);
    }

}