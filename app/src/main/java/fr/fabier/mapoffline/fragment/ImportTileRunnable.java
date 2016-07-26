package fr.fabier.mapoffline.fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

import android.annotation.TargetApi;
import android.os.Build;
import fr.fabier.mapoffline.Constants;

public class ImportTileRunnable implements Callable<MapTile> {
	private MapTile mapTile;
	private File mapnik;
	private MapTileProviderBase mapTileProviderBase;
	private OnlineTileSourceBase onlineTileSourceBase;

	public ImportTileRunnable(File mapnik, OnlineTileSourceBase onlineTileSourceBase, MapTile mapTile) {
		this.mapnik = mapnik;
		this.onlineTileSourceBase = onlineTileSourceBase;
		this.mapTile = mapTile;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public MapTile call() throws Exception {
		// task.publishProgress(mapTile);
		File pngFile = new File(mapnik, String.format("%d/%d/%d.png.tile", mapTile.getZoomLevel(), mapTile.getX(), mapTile.getY()));
		if (!pngFile.exists()
				|| pngFile.lastModified() < System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(Constants.DELAY_CACHE_EXPIRATION, TimeUnit.DAYS)
				|| pngFile.length() == 0) {
			File parentFile = pngFile.getParentFile();
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}
			InputStream openStream = null;
			FileOutputStream fileOutputStream = null;
			try {
				String tileURLString = this.onlineTileSourceBase.getTileURLString(mapTile);
				URL url = new URL(tileURLString);
				fileOutputStream = new FileOutputStream(pngFile);
				openStream = url.openStream();
				IOUtils.copy(openStream, fileOutputStream);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				// Le fichier est incorrect, on le supprime.
				FileUtils.deleteQuietly(pngFile);
			} finally {
				IOUtils.closeQuietly(openStream);
				IOUtils.closeQuietly(fileOutputStream);
			}
		}
		return this.mapTile;
	}

}
