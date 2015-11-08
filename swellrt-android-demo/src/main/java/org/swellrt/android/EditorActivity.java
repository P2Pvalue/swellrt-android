package org.swellrt.android;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import org.swellrt.android.service.SwellRTActivity;
import org.swellrt.android.service.SwellRTFragmentActivity;
import org.swellrt.android.service.WaveDocEditorBinder;
import org.swellrt.model.generic.Model;
import org.swellrt.model.generic.TextType;
import org.swellrt.model.generic.Type;

public class EditorActivity extends SwellRTFragmentActivity implements AddUserDialogFragment.Listener {

  private EditText mEditor;
  private String mModelId;
  private Model mModel;
  private TextType mText;

  private WaveDocEditorBinder mDocBinder;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_editor);

    mEditor = (EditText) findViewById(R.id.editor);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.editor, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {

    case R.id.action_logout:

      getService().stopSession();
      return true;

    case R.id.action_close_model:

      getService().closeModel(mModelId);
      NavUtils.navigateUpFromSameTask(this);
      return true;

    case R.id.action_adduser:

        DialogFragment dialogFragment = new AddUserDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "addusertag");

        return true;


    default:
      return super.onOptionsItemSelected(item);
    }

  }

  @Override
  public void onConnect() {

    if (mModelId == null)
      mModelId = getIntent().getStringExtra(AppConstants.EXTRA_MODEL_ID);

    mModel = getService().getModel(mModelId);

    // Get the test document
    Type instance = mModel.getRoot().get("_doc_");
    if (instance == null) {
      mText = mModel.createText("Hello World! this is an initialization text.");
      mModel.getRoot().put("_doc_", mText);
    } else {
      mText = (TextType) instance;
    }

    // Connect the EditText to the Wave's Doc
    mDocBinder = WaveDocEditorBinder.bind(mEditor, getService().getReadableDocument(mText));

  }

  @Override
  public void onDisconnect() {

  }

    @Override
    public void onAddUserDialogAdd(String user) {
        mModel.addParticipant(user);
    }

    @Override
    public void onAddUserDialogCancel() {

    }
}
