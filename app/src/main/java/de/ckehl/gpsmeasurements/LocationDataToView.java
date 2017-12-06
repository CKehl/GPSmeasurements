package de.ckehl.gpsmeasurements;

/**
 * Created by christian on 06/12/17.
 */
public interface LocationDataToView {
    public void isReceiving(boolean indicator);
    public void updateLongitude(float longitude);
    public void updateLatitude(float latitude);
    public void updateAltitude(float altitude);
}
