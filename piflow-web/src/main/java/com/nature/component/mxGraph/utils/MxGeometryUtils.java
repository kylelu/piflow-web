package com.nature.component.mxGraph.utils;

import com.nature.component.mxGraph.model.MxGeometry;

import java.util.Date;

public class MxGeometryUtils {

    public static MxGeometry mxGeometryNewNoId(String username) {

        MxGeometry mxGeometry = new MxGeometry();
        // basic properties (required when creating)
        mxGeometry.setCrtDttm(new Date());
        mxGeometry.setCrtUser(username);
        // basic properties
        mxGeometry.setEnableFlag(true);
        mxGeometry.setLastUpdateUser(username);
        mxGeometry.setLastUpdateDttm(new Date());
        mxGeometry.setVersion(0L);
        return mxGeometry;
    }
}
