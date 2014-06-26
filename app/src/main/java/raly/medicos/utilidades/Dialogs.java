package raly.medicos.utilidades;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.text.Spanned;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.TextView;

import raly.medicos.app.R;

public class Dialogs {
	private Activity app = null;

	public Dialogs(Activity app) {
		this.app = app;
	}

	public void alert(String... data) {
		CharSequence titulo = data[0], msg = data[1];
		final CharSequence action = data[2];
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(app);
		alertDialog.setTitle(titulo);
		alertDialog.setMessage(msg);
		alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				if (action != null) {
					app.finish();
				}
			}
		});
	}

	public void alert(Spanned _titulo, Spanned _msg) {
		Builder builder = new AlertDialog.Builder(app);
		TextView title = new TextView(app);
		title.setText(_titulo);
		title.setPadding(10, 10, 10, 10);
		title.setGravity(Gravity.CENTER);
		title.setTextSize(23);
		builder.setCustomTitle(title);
		TextView msg = new TextView(app);
		msg.setText(_msg);
		msg.setPadding(10, 10, 10, 10);
		msg.setGravity(Gravity.CENTER);
		msg.setTextSize(18);
		builder.setView(msg);
		builder.setCancelable(true);
		builder.setPositiveButton(app.getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void alert(String titulo, String msg, DialogInterface.OnClickListener callback) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(app);
		alertDialog.setTitle(titulo);
		alertDialog.setMessage(msg);
		alertDialog.setPositiveButton("OK", callback);
		alertDialog.show();
	}

	public void notification(String titulo, String msg, DialogInterface.OnClickListener okcallback) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(app);
		alertDialog.setTitle(titulo);
		alertDialog.setMessage(msg);
		alertDialog.setPositiveButton("Aceptar", okcallback).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
		});
		alertDialog.show();
	}

	public void notification(String titulo, String msg, String okbtn, DialogInterface.OnClickListener okcallback) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(app);
		alertDialog.setTitle(titulo);
		alertDialog.setMessage(msg);
		alertDialog.setPositiveButton(okbtn, okcallback).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
		});
		alertDialog.show();
	}

	public void notification(String titulo, String msg, String okbtn, String cancelbtn, DialogInterface.OnClickListener okcallback,
			DialogInterface.OnClickListener cancelcallback) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(app);
		alertDialog.setTitle(titulo);
		alertDialog.setMessage(msg);
		alertDialog.setPositiveButton(okbtn, okcallback).setNegativeButton(cancelbtn, cancelcallback);
		alertDialog.show();
	}

	public void notification(Spanned _titulo, Spanned _msg, String okbtn, DialogInterface.OnClickListener okcallback) {
		Builder builder = new AlertDialog.Builder(app);
		TextView title = new TextView(app);
		title.setText(_titulo);
		title.setPadding(10, 10, 10, 10);
		title.setGravity(Gravity.CENTER);
		title.setTextSize(23);
		builder.setCustomTitle(title);
		TextView msg = new TextView(app);
		msg.setText(_msg);
		msg.setPadding(10, 10, 10, 10);
		msg.setGravity(Gravity.CENTER);
		msg.setTextSize(18);
		builder.setView(msg);
		builder.setCancelable(true);
		builder.setPositiveButton(okbtn, okcallback).setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public AlertDialog.Builder input(DialogInterface.OnClickListener callback, String... data) {
		final EditText input = new EditText(app);
		String titulo = data[0], btnOk = data[1], btnCancel = data[2];
		input.setPadding(10, 10, 10, 10);
		input.setText("");
		AlertDialog.Builder inputDialog = new AlertDialog.Builder(app).setTitle(titulo).setView(input).setPositiveButton(btnOk, callback)
				.setNegativeButton(btnCancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				});
		return inputDialog;
	}
}
