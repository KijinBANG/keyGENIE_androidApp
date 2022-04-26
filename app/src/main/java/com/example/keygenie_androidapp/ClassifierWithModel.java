package com.example.keygenie_androidapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Pair;

import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//클래스에 있는 static final 변수만을 import (static 을 붙이면 클래스 이름 없이 접근할 수 있음)
import static org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod.NEAREST_NEIGHBOR;

public class ClassifierWithModel {
    //TensorFlow Lite 파일의 파일명
    private static final String MODEL_NAME = "mobilenet_imagenet_model.tflite";
    //안드로이드에서 화면에 대한 정보를 저장한 클래스가 Context
    Context context;

    //입출력 텐서를 구하는 코드
    //입력 이미지의 너비와 높이 및 채널 수를 위한 변수
    int modelInputWidth, modelInputHeight, modelInputChannel;
    //입력 이미지 변수
    TensorImage inputImage;
    //출력 변수
    TensorBuffer outputBuffer;

    //라벨 파일 이름 과 라벨 목록을 저장할 변수 선언
    private static final String LABEL_FILE = "labels.txt";
    private List<String> labels;

    //생성자에서 Context 를 넘겨받아서 초기화
    public ClassifierWithModel(Context context) {
        this.context = context;
    }

    //모델을 가져오는 코드
    Model model;
    public void init() throws IOException {
        model = Model.createModel(context, MODEL_NAME);
        //입출력 텐서를 설정하는 메소드 호출
        this.initModelShape();

        //텍스트 파일의 내용을 불러와서 메모리에 저장
        //Context - 문맥
        labels = FileUtil.loadLabels(context, LABEL_FILE);
    }


    //입출력 텐서를 설정하는 메소드
    private void initModelShape() {
        //모델의 입력 덴서를 가져와서 입력 데이터의 크기와 채널수를 설정
        Tensor inputTensor = model.getInputTensor(0);
        int[] shape = inputTensor.shape();
        modelInputChannel = shape[0];
        modelInputWidth = shape[1];
        modelInputHeight = shape[2];

        inputImage = new TensorImage(inputTensor.dataType());

        //출력 텐서를 만드는 부분
        Tensor outputTensor = model.getOutputTensor(0);
        outputBuffer =
                TensorBuffer.createFixedSize(outputTensor.shape(),
                        outputTensor.dataType());
    }

    //이미지 변환 메소드
    private Bitmap convertBitmapToARGB8888(Bitmap bitmap) {
        return bitmap.copy(Bitmap.Config.ARGB_8888,true);
    }

    //이미지 전처리를 위한 메소드
    //안드로이드의 이미지를 받아서 전처리를 수행해서 리턴하는 메소드
    private TensorImage loadImage(final Bitmap bitmap) {
        //bitmap 를 읽어서 입력 텐서로 변환
        if(bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            inputImage.load(convertBitmapToARGB8888(bitmap));
        } else {
            inputImage.load(bitmap);
        }
        
        //이미지의 크기를 변환하고 값을 정규화
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeOp(modelInputWidth, modelInputHeight,
                                NEAREST_NEIGHBOR))
                        .add(new NormalizeOp(0.0f, 255.0f))
                        .build();
        return imageProcessor.process(inputImage);
    }

    //추론 결과와 레이블을 매핑하는 메소드
    //분류 모델은 ㅜㅊ론을 하게 되면 클래스 넘버와 확률을 리턴하는데,
    //레이블 이름과 확률을 받아서 가장 높은 확률을 가진 항목을 리턴
    //Map<String, Float> 자료구조를 처리하는 argmax 메소드
    private Pair<String, Float> argmax(Map<String, Float> map) {
        String maxKey = "";
        float maxVal = -1;
        for(Map.Entry<String, Float> entry : map.entrySet()) {
            float f = entry.getValue();
            if(f > maxVal) {
                maxKey = entry.getKey();
                maxVal = f;
            }
        }
        return new Pair<>(maxKey, maxVal);
    }

    //이미지와 기기방향을 매개변수로 받아서 추론하는 메소드
    //모델 출력 과 label 매핑
    public Pair<String, Float> classify(Bitmap image, int sensorOrientation) {
        inputImage = loadImage(image);
        Object[] inputs = new Object[]{inputImage.getBuffer()};
        Map<Integer, Object> outputs = new HashMap();
        outputs.put(0, outputBuffer.getBuffer().rewind());
        model.run(inputs, outputs);
        Map<String, Float> output =
                new TensorLabel(labels, outputBuffer).getMapWithFloatValue();
        return argmax(output);
    }
    
    //모델 출력 과 label 매핑 - 기기 방향 없이 추론하는 메소드 - 아래의 코드는 파이썬에서는 필요없음!(이유를 생각해보자!)
    //자바의 문제점 중 하나는 매개변수의 초기화 지원하지 않음! (자바 이후의 언어들은 대부분 지원!)
    public Pair<String, Float> classify(Bitmap image) {
        return classify(image, 0);
    }

    //자원 해제 메소드 - 있는지 없는지 확인하고 클로즈!
    public void finish() {
        if(model != null) {
            model.close();
        }
    }
}
