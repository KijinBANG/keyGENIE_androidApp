package com.example.keygenie_androidapp;

import android.content.Context;
import android.graphics.Bitmap;

import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;

//클래스에 있는 static final 변수만을 import (static 을 붙이면 클래스 이름 없이 접근할 수 있음)
import static org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod.NEAREST_NEIGHBOR;

public class ClassifierWithModel {
    //TensorFlow Lite 파일의 파일명
    private static final String MODEL_NAME = "mobilenet_imagenet_model.tflite";
    //안드로이드에서 화면에 대한 정보를 저장한 클래스가 Context
    Context context;
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
    }

    //입출력 텐서를 구하는 코드
    //입력 이미지의 너비와 높이 및 채널 수를 위한 변수
    int modelInputWidth, modelInputHeight, modelInputChannel;
    //입력 이미지 변수
    TensorImage inputImage;
    //출력 변수
    TensorBuffer outputBuffer;
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

    //이미지 전처리를 위한 메소드
    //안드로이드의 이미지를 받아서 전처리를 수행해서 리턴하는 메소드
    private TensorImage loadImage(final Bitmap bitmap) {
        //bitmap 를 읽어서 입력 텐서로 변환
        inputImage.load(bitmap);
        
        //이미지의 크기를 변환하고 값을 정규화
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeOp(modelInputWidth, modelInputHeight,
                                NEAREST_NEIGHBOR))
                        .add(new NormalizeOp(0.0f, 255.0f))
                        .build();
        return imageProcessor.process(inputImage);
    }
}
