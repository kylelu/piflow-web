package com.nature.domain.custom;


import com.nature.repository.process.ProcessGroupJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class ProcessAndProcessGroupDomain {

    @Resource
    private ProcessGroupJpaRepository processGroupJpaRepository;

    public Page<Map<String,Object>> getProcessAndProcessGroupListPage(int page, int size, String param) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "crt_dttm"));
        return processGroupJpaRepository.getProcessAndProcessGroupListPage(null == param ? "" : param, pageRequest);
    }
    public Page<Map<String,Object>> getProcessAndProcessGroupListPageByUser(String username,int page, int size, String param) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "crt_dttm"));
        return processGroupJpaRepository.getProcessAndProcessGroupListPageByUser(username, null == param ? "" : param, pageRequest);
    }

}
