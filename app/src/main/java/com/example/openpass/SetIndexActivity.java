package com.example.openpass;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

public class SetIndexActivity extends AppCompatActivity {
	public static final int RESULT_SUSPENDED = 5;
	
	private EditText indexInput = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_index);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
		
		indexInput = (EditText)findViewById(R.id.indexInput);
		indexInput.setText(String.valueOf(getIntent().getLongExtra(Schema.COL_NEXT_INDEX, 0)));
	}
	
	public void onAcceptIndex(View v) {
		Intent result = new Intent();
		Bundle extras = new Bundle();
		extras.putLong(Schema.COL_NEXT_INDEX, Long.valueOf(indexInput.getText().toString()));
		result.putExtras(extras);
		setResult(RESULT_OK, result);
		finish();
	}
	
	public void onCancelIndex(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	@Override
	public void onPause() {
		setResult(RESULT_SUSPENDED);
		finish();
		super.onPause();
	}

	@Override
	public void onConfigurationChanged (Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
}
