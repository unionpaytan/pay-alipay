package com.bootpay.mng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.excetion.TranException;
import com.bootpay.core.entity.PermissionInfo;
import com.bootpay.core.service.IPermissionInfoService;
import com.bootpay.core.service.IRolePermissionAssociateService;
import com.bootpay.mng.service.UserPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 获取用户权限
 * 
 * @author Administrator
 *
 */
@Service
public class UserPermissionServiceImpl implements UserPermissionService {

	private Logger _log = LoggerFactory.getLogger(UserPermissionServiceImpl.class);
	@Autowired
	private IRolePermissionAssociateService iRolePermissionAssociateService;
	@Autowired
	private IPermissionInfoService iPermissionInfoService;

	@Override
	public List<PermissionInfo> queryUserPermission(Integer roleId) throws TranException {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("roleId", roleId);
		// roleID = 1 or roleID = 5 少了个为何会少了个菜单管理
		List<PermissionInfo> allPermissionInfosList = iRolePermissionAssociateService.queryUserPermission(param);
		if(allPermissionInfosList == null || allPermissionInfosList.size() == 0){
			throw new TranException(CodeConst.SYSTEM_ERROR_CODE,"未配置权限");
		}
		return recursive(allPermissionInfosList);
	}
	
	/**
	 * 递归获取树形结构
	 * @param infos
	 * @return
	 */
	public List<PermissionInfo> recursive(List<PermissionInfo> infos){
		Set<String> fatherSet = new HashSet<>();
		infos.forEach( permission-> {
			// _log.info("permission menuId:{},parent menuId:{}",permission.getId(),permission.getpMenuId());
			if(permission.getpMenuId() != null){
				fatherSet.add(permission.getpMenuId().toString());
			}
		});
		if(fatherSet.size() == 0){
			return infos;
		}
		System.out.println("===>>>>fatherSet:" + fatherSet);
		List<PermissionInfo> fatherPermissionList =  iPermissionInfoService.list(new QueryWrapper<PermissionInfo>().lambda().in(PermissionInfo :: getId, fatherSet));

		fatherPermissionList.forEach( fatherPerm-> {
			List<PermissionInfo> addList = new ArrayList<>();
			infos.forEach(permissionInfo ->{
				//_log.info("getid:{},getpMenuId:{}",fatherPerm.getId(),permissionInfo.getpMenuId());
				if(fatherPerm.getId().equals(permissionInfo.getpMenuId()) ){
					addList.add(permissionInfo);
				}
			});
			fatherPerm.setChildrenList(addList);
		});
		return recursive(fatherPermissionList);
	}
	
	
	

}
