package com.bootpay.mng.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bootpay.common.authenticator.GoogleAuthenticator;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.CodeConst.GoogleConst;
import com.bootpay.common.constants.enums.EnumAccountType;
import com.bootpay.common.constants.enums.EnumChannelStatus;
import com.bootpay.common.constants.enums.EnumFlowType;
import com.bootpay.common.constants.enums.EnumMerchantStatusType;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.AvoidDuplicateFormToken;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.*;
import com.bootpay.core.entity.*;
import com.bootpay.core.service.*;
import com.bootpay.mng.service.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;

/**
 * <p>
 * 币址通道管理
 * </p>
 *
 * @author 
 * @since 2021-07-30
 */
@Validated
@RestController
@RequestMapping("/coin")
public class CoinChannelInfoController {

    private Logger _log = LoggerFactory.getLogger(CoinChannelInfoController.class);

    @Value("${app.privateAESKey}") //application.yml文件读取
    private String privateAESKey;

    @Autowired
    private AuthenticatorService authenticatorService;

    @Autowired
    private ICardChannelInfoService iCardChannelInfoService; //卡通道


    @Autowired
    private ICardChannelFlowInfoService iCardChannelFlowInfoService; //卡通道明细

    @Autowired
    private CardChannelManagerService cardChannelManagerService;

    @Autowired
    private CoinChannelManagerService coinChannelManagerService; //币址

    @Autowired
    private IPayMerchantInfoService iPayMerchantInfoService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ICoinChannelWithdrawInfoService iCoinChannelWithdrawInfoService; //COIN代付

    @Autowired
    private ICoinChannelDepositInfoService iCoinChannelDepositInfoService; //收

    @Autowired
    private ICoinChannelWithdrawFlowService iCoinChannelWithdrawFlowService; //COIN代付


    @Autowired
    private ICoinPayerWithdrawInfoService iCoinPayerWithdrawInfoService; //COIN代付

    @Autowired
    private ICoinPayerWithdrawFlowService iCoinPayerWithdrawFlowService;

    @Autowired
    private ICoinPayerInfoService iCoinPayerInfoService;

    @Autowired
    private CoinWithdrawManagerService coinWithdrawManagerService;

    @Autowired
    private MerchantAccountManagerService merchantAccountManagerService;


    @Autowired
    private IPayMerchantChannelSiteService iPayMerchantChannelSiteService;

    @Autowired
    private ICoinHolderInfoService iCoinHolderInfoService;

    @Autowired
    private ICoinHolderDepositInfoService iCoinHolderDepositInfoService; //COIN代收

    @Autowired
    private IPayMerchantBalanceService iPayMerchantBalanceService;

    @Autowired
    private IPayAgentChannelSiteService iPayAgentChannelSiteService; //代理通道

    @Autowired
    private IPayMerchantMoneyChangeService iPayMerchantMoneyChangeService; //商户流水明细

    @Value("${app.walletPrivateKeySalt}") //application.yml文件读取
    private String walletPrivateKeySalt;

    /**
     * 代付通道
     *
     * @return ReturnMsg
     * @throws TranException
     */
    @RequestMapping(value = "/registerCoinWithdrawChannel", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    public ReturnMsg registerCoinWithdrawChannel(
            @NotNull(message = "通道汇率不能为空") BigDecimal channelRate,
            @NotNull(message = "下发手续费不能为空") BigDecimal channelSingle,
            @NotNull(message = "通道名称不能为空") String channelName,
            String email,
            String phone,
            String contact,
            String status,
            String remark) throws Exception {

        CoinChannelWithdrawInfo coinChannelWithdrawInfo = iCoinChannelWithdrawInfoService.getOne(new QueryWrapper<CoinChannelWithdrawInfo>().lambda().eq(CoinChannelWithdrawInfo::getChannelName, channelName));
        if (coinChannelWithdrawInfo != null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "通道名称重复");
        }
        //创建币址通道唯一账户
        String channelCode = MySeq.getSeqCoinNo();

        String apiKeyEncrypt =  EncryptUtil.aesEncrypt(MySeq.getAPIkey(),walletPrivateKeySalt);

        CoinChannelWithdrawInfo info = new CoinChannelWithdrawInfo();
        info.setChannelCode(channelCode);
        info.setChannelName(channelName);
        info.setChannelRate(channelRate);
        info.setChannelSingle(channelSingle);
        info.setEmail(email);
        info.setPhone(phone);
        info.setContact(contact);
        //info.setCreateTime(new Date());
        info.setStatus(CodeConst.CoinChannelStatus.ENABLE);

        info.setApiKey(apiKeyEncrypt); //设置通道对接api key
        info.setRemark(HtmlUtil.delHTMLTag(remark)); //java过滤特殊字符

        iCoinChannelWithdrawInfoService.save(info);

        return new ReturnMsg();
    }


    /**
     * 三方银行卡通道修改
     *
     * @return ReturnMsg
     * @throws TranException
     */
    @RequestMapping(value = "/editCoinWithdrawChannel", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    public ReturnMsg editCoinWithdrawChannel(
            @NotNull(message = "通道编号不能为空") String channelCode,
            @NotNull(message = "通道名称不能为空") String channelName,
            @NotNull(message = "通道汇率不能为空") BigDecimal channelRate,
            @NotNull(message = "下发手续费不能为空") BigDecimal channelSingle,
            String email,
            String phone,
            String contact,
            String remark) throws TranException {

        CoinChannelWithdrawInfo info = iCoinChannelWithdrawInfoService.getOne(new QueryWrapper<CoinChannelWithdrawInfo>().lambda().eq(CoinChannelWithdrawInfo::getChannelCode, channelCode));
        if (info == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "通道编号不存在");
        }


        CoinChannelWithdrawInfo coinEntity = new CoinChannelWithdrawInfo();

        coinEntity.setChannelName(channelName);
        coinEntity.setChannelRate(channelRate);
        coinEntity.setChannelSingle(channelSingle);
        coinEntity.setEmail(email);
        coinEntity.setPhone(phone);
        coinEntity.setContact(contact);
        coinEntity.setRemark(HtmlUtil.delHTMLTag(remark)); //java过滤特殊字符

        //Mybatis-Plus 更新语句
        boolean isUpdate = iCoinChannelWithdrawInfoService.update(
                coinEntity,
                new UpdateWrapper<CoinChannelWithdrawInfo>().lambda().eq(CoinChannelWithdrawInfo::getChannelCode, channelCode));

        if (!isUpdate) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "通道修改有误");
        }
        return new ReturnMsg();

    }


    /**
     * 代收通道
     *
     * @return ReturnMsg
     * @throws TranException
     */
    @RequestMapping(value = "/registerCoinDepositChannel", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    public ReturnMsg registerCoinDepositChannel(
            @NotNull(message = "通道汇率不能为空") BigDecimal channelRate,
            @NotNull(message = "充值手续费不能为空") BigDecimal channelSingle,
            @NotNull(message = "通道名称不能为空") String channelName,
            String usdtLiveFixed,
            BigDecimal usdtLive,
            String email,
            String phone,
            String contact,
            String status,
            String remark) throws Exception {

        CoinChannelDepositInfo coinChannelDepositInfo = iCoinChannelDepositInfoService.getOne(new QueryWrapper<CoinChannelDepositInfo>().lambda().eq(CoinChannelDepositInfo::getChannelName, channelName));
        if (coinChannelDepositInfo != null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "通道名称重复");
        }
        //创建币址通道唯一账户
        String channelCode = MySeq.getSeqCoinNo();

        String apiKeyEncrypt =  EncryptUtil.aesEncrypt(MySeq.getAPIkey(),walletPrivateKeySalt);
        CoinChannelDepositInfo info = new CoinChannelDepositInfo();
        info.setChannelCode(channelCode);
        info.setChannelName(channelName);
        info.setChannelRate(channelRate);
        info.setChannelSingle(channelSingle);
        info.setUsdtLiveFixed(usdtLiveFixed);
        info.setUsdtLive(usdtLive);
        info.setEmail(email);
        info.setPhone(phone);
        info.setContact(contact);
        info.setCreateTime(new Date());
        info.setStatus(CodeConst.CoinChannelStatus.ENABLE);

        info.setApiKey(apiKeyEncrypt); //设置通道对接api key
        info.setRemark(HtmlUtil.delHTMLTag(remark)); //java过滤特殊字符

        iCoinChannelDepositInfoService.save(info);
//
//		//初始化商户余额
//		PayMerchantBalance merchantBalance = new PayMerchantBalance();
//		merchantBalance.setMerchantId(merchantId);
//		merchantBalance.setFreezeBalance(new BigDecimal("0.00"));
//		merchantBalance.setAccountBalance(new BigDecimal("0.00"));
//		iPayMerchantBalanceService.save(merchantBalance);
        return new ReturnMsg();
    }

    @RequestMapping(value = "/editCoinDepositChannel", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    public ReturnMsg editCoinDepositChannel(
            @NotNull(message = "通道编号不能为空") String channelCode,
            @NotNull(message = "通道名称不能为空") String channelName,
            @NotNull(message = "通道汇率不能为空") BigDecimal channelRate,
            @NotNull(message = "下发手续费不能为空") BigDecimal channelSingle,
            String usdtLiveFixed,
            BigDecimal usdtLive,
            String email,
            String phone,
            String contact,
            String remark) throws TranException {

        CoinChannelDepositInfo info = iCoinChannelDepositInfoService.getOne(new QueryWrapper<CoinChannelDepositInfo>().lambda().eq(CoinChannelDepositInfo::getChannelCode, channelCode));
        if (info == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "通道编号不存在");
        }


        CoinChannelDepositInfo coinEntity = new CoinChannelDepositInfo();

        coinEntity.setChannelName(channelName);
        coinEntity.setChannelRate(channelRate);
        coinEntity.setChannelSingle(channelSingle);
        coinEntity.setUsdtLiveFixed(usdtLiveFixed);
        coinEntity.setUsdtLive(usdtLive);
        coinEntity.setEmail(email);
        coinEntity.setPhone(phone);
        coinEntity.setContact(contact);
        coinEntity.setRemark(HtmlUtil.delHTMLTag(remark)); //java过滤特殊字符

        //Mybatis-Plus 更新语句
        boolean isUpdate = iCoinChannelDepositInfoService.update(
                coinEntity,
                new UpdateWrapper<CoinChannelDepositInfo>().lambda().eq(CoinChannelDepositInfo::getChannelCode, channelCode));

        if (!isUpdate) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "通道修改有误");
        }
        return new ReturnMsg();

    }


    /**
     * 代收通道查询
     *
     * @param
     * @param
     * @param rows
     * @param page
     * @return
     */
    @RequestMapping(value = "/queryCoinWithdrawChannelList", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    public ReturnMsg queryCoinWithdrawChannelList(
            String channelCode,
            String channelName,
            String status,
            Integer rows,
            Integer page) {

        Page<Map<String, Object>> pages = new Page<>(page, rows);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("channelCode", channelCode);
        params.put("status", status); //chlStatus为参数
        params.put("channelName", channelName);

        IPage<Map<String, Object>> pageMap = iCoinChannelWithdrawInfoService.queryCoinChannelWithdrawInfoList(pages, params);

        Map<String, Object> countMap = iCoinChannelWithdrawInfoService.countCoinChannelWithdrawBalance(params); //@Param("params")

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("pages", pageMap);
        resultMap.put("countMap", countMap);
        return new ReturnMsg(resultMap);

    }


    /**
     * 代收通道查询
     *
     * @param
     * @param
     * @param rows
     * @param page
     * @return
     */
    @RequestMapping(value = "/queryCoinDepositChannelList", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    public ReturnMsg queryCoinDepositChannelList(
            String channelCode,
            String channelName,
            String status,
            Integer rows,
            Integer page) {

        Page<Map<String, Object>> pages = new Page<>(page, rows);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("channelCode", channelCode);
        params.put("status", status); //chlStatus为参数
        params.put("channelName", channelName);

        IPage<Map<String, Object>> pageMap = iCoinChannelDepositInfoService.queryCoinChannelDepositInfoList(pages, params);

        //Map<String, Object> countMap = iCoinChannelDepositInfoService.countCoinChannelDepositBalance(params); //代收不用手续费

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("pages", pageMap);
        //resultMap.put("countMap", countMap);
        resultMap.put("countMap", '-');
        return new ReturnMsg(resultMap);

    }

    /**
     * 删除卡通道信息
     * cardChannelCode 卡通道编号
     *
     * @return
     * @throws TranException
     */
    @RequestMapping(value = "/delCoinWithdrawChannel", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    public ReturnMsg delCoinWithdrawChannel(
            @NotNull(message = "通道编号不能为空") String channelCode,
            @NotNull(message = "管理员ID不能为空") String tableMerchantId,
            String googleCode) throws Exception {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

        if (googleCode == null || "".equals(googleCode)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "google验证码错误");
        }

        String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
        if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
        }

        iCoinChannelWithdrawInfoService.remove(new QueryWrapper<CoinChannelWithdrawInfo>().lambda().eq(CoinChannelWithdrawInfo::getChannelCode, channelCode));
        return new ReturnMsg();

    }


    @RequestMapping(value = "/delCoinDepositChannel", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    public ReturnMsg delCoinDepositChannel(
            @NotNull(message = "通道编号不能为空") String channelCode,
            @NotNull(message = "管理员ID不能为空") String tableMerchantId,
            String googleCode) throws Exception {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

        if (googleCode == null || "".equals(googleCode)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "google验证码错误");
        }

        String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
        if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
        }

        iCoinChannelDepositInfoService.remove(new QueryWrapper<CoinChannelDepositInfo>().lambda().eq(CoinChannelDepositInfo::getChannelCode, channelCode));
        return new ReturnMsg();

    }

    /**
     * 批量修改卡通道状态
     *
     * @param
     * @param
     * @param
     * @param
     * @return
     */
    @RequestMapping(value = "/batchModifyCoinWithdrawChannelInfoStatus", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    public ReturnMsg batchModifyCoinWithdrawChannelInfoStatus(
            @NotNull(message = "请选择通道") String channelCodes,
            @NotNull(message = "设置状态不能为空") String status
    ) throws TranException {
        List<String> channelCodesList = Arrays.asList(channelCodes.split(","));
        CoinChannelWithdrawInfo entity = new CoinChannelWithdrawInfo();
        entity.setStatus(status);
        iCoinChannelWithdrawInfoService.update(entity, new QueryWrapper<CoinChannelWithdrawInfo>().lambda().in(CoinChannelWithdrawInfo::getChannelCode, channelCodesList));
        return new ReturnMsg();
    }

    /**
     * 修改coin通道状态
     *
     * @param
     * @return
     * @throws TranException
     */
    @RequestMapping(value = "/modifyCoinWithdrawChannelStatus", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    public ReturnMsg modifyCoinWithdrawChannelStatus(
            @NotNull(message = "通道编号不能为空") String channelCode,
            @NotNull(message = "请选择通道状态") String status
    ) throws TranException {

        CoinChannelWithdrawInfo infoEntity = new CoinChannelWithdrawInfo();
        infoEntity.setStatus(status);
        iCoinChannelWithdrawInfoService.update(infoEntity, new UpdateWrapper<CoinChannelWithdrawInfo>().lambda().eq(CoinChannelWithdrawInfo::getChannelCode, channelCode));
        return new ReturnMsg();
    }

    @RequestMapping(value = "/modifyCoinDepositChannelStatus", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    public ReturnMsg modifyCoinDepositChannelStatus(
            @NotNull(message = "通道编号不能为空") String channelCode,
            @NotNull(message = "请选择通道状态") String status
    ) throws TranException {

        CoinChannelDepositInfo infoEntity = new CoinChannelDepositInfo();
        infoEntity.setStatus(status);
        iCoinChannelDepositInfoService.update(infoEntity, new UpdateWrapper<CoinChannelDepositInfo>().lambda().eq(CoinChannelDepositInfo::getChannelCode, channelCode));
        return new ReturnMsg();
    }


    /**
     * 获取充值流水列表
     *
     * @param
     * @param rows 条数
     * @param page 页码
     * @return
     */
    @RequestMapping(value = "/queryCoinChannelFlowPage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryCoinChannelFlowPage(
            String withdrawNo, //四方流水号
            String channelWithdrawNo, //通道单号
            String channelCode,
            String flowType,
            String rechargeType,
            String remark,
            String beginTime,
            String endTime,
            String merchantId,
            String tableMerchantId,
            @NotNull(message = "行数不能为空") Integer rows,
            @NotNull(message = "页码不能为空") Integer page,
            HttpServletRequest request) throws TranException {

        if (tableMerchantId == null || "".equals(tableMerchantId)) {
            throw new TranException(CodeConst.PERMISSION_CODE, "商户编号为空");
        }

        if (merchantId == null || "".equals(merchantId)) { //只有管理员能查询所有商户的手续费流水
            PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                throw new TranException(CodeConst.PERMISSION_CODE, "无权访问");
            }
        }
        /**
         Page<CoinChannelWithdrawFlow> pages = new Page<>(page, rows);
         QueryWrapper<CoinChannelWithdrawFlow> queryWrapper = new QueryWrapper<>();
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("MERCHANT_ID", merchantId);
         params.put("CHANNEL_CODE", channelCode);
         params.put("FLOW_TYPE", flowType);
         params.put("WITHDRAW_NO", withdrawNo);
         params.put("CHANNEL_WITHDRAW_NO", channelWithdrawNo);
         queryWrapper = queryWrapper.allEq(params, false)
         .like(StringUtils.isNotBlank(remark), "REMARK", HtmlUtil.delHTMLTag(remark))
         .ge(StringUtils.isNotBlank(beginTime), "CREATE_TIME", beginTime + " 00:00:00")
         .le(StringUtils.isNotBlank(endTime), "CREATE_TIME", endTime + " 23:59:59");
         queryWrapper.orderByDesc("ID");
         IPage<CoinChannelWithdrawFlow> pageMap = iCoinChannelWithdrawFlowService.page(pages, queryWrapper);
         *
         * */
        Page<Map<String, Object>> pages = new Page<>(page, rows);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId", merchantId);
        params.put("channelCode", channelCode);
        params.put("flowType", flowType);
        params.put("withdrawNo", withdrawNo);
        params.put("channelWithdrawNo", channelWithdrawNo);
        params.put("beginTime", beginTime + " 00:00:00");
        params.put("endTime", endTime + " 23:59:59");
        params.put("remark", remark);
        IPage<Map<String, Object>> pageMap = iCoinChannelWithdrawFlowService.queryChannelWithdrawFlowPage(pages, params);

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("pages", pageMap);
        return new ReturnMsg(resultMap);

    }

    /**
     * 获取三方通道
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/queryCardChannelCodeDataList", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryCardChannelCodeDataList() {
        QueryWrapper<CardChannelInfo> queryWrapper = new QueryWrapper<CardChannelInfo>();
        queryWrapper.select("CHANNEL_NAME as cardChannelName", "CARD_CHANNEL_CODE as cardChannelCode", "REMARK as remark");
        queryWrapper.lambda().orderByAsc(CardChannelInfo::getChlName);//按名称排序;
        List<Map<String, Object>> list = iCardChannelInfoService.listMaps(queryWrapper);
        return new ReturnMsg(list);
    }

    /**
     * 获取通道汇率
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/queryRateByCardChannelCode", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryRateByCardChannelCode(String cardChannelCode) {
        QueryWrapper<CardChannelInfo> queryWrapper = new QueryWrapper<CardChannelInfo>();
        queryWrapper.select("CHANNEL_RATE as chlRate", "CHANNEL_SINGLE as chlSingle");
        queryWrapper.lambda().eq(CardChannelInfo::getCardChannelCode, cardChannelCode);

        CardChannelInfo channelInfo = iCardChannelInfoService.getOne(queryWrapper);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("chlRate", channelInfo.getChlRate());
        resultMap.put("chlSingle", channelInfo.getChlSingle());

        return new ReturnMsg(resultMap);
    }


    /**
     * @手续费汇总
     */
    @RequestMapping(value = "/querySumCoinPayer", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg querySumCoinPayer(
            String withdrawNo, //四方流水号(平台)
            String channelWithdrawNo, //通道单号
            String channelCode, //通道号
            String flowType,//手续费流水类型
            String remark,
            String beginTime,
            String endTime,
            String merchantId,
            String tableMerchantId,
            HttpServletRequest request) throws TranException {

        if (tableMerchantId == null || "".equals(tableMerchantId)) {
            throw new TranException(CodeConst.PERMISSION_CODE, "商户编号为空");
        }

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));


        //1.通道剩余手续费 --> 改为查询商户余额手续费
        Map<String, Object> paramsSum = new HashMap<String, Object>();
        paramsSum.put("merchantId", merchantId);
        BigDecimal sumPayerRemainFee = iPayMerchantBalanceService.sumPayerRemainFee(paramsSum);

        //2统计代付交易订单
        Map<String, Object> paramsSearch = new HashMap<String, Object>();
        //paramsSearch.put("merchantId", merchantId);
        paramsSearch.put("beginTime", beginTime);
        paramsSearch.put("endTime", endTime);

        //如果是商户 必须带通道号
        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
            paramsSearch.put("channelCode", merchantInfo.getChannelCode());
        } else {
            paramsSearch.put("channelCode", channelCode);
        }


        paramsSearch.put("status", ""); //下发手续费不受订单状态选项的影响 (手续费先扣)
        Map<String, Object> sumAllWithdrawFeeMap = iCoinPayerWithdrawInfoService.sumCoinPayerWithdrawFee(paramsSearch);

        paramsSearch.put("status", CodeConst.WithdrawStatusConst.WITHDRAW_FAIL);//失败的手续费
        Map<String, Object> sumFailWithdrawFeeMap = iCoinPayerWithdrawInfoService.sumCoinPayerWithdrawFee(paramsSearch);

        BigDecimal sumPayerSingleFee = new BigDecimal(sumAllWithdrawFeeMap.get("sumPayerSingleFee").toString());
        BigDecimal sumPayerRateFee = new BigDecimal(sumAllWithdrawFeeMap.get("sumPayerRateFee").toString());

        BigDecimal sumPayerFailRateFee = new BigDecimal(sumFailWithdrawFeeMap.get("sumPayerRateFee").toString());
        BigDecimal sumPayerFailSingleFee = new BigDecimal(sumFailWithdrawFeeMap.get("sumPayerSingleFee").toString());

        //3.代收 总代收手续费+总代收单笔手续费
        paramsSearch.remove("status");
        paramsSearch.remove("channelCode");

        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
            paramsSearch.put("channelCode", merchantInfo.getDepositChannelCode());
        } else {
            paramsSearch.put("channelCode", channelCode);
        }

        paramsSearch.put("status", CodeConst.WithdrawStatusConst.WITHDRAW_SUCCESS);//代收成功的手续费
        Map<String, Object> sumAllDepositMap = iCoinHolderDepositInfoService.sumCoinHolderDepositFee(paramsSearch);

        paramsSearch.put("flowType", EnumFlowType.RECHARGE.getName());//充值
        paramsSearch.put("merchantId", merchantId);// 哪个商户充值的ID

        //4.充值手续费
        BigDecimal sumRechargeFee = iCoinChannelWithdrawFlowService.sumRechargeFee(paramsSearch);

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("sumPayerRemainFee", sumPayerRemainFee); //商户剩余手续费

        resultMap.put("sumPayerWithdrawSingleFee", sumPayerSingleFee.subtract(sumPayerFailSingleFee)); //总下发(单笔)手续费 - 失败的(单笔)手续费
        resultMap.put("sumPayerWithdrawRateFee", sumPayerRateFee.subtract(sumPayerFailRateFee)); //总下发手续费 - 失败的

        resultMap.put("sumPayerSingleFee", sumAllDepositMap.get("sumPayerSingleFee").toString()); //总代收单笔
        resultMap.put("sumPayerRateFee", sumAllDepositMap.get("sumPayerRateFee").toString()); //总代收费率

        resultMap.put("sumRechargeFee", String.valueOf(sumRechargeFee));


        return new ReturnMsg(resultMap);

    }


    /**
     * 获取三方可用通道
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/queryCoinChannelWithdrawInfo", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryCoinChannelWithdrawInfo() {
        QueryWrapper<CoinChannelWithdrawInfo> queryWrapper = new QueryWrapper<CoinChannelWithdrawInfo>();
        queryWrapper.select("CHANNEL_CODE as channelCode", "CHANNEL_NAME as channelName", "REMARK as remark");
        queryWrapper.lambda()
                .eq(CoinChannelWithdrawInfo::getStatus, EnumChannelStatus.ENABLE.getName()).orderByAsc(CoinChannelWithdrawInfo::getChannelName);
        List<Map<String, Object>> list = iCoinChannelWithdrawInfoService.listMaps(queryWrapper);
        return new ReturnMsg(list);
    }


    /**
     * 查询代付钱包
     *
     * @return
     */
    @RequestMapping(value = "/queryCoinPayerInfoPage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryCoinPayerInfoPage(
            String channelCode,
            String coinType,
            String payerName,
            String payerAddr,
            String status,
            String remark,
            String tableMerchantId,
            Integer rows,
            Integer page,
            HttpServletRequest request) throws TranException {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
        PayMerchantChannelSite payMerchantChannelSite = iPayMerchantChannelSiteService.getOne(
                new QueryWrapper<PayMerchantChannelSite>().lambda()
                        .eq(PayMerchantChannelSite::getMerchantId, tableMerchantId)
                        .eq(PayMerchantChannelSite::getPayType, CodeConst.PayType.SEND_OUT)
        );

        String hasChannelCode = merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName()) ? payMerchantChannelSite.getChannelCode() : channelCode;

        if ("".equals(hasChannelCode) || hasChannelCode == null) {

            String token = request.getHeader("authorization");
            if (!authenticatorService.checkPermission(token).equals("admin")) {
                throw new TranException(CodeConst.PERMISSION_CODE, "无权访问");
            }

            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
            }
        }

        Page<Map<String, Object>> pages = new Page<>(page, rows);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("channelCode", hasChannelCode);
        params.put("coinType", coinType);
        params.put("payerName", payerName); //钱包名称
        params.put("payerAddr", payerAddr); //钱包名称
        params.put("status", status);
        params.put("remark", HtmlUtil.delHTMLTag(remark));
        params.put("date", DateUtil.getDate());
        params.put("month", DateUtil.getMonth());//当月

        IPage<Map<String, Object>> pageMap = iCoinPayerInfoService.queryCoinPayerInfoPage(pages, params);


        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("pages", pageMap);
        return new ReturnMsg(resultMap);

    }

    /**
     * 注册代付钱包
     */
    @RequestMapping(value = "/registerCoinPayer", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    public ReturnMsg registerCoinPayer(
            String channelCode,
            @NotNull(message = "钱包名称不能为空") String payerName,
            @NotNull(message = "钱包地址不能为空") String payerAddr,
            String coinType,
            String payerLimitDay,
            String payerLimitMonth,
            @NotNull(message = "钱包状态不能为空") String status,
            @NotNull(message = "登录密码不能为空") String loginPwd,
            @NotNull(message = "支付密码不能为空") String payPwd,
            @NotNull(message = "设备ID不能为空") String deviceId,
            @NotNull(message = "设备IMEI不能为空") String deviceImei,
            @NotNull(message = "硬件系列号不能为空") String deviceSerial,
            String privateKey,
            String remark,
            String merchantId,
            String tableMerchantId) throws Exception {

        //privateKey -- 私钥暂时不通过网上添加

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

//        if (!"0".equals(payerAddr.substring(0, 1)) && !"9".equals(payerAddr.substring(0, 1))) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "[" + payerAddr + "]非正确的GCash钱包");
//        }
//
//        if (!"10".equals(String.valueOf(payerAddr.length())) && !"11".equals(String.valueOf(payerAddr.length()))) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "[" + payerAddr + "]非正确的GCash钱包");
//        }
//
//        if ("10".equals(String.valueOf(payerAddr.length()))) {
//            payerAddr = "0".concat(payerAddr);//前位数补0
//        }
        //银行卡
        CoinPayerInfo iCoinPayerInfoServiceOne = iCoinPayerInfoService.getOne(new QueryWrapper<CoinPayerInfo>().lambda().eq(CoinPayerInfo::getPayerAddr, payerAddr.trim()));
        if (iCoinPayerInfoServiceOne != null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "钱包地址重复");
        }

//        String tronUrl = "https://api.trongrid.io/v1/accounts/"+payerAddr;
//        String serverResult = HttpClient4Utils.sendHttpRequest(tronUrl,null,"utf-8",false);
//        if (serverResult == null){
//            _log.error("invalid trx address - " + payerAddr);
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "["+payerAddr+"]非正确的TRX地址");
//        }

        //获取通道名称
        String channelName = "";
        if (!"".equals(channelCode) && channelCode != null) {
            CoinChannelWithdrawInfo channelInfo = iCoinChannelWithdrawInfoService.getOne(new QueryWrapper<CoinChannelWithdrawInfo>()
                    .lambda().eq(CoinChannelWithdrawInfo::getChannelCode, channelCode));
            channelName = channelInfo.getChannelName();
        }

        CoinPayerInfo info = new CoinPayerInfo();

        info.setChannelCode(channelCode);
        info.setChannelName(channelName);

        info.setMerchantId(merchantId); //所属卡主
        info.setPayerName(payerName);
        info.setPayerAddr(payerAddr.trim());

        info.setCoinType(coinType.trim());
        info.setStatus(status.trim()); //卡号数字串

        info.setPayerLimitDay(new BigDecimal(payerLimitDay)); //日限额
        info.setPayerLimitMonth(new BigDecimal(payerLimitMonth)); //月付出限额

        info.setLoginPwd(EncryptUtil.aesEncrypt(loginPwd, privateAESKey)); //加aes存入数据库
        info.setPayPwd(EncryptUtil.aesEncrypt(payPwd, privateAESKey)); //aesEn


        info.setDeviceId(deviceId);
        info.setDeviceImei(deviceImei);
        info.setDeviceSerial(deviceSerial);
        info.setCreateTime(new Date());
        info.setDate(DateUtil.getDate());
        info.setRemark(HtmlUtil.delHTMLTag(remark)); //java过滤特殊字符
        iCoinPayerInfoService.save(info);

        //新增通道卡数量 (没必要 ,直接用查询即可)
        //iCardChannelInfoService.updateCardChannel(cardChannelCode, new BigDecimal(0), new BigDecimal(0), CodeConst.CardRechargeTypeConst.CHANNEL, 1, null);

        return new ReturnMsg();
    }


    @RequestMapping(value = "/delCoinPayerInfo", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg delCoinPayerInfo(
            @NotNull(message = "钱包地址不能为空") String payerAddr,
            @NotNull(message = "管理员ID不能为空") String tableMerchantId,
            String googleCode) throws Exception {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

        String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
        if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
        }

        //银行卡
        CoinPayerInfo coinPayerInfo = iCoinPayerInfoService.getOne(new QueryWrapper<CoinPayerInfo>().lambda().eq(CoinPayerInfo::getPayerAddr, payerAddr));
        if (coinPayerInfo == null) {
            throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "钱包地址不存在");
        }

        //删除卡号
        Boolean isRemovePayerCardNo = iCoinPayerInfoService.remove(new QueryWrapper<CoinPayerInfo>().lambda().eq(CoinPayerInfo::getPayerAddr, payerAddr));
        if (!isRemovePayerCardNo) {
            throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "删除钱包失败");
        }

        //iCardChannelInfoService.updateCardChannel(iCardPayerInfoServiceOne.getCardChannelCode(), new BigDecimal(0), new BigDecimal(0), CodeConst.CardRechargeTypeConst.CHANNEL, -1, null);

        return new ReturnMsg();

    }

    /**
     * 修改 付款钱包状态
     */
    @RequestMapping(value = "/modifyCoinPayerStatus", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg modifyCoinPayerStatus(@NotNull(message = "钱包地址不能为空") String payerAddr,
                                           @NotNull(message = "请选择状态") String status,
                                           @NotNull(message = "管理员ID不能为空") String tableMerchantId
    ) throws TranException {
        //管理员
        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
//        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
//        }

        //1.查询卡原来所属的通道
        CoinPayerInfo iCoinPayerInfoServiceOne = iCoinPayerInfoService.getOne(new QueryWrapper<CoinPayerInfo>().lambda().eq(CoinPayerInfo::getPayerAddr, payerAddr));
        if (iCoinPayerInfoServiceOne == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "钱包不存在");
        }

        //2.获取卡的余额及所属通道
        // BigDecimal payerAmt = iCardPayerInfoServiceOne.getPayerAmt(); //卡余额
        // String cardChannelCode = iCardPayerInfoServiceOne.getCardChannelCode();//所属通道
        // String exPayerCardStatus = iCardPayerInfoServiceOne.getPayerCardStatus();//银行卡原来的状态
        // String channelName = iCardPayerInfoServiceOne.getCardChannelName(); //所属通道名称

        //3.修改银行卡的状态
        CoinPayerInfo entity = new CoinPayerInfo();
        entity.setStatus(status);
        boolean isUpdated = iCoinPayerInfoService.update(entity, new UpdateWrapper<CoinPayerInfo>().lambda().eq(CoinPayerInfo::getPayerAddr, payerAddr));
        if (!isUpdated) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "钱包状态修改失败");
        }
        return new ReturnMsg();
    }


    /**
     * 钱包/收款修改
     *
     * @return ReturnMsg
     * @throws TranException
     */
    @RequestMapping(value = "/modifyCoinPayer", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    public ReturnMsg modifyCoinPayer(
            String channelCode,
            @NotNull(message = "钱包名称不能为空") String payerName,
            @NotNull(message = "钱包地址不能为空") String payerAddr,
            String coinType,
            String payerLimitDay,
            String payerLimitMonth,
            @NotNull(message = "钱包状态不能为空") String status,
            @NotNull(message = "登录密码不能为空") String loginPwd,
            @NotNull(message = "支付密码不能为空") String payPwd,
            @NotNull(message = "设备ID不能为空") String deviceId,
            @NotNull(message = "设备IMEI不能为空") String deviceImei,
            @NotNull(message = "硬件系列号不能为空") String deviceSerial,
            String remark,
            String merchantId,
            String tableMerchantId) throws Exception {
        try {

            PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
//            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
//            }


            String channelName = "";
            if (!"".equals(channelCode) && channelCode != null) {
                CoinChannelWithdrawInfo channelInfo = iCoinChannelWithdrawInfoService.getOne(new QueryWrapper<CoinChannelWithdrawInfo>()
                        .lambda().eq(CoinChannelWithdrawInfo::getChannelCode, channelCode));
                channelName = channelInfo.getChannelName();
            }

            CoinPayerInfo info = new CoinPayerInfo();

            info.setChannelCode(channelCode);
            info.setChannelName(channelName);

            info.setMerchantId(merchantId); //所属卡主
            info.setPayerName(payerName);
            //info.setPayerAddr(payerAddr.trim()); //地址不更新

            info.setCoinType(coinType.trim());
            info.setStatus(status.trim()); //卡号数字串

            info.setPayerLimitDay(new BigDecimal(payerLimitDay)); //日限额
            info.setPayerLimitMonth(new BigDecimal(payerLimitMonth)); //月付出限额

            if (!"".equals(loginPwd)) {
                info.setLoginPwd(EncryptUtil.aesEncrypt(loginPwd, privateAESKey));
            }
            if (!"".equals(payPwd)) {
                info.setPayPwd(EncryptUtil.aesEncrypt(payPwd, privateAESKey));
            }

            info.setDeviceId(deviceId);
            info.setDeviceImei(deviceImei);
            info.setDeviceSerial(deviceSerial);

            info.setRemark(HtmlUtil.delHTMLTag(remark)); //java过滤特殊字符

            //Mybatis-Plus 更新语句
            //防止卡号输入重复 禁止修改
            boolean isUpdate = iCoinPayerInfoService.update(
                    info,
                    new UpdateWrapper<CoinPayerInfo>().lambda().eq(CoinPayerInfo::getPayerAddr, payerAddr));

            if (!isUpdate) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "钱包修改有误");
            }
            return new ReturnMsg();
        } catch (Exception e) {
            e.printStackTrace();
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "系统异常");

        }
    }


    /**
     * 获取代付钱包通道汇率
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/queryRateByWithdrawChannelCode", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryRateByWithdrawChannelCode(String channelCode) {
        QueryWrapper<CoinChannelWithdrawInfo> queryWrapper = new QueryWrapper<CoinChannelWithdrawInfo>();
        queryWrapper.select("CHANNEL_RATE as channelRate", "CHANNEL_SINGLE as channelSingle");
        queryWrapper.lambda().eq(CoinChannelWithdrawInfo::getChannelCode, channelCode);

        CoinChannelWithdrawInfo channelInfo = iCoinChannelWithdrawInfoService.getOne(queryWrapper);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("channelRate", channelInfo.getChannelRate());
        resultMap.put("channelSingle", channelInfo.getChannelSingle());

        return new ReturnMsg(resultMap);
    }

    /**
     * 获取代收充值钱包通道汇率
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/queryRateByDepositChannelCode", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryRateByDepositChannelCode(String channelCode) {
        QueryWrapper<CoinChannelDepositInfo> queryWrapper = new QueryWrapper<CoinChannelDepositInfo>();
        queryWrapper.select("CHANNEL_RATE as channelRate", "CHANNEL_SINGLE as channelSingle");
        queryWrapper.lambda().eq(CoinChannelDepositInfo::getChannelCode, channelCode);

        CoinChannelDepositInfo channelInfo = iCoinChannelDepositInfoService.getOne(queryWrapper);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("channelRate", channelInfo.getChannelRate());
        resultMap.put("channelSingle", channelInfo.getChannelSingle());

        return new ReturnMsg(resultMap);
    }


    /**
     * 钱包代付充值
     * TODO:新增有商家分配了通道 则也自动上分
     *
     * @param
     * @param
     * @param
     * @param remark
     * @return
     * @throws TranException
     */
    @RequestMapping(value = "/coinPayerRechargeBalance", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    @AvoidDuplicateFormToken
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg coinPayerRechargeBalance(
            @NotNull(message = "钱包不能为空") String payerAddr,
            @NotNull(message = "通道编号不能为空") String channelCode,
            @NotNull(message = "充值金额不能为空") BigDecimal rechargeAmt,
            @NotNull(message = "管理员ID不能为空") String tableMerchantId,
            @NotNull(message = "google谷歌验证码不能为空") String googleCode,
            String remark) throws Exception {

        return new ReturnMsg();

//        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
//
//        if (merchantInfo.getIsGoogleAuth().equals(GoogleConst.GOOGLE_AUTH_N)) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "未开启google验证");
//        }
////        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
////            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
////        }
//        String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
//        if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
//        }
//
//        //钱包
//        CoinPayerInfo info = iCoinPayerInfoService.getOne(new QueryWrapper<CoinPayerInfo>().lambda().eq(CoinPayerInfo::getPayerAddr, payerAddr));
//        if (info == null) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "钱包不存在");
//        }
//
//        /**
//         * @Service
//         * 银行卡余额变动+流水变动服务
//         * */
//        String flowType = EnumFlowType.RECHARGE.getName(); //充值
//        String channelWithdrawNo = "C" + DateUtil.format(new Date(), DateUtil.FULL_TIME_FORMAT_EN) + "0" + System.currentTimeMillis();
//        remark = !"".equals(remark) ? HtmlUtil.delHTMLTag(remark) : "[" + merchantInfo.getMerchantName() + "]手动充值[" + info.getPayerName() + "]" + info.getPayerAddr();
//        coinWithdrawManagerService.alterCoinPayerBalanceNAddFlowService(info, rechargeAmt, flowType, channelWithdrawNo, channelWithdrawNo, remark);
//
//        /**
//         * warn:有通道但不一定有分配商家
//         * */
//
//        //通道银行卡找到通道号
//        //cardPayerInfo.getCardChannelCode();
//        //查找通道对应分配的商家
//
//        //7.如果商家就加 上分 "商户充值" + 流水 PayMerchantMoneyChange
//        Map<String, Object> paramsMerchant = new HashMap<String, Object>();
//        paramsMerchant.put("agentRate", "0"); //商家0
//        paramsMerchant.put("channelCode", channelCode); //通道号
//        paramsMerchant.put("status", EnumMerchantStatusType.NORMAL.getName()); //自动上分只加到可用商户
//
//        PayMerchantInfo curMerchantInfo = iPayMerchantInfoService.findOneMerchantByCardChannelCode(paramsMerchant);
//        int isMerchantRecharge = 0;
//        //TODO:付款卡 不在前台显示 只用于后台上分 因此不上商户余额
//        if (curMerchantInfo != null) {
////            remark = "[" +merchantInfo.getMerchantName() + "]手动上分";
////            merchantAccountManagerService.merchantRecharge(rechargeAmt, curMerchantInfo.getMerchantId(), curMerchantInfo.getMerchantName(), remark, curMerchantInfo.getMerchantName(), curMerchantInfo.getLabel(),channelCode,channelWithdrawNo,EnumFlowType.RECHARGE.getName());
////            isMerchantRecharge = 1;
//        }
//        Map<String, Object> resultMap = new HashMap<String, Object>();
//        resultMap.put("rechargeAmt", rechargeAmt);
//        resultMap.put("isMerchantRecharge", isMerchantRecharge); //同时有充值了商户(商户已分配了通道)
//        resultMap.put("merchantName", curMerchantInfo != null ? curMerchantInfo.getMerchantName() : "");
//        return new ReturnMsg(resultMap);
    }


    /**
     * 钱包代收补充值
     *
     * @param
     * @param
     * @param
     * @param remark
     * @return
     * @throws TranException
     */
    @RequestMapping(value = "/coinHolderRechargeBalance", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    @AvoidDuplicateFormToken
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg coinHolderRechargeBalance(
            @NotNull(message = "钱包不能为空") String payerAddr,
            @NotNull(message = "通道编号不能为空") String channelCode,
            @NotNull(message = "充值金额不能为空") BigDecimal rechargeAmt,
            @NotNull(message = "管理员ID不能为空") String tableMerchantId,
            @NotNull(message = "google谷歌验证码不能为空") String googleCode,
            String remark) throws Exception {

        return new ReturnMsg();
//
//        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
//
//        if (merchantInfo.getIsGoogleAuth().equals(GoogleConst.GOOGLE_AUTH_N)) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "未开启google验证");
//        }
//
//        String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
//        if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
//        }
//
//        //收款钱包
//        CoinHolderInfo info = iCoinHolderInfoService.getOne(new QueryWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getPayerAddr, payerAddr));
//        if (info == null) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "代收钱包不存在，请检查设备ID是否有误");
//        }
//
//        if (!info.getStatus().equals(CodeConst.PayerCardStatusConst.CARD_STATUS_BELONG)) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "代收钱包[" + info.getPayerAddr() + "]未分配");
//        }
//
//
//        String flowType = EnumFlowType.RECHARGE.getName(); //充值
//
//        String withdrawNo = MySeq.getWithdrawNo();
//        String channelWithdrawNo = "C" + DateUtil.format(new Date(), DateUtil.FULL_TIME_FORMAT_EN) + "0" + System.currentTimeMillis();
//
//        remark += "[" + merchantInfo.getMerchantName() + "]手动补单[" + info.getPayerName() + "]" + info.getPayerAddr();
//        //1.钱包更改余额+流水表
//        coinWithdrawManagerService.alterCoinHolderBalanceNAddFlowService(info, null,rechargeAmt, rechargeAmt, flowType, channelWithdrawNo, channelWithdrawNo, remark, "");
//
//        //2.商家余额+流水表 | 通道收款钱包上分时 自动上分商户余额 || 掉单补单
//        int isMerchantRecharge = 0;
//
//        Map<String, Object> paramsMerchant = new HashMap<String, Object>();
//        paramsMerchant.put("agentRate", "0"); //商家0
//        paramsMerchant.put("channelCode", channelCode); //一个商家只有分配一个代收、代付的通道号
//        paramsMerchant.put("status", EnumMerchantStatusType.NORMAL.getName()); //自动上分只加到可用商户
//
//        PayMerchantInfo currentMerchantInfo = iPayMerchantInfoService.findOneMerchantByCardChannelCode(paramsMerchant); //商户ID
//        //只查询商户代收通道的配置
//        PayMerchantChannelSite currentChannelSite = iPayMerchantChannelSiteService.getOne(new QueryWrapper<PayMerchantChannelSite>()
//                .lambda()
//                .eq(PayMerchantChannelSite::getChannelCode, channelCode)
//                .eq(PayMerchantChannelSite::getPayType, CodeConst.PayType.RECEIVE_IN));
//        String merchantName = "";
//        if (currentMerchantInfo != null) {
//
//            isMerchantRecharge = 1;
//            merchantName = currentMerchantInfo.getMerchantName();
//            BigDecimal merchantRateFee = rechargeAmt.divide(new BigDecimal("100")).multiply(new BigDecimal(currentChannelSite.getMerchantRate())).setScale(2, BigDecimal.ROUND_HALF_UP);
//            BigDecimal merchantSingleFee = currentChannelSite.getSinglePoundage();//单笔手续费;
//            BigDecimal amtCredit = rechargeAmt.subtract(merchantRateFee).subtract(merchantSingleFee);
//
//            merchantAccountManagerService.merchantRecharge(rechargeAmt, amtCredit, currentMerchantInfo.getMerchantId(), currentMerchantInfo.getMerchantName(), remark, currentMerchantInfo.getMerchantName(), currentMerchantInfo.getLabel(), channelCode, withdrawNo, EnumFlowType.RECHARGE.getName());
//
//            //3.代理分润+流水表
//            BigDecimal agentSingleProfit = new BigDecimal(0);  //初始代理单笔分成 为 0元
//            BigDecimal agentRateProfit = new BigDecimal(0);    //初始代理下发分成 为 0元
//
//            //递归取得代理的单笔总分成 withdrawNo,channelWithdrawNo,rechargeAmt,PayMerchantChannelSite
//            alterAgentSingleBalance(agentSingleProfit, currentMerchantInfo, withdrawNo, channelWithdrawNo, rechargeAmt, currentChannelSite);
//
//            //递归下发单笔费率总分成
//            alterAgentRateBalance(agentRateProfit, currentMerchantInfo, withdrawNo, channelWithdrawNo, rechargeAmt, currentChannelSite);
//
//            //4.记录代收商家手续费
////            coinChannelManagerService.alterCoinChannelBalanceService(
////                    currentMerchantInfo.getMerchantId(),
////                    merchantRateFee.add(merchantSingleFee).negate(),//商家手续费 扣除
////                    CodeConst.CardFlowTypeConst.RECHARGE, //手动补单充值
////                    withdrawNo,//平台订单号
////                    channelWithdrawNo,//三方通道号
////                    channelCode,
////                    merchantInfo.getMerchantId(),//此ID为管理员
////                    currentChannelSite.getChannelCode(),
////                    remark
////            );
//        }
//        Map<String, Object> resultMap = new HashMap<String, Object>();
//        resultMap.put("rechargeAmt", rechargeAmt);
//        resultMap.put("isMerchantRecharge", isMerchantRecharge); // 1 同时充值了商户(商户已分配了通道) 0-未充值商户余额 收款钱包没有绑定商户 商户余额为所有代付钱包的余额
//        resultMap.put("merchantName", merchantName);//  彩票：钱包未分配商户
//        return new ReturnMsg(resultMap);
    }


    /**
     * @返回通道内卡 CARD_PAYER_INFO 的最高单笔余额
     */
    @RequestMapping(value = "/getCoinPayerHighestAmt", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg getCoinPayerHighestAmt(
            String channelCode,
            String status,
            String payerName,
            String payerAddr,
            String coinType,
            String remark,
            String merchantId,
            String tableMerchantId
    ) throws TranException {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
        PayMerchantChannelSite payMerchantChannelSite = iPayMerchantChannelSiteService.getOne(
                new QueryWrapper<PayMerchantChannelSite>().lambda()
                        .eq(PayMerchantChannelSite::getMerchantId, tableMerchantId)
                        .eq(PayMerchantChannelSite::getPayType, CodeConst.PayType.SEND_OUT)
        );
        String hasChannelCode = merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName()) ? payMerchantChannelSite.getChannelCode() : channelCode;

        if ("".equals(hasChannelCode) || hasChannelCode == null) {

            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
            }
        }

        String walletStatus = merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName()) ? CodeConst.PayerCardStatusConst.CARD_STATUS_BELONG : status;

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("channelCode", hasChannelCode);
        params.put("status", walletStatus);
        params.put("payerName", payerName);
        params.put("payerAddr", payerAddr);
        params.put("coinType", coinType); //人工还是自动
        params.put("merchantId", merchantId); //所属卡主
        params.put("remark", HtmlUtil.delHTMLTag(remark));

        Map<String, Object> highestAmt = iCoinPayerInfoService.selectOneCoinPayerInfoOfHighestAmt(params);
        return new ReturnMsg(highestAmt);
    }

    /**
     * @统计卡余额 + 笔数数
     */
    @RequestMapping(value = "/getCoinPayerCountMap", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg getCoinPayerCountMap(
            String channelCode,
            String status,
            String payerName,
            String payerAddr,
            String coinType,
            String merchantId,
            String remark,
            String tableMerchantId) throws TranException {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
        PayMerchantChannelSite payMerchantChannelSite = iPayMerchantChannelSiteService.getOne(
                new QueryWrapper<PayMerchantChannelSite>().lambda()
                        .eq(PayMerchantChannelSite::getMerchantId, tableMerchantId)
                        .eq(PayMerchantChannelSite::getPayType, CodeConst.PayType.SEND_OUT)
        );

        String hasChannelCode = merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName()) ? payMerchantChannelSite.getChannelCode() : channelCode;

        if ("".equals(hasChannelCode) || channelCode == null) {
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
            }
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("channelCode", hasChannelCode);
        params.put("status", status);
        params.put("payerName", payerName); //持卡人
        params.put("payerAddr", payerAddr);
        params.put("coinType", coinType);
        params.put("merchantId", merchantId); //所属卡主
        params.put("remark", HtmlUtil.delHTMLTag(remark));

        //银行卡总余额 -- 查询充值流水订单
        BigDecimal sumPayerAmt = iCoinPayerInfoService.selectSumPayerAmt(params);
        //排队中笔数
        Integer countProcessingNum = 0;
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("sumPayerAmt", sumPayerAmt);
        resultMap.put("countProcessingNum", countProcessingNum);
        return new ReturnMsg(resultMap);

    }

    /**
     * 钱包代付流水明细
     */
    @RequestMapping(value = "/queryCoinPayerFlowPage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryCoinPayerFlowPage(
            String withdrawNo,
            String channelWithdrawNo,
            String payerName,
            String payerAddr,
            String channelCode,
            String flowType,
            String coinType,
            String beginTime,
            String endTime,
            String remark,
            @NotNull(message = "行数不能为空") Integer rows,
            @NotNull(message = "页码不能为空") Integer page,
            String tableMerchantId
    ) throws TranException {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
        PayMerchantChannelSite payMerchantChannelSite = iPayMerchantChannelSiteService.getOne(
                new QueryWrapper<PayMerchantChannelSite>().lambda()
                        .eq(PayMerchantChannelSite::getMerchantId, tableMerchantId)
                        .eq(PayMerchantChannelSite::getPayType, CodeConst.PayType.SEND_OUT)
        );

        String hasChannelCode = merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName()) ? payMerchantChannelSite.getChannelCode() : channelCode;

        if ("".equals(hasChannelCode) || hasChannelCode == null) {
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
            }
        }

        Page<CoinPayerWithdrawFlow> pages = new Page<>(page, rows);

        QueryWrapper<CoinPayerWithdrawFlow> queryWrapper = new QueryWrapper<>();

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("CHANNEL_CODE", hasChannelCode);
        params.put("WITHDRAW_NO", withdrawNo);
        params.put("CHANNEL_WITHDRAW_NO", channelWithdrawNo);
        params.put("FLOW_TYPE", flowType);
        params.put("COIN_TYPE", coinType);
        params.put("REMARK", HtmlUtil.delHTMLTag(remark));
        params.put("PAYER_NAME", payerName);
        params.put("PAYER_ADDR", payerAddr);

        queryWrapper = queryWrapper.allEq(params, false)
                .like(StringUtils.isNotBlank(remark), "REMARK", HtmlUtil.delHTMLTag(remark))
                .ge(StringUtils.isNotBlank(beginTime), "CREATE_TIME", beginTime + " 00:00:00")
                .le(StringUtils.isNotBlank(endTime), "CREATE_TIME", endTime + " 23:59:59");
        queryWrapper.orderByDesc("ID");
        //page
        IPage<CoinPayerWithdrawFlow> pageMap = iCoinPayerWithdrawFlowService.page(pages, queryWrapper);

        //2统计交易订单
        Map<String, Object> paramsCount = new HashMap<String, Object>();
        paramsCount.put("flowType", "");
        Map<String, Object> countFlowInfo = iCoinPayerWithdrawFlowService.selectPayerCountRecharge(paramsCount);

        //3//按条件搜索充值金额
        Map<String, Object> paramsSearch = new HashMap<String, Object>();

        paramsSearch.put("withdrawNo", withdrawNo);
        paramsSearch.put("channelWithdrawNo", channelWithdrawNo);
        paramsSearch.put("payerName", payerName);
        paramsSearch.put("payerAddr", payerAddr);
        paramsSearch.put("channelCode", channelCode);
        paramsSearch.put("remark", HtmlUtil.delHTMLTag(remark));
        paramsSearch.put("flowType", flowType);
        paramsSearch.put("coinType", coinType);
        paramsSearch.put("beginTime", beginTime == null ? DateUtil.getDate() + " 00:00:00" : beginTime + " 00:00:00");
        paramsSearch.put("endTime", endTime == null ? DateUtil.getDate() + " 23:59:59" : endTime + " 23:59:59");
        Map<String, Object> countMapBySearch = iCoinPayerWithdrawFlowService.selectPayerCountRechargeBySearch(paramsSearch);

        //查询结果 map
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("countFlowInfo", countFlowInfo);
        resultMap.put("countMapBySearch", countMapBySearch);
        resultMap.put("pages", pageMap);

        return new ReturnMsg(resultMap);
    }


    /**
     * 修改代理商的余额
     * 记录代理商的流水
     * 商户 1元 代理 0.7 总代 0.6 == 单笔
     */
    @SuppressWarnings({"deprecation"})
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal alterAgentSingleBalance(BigDecimal agentsProfit, PayMerchantInfo curMerchantInfo, String withdrawNo, String channelWithdrawNo, BigDecimal rechargeAmt, PayMerchantChannelSite currentChannelSite) throws TranException {

        //当前商户汇率
        PayAgentChannelSite curMerchantChannelSite = iPayAgentChannelSiteService.getOne(
                new QueryWrapper<PayAgentChannelSite>().lambda()
                        .eq(PayAgentChannelSite::getMerchantId, curMerchantInfo.getMerchantId())
                        .eq(PayAgentChannelSite::getChannelCode, currentChannelSite.getChannelCode())
                        .eq(PayAgentChannelSite::getPayType, CodeConst.PayType.RECEIVE_IN)
        );
        //上级商户
        PayMerchantInfo parentMerchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, curMerchantInfo.getParentId()));
        //找到上级代理
        if (parentMerchantInfo != null) {
            //上级代理通道信息
            PayAgentChannelSite parentChannelSite = iPayAgentChannelSiteService.getOne(new QueryWrapper<PayAgentChannelSite>().lambda()
                    .eq(PayAgentChannelSite::getMerchantId, parentMerchantInfo.getMerchantId())
                    .eq(PayAgentChannelSite::getChannelCode, currentChannelSite.getChannelCode())
                    .eq(PayAgentChannelSite::getPayType, CodeConst.PayType.RECEIVE_IN)
            );
            //上级代理商未分配代收通道 以防出错 直接返回 0
            if (parentChannelSite == null) {
                return new BigDecimal(0);
            }
            //代理手续费 = 商户成本 - 代理成本
            _log.info("商户单笔代收手续费:{},代理成本:{}", curMerchantChannelSite.getSinglePoundage(), parentChannelSite.getSinglePoundage());
            BigDecimal parentSingleProfit = curMerchantChannelSite.getSinglePoundage().subtract(parentChannelSite.getSinglePoundage());
            if (BigDecimalUtils.lessThan(rechargeAmt, new BigDecimal(0))) {
                parentSingleProfit = parentSingleProfit.negate();
            }
            Integer parentIndex = iPayMerchantBalanceService.alterMerchantBalance(parentMerchantInfo.getMerchantId(), parentSingleProfit, null);
            if (parentIndex != 1) throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "分配代理商利润失败");
            _log.info("===>>>代理:{},单笔分成:{}", parentMerchantInfo.getMerchantId(), parentSingleProfit);

            //继续查询是否有代理的上级代理 即总代
            //FIXME:充值bug修复 若money_change变中没有上级代理,则查找上级代理的余额为 null,会导致 插入代理商分润流水失败
            //以防死锁 selectLatestMoneyChange
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("merchantId", parentMerchantInfo.getMerchantId()); //某一个代理
            PayMerchantMoneyChange latestRecord = iPayMerchantMoneyChangeService.selectLatestMoneyChangeForUpdate(params);

            //没有流水 要新插入
            if (latestRecord == null) {

                PayMerchantMoneyChange addMoneyChangeEntity = new PayMerchantMoneyChange();
                addMoneyChangeEntity.setMerchantId(parentMerchantInfo.getMerchantId()); //某一个代理
                addMoneyChangeEntity.setPayType(CodeConst.PayType.RECEIVE_IN);
                addMoneyChangeEntity.setFlowType(EnumFlowType.RECHARGE.getName()); //代付类型
                addMoneyChangeEntity.setMerchantWithdrawNo("");//设为空先
                addMoneyChangeEntity.setWithdrawNo(withdrawNo);
                addMoneyChangeEntity.setAmt(parentSingleProfit); // 提交代收金额
                addMoneyChangeEntity.setCreditAmt(parentSingleProfit); //实际入账金额
                addMoneyChangeEntity.setAmtBefore(new BigDecimal(0));
                addMoneyChangeEntity.setAmtNow(parentSingleProfit);

                //addMoneyChangeEntity.setCreateTime(new Date());
                addMoneyChangeEntity.setRemark("代理[" + parentMerchantInfo.getMerchantId() + "]单笔代收分成[" + parentSingleProfit + "]");
                addMoneyChangeEntity.setChannelName(parentMerchantInfo.getChannelName());
                addMoneyChangeEntity.setChannelCode(parentChannelSite.getChannelCode());

                boolean isAddMoneyChangeEntity = iPayMerchantMoneyChangeService.save(addMoneyChangeEntity);

                if (!isAddMoneyChangeEntity) {
                    _log.error("代收失败:插入代理商:{}分润流水失败", parentMerchantInfo.getMerchantId());
                    throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "代收失败:插入代理商分润流水失败");
                }
            } else {

                //更新时间
                _log.info("代理代收单笔流水记录===>>>商家:{},账变前:{},账变后:{}", parentMerchantInfo.getMerchantId(), latestRecord.getAmtNow(), latestRecord.getAmtNow().add(parentSingleProfit));

                //3.记录MONEY_CHANGE 充值流水
                PayMerchantMoneyChange moneyChangeEntity = new PayMerchantMoneyChange();
                moneyChangeEntity.setMerchantId(parentMerchantInfo.getMerchantId());
                moneyChangeEntity.setPayType(CodeConst.PayType.RECEIVE_IN);
                moneyChangeEntity.setFlowType(EnumFlowType.RECHARGE.getName()); //充值状态
                moneyChangeEntity.setMerchantWithdrawNo("");
                moneyChangeEntity.setWithdrawNo(withdrawNo);
                moneyChangeEntity.setAmt(parentSingleProfit); //资金变动
                moneyChangeEntity.setCreditAmt(parentSingleProfit); //实际入账金额
                moneyChangeEntity.setAmtBefore(latestRecord.getAmtNow());
                moneyChangeEntity.setAmtNow(latestRecord.getAmtNow().add(parentSingleProfit));
                //moneyChangeEntity.setCreateTime(new Date());
                moneyChangeEntity.setRemark("代理[" + parentMerchantInfo.getMerchantId() + "]单笔代收分成[" + parentSingleProfit + "]");
                moneyChangeEntity.setChannelName(currentChannelSite.getChannelCode());
                moneyChangeEntity.setChannelCode(currentChannelSite.getChannelCode());
                boolean isSaveMoneyChange = iPayMerchantMoneyChangeService.save(moneyChangeEntity);
                if (!isSaveMoneyChange) {
                    _log.info("=====>>>>>代理商充值/代收余额变动明细表:{},代收流水表:{}失败", parentMerchantInfo.getMerchantId(), parentSingleProfit);
                    throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "代收分成给代理商[" + parentMerchantInfo.getMerchantId() + "],金额[" + parentSingleProfit + "]失败");
                }
            }
            agentsProfit = agentsProfit.add(parentSingleProfit);
            return alterAgentSingleBalance(agentsProfit, parentMerchantInfo, withdrawNo, channelWithdrawNo, rechargeAmt, currentChannelSite); //递归
        }
        return agentsProfit;
    }


    @SuppressWarnings({"deprecation"})
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal alterAgentRateBalance(BigDecimal agentsProfit, PayMerchantInfo curMerchantInfo, String withdrawNo, String channelWithdrawNo, BigDecimal rechargeAmt, PayMerchantChannelSite currentChannelSite) throws TranException {

        //当前商户汇率
        PayAgentChannelSite curMerchantChannelSite = iPayAgentChannelSiteService.getOne(
                new QueryWrapper<PayAgentChannelSite>().lambda()
                        .eq(PayAgentChannelSite::getMerchantId, curMerchantInfo.getMerchantId())
                        .eq(PayAgentChannelSite::getChannelCode, currentChannelSite.getChannelCode())
                        .eq(PayAgentChannelSite::getPayType, CodeConst.PayType.RECEIVE_IN)
        );
        //上级商户
        PayMerchantInfo parentMerchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, curMerchantInfo.getParentId()));
        //找到上级代理
        if (parentMerchantInfo != null) {

            //上级代理通道信息
            PayAgentChannelSite parentChannelSite = iPayAgentChannelSiteService.getOne(new QueryWrapper<PayAgentChannelSite>().lambda()
                    .eq(PayAgentChannelSite::getMerchantId, parentMerchantInfo.getMerchantId())
                    .eq(PayAgentChannelSite::getChannelCode, currentChannelSite.getChannelCode())
                    .eq(PayAgentChannelSite::getPayType, CodeConst.PayType.RECEIVE_IN));

            //上级代理商未分配代收通道 以防出错 直接返回 0
            if (parentChannelSite == null) {
                return new BigDecimal(0);
            }

            //代理手续费 = （商户成本 - 代理成本） * 下发金额
            _log.info("商户代收手续费率 * 代收金额:{},代理成本:{}", curMerchantChannelSite.getMerchantRate(), parentChannelSite.getMerchantRate());
            BigDecimal parentPayProfitRate = new BigDecimal(curMerchantChannelSite.getMerchantRate()).subtract(new BigDecimal(parentChannelSite.getMerchantRate()));

            BigDecimal parentPayProfit = parentPayProfitRate.divide(new BigDecimal("100")).multiply(rechargeAmt).setScale(4, BigDecimal.ROUND_HALF_UP);

            Integer parentIndex = iPayMerchantBalanceService.alterMerchantBalance(parentMerchantInfo.getMerchantId(), parentPayProfit, null);
            if (parentIndex != 1) throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "分配代理商利润失败");
            _log.info("===>>>代理:{},单笔下发手续费率*下发金额分成:{}", parentMerchantInfo.getMerchantId(), parentPayProfit);


            Map<String, Object> params = new HashMap<String, Object>();
            params.put("merchantId", parentMerchantInfo.getMerchantId());
            PayMerchantMoneyChange latestRecord = iPayMerchantMoneyChangeService.selectLatestMoneyChangeForUpdate(params);
            //没有流水 要新插入
            if (latestRecord == null) {

                PayMerchantMoneyChange addMoneyChangeEntity = new PayMerchantMoneyChange();

                addMoneyChangeEntity.setMerchantId(parentMerchantInfo.getMerchantId()); //某一个代理
                addMoneyChangeEntity.setFlowType(EnumFlowType.RECHARGE.getName()); //充值类型
                addMoneyChangeEntity.setPayType(CodeConst.PayType.RECEIVE_IN);
                addMoneyChangeEntity.setMerchantWithdrawNo("");
                addMoneyChangeEntity.setWithdrawNo(withdrawNo);
                addMoneyChangeEntity.setAmt(parentPayProfit); // 提交代付金额
                addMoneyChangeEntity.setCreditAmt(parentPayProfit); //实际入账金额
                addMoneyChangeEntity.setAmtBefore(new BigDecimal(0));
                addMoneyChangeEntity.setAmtNow(parentPayProfit);
                //addMoneyChangeEntity.setCreateTime(new Date());
                addMoneyChangeEntity.setRemark("代理[" + parentMerchantInfo.getMerchantId() + "]代收分成");
                addMoneyChangeEntity.setChannelName(parentMerchantInfo.getChannelName());
                addMoneyChangeEntity.setChannelCode(parentChannelSite.getChannelCode());

                boolean isAddMoneyChangeEntity = iPayMerchantMoneyChangeService.save(addMoneyChangeEntity);

                if (!isAddMoneyChangeEntity) {
                    _log.error("代收失败:插入代理商:{}分润流水失败", parentMerchantInfo.getMerchantId());
                    throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "代收失败:插入代理商分润流水失败");
                }
            } else {

                //更新时间
                _log.info("代理代收流水记录===>>>商家:{},账变前:{},账变后:{}", parentMerchantInfo.getMerchantId(), latestRecord.getAmtNow(), latestRecord.getAmtNow().add(parentPayProfit));

                //3.记录MONEY_CHANGE 退款流水
                PayMerchantMoneyChange moneyChangeEntity = new PayMerchantMoneyChange();
                moneyChangeEntity.setMerchantId(parentMerchantInfo.getMerchantId());
                moneyChangeEntity.setPayType(CodeConst.PayType.RECEIVE_IN);
                moneyChangeEntity.setFlowType(EnumFlowType.RECHARGE.getName()); //充值状态
                moneyChangeEntity.setMerchantWithdrawNo("");
                moneyChangeEntity.setWithdrawNo(withdrawNo);
                moneyChangeEntity.setAmt(parentPayProfit); //资金变动
                moneyChangeEntity.setCreditAmt(parentPayProfit); //实际入账金额
                moneyChangeEntity.setAmtBefore(latestRecord.getAmtNow());
                moneyChangeEntity.setAmtNow(latestRecord.getAmtNow().add(parentPayProfit));

                //moneyChangeEntity.setCreateTime(new Date());
                moneyChangeEntity.setRemark("代理[" + parentMerchantInfo.getMerchantId() + "]代收分成[" + parentPayProfit + "]");
                moneyChangeEntity.setChannelName(currentChannelSite.getChannelCode());
                moneyChangeEntity.setChannelCode(currentChannelSite.getChannelCode());
                boolean isSaveMoneyChange = iPayMerchantMoneyChangeService.save(moneyChangeEntity);

                // boolean isUpdateMoneyChange = iPayMerchantMoneyChangeService.update(moneyChangeEntity, new QueryWrapper<PayMerchantMoneyChange>().lambda().in(PayMerchantMoneyChange::getId, moneyLatestChangeEntity.getId()));

                if (!isSaveMoneyChange) {
                    _log.info("=====>>>>>代理商充值/代收余额变动明细表:{},代收流水表:{}失败", parentMerchantInfo.getMerchantId(), parentPayProfit);
                    throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "代收分成给代理商[" + parentMerchantInfo.getMerchantId() + "],金额[" + parentPayProfit + "]失败");
                }
            }
            agentsProfit = agentsProfit.add(parentPayProfit);
            return alterAgentRateBalance(agentsProfit, parentMerchantInfo, withdrawNo, channelWithdrawNo, rechargeAmt, currentChannelSite);
        }
        return agentsProfit;
    }


}