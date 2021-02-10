package co.za.gmapssolutions.beatraffic.services;

import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import co.za.gmapssolutions.beatraffic.R;
import com.google.android.gms.location.DetectedActivity;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

public class MyLocation extends Overlay {
    protected Paint mPaint = new Paint();
    protected Paint mCirclePaint = new Paint();
    private final Point mDrawPixel = new Point();
    protected Bitmap mPersonBitmap;
    protected Bitmap mDirectionArrowBitmap;
    protected final PointF mPersonHotspot;
    protected final float mScale;

    protected float mDirectionArrowCenterX;
    protected float mDirectionArrowCenterY;
    private Location myLocation;
    private final MapView map;
    private final GeoPoint mGeoPoint = new GeoPoint(0d, 0d);
    private int userActivity = -1 , userConfidence = -1;
    public MyLocation(MapView map){
        super();
        this.map = map;
        mScale = map.getContext().getResources().getDisplayMetrics().density;
        mPersonHotspot = new PointF(24.0f * mScale + 0.5f, 39.0f * mScale + 0.5f);

        mCirclePaint.setARGB(0, 100, 100, 255);
        mCirclePaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);

        setDirectionArrow(((BitmapDrawable)map.getContext().getDrawable(R.drawable.person)).getBitmap(),
                ((BitmapDrawable)map.getContext().getDrawable(R.drawable.round_navigation_white_48)).getBitmap());

    }
    private void setDirectionArrow(final Bitmap personBitmap, final Bitmap directionArrowBitmap){
        this.mPersonBitmap = personBitmap;
        this.mDirectionArrowBitmap=directionArrowBitmap;
        mDirectionArrowCenterX = mDirectionArrowBitmap.getWidth() / 2.0f - 0.5f;
        mDirectionArrowCenterY = mDirectionArrowBitmap.getHeight() / 2.0f - 0.5f;
    }
    public void setMyLocation(Location myLocation){
        this.myLocation = myLocation;
        mGeoPoint.setCoords(this.myLocation.getLatitude(), this.myLocation.getLongitude());
    }
    public void setUserActivity(int userActivity){
        this.userActivity = userActivity;
    }
    public void setUserConfidence(int userConfidence){
        this.userConfidence = userConfidence;
    }
    protected void drawMyLocation(final Canvas canvas, final Projection pj,  final Location lastFix) {
        pj.toPixels(mGeoPoint, mDrawPixel);

        //if (mDrawAccuracyEnabled) {
            final float radius = lastFix.getAccuracy()
                    / (float) TileSystem.GroundResolution(lastFix.getLatitude(),
                    pj.getZoomLevel());

            mCirclePaint.setAlpha(50);
            mCirclePaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(mDrawPixel.x, mDrawPixel.y, radius, mCirclePaint);

            mCirclePaint.setAlpha(150);
            mCirclePaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(mDrawPixel.x, mDrawPixel.y, radius, mCirclePaint);
        //}
        if (lastFix.hasBearing() && userActivity == DetectedActivity.IN_VEHICLE ){//&& userConfidence > 70
            canvas.save();
            // Rotate the icon if we have a GPS fix, take into account if the map is already rotated
            float mapRotation;
            mapRotation=lastFix.getBearing();
            if (mapRotation >=360.0f)
                mapRotation=mapRotation-360f;
            canvas.rotate(mapRotation, mDrawPixel.x, mDrawPixel.y);
            // Draw the bitmap
            canvas.drawBitmap(mDirectionArrowBitmap, mDrawPixel.x
                            - mDirectionArrowCenterX, mDrawPixel.y - mDirectionArrowCenterY,
                    mPaint);
            canvas.restore();
        } else {
            canvas.save();
            // Unrotate the icon if the maps are rotated so the little man stays upright
            canvas.rotate(-map.getMapOrientation(), mDrawPixel.x,
                    mDrawPixel.y);
            // Draw the bitmap
            canvas.drawBitmap(mPersonBitmap, mDrawPixel.x - mPersonHotspot.x,
                    mDrawPixel.y - mPersonHotspot.y, mPaint);
            canvas.restore();
        }
    }
    @Override
    public void draw(Canvas c, Projection pProjection) {
        if(myLocation != null)
        drawMyLocation(c,pProjection,myLocation);
    }
    public GeoPoint getLastKnownLocation() {
        return mGeoPoint;
    }

}
