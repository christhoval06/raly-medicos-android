package raly.medicos.utilidades;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.Uri;

import java.io.File;
import java.util.List;

import raly.medicos.app.R;

public class Utils {

	private Context app;

	public Utils(Context _app) {
		app = _app;
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected())
			return true;
		else
			return false;
	}

	public void openPDF(final Activity actividad, String file, String mineType) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(file)), mineType);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			actividad.startActivity(intent);
			actividad.overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
		} catch (ActivityNotFoundException e) {
			new Dialogs(actividad).notification("Advertencia", "No tiene un App para ver PDFs\nQuiere descargar Adobe Reader?", "ir a PlayStore",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							actividad.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.adobe.reader")));
							actividad.overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
						}
					});
		}
	}

	public boolean isAvailable(Context ctx, Intent intent) {
		final PackageManager mgr = ctx.getPackageManager();
		List<ResolveInfo> list = mgr.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
}
