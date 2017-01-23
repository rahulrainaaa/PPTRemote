package app.project.pptremote;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity implements SensorEventListener, CompoundButton.OnCheckedChangeListener {

    //Global UI Objects declaration.
    private ToggleButton toggleButton = null;
    private EditText editText = null;
    private TextView textView = null;
    private WebView webView = null;

    //Global Sensor Objects declaration.
    private SensorManager sensorManager = null;
    private Sensor sensor = null;

    //Global Flags & dataObjects declaration.
    private int lastEventVal = 0;
    private String hostURL = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        editText = (EditText) findViewById(R.id.editText);
        webView = (WebView) findViewById(R.id.webView);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onResume() {
        if (toggleButton.isChecked()) {
            startSensor();
        } else {
            stopSensor();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        stopSensor();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopSensor();
        sensorManager = null;
        sensor = null;
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        hostURL = editText.getText().toString().trim();
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            sendSignal("next");
        } else if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            sendSignal("prev");
        }
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        int x = (int) event.values[0];
        int y = (int) event.values[1];
        int z = (int) event.values[2];

        textView.setText("X: " + x + "\nY: " + y + "\nZ: " + z);
        doProcessing(x, y, z);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            startSensor();
        } else {
            stopSensor();
        }
    }

    /**
     * @method startSensor
     * @desc Method to start Sensor and register Sensor event listener.
     */
    private void startSensor() {

        hostURL = editText.getText().toString().trim();
        if (sensorManager != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            editText.setEnabled(false);
        }
    }

    /**
     * @method stopSensor
     * @desc Method to stop Sensor Events.
     */
    private void stopSensor() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            editText.setEnabled(true);
            textView.setText("Sensor: OFF");
        }
    }

    /**
     * @param x X-Axis
     * @param y Y-Axis
     * @param z Z-Axis
     * @method doProcessing
     * @desc Method to process coordinates and derive the new directions of ppt.
     */
    private void doProcessing(int x, int y, int z) {

        if (x > 8) { //prev

            if (lastEventVal < 1) {
                sendSignal("prev");
                lastEventVal = x;
            }
        } else if (x < -8) { //next

            if (lastEventVal > -1) {
                sendSignal("next");
                lastEventVal = x;
            }
        } else if ((x > -2) && (x < 2)) {
            lastEventVal = 0;
        }
    }

    /**
     * @param signal direction string
     * @method sendSignal
     * @desc Method to send direction signal to target device.
     */
    private void sendSignal(String signal) {
        toggleButton.setText("" + signal);
        webView.loadUrl("" + hostURL + "/PPTRobot/fetchSignal?signal=" + signal);
        Log.d("direction", "" + hostURL + "/PPTRobot/fetchSignal?signal=" + signal);
    }
}
