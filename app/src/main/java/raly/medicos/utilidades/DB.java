package raly.medicos.utilidades;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class DB {
	private Context nContext;
	private SQLiteDatabase db = null;

	private DBHelper medicosdb;

	public static String TABLA_PROMOCIONES = "promociones", TABLA_PACIENTES = "pacientes", TABLA_FILES = "resultadosfiles",
			TABLA_RESULTADOS = "resultados", TABLA_MEDICOS = "medicos";

	public static String sqlPromociones = "CREATE TABLE IF NOT EXISTS " + TABLA_PROMOCIONES + " (" + "id INTEGER primary key autoincrement,"
			+ "titulo TEXT not null," + "precio TEXT not null," + "url TEXT not null," + "imagen TEXT not null," + "html TEXT not null)",
			sqlPacientes = "CREATE TABLE IF NOT EXISTS " + TABLA_PACIENTES + " (" + "id INTEGER primary key autoincrement,"
					+ "pacienteid TEXT not null," + "nombre TEXT not null," + "codigo TEXT not null," + "cedula TEXT not null,"
					+ "foto TEXT not null, " + "telefono TEXT not null)",
			sqlFiles = "CREATE TABLE IF NOT EXISTS "
					+ TABLA_FILES
					+ " (id INTEGER primary key autoincrement, pacienteid TEXT not null, paciente TEXT , resultadoid TEXT not null, resultado TEXT, fecha TEXT , file TEXT not null)",
			sqlMedicos = "CREATE TABLE IF NOT EXISTS "
					+ TABLA_MEDICOS
					+ " (id INTEGER primary key autoincrement, medico TEXT not null, telefono TEXT not null, usuario TEXT not null, skey TEXT not null)",
			sqlResultados = "CREATE TABLE IF NOT EXISTS "
					+ TABLA_RESULTADOS
					+ " (id INTEGER primary key autoincrement, titulo TEXT not null, pacienteid TEXT not null, fecha TEXT not null, resultadoid TEXT not null, pdf TEXT, estado INTEGER)";

	public DB(Context context) {
		nContext = context;
		medicosdb = DBHelper.getInstance(nContext);
		db = medicosdb.getWritableDatabase();
		if (db != null) {
		}
	}

	public SQLiteDatabase db() {
		return this.db;
	}

	public JSONObject sql2JSONOBJECT(String sql, String items) {
		return sql2JSONOBJECT(db.rawQuery(sql, null), items);
	}

	public JSONObject sql2JSONOBJECT(Cursor c, String items) {
		if (c != null) {
			try {
				if (c.moveToFirst()) {
					JSONObject json = new JSONObject();
					JSONArray data = new JSONArray();
					json.put(items, data);
					do {
						JSONObject j = new JSONObject();
						for (String item : c.getColumnNames())
							j.put(c.getColumnName(c.getColumnIndex(item)), c.getString(c.getColumnIndex(item)));
						data.put(j);
					} while (c.moveToNext());

					return json;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			} finally {
				c.close();
			}
		}
		return null;
	}

	public JSONObject sql2JSONARRAY(String sql) {
		return sql2JSONARRAY(db.rawQuery(sql, null));
	}

	public JSONObject sql2JSONARRAY(Cursor c) {
		if (c != null) {
			try {
				if (c.moveToFirst()) {
					JSONObject json = new JSONObject();
					do {
						for (String item : c.getColumnNames()) {
							json.put(c.getColumnName(c.getColumnIndex(item)), c.getString(c.getColumnIndex(item)));
						}
					} while (c.moveToNext());
					return json;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			} finally {
				c.close();
			}
		}
		return null;
	}

	public boolean borrarTabla(String tabla) {
		return db.delete(tabla, null, null) > 0;
	}

	public boolean save(String tabla, JSONObject json) {
		long id = 0;
		ContentValues cv = new ContentValues();
		try {
			@SuppressWarnings("unchecked")
			Iterator<Object> keys = json.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				cv.put(key, json.getString(key));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		id = db.insert(tabla, null, cv);
		if (id != -1 && id > 0) {
			return true;
		}
		return false;
	}

	public JSONObject getAll(String tabla, String key) {
		return sql2JSONOBJECT("SELECT * FROM " + tabla, key);
	}

	public JSONObject getAll(Cursor c, String key) {
		return sql2JSONOBJECT(c, key);
	}

	public boolean have(String tabla) {
		return have(db.rawQuery("SELECT * FROM " + tabla + " LIMIT 1;", null));
	}

	public boolean have(Cursor c) {
		if (c != null) {
			try {

				if (c.moveToFirst()) {
					return true;
				}

			} finally {
				c.close();
			}
		}
		return false;
	}

	public void clearCache() {
		borrarTabla(TABLA_PACIENTES);
		borrarTabla(TABLA_PROMOCIONES);
		borrarTabla(TABLA_RESULTADOS);
	}

	/****************** MEDICO ***************************/
	public boolean isMedico() {
		return  have(db.rawQuery("SELECT * FROM " + TABLA_MEDICOS + " LIMIT 1;", null));
	}

	public boolean deleteMedico() {
		return borrarTabla(TABLA_MEDICOS);
	}

	public boolean saveMedico(JSONObject json) {
		return save(TABLA_MEDICOS, json);
	}

	public String getSkey() {
		return getMedico("skey");
	}

	public String getMedicoNombre() {
		return getMedico("medico");
	}

	public JSONObject getMedico() {
		Cursor c = db.rawQuery("SELECT * FROM medicos LIMIT 1", null);
		if (c != null) {
			try {
				if (c.moveToFirst()) {
					JSONObject json = new JSONObject();
					do {
						for (String item : c.getColumnNames())
							json.put(c.getColumnName(c.getColumnIndex(item)), c.getString(c.getColumnIndex(item)));

					} while (c.moveToNext());
					return json;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			} finally {
				c.close();
			}
		}

		return null;
	}

	public String getMedico(String key) {
		Cursor c = db.rawQuery("SELECT * FROM " + TABLA_MEDICOS + " LIMIT 1", null);
		if (c != null) {
			try {
				if (c.moveToFirst()) {
					String dato = "";
					do {
						dato = c.getString(c.getColumnIndex(key));
					} while (c.moveToNext());
					return dato;
				}
			} finally {
				c.close();
			}
		}
		return null;
	}

	/************************ PROMOCIONES ****************/
	public boolean savePromocion(JSONObject json) {
		return save(TABLA_PROMOCIONES, json);
	}

	public JSONObject getPromociones() {
		return getAll(TABLA_PROMOCIONES, "promociones");
	}

	public boolean deletePromociones() {
		return borrarTabla(TABLA_PROMOCIONES);
	}

	/*********************** PACIENTES ***********************************/
	public String getPaciente(String key, String pacienteid) {
		Cursor c = db.rawQuery("SELECT * FROM " + TABLA_PACIENTES + " Where pacienteid=? LIMIT 1;", new String[] { pacienteid });
		if (c != null) {
			try {
				if (c.moveToFirst()) {
					String dato = "";
					do {
						dato = c.getString(c.getColumnIndex(key));
					} while (c.moveToNext());
					return dato;
				}
			} finally {
				c.close();
			}
		}
		return null;
	}

	public JSONObject getPaciente(String pacienteid) {
		return sql2JSONARRAY(db.rawQuery("SELECT * FROM " + TABLA_PACIENTES + " Where pacienteid=? LIMIT 1;", new String[] { pacienteid }));
	}

	public boolean savePaciente(JSONObject json) {
		return save(TABLA_PACIENTES, json);
	}

	public boolean deletePacientes() {
		return borrarTabla(TABLA_PACIENTES);
	}

	public JSONObject getPacientes() {
		return getAll(TABLA_PACIENTES, "pacientes");
	}

	/****************** RESULTADOS ***************************/

	public boolean saveResultado(JSONObject json) {
		return save(TABLA_RESULTADOS, json);
	}

	public JSONObject getResultados(String pacienteid) {
		return getAll(db.rawQuery("SELECT * FROM " + TABLA_RESULTADOS + " Where pacienteid=?;", new String[] { pacienteid }), "resultados");
	}

	public boolean deleteResultados() {
		return borrarTabla(TABLA_RESULTADOS);

	}

	public boolean PacienteHaveResultados(String pacienteid) {
		return have(db.rawQuery("SELECT * FROM " + TABLA_RESULTADOS + " Where pacienteid=? LIMIT 1;", new String[] { pacienteid }));
	}

	public boolean deleteResultados(String pacienteid) {
		return db.delete(DB.TABLA_RESULTADOS, "pacienteid=?", new String[] { pacienteid }) > 0;

	}

	/****************** RESULTADOS FILES ***************************/

	public JSONObject getResultadosFiles() {
		return getAll(db.rawQuery("SELECT * FROM " + TABLA_FILES + " order by paciente,fecha asc,resultado", null), "resultados");
	}

	public boolean deleteResultadoFile(String pacienteid, String resultadoid) {
		return db.delete(DB.TABLA_FILES, "pacienteid=? and resultadoid=?", new String[] { pacienteid, resultadoid }) > 0;

	}

	public boolean saveResultadoFile(JSONObject json) {
		return save(TABLA_FILES, json);
	}

	public String getPDF(String pacienteid, String resultadoid) {
		Cursor c = db.rawQuery("SELECT file FROM " + TABLA_FILES + " Where pacienteid=? and resultadoid=? LIMIT 1;", new String[] { pacienteid,
				resultadoid });
		if (c != null) {
			try {
				if (c.moveToFirst()) {
					String file = null;
					do {
						file = c.getString(c.getColumnIndex("file"));
					} while (c.moveToNext());
					return file;
				}
			} finally {
				c.close();
			}
		}
		return "";
	}

}