package raly.medicos.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.actionbarsherlock.widget.SearchView;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import raly.medicos.utilidades.DB;
import raly.medicos.utilidades.Dialogs;
import raly.medicos.utilidades.PacienteItem;
import raly.medicos.utilidades.PacientesAdapter;
import raly.medicos.utilidades.Utils;

public class Pacientes extends SherlockListActivity {
	public static final String EXTRA_SKEY = "skey";
	private String skey;
	private DB medicosdb;
	private InputMethodManager mgr;
	private AQuery aq;
	private Utils utils;
	private Dialogs dialog;
	private MenuItem buscarPacientesMenuItem;
	private boolean isclosed = true, cargando = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		inicarComponentes();
	}

	private void inicarComponentes() {
		aq = new AQuery(this);
		medicosdb = new DB(Pacientes.this);
		mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		utils = new Utils(getApplicationContext());
		dialog = new Dialogs(this);
        checkLogin();
	}

    private void checkLogin() {
        if (!medicosdb.isMedico())
            goLogin();
        else
            iniciar();
    }

	private void iniciar() {
		skey = medicosdb.getSkey();
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.pacientes_layout);
		setTitle(Html.fromHtml("<b>" + getString(R.string.actividad_pacientes) + "</b>"));

		ActionBar aBar = getSupportActionBar();
		aBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
		aBar.setDisplayShowTitleEnabled(true);
		aq.id(android.R.id.empty).text(R.string.loader_msg_pacientes);

		mostrarPacientes();
	}

	private void mostrarPacientes() {
		if (medicosdb.have(DB.TABLA_PACIENTES))
			asignarPacientes(medicosdb.getPacientes(), false);
		else {
			cargarPacientes();
		}
	}

	private void cargarPacientes() {
		if (utils.isOnline()) {
			cargando = true;
			setSupportProgressBarIndeterminateVisibility(cargando);
			String url = "http://laboratorioraly.com/raly/medicom";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("skey", skey);
			params.put("fnt", "listamispacientes");
			aq.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {

				@Override
				public void callback(String url, JSONObject json, AjaxStatus status) {
					if (status.getCode() == 200) {
						try {
							if (json.getBoolean("success")) {
								medicosdb.deletePacientes();
								asignarPacientes(json, true);
							}
						} catch (JSONException e) {
							Log.v(Pacientes.class.toString(), "Error parsing data " + e.toString());
						}
					}
					if (cargando) {
						cargando = false;
						setSupportProgressBarIndeterminateVisibility(cargando);
					}
				}
			});
		} else {
			dialog.alert("", getString(R.string.no_internet_error), null);
			aq.id(android.R.id.empty).text(R.string.no_internet_error);
		}
	}

	private void asignarPacientes(JSONObject json, boolean save) {
		List<PacienteItem> pacientes = new ArrayList<PacienteItem>();
		try {
			JSONArray jpacientes = json.getJSONArray("pacientes");
			for (int i = 0; i < jpacientes.length(); i++) {
				if (save)
					pacientes.add(new PacienteItem(jpacientes.getJSONObject(i), skey, medicosdb));
				else
					pacientes.add(new PacienteItem(jpacientes.getJSONObject(i), skey));
			}
			getListView().setFastScrollEnabled(true);
			setListAdapter(new PacientesAdapter(Pacientes.this, pacientes));
			aq.id(android.R.id.empty).text(R.string.no_paciente);
		} catch (JSONException e) {
			Log.v(Pacientes.class.toString(), "Error parsing data " + e.toString());
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		PacienteItem paciente = (PacienteItem) getListView().getItemAtPosition(position);

		Intent resultados = new Intent(Pacientes.this, Resultados.class);
		Bundle b = new Bundle();
		b.putString(Resultados.EXTRA_PACIENTEID, paciente.getPacienteid());
		b.putString(Resultados.EXTRA_SKEY, skey);
		resultados.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		resultados.putExtras(b);
		startActivity(resultados);
		overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
	}

	@SuppressWarnings("unused")
	private void ocultarKeyBoard() {
		mgr.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	SearchView.OnQueryTextListener buscar = new SearchView.OnQueryTextListener() {

		@Override
		public boolean onQueryTextSubmit(String query) {
			buscarPacientes(query);
			return false;
		}

		@Override
		public boolean onQueryTextChange(String newText) {
			if ((PacientesAdapter) getListAdapter() != null)
				((PacientesAdapter) getListAdapter()).getFilter().filter(newText);
			return false;
		}
	};

	private void buscarPacientes(String q) {
		if (utils.isOnline()) {
			buscarPacientesMenuItem.collapseActionView();
			String url = "http://laboratorioraly.com/raly/medicom";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("skey", skey);
			params.put("buscar", q);
			params.put("fnt", "buscarpaciente");
			setSupportProgressBarIndeterminateVisibility(true);
			aq.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {

				@Override
				public void callback(String url, JSONObject json, AjaxStatus status) {
					if (status.getCode() == 200) {
						try {
							if (json.getBoolean("success")) {
								medicosdb.deletePacientes();
								asignarPacientes(json, true);
							}
						} catch (JSONException e) {
							Log.v(Pacientes.class.toString(), "Error parsing data " + e.toString());
						}
					}
					setSupportProgressBarIndeterminateVisibility(false);
				}
			});
		} else {
			dialog.alert("", getString(R.string.no_internet_error), null);
			aq.id(android.R.id.empty).text(R.string.no_internet_error);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.pacientes, menu);

		SearchView buscarPacientes = new SearchView(Pacientes.this);
		buscarPacientes.setQueryHint(getString(R.string.buscar_pacientes));
		buscarPacientes.setOnQueryTextListener(buscar);
		buscarPacientesMenuItem = menu.findItem(R.id.menu_buscar_pacientes);
		buscarPacientesMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_ALWAYS);
		buscarPacientesMenuItem.setActionView(buscarPacientes);
		buscarPacientesMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				isclosed = false;
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				isclosed = true;
				return true;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_info:
                goInfo();
                break;
            case R.id.menu_configuracion:
                goConfiguraciones();
                break;
            case R.id.menu_cerrarsesion:
                cerrarSesion();
                break;
            case R.id.menu_resultados:
                goMisResultados();
                break;
            case R.id.menu_salir:
                salir();
                break;
            case R.id.menu_recargar_pacientes:
                cargarPacientes();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onBackPressed() {
		if (!isclosed) {
			buscarPacientesMenuItem.collapseActionView();
			((SearchView) buscarPacientesMenuItem.getActionView()).setQuery("", false);
			isclosed = true;
		} else {
			salir();
		}
	}

    private void goConfiguraciones() {
        Intent verConfiguraciones = new Intent(Pacientes.this, Configuracion.class);
        verConfiguraciones.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(verConfiguraciones);
        overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
    }

    private void goLogin() {
        Intent login = new Intent(Pacientes.this, Login.class);
        login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(login);
        overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
        salir();
    }

    private void goInfo() {
        Intent info = new Intent(Pacientes.this, InfoGral.class);
        info.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle b = new Bundle();
        b.putString(Pacientes.EXTRA_SKEY, skey);
        info.putExtras(b);
        startActivity(info);
        overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
    }

    private void goMisResultados() {
        Intent resultados = new Intent(Pacientes.this, MisResultadosGuardados.class);
        resultados.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(resultados);
        overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
    }

    private void cerrarSesion() {
        medicosdb.deleteMedico();
        medicosdb.clearCache();
        goLogin();
    }

    private void salir() {
        medicosdb.clearCache();
        finish();
    }
}