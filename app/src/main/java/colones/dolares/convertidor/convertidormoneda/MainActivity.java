package colones.dolares.convertidor.convertidormoneda;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String NAMESPACE = "http://ws.sdde.bccr.fi.cr";
    private static final String METHOD_NAME = "ObtenerIndicadoresEconomicosXML";
    private static final String SOAP_ACTION = "http://ws.sdde.bccr.fi.cr/ObtenerIndicadoresEconomicosXML";
    private static final String URL = "http://indicadoreseconomicos.bccr.fi.cr/indicadoreseconomicos/WebServices/wsIndicadoresEconomicos.asmx?WSDL";
    private String RESULTADO;
    private String fecha;
    private String indicador;
    private String montoDig;
    private Date date = new Date();
    private double tipoCambio = 0;
    private double cambio = 0.0;

    private RelativeLayout rl = null;
    RadioButton rbtnCol_Dol;
    RadioButton rbtnDol_Col;
    RadioGroup rgrp_Opcion;
    EditText txtMonto;
    TextView txtResultado, txtTipoCambio;


    private TextWatcher textChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            calcular_y_actualizar();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private RadioGroup.OnCheckedChangeListener checkedChangeListener = new RadioGroup.OnCheckedChangeListener(){

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            if(rbtnCol_Dol.isChecked()){
                txtResultado.setText("Colones:\nDólares:");
            }else if(rbtnDol_Col.isChecked()){
                txtResultado.setText("Dólares:\nColones:");
            }

            calcular_y_actualizar();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rgrp_Opcion = findViewById(R.id.rgrp_Opcion);

        rbtnDol_Col = findViewById(R.id.rdbtnDol_Col);
        rbtnDol_Col.setText("Dólares a Colones");
        rbtnCol_Dol = findViewById(R.id.rdbtnCol_Dol);
        rbtnCol_Dol.setText("Colones a Dólares");

        rgrp_Opcion.setOnCheckedChangeListener(checkedChangeListener);

        txtMonto = findViewById(R.id.txtMonto);
        txtMonto.setEnabled(false);
        txtMonto.addTextChangedListener(textChangedListener);

        txtResultado = findViewById(R.id.txt_view_Resultado);
        txtResultado.setText("Colones:\nDólares:");
        txtTipoCambio = findViewById(R.id.txtTipoCambio);

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        fecha = dateFormat.format(date);

        new SoapCall().execute();
    }

    private class SoapCall extends AsyncTask<String, Object, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            request.addProperty("tcIndicador", "318");
            request.addProperty("tcFechaInicio", fecha);
            request.addProperty("tcFechaFinal", fecha);
            request.addProperty("tcNombre", "App");
            request.addProperty("tnSubNiveles", "N");

            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE transportSE = new HttpTransportSE(URL);

            try{
                transportSE.call(SOAP_ACTION, envelope);
                RESULTADO = envelope.getResponse().toString();
            }catch (Exception e){
                e.printStackTrace();
                Log.e("ERROR...", e.getMessage());
            }
            return RESULTADO;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            getTipoCambio();
        }
    }

    private void getTipoCambio() {
        String valor = ((RESULTADO.split("NUM_VALOR>"))[1].split("<"))[0];
        tipoCambio = Double.parseDouble(valor);
        txtTipoCambio.setText("Venta: " + Double.toString(tipoCambio));
        txtMonto.setEnabled(true);
    }

    private void calcular_y_actualizar(){
        montoDig = txtMonto.getText().toString();
        if(montoDig.split(".").length == 1){
            montoDig = montoDig.split(".")[0] + ".0";
        }
        if(montoDig.equals(".0")){
            montoDig = "0.0";
        }

        if(rbtnCol_Dol.isChecked()){
            cambio = Double.parseDouble(montoDig) / tipoCambio;
            txtResultado.setText("Colones: " + montoDig + "\nDólares:" + cambio);
        }else if(rbtnDol_Col.isChecked()){
            cambio = Double.parseDouble(montoDig) * tipoCambio;
            txtResultado.setText("Dólares: " + montoDig + "\nColones:" + cambio);
        }
    }
}
