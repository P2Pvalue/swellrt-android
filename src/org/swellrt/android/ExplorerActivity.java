package org.swellrt.android;

import org.swellrt.android.service.SwellRTService;
import org.swellrt.android.service.SwellRTService.SwellRTServiceCallback;
import org.swellrt.model.generic.ListType;
import org.swellrt.model.generic.Model;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ExplorerActivity extends Activity implements ServiceConnection, SwellRTServiceCallback {

  private String mModelId;
  private Model mModel;
  private ListType mList;
  private SwellRTService mSwellRT;

  private EditText txtNewItem;
  private Button btnAddItem;

  private ListView listView;

  protected void bindSwellRTService() {

    if (mSwellRT == null) {
      final Intent mWaveServiceIntent = new Intent(this, SwellRTService.class);
      bindService(mWaveServiceIntent, this, Context.BIND_AUTO_CREATE);

    }

  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_explorer);

    getActionBar().setDisplayShowHomeEnabled(false);

    txtNewItem = (EditText) findViewById(R.id.input_item);
    btnAddItem = (Button) findViewById(R.id.button_add_item);
    btnAddItem.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {

        if (!txtNewItem.getText().toString().isEmpty()) {
          mList.add(mModel.createString(txtNewItem.getText().toString()));
          txtNewItem.getText().clear();
        }

      }
    });

    listView = (ListView) findViewById(R.id.list_items);

    bindSwellRTService();

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.explorer, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {

    case R.id.action_logout:

      doStopSession();
      return true;

    case R.id.action_close_model:

      doCloseModel();
      return true;

    default:
      return super.onOptionsItemSelected(item);
    }

  }

  @Override
  public void onStart() {
    super.onStart();

  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mSwellRT != null) {
      unbindService(this);
      mSwellRT = null;
    }
  }


  private void doStopSession() {

    if (mSwellRT != null) {
      mSwellRT.stopSession();
      NavUtils.navigateUpFromSameTask(this);
    }

  }

  private void doCloseModel() {

    if (mSwellRT != null && mModelId != null) {
      mSwellRT.closeModel(mModelId);
      NavUtils.navigateUpFromSameTask(this);
    }

  }

  // SwellRT Service Callbacks

  @Override
  public void onServiceConnected(ComponentName name, IBinder service) {
    mSwellRT = ((SwellRTService.SwellRTBinder) service).getService(this);
    Log.d(this.getClass().getSimpleName(), "SwellRT Service Bound");

    // Retrieve & Connect to collaborative list

    if (mModelId == null)
      mModelId = getIntent().getStringExtra(AppConstants.EXTRA_MODEL_ID);

    mModel = mSwellRT.getModel(mModelId);

    if (mModel.getRoot().keySet().size() == 0) {
      mModel.getRoot().put("list", mModel.createList());
    }

    mList = (ListType) mModel.getRoot().get("list");

    ListTypeAdapter listAdapter = new ListTypeAdapter(this, mList);
    listView.setAdapter(listAdapter);
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    mSwellRT = null;
  }

  @Override
  public void onStartSessionSuccess(String session) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onStartSessionFail(String error) {
    // TODO Auto-generated method stub

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
    // TODO Auto-generated method stub

  }

  @Override
  public void onOpen(Model model) {
    // TODO Auto-generated method stub

  }

}
