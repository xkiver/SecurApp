package cl.telematica.android.securapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int SOLICITA_ACTIVACION = 1;
    private static final int SOLICITA_CONEXION = 2;
    private static String MAC = null;

    UUID mUUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    boolean conexion = false;

    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothDevice mBluetoothDevice = null;
    BluetoothSocket mBluetoothSocket = null;

    Button btnConexion, btnPuerta, btnPorton, btnAscensor1, btnAscensor2, btnAscensor3;
    TextView textViewEstado, tvAscensor;

    final byte delimiter = 33; //signo ! en tabla ascii
    int readBufferPosition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final android.os.Handler handler = new android.os.Handler();

        btnConexion = (Button)findViewById(R.id.btnConection);
        btnPuerta = (Button)findViewById(R.id.btnPuerta);
        btnPuerta.setVisibility(View.INVISIBLE);
        btnPorton = (Button)findViewById(R.id.btnPorton);
        btnPorton.setVisibility(View.INVISIBLE);
        btnAscensor1 = (Button)findViewById(R.id.btnAscensor1);
        btnAscensor1.setVisibility(View.INVISIBLE);
        btnAscensor2 = (Button)findViewById(R.id.btnAscensor2);
        btnAscensor2.setVisibility(View.INVISIBLE);
        btnAscensor3 = (Button)findViewById(R.id.btnAscensor3);
        btnAscensor3.setVisibility(View.INVISIBLE);
        textViewEstado = (TextView)findViewById(R.id.textViewEstado);
        tvAscensor= (TextView)findViewById(R.id.twAscensor);
        tvAscensor.setVisibility(View.INVISIBLE);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null){
            Toast.makeText(getApplicationContext(), "Su dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
        }else if(!mBluetoothAdapter.isEnabled()){
            Intent activaBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(activaBluetooth, SOLICITA_ACTIVACION);

        }

        btnConexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(conexion){
                    //desconectar
                    try {
                        mBluetoothSocket.close();
                        conexion = false;
                        btnConexion.setText("Conectar");
                        Toast.makeText(getApplicationContext(),"Bluetooth fue desconectado",Toast.LENGTH_SHORT).show();

                    }catch (IOException error){
                        Toast.makeText(getApplicationContext(),"Error: "+ error,Toast.LENGTH_SHORT).show();
                    }
                }else{
                    //conectar
                    btnConexion.setVisibility(View.INVISIBLE);

                    Intent abreLista = new Intent(MainActivity.this, ListaDispositivos.class);
                    startActivityForResult(abreLista, SOLICITA_CONEXION);

                }
            }
        });

        final class workerThread implements Runnable{

            private String btMsg;

            public workerThread(String msg){ btMsg = msg;}

            @Override
            public void run() {

                try {
                    sendBtMsg(btMsg);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                while (!Thread.currentThread().isInterrupted()){
                    int bytesAvaible;
                    boolean workDone = false;

                    try{
                        final InputStream mmInputStream;
                        mmInputStream = mBluetoothSocket.getInputStream();
                        bytesAvaible = mmInputStream.available();

                        if (bytesAvaible > 0){
                            byte[] packetBytes = new byte[bytesAvaible];
                            Log.e("MMFundamenta recv bt","bytes avaible");
                            byte[] readBuffer = new byte[1024];
                            mmInputStream.read(packetBytes);

                            for(int i=0; i < bytesAvaible ; i++){

                                byte b = packetBytes[i];

                                if(b == delimiter){

                                    byte[] encondedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encondedBytes, 0, encondedBytes.length);
                                    final String data = new String(encondedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            textViewEstado.setText(data);
                                        }
                                    });

                                    workDone = true;
                                    break;

                                }else{
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                            if (workDone == true){
                                mBluetoothSocket.close();
                                conexion = false;
                                break;
                            }
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        btnPuerta.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                (new Thread(new workerThread("puerta"))).start();
            }
        });

        btnPorton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                (new Thread(new workerThread("porton"))).start();
            }
        });

        btnAscensor1.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                (new Thread(new workerThread("piso1"))).start();
            }
        });

        btnAscensor2.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                (new Thread(new workerThread("piso2"))).start();
            }
        });

        btnAscensor3.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                (new Thread(new workerThread("piso3"))).start();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){

            case SOLICITA_ACTIVACION:
                if(resultCode == Activity.RESULT_OK){
                    Toast.makeText(getApplicationContext(), "El bluetooth fue activado", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(), "El bluetooth no fue activado, la aplicación se cerrará", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;

            case SOLICITA_CONEXION:
                if(resultCode == Activity.RESULT_OK){
                    MAC = data.getExtras().getString(ListaDispositivos.DIRECCION_MAC);
                    mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(MAC);

                    try {

                        mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(mUUID);
                        mBluetoothSocket.connect();
                        conexion = true;
                        Toast.makeText(getApplicationContext(),"Conectado",Toast.LENGTH_SHORT).show();
                        btnConexion.setText("Desconectar");
                        btnAscensor1.setVisibility(View.VISIBLE);
                        btnAscensor2.setVisibility(View.VISIBLE);
                        btnAscensor3.setVisibility(View.VISIBLE);
                        btnPuerta.setVisibility(View.VISIBLE);
                        btnPorton.setVisibility(View.VISIBLE);
                        tvAscensor.setVisibility(View.VISIBLE);

                    }catch (IOException error){
                        conexion = false;
                        Toast.makeText(getApplicationContext(),"Ha ocurrido un eroor: "+ error,Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(getApplicationContext(),"Falta obtener dirección MAC",Toast.LENGTH_SHORT).show();
                }
        }
    }

    public void sendBtMsg (String msgSend) throws IOException {
        if (conexion == true){
            OutputStream mmOutputStream = mBluetoothSocket.getOutputStream();
            mmOutputStream.write(msgSend.getBytes());
        }else{
            try{
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(mUUID);

                if(!mBluetoothSocket.isConnected()){
                    mBluetoothSocket.connect();
                }
                OutputStream mmOutputStream = mBluetoothSocket.getOutputStream();
                mmOutputStream.write(msgSend.getBytes());

            }catch (IOException e){
                e.printStackTrace();

            }
        }
    }
}
