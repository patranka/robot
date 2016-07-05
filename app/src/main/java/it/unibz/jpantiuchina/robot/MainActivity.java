package it.unibz.jpantiuchina.robot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SeekBar;

import java.io.IOException;
import java.util.UUID;

import it.unibz.jpantiuchina.robot.hardware.Robot;


public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener
{
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT_TAG = 546; // means nothing, just an unique integer
//    private static final String MY_DEVICE_NAME = "new-lenovo";
    private static final String MY_DEVICE_NAME = "HC-06";

    //http://stackoverflow.com/questions/23963815/sending-data-from-android-to-arduino-with-hc-06-bluetooth-module
    // Well known SPP UUID
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private SeekBar sbSpeed;

    private Robot robot;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sbSpeed = (SeekBar) findViewById(R.id.sbSpeed);
        sbSpeed.setOnSeekBarChangeListener(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null)
            throw new RuntimeException("Device does not support bluetooth");

        if (!bluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT_TAG);
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothDeviceFoundReceiver, filter);

        if (bluetoothAdapter.isEnabled())
            startDiscovery();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_ENABLE_BT_TAG)
        {
            if (resultCode == RESULT_OK)
                startDiscovery();
            else
                throw new RuntimeException("Cannot work with disabled Bluetooth.");
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy()
    {
        if (socket != null)
        {
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        unregisterReceiver(bluetoothDeviceFoundReceiver);
        super.onDestroy();
    }


    private void startDiscovery()
    {
        Log.i(TAG, "Searching for device " + MY_DEVICE_NAME);
        bluetoothAdapter.startDiscovery();
    }




    private void onBluetoothDeviceFound(BluetoothDevice device) throws Exception
    {
        Log.i(TAG, "Found Bluetooth device " + device.getName() + ' ' + device.getAddress());

        if (!MY_DEVICE_NAME.equals(device.getName()))
            return;


        Log.i(TAG, "Connecting to this device");

        //UUID uuid = device.getUuids()[0].getUuid();
        //solution from stackoverflow
        UUID uuid = MY_UUID;

        Log.i(TAG, "Connecting to UUID " + uuid);

        socket = device.createRfcommSocketToServiceRecord(uuid);

        bluetoothAdapter.cancelDiscovery(); // Save resources and prevent double connects

        try
        {
            socket.connect();
        }
        catch (IOException e)
        {
            socket.close();
            Log.e(TAG, "Cannot connect", e);
            throw new RuntimeException(e);
        }

        Log.i(TAG, "Successfully connected");

        robot = new Robot(socket.getInputStream(), socket.getOutputStream());

//        while (true)
//        {
//            robot.setMotorSpeedsAndUpdateSensors(-120, -120); //-120 go slowly
//            Log.i(TAG, robot.toString());
//            if (robot.isDangerOnLeft())
//            {
//                robot.setMotorSpeedsAndUpdateSensors(128 + 10,128 - 10);
//            }
//            else if (robot.isDangerOnRight())
//            {
//                robot.setMotorSpeedsAndUpdateSensors(128 - 10,128 + 10);
//            }
//            Thread.sleep(200);
//        }


    }


    private static final int FORWARD_SPEED = 10;
    private static final int TURN_SPEED = 50;

    private enum Direction { FORWARD, BACKWARD, LEFT, RIGHT, STOP }

    private Direction currentDirection = Direction.STOP;


    private int getForwardSpeed()
    {
        return 127 - sbSpeed.getProgress(); //101
//        return FORWARD_SPEED;
    }


    public void turnLeft()
    {
        Log.i(TAG, "left");
        if (currentDirection == Direction.RIGHT)
        {
            stop();
        }
        else
        {
            //robot.setMotorSpeedsAndUpdateSensorsWithoutExceptions(-TURN_SPEED, TURN_SPEED);
            robot.setMotorSpeedsAndUpdateSensorsWithoutExceptions(-getForwardSpeed(), getForwardSpeed());
            currentDirection = Direction.LEFT;
        }
    }


    public void turnRight()
    {
        Log.i(TAG, "right");
        if (currentDirection == Direction.LEFT)
        {
            stop();
        }
        else
        {
            //robot.setMotorSpeedsAndUpdateSensorsWithoutExceptions(TURN_SPEED, -TURN_SPEED);
            robot.setMotorSpeedsAndUpdateSensorsWithoutExceptions(getForwardSpeed(), -getForwardSpeed());
            currentDirection = Direction.RIGHT;
        }
    }

    public void goForward()
    {
        Log.i(TAG, "forward");
        if (currentDirection == Direction.BACKWARD)
        {
            stop();
        }
        else
        {
            robot.setMotorSpeedsAndUpdateSensorsWithoutExceptions(getForwardSpeed(), getForwardSpeed());
            currentDirection = Direction.FORWARD;
        }
    }

    public void goBackward()
    {
        Log.i(TAG, "backward");
        if (currentDirection == Direction.FORWARD)
        {
            stop();
        }
        else
        {
            robot.setMotorSpeedsAndUpdateSensorsWithoutExceptions(-getForwardSpeed(), -getForwardSpeed());
            currentDirection = Direction.BACKWARD;
        }
    }


    private void stop()
    {
        Log.i(TAG, "stop");
        robot.setMotorSpeedsAndUpdateSensorsWithoutExceptions(0x80, 0x80); //0x80
        currentDirection = Direction.STOP;
    }


    // From SeekBar
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {

    }

    // From SeekBar, ignore
    @Override public void onStartTrackingTouch(SeekBar seekBar) { }
    @Override public void onStopTrackingTouch(SeekBar seekBar) { }





    private final BroadcastReceiver bluetoothDeviceFoundReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            try
            {
                onBluetoothDeviceFound(bluetoothDevice);
            }
            catch (Exception e)
            {
                Log.e(TAG, "Error in device discovery", e);
//                throw new RuntimeException(e);
            }
        }
    };
}





