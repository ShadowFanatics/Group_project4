package com.test.drawable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.example.camera.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
	
	private Button redButton;
	private Button blueButton;
	private Button greenButton;
	
	public DrawableView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setFocusable(true);
		this.context = context;
		
		initialize();
	}
	
	private void initialize()
	{
		//get screen size
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int screenWidth = metrics.widthPixels;
//		int screenHeight = metrics.heightPixels;
		
		//read picture (R.drawable) TODO read from ?? directory
		
		
		
		Bitmap pictureBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.test1);
		int width = pictureBitmap.getWidth();
	    int height = pictureBitmap.getHeight();
//	    float scaleWidth = ((float) screenWidth) / width;
//	    float scaleHeight = ((float) screenHeight) / height;
	    float scale = ((float) screenWidth) / width;
	    // CREATE A MATRIX FOR THE MANIPULATION
	    Matrix matrix = new Matrix();
	    // RESIZE THE BIT MAP
//	    matrix.postScale(scaleWidth, scaleHeight);
	    matrix.postScale(scale, scale);

	    // "RECREATE" THE NEW BITMAP
	    originalBitmap = Bitmap.createBitmap(pictureBitmap, 0, 0, width, height, matrix, false);
	    
//	    originalBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.test1);
		
		//Undo history
		historyBitmaps = new ArrayList<Bitmap>();
		//create an empty Bitmap with the same size of this View
//		originalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		//currentBitmap used for drawing
		currentBitmap = originalBitmap;
		
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.BLACK);
		
		penSize = 10;
		//iniViews();
		
	}
	
	private void iniViews(){
		redButton = (Button)findViewById(R.id.button1);
		blueButton = (Button)findViewById(R.id.button2);
		greenButton = (Button)findViewById(R.id.button3);
		
		redButton.setOnClickListener(bClickListener);
		blueButton.setOnClickListener(bClickListener);
		greenButton.setOnClickListener(bClickListener);
	}
	
	private Button.OnClickListener bClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v.getId() == redButton.getId()){
				paint.setColor(Color.RED);
			}
			else if (v.getId() == blueButton.getId()) {
				paint.setColor(Color.BLUE);
			}
			else if(v.getId() == greenButton.getId()){
				paint.setColor(Color.GREEN);
			}
		}
	};

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		//draw to this View
		canvas.drawBitmap(currentBitmap, 0, 0, null);
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
