package com.example.camera_17;

import java.io.FileNotFoundException;
import java.io.IOException;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Images.Media;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;


public class MainActivity extends Activity {
	//宣告
	private ImageView photo;
	private DisplayMetrics mPhone;
	private Button button_shot;
	private Button button_view;
	private Button button_edit;
	   
	private final static int CAMERA = 66 ;
	private final static int PHOTO = 99 ;
	        
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	                
		//讀取手機解析度
		mPhone = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(mPhone);
	      
		setView();
		setListener();
	      
	}
	private void setView(){
		photo = (ImageView) findViewById(R.id.photo);
		button_shot = (Button)findViewById(R.id.button_shot);
		button_view = (Button)findViewById(R.id.button_view);
		button_edit = (Button)findViewById(R.id.button_edit);
	}
	private void setListener(){
		button_shot.setOnClickListener(btnShot);
		button_view.setOnClickListener(btnView);
		button_edit.setOnClickListener(btnEdit);
	}
	private OnClickListener btnShot = new OnClickListener(){
		@Override
		public void onClick(View v) 
		{
			//開啟相機功能，並將拍照後的圖片存入SD卡相片集內，須由startActivityForResult且
			//帶入 requestCode進行呼叫，原因為拍照完畢後返回程式後則呼叫onActivityResult
			ContentValues value = new ContentValues();
			value.put(MediaColumns.MIME_TYPE, "image/jpeg");                                      
			Uri uri= getContentResolver().insert(Media.EXTERNAL_CONTENT_URI,
		                                              value); 
			Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri.getPath());  
			startActivityForResult(intent, CAMERA);      
	    }
	};
	private OnClickListener btnView = new OnClickListener(){
		@Override
		public void onClick(View v) 
		{
			//開啟相簿相片集，須由startActivityForResult且帶入requestCode進行呼叫，原因
			//為點選相片後返回程式呼叫onActivityResult
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, PHOTO);
		}
	};
	private OnClickListener btnEdit = new OnClickListener(){
		@Override
		public void onClick(View v) 
		{
		       
		}
	};
	        
	//拍照完畢或選取圖片後呼叫此函式
	@Override 
	protected void onActivityResult(int requestCode, int resultCode,Intent data)
	{
		//藉由requestCode判斷是否為開啟相機或開啟相簿而呼叫的，且data不為null
		if ((requestCode == CAMERA || requestCode == PHOTO ) && data != null)
		{
			//取得照片路徑uri
			Uri uri = data.getData();
			ContentResolver cr = this.getContentResolver();	         
			String path = getImagePath(uri);
	                    
			try{
				//讀取照片，型態為Bitmap
				Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
		         
				//判斷照片為橫向或者為直向，並進入ScalePic判斷圖片是否要進行縮放
				if(bitmap.getWidth()>bitmap.getHeight()){
					ScalePic(bitmap,mPhone.heightPixels, readImageDegree(path));
		        	Log.i("tag","旋轉"+Integer.toString(readImageDegree(path)));
		        }
		        else{ 
		        	ScalePic(bitmap,mPhone.widthPixels,readImageDegree(path));
		        	Log.i("tag","旋轉"+Integer.toString(readImageDegree(path))+path);
		        }
		        //rotateBitmap(readImageDegree(uri.toString()),bitmap);
		        //photo.setImageBitmap(bitmap);
			} 
			catch (FileNotFoundException e){
			}
		}	                
		super.onActivityResult(requestCode, resultCode, data);	      
	}
	public String getImagePath(Uri uri){
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		cursor.moveToFirst();
		String document_id = cursor.getString(0);
		document_id = document_id.substring(document_id.lastIndexOf(":")+1);
		cursor.close();

		cursor = getContentResolver().query( 
		android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
		null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
		cursor.moveToFirst();
		String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
		cursor.close();

		return path;
	}
	public String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try { 
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
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
	private void ScalePic(Bitmap bitmap,int phone,int angle)
	{
		Matrix mMat = new Matrix() ;
		   	   
		//縮放比例預設為1
		float mScale = 1 ;
	                
		//如果圖片寬度大於手機寬度則進行縮放，否則直接將圖片放入ImageView內
		if(bitmap.getWidth() > phone )
		{	    	  
			//判斷縮放比例
			mScale = (float)phone/(float)bitmap.getWidth();	                      
			  
			mMat.setScale(mScale, mScale);
			//轉正
			mMat.postRotate(angle);
			Bitmap mScaleBitmap = Bitmap.createBitmap(bitmap,
	                                                   0,
	                                                   0,
	                                                   bitmap.getWidth(),
	                                                   bitmap.getHeight(),
	                                                   mMat,
	                                                   false);
			photo.setImageBitmap(mScaleBitmap);
			Log.i("tag","resize");
			bitmap.recycle();			   
		}
		else {
	    	  
			Log.i("tag","not resize");
			photo.setImageBitmap(bitmap);
			bitmap.recycle();
	    	  
		}
	}
}
