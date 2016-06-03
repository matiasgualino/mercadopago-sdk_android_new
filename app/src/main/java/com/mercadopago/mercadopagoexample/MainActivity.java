package com.mercadopago.mercadopagoexample;

import android.media.session.MediaSession;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;

import com.mercadopago.core.MercadoPago;

import android.content.Intent;
import android.widget.Toast;
import android.widget.TextView;

import com.mercadopago.model.ApiException;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.Payment;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.PaymentPreference;
import com.mercadopago.model.Token;
import com.mercadopago.util.CurrenciesUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Método ejecutado al hacer clic en el botón
    public void submitCheckout(View view) {

        // Obtener ID de la preferencia (Paso 3)

        // Iniciar el checkout de MercadoPago
        new MercadoPago.StartActivityBuilder()
                .setActivity(this)
                .setPublicKey("TEST-ad365c37-8012-4014-84f5-6c895b3f8e0a")
                .setCheckoutPreferenceId("150216849-ceed1ee4-8ab9-4449-869f-f4a8565d386f")
                .startCheckoutActivity();

    }

    public void submitPaymentVault(View view) {

        // Llamada al Flavor 2 (Igual a la llamada de la version anterior)
        new MercadoPago.StartActivityBuilder()
                .setActivity(this)
                .setPublicKey("TEST-ad365c37-8012-4014-84f5-6c895b3f8e0a")
                .setCurrency(CurrenciesUtil.CURRENCY_ARGENTINA)
                .setAmount(new BigDecimal(100.0))
                .startPaymentVaultActivity();

    }

    public void submitPaymentVaultExcludedOffAndAmex(View view) {

        // Llamada al Flavor 2 (Igual a la llamada de la version anterior)
        List<String> excludedPaymentMethods = new ArrayList<>();
        excludedPaymentMethods.add("amex");

        List<String> excludedPaymentTypes = new ArrayList<>();
        excludedPaymentTypes.add("atm");
        excludedPaymentTypes.add("ticket");
        excludedPaymentTypes.add("bank_transfer");

        PaymentPreference paymentPreference = new PaymentPreference();
        paymentPreference.setExcludedPaymentMethodIds(excludedPaymentMethods); // Poner 3777 77 como BIN, "Medio de pago no soportado"
        paymentPreference.setExcludedPaymentTypeIds(excludedPaymentTypes);

        new MercadoPago.StartActivityBuilder()
                .setActivity(this)
                .setPublicKey("TEST-ad365c37-8012-4014-84f5-6c895b3f8e0a")
                .setCurrency(CurrenciesUtil.CURRENCY_ARGENTINA)
                .setAmount(new BigDecimal(100.0))
                .setPaymentPreference(paymentPreference)
                .startPaymentVaultActivity();

    }

    // Espera los resultados de MercadoPago
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {

        if (requestCode == MercadoPago.CHECKOUT_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {

                // Listo! El pago ya fue procesado por MP.
                Payment payment = (Payment) data.getSerializableExtra("payment");
                TextView results = (TextView) findViewById(R.id.mp_results);

                if (payment != null) {
                    results.setText("PaymentID: " + payment.getId() + " - PaymentStatus: " + payment.getStatus());
                } else {
                    results.setText("El usuario no concretó un pago.");
                }

            } else {

                if ((data != null) &&
                        (data.getSerializableExtra("apiException") != null)) {
                    ApiException apiException
                            = (ApiException) data.getSerializableExtra("apiException");
                    Toast.makeText(getApplicationContext(), apiException.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        } else if (requestCode == MercadoPago.PAYMENT_VAULT_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {

                // Obtener los datos ingresados por el usuario
                PaymentMethod paymentMethod = (PaymentMethod) data.getSerializableExtra("paymentMethod");
                Issuer issuer = (Issuer) data.getSerializableExtra("issuer");
                PayerCost payerCost = (PayerCost) data.getSerializableExtra("payerCost");
                Token token = (Token) data.getSerializableExtra("token");

                TextView results = (TextView) findViewById(R.id.mp_results);

                String message = "PaymentMethod: " + paymentMethod.getId();

                if(issuer != null) {
                    message += " - Issuer: " + issuer.getName();
                }

                if (payerCost != null) {
                    message += " - PayerCost: " + payerCost.getInstallments();
                }

                if(token != null) {
                    message += " - Token: " + token.getId();
                }

                results.setText(message);

            } else {

                if ((data != null) &&
                        (data.getSerializableExtra("apiException") != null)) {
                    ApiException apiException
                            = (ApiException) data.getSerializableExtra("apiException");
                    Toast.makeText(getApplicationContext(), apiException.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
