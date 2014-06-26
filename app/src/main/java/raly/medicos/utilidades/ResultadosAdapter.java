package raly.medicos.utilidades;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import raly.medicos.app.R;

public class ResultadosAdapter extends BaseAdapter {

	List<ResultadosItem> lista;
	Context contexto;
	SimpleDateFormat sdf;

	private static final int VIEW_TYPE_GROUP_START = 0;
	private static final int VIEW_TYPE_GROUP_CONT = 1;
	private static final int VIEW_TYPE_COUNT = 2;

	@SuppressLint("SimpleDateFormat")
	public ResultadosAdapter(Context c, List<ResultadoItem> list) {
		contexto = c;
		List<ResultadoItem> resultados = list;

		sdf = new SimpleDateFormat("dd/MM/yyyy");
		Collections.sort(resultados, new Comparator<ResultadoItem>() {

			@Override
			public int compare(ResultadoItem lhs, ResultadoItem rhs) {
				if (sdf.parse(lhs.getFecha(), new ParsePosition(0)).before(sdf.parse(rhs.getFecha(), new ParsePosition(0)))) {
					return -1;
				} else if (sdf.parse(lhs.getFecha(), new ParsePosition(0)).equals(sdf.parse(rhs.getFecha(), new ParsePosition(0)))) {
					return 0;
				} else {
					return 1;
				}
			}
		});

		Collections.reverse(resultados);
		lista = getLista(list);
	}

	private List<ResultadosItem> getLista(List<ResultadoItem> list) {
		List<ResultadosItem> _lista = new ArrayList<ResultadosItem>();
		for (int i = 0; i < list.size(); i++) {
			if (i == 0) {
				_lista.add(new ResultadosItem((list.get(i)).getFecha()));
				_lista.add(new ResultadosItem(list.get(i)));
			} else {
				boolean newGroup = isNewGroup(list, i);
				if (newGroup) {
					_lista.add(new ResultadosItem((list.get(i)).getFecha()));
					_lista.add(new ResultadosItem(list.get(i)));
				} else
					_lista.add(new ResultadosItem(list.get(i)));
			}
		}
		return _lista;
	}

	@Override
	public int getCount() {
		return lista.size();
	}

	@Override
	public int getViewTypeCount() {
		return VIEW_TYPE_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		if (getItem(position).isGrupo())
			return VIEW_TYPE_GROUP_START;
		else
			return VIEW_TYPE_GROUP_CONT;
	}

	@Override
	public ResultadosItem getItem(int position) {
		return lista.get(position);
	}

	@Override
	public long getItemId(int position) {
		return lista.indexOf(getItem(position));
	}

	private class ViewHolder {
		TextView grupo = null;
		TextView titulo;
		ImageView descargado;
	}

	private int getLayout(int position) {
		if ((lista.get(position)).isGrupo()) {
			return R.layout.resultado_item_grupo_layout_1;
		} else
			return R.layout.resultado_item_layout_1;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(contexto).inflate(getLayout(position), null);

			holder.titulo = (TextView) convertView.findViewById(R.id.resultado_titulo);
			holder.grupo = (TextView) convertView.findViewById(R.id.resultado_grupo);
			holder.descargado = (ImageView) convertView.findViewById(R.id.downloaded_file);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ResultadosItem item = (ResultadosItem) getItem(position);

		if (!item.isGrupo()) {
			holder.titulo.setText(item.getResultado().getTitulo());
			if (item.getResultado().getTitulo().toUpperCase().startsWith("---VER"))
				convertView.setBackgroundColor(contexto.getResources().getColor(R.color.white_smoke));
			else
				convertView.setBackgroundColor(contexto.getResources().getColor(R.color.white));

			if (item.getResultado().isDownloaded())
				holder.descargado.setImageResource(R.drawable.ic_downloaded_file);
			else
				holder.descargado.setImageResource(R.drawable.ic_no_downloaded_file);
		} else {
			if (holder.grupo != null)
				holder.grupo.setText(item.getGrupoText());
			convertView.setEnabled(false);
		}

		return convertView;
	}

	private boolean isNewGroup(List<ResultadoItem> list, int position) {
		ResultadoItem prev, actual = list.get(position);
		try {
			prev = list.get(position - 1);
		} catch (NullPointerException e) {
			prev = null;
		}
		if (prev == null) {
			return true;
		}

		if (sdf.parse(prev.getFecha(), new ParsePosition(0)).before(sdf.parse(actual.getFecha(), new ParsePosition(0)))) {
			return false;
		} else if (sdf.parse(prev.getFecha(), new ParsePosition(0)).equals(sdf.parse(actual.getFecha(), new ParsePosition(0)))) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean isEnabled(int position) {
		return !lista.get(position).isGrupo();
	}

}