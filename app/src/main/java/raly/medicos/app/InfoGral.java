package raly.medicos.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import raly.medicos.utilidades.DB;
import raly.medicos.utilidades.Dialogs;
import raly.medicos.utilidades.PromocionAdapter;
import raly.medicos.utilidades.PromocionItem;
import raly.medicos.utilidades.Utils;

public class InfoGral extends SherlockActivity {

	private DB medicosdb;
	private Utils utils;
	private AQuery aq;
	private String skey;
	private Dialogs dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		iniciarComponentes();
	}

	private void iniciarComponentes() {
		aq = new AQuery(this);
		medicosdb = new DB(InfoGral.this);
		utils = new Utils(getApplicationContext());
		dialog = new Dialogs(this);
        iniciar();
	}

	private void iniciar() {
		setTitle(Html.fromHtml("<b>" + getResources().getString(R.string.actividad_info) + "</b>"));
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main_layout);
        ActionBar aBar = getSupportActionBar();
        aBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
        aBar.setDisplayShowHomeEnabled(true);
        aBar.setDisplayHomeAsUpEnabled(true);
        aBar.setDisplayShowTitleEnabled(true);
        aBar.setHomeButtonEnabled(true);
		skey = medicosdb.getSkey();
		aq.id(R.id.promociones_list_empty).text(R.string.loader_msg_promociones);
		mostrarPromociones();
	}

	private void mostrarPromociones() {
		aq.id(R.id.promociones_list).getListView().setEmptyView(findViewById(R.id.promociones_list_empty));
		aq.id(R.id.promociones_list).getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
				goPromocion((PromocionItem) aq.id(R.id.promociones_list).getListView().getItemAtPosition(position));
			}
		});

		if (medicosdb.have(DB.TABLA_PROMOCIONES))
			asignarPromociones(medicosdb.getPromociones(), false);
		else
			cargarPromociones();
	}

	private void cargarPromociones() {
		if (utils.isOnline()) {
			String url = "http://laboratorioraly.com/raly/pacientem";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("skey", skey);
			params.put("fnt", "promociones");

			setSupportProgressBarIndeterminateVisibility(true);
			aq.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {

				@Override
				public void callback(String url, JSONObject json, AjaxStatus status) {
					if (status.getCode() == 200) {
						try {
							if (json.getBoolean("success")) {
								medicosdb.deletePromociones();
								asignarPromociones(json, true);
							}
						} catch (JSONException e) {
							Log.v(InfoGral.class.toString(), "Error parsing data " + e.toString());
						}
					}
					setSupportProgressBarIndeterminateVisibility(false);
				}
			});
		} else {
			dialog.alert("", getString(R.string.no_internet_error), null);
			aq.id(R.id.promociones_list_empty).text(R.string.no_internet_error);
		}
	}

	private void asignarPromociones(JSONObject json, boolean save) {
		ArrayList<PromocionItem> promociones = new ArrayList<PromocionItem>();
		try {
			JSONArray promos = json.getJSONArray("promociones");
			for (int i = 0; i < promos.length(); i++) {
				if (save)
					promociones.add(new PromocionItem(promos.getJSONObject(i), medicosdb));
				else
					promociones.add(new PromocionItem(promos.getJSONObject(i)));
			}
			aq.id(R.id.promociones_list).getListView().setAdapter(new PromocionAdapter(InfoGral.this, promociones));
			aq.id(R.id.promociones_list_empty).text(R.string.no_promociones);
		} catch (JSONException e) {
			Log.v(InfoGral.class.toString(), "Error parsing data " + e.toString());
		}
	}

	private void goPromocion(PromocionItem promo) {
		Intent verPromocion = new Intent(InfoGral.this, Promocion.class);
		Bundle b = new Bundle();
		b.putString(Promocion.NOMBRE, promo.getTitulo());
		b.putString(Promocion.PRECIO, promo.getPrecio());
		b.putString(Promocion.DESCRIPCION, promo.getDescripcion());
		b.putString(Promocion.ICON, promo.getImagen());
		verPromocion.putExtras(b);
		verPromocion.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(verPromocion);
		overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finalizar();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finalizar();
    }

    private void finalizar() {
        finish();
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
    }
}