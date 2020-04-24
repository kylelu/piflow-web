package com.nature.component.process.service.Impl;

import com.nature.base.util.*;
import com.nature.base.vo.UserVo;
import com.nature.common.Eunm.ProcessParentType;
import com.nature.common.Eunm.ProcessState;
import com.nature.common.Eunm.RunModeType;
import com.nature.component.process.model.ProcessAndProcessGroup;
import com.nature.component.process.model.ProcessGroup;
import com.nature.component.process.service.IProcessAndProcessGroupService;
import com.nature.component.process.service.IProcessGroupService;
import com.nature.component.process.utils.ProcessGroupUtils;
import com.nature.component.process.utils.ProcessUtils;
import com.nature.component.process.vo.DebugDataRequest;
import com.nature.component.process.vo.DebugDataResponse;
import com.nature.component.process.vo.ProcessGroupVo;
import com.nature.domain.custom.ProcessAndProcessGroupDomain;
import com.nature.domain.process.ProcessDomain;
import com.nature.domain.process.ProcessGroupDomain;
import com.nature.mapper.process.ProcessGroupMapper;
import com.nature.third.service.IFlow;
import com.nature.third.service.IGroup;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

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
        Page<Map<String,Object>> processGroupListPage = processAndProcessGroupDomain.getProcessAndProcessGroupListPage(offset - 1, limit, param);
        Map<String, Object> rtnMap = ReturnMapUtils.setSucceededMsg(ReturnMapUtils.SUCCEEDED_MSG);
        rtnMap.put("msg", "");
        rtnMap.put("count", processGroupListPage.getTotalElements());
        rtnMap.put("data", processGroupListPage.getContent());//Data collection
        return JsonUtils.toJsonNoException(rtnMap);
    }

}