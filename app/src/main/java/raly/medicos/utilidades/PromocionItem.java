package raly.medicos.utilidades;

import org.json.JSONException;
import org.json.JSONObject;

public class PromocionItem {
	private String titulo, precio, url, imagen, html;

	public PromocionItem(JSONObject data) {
		try {
			this.titulo = data.getString("titulo");
			this.precio = data.getString("precio");
			this.url = data.getString("url");
			this.imagen = data.getString("imagen");
			this.html = data.getString("html");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public PromocionItem(JSONObject data, DB medicodb) {
		try {
			this.titulo = data.getString("titulo");
			this.precio = data.getString("precio");
			this.url = data.getString("url");
			this.imagen = data.getString("imagen");
			this.html = data.getString("html");
			medicodb.savePromocion(data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getUrl() {
		return this.url;
	}

	public String getPrecio() {
		return precio;
	}

	public String getImagen() {
		return imagen.isEmpty() ? "" : imagen;
	}

	public String getTitulo() {
		return titulo;
	}

	public String getDescripcion() {
		return html;
	}
}