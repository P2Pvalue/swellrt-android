package org.swellrt.android;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.swellrt.android.service.SwellRTService;
import org.swellrt.android.service.SwellRTService.SwellRTServiceCallback;
import org.swellrt.model.generic.Model;
import org.waveprotocol.wave.model.wave.InvalidParticipantAddress;

import java.net.MalformedURLException;

public class MainActivity extends Activity implements ServiceConnection, SwellRTServiceCallback {


  private SwellRTService mSwellRT;

  private Intent mSelectModelIntent;

  private EditText txtServerUrl;
  private EditText txtUserName;
  private EditText txtUserPassword;
  private Button btnLogin;
  private Button btnRegister;

  AsyncTask<String, Void, Boolean> mRegisterTask;

  protected void bindSwellRTService() {

    if (mSwellRT == null) {
      final Intent mWaveServiceIntent = new Intent(this, SwellRTService.class);
      bindService(mWaveServiceIntent, this, Context.BIND_AUTO_CREATE);
    }

  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mSelectModelIntent = new Intent(getApplicationContext(),
        org.swellrt.android.SelectModelActivity.class);

    btnLogin = (Button) findViewById(R.id.button_start_session);
    btnLogin.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        doStartSession();
      }

    });
    btnLogin.setEnabled(false);

    txtServerUrl = (EditText) findViewById(R.id.input_server_url);
    txtUserName = (EditText) findViewById(R.id.input_user_name);
    txtUserPassword = (EditText) findViewById(R.id.input_user_pass);

    mRegisterTask = new AsyncTask<String, Void, Boolean>() {

      @Override
      protected Boolean doInBackground(String... params) {
        return mSwellRT.registerUser(params[0], params[1], params[2]);
      }

      @Override
      protected void onPostExecute(Boolean result) {
        if (result)
          Toast.makeText(MainActivity.this, "User created successfully", Toast.LENGTH_LONG).show();
        else
          Toast.makeText(MainActivity.this, "Error creating user", Toast.LENGTH_LONG).show();
      }
    };

    btnRegister = (Button) findViewById(R.id.button_register_user);
    btnRegister.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        mRegisterTask.execute(txtServerUrl.getText().toString(), txtUserName.getText().toString(),
            txtUserPassword.getText().toString());

      }

    });
    btnRegister.setEnabled(false);

    bindSwellRTService();

  }


  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mSwellRT != null) {
      unbindService(this);
      mSwellRT = null;
    }
  }


  private void doStartSession() {


    try {
      mSwellRT.startSession(txtServerUrl.getText().toString(), txtUserName.getText().toString(),
          txtUserPassword.getText().toString());
    } catch (MalformedURLException e) {

      txtServerUrl.setError("Malformed URL");
      return;

    } catch (InvalidParticipantAddress e) {

      txtUserName.setError("Invalid username format");
      return;

    }

    btnLogin.setEnabled(false);
  }

  // SwellRT Service Callbacks

  @Override
  public void onServiceConnected(ComponentName name, IBinder service) {
    mSwellRT = ((SwellRTService.SwellRTBinder) service).getService(this);
    Log.d(this.getClass().getSimpleName(), "SwellRT Service Bound");

    btnLogin.setEnabled(!mSwellRT.isSessionStarted());
    btnRegister.setEnabled(!mSwellRT.isSessionStarted());


  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    mSwellRT = null;
    Log.d(this.getClass().getSimpleName(), "SwellRT Service unBound");

    btnLogin.setEnabled(false);
  }

  // Wave Service Operational Callbacks

  @Override
  public void onStartSessionSuccess(String session) {
    Toast.makeText(this, "Session started", Toast.LENGTH_LONG).show();
    btnLogin.setEnabled(false);
    startActivity(mSelectModelIntent);
  }

  @Override
  public void onStartSessionFail(String message) {
    Toast.makeText(this, "Login Error: " + message, Toast.LENGTH_LONG).show();
    btnLogin.setEnabled(true);
  }

  @Override
  public void onClose(boolean everythingCommitted) {
    Toast.makeText(this, "Connection closed", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onUpdate(int inFlightSize, int notAckedSize, int unCommitedSize) {
    if (inFlightSize == 0 && notAckedSize == 0 && unCommitedSize == 0)
      Toast.makeText(this, "All data sent", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onDebugInfo(String message) {
    Toast.makeText(this, "Debug: " + message, Toast.LENGTH_LONG).show();
  }

  @Override
  public void onError(String message) {
    Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
  }

  @Override
  public void onCreate(Model model) {
  }

  @Override
  public void onOpen(Model model) {
  }

}
