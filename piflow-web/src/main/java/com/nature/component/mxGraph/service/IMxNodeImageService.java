package com.nature.component.mxGraph.service;


import org.springframework.web.multipart.MultipartFile;

public interface IMxNodeImageService {

    public String uploadNodeImage(MultipartFile file);

    public String getMxNodeImageList();

}
