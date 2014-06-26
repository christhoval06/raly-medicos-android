package raly.medicos.utilidades;

public class ResultadosItem {

	private ResultadoItem resultado = null;
	private boolean esGrupo = false;
	private String grupoText = "";

	public ResultadosItem(ResultadoItem item) {
		resultado = item;
	}

	public ResultadosItem(String text) {
		esGrupo = true;
		grupoText = text;
	}

	public String getGrupoText() {
		return grupoText;
	}

	public ResultadoItem getResultado() {
		return resultado;
	}

	public boolean isGrupo() {
		return esGrupo;
	}

}
