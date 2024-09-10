package com.bootpay.channel.vo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 
 * @author Administrator
 *
 */
public class ChannelResultVo {

	/**
	 * 因为使用商户号和商户代付订单号建立了索引 所以使用这两个进行订单修改和查询
	 * 商户代付订单号
	 */
	private String merchantWihtdrawNo;
	
	/**
	 * 平台商户号
	 */
	// merchant_id
	private String merchantId;
	
	/**
	 * 平台订单号
	 */
	private String withdrawNo;

	/**
	 * 渠道订单号
	 * */
	private String channelWithdrawNo;

	/**
	 * 提现状态
	 */
	private String withdrawStatus;


	/**
	 * 提现备注
	 */
	private String withdrawRemark;
	
	/**
	 * 代付渠道｜通道编号
	 */
	private String channelCode;
	
	/**
	 * 平台利润
	 */
	private BigDecimal platformProfit;
	
	/**
	 * 代付订单创建时间
	 */
	private Date createTime;
	
	/**
	 * 订单来源
	 */
	private String requestType;
	
	/**
	 * 是否发送通知消息
	 */
	private boolean isNotify;
	
	
	/**
	 * 提现金额
	 */
	private BigDecimal withdrawAmt;
	
	/**
	 * 单笔提现手续费
	 */
	private BigDecimal merchantFee;

	/**
	 * 提现手续费/费率
	 */
	private BigDecimal merchantRateFee;

	/**
	 * 矿工费
	 * */
	private BigDecimal mintFee;

	public String getMerchantWihtdrawNo() {
		return merchantWihtdrawNo;
	}

	public void setMerchantWihtdrawNo(String merchantWihtdrawNo) {
		this.merchantWihtdrawNo = merchantWihtdrawNo;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getWithdrawNo() {
		return withdrawNo;
	}

	public void setWithdrawNo(String withdrawNo) {
		this.withdrawNo = withdrawNo;
	}

	public String getChannelWithdrawNo() {
		return channelWithdrawNo;
	}

	public void setChannelWithdrawNo(String channelWithdrawNo) {
		this.channelWithdrawNo = channelWithdrawNo;
	}

	public String getWithdrawStatus() {
		return withdrawStatus;
	}

	public void setWithdrawStatus(String withdrawStatus) {
		this.withdrawStatus = withdrawStatus;
	}

	public String getWithdrawRemark() {
		return withdrawRemark;
	}

	public void setWithdrawRemark(String withdrawRemark) {
		this.withdrawRemark = withdrawRemark;
	}

	public String getChannelCode() {
		return channelCode;
	}

	public void setChannelCode(String channelCode) {
		this.channelCode = channelCode;
	}

	public BigDecimal getPlatformProfit() {
		return platformProfit;
	}

	public void setPlatformProfit(BigDecimal platformProfit) {
		this.platformProfit = platformProfit;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public boolean isNotify() {
		return isNotify;
	}

	public void setNotify(boolean notify) {
		isNotify = notify;
	}

	public BigDecimal getWithdrawAmt() {
		return withdrawAmt;
	}

	public void setWithdrawAmt(BigDecimal withdrawAmt) {
		this.withdrawAmt = withdrawAmt;
	}

	public BigDecimal getMerchantFee() {
		return merchantFee;
	}

	public void setMerchantFee(BigDecimal merchantFee) {
		this.merchantFee = merchantFee;
	}

	public BigDecimal getMerchantRateFee() {
		return merchantRateFee;
	}

	public void setMerchantRateFee(BigDecimal merchantRateFee) {
		this.merchantRateFee = merchantRateFee;
	}

	public BigDecimal getMintFee() {
		return mintFee;
	}

	public void setMintFee(BigDecimal mintFee) {
		this.mintFee = mintFee;
	}

	@Override
	public String toString() {
		return "ChannelResultVo{" +
				"merchantWihtdrawNo='" + merchantWihtdrawNo + '\'' +
				", merchantId='" + merchantId + '\'' +
				", withdrawNo='" + withdrawNo + '\'' +
				", channelWithdrawNo='" + channelWithdrawNo + '\'' +
				", withdrawStatus='" + withdrawStatus + '\'' +
				", withdrawRemark='" + withdrawRemark + '\'' +
				", channelCode='" + channelCode + '\'' +
				", platformProfit=" + platformProfit +
				", createTime=" + createTime +
				", requestType='" + requestType + '\'' +
				", isNotify=" + isNotify +
				", withdrawAmt=" + withdrawAmt +
				", merchantFee=" + merchantFee +
				", merchantRateFee=" + merchantRateFee +
				", mintFee=" + mintFee +
				'}';
	}
}
