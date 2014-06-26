package raly.medicos.utilidades;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class MiResultadoItem {
	private String resultadoid;
	private String resultado;
	private String pacienteid;
	private String paciente;
	private String file;
	private String fecha;

	private DB medicodb;

	public MiResultadoItem(JSONObject data, DB _medicodb) {
		medicodb = _medicodb;
		try {
			pacienteid = data.getString("pacienteid");
			paciente = data.getString("paciente");
			file = data.getString("file");
			fecha = data.getString("fecha");
			resultadoid = data.getString("resultadoid");
			resultado = data.getString("resultado");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getResultado() {
		return resultado;
	}

	public String getFile() {
		return file;
	}

	public String getPaciente() {
		return paciente;
	}

	public String getPacienteid() {
		return pacienteid;
	}

	public String getResultadoid() {
		return resultadoid;
	}

	@Override
	public String toString() {
		return getPaciente() + " - " + getResultado();
	}

	public boolean delete() {
		File file = new File(getFile());
		if (!file.exists())
			return false;
		if (!file.isDirectory()) {
			if (file.delete()) {
				return medicodb.deleteResultadoFile(getPacienteid(), getResultadoid());
			}
		}
		return false;
	}

	public String getFecha() {
		return fecha;
	}
}
