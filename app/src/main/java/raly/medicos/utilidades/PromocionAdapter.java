package raly.medicos.utilidades;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import raly.medicos.app.R;

public class PromocionAdapter extends BaseAdapter implements OnClickListener {

	private List<PromocionItem> promocionItems;
	private Context contexto;

	public PromocionAdapter(Context c, List<PromocionItem> l) {
		contexto = c;
		promocionItems = l;
	}

	@Override
	public int getCount() {
		return promocionItems.size();
	}

	@Override
	public Object getItem(int position) {
		return promocionItems.get(position);
	}

	@Override
	public int getViewTypeCount() {
		return super.getViewTypeCount();
	}

	@Override
	public long getItemId(int position) {
		return promocionItems.indexOf(getItem(position));
	}

	private class ViewHolder {
		TextView titulo;
		TextView precio;
		ImageView icon;
	}

	private int getLayout(int position) {
		return R.layout.promocionitem_layout_1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(contexto).inflate(getLayout(position), null);

			holder.titulo = (TextView) convertView.findViewById(R.id.titulo_promocion);
			holder.precio = (TextView) convertView.findViewById(R.id.precio_promocion);
			holder.icon = (ImageView) convertView.findViewById(R.id.imagen_promocion);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		PromocionItem promocion = (PromocionItem) getItem(position);
		String url = promocion.getImagen().toString();

		holder.titulo.setText(promocion.getTitulo());
		holder.precio.setText(promocion.getPrecio());
		Picasso.with(contexto).load(Uri.parse(url)).into(holder.icon);

		holder.icon.setOnClickListener(this);

		return convertView;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imagen_promocion:
			verImagen((ImageView) v);
			break;
		}
	}

	private void verImagen(ImageView v) {
		if (v.getDrawable().getClass().getSimpleName().equalsIgnoreCase("picassodrawable")) {
			final Dialog dialog = new Dialog((SherlockActivity) contexto, android.R.style.Theme_Translucent);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setCancelable(true);

			dialog.setContentView(R.layout.overlay_dialog_image);

			ImageView imagen = (ImageView) dialog.findViewById(R.id.promo_imagen_full);
			imagen.setImageDrawable(v.getDrawable());

			imagen.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					dialog.dismiss();

				}
			});

			dialog.show();
		}
	}
}