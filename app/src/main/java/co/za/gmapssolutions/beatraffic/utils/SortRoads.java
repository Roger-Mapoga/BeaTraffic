package co.za.gmapssolutions.beatraffic.utils;

import org.osmdroid.bonuspack.routing.Road;

import java.util.Comparator;

public class SortRoads implements Comparator<Road> {
    public int compare(Road a, Road b)
    {
        return (int) (Math.round(a.mLength) - Math.round(b.mLength));
    }
}
