package com.bootpay.channel.handle;

import com.bootpay.channel.vo.ChannelResultVo;
import com.bootpay.core.entity.PayWithdrawInfo;

/**
 * 代付通道实现接口
 * @author Administrator
 *
 */
public interface  BaseWithdrawHandle {

	/**
	 * 发起提现
	 * @param info
	 * @return
	 * @throws Exception
	 */
	public ChannelResultVo withdraw(PayWithdrawInfo info,String channelCode)throws Exception;

	/**
	 * 查询提现
	 * @param channelResultVo
	 * @return
	 * @throws Exception
	 */
	public ChannelResultVo queryWithdraw(ChannelResultVo channelResultVo,String channelCode)throws Exception;
}
