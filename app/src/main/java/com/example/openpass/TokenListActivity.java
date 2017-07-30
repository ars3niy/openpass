package com.example.openpass;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class TokenListActivity extends AppCompatActivity {
	
	private ListView tokenList = null;
	private SimpleAdapter adapter = null;
	private List<HashMap<String, String>> tokensData = new ArrayList<HashMap<String, String>>();
	private Vector<TokenInfo> tokens = null;
	private int itemToDelete = -1;
	private static final String LISTITEM_NAME = "name";
	
	private static final int ACTIVITY_ADD_TOKEN = 0;
	private static final int ACTIVITY_ENTER_PIN = 1;
	private static final int ACTIVITY_GENERATE_PASSWORD = 2;
	
	private void updateTokens() {
		tokensData.clear();
		tokens = Settings.loadTokens(this);

		for (TokenInfo token: tokens) {
			HashMap<String, String> map = new HashMap<>();
			map.put(LISTITEM_NAME, token.name);
			tokensData.add(map);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_token_list);
		
		updateTokens();
		
		adapter = new SimpleAdapter(this, tokensData, R.layout.token_item,
				new String[]{LISTITEM_NAME}, new int[]{R.id.listItemTokenName});
		tokenList = (ListView)findViewById(R.id.tokensList);
		tokenList.setAdapter(adapter);

		tokenList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d("OpenPass", "Clicked item " + id);
				TokenInfo token = tokens.elementAt((int) id);

				Bundle extras = new Bundle();
				extras.putString(Schema.COL_NAME, token.name);
				extras.putString(Schema.COL_ACTIVATION_CODE, token.encryptedCode);
				extras.putLong(Schema.COL_NEXT_INDEX, token.nextSequenceIndex);
				extras.putLong(Schema.COL_TOKEN_INDEX, id);

				Intent i = new Intent(TokenListActivity.this, PinEnterActivity.class);
				i.putExtras(extras);
				startActivityForResult(i, ACTIVITY_ENTER_PIN);
			}
		});

		registerForContextMenu(tokenList);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.tokenlist_menu, menu);
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
									ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.tokenlist_context_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.tokenlist_contextmenu_addtoken:
				onAddToken(null);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void deleteToken() {
		tokens.remove(itemToDelete);
		tokensData.remove(itemToDelete);
		Settings.saveTokens(this, tokens);
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.tokenlist_tokencontextmenu_delete:
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
				itemToDelete = info.position;

				new AlertDialog.Builder(this)
						.setTitle("Wait...")
						.setMessage("Orly?")
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								TokenListActivity.this.deleteToken();
							}
						})
						.setNegativeButton(android.R.string.no, null)
						.create().show();

				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onAddToken(View v) {
		Intent i = new Intent(this, AddTokenActivity.class);
		Log.d("OpenPass", "onAddToken");
		startActivityForResult(i, ACTIVITY_ADD_TOKEN);
	}
	
	private void addedToken(Intent sender) {
		Bundle param = sender.getExtras();
		Log.d("OpenPass", "onActivityResult att doken RESULT_OK encrypted: " + param.getString(Schema.COL_ACTIVATION_CODE));

		tokens.add(new TokenInfo(param.getString(Schema.COL_NAME),
				param.getString(Schema.COL_ACTIVATION_CODE), 0));
		Settings.saveTokens(this, tokens);

		HashMap<String, String> map = new HashMap<>();
		map.put(LISTITEM_NAME, param.getString(Schema.COL_NAME));
		tokensData.add(map);
		adapter.notifyDataSetChanged();
	}
	
	private void enteredApparentlyCorrectPin(Intent sender) {
		Bundle param = sender.getExtras();
		Log.d("OpenPass", "onActivityResult enter pin RESULT_OK decrypted: " + param.getString(Schema.COL_ACTIVATION_CODE));

		Bundle extras = new Bundle();
		extras.putString(Schema.COL_ACTIVATION_CODE, param.getString(Schema.COL_ACTIVATION_CODE));
		extras.putLong(Schema.COL_NEXT_INDEX, param.getLong(Schema.COL_NEXT_INDEX, -1));
		extras.putLong(Schema.COL_TOKEN_INDEX, param.getLong(Schema.COL_TOKEN_INDEX, -1));

		Intent i = new Intent(TokenListActivity.this, GeneratorActivity.class);
		i.putExtras(extras);
		startActivityForResult(i, ACTIVITY_GENERATE_PASSWORD);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent sender) {
		super.onActivityResult(requestCode, resultCode, sender);
		Log.d("OpenPass", "onActivityResult");

		if (requestCode == ACTIVITY_ADD_TOKEN) {
			if (resultCode == RESULT_OK)
				addedToken(sender);
		} else if (requestCode == ACTIVITY_ENTER_PIN) {
			if (resultCode == RESULT_OK)
				enteredApparentlyCorrectPin(sender);
		} else if (requestCode == ACTIVITY_GENERATE_PASSWORD) {
			updateTokens();
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onConfigurationChanged (Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
}
