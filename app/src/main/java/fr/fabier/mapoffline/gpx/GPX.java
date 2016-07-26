package fr.fabier.mapoffline.gpx;

import java.util.ArrayList;
import java.util.List;

public class GPX {
	private List<Track> tracks;

	public GPX() {
	}

	public List<Track> getTracks() {
		return tracks;
	}

	public void setTracks(List<Track> tracks) {
		this.tracks = tracks;
	}

	public void add(Track track) {
		if (this.tracks == null) {
			this.tracks = new ArrayList<>();
		}
		this.tracks.add(track);
	}
}
