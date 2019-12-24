package com.nature.base.config;

import com.nature.common.constant.SysParamsCache;
import com.nature.component.system.model.SysInitRecords;
import com.nature.domain.system.SysInitRecordsDomain;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Defining interceptors
 */
@Component
@Log4j
public class ConfigInterceptor implements HandlerInterceptor {

    @Autowired
    private SysInitRecordsDomain sysInitRecordsDomain;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/piflow-web/bootPage") && !SysParamsCache.IS_BOOT_COMPLETE) {
            SysInitRecords sysInitRecordsLastNew = sysInitRecordsDomain.getSysInitRecordsLastNew(1);
            if (null == sysInitRecordsLastNew || !sysInitRecordsLastNew.getIsSucceed()) {
                log.info("No initialization, enter the boot page");
            } else {
                response.sendRedirect("/piflow-web/"); // Redirect to the boot page
                return false;
            }
        } else if (!SysParamsCache.IS_BOOT_COMPLETE) {
            response.sendRedirect("/piflow-web/bootPage/initPage"); // Redirect to the boot page
            return false;
        } else if (requestURI.startsWith("/piflow-web/bootPage")) {
            response.sendRedirect("/piflow-web/"); // Redirect to the boot page
            return false;
        }
        return true;
    }
}
