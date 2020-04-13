package com.nature.component.mxGraph.service.impl;

import com.nature.base.util.*;
import com.nature.base.vo.UserVo;
import com.nature.common.constant.SysParamsCache;
import com.nature.component.mxGraph.model.MxNodeImage;
import com.nature.component.mxGraph.service.IMxNodeImageService;
import com.nature.component.mxGraph.utils.MxNodeImageUtils;
import com.nature.component.mxGraph.vo.MxNodeImageVo;
import com.nature.domain.mxGraph.MxNodeImageDomain;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class MxNodeImageServiceImpl implements IMxNodeImageService {

    Logger logger = LoggerUtil.getLogger();

    @Resource
    private MxNodeImageDomain mxNodeImageDomain;

    @Override
    public String uploadNodeImage(MultipartFile file) {
        UserVo user = SessionUserUtil.getCurrentUser();
        String username = (null != user) ? user.getUsername() : "-1";
        if (file.isEmpty()) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Upload failed, please try again later");
        }
        Map<String, Object> uploadMap = FileUtils.uploadRtnMap(file, SysParamsCache.IMAGES_PATH);
        if (null == uploadMap || uploadMap.isEmpty()) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Upload failed, please try again later");
        }
        Integer code = (Integer) uploadMap.get("code");
        if (500 == code) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("failed to upload file");
        }
        String name = (String) uploadMap.get("fileName");
        String path = (String) uploadMap.get("path");
        MxNodeImage mxNodeImage = MxNodeImageUtils.newMxNodeImageNoId(username);
        mxNodeImage.setId(SqlUtils.getUUID32());
        SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmSSSS");
        Date nowDate = new Date();
        String fileName = sdf.format(nowDate);
        //File name prefix
        String prefix = name.substring(0, name.length() - 4);
        //Suffix .xml
        String Suffix = name.substring(name.length() - 4);
        //Add timestamp
        String uploadfileName = prefix + "-" + fileName;
        mxNodeImage.setImageName(uploadfileName + Suffix);
        mxNodeImage.setImagePath(path);
        mxNodeImage.setImageUrl("/images/" + name);
        mxNodeImageDomain.saveOrUpdate(mxNodeImage);
        return ReturnMapUtils.setSucceededCustomParamRtnJsonStr("imgUrl", mxNodeImage.getImageUrl());
    }

    @Override
    public String getMxNodeImageList() {
        List<MxNodeImageVo> mxNodeImageVoList = new ArrayList<>();
        List<MxNodeImage> mxNodeImageList = mxNodeImageDomain.getMxNodeImageList();
        if (null == mxNodeImageList && mxNodeImageList.size() <= 0) {
            return ReturnMapUtils.setSucceededCustomParamRtnJsonStr("nodeImageList", mxNodeImageList);
        }
        MxNodeImageVo mxNodeImageVo;
        for (MxNodeImage mxNodeImage : mxNodeImageList) {
            if (null == mxNodeImage) {
                continue;
            }
            mxNodeImageVo = new MxNodeImageVo();
            BeanUtils.copyProperties(mxNodeImage, mxNodeImageVo);
            mxNodeImageVoList.add(mxNodeImageVo);
        }
        return ReturnMapUtils.setSucceededCustomParamRtnJsonStr("nodeImageList", mxNodeImageList);
    }
}
