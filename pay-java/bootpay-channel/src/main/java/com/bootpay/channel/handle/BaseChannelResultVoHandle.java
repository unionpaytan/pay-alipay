package com.bootpay.channel.handle;

import org.apache.commons.lang.StringUtils;

import com.bootpay.channel.vo.ChannelResultVo;
import com.bootpay.core.entity.PayWithdrawInfo;


public class BaseChannelResultVoHandle {
	/**
	 * 通道查询通用对象
	 * @param withdrawInfo
	 * @return
	 */
	public ChannelResultVo getChannelResultVo(PayWithdrawInfo withdrawInfo){
		ChannelResultVo channelResultVo = new ChannelResultVo();
		channelResultVo.setChannelCode(withdrawInfo.getChannelCode());
		channelResultVo.setMerchantId(withdrawInfo.getMerchantId());
		channelResultVo.setMerchantWihtdrawNo(withdrawInfo.getMerchantWithdrawNo());
		channelResultVo.setCreateTime(withdrawInfo.getCreateTime());
		channelResultVo.setPlatformProfit(withdrawInfo.getPlatformProfit());
		channelResultVo.setWithdrawNo(withdrawInfo.getWithdrawNo());
		channelResultVo.setWithdrawStatus(withdrawInfo.getWithdrawStatus());
		channelResultVo.setWithdrawRemark(withdrawInfo.getRemark());
		channelResultVo.setMerchantFee(withdrawInfo.getMerchantFee());
		channelResultVo.setRequestType(withdrawInfo.getRequestType());
		channelResultVo.setWithdrawAmt(withdrawInfo.getAmt());
		channelResultVo.setNotify(StringUtils.isNotBlank(withdrawInfo.getMerchantNotifyUrl()));
		return channelResultVo;
	}

}
