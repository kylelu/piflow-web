package com.nature.component.system.service.Impl;

import com.nature.base.util.ReturnMapUtils;
import com.nature.base.util.SqlUtils;
import com.nature.common.Eunm.SysRoleType;
import com.nature.component.system.model.SysRole;
import com.nature.component.system.model.SysUser;
import com.nature.component.system.service.ISysUserService;
import com.nature.component.system.vo.SysUserVo;
import com.nature.domain.system.SysRoleDomain;
import com.nature.domain.system.SysUserDomain;
import com.nature.mapper.system.SysUserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class SysUserServiceImpl implements ISysUserService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysUserDomain sysUserDomain;

    @Resource
    private SysRoleDomain sysRoleDomain;

    @Override
    public SysUser findByUsername(String username) {
        return sysUserMapper.findUserByUserName(username);
    }

    @Override
    public String checkUserName(String username){
        if (StringUtils.isBlank(username)) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Username can not be empty");
        }
        String addUser = sysUserDomain.checkUsername(username);
        if (StringUtils.isNotBlank(addUser)) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Username is already taken");
        } else {
            return ReturnMapUtils.setSucceededMsgRtnJsonStr("Username is available");
        }
    }

    @Override
    public List<SysUser> findByName(String name) {
        if (StringUtils.isBlank(name)) {
            name = "";
        }
        return sysUserMapper.findUserByName(name);
    }

    @Override
    public List<SysUser> getUserList() {
        List<SysUser> listUser = sysUserMapper.getUserList();
        return listUser;
    }

    @Override
    public SysUser addUser(SysUser user) {
        sysUserDomain.saveOrUpdate(user);
        return user;
    }

    @Override
    public int saveOrUpdate(SysUser user) {
        sysUserDomain.saveOrUpdate(user);
        return 1;
    }

    @Override
    public int deleteUser(String id) {
        sysUserDomain.delete(id);
        return 1;
    }

    @Override
    public String registerUser(SysUserVo sysUserVo) {
        if (null == sysUserVo) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Registration failed, username or password is empty");
        }
        String username = sysUserVo.getUsername();
        String password = sysUserVo.getPassword();
        // Determine if it is empty
        if (StringUtils.isAllEmpty(username, password)) {
            return ReturnMapUtils.setFailedMsgRtnJsonStr("Registration failed, username or password is empty");
        }
        //Encrypted password
        password = new BCryptPasswordEncoder().encode(password);
        SysUser sysUser = new SysUser();
        sysUser.setId(SqlUtils.getUUID32());
        sysUser.setCrtDttm(new Date());
        sysUser.setCrtUser("system");
        sysUser.setLastUpdateDttm(new Date());
        sysUser.setLastUpdateUser("system");
        sysUser.setEnableFlag(true);
        sysUser.setUsername(username);
        sysUser.setPassword(password);
        sysUser.setName(sysUserVo.getName());
        sysUser.setAge(sysUserVo.getAge());
        sysUser.setSex(sysUserVo.getSex());

        List<SysRole> sysRoleList = new ArrayList<>();
        SysRole sysRole = new SysRole();
        sysRole.setRole(SysRoleType.USER);
        sysRole.setSysUser(sysUser);

        sysRoleList.add(sysRole);
        sysUser.setRoles(sysRoleList);

        sysUserDomain.saveOrUpdate(sysUser);
        return ReturnMapUtils.setSucceededMsgRtnJsonStr("Congratulations, registration is successful");
    }
}
