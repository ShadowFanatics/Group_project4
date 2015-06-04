package com.test.drawable;

import com.example.camera.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;


public class DrawActivity extends Activity
{
	private DrawableView drawableView;
	private SeekBar seekBar;
	private Button pickButton;
	private int seekBarProgress;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        
        initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.draw, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	switch (item.getItemId())
    	{
		case R.id.action_close:
			finish();
        	return true;
		case R.id.action_save:
			drawableView.save();
			break;
		case R.id.action_undo:
			drawableView.undo();
        	return true;
		case R.id.action_clean:
			drawableView.clean();
        	return true;
		case R.id.action_texture:
			drawableView.texture();
        	return true;
		default:
			break;
		}
        return super.onOptionsItemSelected(item);
    }
    
    private void initialize()
    {
    	Bundle extras = getIntent().getExtras();        
        String path = (String) extras.getString("sendString");
        
        drawableView = (DrawableView) findViewById(R.id.drawableView);
        drawableView.set(path, this.getContentResolver());
        
    	seekBarProgress = 5;
    	seekBar = (SeekBar) findViewById(R.id.seekBar1);
    	seekBar.setProgress(0);
    	seekBar.setMax(45);
    	seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
    	{
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				drawableView.setPenSize(seekBarProgress);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser)
			{
				seekBarProgress = 5+progress;
			}
		});
    	
    	pickButton = (Button) findViewById(R.id.button_pick);
    	pickButton.setOnClickListener(new Button.OnClickListener()
    	{
			@Override
			public void onClick(View v)
			{
				showColorPickerDialog();
			}
    	});
    }
    
    private void showColorPickerDialog()
    {
    	HSVColorPickerDialog colorPickerDialog = new HSVColorPickerDialog( DrawActivity.this,
    			drawableView.getPenColor(), new HSVColorPickerDialog.OnColorSelectedListener()
    	{
    	    @Override
    	    public void colorSelected(Integer color)
    	    {
    	    	drawableView.setPenColor(color.intValue());
    	    }
    	});
    	colorPickerDialog.setTitle( "Pick a color" );
    	colorPickerDialog.show();
    }
}
