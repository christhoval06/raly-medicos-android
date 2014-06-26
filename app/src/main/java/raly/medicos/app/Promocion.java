package raly.medicos.app;

import android.net.Uri;
import android.os.Bundle;
import android.text.Html;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;
import com.squareup.picasso.Picasso;

public class Promocion extends SherlockActivity {

	public static String NOMBRE = "P_NOMBRE", PRECIO = "P_PRECIO", DESCRIPCION = "P_DESCRIPCION", ICON = "P_ICON";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.promocion_layout);
		ActionBar aBar = getSupportActionBar();
		aBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
		aBar.setDisplayShowHomeEnabled(true);
		aBar.setDisplayHomeAsUpEnabled(true);
		aBar.setDisplayShowTitleEnabled(true);
		aBar.setHomeButtonEnabled(true);
		
		/*
		 SwipeGestureListener swipe = new SwipeGestureListener(this, new SwipeGestureListener.OnSwipe(){
			@Override
			public void swipeToLeft() {}

			@Override
			public void swipeToRight() {
				finalizar();
			}
			
		});
		
		getWindow().getDecorView().getRootView().setOnTouchListener(swipe.getGestureListener());
		*/
		
		AQuery aq = new AQuery(this);

		String nombre = null, precio = null, descripcion = null, icon = null;

		Bundle b = getIntent().getExtras();
		if (b != null) {
			nombre = b.containsKey(NOMBRE) ? b.getString(NOMBRE) : null;
			precio = b.containsKey(PRECIO) ? b.getString(PRECIO) : null;
			descripcion = b.containsKey(DESCRIPCION) ? b.getString(DESCRIPCION) : null;
			icon = b.containsKey(ICON) ? b.getString(ICON) : null;
		}
		cargarImagen(aq, icon);
		if (nombre != null) {
			setTitle(Html.fromHtml("<b>" + nombre + "</b>"));
			aq.id(R.id.nombre).text(nombre);
		}
		if (precio != null)
			aq.id(R.id.precio).text(precio);
		if (descripcion != null) {
			aq.id(R.id.descripcion).getWebView().loadDataWithBaseURL(null, descripcion, "text/html", "utf-8", null);
		}
	}

	private void cargarImagen(AQuery aq, String icon) {
		Picasso.with(Promocion.this).load(Uri.parse(icon)).placeholder(R.drawable.image).error(R.drawable.no_image)
				.into(aq.id(R.id.imagen).getImageView());
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
		super.onBackPressed();
		finalizar();
	}

	private void finalizar() {
		finish();
		overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
	}

}
