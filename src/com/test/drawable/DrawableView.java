package com.test.drawable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.example.camera.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class DrawableView extends View
{
	private Context context;
	private ArrayList<Bitmap> historyBitmaps = null;
	private Bitmap originalBitmap = null;
	private Bitmap currentBitmap = null;
	private Paint paint = null;
	private Canvas drawingCanvas;
	
	private final int UNDO_LIMIT = 5;	//times for Undo history
	private float penSize;
	private float touchX;
	private float touchY;
	private boolean isNew;
	
	public Uri reciveUri;
	public ContentResolver cr;
	public DrawableView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setFocusable(true);
		this.context = context;
		
		initialize();
	}
	
	public void set(String path, Uri uri, ContentResolver cr) {
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		//int screenWidth = metrics.widthPixels;
		//float scale = ((float) screenWidth) / width;
		//try {
			BitmapFactory.Options options = new BitmapFactory.Options();    
			options.inJustDecodeBounds = true;     
			options.inSampleSize = 3;
			options.inJustDecodeBounds = false;
			Bitmap pictureBitmap = BitmapFactory.decodeFile(path,options);
			Matrix mMat = new Matrix() ;
			mMat.setRotate(readImageDegree(path));
			pictureBitmap = Bitmap.createBitmap(pictureBitmap,
                    0,
                    0,
                    pictureBitmap.getWidth(),
                    pictureBitmap.getHeight(),
                    mMat,
                    false);
			originalBitmap = pictureBitmap;
		    currentBitmap = originalBitmap;
		/*} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		Log.e("set","set");
	}
	public static int readImageDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
	            
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException ex) {
			Log.d("讀取錯誤", "----" + ex.getMessage());
			ex.printStackTrace();
		}
		return degree;
	}
	
	private void initialize()
	{
		//get screen size
		
//		int screenHeight = metrics.heightPixels;
		
		//read picture (R.drawable) TODO read from ?? directory
	    
//	    originalBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.test1);
		
		//Undo history
		historyBitmaps = new ArrayList<Bitmap>();
		//create an empty Bitmap with the same size of this View
//		originalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		//currentBitmap used for drawing

		
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.BLACK);
		
		penSize = 10;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		Log.e("DRAW", "DRAW");
		//draw to this View
		if ( currentBitmap != null ) {
			canvas.drawBitmap(currentBitmap, 0, 0, null);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		touchX = event.getX();
		touchY = event.getY();
		
		// TODO getActionMasked()??
		switch (event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			//save currentBitmap
			if(historyBitmaps.size() < UNDO_LIMIT)
			{
				historyBitmaps.add(0, currentBitmap);
			}
			else
			{
				historyBitmaps.remove(UNDO_LIMIT-1);
				historyBitmaps.add(0, currentBitmap);
			}
			//copy an Bitmap from currentBitmap used for drawing
			currentBitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true);
			isNew = true;
			drawToBitmap();
			return true;
		case MotionEvent.ACTION_MOVE:
			drawToBitmap();
			return true;
		case MotionEvent.ACTION_UP:
			//I don't know why
			performClick();
			return true;
		default:
			break;
		}
		
		return super.onTouchEvent(event);
	}
	
	@Override
	public boolean performClick()	//I don't know why
	{
		return super.performClick();
	}
	
	private void drawToBitmap()
	{
		if(isNew)
		{
			drawingCanvas = new Canvas(currentBitmap);
			isNew = false;
		}
		
		drawingCanvas.drawCircle(touchX, touchY, penSize, paint);
		
		invalidate();
	}
	
	public void setPenSize(float size)
	{
		penSize = size;
	}
	
	public void setPenColor(int color)
	{
		paint.setColor(color);
	}
	
	public void save()
	{
		//TODO save to ??
		File file = new File(Environment.getExternalStorageDirectory()+File.separator+"pic.png");
		FileOutputStream fileOutputStream = null;
		try
		{
			fileOutputStream = new FileOutputStream(file, false);
			if(currentBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream))
			{
				Toast.makeText(context, "Save to "+file.getAbsolutePath(), Toast.LENGTH_LONG).show();
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(fileOutputStream != null)
			{
				try
				{
					fileOutputStream.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public void undo()
	{
		if(historyBitmaps.size() > 0)
		{
			currentBitmap = historyBitmaps.get(0);
			historyBitmaps.remove(0);
			invalidate();
		}
	}
	
	public void clean()
	{
		if(historyBitmaps.size() < UNDO_LIMIT)
		{
			historyBitmaps.add(0, currentBitmap);
		}
		else
		{
			historyBitmaps.remove(UNDO_LIMIT-1);
			historyBitmaps.add(0, currentBitmap);
		}
		currentBitmap = originalBitmap;
		
		invalidate();
	}
}
