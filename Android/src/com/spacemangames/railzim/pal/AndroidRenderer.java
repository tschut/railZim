package com.spacemangames.railzim.pal;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.spacemangames.framework.SpaceUtil;
import com.spacemangames.library.SpaceBackgroundObject;
import com.spacemangames.library.SpaceObject;
import com.spacemangames.math.Rect;
import com.spacemangames.pal.IRenderer;

public class AndroidRenderer implements IRenderer {
    private boolean initialized = false;

    private Canvas  canvas;
    private Rect    viewport;
    private Rect    screen;

    // initialize platform stuff
    public void initialize(Canvas canvas, Rect viewport, Rect screen) {
        this.canvas = canvas;

        this.viewport = viewport;
        this.screen = screen;
        initialized = true;
    }

    @Override
    public void doDraw(List<SpaceObject> objects, SpaceBackgroundObject backgroundObject) {
        assert !initialized;

        doDraw(backgroundObject);

        int count = objects.size();
        for (int i = 0; i < count; ++i) {
            objects.get(i).dispatchToRenderer(this);
        }

        initialized = false;
    }

    @Override
    public void doDraw(SpaceBackgroundObject object) {
        canvas.drawColor(Color.GREEN);
    }

    @Override
    public void doDraw(SpaceObject object) {
        AndroidBitmap bitmap = (AndroidBitmap) object.getBitmap();
        Drawable drawable = bitmap.getDrawable();

        float x = SpaceUtil.transformX(viewport, screen, object.position.x);
        float y = SpaceUtil.transformY(viewport, screen, object.position.y);
        float w = SpaceUtil.scaleX(viewport, screen, bitmap.getWidth());
        float h = SpaceUtil.scaleY(viewport, screen, bitmap.getHeight());

        // Draw object
        int yTop = (int) (y - (h / 2.0f));
        int xLeft = (int) (x - (w / 2.0f));
        drawable.setBounds(xLeft, yTop, (int) (xLeft + w), (int) (yTop + h));
        drawable.draw(canvas);
    }
}
