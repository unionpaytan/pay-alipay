import axios from 'axios'

//登录校验账号密码
export const login = (data) => {
    return axios.post('/loginManager/login', data)
}

//直接登录
export const confirmLogin = (data) => {
    return axios.post('/loginManager/confirmLogin', data) 
}

//发起修改密码
export const modifyPwd = (data) => {
    return axios.post('loginManager/modifyPwd', data)
}

//邮箱验证码
export const sendEmailCode = (data) => {
    return axios.post('/loginManager/sendEmailCode', data)
}

//确认修改密码
export const confirmModifyPwd = (data) => {
    return axios.post('/loginManager/confirmModifyPwd', data)
}

//getNewGoogleAuthUrl
//发起新的绑定google身份认证器
export const getNewGoogleAuthUrl = (data) => {
    return axios.post('/authenticator/getNewGoogleAuthUrl', data)
}


//查询商户原先已绑定的google身份认证器
export const getMerchantGoogleAuthUrl = (data) => {
    return axios.post('/authenticator/getMerchantGoogleAuthUrl', data)
}

//发起绑定google身份认证器
export const getGoogleAuthUrl = (data) => {
    return axios.post('/authenticator/getGoogleAuthUrl', data)
}

//google验证码校验(确认绑定&取消验证器)
export const modifyGoogleAuthStatus = (data) => {
    return axios.post('/authenticator/modifyGoogleAuthStatus', data)
}

//查询代付通道列表
export const queryChannelInfoPage = (data) => {
    return axios.post('/channelManager/queryChannelInfoPage', data)
}

//查询代理代付通道列表
export const queryMerchantChannelInfo = (data) => {
    return axios.post('/channelManager/queryMerchantChannelInfo', data)
}

//删除代付通道
export const delChannelInfo = (data) => {
    return axios.post('/channelManager/delChannelInfo', data)
}

//添加代付通道
export const addChannelInfo = (data) => {
    return axios.post('/channelManager/addChannelInfo', data)
}

//修改代付通道
export const modifyChannelInfo = (data) => {
    return axios.post('/channelManager/modifyChannelInfo', data)
}

//修改通道状态
export const modifyChannelStatus = (data) => {
    return axios.post('/channelManager/modifyChannelStatus', data)
}


//获取三方可用通道列表
export const queryCardChannelInfo = (data) => {
    return axios.post('/channelManager/queryCardChannelInfo', data)
}

export const queryDepositChannelInfo = (data) => {
    return axios.post('/channelManager/queryDepositChannelInfo', data)
}
//获取三方可用通道列表
export const queryCountSms = (data) => {
    return axios.post('/app/getCountSms', data)
}

//通道收益
export const platformBalance = (data) => {
    return axios.post('/withdraw/platformBalance', data)
}

//通道利润提现
export const platformWithdraw = (data) => {
    return axios.post('/withdraw/platformWithdraw', data)
}

//商户提现报表
export const queryMerchantWtihdrawCount = (data) => {
    return axios.post('/flow/queryMerchantWtihdrawCount', data)
} 

//商户充值记录商户流水)
export const queryflowPage = (data) => {
    return axios.post('/flow/queryflowPage', data)
}

//商户资金流水记录
export const queryMoneyChangePage = (data) => {
    return axios.post('/flow/queryMoneyChangePage', data)
}

//码商账变明细
export const queryCardMerchantMoneyChangePage = (data) => {
    return axios.post('/flow/queryCardMerchantMoneyChangePage', data)
}
//码商分润明细
export const queryCardProfitFlowPage = (data) => {
    return axios.post('/flow/queryCardProfitFlowPage', data)
}

//代理资金流水记录
export const queryAgentMoneyChangePage = (data) => {
    return axios.post('/flow/queryAgentMoneyChangePage', data)
}

//商户资金每日统计记录
export const queryMoneyBalancePage = (data) => {
    return axios.post('/flow/queryMoneyBalancePage', data)
}

//下级商户充值记录商户流水)
export const queryAgentflowPage = (data) => {
    return axios.post('/flow/queryAgentflowPage', data)
}

//订单列表
export const queryWithdrawPage = (data) => {
    return axios.post('/withdraw/queryWithdrawPage', data)
}

export const queryDepositPage = (data) => {
    return axios.post('/deposit/queryDepositPage', data)
}


//代理订单列表//订单列表
export const queryAgentWithdrawPage = (data) => {
    return axios.post('/withdraw/queryAgentWithdrawPage', data)
}

//代理订单列表//收款订单列表
export const queryAgentDepositPage = (data) => {
    return axios.post('/deposit/queryAgentDepositPage', data)
}

//代理下级商户报表
export const queryMerchantsReportByParentId = (data) => {
    return axios.post('/flow/queryMerchantsReportByParentId', data)
}

//代理统计报表
export const querySysReportForm = (data) => {
    return axios.post('/flow/querySysReportForm', data)
}

//商户统计报表
export const queryMerchantReportForm = (data) => {
    return axios.post('/flow/queryMerchantReportForm', data)
}

//代理商户统计报表
export const queryMerchantReportFormByAgents = (data) => {
    return axios.post('/flow/queryMerchantReportFormByAgents', data)
}

//代付补发通知
export const notice = (data) => {
    return axios.post('/withdraw/notice', data)
}
//代收
export const noticeDeposit = (data) => {
    return axios.post('/deposit/noticeDeposit', data)
}

export const blockIp = (data) => {
    return axios.post('/deposit/blockIp', data)
}

export const removeBlockIp = (data) => {
    return axios.post('/deposit/removeBlockIp', data)
}

export const queryIpBlockList = (data) => {
    return axios.post('/deposit/queryIpBlockList', data)
}

//订单修正
export const withdrawStatusFix = (data) => {
    return axios.post('/withdraw/withdrawStatusFix', data)
}

/**
 * 
 * @卡主管理 ********************************
 * 
 */

 export const queryMerchantInfoListPage = (data) => {
    return axios.post('/merchantAcountManage/queryMerchantInfoListPage', data)
}

export const queryAgentBalance = (data) => {
    return axios.post('/merchantAcountManage/queryAgentBalance', data)
}


 export const setMerchantSelfRechargeStatus = (data) => {
    return axios.post('/app/setMerchantSelfRechargeStatus', data)
}


export const queryMerchantIdListInfo = (data) => {
    return axios.post('/merchantAcountManage/queryMerchantIdListInfo', data)
}

/**
 * 
 * @商户管理 ********************************
 * 
 */

//查询水晶报表
export const queryCrystalData = (data) => {
    return axios.post('/merchantAcountManage/queryCrystalData', data)
}

//获取商户信息列表
export const queryPayMerchantInfoList = (data) => {
    return axios.post('/merchantAcountManage/queryPayMerchantInfoList', data)
} 
 
//获取代理下级商户信息列表
export const queryAgentsMerchantInfoList = (data) => {
    return axios.post('/merchantAcountManage/queryAgentsMerchantInfoList', data)
}

//商户注册接口
export const registerMerchant = (data) => {
    return axios.post('/merchantAcountManage/registerMerchant', data)
}

//编辑商户信息
export const updateMerchantInf = (data) => {
    return axios.post('/merchantAcountManage/updateMerchantInf', data)
}

//编辑API KEY
export const merchantApiKeyGenerate = (data) => {
    return axios.post('/merchantAcountManage/merchantApiKeyGenerate', data)
}

export const merchantPasswordGenerate = (data) => {
    return axios.post('/merchantAcountManage/merchantPasswordGenerate', data)
}

export const modifyMerchantApiKey = (data) => {
    return axios.post('/merchantAcountManage/modifyMerchantApiKey', data)
}
export const modifyMerchantPassword = (data) => {
    return axios.post('/merchantAcountManage/modifyMerchantPassword', data)
}

export const modifyMerchantGoogle = (data) => {
    return axios.post('/merchantAcountManage/modifyMerchantGoogle', data)
}

export const modifyAgentBalance = (data) => {
    return axios.post('/merchantAcountManage/modifyAgentBalance', data)
}

//修改商户状态
export const modifyMerchantStatus = (data) => {
    return axios.post('/merchantAcountManage/modifyMerchantStatus', data)
}

//删除商户
export const deleteMerchantInfo = (data) => {
    return axios.post('/merchantAcountManage/deleteMerchantInfo', data)
}

//充值接口
export const merchantRecharge = (data) => {
    return axios.post('/merchantAcountManage/merchantRecharge', data)
}

//充值接口
export const merchantHolderSelfRecharge = (data) => {
    return axios.post('/app/merchantHolderSelfRecharge', data)
}

//手续费充值接口
export const merchantFeeRecharge = (data) => {
    return axios.post('/merchantAcountManage/merchantFeeRecharge', data)
}

//查询商户余额
export const queryMerchantBalance = (data) => {
    return axios.post('/merchantAcountManage/queryMerchantBalance', data)
}


//查询商户单笔最高可以下余额
export const queryMerchantHighestBalance = (data) => {
    return axios.post('/merchantAcountManage/queryMerchantHighestBalance', data)
}

//查询商户手续费余额
export const queryMerchantAccountChannelFeeBalance = (data) => {
    return axios.post('/merchantAcountManage/queryMerchantAccountChannelFeeBalance', data)
}

//查询银行卡周数据
export const queryWeekBalance = (data) => {
    return axios.post('/merchantAcountManage/queryWeekBalance', data)
}

//查询银行卡four周数据水晶报表
export const queryWeekBalanceCrystalData = (data) => {
    return axios.post('/merchantAcountManage/queryWeekBalanceCrystalData', data)
}


//查询商户的上级用户
export const queryMerchantParentId = (data) => {
    return axios.post('/merchantAcountManage/queryMerchantParentId', data)
}

//商户查询IP白名单
export const queryIpList = (data) => {
    return axios.post('/merchantAcountManage/queryIpList', data)
}

//商户查询IP白名单
export const queryWebIpList = (data) => {
    return axios.post('/merchantAcountManage/queryWebIpList', data)
}

//商户绑定IP白名单
export const bindIp = (data) => {
    return axios.post('/merchantAcountManage/bindIp', data)
}

//商户绑定WEBIP白名单
export const bindWebIp = (data) => {
    return axios.post('/merchantAcountManage/bindWebIp', data)
}

//查询商户代付通道配置
export const queryMerchantChannel = (data) => {
    return axios.post('/merchantChannelSite/queryMerchantChannel', data)
}

//查询代理代付通道配置
export const queryAgentChannel = (data) => {
    return axios.post('/merchantChannelSite/queryAgentChannel', data)
}

//删除代理代付通道配置
export const delAgentChannel = (data) => {
    return axios.post('/merchantChannelSite/delAgentChannel', data)
}

//商户配置代付通道
export const registerMerchantSiteChannel = (data) => {
    return axios.post('/merchantChannelSite/registerMerchantSiteChannel', data)
}

//商户后台WEB代付获取代付说明
export const queryMerchantSiteChannel = (data) => {
    return axios.post('/merchantAcountManage/queryMerchantSiteChannel', data)
}

//商户批量代付
export const merchantWithdraw = (data) => {
    return axios.post('/withdraw/merchantWithdraw', data)
}

//开发文档
export const documentAPIDownload = (data) => {
    return axios.post('/fileDownload/documentAPIDownload', data)
}

//获取商户APIKEY
export const getMerchantAPIKey = (data) => {
    return axios.post('/merchantAcountManage/getMerchantAPIKey', data)
}

export const queryPayMerchantNameById = (data) => {
    return axios.post('/merchantAcountManage/queryPayMerchantNameById', data)
}

//充值订单
export const queryMerchantRechargeCount = (data) => {
    return axios.post('/flow/queryMerchantRechargeCount', data)
}

//批量修改商户状态(新版本)
export const batchModifyMerchantInfoStatus = (data) => {
    return axios.post('/merchantAcountManage/batchModifyMerchantInfoStatus', data)
}

/**
 * 
 * @银行卡通道 ********************************
 * 
 */

 //查询相机状态
export const checkCameraHeartBeat = (data) => {
    return axios.post('/app/checkCameraHeartBeat', data)
}

 //查询相机状态
 export const checkCameraImage = (data) => {
    return axios.post('/app/checkCameraImage', data)
}


//银行卡注册通道
export const registerCardChannel = (data) => {
    return axios.post('/card/registerCardChannel', data)
}

//修改卡通道
export const modifyCardChannel = (data) => {
    return axios.post('/card/modifyCardChannel', data)
}


//获取银行卡注册通道列表
export const queryCardChannelInfoList = (data) => {
    return axios.post('/card/queryCardChannelInfoList', data)
}

//获取通道费率
export const queryRateByCardChannelCode = (data) => {
    return axios.post('/card/queryRateByCardChannelCode', data)
}


//删除通道
export const deleteCardChannelInfo = (data) => {
    return axios.post('/card/deleteCardChannelInfo', data)
}

//批量修改通道状态
export const batchModifyCardChannelInfoStatus = (data) => {
    return axios.post('/card/batchModifyCardChannelInfoStatus', data)
}


//修改通道状态
export const modifyCardChannelStatus = (data) => {
    return axios.post('/card/modifyCardChannelStatus', data)
}

//通道接口
export const cardChannelRecharge = (data) => {
    return axios.post('/card/cardChannelRecharge', data)
}

//查询卡通道流水列表
export const queryCardChannelFlowPage = (data) => {
    return axios.post('/card/queryCardChannelFlowPage', data)
}

//获取可用通道列表
export const queryCardChannelCodeDataList = (data) => {
    return axios.post('/card/queryCardChannelCodeDataList', data)
}


/**
 * 
 * @持卡人 ********************************
 * 
 */

//商户自助充值订单
export const queryPayMerchantSelfRechargePage = (data) => {
    return axios.post('/app/queryPayMerchantSelfRechargePage', data)
}

//商户自助充值
export const merchantSelfRecharge = (data) => {
    return axios.post('/app/merchantSelfRecharge', data)
}

//获取可用银行列表
export const querybankCodeDataList = (data) => {
    return axios.post('/card/querybankCodeDataList', data)
}

//获取银行卡注册通道列表
export const queryCardPayerInfoPage = (data) => {
    return axios.post('/card/queryCardPayerInfoPage', data)
}

//获取商户银行卡注册通道列表
export const queryMerchantCardPayerInfoPage = (data) => {
    return axios.post('/card/queryMerchantCardPayerInfoPage', data)
}

//获取商户钱包列表
export const queryMerchantCoinPayerInfoPage = (data) => {
    return axios.post('/coin/queryMerchantCoinPayerInfoPage', data)
}

//
export const queryMerchantCoinHolderInfoPage = (data) => {
    return axios.post('/coin/queryMerchantCoinHolderInfoPage', data)
}
//获取代理下级商户代付钱包列表
export const queryAgentsMerchantCoinPayerInfoPage = (data) => {
    return axios.post('/coin/queryAgentsMerchantCoinPayerInfoPage', data)
}

//获取代理下级商户代收钱包
export const queryAgentsMerchantCoinHolderInfoPage = (data) => {
    return axios.post('/coin/deposit/queryAgentsMerchantCoinHolderInfoPage', data)
}


export const batchModifyPayerInfoStatus = (data) => {
    return axios.post('/coin/batchModifyPayerInfoStatus', data)
}

export const batchDelPayerInfoStatus = (data) => {
    return axios.post('/coin/batchDelPayerInfoStatus', data)
}

export const batchUploadImage = (data) => {
    return axios.post('/coin/batchUploadImage', data)
}
//删除已付款
export const batchDelPayerPayedList = (data) => {
    return axios.post('/coin/batchDelPayerPayedList', data)
}


//银行卡注册
export const registerCardPayer = (data) => {
    return axios.post('/card/registerCardPayer', data)
}

//修改银行卡卡信息
export const modifyCardPayer = (data) => {
    return axios.post('/card/modifyCardPayer', data)
}

//批量修改银行卡状态
export const batchModifyCardPayerInfoStatus = (data) => {
    return axios.post('/card/batchModifyCardPayerInfoStatus', data)
}


//修改银行卡
export const modifyCardPayerStatus = (data) => {
    return axios.post('/card/modifyCardPayerStatus', data)
}

//删除银行卡
export const delCardPayerInfo = (data) => {
    return axios.post('/card/delCardPayerInfo', data)
}

//银行卡充值变动
export const cardPayerRechargeBalance = (data) => {
    return axios.post('/card/cardPayerRechargeBalance', data)
}

//银行卡充值订单
export const queryPayerFlowPageRechargeCount = (data) => {
    return axios.post('/card/queryPayerFlowPageRechargeCount', data)
}

//查询卡交易流水列表
export const queryCardPayerFlowPage = (data) => {
    return axios.post('/card/queryCardPayerFlowPage', data)
}

//银行卡最高余额
export const getCardPayerHighestAmt = (data) => {
    return axios.post('/card/getCardPayerHighestAmt', data)
}

//代理下级商户银行卡最高余额
export const queryAgentsCardPayerHighestAmt = (data) => {
    return axios.post('/card/queryAgentsCardPayerHighestAmt', data)
}

//商户银行卡总可提现余额
export const getCardPayerCountMap = (data) => {
    return axios.post('/card/getCardPayerCountMap', data)
}

//代理下级商户银行卡最高余额
export const queryAgentsCardPayerCountMap = (data) => {
    return axios.post('/card/queryAgentsCardPayerCountMap', data)
}


//钱包手续费
export const querySumCoinPayer = (data) => {
    return axios.post('/coin/querySumCoinPayer', data)
}

/**
 * app/
 * withdraw
 * 三方订单列表
 * 
 */

//查询卡交易流水列表
export const queryCardPayerWithdrawPage = (data) => {
    return axios.post('/app/queryCardPayerWithdrawPage', data)
}

//查询卡交易流水列表
export const queryCardPayerReturnPage = (data) => {
    return axios.post('/app/queryCardPayerReturnPage', data)
}

export const queryCoinPayerReturnPage = (data) => {
    return axios.post('/app/queryCoinPayerReturnPage', data)
}
//

//查询卡主户冲正列表
export const queryMerchantCardPayerReturnPage = (data) => {
    return axios.post('/app/queryMerchantCardPayerReturnPage', data)
}

//订单修正
export const setWithdrawStatusFix = (data) => {
    return axios.post('/app/setWithdrawStatusFix', data)
}

//订单设为失败
export const setWithdrawStatusFail = (data) => {
    return axios.post('/app/setWithdrawStatusFail', data)
}

//订单设为强制失败
export const setWithdrawStatusForceFail = (data) => {
    return axios.post('/app/setWithdrawStatusForceFail', data)
}

//订单设为冲正
export const setWithdrawStatusReturn = (data) => {
    return axios.post('/app/setWithdrawStatusReturn', data)
}

 
export const setWithdrawStatusSuccess = (data) => {
    return axios.post('/app/setWithdrawStatusSuccess', data)
}

//预设为成功
export const setDepositPreStatusSuccess = (data) => {
    return axios.post('/coin/deposit/setDepositPreStatusSuccess', data)
}

 
export const setDepositStatusSuccess = (data) => {
    return axios.post('/coin/deposit/setDepositStatusSuccess', data)
}

export const setDepositStatusFail = (data) => {
    return axios.post('/coin/deposit/setDepositStatusFail', data)
}

export const setMerchantDepositStatusFail = (data) => {
    return axios.post('/withdraw/deposit/setMerchantDepositStatusFail', data)
}

//卡主设为成功
export const setWithdrawStatusSuccessByManual = (data) => {
    return axios.post('/app/setWithdrawStatusSuccessByManual', data)
}

//订单设为处理中
export const setWithdrawStatusProcess = (data) => {
    return axios.post('/app/setWithdrawStatusProcess', data)
}

//订单人为设置状态
export const setWithdrawStatusByManual = (data) => {
    return axios.post('/app/setWithdrawStatusByManual', data)
}

//订单设为强制成功
export const setWithdrawStatusForceSuccess = (data) => {
    return axios.post('/app/setWithdrawStatusForceSuccess', data)
}

//订单设为强制冲正
export const setWithdrawStatusForceReturn = (data) => {
    return axios.post('/app/setWithdrawStatusForceReturn', data)
}

//订单设为暂停
export const setWithdrawStatusPause = (data) => {
    return axios.post('/app/setWithdrawStatusPause', data)
}

//银行卡短信明细
export const querySmsFlowPage = (data) => {
    return axios.post('/app/querySmsFlowPage', data)
}

//银行APP实时交易明细
export const queryAppRecordList = (data) => {
    return axios.post('/app/queryAppRecordList', data)
}

//中银行卡短信明细
export const queryBocSmsFlowPage = (data) => {
    return axios.post('/app/queryBocSmsFlowPage', data)
}

//银行卡自动上分
export const queryCardPayerSmsAutoRechargeFlowPage = (data) => {
    return axios.post('/app/queryCardPayerSmsAutoRechargeFlowPage', data)
}


/**
 * @合作伙伴
 * 
 * 
*/

export const queryPartnerMonitorSucess = (data) => {
    return axios.post('/partner/monitorSuccess', data)
}

export const queryPartnerDepositList = (data) => {
    return axios.post('/partner/queryPartnerDepositList', data)
}

export const queryOkexTradeInfo = async (data) => {
    return await axios.post('/robot/queryOkexTradeInfo', data)
}

//修改外包状态
export const modifyPartnerStatus = (data) => {
    return axios.post('/partner/modifyPartnerStatus', data)
}

//新增外包
export const registerPartner = (data) => {
    return axios.post('/partner/registerPartner', data)
}

//编辑外包信息
export const updatePartnerInfo = (data) => {
    return axios.post('/partner/updatePartnerInfo', data)
}

//获取外包列表
export const queryPartnerList = (data) => {
    return axios.post('/partner/queryPartnerList', data)
}

//删除外包
export const deletePartnerInfo = (data) => {
    return axios.post('/partner/deletePartnerInfo', data)
}

//外包手续费
export const queryPlatformBalance = (data) => {
    return axios.post('/partner/queryPlatformBalance', data)
}


//外包充值
export const partnerRecharge = (data) => {
    return axios.post('/partner/partnerRecharge', data)
}

//跑量初始设置
export const partnerFreeze = (data) => {
    return axios.post('/partner/partnerFreeze', data)
}

//外包手续费账变流水记录
export const queryPartnerMoneyChangePage = (data) => {
    return axios.post('/partner/queryPartnerMoneyChangePage', data)
}


//外包跑量账变流水记录
export const queryPartnerAmtMoneyChangePage = (data) => {
    return axios.post('/partner/queryPartnerAmtMoneyChangePage', data)
}

//获取三方可用通道列表
export const querySuccessOrder = (data) => {
    return axios.post('/app/querySuccessOrder', data)
}

//获取卡主新订单
export const queryNewOrder = (data) => {
    return axios.post('/app/queryNewOrder', data)
}

//外包手续费账变流水记录by merchantId
export const queryPartnerMoneyChangePageByMerchantId = (data) => {
    return axios.post('/partner/local/queryPartnerMoneyChangePageByMerchantId', data)
}

//外包跑量账变流水记录by merchantId
export const queryPartnerAmtMoneyChangePageByMerchantId = (data) => {
    return axios.post('/partner/local/queryPartnerAmtMoneyChangePageByMerchantId', data)
}

/***
 * 
 * 卡主
 * 
 */
//卡主下挂银行卡 
export const queryCardAgentPayerInfoPage = (data) => {
    return axios.post('/card/queryCardAgentPayerInfoPage', data)
} 

export const queryCardAgentRechargeOrderPage = (data) => {
    return axios.post('/card/queryCardAgentRechargeOrderPage', data)
}

export const queryCardAgentWithdrawList = (data) => {
    return axios.post('/app/queryCardAgentWithdrawList', data)
}

export const queryCoinAgentWithdrawList = (data) => {
    return axios.post('/app/queryCoinAgentWithdrawList', data)
}


export const getCardAgentPayerCountMap = (data) => {
    return axios.post('/card/getCardAgentPayerCountMap', data)
}

export const getCardAgentPayerHighestAmt = (data) => {
    return axios.post('/card/getCardAgentPayerHighestAmt', data)
}

//银行卡充值变动
export const cardAgentRechargeBalance = (data) => {
    return axios.post('/card/cardAgentRechargeBalance', data)
}

//在线状态
export const setCardAgentOnlineStatus = (data) => {
    return axios.post('/app/setCardAgentOnlineStatus', data)
}



/***
 * 
 * COIN
 * 
 */

//获取coin代付通道列表
export const queryCoinWithdrawChannelList = (data) => {
    return axios.post('/coin/queryCoinWithdrawChannelList', data)
}

//获取coin代收通道列表
export const queryCoinDepositChannelList = (data) => {
    return axios.post('/coin/queryCoinDepositChannelList', data)
}

//修改coin通道
export const editCoinWithdrawChannel = (data) => {
    return axios.post('/coin/editCoinWithdrawChannel', data)
}

export const editCoinDepositChannel = (data) => {
    return axios.post('/coin/editCoinDepositChannel', data)
}

//删除coin通道
export const delCoinWithdrawChannel = (data) => {
    return axios.post('/coin/delCoinWithdrawChannel', data)
}

export const delCoinDepositChannel = (data) => {
    return axios.post('/coin/delCoinDepositChannel', data)
}


//修改coin代付通道状态
export const modifyCoinWithdrawChannelStatus = (data) => {
    return axios.post('/coin/modifyCoinWithdrawChannelStatus', data)
}
//修改coin代收通道状态
export const modifyCoinDepositChannelStatus = (data) => {
    return axios.post('/coin/modifyCoinDepositChannelStatus', data)
}



//批量修改通道状态
export const batchModifyCoinWithdrawChannelInfoStatus = (data) => {
    return axios.post('/coin/batchModifyCoinWithdrawChannelInfoStatus', data)
}

//新增coin通道状态
export const registerCoinWithdrawChannel = (data) => {
    return axios.post('/coin/registerCoinWithdrawChannel', data)
}

//新增coin通道状态
export const registerCoinDepositChannel = (data) => {
    return axios.post('/coin/registerCoinDepositChannel', data)
}


//通道接口
export const coinChannelRecharge = (data) => {
    return axios.post('/coin/coinChannelRecharge', data)
}


//通道接口
export const queryCoinChannelFlowPage = (data) => {
    return axios.post('/coin/queryCoinChannelFlowPage', data)
}


//获取三方代付通道列表
export const queryCoinChannelWithdrawInfo = (data) => {
    return axios.post('/coin/queryCoinChannelWithdrawInfo', data)
}


export const queryCoinPayerInfoPage = (data) => {
    return axios.post('/coin/queryCoinPayerInfoPage', data)
}

//钱包注册
export const registerCoinPayer = (data) => {
    return axios.post('/coin/registerCoinPayer', data)
}

//钱包修改
export const modifyCoinPayer = (data) => {
    return axios.post('/coin/modifyCoinPayer', data)
}

//钱包del
export const delCoinPayerInfo = (data) => {
    return axios.post('/coin/delCoinPayerInfo', data)
}
//modifyCoinPayerStatus
export const modifyCoinPayerStatus = (data) => {
    return axios.post('/coin/modifyCoinPayerStatus', data)
} 
 
export const queryRateByWithdrawChannelCode = (data) => {
    return axios.post('/coin/queryRateByWithdrawChannelCode', data)
} 

export const queryRateByDepositChannelCode = (data) => {
    return axios.post('/coin/queryRateByDepositChannelCode', data)
} 

//代付钱包充值
export const coinPayerRechargeBalance = (data) => {
    return axios.post('/coin/coinPayerRechargeBalance', data)
} 

//代收钱包充值
export const coinHolderRechargeBalance = (data) => {
    return axios.post('/coin/coinHolderRechargeBalance', data)
} 


//
export const getCoinPayerHighestAmt = (data) => {
    return axios.post('/coin/getCoinPayerHighestAmt', data)
} 
//
export const getCoinPayerCountMap = (data) => {
    return axios.post('/coin/getCoinPayerCountMap', data)
}  

export const queryCoinPayerFlowPage = (data) => {
    return axios.post('/coin/queryCoinPayerFlowPage', data)
}  

/**
 * 代收
 * 
*/
//获取三方代付通道列表
export const queryCoinChannelDepositInfo = (data) => {
    return axios.post('/coin/queryCoinChannelDepositInfo', data)
}
 

//获取三方代付通道列表
export const queryPayerMerchantInfoList = (data) => {
    return axios.post('/coin/queryPayerMerchantInfoList', data)
}

//收款码批量分配通道
export const batchUpdateCoinChannelCode = (data) => {
    return axios.post('/coin/batchUpdateCoinChannelCode', data)
}

export const queryCoinHolderInfoPage = (data) => {
    return axios.post('/coin/queryCoinHolderInfoPage', data)
}

export const modifyCoinHolder = (data) => {
    return axios.post('/coin/modifyCoinHolder', data)
}

export const modifyAgentCoinHolder = (data) => {
    return axios.post('/coin/modifyAgentCoinHolder', data)
}

export const modifyOperatorCoinHolder = (data) => {
    return axios.post('/coin/modifyOperatorCoinHolder', data)
}

export const delCoinHolderInfo = (data) => {
    return axios.post('/coin/delCoinHolderInfo', data)
}

export const modifyCoinHolderStatus = (data) => {
    return axios.post('/coin/modifyCoinHolderStatus', data)
}

export const modifyOperatorCoinHolderStatus = (data) => {
    return axios.post('/coin/modifyOperatorCoinHolderStatus', data)
}


export const registerCoinHolder = (data) => {
    return axios.post('/coin/registerCoinHolder', data)
}
export const registerAgentCoinHolder = (data) => {
    return axios.post('/coin/registerAgentCoinHolder', data)
}

export const registerOperatorCoinHolder = (data) => {
    return axios.post('/coin/registerOperatorCoinHolder', data)
}

export const queryCoinHolderFlowPage = (data) => {
    return axios.post('/coin/queryCoinHolderFlowPage', data)
}


export const queryChannelInfo = (data) => {
    return axios.post('/channelManager/queryChannelInfo', data)
}

export const addDepositChannelInfo = (data) => {
    return axios.post('/coin/addDepositChannelInfo', data)
}
//modifyChannelInfo
export const modifyDepositChannelInfo = (data) => {
    return axios.post('/coin/modifyDepositChannelInfo', data)
}
//delChannelInfo
export const delDepositChannelInfo = (data) => {
    return axios.post('/coin/delDepositChannelInfo', data)
}

export const queryCoinPayerWithdrawPage = (data) => {
    return axios.post('/coin/queryCoinPayerWithdrawPage', data)
}

//三方代收订单列表
export const queryCoinPayerDepositPage = (data) => {
    return axios.post('/coin/deposit/queryCoinPayerDepositPage', data)
}

// export const getCoinHolderHighestAmt = (data) => {
//     return axios.post('/coin/getCoinHolderHighestAmt', data)
// }

export const getCoinHolderCountMap = (data) => {
    return axios.post('/coin/getCoinHolderCountMap', data)
}

export const queryFollowerOrderList = (data) => {
    return axios.post('/coin/deposit/queryFollowerOrderList', data)
}
export const queryFollowerOrcodeList = (data) => {
    return axios.post('/coin/queryFollowerOrcodeList', data)
}

export const queryFollowerOrcodeCount = (data) => {
    return axios.post('/coin/queryFollowerOrcodeCount', data)
}

export const generateQrcodeByUid = (data) => {
    return axios.post('/coin/generateQrcodeByUid', data)
}

//获取支付宝商家信息列表
export const queryCoinAlipayInfoList = (data) => {
    return axios.post('/AlipayAccountManage/queryCoinAlipayInfoList', data)
} 