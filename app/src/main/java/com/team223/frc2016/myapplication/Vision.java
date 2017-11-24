package com.team223.frc2016.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;


public class Vision extends AppCompatActivity
{
	private static final String TAG = "VisionActivity";

	private CVCamera cvCamera = new CVCamera(this);
	private CameraBridgeViewBase cameraBridge;
	private Button mButton;
	private Button sendButton;
	private boolean mIsOn = false;
	private SocketClient sClient;

	private String mIP = "192.168.1.16";
	private int mPort = 50;

	private boolean mIsJavaCamera = true;
	private MenuItem mItemSwitchCamera = null;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)
	{
		@Override
		public void onManagerConnected(int status)
		{
			switch (status)
			{
				case LoaderCallbackInterface.SUCCESS:
				{
					Log.i(TAG, "OpenCV loaded successfully");
					cameraBridge.enableView();
				}
				break;
				default:
				{
					Log.i(TAG, "OTHERSTUFF");
					super.onManagerConnected(status);
				}
				break;
			}
		}
	};

	public Vision()
	{
		Log.i(TAG, "Started VisionActivity");
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_vision);
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
		{
			Log.i(TAG, "I DON'T HAVE PERMISSION TO LOOK AT THE CAMERA!");
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
		}
		mButton = (Button) findViewById(R.id.socket);
		mButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (!mIsOn)
				{
					if (mIP == null)
					{
						sClient = new SocketClient();
					}
					else
					{
						sClient = new SocketClient(mIP, mPort);
					}

					mIsOn = true;
					mButton.setText(R.string.stop);
				}
				else
				{
					closeSocketClient();
					reset();
				}
			}
		});
		sendButton = (Button) findViewById(R.id.send);
		sendButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (sClient != null)
				{
					//sClient.sendImages(sClient.calcImage(cvCamera.getImageBuffer()));
					sendStuff();


				}
				else

				{
					reset();
				}
			}
		});
		cameraBridge = (JavaCameraView)

				findViewById(R.id.camera_view);
		cameraBridge.setVisibility(SurfaceView.VISIBLE);
		cameraBridge.setCvCameraViewListener(cvCamera);
		/*
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera);
		preview.addView(cPreview);*/
	}

	public void sendStuff()
	{
		Thread t = new Thread()
		{
			@Override
			public void run()
			{
				while (true)
				{
					try
					{
						//sClient.calcData(cvCamera.getImageBuffer());
						sClient.sendData(CVMath.calcImage(cvCamera.getImageBuffer()).offCenter);
						Thread.sleep(100);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					catch (NullPointerException e)
					{
						return;
					}
				}
			}
		};
		t.start();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (sClient != null) sClient.close();
		if (cameraBridge != null) cameraBridge.disableView();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if (!OpenCVLoader.initDebug())
		{
			Log.d(TAG, "Could not Init OpenCV");
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
		}
		else
		{
			Log.d(TAG, "Foud OpenCV");
			mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (cameraBridge != null) cameraBridge.disableView();
	}

	private void reset()
	{
		mButton.setText(R.string.start);
		mIsOn = false;
	}


	private void closeSocketClient()
	{
		if (sClient == null) return;

		sClient.interrupt();
		sClient.close();
		try
		{
			sClient.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		sClient = null;
	}
}