package cl.telematica.android.securapp;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

/**
 * Created by mavin on 28/10/2016.
 */

public class ListaDispositivos extends ListActivity {

    private BluetoothAdapter mBluetoothAdapter2 = null;

    static String DIRECCION_MAC = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayAdapter<String> arrayBluetooth = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        mBluetoothAdapter2 = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> dispositivosEmparejados = mBluetoothAdapter2.getBondedDevices();

        if(dispositivosEmparejados.size() > 0){
            for(BluetoothDevice dispositivos:dispositivosEmparejados){
                String nombreBluetooth = dispositivos.getName();
                String macBluetooth = dispositivos.getAddress();
                arrayBluetooth.add(nombreBluetooth + "\n" + macBluetooth);
            }
        }

        setListAdapter(arrayBluetooth);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String informacionGeneral = ((TextView) v).getText().toString();
        //Toast.makeText(getApplicationContext(), "Información: "+ informacionGeneral,Toast.LENGTH_SHORT).show();

        String direccionMAC = informacionGeneral.substring(informacionGeneral.length() - 17);

        Intent retornaMAC = new Intent();
        retornaMAC.putExtra(DIRECCION_MAC, direccionMAC);
        setResult(RESULT_OK, retornaMAC);
        finish();
    }
}
