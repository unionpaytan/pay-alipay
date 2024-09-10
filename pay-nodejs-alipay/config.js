
/**
 * 
 * @返回主程序以便启动其他APP，注意赋予主程序后台启动权限
 * @查询转账记录:因银行月结,转账记录偶尔会不存在 订单失败须同时满足不在[短信列表]及银行app[转账记录]中
 */
 
var CONFIG = {

PRIVATE_MD5KEY : "EDtKm", //32位MD5加密的MD5密钥 java application.xml
GATEWAY_SAVETASK :"",

TASK_INFO : { 
        "withdrawNo" : "", //订单号
        "withdrawStatus" : "", //订单状态 WithdrawStatus
        "payerName": "", //tiktok名称
        "payerAddr": "", //tiktok登录账号
        "missionCode": "", //任务类别 可replace taskType
        "missionUrl": "",//从外部调起 tiktok的URL
        "missionComment": "",//评论 系统后台输入的 comment 
        "liveComment":"",//直播评论,
        "missionLiveTime":"",//直播间时长 30/60/90/120/150/180/210/240/270/300 分钟
        "taskStartTime": "", //提交CCB转账时间 < 银行转账成功时间 datetime
        "taskEndtime": "",
        "taskType": "",
        "memo":"",//备注 || 比如传送订单号 以便 app查找到对应的要评论的已购订单号
    },
 
 WITHDRAW_STATUS : {
    "PROCESSING": "0", //订单正在三方服务器上排队中
    "SUCCESS": "1",
    "FAIL": "2", 
    "ONGOING": "3", //处理中
    "EXPIRED": "-1", //5分钟后超时|服务器任务
},
//直播间状态
LIVE_STATUS : {
    "SHOW_OFF": "0", //已退出
    "SHOW_ON": "1",  //正在直播间
    "NOTLIVE": "-1",  //默认订单为非直播任务
},

//MISSION_CODE
 TASK_TYPE : {
    "order_add":"order_add",//订单订购
    "order_comment_rate":"order_comment_rate",//已购订单评论评价

    "video_like":"video_like",//视频点赞
    "video_favorite":"video_favorite",//视频收藏
    "video_comment":"video_comment",//视频评论
    
    "add_following":"add_following",//跟随成为 关注加粉

    "live_login":"live_login",//直播间登录 ||@TODO:
    //"live_logout":"live_logout",
    "live_comment":"live_comment",//直播间评论 ||@TODO:

    "sys_browser_randomly":"sys_browser_randomly", //养号 随机关注视频
    "sys_order_clear":"sys_order_clear", //养号 删除未付款订单
 },  
}
 
//导出通用对象
module.exports = CONFIG;