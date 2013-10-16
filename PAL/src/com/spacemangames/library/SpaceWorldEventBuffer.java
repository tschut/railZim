package com.spacemangames.library;

import java.util.LinkedList;
import java.util.Queue;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.spacemangames.framework.SpaceGameState;
import com.spacemangames.pal.PALManager;

public class SpaceWorldEventBuffer {
    // Game events
    public static final int     EVENT_HIT_ROCKET     = 0;
    public static final int     EVENT_HIT_DOI_OBJECT = 1;
    public static final int     EVENT_SCORE_BONUS    = 2;

    private static final String TAG                  = "SpaceWorldEventBuffer";

    private class SpaceContactListener implements ContactListener {
        @Override
        public void beginContact(Contact contact) {
            SpaceObject lObjectA = (SpaceObject) contact.getFixtureA().getBody().getUserData();
            SpaceObject lObjectB = (SpaceObject) contact.getFixtureB().getBody().getUserData();
            SpaceObject lSpaceMan, lOther;

            if (lObjectA.mType == SpaceObject.TYPE_SPACEMAN) {
                lSpaceMan = lObjectA;
                lOther = lObjectB;
            } else if (lObjectB.mType == SpaceObject.TYPE_SPACEMAN) {
                lSpaceMan = lObjectB;
                lOther = lObjectA;
            } else {
                PALManager.getLog().e(TAG, "Error: collision between two object detected but neither is spaceman");
                return;
            }

            if (lOther.mType == SpaceObject.TYPE_ROCKET) { // woohoo we hit the
                                                           // rocket!
                mEvents.add(EVENT_HIT_ROCKET);
            } else if (lOther.deathOnImpact()) {
                mEvents.add(EVENT_HIT_DOI_OBJECT);
            } else if (lOther.mType == SpaceObject.TYPE_BONUS && !SpaceGameState.INSTANCE.isPredicting()) {
                SpaceBonusObject lObject = (SpaceBonusObject) lOther;
                if (lObject.visible()) {
                    mEvents.add(EVENT_SCORE_BONUS);
                    // make the object invisible
                    lObject.setVisible(false);
                }
            } else {
                PALManager.getLog().w(TAG, "Warning: Unhandled collision detected");
                return;
            }
        }

        @Override
        public void endContact(Contact contact) {
        }
    }

    public Queue<Integer> mEvents;

    // private constructor
    private SpaceWorldEventBuffer() {
        mListener = new SpaceContactListener();
        mEvents = new LinkedList<Integer>();
    }

    // Singleton holder
    private static class SingletonHolder {
        public static final SpaceWorldEventBuffer INSTANCE = new SpaceWorldEventBuffer();
    }

    // Singleton access
    public static SpaceWorldEventBuffer getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private SpaceContactListener mListener;

    public SpaceContactListener getContactListener() {
        return mListener;
    }

    public boolean forceStopEventHappened() {
        boolean result = false;

        if (mEvents.contains(EVENT_HIT_ROCKET))
            result = true;
        if (mEvents.contains(EVENT_HIT_DOI_OBJECT))
            result = true;

        return result;
    }

    public void clear() {
        mEvents.clear();
    }
}
