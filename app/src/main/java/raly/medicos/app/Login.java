package raly.medicos.app;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import raly.medicos.utilidades.DB;
import raly.medicos.utilidades.Utils;

public class Login extends SherlockActivity {

    private AQuery aq;
    private ProgressDialog loader;
    private DB medicosdb;
    private InputMethodManager mgr;
    private Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.login_layout);

		/* inicializando aQuery */
        aq = new AQuery(this);
        aq.id(R.id.entrar_btn).enabled(false);
        aq.id(R.id.entrar_btn).clicked(eventosBotones);
        aq.id(R.id.salir_btn).clicked(eventosBotones);
        aq.id(R.id.fbk).clicked(eventosBotones);
        aq.id(R.id.twt).clicked(eventosBotones);
        aq.id(R.id.usuario_txt).getEditText().addTextChangedListener(CambioTexto);
        aq.id(R.id.clave_txt).getEditText().addTextChangedListener(CambioTexto);
        aq.id(R.id.clave_txt).getEditText().setOnEditorActionListener(actionListener);
        aq.id(R.id.usuario_txt).getEditText().setSelected(false);
        aq.id(R.id.clave_txt).getEditText().setSelected(false);

		/* inicializando la base de datos */
        medicosdb = new DB(Login.this);
        utils = new Utils(this);

        loader = new ProgressDialog(this);
        loader.setMessage(getString(R.string.login_loader));
        loader.setCancelable(false);
        loader.setIndeterminate(true);

        mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    TextWatcher CambioTexto = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!aq.id(R.id.entrar_btn).getButton().isEnabled() && s.length() > 0)
                aq.id(R.id.entrar_btn).getButton().setEnabled(true);
            if (s.length() == 0)
                aq.id(R.id.entrar_btn).getButton().setEnabled(false);
        }
    };

    OnEditorActionListener actionListener = new OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                ocultarKeyBoard();
                checkearLogin();
                return true;
            }
            return false;
        }
    };

    View.OnClickListener eventosBotones = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.entrar_btn:
                    ocultarKeyBoard();
                    checkearLogin();
                    break;
                case R.id.fbk:
                    launchFacebook();
                    break;
                case R.id.twt:
                    launchTwitter();
                    break;
                case R.id.salir_btn:
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    private void launchFacebook(){
        try{
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/616228871803418")));
        }catch (ActivityNotFoundException e){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com/profile.php?id=616228871803418")));
        }
    }

    private void launchTwitter(){
        try{
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://Ralylab")));
        }catch (ActivityNotFoundException e){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.twitter.com/Ralylab")));
        }
    }

    private void checkearLogin() {
        String usuario = aq.id(R.id.usuario_txt).getText().toString(), clave = aq.id(R.id.clave_txt).getText().toString();
        if (usuario.isEmpty())
            aq.id(R.id.login_error_label).text(R.string.usuario_error);
        else if (clave.isEmpty())
            aq.id(R.id.login_error_label).text(R.string.clave_error);
        else {
            if (utils.isOnline())
                hacerLogin(usuario, clave);
            else
                aq.id(R.id.login_error_label).text(R.string.no_internet_error);

        }
    }

    private void hacerLogin(String usuario, String clave) {
        loader.show();
        String url = "http://laboratorioraly.com/raly/hacerloginmed";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("usuario", usuario);
        params.put("clave", clave);
        aq.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {

            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                if (status.getCode() == 200) {
                    try {
                        boolean success = json.getBoolean("success");
                        if (success) {
                            json.remove("success");
                            guardarMedico(json);
                        } else {
                            aq.id(R.id.login_error_label).text(json.getString("text"));
                        }
                    } catch (JSONException e) {
                        Log.v(Login.class.getSimpleName().toString(), "Error parsing data " + e.toString());
                    }
                }
                loader.dismiss();
            }
        });
    }

    private void guardarMedico(JSONObject medico) {
        if (medicosdb.saveMedico(medico)) {
            goMain();
        } else
            aq.id(R.id.login_error_label).text(getString(R.string.save_medico_error));
    }

    private void ocultarKeyBoard() {
        mgr.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void goMain() {
        Intent main = new Intent(Login.this, Pacientes.class);
        main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(main);
        overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
        finish();
    }
}
