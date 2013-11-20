package com.spacemangames.railzim.pal.levelsjson;

public class ObjectJson {
    private String  type;
    private String  bitmap;
    private String  arrowBitmap;

    private int     collisionSize;

    private boolean deathOnImpact;
    private float   gravity;

    private boolean move;
    private int     moveDps;
    private int     moveOffset;
    private int     moveRadius;

    private int     posx;
    private int     posy;
    private boolean lazyLoading = false;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBitmap() {
        return bitmap;
    }

    public void setBitmap(String bitmap) {
        this.bitmap = bitmap;
    }

    public String getArrowBitmap() {
        return arrowBitmap;
    }

    public void setArrowBitmap(String arrowBitmap) {
        this.arrowBitmap = arrowBitmap;
    }

    public int getCollisionSize() {
        return collisionSize;
    }

    public void setCollisionSize(int collisionSize) {
        this.collisionSize = collisionSize;
    }

    public boolean getDeathOnImpact() {
        return deathOnImpact;
    }

    public void setDeathOnImpact(boolean deathOnImpact) {
        this.deathOnImpact = deathOnImpact;
    }

    public float getGravity() {
        return gravity;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public boolean getMove() {
        return move;
    }

    public void setMove(boolean move) {
        this.move = move;
    }

    public int getMoveDps() {
        return moveDps;
    }

    public void setMoveDps(int moveDps) {
        this.moveDps = moveDps;
    }

    public int getMoveOffset() {
        return moveOffset;
    }

    public void setMoveOffset(int moveOffset) {
        this.moveOffset = moveOffset;
    }

    public int getMoveRadius() {
        return moveRadius;
    }

    public void setMoveRadius(int moveRadius) {
        this.moveRadius = moveRadius;
    }

    public int getPosx() {
        return posx;
    }

    public void setPosx(int posx) {
        this.posx = posx;
    }

    public int getPosy() {
        return posy;
    }

    public void setPosy(int posy) {
        this.posy = posy;
    }

    public boolean getLazyLoading() {
        return lazyLoading;
    }

    public void setLazyLoading(boolean lazyLoading) {
        this.lazyLoading = lazyLoading;
    }
}
