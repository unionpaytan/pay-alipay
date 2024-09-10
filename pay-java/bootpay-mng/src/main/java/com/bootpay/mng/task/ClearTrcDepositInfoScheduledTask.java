package com.bootpay.mng.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.core.entity.CoinHolderDepositInfo;
import com.bootpay.core.entity.PayDepositInfo;
import com.bootpay.core.entity.PayPartnerDepositInfo;
import com.bootpay.core.service.*;
import com.bootpay.mng.amqueue.AMProducerUtils;
import com.bootpay.mng.service.NotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 定时查询清空超时的 代收充值 订单
 *
 * @author Administrator
 */
@Component
public class ClearTrcDepositInfoScheduledTask {

    private Logger _log = LoggerFactory.getLogger(ClearTrcDepositInfoScheduledTask.class);

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IPayWithdrawInfoService infoService;
    @Autowired
    private IPayPlatformIncomeInfoService incomeInfoService;
    @Autowired
    private AMProducerUtils amProducerUtils;

    @Autowired
    private IPayMerchantInfoService iPayMerchantInfoService; //商家表服务==>PayMerchantInfo

    @Autowired
    private IPayMerchantChannelSiteService iPayMerchantChannelSiteService; //商户通道成本服务

    @Autowired
    private IPayMerchantBalanceService iPayMerchantBalanceService; //商户余额服务

    @Autowired
    private IPayMerchantMoneyBalanceService iPayMerchantMoneyBalanceService; //商户每日统计

    @Autowired
    private ICardPayerWithdrawInfoService iCardPayerWithdrawInfoService; //订单列表

    @Autowired
    private ICoinPayerWithdrawInfoService iCoinPayerWithdrawInfoService;

    @Autowired
    private ICoinHolderDepositInfoService iCoinHolderDepositInfoService; //COIN代收

    @Autowired
    private IPayDepositInfoService iPayDepositInfoService;

    @Autowired
    private IPayPartnerDepositInfoService iPayPartnerDepositInfoService;

    @Autowired
    private NotifyService notifyService;

    /**
     * 1秒（0~59）
     * 2分钟（0~59）
     * 3 小时（0~23）
     * 4天（0~31）
     * 5 月（0~11）
     * 6星期（1~7 1=SUN 或 SUN，MON，TUE，WED，THU，FRI，SAT）
     * 7.年份（1970－2099）
     * 例: 5 48 16 * * ?  5秒 48分 16时 每天
     * TASK任务要捕获异常 若不处理 会卡死 不再执行下一个任务
     */
    @Scheduled(cron = "0/59 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void scheduled() throws Exception {

        int secondsAgo = 60 * 10; //60秒 10分种
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -secondsAgo);

        String previewTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());

        _log.warn("清空支付超时任务==>> 60 * 10 秒前的时间:{}", previewTime);

        Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("createTime", previewTime);       //打款任务10分钟前
        queryParams.put("status", CodeConst.WithdrawStatusConst.WITHDRAW_UNKNOWN); //待支付 WARNING:数据库字段已从 withdrawStatus 改为 status

        List<PayPartnerDepositInfo> onGoingList = iPayPartnerDepositInfoService.selectPartnerDepositInfoExpiredList(queryParams);

        if (onGoingList == null || onGoingList.size() == 0) {
            return;
        }

        for (PayPartnerDepositInfo info : onGoingList) {
            //1.修改订单状态为 失败
            PayPartnerDepositInfo updEntity = new PayPartnerDepositInfo();
            updEntity.setStatus(CodeConst.WithdrawStatusConst.WITHDRAW_NO_CLEAR); //更新为支付超时
            updEntity.setRemark("支付超时");
            updEntity.setEndTime(new Date());
            iPayPartnerDepositInfoService.update(updEntity, new UpdateWrapper<PayPartnerDepositInfo>().lambda()
                    .eq(PayPartnerDepositInfo::getWithdrawNo, info.getWithdrawNo()));
        }


    }
}
