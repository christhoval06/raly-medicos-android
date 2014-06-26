package raly.medicos.app;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.text.Html;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class Configuracion extends SherlockPreferenceActivity {

	private static int prefs = R.xml.configs;
	SharedPreferences _prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.setTitle(Html.fromHtml("<b>Configuraciones</b>"));
		super.onCreate(savedInstanceState);
		try {
			getClass().getMethod("getFragmentManager");
			AddResourceApi11AndGreater();
		} catch (NoSuchMethodException e) { // Api < 11
			AddResourceApiLessThan11();
		}

		ActionBar aBar = getSupportActionBar();
		aBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
		aBar.setDisplayShowHomeEnabled(true);
		aBar.setDisplayHomeAsUpEnabled(true);
		aBar.setDisplayShowTitleEnabled(true);
		aBar.setHomeButtonEnabled(true);
	}

	@SuppressWarnings("deprecation")
	protected void AddResourceApiLessThan11() {
		addPreferencesFromResource(prefs);
	}

	@TargetApi(11)
	protected void AddResourceApi11AndGreater() {
		getFragmentManager().beginTransaction().replace(android.R.id.content, new PF()).commit();
	}

	@TargetApi(11)
	public static class PF extends PreferenceFragment {
		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(Configuracion.prefs);
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