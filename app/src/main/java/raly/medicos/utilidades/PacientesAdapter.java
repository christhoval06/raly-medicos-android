package raly.medicos.utilidades;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import raly.medicos.app.R;

@SuppressLint("DefaultLocale")
public class PacientesAdapter extends BaseAdapter implements Filterable, SectionIndexer, OnClickListener {

	private List<PacienteItem> pacientes;
	private List<PacienteItem> originalItems;
	private Context contexto;
	private String[] sections;
	private HashMap<String, Integer> alphaIndexer;
	private AQuery aQ;
	private Utils utils;
	private Dialogs d;

	private static final int VIEW_TYPE_GROUP_START = 0;
	private static final int VIEW_TYPE_GROUP_CONT = 1;
	private static final int VIEW_TYPE_COUNT = 2;

	public PacientesAdapter(Context c, List<PacienteItem> list) {
		contexto = c;
		pacientes = list;
		aQ = new AQuery(c);
		utils = new Utils(c);
		d = new Dialogs((SherlockListActivity) contexto);

		Collections.sort(this.pacientes, new Comparator<PacienteItem>() {

			@Override
			public int compare(PacienteItem lhs, PacienteItem rhs) {
				String nombre1 = lhs.getNombre();
				String nombre2 = rhs.getNombre();

				int orden = nombre1.compareToIgnoreCase(nombre2);
				if (orden > 0) {
					return 1;
				} else if (orden < 0) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		originalItems = this.pacientes;

		alphaIndexer = new HashMap<String, Integer>();
		int size = pacientes.size();
		for (int i = size - 1; i >= 0; i--) {
			String element = pacientes.get(i).getNombre();
			alphaIndexer.put(element.substring(0, 1), i);
		}

		Set<String> keys = alphaIndexer.keySet();
		Iterator<String> it = keys.iterator();
		ArrayList<String> keyList = new ArrayList<String>();

		while (it.hasNext()) {
			keyList.add(it.next());
		}

		Collections.sort(keyList);

		sections = new String[keyList.size()];
		keyList.toArray(sections);
	}

	@Override
	public int getCount() {
		return pacientes.size();
	}

	@Override
	public int getViewTypeCount() {
		return VIEW_TYPE_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return VIEW_TYPE_GROUP_START;
		}
		boolean newGroup = isNewGroup(position);
		if (newGroup) {
			return VIEW_TYPE_GROUP_START;
		} else {
			return VIEW_TYPE_GROUP_CONT;
		}
	}

	@Override
	public Object getItem(int position) {
		return pacientes.get(position);
	}

	@Override
	public long getItemId(int position) {
		return pacientes.indexOf(getItem(position));
	}

	private class ViewHolder {
		TextView grupo = null;
		ImageView imageView;
		TextView nombre;
		TextView cedula;
		TextView codigo;
	}

	private int getLayout(int position) {
		int nViewType = getItemViewType(position);
		if (nViewType == VIEW_TYPE_GROUP_START) {
			return R.layout.paciente_item_grupo_layout_1;
		} else
			return R.layout.paciente_item_layout_1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(contexto).inflate(getLayout(position), null);

			holder.nombre = (TextView) convertView.findViewById(R.id.paciente_nombre);
			holder.cedula = (TextView) convertView.findViewById(R.id.paciente_cedula);
			holder.codigo = (TextView) convertView.findViewById(R.id.paciente_codigo);
			holder.imageView = (ImageView) convertView.findViewById(R.id.paciente_imagen);
			holder.grupo = (TextView) convertView.findViewById(R.id.paciente_grupo);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		PacienteItem paciente = (PacienteItem) getItem(position);

		String url = paciente.getFoto().toString();

		holder.nombre.setText(paciente.getNombre());
		holder.codigo.setText(paciente.getCodigo());
		holder.cedula.setText(paciente.getCedula());

		Picasso.with(contexto).load(Uri.parse(url)).placeholder(R.drawable.no_image).error(R.drawable.no_image)
				.resizeDimen(R.dimen.pacientes_item_image_size_h, R.dimen.pacientes_item_image_size_h).centerCrop().into(holder.imageView);

		holder.imageView.setOnClickListener(this);
		holder.imageView.setTag(R.string.mi_resultado_tag, paciente);

		if (holder.grupo != null) {
			holder.grupo.setText(paciente.getNombre().substring(0, 1));
		}
		return convertView;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {

			@SuppressLint("DefaultLocale")
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();
				List<PacienteItem> FilteredArrList = new ArrayList<PacienteItem>();

				if (constraint == null || constraint.length() == 0) {
					results.count = originalItems.size();
					results.values = originalItems;
				} else {
					constraint = constraint.toString();

					for (int i = 0; i < originalItems.size(); i++) {
						PacienteItem data = originalItems.get(i);

						if (data.getNombre().toLowerCase().startsWith(constraint.toString().toLowerCase())
								|| data.getCedula().toLowerCase().startsWith(constraint.toString().toLowerCase())
								|| data.getCodigo().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
							FilteredArrList.add(data);
						}
					}

					results.count = FilteredArrList.size();
					results.values = FilteredArrList;
				}

				return results;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				pacientes = (List<PacienteItem>) results.values;
				notifyDataSetChanged();
			}

		};
		return filter;
	}

	@Override
	public int getPositionForSection(int section) {
		String letter = sections[section];
		return alphaIndexer.get(letter);
	}

	@Override
	public int getSectionForPosition(int position) {
		return 0;
	}

	@Override
	public Object[] getSections() {
		return sections;
	}

	private boolean isNewGroup(int position) {
		PacienteItem prev, actual = pacientes.get(position);
		try {
			prev = pacientes.get(position - 1);
		} catch (NullPointerException e) {
			prev = null;
		}
		if (prev == null) {
			return true;
		}

		int orden = prev.getNombre().substring(0, 1).compareToIgnoreCase(actual.getNombre().substring(0, 1));
		if (orden > 0) {
			return false;
		} else if (orden < 0) {
			return true;
		} else {
			return false;
		}
	}

	public void clearData() {
		pacientes.clear();
		originalItems.clear();
		notifyDataSetChanged();
	}

	public void setData(List<PacienteItem> list) {
		this.pacientes = list;

		Collections.sort(this.pacientes, new Comparator<PacienteItem>() {

			@Override
			public int compare(PacienteItem lhs, PacienteItem rhs) {
				String nombre1 = lhs.getNombre();
				String nombre2 = rhs.getNombre();

				int orden = nombre1.compareToIgnoreCase(nombre2);
				if (orden > 0) {
					return 1;
				} else if (orden < 0) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		originalItems = this.pacientes;

		alphaIndexer = new HashMap<String, Integer>();
		int size = pacientes.size();
		for (int i = size - 1; i >= 0; i--) {
			String element = pacientes.get(i).getNombre();
			alphaIndexer.put(element.substring(0, 1), i);
		}

		Set<String> keys = alphaIndexer.keySet();
		Iterator<String> it = keys.iterator();
		ArrayList<String> keyList = new ArrayList<String>();

		while (it.hasNext()) {
			keyList.add(it.next());
		}

		Collections.sort(keyList);

		sections = new String[keyList.size()];
		keyList.toArray(sections);

		notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.paciente_imagen:
			mostarDataPaciente((ImageView) v);
			break;
		case 21212:
			try {
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + v.getTag(R.string.paciente_telefono)));
				((SherlockListActivity) contexto).startActivity(intent);
			} catch (ActivityNotFoundException e) {

			}
			break;
		case 31313:
			Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + v.getTag(R.string.paciente_email)));
			((SherlockListActivity) contexto).startActivity(Intent.createChooser(emailIntent,
					((SherlockListActivity) contexto).getString(R.string.msg_selecct_app)));
			break;
		default:
			break;
		}

	}

	private void mostarDataPaciente(final ImageView v) {
		if (utils.isOnline()) {
			PacienteItem paciente = (PacienteItem) v.getTag(R.string.mi_resultado_tag);
			String url = "http://laboratorioraly.com/raly/medicom";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("skey", paciente.getSkey());
			params.put("fnt", "datapaciente");
			params.put("pacienteid", paciente.getPacienteid());
			((SherlockListActivity) contexto).setSupportProgressBarIndeterminateVisibility(true);
			aQ.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {

				@Override
				public void callback(String url, JSONObject json, AjaxStatus status) {
					if (status.getCode() == 200) {
						mostrarDialogo(v, json);
					}
					((SherlockListActivity) contexto).setSupportProgressBarIndeterminateVisibility(false);
				}
			});

		} else
			d.alert("", ((SherlockListActivity) contexto).getString(R.string.no_internet_error), null);
	}

	private void mostrarDialogo(ImageView v, JSONObject json) {
		final Dialog dialog = new Dialog((SherlockListActivity) contexto, android.R.style.Theme_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCancelable(true);

		dialog.setContentView(R.layout.overlay_dialog_paciente_info);

		RelativeLayout r = (RelativeLayout) dialog.findViewById(R.id.dialogo_full);
		r.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();

			}
		});

		ImageView imagen = (ImageView) dialog.findViewById(R.id.paciente_imagen_full);
		imagen.setImageDrawable(v.getDrawable());

		TextView nombrePaciente = (TextView) dialog.findViewById(R.id.nombrePaciente);
        LinearLayout botones = (LinearLayout) dialog.findViewById(R.id.pacienteComunicacion);
        try {
            JSONObject paciente = (json.getJSONArray("paciente")).getJSONObject(0);
            String nombre = paciente.getString("nombre1"), apellido = paciente.getString("apellido1");
            nombrePaciente.setText(nombre + " " + apellido);

            String telefono = paciente.getString("telefono"), email = paciente.getString("mail");

            if (!telefono.isEmpty()) {
                if (utils.isAvailable(contexto, new Intent(Intent.ACTION_DIAL, Uri.parse("tel: " + telefono))))
                    createImageButton(botones, 21212, R.drawable.ic_phone, R.string.paciente_telefono, telefono);
            }
            if (!email.isEmpty())
                createImageButton(botones, 31313, R.drawable.ic_email, R.string.paciente_email, email);
            if (telefono.isEmpty() && email.isEmpty())
                createTextView(botones, ((SherlockListActivity) contexto).getString(R.string.msg_no_contact_info));
        } catch (JSONException e) {
            e.printStackTrace();
        }
		imagen.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();

			}
		});

		dialog.show();
	}

	private void createImageButton(LinearLayout l, int id, int src, int tag, String Tag) {
		ImageButton btn = new ImageButton(contexto);
		btn.setImageResource(src);
		btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
		btn.setOnClickListener(this);
		btn.setBackgroundColor(Color.TRANSPARENT);
		btn.setTag(tag, Tag);
		btn.setId(id);

		l.addView(btn);
	}

	private void createTextView(LinearLayout l, String text) {

		TextView tv = new TextView(contexto);
		tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
		tv.setBackgroundColor(((SherlockListActivity) contexto).getResources().getColor(R.color.malibu));
		tv.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		tv.setPadding(3, 3, 3, 3);
		tv.setShadowLayer(2f, 2f, 2f, R.color.dark_gray);
		tv.setTextColor(((SherlockListActivity) contexto).getResources().getColor(R.color.black));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		tv.setTypeface(Typeface.DEFAULT_BOLD);
		tv.setText(text);

		l.addView(tv);
	}
}