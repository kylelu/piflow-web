package com.nature.component.process.service.Impl;

import com.nature.base.util.JsonUtils;
import com.nature.base.util.LoggerUtil;
import com.nature.base.util.ReturnMapUtils;
import com.nature.base.util.SessionUserUtil;
import com.nature.component.process.service.IProcessAndProcessGroupService;
import com.nature.domain.custom.ProcessAndProcessGroupDomain;
import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class ProcessAndProcessGroupServiceImpl implements IProcessAndProcessGroupService {

    Logger logger = LoggerUtil.getLogger();

    @Resource
    private ProcessAndProcessGroupDomain processAndProcessGroupDomain;

    /**
     * Query ProcessAndProcessGroupList (parameter space-time non-paging)
     *
     * @param offset Number of pages
     * @param limit  Number each page
     * @param param  Search content
     * @return json
     */
    @Override
    public String getProcessAndProcessGroupListPage(Integer offset, Integer limit, String param) {
        if (null == offset || null == limit) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr(ReturnMapUtils.ERROR_MSG);
        }
        Page<Map<String, Object>> processGroupListPage;
        if (SessionUserUtil.isAdmin()) {
            processGroupListPage = processAndProcessGroupDomain.getProcessAndProcessGroupListPage(offset - 1, limit, param);
        } else {
            String currentUsername = SessionUserUtil.getCurrentUsername();
            processGroupListPage = processAndProcessGroupDomain.getProcessAndProcessGroupListPageByUser(currentUsername, offset - 1, limit, param);
        }
        Map<String, Object> rtnMap = ReturnMapUtils.setSucceededMsg(ReturnMapUtils.SUCCEEDED_MSG);
        rtnMap.put("msg", "");
        rtnMap.put("count", processGroupListPage.getTotalElements());
        rtnMap.put("data", processGroupListPage.getContent());//Data collection
        return JsonUtils.toJsonNoException(rtnMap);
    }

}