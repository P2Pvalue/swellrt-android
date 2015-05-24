package org.swellrt.android;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.swellrt.android.service.SwellRTService;
import org.swellrt.android.service.SwellRTService.SwellRTServiceCallback;
import org.swellrt.model.generic.Model;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class SelectModelActivity extends Activity implements ServiceConnection,
    SwellRTServiceCallback {


  private static final String PREFS_RECENTS_MODELS = "SwellRT-Recent-Models";

  private SwellRTService mSwellRT;

  private EditText txtModelId;
  private Button btnCreate;
  private Button btnOpen;
  private ListView mRecentList;

  private Intent mExplorerIntent;
  private List<String> mRecentOpenModels;
  private SharedPreferences mPrefs;
  private SharedPreferences.Editor mPrefsEditor;

  private String mOpeningModelId;

  protected void bindSwellRTService() {

    if (mSwellRT == null) {

      final Intent mWaveServiceIntent = new Intent(this, SwellRTService.class);
      bindService(mWaveServiceIntent, this, Context.BIND_AUTO_CREATE);

    }

  }

  protected void checkSessionSwellRTService() {

    if (!mSwellRT.isSessionStarted()) {
      // If session is gone, go back to first activity
      Toast.makeText(this, "No open session", Toast.LENGTH_SHORT).show();
      NavUtils.navigateUpFromSameTask(this);
    }

  }

  @SuppressWarnings("unchecked")
  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_select_model);

    getActionBar().setDisplayShowHomeEnabled(false);

    // Intents

    mExplorerIntent = new Intent(getApplicationContext(),
        org.swellrt.android.ExplorerActivity.class);

    btnCreate = (Button) findViewById(R.id.button_create_model);
    btnCreate.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {

        mOpeningModelId = mSwellRT.createModel();

      }

    });
    btnCreate.setEnabled(false);

    btnOpen = (Button) findViewById(R.id.button_open_model);
    btnOpen.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {

        mOpeningModelId = txtModelId.getText().toString();

        if (mOpeningModelId != null)
          mSwellRT.openModel(mOpeningModelId);

      }

    });
    btnOpen.setEnabled(false);

    bindSwellRTService();

    // Get list of recent opened models
    mPrefs = getSharedPreferences(PREFS_RECENTS_MODELS, 0);
    mPrefsEditor = mPrefs.edit();

    // List for recent opened models
    mRecentList = (ListView) findViewById(R.id.list_open_recent);
    mRecentList.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        ListView list = (ListView) adapterView;
        String modelId = (String) list.getAdapter().getItem(position);
        doSelectRecentModel(modelId);
      }

    });

    txtModelId = (EditText) findViewById(R.id.input_model_id);
  }



  public void onStart() {
    super.onStart();
    loadRecentModels();
  }

  @Override
  public void onStop() {
    super.onStop();
    unbindService(this);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.select_model, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {

    case R.id.action_logout:
      doStopSession();
      return true;

    default:
      return super.onOptionsItemSelected(item);
    }

  }


  private void loadRecentModels() {

    // Reload list of recent
    mRecentOpenModels = new ArrayList<String>((Collection<? extends String>) mPrefs.getAll()
        .keySet());

    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1, mRecentOpenModels);

    mRecentList.setAdapter(adapter);
  }

  private void doSelectRecentModel(String modelId) {
    txtModelId.setText(modelId);
  }

  private void doStopSession() {

    if (mSwellRT != null) {
      mSwellRT.stopSession();
      NavUtils.navigateUpFromSameTask(this);
    }

  }


  // SwellRT Service Callbacks

  @Override
  public void onServiceConnected(ComponentName name, IBinder service) {
    mSwellRT = ((SwellRTService.SwellRTBinder) service).getService(this);
    Log.d(this.getClass().getSimpleName(), "SwellRT Service Bound");

    checkSessionSwellRTService();

    btnOpen.setEnabled(true);
    btnCreate.setEnabled(true);
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    mSwellRT = null;

    btnOpen.setEnabled(false);
    btnCreate.setEnabled(false);
  }


  @Override
  public void onStartSessionSuccess(String session) {

  }

  @Override
  public void onStartSessionFail(String error) {

  }


  @Override
  public void onClose(boolean everythingCommitted) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onUpdate(int inFlightSize, int notAckedSize, int unCommitedSize) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onError(String message) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onDebugInfo(String message) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onCreate(Model model) {

    if (model != null) {

      // Add modelId to recent list
      mPrefsEditor.putString(mOpeningModelId, mOpeningModelId);
      mPrefsEditor.commit();

      // start explorer acitvity
      mExplorerIntent.putExtra(AppConstants.EXTRA_MODEL_ID, mOpeningModelId);
      startActivity(mExplorerIntent);

    } else {
      Toast.makeText(this, "Model can't be created", Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public void onOpen(Model model) {

    if (model != null) {

      // Add modelId to recent list
      mPrefsEditor.putString(mOpeningModelId, mOpeningModelId);
      mPrefsEditor.commit();

      // start explorer acitvity
      mExplorerIntent.putExtra(AppConstants.EXTRA_MODEL_ID, mOpeningModelId);
      startActivity(mExplorerIntent);

    } else {
      Toast.makeText(this, "Model can't be opened", Toast.LENGTH_LONG).show();
    }
  }


}
