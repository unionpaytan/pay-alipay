package com.bootpay.mng.service;

import java.util.List;

import com.bootpay.common.excetion.TranException;
import com.bootpay.core.entity.PermissionInfo;

/**
 * 获取用户权限
 * @author Administrator
 *
 */
public interface UserPermissionService {
	
	/**
	 * 获取用户权限树
	 * @param roleId
	 * @return
	 */
	public List<PermissionInfo> queryUserPermission(Integer roleId) throws TranException ;

}
