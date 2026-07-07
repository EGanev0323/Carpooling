package bg.tu_sofia.carpooling.rides.service;

import bg.tu_sofia.carpooling.geo.domain.City;

import java.util.List;

/**
 * Implements Google Encoded Polyline Algorithm Format.
 * See: https://developers.google.com/maps/documentation/utilities/polylinealgorithm
 */
public class PolylineEncoder {

    private PolylineEncoder() {}

    public static String encodePoints(List<double[]> points) {
        StringBuilder result = new StringBuilder();
        int prevLat = 0, prevLng = 0;
        for (double[] point : points) {
            int lat = (int) Math.round(point[0] * 1e5);
            int lng = (int) Math.round(point[1] * 1e5);
            result.append(encodeValue(lat - prevLat));
            result.append(encodeValue(lng - prevLng));
            prevLat = lat;
            prevLng = lng;
        }
        return result.toString();
    }

    private static String encodeValue(int value) {
        value = value < 0 ? ~(value << 1) : (value << 1);
        StringBuilder chunk = new StringBuilder();
        while (value >= 0x20) {
            chunk.append((char) ((0x20 | (value & 0x1f)) + 63));
            value >>= 5;
        }
        chunk.append((char) (value + 63));
        return chunk.toString();
    }

    public static String encode(City origin, City destination) {
        double[] from = {
            origin.getLatitude().doubleValue(),
            origin.getLongitude().doubleValue()
        };
        double[] to = {
            destination.getLatitude().doubleValue(),
            destination.getLongitude().doubleValue()
        };
        return encodePoints(List.of(from, to));
    }
}
