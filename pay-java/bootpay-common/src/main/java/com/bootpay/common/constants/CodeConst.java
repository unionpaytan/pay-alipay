package com.bootpay.common.constants;

/**
 * 业务编码
 * @author Administrator
 *
 */
public class CodeConst {
	
	public static final String SUCCESS_CODE = "0000";
	public static final String SUCCESS_MSG = "正常";

	public static final String SYSTEM_ERROR_CODE = "1000";//系统异常

	public static final String BUSINESS_ERROR_CODE = "1002";//业务异常
	public static final String CHANNEL_ERROR_CODE = "1003";//通道接口异常
	public static final String PROCESSING_ERROR_CODE = "1004";//已有任务在执行
	public static final String WITHOUT_ORDER_ERROR_CODE = "1005";//无任务
	public static final String TOKEN_CODE = "6000";//token 过期
	public static final String PERMISSION_CODE = "9000";// 无权访问



	public static final class DoubleConfirm{
		public static final String NO = "0";//收款人确认收款无须审核 直接确认
		public static final String YES = "1";//启用确认
	}

	/**
	 * 谷歌身份认证状态
	 * @author Administrator
	 *
	 */
	public static final class GoogleConst{
		public static final String GOOGLE_AUTH_N = "0";//未启用
		public static final String GOOGLE_AUTH_Y = "1";//启用
	}

	public static final class BusinessType{
		public static final String PERSON = "0";//个人码
		public static final String BUSINESS = "1";//商家码(只能收一次)
	}
	public static final class AutoSuccessType{
		public static final String MANUAL = "0";//手动设为成功
		public static final String AUTO = "1";//自动
	}

	/**
	 * 代付订单的 是否暂停出款
	 * @author Administrator
	 *
	 */
	public static final class WithdrawPauseConst{
		public static final String WITHDRAW_PAUSE_N= "0"; //正常
		public static final String WITHDRAW_PAUSE_Y = "1";//暂停
	}
	
	/**
	 * 通道状态
	 * @author Administrator
	 *
	 */
	public static final class CoinChannelStatus{
		public static final String ENABLE = "1";//启用
		public static final String DISABLE = "0";//禁用
	}

	/**
	 * 通道手续费状态
	 * @author Administrator
	 *
	 */
	public static final class ChannelTypeConst{
		public static final String WITHDRAW = "0";//代付
		public static final String DEPOSIT = "1";//代收
	}

	/**
	 * 通道手续费状态
	 * @author Administrator
	 *
	 */
	public static final class ChannelFeeTypeConst{
		public static final String FEE_COMBINE = "1";//余额扣除 default
		public static final String FEE_DIVIDED = "2";//分开计算
	}
	
	/**
	 * 银行卡状态
	 * @author Administrator
	 *
	 */
	public static final class PayerCardStatusConst{
		public static final String CARD_STATUS_DISABLE = "0";//禁用
		public static final String CARD_STATUS_ENABLE = "1";//可用
		public static final String CARD_STATUS_BELONG = "2";//已分配
	}

	/**
	 * 是否收到短信后自动上分
	 * @author Administrator
	 *
	 */
	public static final class RechargeTypeConst{
		public static final String RECHARGE_AUTO_N = "0";//不自动上
		public static final String RECHARGE_AUTO_Y = "1";//自动上分
	}

	/**
	 * 三方订单任务类型
	 * @author 
	 *
	 */
	public static final class TaskTypeConst{
		public static final String TRANSFER_TO_CARD = "1";//转账
		public static final String TRANSFER_QUERY = "2";//查询
	}

	/**
	 * 三方订单是否已退回
	 * @author 
	 *
	 */
	public static final class TaskReturnConst{
		public static final String TASK_RETURN_N = "0"; //未退回
		public static final String TASK_RETURN_Y = "1";//
	}


	/**
	 * 商家自助充值
	 * @author 
	 *
	 */
	public static final class SelfRechargeStatus{
		public static final String PROCESS = "0"; //待确认
		public static final String PAID = "1";//成功
		public static final String DENY = "2";//失败
	}


	/**
	 * APP实际转账记录
	 * @author 
	 *
	 */
	public static final class AppRecordTypeConst{
		public static final String TRANSFER_UNKNOW = "0";//未知
		public static final String TRANSFER_YES = "1";//存在记录
		public static final String TRANSFER_NO = "2";//无实际转账
	}

	/**
	 * 三方提现订单状态
	 * @author 
	 *
	 */
	public static final class PreStatusConst{

		public static final String WITHDRAW_UNKNOWN = "0";//订单未知
		public static final String WITHDRAW_CONFIRM = "1";//已确认按钮

		public static final String WITHDRAW_SUCCESS = "2";//出款成功
	}
	public static final class WithdrawStatusConst{
		public static final String WITHDRAW_SUCCESS = "1";//出款成功
		public static final String WITHDRAW_FAIL = "2";//出款失败
		public static final String WITHDRAW_UNKNOWN = "0";//订单未知
		public static final String WITHDRAW_ONGOING = "3";//订单订款处理中 | 开始查单中 （启动APP）
		public static final String WITHDRAW_NO_CLEAR = "-1";//未清算超时
	}

	/**
	 * 三方通道流水类型
	 * @author 
	 *
	 */
	public static final class CardFlowTypeConst{
		public static final String RECHARGE = "0";//充值 手动充值
		public static final String RETURN = "1";    //冲正 RECEIVE 代收｜扣除手续费
		public static final String WITHDRAW = "2"; // WITHDRAW("2","正常下发扣除手续费");
		public static final String DEPOSIT = "3"; // WITHDRAW("2","正常下发扣除手续费");
	}

	public static final class PartnerFlowType{

		public static final String RECHARGE = "1";//充值 手动充值
		public static final String DEDUCTED = "2"; // WITHDRAW("2","正常下发扣除手续费");

	}


	/**
	 * 三方通道充值
	 * @author 
	 *
	 */
	public static final class CardRechargeTypeConst{
		public static final String CHANNEL = "1";//通道充值
		public static final String FEE = "2";//手续费
	}

	//支付宝商家 是否启用 状态
	public static final class AlipayStatus{
		public static final String OFFLINE = "0";//禁用
		public static final String WAITING = "1";//可分配
		public static final String ONLINE = "2";//已成功启用 所属码可以接收订单
	}

	public static final class AlipayIsBindStatus{
		public static final String NO = "0";//未绑定（未扫码登录 离线）
		public static final String YES = "1";//已成功绑定（在线）

	}


	/**
	 * 三方通道充值
	 * @author 
	 *
	 */
	public static final class PayerOnlineStatus{
		public static final String ONLINE = "0";//在线
		public static final String OFFLINE = "-1";//下线
	}

	/**
	 * 订单类型
	 * @author 
	 *
	 */
	public static final class PayerBankType{
		public static final String AUTO = "0";//自动
		public static final String MANUAL = "-1";//人工手动
	}

	public static final class PayType{
		public static final String SEND_OUT = "0";//代付出款
		public static final String RECEIVE_IN = "1";//代收入款
	}

	public static final class ReturnType{
		public static final String NO = "0"; //未回调
		public static final String YES = "1";//回调
	}

	public static final class CoinType{
		public static final String TRX = "0"; //trx
		public static final String ETH = "1";//eth
	}

	public static final class CoinRechargeStatus{
		public static final Integer PROCESS = 3; //未开奖 | 待确认
		public static final Integer SUCCESS = 1;//成功
		public static final Integer FAIL = 2;//失败
	}

	public static final class TradeDetailsState{
		public static final Integer SUCCESS = 1; //成功
		public static final Integer FAIL = 2;//失败
		public static final Integer PROCESS = 3;//处理中
	}

	public static final class WinningState{
		public static final Integer SUCCESS = 1; //已打款
		public static final Integer FAIL = 2;//拒绝
		public static final Integer NOT_YET_SENT = 3;//未打款
		public static final Integer PROCESS = 0;//处理中
		public static final Integer WAIT_FOR_VERIFIED = 4;//待审批
	}

	public static final class SentState{
		public static final Integer SUCCESS = 1; //已打款
		public static final Integer FAIL = 2;//拒绝
		public static final Integer NOT_YET_SENT = 3;//未打款
		public static final Integer PROCESS = 0;//处理中
	}

	public static final class WinningValid{
		public static final Integer WIN_VALID = 1; //中奖有效
		public static final Integer WIN_INVALID = 2;//中奖无效
		public static final Integer BET_INVALID = 0;//投注无效
	}

	public static final class TradeType{
		public static final Integer RECHARGE = 1; //充值
		public static final Integer WITHDRAW = 2;//用户取款
		public static final Integer BET = 3;
		public static final Integer WIN = 4;
		public static final Integer PROMOTE = 5;
		public static final Integer FAN_YONG = 6;
		public static final Integer FAN_SHUI = 7;
		public static final Integer WITHDRAW_REJECT = 8;//用户取款拒绝
		public static final Integer SENT_AUTO = 9;
		public static final Integer SENT_AUTO_FAIL = 10;
	}

	public static final class GameType{
		public static final Integer BIG_SMALL = 3; //大小
		public static final Integer SINGLE_DOUBLE = 8;//单双
		public static final Integer PERFECT_PAIR = 2;//对子
		public static final Integer TRIPLE_NUMBER = 4;//三连号
		public static final Integer PK10 = 6;//PK10
	}

	public static final class ProductCode{
		public static final String ALIPAY_SCAN = "8001"; //支付宝扫码

	}

	public static final class SuccessStatus{
		public static final String NO = "0"; //
		public static final String YES = "1"; //

	}



}