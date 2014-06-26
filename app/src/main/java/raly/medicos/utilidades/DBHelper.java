package raly.medicos.utilidades;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static DBHelper sInstance = null;
	public static final int VERSION_DB = 2;
	private static String DBNAME = "medicoDB";

	public static DBHelper getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new DBHelper(context.getApplicationContext(), DBNAME, null, VERSION_DB);
		}
		return sInstance;
	}

	private DBHelper(Context _contexto, String db, CursorFactory factory, int version) {
		super(_contexto, db, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTablas(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1 && newVersion == 2) {
			recrearTablas(db);
		} else {
			recrearTablas(db);
		}
	}

	public void recrearTablas(SQLiteDatabase db) {
		dropTablas(db);
		createTablas(db);
	}

	public void create(SQLiteDatabase db) {
		createTablas(db);
	}

	private void createTablas(SQLiteDatabase db) {
		db.execSQL(DB.sqlMedicos);
		db.execSQL(DB.sqlPacientes);
		db.execSQL(DB.sqlPromociones);
		db.execSQL(DB.sqlResultados);
		db.execSQL(DB.sqlFiles);
	}

	private void dropTablas(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + DB.TABLA_PROMOCIONES);
		db.execSQL("DROP TABLE IF EXISTS " + DB.TABLA_PACIENTES);
		db.execSQL("DROP TABLE IF EXISTS " + DB.TABLA_MEDICOS);
		db.execSQL("DROP TABLE IF EXISTS " + DB.TABLA_FILES);
		db.execSQL("DROP TABLE IF EXISTS " + DB.TABLA_RESULTADOS);
	}
}