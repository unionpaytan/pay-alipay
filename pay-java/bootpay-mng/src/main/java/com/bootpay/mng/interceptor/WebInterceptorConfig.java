package com.bootpay.mng.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.bootpay.common.framework.RedisUtil;
import com.bootpay.mng.service.AuthenticatorService;

@Configuration
public class WebInterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private AuthenticatorService authenticatorService;
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CORSInterceptor corsInterceptor;

    /**
     * 不拦截/放行的url路径（可直接通过浏览器地址打开）
     */
    String[] urls = new String[]{
            //"/app/**", // app客户端底下的所有链接
            "/robot/**", // 机器人 telegram robot



            "/deposit/merchantDeposit",//web代收充值
            "/deposit/monitorSuccess",//查询是否已支付成功
            "/AlipayAccountManage/monitorToken",//查询是否有 cookie
            "/AlipayAccountManage/monitorBarcodeStatus",//查询是否有 cookie

            "/partner/api/queryPlatformBalance",//平台手续费
            "/partner/api/queryPartnerMoneyChangePageByApi",//外包手续费流水变动列表
            "/partner/api/queryPartnerAmtMoneyChangePageByApi",//获取外包跑量流水变动列表

            "/loginManager/login",//登陆
            "/loginManager/confirmLogin",//确认登陆
            "/loginManager/modifyPwd",//修改密码
            "/loginManager/sendEmailCode",//邮箱验证码


            "/apiQueryFee",//api 查询账户手续费
            "/apiQueryBalance",//api 查询账户余额

            "/deposit/apiDeposit",//api 接口代收
            "/deposit/apiQueryDeposit",//api 接口代收
            "/deposit/verifyPayUrl",//前端vue检验 支付链接
            "/deposit/payerConfirmPaid",//已点击已完成支付
            "/deposit/payerConfirmFromAddr",//已输入付款人姓名
            "/deposit/getServerTime",//前端vue检验 获取服务器时间 精确倒计时

            "/withdraw/apiWithdraw",//api 接口提现
            "/withdraw/apiQueryWithdraw",//api 提现订单查询

            "/withdraw/apiQueryCardPayer",//api 商户银行卡
            "/withdraw/apiQuery",

            "/withdraw/channelWithdraw",
            "/withdraw/channelQueryWithdraw",


            //"/coin/uploadImage",//图片上传 不可对外开放 否则被黑
            "/fileDownload/exportExcelOfOrderList",//商户下单文件下载
            "/fileDownload/exportRechargeExcel",//商户充值注水下载
            "/fileDownload/rechargeExportExcel",//文件下载
            "/fileDownload/exportMerchantMoneyChangeExcel",//余额账变明细
            "/fileDownload/exportMerchantPayerFeeExcel",//手续费流水
            "/fileDownload/coinPayerWithdrawListExportExcel",//三方订单下载
            "/**.html",
            "/static/**",
            "/**.zip",
            "/**.rar",
            "/**.ico"
    };

    String[] chekIpUrls = new String[]{
            "/withdraw/apiWithdraw",//api 接口提现
            "/withdraw/apiQueryWithdraw",//api 提现订单查询
            "/withdraw/apiQueryCardPayer",//api 商户银行卡
    };

    //FIXME:不再验证IP
    String[] chekWebIpUrls = new String[]{
            "/withdraw/merchantWithdraw",//web代付
//            "/coin/registerOperatorCoinHolder",//码商操作员
//            "/coin/registerAgentCoinHolder",//码商新增
//            "/coin/modifyOperatorCoinHolder",
//            "/coin/modifyAgentCoinHolder",
//            "/cardAccountManage/addMerchantOperator",//新增操作员
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //校验token拦截器
        InterceptorRegistration registration = registry.addInterceptor(new LoginInterceptor(authenticatorService));
        registration.excludePathPatterns(urls);
        //registration.addPathPatterns("/**");
        // ip校验拦截器
        InterceptorRegistration ipRegistration = registry.addInterceptor(new CheckIpInterceptor(authenticatorService));
        ipRegistration.addPathPatterns(chekIpUrls);

        // web ip校验拦截器
        InterceptorRegistration webIpRegistration = registry.addInterceptor(new CheckWebIpInterceptor(authenticatorService));
        webIpRegistration.addPathPatterns(chekWebIpUrls);

        //表单重复提交校验拦截器
        InterceptorRegistration dupRegistration = registry.addInterceptor(new DuplicateSubmitInterceptor(redisUtil));
        dupRegistration.excludePathPatterns(urls);
        dupRegistration.addPathPatterns("/**"); //

        //启用拦截器
        registry.addInterceptor(corsInterceptor);

    }

}
