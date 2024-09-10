import axios from 'axios'

//获取权限资源列表 （菜单）
export const queryPermissionInfoList = (data) => { 
    return axios.post('/permissionInfo/queryPermissionInfoList',data)
}

//删除资源 （菜单）
export const delPermissionInfo = (data) => { 
    return axios.post('/permissionInfo/delPermissionInfo',data)
}

//新增权限 （菜单）
export const addPermissionInfo = (data) => { 
    return axios.post('/permissionInfo/addPermissionInfo',data)
}

//修改资源权限 （菜单）
export const modifyPermissionInfo = (data) => { 
    return axios.post('/permissionInfo/modifyPermissionInfo',data)
}

/**
 * ***
 * *** =========== 号段 ============== ***
 * ***
 */

//号段管理
export const getSysCardList = (data) => { 
    return axios.post('/adminAcountManage/getSysCardList',data)
}
//新增号段
export const addSystemCard = (data) => { 
    return axios.post('/adminAcountManage/addSystemCard',data)
}

/**
 * ***
 * *** =========== 系统用户 ============== ***
 * ***
 */

//用户列表 
export const getSysUserList = (data) => { 
    return axios.post('/adminAcountManage/getSysUserList',data)
} 

//新增用户 
export const addSystemUser = (data) => { 
    return axios.post('/adminAcountManage/addSystemUser',data)
} 

//删除用户 
export const delSysUser = (data) => { 
    return axios.post('/adminAcountManage/delSysUser',data)
}

//删除号段
export const delSysCard = (data) => { 
    return axios.post('/adminAcountManage/delSysCard',data)
}

//修改用户信息 
export const updateSysUser = (data) => { 
    return axios.post('/adminAcountManage/updateSysUser',data)
}

//修改用户账号状态 
export const updateUserStatus = (data) => { 
    return axios.post('/adminAcountManage/updateUserStatus',data)
}

/**
 * ***
 * *** =========== 支付宝商家 ============== ***
 * ***
 */
 export const queryAlipayInfo = (data) => { 
    return axios.post('/AlipayAccountManage/queryAlipayInfo',data)
}
export const addAlipayInfo = (data) => { 
    return axios.post('/AlipayAccountManage/addAlipayInfo',data)
}
export const updateAlipayInfo = (data) => { 
    return axios.post('/AlipayAccountManage/updateAlipayInfo',data)
}
export const updateAlipayInfoStatus = (data) => { 
    return axios.post('/AlipayAccountManage/updateAlipayInfoStatus',data)
} 

export const delAlipay = (data) => { 
    return axios.post('/AlipayAccountManage/delAlipay',data)
}

export const unbindAlipay = (data) => { 
    return axios.post('/AlipayAccountManage/unbindAlipay',data)
}
 
export const unbindCoinHolder= (data) => { 
    return axios.post('/AlipayAccountManage/unbindCoinHolder',data)
}
/**
 * ***
 * *** =========== 卡主 ============== ***
 * ***
 */
//卡主 新增
export const addCardUser = (data) => { 
    return axios.post('/cardAccountManage/addCardUser',data)
}

export const addMerchantFollower = (data) => { 
    return axios.post('/cardAccountManage/addMerchantFollower',data)
}

export const addMerchantOperator = (data) => { 
    return axios.post('/cardAccountManage/addMerchantOperator',data)
}


export const updateCardUser = (data) => { 
    return axios.post('/cardAccountManage/updateCardUser',data)
}

//卡主列表 
export const queryMerchantWholeSeller = (data) => { 
    return axios.post('/cardAccountManage/queryMerchantWholeSeller',data)
}

export const queryMerchantWholeSellerReport= (data) => { 
    return axios.post('/cardAccountManage/queryMerchantWholeSellerReport',data)
}

export const queryCoinHolderDepositList = (data) => {
    return axios.post('/coin/deposit/queryCoinHolderDepositList', data)
}

export const queryMerchantQueue = (data) => { 
    return axios.post('/cardAccountManage/queryMerchantQueue',data)
}

export const queryMerchantFollowers = (data) => { 
    return axios.post('/cardAccountManage/queryMerchantFollowers',data)
}

export const queryMerchantOperators = (data) => { 
    return axios.post('/cardAccountManage/queryMerchantOperators',data)
}

//卡主删除
export const delCardUser = (data) => { 
    return axios.post('/cardAccountManage/delCardUser',data)
}

export const delMerchantFollower = (data) => { 
    return axios.post('/cardAccountManage/delMerchantFollower',data)
}

//修改卡主账号状态 
export const updateCardUserStatus = (data) => { 
    return axios.post('/cardAccountManage/updateCardUserStatus',data)
}

export const updateMerchantFollowerStatus = (data) => { 
    return axios.post('/cardAccountManage/updateMerchantFollowerStatus',data)
}

export const updateMerchantFollower = (data) => { 
    return axios.post('/cardAccountManage/updateMerchantFollower',data)
}

/**
 * ***
 * *** =========== 角色 ============== ***
 * ***
 */

//获取角色列表 
export const queryUserRoleListPage = (data) => { 
    return axios.post('/userRole/queryUserRoleListPage',data)
}

//新增角色 
export const addUserRole = (data) => { 
    return axios.post('/userRole/addUserRole',data)
}


//删除角色 
export const delUserRole = (data) => { 
    return axios.post('/userRole/delUserRole',data)
}

//获取角色权限 
export const queryRolePermission = (data) => { 
    return axios.post('/userRole/queryRolePermission',data)
}

//配置角色权限 
export const sitiRolePermission = (data) => { 
    return axios.post('/userRole/sitiRolePermission',data)
}































