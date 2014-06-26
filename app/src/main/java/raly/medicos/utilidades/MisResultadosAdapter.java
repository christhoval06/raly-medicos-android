package raly.medicos.utilidades;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import raly.medicos.app.R;

public class MisResultadosAdapter extends BaseAdapter implements OnClickListener {

	List<MiResultadoItem> resultados;
	Context contexto;
	private Dialogs dialogos;

	private static final int VIEW_TYPE_GROUP_START = 0;
	private static final int VIEW_TYPE_GROUP_CONT = 1;
	private static final int VIEW_TYPE_COUNT = 2;

	public MisResultadosAdapter(Context c, List<MiResultadoItem> list, Dialogs _dialogos) {
		contexto = c;
		resultados = list;
		dialogos = _dialogos;
	}

	@Override
	public int getCount() {
		return resultados.size();
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
		return resultados.get(position);
	}

	@Override
	public long getItemId(int position) {
		return resultados.indexOf(getItem(position));
	}

	private class ViewHolder {
		TextView grupo = null;
		TextView titulo;
		TextView fecha;
		ImageButton borrar;
	}

	private int getLayout(int position) {
		int nViewType = getItemViewType(position);
		if (nViewType == VIEW_TYPE_GROUP_START) {
			return R.layout.mi_resultado_item_grupo_layout;
		} else
			return R.layout.mi_resultado_item_layout;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(contexto).inflate(getLayout(position), null);

			holder.titulo = (TextView) convertView.findViewById(R.id.resultado_titulo);
			holder.grupo = (TextView) convertView.findViewById(R.id.resultado_grupo);
			holder.fecha = (TextView) convertView.findViewById(R.id.resultado_fecha);
			holder.borrar = (ImageButton) convertView.findViewById(R.id.file_delete);
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		MiResultadoItem resultado = (MiResultadoItem) getItem(position);

		holder.titulo.setText(resultado.getResultado());
		holder.fecha.setText(resultado.getFecha());

		if (holder.grupo != null) {
			holder.grupo.setText(resultado.getPaciente());
		}

		holder.borrar.setTag(R.string.mi_resultado_tag, resultado);
		holder.borrar.setTag(R.string.mi_resultado_posicion_tag, position);
		holder.borrar.setOnClickListener(this);

		return convertView;
	}

	private boolean isNewGroup(int position) {
		MiResultadoItem prev;
		MiResultadoItem actual = resultados.get(position);
		try {
			prev = resultados.get(position - 1);
		} catch (NullPointerException e) {
			prev = null;
		}
		if (prev == null) {
			return true;
		}
		if (prev.getPaciente().compareToIgnoreCase(actual.getPaciente()) == 0)
			return false;
		else
			return true;
	}

	private void borrar(final MiResultadoItem resultado, final int posicion) {
		dialogos.notification(Html.fromHtml(resultado.getPaciente()),
				Html.fromHtml("<b>" + (contexto.getString(R.string.msg_file_delete)).replace("xxxx", resultado.getResultado()) + "</b>"),
				contexto.getString(R.string.btn_ok), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (resultado.delete())
							if (resultados.remove(resultados.get(posicion))) {
								notifyDataSetChanged();
								dialog.cancel();
							}

					}
				});
	}

	@Override
	public void onClick(View v) {
		final MiResultadoItem resultado = ((MiResultadoItem) v.getTag(R.string.mi_resultado_tag));
		switch (v.getId()) {
		case R.id.file_delete:
			borrar(resultado, (Integer) v.getTag(R.string.mi_resultado_posicion_tag));
			break;
		}
	}

}