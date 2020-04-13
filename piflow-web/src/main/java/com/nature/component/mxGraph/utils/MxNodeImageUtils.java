package com.nature.component.mxGraph.utils;

import com.nature.component.mxGraph.model.MxNodeImage;

import java.util.Date;

public class MxNodeImageUtils {

    public static MxNodeImage newMxNodeImageNoId(String username){
        MxNodeImage mxNodeImage = new MxNodeImage();
        // basic properties (required when creating)
        mxNodeImage.setCrtDttm(new Date());
        mxNodeImage.setCrtUser(username);
        // basic properties
        mxNodeImage.setEnableFlag(true);
        mxNodeImage.setLastUpdateUser(username);
        mxNodeImage.setLastUpdateDttm(new Date());
        mxNodeImage.setVersion(0L);
        return mxNodeImage;
    }
}
