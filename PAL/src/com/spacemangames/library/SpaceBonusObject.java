package com.spacemangames.library;

import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.spacemangames.framework.IMoveProperties;
import com.spacemangames.pal.IRenderer;

public class SpaceBonusObject extends SpaceObject {
    private boolean mVisible = true;

    public SpaceBonusObject(String aBitmap, int aX, int aY, int aCollisionSize, IMoveProperties moveProperties) {
        super(aBitmap, false, ObjectType.BONUS, aX, aY, aCollisionSize, moveProperties);
    }

    @Override
    public void reset() {
        super.reset();
        mVisible = true;
    }

    @Override
    public void dispatchToRenderer(IRenderer aRenderer) {
        if (!mVisible)
            return;

        aRenderer.doDraw((SpaceObject) this);
    }

    public boolean visible() {
        return mVisible;
    }

    public void setVisible(boolean aVisible) {
        mVisible = aVisible;
    }

    @Override
    public FixtureDef createFixtureDef(Shape sd) {
        FixtureDef fdef = new FixtureDef();
        fdef.shape = sd;
        fdef.isSensor = true;

        return fdef;
    }
}
