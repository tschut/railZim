package com.spacemangames.math;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class PointFTest {

    private static final float DELTA = 0.0001f;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testPointF() {
        PointF point = new PointF();

        Assert.assertEquals(0.0, point.x, DELTA);
        Assert.assertEquals(0.0, point.y, DELTA);
    }

    @Test
    public void testPointFFloatFloat() {
        PointF point = new PointF(1.3f, 1234.5678f);

        Assert.assertEquals(1.3, point.x, DELTA);
        Assert.assertEquals(1234.5678, point.y, DELTA);
    }

    @Test
    public void testSet() {
        PointF point = new PointF();

        point.set(-50.3f, 7.3f);

        Assert.assertEquals(-50.3, point.x, DELTA);
        Assert.assertEquals(7.3, point.y, DELTA);
    }

}
