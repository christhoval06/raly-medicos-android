package raly.medicos.app;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import raly.medicos.utilidades.DB;
import raly.medicos.utilidades.Dialogs;
import raly.medicos.utilidades.PacienteItem;
import raly.medicos.utilidades.ResultadoItem;
import raly.medicos.utilidades.ResultadosAdapter;
import raly.medicos.utilidades.ResultadosItem;
import raly.medicos.utilidades.SwipeListViewTouchListener;
import raly.medicos.utilidades.Utils;

public class Resultados extends SherlockListActivity {
	public static final String EXTRA_SKEY = "skey", EXTRA_PACIENTEID = "pacienteid";
	private String pacienteid, skey;
	private DB medicosdb;
	private AQuery aq;
	private Utils utils;
	private Dialogs dialog;
	private SharedPreferences mPrefs;
	private boolean cargando = false;
	private PacienteItem paciente;
	private SwipeListViewTouchListener touchListener;
	private ProgressBar resultadoItemProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		inicarComponentes();
	}

	private void inicarComponentes() {
		aq = new AQuery(this);
		medicosdb = new DB(Resultados.this);
		utils = new Utils(getApplicationContext());
		dialog = new Dialogs(this);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(Resultados.this);
		iniciar();
	}

	private void iniciar() {
		Bundle b = getIntent().getExtras();
		if (b != null) {
			skey = b.containsKey(EXTRA_SKEY) ? b.getString(EXTRA_SKEY) : null;
			pacienteid = b.containsKey(EXTRA_PACIENTEID) ? b.getString(EXTRA_PACIENTEID) : null;
		}
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.pacientes_layout);
		paciente = new PacienteItem(medicosdb.getPaciente(pacienteid), skey);
		setTitle(Html.fromHtml("<b>" + paciente.getNombre() + "</b>"));

		ActionBar aBar = getSupportActionBar();
		aBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
		aBar.setDisplayShowHomeEnabled(true);
		aBar.setDisplayHomeAsUpEnabled(true);
		aBar.setDisplayShowTitleEnabled(true);
		aBar.setHomeButtonEnabled(true);
		aq.id(android.R.id.empty).text(R.string.loader_msg_resultados);

		mostrarResultados();
 		touchListener = new SwipeListViewTouchListener(getListView(), new SwipeListViewTouchListener.OnSwipeCallback() {
			@Override
			public void onSwipeLeft(ListView listView, int[] reverseSortedPositions) {
				YourSlideRightToLeft(reverseSortedPositions[0]);
			}

			@Override
			public void onSwipeRight(ListView listView, int[] reverseSortedPositions) {
				YourSlideLeftToRight(reverseSortedPositions[0]);
			}
		}, false, false, false, true);
		getListView().setOnTouchListener(touchListener);
		getListView().setOnScrollListener(touchListener.makeScrollListener());
	}

	private void mostrarResultados() {
		if (medicosdb.PacienteHaveResultados(pacienteid))
			asignarResultados(medicosdb.getResultados(pacienteid), false);
		else {
			cargando = true;
			setSupportProgressBarIndeterminateVisibility(cargando);
			cargarResultados();
		}
	}

	private void cargarResultados() {
		if (utils.isOnline()) {
			String url = "http://laboratorioraly.com/raly/medicom";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("skey", skey);
			params.put("fnt", "resultados");
			params.put("pacienteid", pacienteid);

			aq.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {

				@Override
				public void callback(String url, JSONObject json, AjaxStatus status) {
					if (status.getCode() == 200) {
						try {
							if (json.getBoolean("success")) {
								medicosdb.deleteResultados(pacienteid);
								asignarResultados(json, true);
							}
						} catch (JSONException e) {
							Log.v(Resultados.class.toString(), "Error parsing data " + e.toString());
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

	private void asignarResultados(JSONObject json, boolean save) {
		List<ResultadoItem> resultados = new ArrayList<ResultadoItem>();
		try {
			JSONArray jresultados = json.getJSONArray("resultados");
			for (int i = 0; i < jresultados.length(); i++) {
				JSONObject e = jresultados.getJSONObject(i);
				boolean descargado = (medicosdb.getPDF(pacienteid, e.getString("resultadoid"))).isEmpty() ? false : true;
				if (save) {
					int aux = e.getBoolean("estado") ? 1 : 0;
					e.remove("estado");
					e.put("estado", aux);
					resultados.add(new ResultadoItem(e, descargado, skey, pacienteid, medicosdb));
				} else
					resultados.add(new ResultadoItem(e, descargado, skey));
			}
			getListView().setFastScrollEnabled(true);
			setListAdapter(new ResultadosAdapter(Resultados.this, resultados));
			registerForContextMenu(getListView());
			aq.id(android.R.id.empty).text(R.string.no_resultados);
		} catch (JSONException e) {
			Log.v(Resultados.class.toString(), "Error parsing data " + e.toString());
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		ResultadoItem resultado = ((ResultadosItem) getListView().getItemAtPosition(position)).getResultado();
		if (resultado != null) {
			if (v.findViewById(R.id.download_file) != null)
				resultadoItemProgress = (ProgressBar) v.findViewById(R.id.download_file);

			if (resultado.getEstado())
				v.showContextMenu();
			else
				dialog.alert("", getString(R.string.resultado_no_listo), null);

			super.onListItemClick(l, v, position, id);
		}
	}

	private void descargarPDF(final ResultadoItem resultado, final String antecedente, final boolean soloDescargar) {
		if (utils.isOnline()) {
			showProgressBar(antecedente);

			String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar + "Android/data/" + getPackageName()
					+ "/files/", filePath = mPrefs.getBoolean("guardar", true) ? pacienteid + "_" + resultado.getResultadoid() + ".pdf" : mPrefs
					.getString("file", getString(R.string.file_default));

			try {
				String url = resultado.getPdf() + (antecedente.isEmpty() ? "" : antecedente);
				filePath = antecedente.isEmpty() ? filePath : getString(R.string.file_antecedente);
				File file = new File(dirPath + filePath);

				if (file.exists() == false) {
					file.getParentFile().mkdirs();
					file.createNewFile();
				}

				aq.download(url, file, new AjaxCallback<File>() {
					public void callback(String url, File file, AjaxStatus status) {
						if (file != null) {
							try {
								if ((soloDescargar || mPrefs.getBoolean("guardar", true)) && antecedente.isEmpty()) {
									if (!resultado.getTitulo().toUpperCase().startsWith("---VER")) {
										JSONObject archivo = new JSONObject();
										archivo.put("pacienteid", pacienteid);
										archivo.put("resultadoid", resultado.getResultadoid());
										archivo.put("paciente", paciente.getNombre());
										archivo.put("resultado", resultado.getTitulo());
										archivo.put("fecha", resultado.getFecha());
										archivo.put("file", file.getAbsolutePath());
										medicosdb.saveResultadoFile(archivo);
										resultado.setDownloaded(true);
										((ResultadosAdapter) getListAdapter()).notifyDataSetChanged();
									}
								}

								hideProgressBar(antecedente);

								if (!soloDescargar)
									openFile(file.getAbsolutePath(), "application/pdf");
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						hideProgressBar(antecedente);
					}
				});

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			dialog.alert("", getString(R.string.no_internet_error), null);
			aq.id(android.R.id.empty).text(R.string.no_internet_error);
		}
	}

	private void showProgressBar(String antecedente) {
		if (!antecedente.isEmpty())
			setSupportProgressBarIndeterminateVisibility(true);
		else {
			if (resultadoItemProgress != null)
				resultadoItemProgress.setVisibility(View.VISIBLE);
		}
	}

	private void hideProgressBar(String antecedente) {
		if (!antecedente.isEmpty())
			setSupportProgressBarIndeterminateVisibility(false);
		else {
			if (resultadoItemProgress != null)
				resultadoItemProgress.setVisibility(View.GONE);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == getListView().getId()) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle(getResultadoItem(info.position).getTitulo());
			String[] menuItems = getResources().getStringArray(R.array.resultado_menu_contextual);
			for (int i = 0; i < menuItems.length; i++)
				if (i == 2) {
					if (mPrefs.getBoolean("guardar", true))
						menu.add(Menu.NONE, i, i, menuItems[i]);
				} else
					menu.add(Menu.NONE, i, i, menuItems[i]);
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	private ResultadoItem getResultadoItem(int pos) {
		return ((ResultadosItem) getListAdapter().getItem(pos)).getResultado();
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		ResultadoItem r = getResultadoItem(info.position);
		switch (menuItemIndex) {
		case 0:
			buscarResultado(r, 'r');
			break;
		case 1:
			buscarResultado(r, 'a');
			break;
		case 2:
			buscarResultado(r, 'd');
			break;
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	private void buscarResultado(ResultadoItem r, char doc) {
		String file = medicosdb.getPDF(pacienteid, r.getResultadoid());
		switch (doc) {
		case 'r':
			if (!file.isEmpty())
				openFile(file, "application/pdf");
			else
				descargarPDF(r, "", false);
			break;
		case 'a':
			descargarPDF(r, "&bloque=2", false);
			break;
		case 'd':
			descargarPDF(r, "", true);
			break;
		}
	}

	// (<-----)
	public void YourSlideRightToLeft(int position) {
        return;
	}

	// (----->)
	public void YourSlideLeftToRight(int position) {
		descargarPDF(getResultadoItem(position), "&bloque=2", false);
	}

	private void openFile(String file, String mineType) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(file)), mineType);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
		} catch (ActivityNotFoundException e) {
			dialog.notification("Advertencia", "No tiene un App para ver PDFs\nQuiere descargar Adobe Reader?", "ir a PlayStore",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.adobe.reader")));
							overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
						}
					});
		}
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