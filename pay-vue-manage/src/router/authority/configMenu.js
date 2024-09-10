import _import from "../_import";

//index.vue addRouter
let admin  =    [
    //  { rid:'1', children:[]},
    //  { name:'平台运营',children:[  {rid:'3'},{rid:'2'} ] }, //平台运营
    //  { name:'商户管理',children:[  {rid:'4'},{rid:'5'},{rid:'15'},{rid:'6'},{rid:'20'} ] },
    //  { name:'代理管理',children:[  {rid:'20'},] },
    //  { name:'订单管理',children:[  {rid:'7'},] },
    //  { name:'通道管理',children:[  {rid:'9'} ] },
    //  { name:'系统管理',children:[  {rid:'16'},{rid:'17'},{rid:'18'} ] }, //对应addRouter.js路由 | rid子菜单
]
let administrator = admin

let merchant = [
    { rid:'1', children:[]},  //首页:商户 
    { rid:'13', children:[]}, //WEB代付
    { rid:'12', children:[]}, //代付订单
    { rid:'208802', children:[]}, //收款订单
    { rid:'31', children:[]}, //商户资金账变明细
    //{ rid:'102', children:[]}, //商户手续费流水

    //{ rid:'101', children:[]}, //代付钱包列表/自助充值  
    //{ rid:'103', children:[]}, //冲正订单 
    //{ rid:'108801', children:[]}, //商户自助充值订单 
    //{ rid:'208801', children:[]}, //代收钱包/自助补单
    //{ rid:'11', children:[]}, //自助订单流水记录 
  
    { rid:'14', children:[]}, //开发文档
]

let agent = [
    { rid:'1', children:[]},  //首页:代理 总代
    { rid:'22', children:[]}, //下级商户充值订单
    { rid:'23', children:[]}, //下级商户提现/代付订单
    { rid:'2302', children:[]}, //代收钱包订单列表
    //{ rid:'24', children:[]}, //代理下绷商户统计报表 FIXME
    { rid:'1001', children:[]}, //代理账变明细 rid 对应 addRouter中的rid
    { rid:'1002', children:[]}, //代理下级商户
    // { rid:'1003', children:[]}, //代理下级商户钱包
    // { rid:'1005', children:[]}, //代理下级商户收款钱包
]

let card = [
    { rid:'1', children:[]},  //首页:码商
    //{ rid:'882000', children:[]}, // 商户自助充值订单
   // { rid:'882001', children:[]}, // 已充值记录流水
    { rid:'882002', children:[]}, // 收款订单管理 
    // { rid:'882004', children:[]}, // 收款人
   
    { rid:'882015', children:[]}, // 支付宝商家管理
    { rid:'882025', children:[]}, // 商家码管理
    { rid:'882005', children:[]}, // 个码管理
    { rid:'882006', children:[]}, // 码商账变明细
    { rid:'882007', children:[]}, // 码商分润明细
    { rid:'882003', children:[]}, // 码商操作员管理
]

let operator = [
    { rid:'1', children:[]},  //首页（码商操作员）
    { rid:'883001', children:[]}, // 收款订单管理
    { rid:'883015', children:[]}, // 收款人||码商操作员
    { rid:'883003', children:[]}, // 收款二维码管理||码商操作员
    { rid:'883006', children:[]}, // 收款二维码管理||码商操作员
]

 
let follower = [
    { rid:'1', children:[]},  //首页
    { rid:'883004', children:[]}, //收款人收款订单
    { rid:'883005', children:[]}, //收款人收款二维码管理
];

handelName(admin);
handelName(merchant);
handelName(agent);
handelName(card);
handelName(operator);
handelName(follower);
export default {
    admin,
    merchant,
    agent,
    card,
    operator,
    follower,
    administrator
}

function handelName(m){
    m.forEach(e => {
       if(e.children){
        e.children.forEach(e2=>{
            e2.fName = e.name
        })
       }
      
    });

}