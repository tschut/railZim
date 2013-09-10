package com.games.spaceman.PAL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.games.spaceman.R;
import com.games.spaceman.SpaceApp;
import com.games.spaceman.PAL.LevelsJson.LevelJson;
import com.games.spaceman.PAL.LevelsJson.LevelsJson;
import com.games.spaceman.PAL.LevelsJson.ObjectJson;
import com.google.gson.Gson;
import com.spacemangames.framework.MoveProperties;
import com.spacemangames.library.SpaceData;
import com.spacemangames.library.SpaceLevel;
import com.spacemangames.pal.IResourceHandler;
import com.spacemangames.pal.PALManager;

public class AndroidResourceHandler implements IResourceHandler {
    private static final String TAG = "AndroidResourceHandler";

    public void preloadAllLevels(ArrayList<SpaceLevel> aLevels) {
        LevelsJson levelsJson = getLevelsJson(R.raw.defaultlevels);
        preloadLevels(aLevels, levelsJson);

        LevelsJson specials = getLevelsJson(R.raw.speciallevels);
        preloadLevels(SpaceData.getInstance().mSpecialLevels, specials);
    }

    public LevelsJson getLevelsJson(int resource) {
        String json = getJson(resource);
        Gson gson = new Gson();
        return gson.fromJson(json, LevelsJson.class);
    }

    private String getJson(int resource) {
        InputStream is = SpaceApp.mAppContext.getResources().openRawResource(resource);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (IOException e) {
            throw new RuntimeException();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }

        return writer.toString();
    }

    public void preloadLevels(List<SpaceLevel> levels, LevelsJson levelsJson) {
        for (LevelJson levelJson : levelsJson.getLevels()) {
            SpaceLevel level = new SpaceLevel();
            level.mId = levelJson.getId();
            level.mName = levelJson.getName();
            level.setStartCenterX(levelJson.getStartCenterX());
            level.setStartCenterY(levelJson.getStartCenterY());
            level.setPredictionBitmap(levelJson.getPredictionBitmap());
            level.setSilver(levelJson.getSilver());
            level.setGold(levelJson.getGold());

            level.addBackground(levelJson.getBackground().getColor_inner(), levelJson.getBackground().getColor_outer());
            for (ObjectJson objectJson : levelJson.getObjects()) {
                MoveProperties moveProperties = new MoveProperties();
                moveProperties.mMove = objectJson.getMove();
                moveProperties.mDPS = objectJson.getMoveDps();
                moveProperties.mOffset = objectJson.getMoveOffset();
                moveProperties.mRadius = objectJson.getMoveRadius();
                String type = objectJson.getType();
                int lX = objectJson.getPosx();
                int lY = objectJson.getPosy();
                String lBitmap = objectJson.getBitmap();
                boolean lazyLoading = objectJson.getLazyLoading();
                String lArrowBitmap = objectJson.getArrowBitmap();
                int lCollisionSize = objectJson.getCollisionSize();
                float lGrav = objectJson.getGravity();
                boolean lDOI = objectJson.getDeathOnImpact();

                if (type.equals("spaceman"))
                    level.addSpaceMan(lX, lY, lBitmap, lArrowBitmap, lCollisionSize, moveProperties);
                else if (type.equals("planet"))
                    level.addPlanet(lX, lY, lBitmap, lazyLoading, lGrav, lCollisionSize, lDOI, moveProperties);
                else if (type.equals("rocket"))
                    level.addRocket(lX, lY, lBitmap, lCollisionSize, moveProperties);
                else if (type.equals("bonus"))
                    level.addBonus(lX, lY, lBitmap, lCollisionSize, moveProperties);
                else
                    PALManager.getLog().e(TAG, "Unexpected object type: " + type);
            }
            levels.add(level.mId, level);
        }
    }

    // public void preloadLevels(ArrayList<SpaceLevel> aLevels, int aXmlResourceID) {
    // Resources lRes = SpaceApp.mAppContext.getResources();
    // XmlResourceParser lParser = lRes.getXml(aXmlResourceID);
    // boolean lInLevelTag = false;
    // String lTag;
    // SpaceLevel lLevel = null;
    //
    // try {
    // int lEventType = lParser.getEventType();
    // while (lEventType != XmlResourceParser.END_DOCUMENT) {
    // switch (lEventType) {
    // case XmlResourceParser.START_TAG:
    // lTag = lParser.getName();
    // if (lTag.equals("level")) {
    // if (lInLevelTag) {
    // PALManager.getLog()
    // .e(TAG, "Error parsing XML: found level start tag while still in previous level tag");
    // } else {
    // // create a new level object
    // lLevel = new SpaceLevel();
    // lInLevelTag = true;
    // }
    // for (int i = 0; i < lParser.getAttributeCount(); i++) {
    // String lAtt = lParser.getAttributeName(i);
    // if (lAtt.equals("id"))
    // lLevel.mId = lParser.getAttributeIntValue(i, 0);
    // else if (lAtt.equals("name"))
    // lLevel.mName = lParser.getAttributeValue(i);
    // else if (lAtt.equals("startcenterX"))
    // lLevel.setStartCenterX(lParser.getAttributeIntValue(i, 0));
    // else if (lAtt.equals("startcenterY"))
    // lLevel.setStartCenterY(lParser.getAttributeIntValue(i, 0));
    // else if (lAtt.equals("predictionBitmap"))
    // lLevel.setPredictionBitmap(lParser.getAttributeValue(i));
    // else if (lAtt.equals("silver"))
    // lLevel.setSilver(lParser.getAttributeIntValue(i, 0));
    // else if (lAtt.equals("gold"))
    // lLevel.setGold(lParser.getAttributeIntValue(i, 0));
    // }
    // } else if (lTag.equals("object")) {
    // int lX = 0, lY = 0, lRotation = 0;
    // int lCollisionSize = SpaceObject.COLLISION_SIZE_IMAGE_WIDTH;
    // float lGrav = 0.f;
    // float lBounce = SpaceObject.BOUNCE_NONE;
    // String lBitmap = "", lType = "", lArrowBitmap = "";
    // boolean lDOI = false;
    // MoveProperties lMove = new MoveProperties();
    // for (int i = 0; i < lParser.getAttributeCount(); i++) {
    // String lAtt = lParser.getAttributeName(i);
    // if (lAtt.equals("bitmap"))
    // lBitmap = lParser.getAttributeValue(i);
    // else if (lAtt.equals("posx"))
    // lX = lParser.getAttributeIntValue(i, 0);
    // else if (lAtt.equals("posy"))
    // lY = lParser.getAttributeIntValue(i, 0);
    // else if (lAtt.equals("angle"))
    // lRotation = lParser.getAttributeIntValue(i, 0);
    // else if (lAtt.equals("type"))
    // lType = lParser.getAttributeValue(i);
    // else if (lAtt.equals("gravity"))
    // lGrav = lParser.getAttributeFloatValue(i, 1.f);
    // else if (lAtt.equals("bounce"))
    // lBounce = lParser.getAttributeFloatValue(i, SpaceObject.BOUNCE_NONE);
    // else if (lAtt.equals("collisionsize"))
    // lCollisionSize = lParser.getAttributeIntValue(i, SpaceObject.COLLISION_SIZE_IMAGE_WIDTH);
    // else if (lAtt.equals("arrowbitmap"))
    // lArrowBitmap = lParser.getAttributeValue(i);
    // else if (lAtt.equals("death_on_impact"))
    // lDOI = lParser.getAttributeBooleanValue(i, false);
    // else if (lAtt.equals("move"))
    // lMove.mMove = lParser.getAttributeBooleanValue(i, false);
    // else if (lAtt.equals("move_radius"))
    // lMove.mRadius = lParser.getAttributeIntValue(i, 0);
    // else if (lAtt.equals("move_dps"))
    // lMove.mDPS = lParser.getAttributeIntValue(i, 0);
    // else if (lAtt.equals("move_offset"))
    // lMove.mOffset = lParser.getAttributeIntValue(i, 0);
    // else
    // PALManager.getLog().e(TAG, "Unexpected attribute [" + lAtt + "] in tag " + lTag);
    // }
    // if (lType.equals("spaceman"))
    // lLevel.addSpaceMan(lX, lY, lBitmap, lArrowBitmap, lCollisionSize, lMove);
    // else if (lType.equals("planet"))
    // lLevel.addPlanet(lX, lY, lBitmap, lGrav, lBounce, lCollisionSize, lDOI, lMove);
    // else if (lType.equals("rocket"))
    // lLevel.addRocket(lX, lY, lBitmap, lCollisionSize, lMove);
    // else if (lType.equals("junk"))
    // lLevel.addJunk(lX, lY, lBitmap, lRotation, lBounce, lDOI, lMove);
    // else if (lType.equals("bonus"))
    // lLevel.addBonus(lX, lY, lBitmap, lCollisionSize, lMove);
    // else
    // PALManager.getLog().e(TAG, "Unexpected object type: " + lType);
    // } else if (lTag.equals("background")) {
    // String lColorInner = "#000000", lColorOuter = "#000000";
    // for (int i = 0; i < lParser.getAttributeCount(); i++) {
    // String lAtt = lParser.getAttributeName(i);
    // if (lAtt.equals("color_inner"))
    // lColorInner = lParser.getAttributeValue(i);
    // else if (lAtt.equals("color_outer"))
    // lColorOuter = lParser.getAttributeValue(i);
    // else
    // PALManager.getLog().e(TAG, "Unexpected attribute [" + lAtt + "] in tag " + lTag);
    // }
    // lLevel.addBackground(lColorInner, lColorOuter);
    // } else {
    // PALManager.getLog().e(TAG, "Unknown tag [" + lTag + "] in levels xml file");
    // }
    // break;
    // case XmlResourceParser.END_TAG:
    // lTag = lParser.getName();
    // if (lTag.equals("level")) {
    // if (!lInLevelTag) {
    // PALManager.getLog().e(TAG, "Error parsing XML: found level end tag without start tag");
    // } else {
    // aLevels.add(lLevel.mId, lLevel);
    // lInLevelTag = false;
    // }
    // }
    // }
    //
    // lParser.next();
    // lEventType = lParser.getEventType();
    // }
    // } catch (XmlPullParserException e) {
    // PALManager.getLog().e("SpaceData", "Exception parsing xml: XmlPullParserException");
    // e.printStackTrace();
    // } catch (IOException e) {
    // PALManager.getLog().e("SpaceData", "Exception parsing xml: IOException");
    // e.printStackTrace();
    // }
    // }
}
