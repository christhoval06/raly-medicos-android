package raly.medicos.utilidades;

import org.json.JSONException;
import org.json.JSONObject;

public class ResultadoItem {
	private String titulo;
	private String fecha;
	private String pdf;
	private int estado;
	private String resultadoid;

	private String skey;

	private boolean downloaded = false;

	public ResultadoItem(JSONObject data, boolean _downloaded, String skey) {
		try {
			this.titulo = data.getString("titulo");
			this.fecha = data.getString("fecha");
			this.pdf = data.getString("pdf");
			this.estado = data.getInt("estado");
			this.resultadoid = data.getString("resultadoid");
			this.skey = skey;
			downloaded = _downloaded;
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public ResultadoItem(JSONObject data, boolean _downloaded, String skey, String pacienteid, DB medicosdb) {
		try {
			this.titulo = data.getString("titulo");
			this.fecha = data.getString("fecha");
			this.pdf = data.getString("pdf");
			this.estado = data.getInt("estado");
			this.resultadoid = data.getString("resultadoid");
			this.skey = skey;
			downloaded = _downloaded;
			data.put("pacienteid", pacienteid);
			medicosdb.saveResultado(data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getResultadoid() {
		return resultadoid;
	}

	public String getPdf() {
		return pdf.replace("/raly", "http://laboratorioraly.com/raly") + "&skey=" + skey + "&d=2";
	}

	public String getFecha() {
		return fecha;
	}

	public boolean getEstado() {
		return estado == 1 ? true : false;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setResultadoid(String resultadoid) {
		this.resultadoid = resultadoid;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public void setPdf(String pdf) {
		this.pdf = pdf;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public void setEstado(int estado) {
		this.estado = estado;
	}

	public boolean isDownloaded() {
		return downloaded;
	}

	public void setDownloaded(boolean _downloaded) {
		downloaded = _downloaded;
	}

	@Override
	public String toString() {
		return getTitulo() + " " + getFecha();
	}

}
