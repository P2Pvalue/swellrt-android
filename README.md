# SwellRT Android App example

This is an Android App example using the SwellRT framework. It allows to edit a shared real-time list of strings.

It is compatible with the Web app example found here: https://github.com/P2Pvalue/swellrt-showcase/tree/master/swell-list.

You can set up this project using the SwellRT project or just importing already built dependencies:

## Building the App using the the SwellRT project

In this case, you will need two projects in your workspace:

- **swellrt**: SwellRT framework project including Android components in *swellrt/android*.
- **swellrt-android**: this example Android app.

### Eclipse

1. Download the **swellrt** source code (https://github.com/P2Pvalue/swellrt)

2. Download the **swellrt-android** (this project)

3. Setup up **swellrt-android**

You must link the Androind service source code folder locate at **swellrt/android**
as a source code in the **swellrt-android**: go to Project -> Properties -> Java Build Path -> Source -> Link Source...

Edit your **AndroidManifest.xml** adding the following service definition to the *application* section:

```
<manifest>
    <application>
        <service android:name="org.swellrt.android.service.SwellRTService"></service>
    </application>
</manifest>
```

Add additional *.jar's* from **swellrt/android/libs**: go to Project -> Properties -> Java Build Path -> Libraries -> Add JARs...


## Building the App using pre-built SwellRT's Android components

Follow the steps in https://github.com/P2Pvalue/swellrt/blob/master/android/README.md to get all .jars you must add to this
Android project.


