package fr.fabier.mapoffline.fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import fr.fabier.mapoffline.Constants;
import fr.fabier.mapoffline.MainActivity;
import fr.fabier.mapoffline.R;
import fr.fabier.mapoffline.concurrent.ListenableFutureTask;
import fr.fabier.mapoffline.concurrent.ResultHandler;
import fr.fabier.mapoffline.gpx.GPX;
import fr.fabier.mapoffline.gpx.GPXParser;
import fr.fabier.mapoffline.gpx.Track;
import fr.fabier.mapoffline.gpx.TrackPoint;
import fr.fabier.mapoffline.gpx.TrackSeg;

/**
 * A placeholder fragment containing a simple view.
 */
public class MapFragment extends Fragment implements OnClickListener {
	private static final String TAG = MapFragment.class.getSimpleName();
	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	private static final int PICK_REQUEST_CODE = 0;

	private MapView mapView;
	private IMapController mapController;

	private Button importButton;

	private Button importGPXButton;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static MapFragment newInstance(int sectionNumber) {
		MapFragment fragment = new MapFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public MapFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);

		this.importButton = (Button) rootView.findViewById(R.id.importButton);
		this.importButton.setOnClickListener(this);

		this.importGPXButton = (Button) rootView.findViewById(R.id.importGPXButton);
		this.importGPXButton.setOnClickListener(this);

		this.mapView = (MapView) rootView.findViewById(R.id.mapview);
		this.mapView.setBuiltInZoomControls(true);
		this.mapView.setMultiTouchControls(true);
		// this.mapView.setUseDataConnection(false);

		this.mapController = mapView.getController();
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		int position = getArguments().getInt(ARG_SECTION_NUMBER);
		((MainActivity) activity).onSectionAttached(position);
	}

	@Override
	public void onPause() {
		super.onPause();

		this.mapView.getOverlays().clear();
	}

	@Override
	public void onResume() {
		super.onResume();
		this.mapController.setZoom(10);

		MyLocationNewOverlay myLocationNewOverlay = new MyLocationNewOverlay(getActivity(), mapView);
		myLocationNewOverlay.enableFollowLocation();
		myLocationNewOverlay.enableMyLocation();
		this.mapView.getOverlayManager().add(myLocationNewOverlay);
		// int position = getArguments().getInt(ARG_SECTION_NUMBER);
		// this.mapView.setTileSource(Constants.TILESOURCES[position]);
		//
		// ITileSource iTileSource = Constants.TILEOVERLAY[position];
		// if (iTileSource != null) {
		// final MapTileProviderBasic anotherTileProvider = new
		// MapTileProviderBasic(getActivity());
		// anotherTileProvider.setTileSource(iTileSource);
		// final TilesOverlay secondTilesOverlay = new
		// TilesOverlay(anotherTileProvider, this.getActivity());
		// secondTilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
		// this.mapView.getOverlays().add(secondTilesOverlay);
		// }
	}

	public void setTileSourceProvider(ITileSource tileSource) {
		if (this.mapView != null) {
			this.mapView.setTileSource(tileSource);
		}
	}

	public void setUseDataConnection(boolean useDataConnection) {
		if (this.mapView != null) {
			this.mapView.setUseDataConnection(useDataConnection);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.importButton:
			ImportTilesTask importTilesTask = new ImportTilesTask(this.mapView.getBoundingBox());
			importTilesTask.execute();
			break;
		case R.id.importGPXButton:
			importGPXFile();
			break;
		default:
			break;
		}
	}

	private void importGPXFile() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_PICK);
		Uri startDir = Uri.fromFile(Environment.getExternalStorageDirectory());

		intent.setDataAndType(startDir, "vnd.android.cursor.dir/lysesoft.andexplorer.file");
		intent.putExtra("browser_filter_extension_whitelist", "*.gpx");
		intent.putExtra("explorer_title", getText(R.string.andex_file_selection_title));
		intent.putExtra("browser_title_background_color", getText(R.string.browser_title_background_color));
		intent.putExtra("browser_title_foreground_color", getText(R.string.browser_title_foreground_color));
		intent.putExtra("browser_list_background_color", getText(R.string.browser_list_background_color));
		intent.putExtra("browser_list_fontscale", "120%");
		intent.putExtra("browser_list_layout", "2");

		try {
			ApplicationInfo info = getActivity().getPackageManager().getApplicationInfo("lysesoft.andexplorer", 0);
			startActivityForResult(intent, PICK_REQUEST_CODE);
		} catch (PackageManager.NameNotFoundException e) {
			showInstallResultMessage(R.string.error_install_andexplorer);
		} catch (Exception e) {
			Log.w(TAG, e.getMessage());
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case PICK_REQUEST_CODE:
			try {
				if (resultCode == Activity.RESULT_OK) {
					// Get the Uri of the selected file
					Uri uri = data.getData();
					Log.d(TAG, "File Uri: " + uri.toString());
					// Get the path
					String path = getPath(getActivity(), uri);
					Log.d(TAG, "File Path: " + path);
					// Get the file instance

					File file = new File(path);
					if (file.isFile() && file.getName().endsWith(".gpx")) {
						// Initiate the upload
						ImportTilesFromGPXTask importTilesFromGPXTask = new ImportTilesFromGPXTask(file);
						importTilesFromGPXTask.execute();
					}
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			break;
		}
	}

	public static String getPath(Context context, Uri uri) throws URISyntaxException {
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = { "_data" };
			Cursor cursor = null;

			try {
				cursor = context.getContentResolver().query(uri, projection, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow("_data");
				if (cursor.moveToFirst()) {
					return cursor.getString(column_index);
				}
			} catch (Exception e) {
				// Eat it
			}
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	private void showInstallResultMessage(int msg_id) {
		AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
		dialog.setMessage(getText(msg_id));
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.button_install), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("market://details?id=lysesoft.andexplorer"));
				startActivity(intent);
				dialog.dismiss();
			}
		});
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getText(R.string.button_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	public int[] getTileNumber(final double lat, final double lon, final int zoom) {
		int xtile = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
		int ytile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));
		if (xtile < 0) {
			xtile = 0;
		}
		if (xtile >= (1 << zoom)) {
			xtile = ((1 << zoom) - 1);
		}
		if (ytile < 0) {
			ytile = 0;
		}
		if (ytile >= (1 << zoom)) {
			ytile = ((1 << zoom) - 1);
		}
		return new int[] { xtile, ytile };
	}

	public int[] getSquareAroundTileNumbers2x2(final double lat, final double lon, final int zoom) {
		return getSquareAroundTileNumbers(lat, lon, zoom, 2);
	}

	public int[] getSquareAroundTileNumbers3x3(final double lat, final double lon, final int zoom) {
		return getSquareAroundTileNumbers(lat, lon, zoom, 3);
	}

	public int[] getSquareAroundTileNumbers4x4(final double lat, final double lon, final int zoom) {
		return getSquareAroundTileNumbers(lat, lon, zoom, 4);
	}

	public int[] getSquareAroundTileNumbers(final double lat, final double lon, final int zoom, int squareSize) {
		double deltaPosition = squareSize / 2.0;

		double xTileAsDouble = (lon + 180) / 360 * (1 << zoom);
		int xtileMin = (int) Math.floor(xTileAsDouble - deltaPosition);
		int xtileMax = xtileMin + squareSize;

		double yTileAsDouble = (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom);
		int ytileMin = (int) Math.floor(yTileAsDouble - deltaPosition);
		int ytileMax = ytileMin + squareSize;

		if (xtileMin < 0) {
			xtileMin = 0;
		}
		if (xtileMax < 0) {
			xtileMax = 0;
		}
		if (xtileMin >= (1 << zoom)) {
			xtileMin = ((1 << zoom) - 1);
		}
		if (xtileMax >= (1 << zoom)) {
			xtileMax = ((1 << zoom) - 1);
		}

		if (ytileMin < 0) {
			ytileMin = 0;
		}
		if (ytileMax < 0) {
			ytileMax = 0;
		}
		if (ytileMin >= (1 << zoom)) {
			ytileMin = ((1 << zoom) - 1);
		}
		if (ytileMax >= (1 << zoom)) {
			ytileMax = ((1 << zoom) - 1);
		}
		return new int[] { xtileMin, ytileMin, xtileMax, ytileMax };
	}

	class ImportTilesTask extends AsyncTask<Void, MapTile, Void> implements ResultHandler<MapTile>, OnCancelListener {

		private ImportTilesProgressDialog dialogFragment;
		private BoundingBoxE6 boundingBox;
		private File osmTilesRootDirectory;
		private ExecutorService threadPool;

		public ImportTilesTask(BoundingBoxE6 boundingBox) {
			this.boundingBox = boundingBox;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			this.threadPool = Executors.newFixedThreadPool(Constants.MAX_THREAD_COUNT);

			// Importer la carto sur l'espace visible.
			File externalStorageDirectory = Environment.getExternalStorageDirectory();
			File osmdroid = new File(externalStorageDirectory, "osmdroid");
			File tiles = new File(osmdroid, "tiles");
			this.osmTilesRootDirectory = new File(tiles, mapView.getTileProvider().getTileSource().name());
			this.dialogFragment = new ImportTilesProgressDialog();
			this.dialogFragment.setOnCancelListener(this);
			this.dialogFragment.show(getFragmentManager(), "dialogFragment");
		}

		@TargetApi(Build.VERSION_CODES.GINGERBREAD)
		@Override
		protected Void doInBackground(Void... params) {
			int tileCount = 0;
			ITileSource tileSource = mapView.getTileProvider().getTileSource();
			if (tileSource instanceof OnlineTileSourceBase) {
				OnlineTileSourceBase onlineTileSourceBase = (OnlineTileSourceBase) tileSource;
				try {
					for (int zoom = onlineTileSourceBase.getMinimumZoomLevel(); zoom <= onlineTileSourceBase.getMaximumZoomLevel(); zoom++) {
						int[] northWestTileXY = getTileNumber(boundingBox.getLatNorthE6() / 1E6, boundingBox.getLonWestE6() / 1E6, zoom);
						int[] southEastTileXY = getTileNumber(boundingBox.getLatSouthE6() / 1E6, boundingBox.getLonEastE6() / 1E6, zoom);
						int minX = northWestTileXY[0];
						int maxX = southEastTileXY[0];
						int minY = northWestTileXY[1];
						int maxY = southEastTileXY[1];
						int tileCountForThisLevel = (maxX - minX + 1) * (maxY - minY + 1);
						if (tileCount + tileCountForThisLevel < Constants.MAX_TILES) {
							tileCount += tileCountForThisLevel;
							this.dialogFragment.setMax(tileCount);
							for (int x = minX; x <= maxX; x++) {
								for (int y = minY; y <= maxY; y++) {
									MapTile mapTile = new MapTile(zoom, x, y);
									threadPool.submit(new ListenableFutureTask<MapTile>(new ImportTileRunnable(osmTilesRootDirectory, onlineTileSourceBase,
											mapTile), this));
								}
							}
						}
					}
				} catch (RejectedExecutionException r) {
				}
			}

			try {
				threadPool.shutdown();
				threadPool.awaitTermination(60, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
			}
			return null;
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			if (this.threadPool != null) {
				this.threadPool.shutdownNow();
			}
		}

		@Override
		public void taskCompleted(Future<MapTile> result) {
			try {
				publishProgress(result.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onProgressUpdate(MapTile... values) {
			super.onProgressUpdate(values);
			int progress = this.dialogFragment.getProgress();
			progress += values.length;
			this.dialogFragment.setProgress(progress);
			String sProgress = String.format(Locale.ENGLISH, "Tuile %1$d/%2$d - %3$2.1f %%", this.dialogFragment.getProgress(), this.dialogFragment.getMax(),
					100.0 * (double) this.dialogFragment.getProgress() / this.dialogFragment.getMax());
			this.dialogFragment.setText(sProgress);
			MapTile mapTile = values[values.length - 1];
			this.dialogFragment.setSubtext(String.format("%d/%d/%d.png", mapTile.getZoomLevel(), mapTile.getX(), mapTile.getY()));
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (dialogFragment != null) {
				dialogFragment.dismiss();
			}
		}
	}

	class ImportTilesFromGPXTask extends AsyncTask<Void, MapTile, Void> implements ResultHandler<MapTile>, OnCancelListener {
		// private static final String GPXFILE =
		// "/storage/emulated/0/Download/danube-cycle-route-passau-to-vienna.gpx";
		private ImportTilesProgressDialog dialogFragment;
		private ExecutorService threadPool;
		private File osmTilesRootDirectory;
		private int maxZoomLevel;
		private File gpxFile;

		public ImportTilesFromGPXTask(File gpxFile) {
			this.gpxFile = gpxFile;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			this.threadPool = Executors.newFixedThreadPool(Constants.MAX_THREAD_COUNT);

			// Importer la carto sur l'espace visible.
			File externalStorageDirectory = Environment.getExternalStorageDirectory();
			File osmdroid = new File(externalStorageDirectory, "osmdroid");
			File tiles = new File(osmdroid, "tiles");
			this.osmTilesRootDirectory = new File(tiles, mapView.getTileProvider().getTileSource().name());
			this.dialogFragment = new ImportTilesProgressDialog();
			this.dialogFragment.setOnCancelListener(this);
			this.dialogFragment.show(getFragmentManager(), "dialogFragment");
		}

		@TargetApi(Build.VERSION_CODES.GINGERBREAD)
		@Override
		protected Void doInBackground(Void... params) {
			GPXParser gpxParser = new GPXParser();
			FileInputStream fileInputStream = null;
			this.maxZoomLevel = 0;
			ITileSource tileSource = mapView.getTileProvider().getTileSource();
			if (tileSource instanceof OnlineTileSourceBase) {
				OnlineTileSourceBase onlineTileSourceBase = (OnlineTileSourceBase) tileSource;
				try {
					fileInputStream = new FileInputStream(this.gpxFile);
					GPX gpx = gpxParser.parse(fileInputStream);
					List<Track> tracks = gpx.getTracks();
					int tileCount = 0;
					for (Track track : tracks) {
						List<TrackSeg> trackSegs = track.getTrackSegs();
						for (TrackSeg trackSeg : trackSegs) {
							List<TrackPoint> trackPoints = trackSeg.getTrackPoints();
							for (int zoom = onlineTileSourceBase.getMinimumZoomLevel(); zoom <= onlineTileSourceBase.getMaximumZoomLevel(); zoom++) {
								int[] previousTileXY = null;

								Set<MapTile> mapTilesForThisZoomLevel = new HashSet<>();
								for (TrackPoint trackPoint : trackPoints) {

									int[] currentTileXY = getSquareAroundTileNumbers4x4(trackPoint.getLatitude(), trackPoint.getLongitude(), zoom);

									if (previousTileXY != null) {
										int minX = Math.min(currentTileXY[0], previousTileXY[0]);
										int maxX = Math.max(currentTileXY[2], previousTileXY[2]);
										int minY = Math.min(currentTileXY[1], previousTileXY[1]);
										int maxY = Math.max(currentTileXY[3], previousTileXY[3]);
										for (int x = minX; x <= maxX; x++) {
											for (int y = minY; y <= maxY; y++) {
												MapTile mapTile = new MapTile(zoom, x, y);
												mapTilesForThisZoomLevel.add(mapTile);
											}
										}
									}
									previousTileXY = currentTileXY;
								}

								if (!mapTilesForThisZoomLevel.isEmpty()) {
									this.maxZoomLevel = zoom;
									int tileCountForThisLevel = mapTilesForThisZoomLevel.size();
									if (tileCount + tileCountForThisLevel < Constants.MAX_TILES) {
										tileCount += tileCountForThisLevel;
										this.dialogFragment.setMax(tileCount);
										for (MapTile mapTile : mapTilesForThisZoomLevel) {
											threadPool.submit(new ListenableFutureTask<MapTile>(new ImportTileRunnable(osmTilesRootDirectory,
													onlineTileSourceBase, mapTile), this));
										}
									}
								}
							}
						}
					}
				} catch (RejectedExecutionException r) {
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} finally {
					IOUtils.closeQuietly(fileInputStream);
				}
			}

			try {
				threadPool.shutdown();
				threadPool.awaitTermination(60, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
			}
			return null;
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			if (this.threadPool != null) {
				this.threadPool.shutdownNow();
			}
		}

		@Override
		public void taskCompleted(Future<MapTile> result) {
			try {
				publishProgress(result.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onProgressUpdate(MapTile... values) {
			super.onProgressUpdate(values);
			int progress = this.dialogFragment.getProgress();
			progress += values.length;
			this.dialogFragment.setProgress(progress);
			String sProgress = String.format(Locale.ENGLISH, "Tuile %1$d/%2$d - %3$2.1f %%", this.dialogFragment.getProgress(), this.dialogFragment.getMax(),
					100.0 * (double) this.dialogFragment.getProgress() / this.dialogFragment.getMax());
			this.dialogFragment.setText(sProgress);
			MapTile mapTile = values[values.length - 1];
			this.dialogFragment.setSubtext(String.format("%d/%d/%d.png", mapTile.getZoomLevel(), mapTile.getX(), mapTile.getY()));
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (dialogFragment != null && dialogFragment.isVisible()) {
				dialogFragment.dismiss();
			}
		}
	}
}