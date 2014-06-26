package raly.medicos.app;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import raly.medicos.utilidades.DB;
import raly.medicos.utilidades.Dialogs;
import raly.medicos.utilidades.MiResultadoItem;
import raly.medicos.utilidades.MisResultadosAdapter;
import raly.medicos.utilidades.Utils;

public class MisResultadosGuardados extends SherlockListActivity {
	private DB medicosdb;
	private AQuery aq;
	private Dialogs dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		inicarComponentes();
	}

	private void inicarComponentes() {
		aq = new AQuery(this);
		medicosdb = new DB(MisResultadosGuardados.this);
		dialog = new Dialogs(this);
		iniciar();
	}

	private void iniciar() {
		setContentView(R.layout.mis_resultados_layout);
		setTitle(Html.fromHtml("<b>" + getString(R.string.menu_resultados) + "</b>"));

		ActionBar aBar = getSupportActionBar();
		aBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
		aBar.setDisplayShowHomeEnabled(true);
		aBar.setDisplayHomeAsUpEnabled(true);
		aBar.setDisplayShowTitleEnabled(true);
		aBar.setHomeButtonEnabled(true);
		aq.id(android.R.id.empty).text(R.string.loader_msg_resultados);
		mostrarResultados();
	}

	private void mostrarResultados() {
		if (medicosdb.have(DB.TABLA_FILES))
			asignarResultados(medicosdb.getResultadosFiles(), false);
		else
			aq.id(android.R.id.empty).text(R.string.no_resultados_save);
	}

	private void asignarResultados(JSONObject json, boolean save) {
		List<MiResultadoItem> resultados = new ArrayList<MiResultadoItem>();
		try {
			JSONArray jresultados = json.getJSONArray("resultados");
			for (int i = 0; i < jresultados.length(); i++) {
				resultados.add(new MiResultadoItem(jresultados.getJSONObject(i), medicosdb));
			}
			getListView().setFastScrollEnabled(true);
			setListAdapter(new MisResultadosAdapter(MisResultadosGuardados.this, resultados, dialog));
			aq.id(android.R.id.empty).text(R.string.no_resultados_save);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		MiResultadoItem resultado = (MiResultadoItem) getListView().getItemAtPosition(position);
		new Utils(this).openPDF(this, resultado.getFile(), "application/pdf");
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