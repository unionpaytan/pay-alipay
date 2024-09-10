import _import from '../_import'
import Router from '../index'
import configMenu from './configMenu'


//全部的菜单路由
const MENU = [
    {
        path: '/index/home',
        name: '首页',
        component: _import('home/homepage'),
        meta: { id: '1' }
    },

    { path: '/index/recharge', name: '提现明细', component: _import('platIncome/recharge'), meta: { id: '2' }, mid: 17 },
    { path: '/index/proxyPay', name: '平台提现', component: _import('platIncome/proxyPay'), meta: { id: '3' }, mid: 7 },

    { path: '/index/agentList', name: '代理列表', component: _import('mall/agentList'), meta: { id: '20' }, mid: 66 },

    { path: '/index/mallList', name: '商户列表', component: _import('mall/mallList'), meta: { id: '4' }, mid: 8 },
    { path: '/index/flowing', name: '商户充值流水', component: _import('mall/flowing'), meta: { id: '5' }, mid: 19 },
    //后台操作菜单
    { path: '/index/moneyChange', name: '商户资金流水明细', component: _import('mall/moneyChange'), meta: { id: '25' }, mid: 73 },
    { path: '/index/moneyBalance', name: '商户每日余额统计', component: _import('mall/moneyBalance'), meta: { id: '26' }, mid: 75 },
    //代理报表ID
    //@代理的报表ID不同
    { path: '/index/sysReportForm', name: '代理统计报表', component: _import('mall/sysReportForm'), meta: { id: '135' }, mid: 135 },


    /**
     * 前台代理菜单 不在后台管理权限当中
     * 
    */
    {
        path: '/index/agentFlow',
        name: '商户补单列表',
        component: _import('mall/agentFlow'), //充值Flow
        meta: { id: '22' },
    },

    {
        path: '/index/agentOrder',
        name: '商户提现订单',
        component: _import('mall/agentOrder'),
        meta: { id: '23' },
    },

    {
        path: '/index/agentReportForm',
        name: '商户统计报表',
        component: _import('mall/agentReportForm'),
        meta: { id: '24' },
    },
    {
        path: '/mall/agentMoneyChange',
        name: '代理账变明细',
        component: _import('mall/agentMoneyChange'),
        meta: { id: '1001' },
    },

    {
        path: '/mall/agentsMerchantList',
        name: '代理下级商户列表',
        component: _import('mall/agentsMerchantList'),
        meta: { id: '1002' },
    },

    {
        path: '/mall/agentsMerchantCardList',
        name: '下级商户代付钱包',
        component: _import('mall/agentsMerchantCardList'),
        meta: { id: '1003' },
    },

    {
        path: '/mall/agentsMerchantHolderList',
        name: '下级商户收款钱包',
        component: _import('mall/agentsMerchantHolderList'),
        meta: { id: '1005' },
    },


    /**
     * 后台管理菜单
     * 
    */

    {
        path: '/index/rechargeOrder',
        name: '平台充值订单',
        component: _import('mall/rechargeOrder'),
        meta: { id: '15' },
        mid: 21
    },

    { path: '/index/reportForm', name: '商户统计报表', component: _import('mall/reportForm'), meta: { id: '6' }, mid: 28 },
    { path: '/index/orderList', name: '提现/代付列表', component: _import('order/orderList'), meta: { id: '7', }, mid: 23 },
    { path: '/index/depositList', name: '充值/代收列表', component: _import('order/depositList'), meta: { id: '72', }, mid: 185 },

    { path: '/index/statistics', name: '统计报表', component: _import('order/statistics'), meta: { id: '8' } },

    { path: '/index/channelList', name: '代收通道列表', component: _import('conduit/channelList'), meta: { id: '9001' }, mid: 25 },
    { path: '/index/conduitList', name: '代付通道列表', component: _import('conduit/conduitList'), meta: { id: '9' }, mid: 180 },


    { path: '/index/rechargeFlow', name: '充值流水', component: _import('conduit/rechargeFlow'), meta: { id: '10' } },
    { path: '/index/merchantFlow', name: '自助订单流水', component: _import('merchant/merchantFlow'), meta: { id: '11' } },

    {
        path: '/index/merchantChange',
        name: '商户资金账变明细',
        component: _import('merchant/merchantMoneyChange'),
        meta: { id: '31' }
    },

    { path: '/index/merchantOrder', name: '提现订单', component: _import('merchant/merchantOrder'), meta: { id: '12' } },
    { path: '/index/merchantWEB', name: '商户提现', component: _import('merchant/merchantWEB'), meta: { id: '13' } },
    { path: '/index/merchantDoc', name: '开发文档', component: _import('merchant/merchantDoc'), meta: { id: '14' } },

    {
        path: '/index/merchantSelfRechargeList',
        name: '自助充值代付订单',
        component: _import('merchant/merchantSelfRechargeList'),
        meta: { id: '108801' }
    },

    {
        path: '/index/merchantPayerList',
        // name: '代付钱包列表\/自助充值',
        name: '代付钱包列表',
        component: _import('merchant/merchantPayerList'),
        meta: { id: '101' }
    },

    //外包手续费流水
    {
        path: '/index/merchantPayerFeeList',
        name: '商户手续费流水',
        component: _import('merchant/merchantPayerFeeList'),
        meta: { id: '102' }
    },

    {
        path: '/index/merchantHolderList',
        name: '收款钱包列表',
        component: _import('merchant/merchantHolderList'),
        meta: { id: '208801' }
    },

    {
        path: '/index/merchantDepositList',
        name: '收款订单',
        component: _import('merchant/merchantDepositList'),
        meta: { id: '208802' }
    },


    {
        path: '/index/agentsMerchantDepositList',
        name: '商户收款订单',
        component: _import('mall/agentsMerchantDepositList'),
        meta: { id: '2302' }
    },

    // {
    //     path: '/index/queryMerchantCardPayerReturnPage',
    //     name: '代付冲正订单',
    //     component: _import('merchant/queryMerchantCardPayerReturnPage'),
    //     meta: { id: '103' }
    // },



    /*
     * 系统管理
     * @ mid 对应permission_info的ID
     * @ 各个外包的号段管理MENU_ID不同
     * */
    { path: '/index/menu', name: '菜单管理', component: _import('sys/menu'), meta: { id: '18' }, mid: 59 }, //mid==>menu id
    { path: '/index/people', name: '后台用户', component: _import('sys/people'), meta: { id: '16' }, mid: 26 },
    { path: '/index/role', name: '后台角色', component: _import('sys/role'), meta: { id: '17' }, mid: 27 },
    { path: '/index/card', name: '号段管理', component: _import('sys/card'), meta: { id: '19' }, mid: 128 }, //各个外包的MENU_ID不同

    /*
    * 三方管理 id 101开始
    * @id 自定义的id 不可重复
    * @mid 对应permission_info的ID
    * */
    // {
    //     path: '/channel/cardChannelList',
    //     name: '三方通道分配',
    //     component: _import('channel/cardChannelList'),
    //     meta: { id: '101' },
    //     mid: 96
    // }, 

    {
        path: '/channel/coinWithdrawChannelList',
        name: '代付通道分配',
        component: _import('channel/coinWithdrawChannelList'),
        meta: { id: '101' },
        mid: 96
    }, 
    


    {
        path: '/channel/coinChannelWithdrawFlowList',
        name: '手续费流水',
        component: _import('channel/coinChannelWithdrawFlowList'),
        meta: { id: '102' },
        mid: 189
    },

    // {
    //     path: '/channel/coinChannelDepositFlowList',
    //     name: '代收手续费流水',
    //     component: _import('channel/coinChannelDepositFlowList'),
    //     meta: { id: '1021' },
    //     mid: 189
    // },



    {
        path: '/channel/coinDepositChannelList',
        name: '代收通道分配',
        component: _import('channel/coinDepositChannelList'),
        meta: { id: '103' },
        mid: 162
    }, 

    /**
     * 三方钱包管理
     * 
     */

    //  {
    //     path: '/coin/coinHolderList',
    //     name: '收款码管理',
    //     component: _import('coin/coinHolderList'),
    //     meta: { id: '301' },
    //     mid: 168
    // },
    // {
    //     path: '/coin/coinHolderFlowList',
    //     name: '收款码账变明细',
    //     component: _import('coin/coinHolderFlowList'),
    //     meta: { id: '302' },
    //     mid: 172,
    // },

    {
        path: '/coin/coinPayerList',
        name: '代付钱包列表',
        component: _import('coin/coinPayerList'),
        meta: { id: '303' },
        mid: 174
    },
    {
        path: '/coin/coinPayerFlowList',
        name: '代付钱包账变明细',
        component: _import('coin/coinPayerFlowList'),
        meta: { id: '304' },
        mid: 178,
    },

    // {
    //     path: '/coin/coinHolderQueue',
    //     name: '收款码接单队列',
    //     component: _import('coin/coinHolderQueue'),
    //     meta: { id: '305' },
    //     mid: 197
    // },


    /**
     * 银行卡管理
     * 
     */
    {
        path: '/payer/cardPayerList',
        name: '银行卡列表',
        component: _import('payer/cardPayerList'),
        meta: { id: '201' },
        mid: 80
    },

    {
        path: '/payer/cardPayerRechargeOrder',
        name: '银行卡充值订单',
        component: _import('payer/cardPayerRechargeOrder'),
        meta: { id: '202' },
        mid: 82,
    },

    {
        path: '/payer/cardPayerFlowList',
        name: '银行卡交易流水',
        component: _import('payer/cardPayerFlowList'),
        meta: { id: '203' },
        mid: 84,
    }, 

    // {
    //     path: '/payer/cardPayerSmsAutoRechargeFlowList',
    //     name: '短信自动上分明细',
    //     component: _import('payer/cardPayerSmsAutoRechargeFlowList'),
    //     meta: { id: '215' },
    //     mid: 123,
    // },


    // {
    //     path: '/payer/cardPayerBocSmsFlowList',
    //     name: '短信列表/中银',
    //     component: _import('payer/cardPayerBocSmsFlowList'),
    //     meta: { id: '206' },
    //     mid: 125,
    // },


    // {
    //     path: '/payer/cardPayerWithdrawList',
    //     name: '三方代付订单列表',
    //     component: _import('payer/cardPayerWithdrawList'),
    //     meta: { id: '204' },
    //     mid: 103,
    // },

    /**
     * 短信管理
     * 
     */
    {
        path: '/payer/ipBlockList',
        name: 'IP黑名单',
        component: _import('payer/ipBlockList'),
        meta: { id: '2003' }, //自定义 但不可重复
        mid: 226, //menu id | permission_info
    },


    {
        path: '/coin/coinPayerWithdrawList',
        name: '三方代付订单列表',
        component: _import('coin/coinPayerWithdrawList'),
        meta: { id: '204' },
        mid: 103,
    },
 

    {
        path: '/coin/coinPayerDepositList',
        name: '三方代收订单列表',
        component: _import('coin/coinPayerDepositList'),
        meta: { id: '2041' },
        mid: 224,
    },

    {
        path: '/app/appRecordList',
        name: '银联APP交易记录',
        component: _import('app/appRecordList'),
        meta: { id: '1955' },
        mid: 195,
    },

    // {
    //     path: '/coin/coinPayerReturnList',
    //     name: '三方代付冲正订单',
    //     component: _import('coin/coinPayerReturnList'),
    //     meta: { id: '208' },
    //     mid: 120,
    // },


    // {
    //     path: '/coin/coinPayerReturnList',
    //     name: '三方代付冲正订单',
    //     component: _import('coin/coinPayerReturnList'),
    //     meta: { id: '2041' },
    //     mid: 201,
    // },

    // {
    //     path: '/coin/coinPayerDepositList',
    //     name: '三方代收订单列表',
    //     component: _import('coin/coinPayerDepositList'),
    //     meta: { id: '208' },
    //     mid: 120,
    // },

    {
        path: '/payer/payerMoneyChange',
        name: '平台代付手续费明细',
        component: _import('payer/payerMoneyChange'),
        meta: { id: '901' },
        mid: 137, //各个外包的手续费ID
    },

    {
        path: '/payer/payerAmtMoneyChange',
        name: '平台代付跑量明细',
        component: _import('payer/payerAmtMoneyChange'),
        meta: { id: '902' },
        mid: 139, //各个外包的跑量ID
    },

    /**
     * 码商管理
     * 
     */ 

    {
        path: '/card/cardMerchant',
        name: '码商列表',
        component: _import('card/cardMerchant'),
        meta: { id: '881001' },
        mid: 142, //卡主列表 :PERMISSIION_INFO->ID
    },

    // {
    //     path: '/card/cardAlipay',
    //     name: '码商支付宝商家列表',
    //     component: _import('card/cardAlipay'),
    //     meta: { id: '88206' },
    //     mid: 206, //卡主列表 :PERMISSIION_INFO->ID
    // },

        {
            path: '/card/cardAlipay',
            name: '支付宝商家管理',
            component: _import('card/cardAlipay'),
            meta: { id: '88206' },
            mid: 206,
        },
        {
            path: '/coin/coinHolderList',
            name: '个人码管理',
            component: _import('coin/coinHolderList'),
            meta: { id: '88515' },
            mid: 215,
        },
        {
            path: '/coin/coinHolderBusinessList',
            name: '商家码管理',
            component: _import('coin/coinHolderBusinessList'),
            meta: { id: '88511' },
            mid: 211,
        },

        {
            path: '/coin/coinHolderFlowList',
            name: '总收款码账变明细',
            component: _import('coin/coinHolderFlowList'),
            meta: { id: '88519' },
            mid: 219,
        },

        {
            path: '/coin/coinHolderQueue',
            name: '总收款码接单队列',
            component: _import('coin/coinHolderQueue'),
            meta: { id: '88521' },
            mid: 221,
        },

        {
            path: '/card/cardMerchantReport',
            name: '码商统计报表',
            component: _import('card/cardMerchantReport'),
            meta: { id: '881005' },
            mid: 199,
        },
    {
        path: '/card/cardMerchantOperator',
        name: '码商下挂操作员',
        component: _import('card/cardMerchantOperator'),
        meta: { id: '881002' },
        mid: 143,
    },

    {
        path: '/card/cardMerchantFollower',
        name: '码商下挂收款人',
        component: _import('card/cardMerchantFollower'),
        meta: { id: '881003' },
        mid: 144,
    },

    {
        path: '/card/cardMerchantReport',
        name: '码商统计报表',
        component: _import('card/cardMerchantReport'),
        meta: { id: '881005' },
        mid: 199,
    },

    {
        path: '/card/cardMerchantMoneyChange',
        name: '码商账变明细',
        component: _import('card/cardMerchantMoneyChange'),
        meta: { id: '881006' },
        mid: 201,
    },

    {
        path: '/card/cardMerchantProfitFlow',
        name: '码商分润明细',
        component: _import('card/cardMerchantProfitFlow'),
        meta: { id: '881007' },
        mid: 228,
    },

    {
        path: '/follower/orderList',
        name: '收款人收款订单',
        component: _import('follower/followerOrderList'),
        meta: { id: '883004' },
        mid: 145, //PERMISSIION_INFO->ID
    },

    {
        path: '/follower/qrcodeList',
        name: '收款人收款二维码',
        component: _import('follower/followerQrcodeList'),
        meta: { id: '883005' },
        mid: 146, //PERMISSIION_INFO->ID
    },

    /**
     * 前台卡主菜单 
     * @06/08/2021
     * 前台码商管理
     * @11/16/2023
     * 
     */
    {
        path: '/cardAgent/cardAgentRechargeOrder',
        name: '成功充值流水',
        component: _import('cardAgent/cardAgentRechargeOrder'),
        meta: { id: '882001' },
    },
    
    //码商
    {
        path: '/cardAgent/coinAgentDepositList',
        name: '收款订单管理',
        component: _import('cardAgent/coinAgentDepositList'),
        meta: { id: '882002' },
    }, 

    {
        path: '/cardAgent/coinOperatorMerchantList',
        name: '操作员管理',
        component: _import('cardAgent/coinOperatorMerchantList'),
        meta: { id: '882003' },
    },


    {
        path: '/cardAgent/cardAgentMerchantMoneyChange',
        name: '码商账变明细',
        component: _import('cardAgent/cardAgentMerchantMoneyChange'),
        meta: { id: '882006' },
    },

    {
        path: '/cardAgent/cardAgentProfitFlow',
        name: '码商分润明细',
        component: _import('cardAgent/cardAgentProfitFlow'),
        meta: { id: '882007' },
    },

    // {
    //     path: '/cardAgent/coinFollowerMerchantList',
    //     name: '收款人管理',
    //     component: _import('cardAgent/coinFollowerMerchantList'),
    //     meta: { id: '882004' },
    // },


    {
        path: '/cardAgent/cardAgentAlipay',
        name: '支付宝商家管理',
        component: _import('cardAgent/cardAgentAlipay'),
        meta: { id: '882015' },
    },

    {
        path: '/cardAgent/cardAgentBusinessHolderList',
        name: '商家码管理',
        component: _import('cardAgent/cardAgentBusinessHolderList'),
        meta: { id: '882025' },
    },


    {
        path: '/cardAgent/coinAgentHolderList',
        name: '个码管理',
        component: _import('cardAgent/coinAgentHolderList'),
        meta: { id: '882005' },
    },


    //码商操作员
    {
        path: '/operator/coinOperatorDepositList',
        name: '收款订单管理',
        component: _import('operator/coinOperatorDepositList'),
        meta: { id: '883001' },
    }, 

    {
        path: '/operator/coinOperatorMerchantList',
        name: '收款人管理',
        component: _import('operator/coinOperatorMerchantList'),
        meta: { id: '883002' },
    },

    {
        path: '/cardAgent/cardOperatorAlipay',
        name: '支付宝商家管理',
        component: _import('cardAgent/cardOperatorAlipay'),
        meta: { id: '883015' },
    },

      {
        path: '/operator/coinOperatorHolderList',
        name: '商家码管理',
        component: _import('operator/coinOperatorHolderList'),
        meta: { id: '883003' },
    },


    {
        path: '/operator/coinOperatorPersonHolderList',
        name: '个码管理',
        component: _import('operator/coinOperatorPersonHolderList'),
        meta: { id: '883006' },
    },

     
    {
        path: '/coin/merchantSelfRechargeList',
        name: '商户自助充值订单',
        component: _import('coin/merchantSelfRechargeList'),
        meta: { id: '883005' },
        mid: 187, //商户自助充值订单
    },


     /**
     * @合作伙伴结算管理/外包
     * @partner
     */
      {
        path: '/partner/partnerList',
        name: '外包列表',
        component: _import('partner/partnerList'),
        meta: { id: '801' },
        mid: 192,
    },

    {
        path: '/partner/partnerMoneyChange',
        name: '外包手续费账变明细',
        component: _import('partner/partnerMoneyChange'),
        meta: { id: '802' },
        mid: 193,
    },

    {
        path: '/partner/partnerAmtMoneyChange',
        name: '外包跑量账变明细',
        component: _import('partner/partnerAmtMoneyChange'),
        meta: { id: '803' },
        mid: 194,
    },


]

let MenuList = [
    {
        path: '/index',
        redirect: '/index/home',
        name: '管理平台',
        meta: { isMeun: 1 },
        component: _import('index'),
        children: []
    },
    { path: '*', component: _import('errorPage/404') },
    { path: '/menu', component: _import('sys/menu') },
]


//处理登录系统权限
let handelAdmin = () => {
    let permissionInfo = sessionStorage.permissionInfo && JSON.parse(sessionStorage.permissionInfo) || []
    let rlist = match(permissionInfo)
    rlist.unshift(MENU[0])
    permissionInfo.unshift(
        { name: '快捷入口', path: '', children: [
        {  
            path: '/index/home',
            name: '首页',
            component: _import('payer/index'),  
            
        },

        { path: '/index/mallList', name: '商户列表', component: _import('mall/mallList'), meta: { id: '4' }, mid: 8 },
        {
            path: '/card/cardAlipay',
            name: '支付宝商家管理',
            component: _import('card/cardAlipay'),
            meta: { id: '88206' },
            mid: 206,
        },
        {
            path: '/coin/coinHolderBusinessList',
            name: '商家收款码管理',
            component: _import('coin/coinHolderBusinessList'),
            meta: { id: '88511' },
            mid: 211,
        },
        {
            path: '/coin/coinHolderList',
            name: '个人收款码管理',
            component: _import('coin/coinHolderList'),
            meta: { id: '88515' },
            mid: 215,
        },

        // {
        //     path: '/index/reportForm',
        //     name: '商户统计报表',
        //     component: _import('mall/reportForm'),
        //     meta: { id: '24' },
        // },
        // {
        //     path: '/coin/coinHolderList',
        //     name: '收款码管理',
        //     component: _import('coin/coinHolderList'),
        //     meta: { id: '301' },
        //     mid: 168
        // },
        // {
        //     path: '/coin/coinHolderQueue',
        //     name: '收款码接单队列',
        //     component: _import('coin/coinHolderQueue'),
        //     meta: { id: '305' },
        //     mid: 197
        // },
        // {
        //     path: '/card/cardAlipay',
        //     name: '支付宝商家管理',
        //     component: _import('card/cardAlipay'),
        //     meta: { id: '88206' },
        //     mid: 206,
        // },
        // {
        //     path: '/coin/coinHolderList',
        //     name: '个人码管理',
        //     component: _import('coin/coinHolderList'),
        //     meta: { id: '88515' },
        //     mid: 215,
        // },
        // {
        //     path: '/coin/coinHolderBusinessList',
        //     name: '商家码管理',
        //     component: _import('coin/coinHolderBusinessList'),
        //     meta: { id: '88511' },
        //     mid: 211,
        // },

        // {
        //     path: '/coin/coinHolderFlowList',
        //     name: '总收款码账变明细',
        //     component: _import('coin/coinHolderFlowList'),
        //     meta: { id: '88519' },
        //     mid: 219,
        // },

        // {
        //     path: '/coin/coinHolderQueue',
        //     name: '总收款码接单队列',
        //     component: _import('coin/coinHolderQueue'),
        //     meta: { id: '88521' },
        //     mid: 221,
        // },

        // {
        //     path: '/card/cardMerchantReport',
        //     name: '码商统计报表',
        //     component: _import('card/cardMerchantReport'),
        //     meta: { id: '881005' },
        //     mid: 199,
        // },



    ] })
    MenuList[0].children = rlist
    sessionStorage.menu = JSON.stringify(permissionInfo)
    Router.addRoutes(MenuList) //添加路由

    //匹配数据
    function match(permissionInfo) {
        let routerList = []
        g(permissionInfo)

        function g(r) {
            r.forEach(i => {
                let children = i.childrenList || []
                i.children = children
                i.name = i.menuName
                if (i.isMenu == 1) {
                    g(children)
                } else {
                    if (i.isMenu == 2) {

                        let item = MENU.find(m => m.mid == i.id)
                        if (item) {
                            //找到相对于路由
                            let autoArr = i.childrenList || []
                            i.path = item.path
                            item.meta.auto = autoArr.map(i => i.identifier)
                            routerList.push(item)
                        }
                    }
                }
            })
        }

        return routerList
    }

}

let addRouter = (key, name) => {
    sessionStorage.routerQuery = JSON.stringify({ key, name })

    if (key != 'admin') { //merchant:0 agent:1,2 card:-1,follower:-2
        let router = configMenu[key]
        if (name) {
            MenuList[0].name = name
        }
        let ridList = getRid(router)
        let filterMenu = MENU.filter(i => {
            return ridList.includes(i.meta.id + '')
        })
        MenuList[0].children = filterMenu
        const menu = handelMenuList(router)
        sessionStorage.menu = JSON.stringify(menu)
        Router.addRoutes(MenuList) //添加路由
    } else {
        handelAdmin() //管理员
    }

}


//处理菜单列表
const handelMenuList = (list) => {
    let arr = list.map(el => {
        let children = el.children || []
        if (children.length) {
            el.children = handelMenuList(el.children)
        } else {
            let obj = getItem(el.rid)
            el = { ...el, ...obj }
        }
        return el
    })
    return arr
}

function getItem(id) {
    let item = MENU.find(i => i.meta.id == id) || {}
    let { path, name, meta } = item
    return { path, name, meta }
}

//获取树形的rid
function getRid(router) {
    let ridList = []
    g(router)

    function g(r) {
        r.forEach(i => {
            let children = i.children || []
            if (children.length) {
                g(children)
            } else {
                ridList.push(i.rid)
            }
        })
    }

    return ridList
}

export default addRouter