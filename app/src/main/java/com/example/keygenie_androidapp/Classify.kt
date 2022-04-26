//package com.example.keygenie_androidapp
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.util.Pair
//import org.tensorflow.lite.support.common.FileUtil
//import org.tensorflow.lite.support.common.ops.NormalizeOp
//import org.tensorflow.lite.support.image.ImageProcessor
//import org.tensorflow.lite.support.image.TensorImage
//import org.tensorflow.lite.support.image.ops.ResizeOp
//import org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod
//import org.tensorflow.lite.support.label.TensorLabel
//import org.tensorflow.lite.support.model.Model
//import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
//import java.io.IOException
//import java.util.*
//
//class Classify {
//    //TensorFlow Lite 파일의 파일명
//    private val MODEL_NAME = "mobilenet_imagenet_model.tflite"
//
//    //안드로이드에서 화면에 대한 정보를 저장한 클래스가 Context
//    var context: Context? = null
//
//    //입출력 텐서를 구하는 코드
//    //입력 이미지의 너비와 높이 및 채널 수를 위한 변수
//    var modelInputWidth = 0, var modelInputHeight: //입출력 텐서를 구하는 코드
//    //입력 이미지의 너비와 높이 및 채널 수를 위한 변수
//    kotlin.Int = 0,
//    var modelInputChannel//입출력 텐서를 구하는 코드
//    //입력 이미지의 너비와 높이 및 채널 수를 위한 변수
//            = 0
//
//    //입력 이미지 변수
//    var inputImage: TensorImage? = null
//
//    //출력 변수
//    var outputBuffer: TensorBuffer? = null
//
//    //라벨 파일 이름 과 라벨 목록을 저장할 변수 선언
//    private val LABEL_FILE = "labels.txt"
//    private var labels: List<String>? = null
//
//    //생성자에서 Context 를 넘겨받아서 초기화
//    fun ClassifierWithModel(context: Context?) {
//        this.context = context
//    }
//
//    //모델을 가져오는 코드
//    var model: Model? = null
//
//    @kotlin.Throws(IOException::class)
//    fun init() {
//        model = Model.createModel(context!!, MODEL_NAME)
//        //입출력 텐서를 설정하는 메소드 호출
//        initModelShape()
//
//        //텍스트 파일의 내용을 불러와서 메모리에 저장
//        //Context - 문맥
//        labels = FileUtil.loadLabels(context!!, LABEL_FILE)
//    }
//
//
//    //입출력 텐서를 설정하는 메소드
//    private fun initModelShape() {
//        //모델의 입력 덴서를 가져와서 입력 데이터의 크기와 채널수를 설정
//        val inputTensor = model!!.getInputTensor(0)
//        val shape = inputTensor.shape()
//        modelInputChannel = shape[0]
//        modelInputWidth = shape[1]
//        modelInputHeight = shape[2]
//        inputImage = TensorImage(inputTensor.dataType())
//
//        //출력 텐서를 만드는 부분
//        val outputTensor = model!!.getOutputTensor(0)
//        outputBuffer = TensorBuffer.createFixedSize(outputTensor.shape(),
//                outputTensor.dataType())
//    }
//
//    //이미지 전처리를 위한 메소드
//    //안드로이드의 이미지를 받아서 전처리를 수행해서 리턴하는 메소드
//    private fun loadImage(bitmap: Bitmap): TensorImage? {
//        //bitmap 를 읽어서 입력 텐서로 변환
//        inputImage!!.load(bitmap)
//
//        //이미지의 크기를 변환하고 값을 정규화
//        val imageProcessor = ImageProcessor.Builder()
//                .add(ResizeOp(modelInputWidth, modelInputHeight, ResizeMethod.NEAREST_NEIGHBOR))
//                .add(NormalizeOp(0.0f, 255.0f))
//                .build()
//        return imageProcessor.process(inputImage)
//    }
//
//    //추론 결과와 레이블을 매핑하는 메소드
//    //분류 모델은 ㅜㅊ론을 하게 되면 클래스 넘버와 확률을 리턴하는데,
//    //레이블 이름과 확률을 받아서 가장 높은 확률을 가진 항목을 리턴
//    //Map<String, Float> 자료구조를 처리하는 argmax 메소드
//    private fun argmax(map: Map<String, Float>): Pair<String, Float>? {
//        var maxKey = ""
//        var maxVal = -1f
//        for ((key, f) in map) {
//            if (f > maxVal) {
//                maxKey = key
//                maxVal = f
//            }
//        }
//        return Pair(maxKey, maxVal)
//    }
//
//    //이미지와 기기방향을 매개변수로 받아서 추론하는 메소드
//    //모델 출력 과 label 매핑
//    fun classify(image: Bitmap, sensorOrientation: Int): Pair<String, Float>? {
//        inputImage = loadImage(image)
//        val inputs = arrayOf<Any>(inputImage!!.buffer)
//        val outputs: MutableMap<Int?, Any?> = HashMap<Any?, Any?>()
//        outputs.put(0, outputBuffer!!.buffer.rewind())
//        model!!.run(inputs, outputs)
//        val output = TensorLabel(labels!!, outputBuffer!!).mapWithFloatValue
//        return argmax(output)
//    }
//
//    //모델 출력 과 label 매핑 - 기기 방향 없이 추론하는 메소드 - 아래의 코드는 파이썬에서는 필요없음!(이유를 생각해보자!)
//    //자바의 문제점 중 하나는 매개변수의 초기화 지원하지 않음! (자바 이후의 언어들은 대부분 지원!)
//    fun classify(image: Bitmap): Pair<String, Float>? {
//        return classify(image, 0)
//    }
//
//    //자원 해제 메소드 - 있는지 없는지 확인하고 클로즈!
//    fun finish() {
//        if (model != null) {
//            model!!.close()
//        }
//    }
//}