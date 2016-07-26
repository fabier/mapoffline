package fr.fabier.mapoffline.gpx;

import java.util.ArrayList;
import java.util.List;

public class TrackSeg {
	private List<TrackPoint> trackPoints;

	public TrackSeg() {
	}

	public List<TrackPoint> getTrackPoints() {
		return trackPoints;
	}

	public void setTrackPoints(List<TrackPoint> trackPoints) {
		this.trackPoints = trackPoints;
	}

	public void add(TrackPoint trackPoint) {
		if (this.trackPoints == null) {
			this.trackPoints = new ArrayList<>();
		}
		this.trackPoints.add(trackPoint);
	}
}
