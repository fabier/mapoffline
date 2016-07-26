package fr.fabier.mapoffline.fragment;

import fr.fabier.mapoffline.R;
import android.app.Dialog;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ImportTilesProgressDialog extends DialogFragment implements OnClickListener {
	public static ImportTilesProgressDialog newInstance(int title) {
		ImportTilesProgressDialog dialog = new ImportTilesProgressDialog();
		Bundle args = new Bundle();
		args.putInt("title", title);
		dialog.setArguments(args);
		return dialog;
	}

	private Button cancelButton;
	private ProgressBar progressBar;
	private TextView dialogSubText;
	private TextView dialogText;
	private OnCancelListener cancelListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_dialog, container, false);

		this.cancelButton = (Button) v.findViewById(R.id.cancelButton);
		this.cancelButton.setOnClickListener(this);
		this.progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
		this.dialogText = (TextView) v.findViewById(R.id.dialogText);
		this.dialogSubText = (TextView) v.findViewById(R.id.dialogSubtext);

		getDialog().setTitle("Import des tuiles OSM");

		return v;
	}

	@Override
	public void onDestroyView() {
		Dialog dialog = getDialog();

		// Work around bug:
		// http://code.google.com/p/android/issues/detail?id=17423
		if ((dialog != null) && getRetainInstance())
			dialog.setDismissMessage(null);

		super.onDestroyView();
	}

	public void setMax(int max) {
		if (this.progressBar != null) {
			this.progressBar.setMax(max);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.equals(this.cancelButton)) {
			dismiss();
			if (this.cancelListener != null) {
				this.cancelListener.onCancel(getDialog());
			}
		}
	}

	public void setProgress(int progress) {
		if (this.progressBar != null) {
			this.progressBar.setProgress(progress);
		}
	}

	public void setText(String text) {
		this.dialogText.setText(text);
	}

	public void setSubtext(String text) {
		this.dialogSubText.setText(text);
	}

	public void setOnCancelListener(OnCancelListener cancelListener) {
		this.cancelListener = cancelListener;
	}

	public int getProgress() {
		return this.progressBar == null ? 0 : this.progressBar.getProgress();
	}

	public int getMax() {
		return this.progressBar == null ? 0 : this.progressBar.getMax();
	}
}
