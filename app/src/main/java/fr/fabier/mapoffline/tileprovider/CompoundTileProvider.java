package fr.fabier.mapoffline.tileprovider;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

public class CompoundTileProvider extends BitmapTileSourceBase implements ITileSource {

	private ITileSource iTileSource1;
	private ITileSource iTileSource2;

	public CompoundTileProvider(ITileSource iTileSource1, ITileSource iTileSource2) {
		super(iTileSource1.name() + "-" + iTileSource2.name(), //
				ResourceProxy.string.base, //
				Math.max(iTileSource1.getMinimumZoomLevel(), iTileSource2.getMinimumZoomLevel()), //
				Math.min(iTileSource1.getMaximumZoomLevel(), iTileSource2.getMaximumZoomLevel()), //
				iTileSource1.getTileSizePixels(), //
				".png");
		this.iTileSource1 = iTileSource1;
		this.iTileSource2 = iTileSource2;
		if (this.iTileSource1.getTileSizePixels() != this.iTileSource2.getTileSizePixels()) {
			throw new IllegalArgumentException("tileSizePixels does not match for iTileSource1 and iTileSource2");
		}
	}

	@Override
	public Drawable getDrawable(String aFilePath) {
		Drawable[] layers = new Drawable[2];
		Drawable drawable = null;
		try {
			Drawable drawable1 = iTileSource1.getDrawable(aFilePath.replace(this.name(), iTileSource1.name()));
			Drawable drawable2 = iTileSource2.getDrawable(aFilePath.replace(this.name(), iTileSource2.name()));
			layers[0] = drawable1;
			layers[1] = drawable2;
			drawable = new LayerDrawable(layers);
		} catch (LowMemoryException e) {
			e.printStackTrace();
		}
		return drawable;
	}
}
