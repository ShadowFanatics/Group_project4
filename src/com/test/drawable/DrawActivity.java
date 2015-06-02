package com.test.drawable;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class DrawActivity extends Activity
{
	private DrawableView drawableView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        
        drawableView = (DrawableView) findViewById(R.id.drawableView);
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
		default:
			break;
		}
        return super.onOptionsItemSelected(item);
    }
    
    
}
