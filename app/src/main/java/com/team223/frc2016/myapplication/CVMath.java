package com.team223.frc2016.myapplication;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by remad on 11/21/2017.
 */

public class CVMath
{
	public final Mat resultImage;
	public  final  int offCenter;
	private CVMath(Mat result, int off)
	{
		this.resultImage=result;
		this.offCenter=off;
	}

	private static String TAG="CVMath";

	public static CVMath calcImage(Mat image)
	{
		Mat i = new Mat(image.rows(), image.cols(), CvType.CV_8UC1);

		Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2HSV, 3);
		Core.inRange(image, new Scalar(50, 193, 127), new Scalar(65, 255, 255), i);

		List<MatOfPoint> c = new ArrayList<MatOfPoint>();
		Mat h = new Mat();
		Imgproc.findContours(i, c, h, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

		double largest_area = 0;
		int largest_contour_index = 0;
		for (MatOfPoint m : c)
		{
			double area = Imgproc.contourArea(m);
			if (area > largest_area)
			{
				largest_area = area;
				largest_contour_index = c.indexOf(m);
			}
		}

		Moments moments = null;
		try
		{
			moments = Imgproc.moments(c.get(largest_contour_index));
		}
		catch (IndexOutOfBoundsException e)
		{
			Log.i(TAG,"Could not find Contours");
			Imgcodecs.imwrite("/storage/emulated/0/Pictures/Screenshots/failed(" + (failedCount++) + ").png", image);
			return new CVMath(image,Integer.MAX_VALUE);
		}

		Point point = new Point();
		point.x = moments.get_m10() / moments.get_m00();
		point.y = moments.get_m01() / moments.get_m00();

		Imgproc.drawContours(image, c, largest_contour_index, new Scalar(0, 0, 255));

		Imgproc.circle(image, point, 4, new Scalar(255, 0, 0));

		return new CVMath(image,(int)point.x);
	}

	static int failedCount = 0;

	public static int calcData(Mat image)
	{
		Mat i = new Mat(image.rows(), image.cols(), CvType.CV_8UC1);
		Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB, 3);

		Core.inRange(image, new Scalar(0, 100, 0), new Scalar(100, 255, 100), i);

		List<MatOfPoint> c = new ArrayList<MatOfPoint>();
		Imgproc.findContours(i, c, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

		double largest_area = 0;
		int largest_contour_index = 0;
		for (MatOfPoint m : c)
		{
			double area = Imgproc.contourArea(m);
			if (area > largest_area)
			{
				largest_area = area;
				largest_contour_index = c.indexOf(m);
			}
		}
		Moments moments = null;
		try
		{
			moments = Imgproc.moments(c.get(largest_contour_index));
		}
		catch (IndexOutOfBoundsException e)
		{
			e.printStackTrace();
			return Integer.MAX_VALUE;
		} Point point = new Point();
		point.x = moments.get_m10() / moments.get_m00();
		point.y = moments.get_m01() / moments.get_m00();
		return (int) point.x;
	}
}
