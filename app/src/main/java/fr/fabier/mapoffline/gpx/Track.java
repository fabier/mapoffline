package fr.fabier.mapoffline.gpx;

import java.util.ArrayList;
import java.util.List;

public class Track {
	private String description;
	private List<TrackSeg> trackSegs;

	public Track() {
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<TrackSeg> getTrackSegs() {
		return trackSegs;
	}

	public void setTrackSegs(List<TrackSeg> trackSegs) {
		this.trackSegs = trackSegs;
	}

	public void add(TrackSeg trackSeg) {
		if (this.trackSegs == null) {
			this.trackSegs = new ArrayList<>();
		}
		this.trackSegs.add(trackSeg);
	}
}
