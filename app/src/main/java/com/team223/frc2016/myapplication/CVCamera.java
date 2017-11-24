package com.team223.frc2016.myapplication;

import android.content.Context;
import android.graphics.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.opengl.GLSurfaceView;

import org.opencv.android.CameraBridgeViewBase;

import android.hardware.camera2.CameraManager;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import org.opencv.videoio.VideoCapture;

import java.util.LinkedList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CVCamera implements CameraBridgeViewBase.CvCameraViewListener2
{
	private String TAG = "CVCamera";
	Context view;
	private Mat i;
	private Mat rgba;

	private LinkedList<Mat> mQueue = new LinkedList<Mat>();
	private Mat lastFrame;
	private static final int MAX_BUFFER = 15;
	
	public CVCamera(Context a)
	{
		view = a;
	}

	@Override
	public void onCameraViewStarted(int width, int height)
	{
		rgba = new Mat(height, width, CvType.CV_8UC4);
		i = new Mat(height, width, CvType.CV_8UC4);
	}

	@Override
	public void onCameraViewStopped()
	{
		Camera a=new Camera();
	}



	@Override
	public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
	{
		lastFrame=inputFrame.rgba();
		return inputFrame.rgba();
	}

	public Mat getImageBuffer()
	{
		return lastFrame;
	}
}
