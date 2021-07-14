package com.vetrack.vetrack.TensorFlow;

import android.content.res.AssetManager;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class TensorFlowVdrSpeed implements VdrSpeed {

    private TensorFlowInferenceInterface inferenceInterface;
    private String inputName;
    private String outputName;

    private String[] outputNames;
    private float[] outputs;

    public static VdrSpeed create(AssetManager assetManager,
                                  String modelFilename, String inputName,
                                  String outputName, int outputSize) {
        TensorFlowVdrSpeed vdrSpeed = new TensorFlowVdrSpeed();
        vdrSpeed.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);
        vdrSpeed.inputName = inputName;
        vdrSpeed.outputName = outputName;

        vdrSpeed.outputNames = new String[]{outputName};
        vdrSpeed.outputs = new float[outputSize];
        return vdrSpeed;
    }

    @Override
    public Prediction predict(float[] acc) {

        // Log this method so that it can be analyzed with systrace.
//        Trace.beginSection("test");
//        System.out.println("test");
//        Trace.endSection();

        // Copy the input data into TensorFlow.
        //Trace.beginSection("feed");
        inferenceInterface.feed(inputName, acc, 1, 30, 150);
        //Trace.endSection();

        // Run the inference call.
        //Trace.beginSection("run");
        inferenceInterface.run(outputNames);
        //Trace.endSection();

        // Copy the output Tensor back into the output array.
        //Trace.beginSection("fetch");
        inferenceInterface.fetch(outputName, outputs);
        //Trace.endSection();

        return new Prediction(0, outputs[29]);
    }
}
