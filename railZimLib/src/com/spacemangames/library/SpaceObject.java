package com.spacemangames.library;

import com.spacemangames.framework.IMoveProperties;
import com.spacemangames.math.PointF;
import com.spacemangames.math.Rect;
import com.spacemangames.pal.IBitmap;
import com.spacemangames.pal.IRenderer;
import com.spacemangames.pal.PALManager;

public abstract class SpaceObject {
    private IBitmap           bitmap;

    public ObjectType         type;

    private PointF            startPosition = new PointF();
    public PointF             position      = new PointF();

    private Rect              rect;

    protected IMoveProperties move;

    public SpaceObject(String bitmap, boolean lazyLoading, ObjectType type,
            PointF startPosition, int collisionSize,
            IMoveProperties moveProperties) {
        if (bitmap != null) {
            this.bitmap = PALManager.getBitmapFactory().createBitmap(bitmap,
                    lazyLoading);
        }
        this.type = type;
        this.startPosition.set(startPosition);
        move = moveProperties;

        rect = new Rect();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Type:           " + type.toString() + "\n");
        builder.append("X:              " + startPosition.x + "\n");
        builder.append("Y:              " + startPosition.y + "\n");
        builder.append("Bitmap:         " + bitmap.getName() + "\n");

        return builder.toString();
    }

    public boolean deathOnImpact() {
        return false;
    }

    public void reset() {
        position.set(startPosition);
        move.reset();
    }

    public void dispatchToRenderer(IRenderer renderer) {
        renderer.doDraw(this);
    }

    public Rect getRect() {
        rect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        rect.offset(position);
        return rect;
    }

    public IBitmap getBitmap() {
        return bitmap;
    }

    public void releaseLazyMemory() {
        bitmap.releaseLazyMemory();
    }

    public PointF getPosition() {
        return position;
    }
}
