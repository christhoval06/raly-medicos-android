package raly.medicos.utilidades;

import org.json.JSONException;
import org.json.JSONObject;

public class PacienteItem {
	private int imageId;
	private String pacienteid;
	private String nombre;
	private String codigo;
	private String cedula;
	private String foto;
	private String telefono;
	private String skey;

	public PacienteItem(JSONObject data, String skey) {
		try {
			this.pacienteid = data.getString("pacienteid");
			this.nombre = data.getString("nombre");
			this.cedula = data.getString("cedula");
			this.codigo = data.getString("codigo");
			this.foto = data.getString("foto");
			this.telefono = data.getString("telefono");
			this.skey = skey;
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public PacienteItem(JSONObject data, String skey, DB medicosdb) {
		try {
			this.pacienteid = data.getString("pacienteid");
			this.nombre = data.getString("nombre");
			this.cedula = data.getString("cedula");
			this.codigo = data.getString("codigo");
			this.foto = data.getString("foto");
			this.telefono = data.getString("telefono");
			this.skey = skey;
			data.remove("fechanacimiento");
			medicosdb.savePaciente(data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public int getImageId() {
		return imageId;
	}

	public String getPacienteid() {
		return pacienteid;
	}

	public String getFoto() {
		return foto.equalsIgnoreCase("../images/fotonula.jpg") ? "" : foto + "&skey=" + skey;
	}

	public String getCodigo() {
		return codigo;
	}

	public String getCedula() {
		return cedula;
	}

	public String getTelefono() {
		return telefono;
	}

	public String getNombre() {
		return nombre;
	}

	public void setImageId(int imageId) {
		this.imageId = imageId;
	}

	public void setPacienteid(String pacienteid) {
		this.pacienteid = pacienteid;
	}

	public void setFoto(String foto) {
		this.foto = foto;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public void setCedula(String cedula) {
		this.cedula = cedula;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getSkey() {
		return skey;
	}

	@Override
	public String toString() {
		return getNombre() + " " + getCedula() + " " + getCodigo();
	}

}
