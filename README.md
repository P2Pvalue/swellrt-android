# SwellRT Android

This is an Android Studio to develop and try the **SwellRT client for Android**.
Modules in this project are:

- **SwellRT Client Library for Android** (/swell-client-android)
- **Demo Android app** (/swell-android-demo)

*This project is unstable yet, changes in the API syntax are expected.
Transport protocol is long-polling.*

# Using SwellRT in your Android App

To use SwellRT in your Android app you have to add the provided */swell-client-android* module to
your project and configure dependencies from other modules as it's shown in this project.

The project uses Gradle to resolve dependencies automatically. See section **Getting SwellRT dependencies** for more info.

## Getting SwellRT dependencies

We don't distribute, on any public Maven repository, the *swellrt-client-commons-0.XX.Y-alpha.jar* library file, containing
shared code between SwellRT server and clients.

You must build this library cloning the [SwellRT project](https://github.com/P2Pvalue/swellrt)
and following these steps:

Build *swellrt-client-commons* library Jar using the Ant script in the SwellRT project's folder:

```
$ant -f build-swellrt.xml dist-swellrt-client-commons
```

Install Jar library as Maven artifact locally

```
$ant -f build-swellrt.xml swellrt-mvn-install-client-commons
```

Or deploy the maven artifact in a remote repository

```
$ant -f build-swellrt.xml swellrt-mvn-deploy-client-commons
```

Please, check the version of the generated artifact and update the dependency accordingly
in the *swellrt-client-android/build.gradle* file:

```
dependencies {
    compile 'org.swellrt:swellrt-client-commons:0.XX.Y-alpha'
}
```



## SwellRT Android Service

SwellRT API is provided through an Android Service. It must be declared in the  *AndroidManifest.xml* file of your app:

```
<manifest>
    <application>
        <service android:name="org.swellrt.android.service.SwellRTService"></service>
    </application>
</manifest>
```

SwellRT is intended to be used from different activities, for example, one activity responsible to start
a session, whereas a different activity could open a collaborative data model, etc.

Each activity using SwellRT must implement the *ServiceConnection* interface,
in order to get the *SwellRTService* service's reference:

```
 @Override
  public void onServiceConnected(ComponentName name, IBinder service) {
    SwellRTService mSwellRT = ((SwellRTService.SwellRTBinder) service).getService(this);
    Log.d(this.getClass().getSimpleName(), "SwellRT Service Bound");
   (...)
    }
```

To bound the activity to the service, create and bind the intent to the *SwellRTService*:

```
final Intent mWaveServiceIntent = new Intent(this, SwellRTService.class);
bindService(mWaveServiceIntent, this, Context.BIND_AUTO_CREATE);
```


## SwellRT API Quick Reference

The SwellRT Service provides same operations than the JavaScript API client.
Please check out the (SwellRT wiki)[https://github.com/P2Pvalue/swellrt/wiki] for detailed info.

A summary of operations and service callbacks follows:


**public void startSession(String serverURL, String username, String password)**

Open a session with the SwellRT/Wave server for the passed credentials.
The service only operates with one open session at once. It is shared across activities in your application.

**public void stopSession()**

Stop the already open session, cleaning up all communication resources and closing all opened data models (waves).


**public void openModel(String modelId)**

Open a model with the passed id. The result is provided asynchronously in **SwellRTServiceCallback.onOpen(Model)**.

**public String createModel()**

Create a new model. The result is provided asynchronously in **SwellRTServiceCallback.onCreate(Model)**.

**public Model getModel(String modelId)**

This method allows you to get an already open data model created or opened in a different activity.
The service keeps track of all open data models until they are closed individually or when session is stopped.

**public void closeModel(String modelId)**

Close the data model. The result is provided asynchronously in **SwellRTServiceCallback.oClose()**.


