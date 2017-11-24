package com.team223.frc2016.myapplication;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


import android.util.Log;

import com.google.gson.JsonObject;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public class SocketClient extends Thread
{

	private Socket mSocket;
	private static final String TAG = "socket";
	private String mIP = "192.168.1.16";
	private int mPort = 8080;
	private boolean ok;

	public SocketClient(String ip, int port)
	{
		mIP = ip;
		mPort = port;
		start();
	}

	public SocketClient()
	{
		start();
	}

	@Override
	public void run()
	{
		super.run();
		try
		{
			//Create socket and datastreams
			mSocket = new Socket();
			Log.i(TAG, mIP + " " + mPort);
			mSocket.connect(new InetSocketAddress(mIP, mPort), 10000);
			ok = true;
		}
		catch (Exception e)
		{
			ok = false;
			e.printStackTrace();
		}
		finally
		{
			if (!ok)
			{
				Log.e(TAG, "Closing Because something was not ok");
				try
				{
					mSocket.close();
					mSocket = null;
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void close()
	{
		if (mSocket == null) return;
		try
		{
			mSocket.close();
			mSocket = null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}




	public synchronized void sendData(final int offCenter)
	{
		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					DataOutputStream outputStream = new DataOutputStream(mSocket.getOutputStream());
					outputStream.writeInt(offCenter);
					outputStream.flush();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}.start();
	}

	public void sendImages(final Mat imageBuffer)
	{
		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					BufferedOutputStream outputStream = new BufferedOutputStream(mSocket.getOutputStream());
					BufferedInputStream inputStream = new BufferedInputStream(mSocket.getInputStream());

					byte[] data = new byte[(int) imageBuffer.total() * imageBuffer.channels()];
					imageBuffer.get(0, 0, data);

					JsonObject jsonObj = new JsonObject();
					jsonObj.addProperty("length", data.length);
					jsonObj.addProperty("height", imageBuffer.height());
					jsonObj.addProperty("width", imageBuffer.width());
					jsonObj.addProperty("type", imageBuffer.type());
					outputStream.write(jsonObj.toString().getBytes());
					outputStream.flush();
					try
					{
						Thread.sleep(200);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}


					outputStream.write(data);
					outputStream.flush();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				super.run();
			}

		}.start();
	}

	public int[] getBytesFromMat(Mat mat)
	{
		byte[] temp = new byte[(int) (mat.total() * mat.channels())];
		mat.get(0, 0, temp);

		MatOfInt rgb = new MatOfInt(CvType.CV_32S);
		mat.convertTo(rgb, CvType.CV_32S);
		int[] data = new int[(int) (rgb.total() * rgb.channels())];
		rgb.get(0, 0, data);

		return data;
	}
}
