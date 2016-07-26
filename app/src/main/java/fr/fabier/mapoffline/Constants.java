package fr.fabier.mapoffline;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

import fr.fabier.mapoffline.tileprovider.CompoundTileProvider;

public interface Constants {

	public static final ITileSource HIKEBIKE = new XYTileSource("HikeBike", null, 0, 18, 256, ".png", new String[] { "http://toolserver.org/tiles/hikebike/" });
	public static final ITileSource HIKEBIKE_TERRAIN = new XYTileSource("HikeBike", null, 0, 18, 256, ".png",
			new String[] { "http://toolserver.org/tiles/hikebike/" });
	public static final ITileSource HILLS = new XYTileSource("Hills", null, 0, 18, 256, ".png", new String[] { "http://toolserver.org/~cmarqu/hill/" });

	public static final OnlineTileSourceBase TOPO = new XYTileSource("Topo", ResourceProxy.string.topo, 0, 18, 256, ".png",
			new String[] { "http://topo2.wanderreitkarte.de/topo" });

	public static final ITileSource COMPOUNDTILEP = new CompoundTileProvider(HIKEBIKE, HILLS);

	public static final ITileSource[] TILESOURCES = new ITileSource[] { TileSourceFactory.MAPNIK, TileSourceFactory.CYCLEMAP, HIKEBIKE,
			TileSourceFactory.MAPQUESTAERIAL, TileSourceFactory.MAPQUESTOSM };
	public static final ITileSource[] TILEOVERLAY = new ITileSource[TILESOURCES.length];

	public static final int MAX_THREAD_COUNT = 16;
	public static final int DELAY_CACHE_EXPIRATION = 7;
	public static final int MAX_TILES = 10 * 1024;
}
