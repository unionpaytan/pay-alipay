import Paging from '../components/paging.vue'
import FloatBall from '../components/floatball.vue'
import Roll from '../components/roll.vue'
import intercepter from './intercepter'
//import numeral from './numeral'
import * as api from '@/api/index'
import rules from './rules'
import * as utils from '../utils/index'
import moment from 'moment'
import axios from "axios";
const ENV = process.env.VUE_APP_ENV
export default {
    install(Vue, opts) {
        intercepter(Vue, opts)

        Vue.component('Paging', Paging);
        Vue.component('FloatBall', FloatBall); //装载组件
        Vue.component('Roll', Roll); //装载组件


        Vue.mixin({
            mixins: [rules],
            data() {
                return {
                    // m_numeral:numeral,
                    m_utils: utils,
                    ENV, //环境
                    formData: { },
                    _formData: '', //记录初始数据
                    m_loading: false,
                    m_list: [],
                    modal_list: [],
                    modal_page: {
                        page: 1,
                        rows: 20, //行数
                        total: 10
                    },
                    m_page: {
                        page: 1,
                        rows: 20, //行数
                        total: 10
                    },
                    m_host: '',
                    m_api: api,
                    m_isadd: true,
                    m_show: false,
                    m_editId: '', //编辑id
                    m_delId: [], //删除id (勾选id)
                    m_checkData: [], //勾选的数据
                    m_editData: { }, //编辑数据(选中的数据)
                    m_payMerchantInfo: {
                    },

                    channelFeeList: [{
                        id: '1',
                        name: '余额扣除'
                    }, {
                        id: '2',
                        name: '分开结算'
                    }], //状态

                    statusList: [
                        {
                            id: '0',
                            name: '禁用'
                        },
                        {
                            id: '1',
                            name: '正常'
                        },
                    ],


                    //通道状态
                    chlStatusList: [{
                        id: '0',
                        name: '禁用'
                    }, {
                        id: '1',
                        name: '正常'
                    }],
                    //代收/代付通道状态
                    channelStatusType: [{
                        id: '0',
                        name: '禁用'
                    }, {
                        id: '1',
                        name: '正常'
                    }],

                    usdtLiveFixedType: [{
                        id: '0',
                        name: '自动采集'
                    }, {
                        id: '1',
                        name: '人工固定'
                    }],


                    isUidType: [
                        {
                            id: '0',
                            name: '否'
                        },
                        {
                            id: '1',
                            name: '是'
                        },
                       
                    ],

                    //coin币
                    coinStatusType: [
                        {
                            id: '0',
                            name: '禁用'
                        },
                        {
                            id: '1',
                            name: '可用'
                        },
                        {
                            id: '2',
                            name: '启用'
                        },
                    ],


                    //银行卡
                    payerCardStatusList: [
                        {
                            id: '0',
                            name: '禁用'
                        },
                        {
                            id: '1',
                            name: '可用'
                        },
                        {
                            id: '2',
                            name: '已分配'
                        },
                    ],

                    //银行卡
                    payerCardTypeList: [
                        {
                            id: '0',
                            name: '银行卡'
                        },
                        {
                            id: '1',
                            name: '手机卡'
                        },
                    ],

                     //游戏ID
                     queryGameInfoList: [
                        {
                            gameId: "888",
                            gameName: '所有游戏'
                        },
                    ],


                    //卡通道流水类型
                    rechargeTypeList: [{
                        id: '1',
                        name: '通道充值'
                    }, {
                        id: '2',
                        name: '手续费充值'
                    }],

                    merchantFlowType: [{
                        id: '0',
                        name: '充值'
                    }, {
                        id: '1',
                        name: '代收'
                    }
                    ],

                    //流水类型 
                    flowTypeList: [{
                        id: '0',
                        name: '充值'
                    }, {
                        id: '1',
                        name: '代收/冲正'
                    }, {
                        id: '2',
                        name: '代付'
                    }
                    ],

                    //流水类型 
                    partnerFlowTypeList: [{
                        id: '0',
                        name: '充值'
                    }, {
                        id: '1',
                        name: '代收/冲正'
                    },
                    {
                        id: '2',
                        name: '代付'
                    }
                    ],


                    //短信类型 
                    smsTypeList: [{
                        id: '0',
                        name: '收入'
                    },

                    {
                        id: '1',
                        name: '支出'
                    }, 
                    // {
                    //     id: '3',
                    //     name: 'OTP'
                    // },
                    ],  
                     //余额修改类型
                     balanceTypeList: [
                        {
                            name: '自动扣除汇率',
                            id: '1'
                        },
                        {
                            name: '以同金额充值',
                            id: '0'
                        }, 
                    ],


                    //代理类型
                    agentRateList: [
                        {
                            name: '商户',
                            id: '0'
                        },
                        {
                            name: '代理',
                            id: '1'
                        },
                        {
                            name: '总代'
                            , id: '2'
                        },
                    ],

                    pollingList: [
                        {
                            name: '单独(否)',
                            id: '0'
                        },
                        {
                            name: '轮询(参与)',
                            id: '1'
                        },
                       
                    ],

                    //代理类型
                    merchantRateForAgentList: [
                        {
                            name: '商户',
                            id: '0'
                        },
                        {
                            name: '代理',
                            id: '1'
                        },
                    ],

                    depositStatusList: [{
                        id: '0',
                        name: '待支付'
                    }, {
                        id: '1',
                        name: '成功'
                    }, {
                        id: '2',
                        name: '失败'
                    }, {
                        id: '-1',
                        name: '订单超时'
                    }],

                    withdrawStatusList: [{
                        id: '0',
                        name: '未知'
                    }, {
                        id: '1',
                        name: '成功'
                    }, {
                        id: '2',
                        name: '失败'
                    }, {
                        id: '3',
                        name: '处理中'
                    }],

                    payerBankTypeStatus: [
                        {
                            id: '0',
                            name: '自动'
                        },
                        {
                            id: '-1',
                            name: '人工'
                        },
                    ],
                    //提现状态
                    payerWithdrawStatusList: [{
                        id: '0',
                        name: '排队中'
                    }, {
                        id: '1',
                        name: '成功'
                    }, {
                        id: '2',
                        name: '失败'
                    }, {
                        id: '3',
                        name: '正在处理'
                    },
                    {
                        id: '-1',
                        name: '未知'
                    }],

                    //三方代收充值状态
                    payerDepositStatusList: [
                        {
                        id: '0',
                        name: '待支付'
                    }, {
                        id: '1',
                        name: '成功'
                    }, {
                        id: '2',
                        name: '失败'
                    },
                    {
                        id: '-1',
                        name: '订单超时'
                    }
                ],


                    //银行类表
                    bankList: [
                        {
                            id: '105100000017',
                            name: '建设银行'
                        },
                        {
                            id: '10692955611',//10692955611
                            name: '兴业银行'
                        },
                        {
                            id: '104100000004',
                            name: '中国银行'
                        },
                        {
                            id: '106980096336',
                            name: '福建农信'
                        },
                        {
                            id: '95580',
                            name: '邮储银行'
                        },
                    ],

                    doubleConfirmList: [
                        {
                            id: '0',
                            name: '否',
                        },
                        {
                            id: '1',
                            name: '是',
                        }, 
                    ],

                    coinTypeList: [
                        {
                            id: '0',
                            name: '微信',
                        },
                        {
                            id: '1',
                            name: '支付宝',
                        },
                        {
                            id: '2',
                            name: '聚合码',
                        },
                    ],

                    //订单来源
                    requestTypeList: [{
                        id: 'API',
                        name: 'API'
                    }, {
                        id: 'WEB',
                        name: '后台提交'
                    },
                        // {
                        //     id: 'PLATFORM', 
                        //     name: 'PLATFORM'   // name: '平台提现'
                        // }
                    ],
                    m_tableLoading: false,

                }
            },
            mounted() {
                this._formData = this._dep(this.formData)
                if (this.ENV == 'development') {
                    this.m_host = window.location.origin + ":8080" //development
                } else {
                    //文件下载时
                    this.m_host = window.location.origin + "/api" //build
                }

            },
            methods: {
                checkId(v) {
                    console.log("check id:" + v);
                },
                //判断权限
                handelAuto(key) {
                    return this.$route.meta.auto.some(i => i == key)
                },
                _dep(obj) {
                    return JSON.parse(JSON.stringify(obj))
                },
                //成功提示
                m_success(mes) {
                    this.$message({
                        message: mes,
                        type: 'success'
                    })
                },
                //失败提示
                m_error(mes) {
                    this.$message({
                        message: mes,
                        type: 'error'
                    })
                },
                m_warning(mes) {
                    this.$message({
                        message: mes,
                        type: 'warning'
                    })
                },
                //待确认对提示框
                m_confirm(data) {
                    let msg = data.msg || ''
                    let title = data.title || '提示'
                    let type = data.type || ''
                    return this.$confirm(msg, title, {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type
                    }).then(() => {
                        return true
                    }).catch(() => {
                        return false
                    })
                },

                //新增
                m_addForm() {
                    console.log('新增');
                    this.m_isadd = true //m_isadd?'添加':'编辑'
                    this.formData = JSON.parse(JSON.stringify(this._formData))
                    this.m_show = true
                },

                //编辑用户
                m_editForm(row) {
                    console.log("编辑用户" + row.merchantId)
                    this.formData = JSON.parse(JSON.stringify(this._formData))
                    this.m_isadd = false
                    this.m_editData = JSON.parse(JSON.stringify(row));

                    this.formData = utils.coverObj(this.formData, row)
                    this.formData.id = row.id
                    this.m_show = true

                    this.formData.agentRate = [
                        { label: '商户', value: '0' },
                        { label: '代理', value: '1' },
                        { label: '总代', value: '2' },
                    ]
                },

                //删除一条
                m_oneDel(row) {
                    this.$confirm(`此操作将永久删除该记录, 是否继续?`, '提示', {
                        confirmButtonText: '确认',
                        cancelButtonText: '取消',
                        type: 'warning'
                    }).then(() => {
                        console.log("删除" + row.id)
                        this.delData(row.id, row)
                    }).catch(() => {
                        //取消删除
                    })

                },

                //编辑新增数据 点击确定
                sureOption() {
                    this.$refs['ruleForm'].validate((valid) => {
                        if (valid) {
                            if (this.m_isadd) {
                                //新增
                                this.addData()
                            } else {
                                //编辑
                                this.editData()
                            }
                        } else {
                            console.log('error submit!!');
                            return false;
                        }
                    })

                },

                //搜索按钮
                m_search() {
                    this.m_page.page = 1;
                    this.getDatalist()
                },

                //改变显示个数
                m_changesize(v) {
                    this.m_page.rows = v;
                    this.m_page.page = 1;
                    this.getDatalist();
                },
                //改变页数
                m_changeindex(v) {
                    this.m_page.page = v
                    this.getDatalist()
                },

                modal_changesize(v) {
                    this.modal_page.rows = v;
                    this.modal_page.page = 1;
                    this.getModalDatalist();
                },
                //改变页数
                modal_changeindex(v) {
                    this.modal_page.page = v
                    this.getModalDatalist()
                }, 
                //表格勾选
                modal_handleSelectionChange(val) {
                    // console.log(val)
                    this.m_delId = []
                    this.m_checkData = this._dep(val)
                    let that = this
                    val.forEach(ele => {
                        that.m_delId.push(ele.id)
                    })

                },

                //表格勾选
                m_handleSelectionChange(val) {
                    // console.log(val)
                    this.m_delId = []
                    this.m_checkData = this._dep(val)
                    let taht = this
                    val.forEach(ele => {
                        taht.m_delId.push(ele.id)
                    })

                },
                //表格高亮
                m_handleCurrentChange(val) {
                    // console.log(val)
                    if (val) {
                        this.m_editId = val.id
                        this.m_editData = JSON.parse(JSON.stringify(val))
                    }
                },
                modal_handleCurrentChange(val) {
                    // console.log(val)
                    if (val) {
                        this.m_editId = val.id
                        this.m_editData = JSON.parse(JSON.stringify(val))
                    }
                },

                //获取公共数据
                getBaseData(obj) {
                    let baseData = JSON.parse(sessionStorage.baseData)
                    let arr = baseData.filter(i => i.module == obj.module && i.key_name == obj.key_name)
                    return arr
                },

                upload_excel(uploadUrl) {
                    var i = document.createElement('iframe')

                    i.src = uploadUrl
                    i.style.display = 'none'
                    document.body.appendChild(i)

                },

                m_getQuery(obj) {
                    const params = []

                    Object.keys(obj).forEach((key) => {
                        let value = obj[key]
                        // 如果值为undefined我们将其置空
                        if (typeof value === 'undefined') {
                            value = ''
                        }
                        // 对于需要编码的文本（比如说中文）我们要进行编码
                        params.push([key, encodeURIComponent(value)].join('='))
                    })

                    return params.join('&')
                },
            },
            //过滤器
            filters: {

                contactPrixZero(v) {
                    if (v.length == 10) {
                        return "0" + v;
                    } 
                    return ''
                },

                //三方通道
                coinStatus(v) {
                    if (v == '0') {
                        return '禁用'
                    }
                    if (v == '1') {
                        return '启用'
                    }
                    return ''
                },


                //三方通道
                chlStatus(v) {
                    if (v == '0') {
                        return '禁用'
                    }
                    if (v == '1') {
                        return '启用'
                    }
                    return ''
                },

                partnerStatus(v) {

                    if (v == 0) {
                        return '启用'
                    }
                    if (v == 1) {
                        return '禁用'
                    }
                    return ''
                },


                //USDT汇率类型  
                usdtLiveFixedFilter(v) {
                    if (v == '0') {
                        return '自动采集'
                    }
                    if (v == '1') {
                        return '固定'
                    }
                    return '代付'
                }, 

                //流水
                partnerFlowTypeFilter(v) {
                    if (v == '0') {
                        return '充值'
                    }
                    if (v == '1') {
                        return '代收'
                    }
                    return '代付'
                },


                //流水
                flowTypeFilter(v) {
                    if (v == '0') {
                        return '充值'
                    }
                    if (v == '1') {
                        return '冲正'
                    }
                    return '代付'
                },

                //流水
                feeTypeFilter(v) {
                    if (v == '0') {
                        return '充值'
                    }
                    if (v == '1') {
                        return '代收'
                    }
                    return '代付'
                },

                //手续费结算方式
                channelFeeTypeFilter(v) {

                    if (v == '1') {
                        return '余额扣除'
                    }
                    return '分开结算'
                },

                //回调状态
                returnTypeFilter(v) {
                    if (v == 0) {
                        return '未回调'
                    } else if (v == 1) {
                        return '已回调'
                    }

                },

                //自动上分状态
                rechargeAutoFilter(v) {

                    if (v == '0') {
                        return '未上分'
                    }
                    return '已自动上分'
                },

                //通道类型
                payTypeFilter(v) {
                    if (v == 0) {
                        return '代付'
                    } else if (v == 1) {
                        return '代收'
                    }
                },

                isBindStatusFilter(v) {
                    if (v == '0') {
                        return '离线'
                    } else if (v == '1') {
                        return '在线'
                    }
                    return '未知'
                },

                //卡状态
                payerCardStatusFilter(v) {
                    if (v == '0') {
                        return '禁用'
                    } else if (v == '1') {
                        return '可用'
                    } else if (v == '2') {
                        return '启用'
                    }
                    return '未知'
                },

                payerStatusChangeFilter(v) {
                    if (v == '0') {
                        return '启用'
                    } else if (v == '2') {
                        return '禁用'
                    }
                    return ''
                },

                //卡在线状态
                payerOnlineFilter(v) {
                    if (v == '0') {
                        return '在线'
                    } else if (v == '-1') {
                        return '离线'
                    }
                    return ''
                },

                switchPayerCardStatusClass(v) {

                    if (v > '0') {
                        return 'red'
                    } else {
                        return 'green'
                    }

                },

                isBindStatusClass(v) {
                    if (v == '0') {
                        return 'red'
                    } else if (v == '1') {
                        return 'green'
                    }
                },


                payerCardStatusClass(v) {
                    if (v == '0') {
                        return 'red'
                    } else if (v == '2') {
                        return 'green'
                    } else if (v == '1') {
                        return ''
                    }
                },

                payerStatusChangeClass(v) {
                    if (v == '0') {
                        return 'green'
                    } else if (v == '2') {
                        return 'red'
                    } else if (v == '1') {
                        return ''
                    }
                },

                rechargeStatusClass(v) {
                    if (v == '0') {
                        return ''
                    } else if (v == '1') {
                        return 'green'
                    } else if (v == '2') {
                        return 'red'
                    }
                },

                amtClassFilter(amt) {
                    let value = amt * 1
                    if (isNaN(amt)) {
                        value = 0
                        return ""
                    }
                    if (value < 0) {
                        return 'red'
                    } else if (value >= 10000) {
                        return 'green'
                    }
                },

                amtClassFlowFilter(amt) {
                    let value = amt * 1
                    if (isNaN(amt)) {
                        value = 0
                        return ""
                    }
                    if (value < 0) {
                        return 'red'
                    } else if (value >= 10000) {
                        return ''
                    }
                },


                amtClassRemainFilter(v) {
                    let value = v * 1
                    if (isNaN(v)) {
                        value = 0
                        return ""
                    }
                    if (value < 1000) {
                        return 'red'
                    } else if (value >= 5000) {
                        return 'green'
                    }
                },

                accountBalanceFilter(v) {
                    let value = v * 1
                    if (isNaN(v)) {
                        value = 0
                        return ""
                    }
                    if (value < 1000) {
                        return 'red'
                    } else if (value >= 3000) {
                        return 'green'
                    } else {
                        return ''
                    }
                },


                gameTypeFilter(v) {
                    if (v == '3') {
                        return '大小'
                    } 
                    if (v == '8') {
                        return '单双'
                    } 
                    if (v == '2') {
                        return '对子'
                    } 
                    if (v == '4') {
                        return '三连号'
                    } 
                    if (v == '6') {
                        return 'PK10'
                    } 
                    return ''
                },

                coinTypeFilter(v) {
                    if (v == '0') {
                        return '微信'
                    } 
                    if (v == '1') {
                        return '支付宝'
                    } 
                    if (v == '2') {
                        return '聚合码'
                    } 
                    return ''
                },


                //流水
                rechargeTypeFilter(v) {
                    if (v == '1') {
                        return '通道'
                    }
                    if (v == '2') {
                        return '手续费'
                    }
                    return ''
                },

                //商户充值订单状态
                rechargeStatusFilter(v) {
                    if (v == '0') {
                        return '待支付'
                    }
                    if (v == '1') {
                        return '成功'
                    }
                    if (v == '2') {
                        return '失败'
                    }
                    if (v == '-1') {
                        return '超时'
                    }
                    return '未知'
                },



                //银行短信
                smsNumberFilter(v) {
                    if (v == '95533') {
                        return '建设银行'
                    } else if (v == '10692955611') {
                        return '兴业银行'
                    } else if (v == '95561') {
                        return '兴业银行'
                    }
                    else if (v == '95566') {
                        return '中国银行'
                    } else if (v == '106980096336') {
                        return '福建农信'
                    } else if (v == '95580') {
                        return '邮储银行'
                    }
                    return ''
                },

                //银行编号
                payerBankCodeFilter(v) {
                    if (v == '105100000017') {
                        return '建设银行'
                    }
                    if (v == '10692955611') {
                        return '兴业银行'
                    }
                    if (v == '104100000004') {
                        return '中国银行'
                    }
                    if (v == '95566') {
                        return '中国银行'
                    }
                    if (v == '106980096336') {
                        return '福建农信'
                    }
                    if (v == '95580') {
                        return '邮储银行'
                    }
                    if (v == '95588') {
                        return '工商银行'
                    }
                    if (v == '95568') {
                        return '民生银行'
                    }
                    if (v == '95595') {
                        return '光大银行'
                    }
                    if (v == '106980095558') {
                        return '中信银行'
                    }
                    if (v == '1065795555') {
                        return '招商银行'
                    }
                    if (v == '95599') {
                        return '农业银行'
                    }
                    if (v == '106573095559') {
                        return '交通银行'
                    }
                    if (v == '96262') {
                        return '陕西信合'
                    }
                    if (v == '95526') {
                        return '北京银行'
                    }
                    if (v == '95561') {
                        return '兴业银行'
                    }
                    if (v == '95511') {
                        return '平安银行'
                    }
                    if (v == '95528') {
                        return '浦发银行'
                    }
                    if (v == '95577') {
                        return '华夏银行'
                    }

                    return ''
                },

                isBind(v) {
                    if (v == '0') {
                        return '离线'
                    }
                    if (v == '1') {
                        return '在线'
                    }
                    return '未知'
                },

                //商户
                status(v) {
                    if (v == '0') {
                        return '正常'
                    }
                    if (v == '1') {
                        return '禁用'
                    }
                    return '未知'
                },
                //商户
                bankTypeFilter(v) {
                    if (v == '0') {
                        return '自动'
                    } else if (v == '-1') {
                        return '人工'
                    }

                },

                agentRateFilter(v) {
                    // console.log("商户等级：" + v);
                    if (v == 1) return "代理"
                    if (v == 2) return "总代"
                    return "商户"
                },

                flowType(v) {
                    if (v == '0') {
                        return '充值'
                    }
                    if (v == '1') {
                        return '代收'
                    }
                    if (v == '2') {
                        return '代付'
                    }
                    return '未知'
                },

                flowTypeIcon(v) {
                    if (v == '0') {
                        return '+'
                    }
                    if (v == '1') {
                        return '+'
                    }
                    if (v == '2') {
                        return ''
                    }
                    return '未知'
                }, 

                withdrawStatus(v) {
                    if (v == '0') {
                        return '未知'
                    }
                    if (v == '1') {
                        return '成功'
                    }
                    if (v == '2') {
                        return '失败'
                    }
                    if (v == '3') {
                        return '处理中'
                    }
                    return '未知'
                },

                withdrawStatusFilter(v) {
                    if (v == '0') {
                        return '排队中'
                    }
                    if (v == '1') {
                        return '成功'
                    }
                    if (v == '2') {
                        return '失败'
                    }
                    if (v == '3') {
                        return '处理中'
                    }
                    return '未知'
                },

                depositStatusCssFilter(v) {
                    if (v == '0') {
                        return ''
                    }
                    if (v == '1') {
                        return 'color:#67C23A'
                    }
                    if (v == '2') {
                        return 'color:#ff0000'
                    }
                    
                    return ''
                },
                preStatusFilter(v) {
                    if (v == '0') {
                        return '未确认'
                    }
                    if (v == '1') {
                        return '付款人点击已完成付款按钮'
                    }
                    if (v == '2') {
                        return '已确认'
                    }
                    return ''
                },
                depositStatusFilter(v) {
                    if (v == '0') {
                        return '待支付'
                    }
                    if (v == '1') {
                        return '成功'
                    }
                    if (v == '2') {
                        return '失败'
                    }
                
                    if (v == '-1') {
                        return '订单超时'
                    }
                    return '未知'
                },

                taskReturnFilter(v) {
                    if (v == '0') {
                        return '待处理'
                    }
                    if (v == '1') {
                        return '已处理'
                    }
                    return '-'
                },



                payerWithdrawStatusListFilter(v) {
                    if (v == '0') {
                        return '排队中'
                    }
                    if (v == '1') {
                        return '成功'
                    }
                    if (v == '2') {
                        return "失败"
                    }
                    if (v == '3') {
                        return '正在处理'
                    }
                    return '未知'
                },

                doubleConfirmFilter(v) {
                    if (v == '0') {
                        return '否'
                    }
                    if (v == '1') {
                        return '是'
                    } 
                    return ''
                },

                payerWithdrawPauseFilter(v) {
                    if (v == '0') {
                        return '通过'
                    }
                    if (v == '1') {
                        return '未审'
                    }
                   
                    return ''
                },

                withdrawStatusClass(v) {
                    if (v == '0') {
                        return ''
                    }
                    if (v == '1') {
                        return 'green'
                    }
                    if (v == '2') {
                        return 'red'
                    }
                    if (v == '3') {
                        return ''
                    }
                    if (v == '-1') {
                        return 'red'
                    }
                },

                payerOnlineStatusClass(v) {
                    if (v == '0') {
                        return 'gray'
                    }
                    if (v == '-1') {
                        return 'red'
                    }
                },

                flowTypeClass(v) {

                    if (v == '0') {
                        return 'green' //充值
                    }
                    if (v == '1') {
                        return '' //代收 无冲正
                    }
                    if (v == '2') {
                        return ''
                    }

                },

                moneyTypeClass(v) {

                    if (v == '0') {
                        return 'green' //充值
                    }
                    if (v == '1') {
                        return '' //收
                    }
                    if (v == '2') { //付
                        return 'red'
                    }

                },


                requestType(v) {
                    //return v
                    if (v == 'API') {
                        return '接口提交'
                    }
                    if (v == 'WEB') {
                        return '后台提交'
                    }
                    if (v == 'PLATFORM') {
                        return '平台提现'
                    }

                    return '未知'

                },

                percent(v) {
                    return (v * 100) + '%'
                },


                time(v, format) {
                    return v
                    var format = format || 'YYYY-MM-DD hh:mm:ss'
                    return new moment(v).format(format)
                },
                convertCurrency(v) {
                    return utils.convertCurrency(v)
                },

                //获得上级用户
                getParentIdFilter(v) {
                    //console.log("用户ID:"+v);
                    if (v) {
                        //查询商户ID
                        //封装axios
                        /* axios.post('/merchantAcountManage/queryMerchantParentId', {merchantId:v}).then(res=>{
                             console.log("===>>>>" + res.data.merchantName)
                         });*/
                        //异步加载
                        api.queryMerchantParentId({ merchantId: v }).then(res => {
                            //console.log("==>" +res.data.merchantId + "-" + res.data.merchantName)
                            console.log(res.data.merchantId)
                            return res.data.merchantId
                        })
                    } else {
                        return "--"
                    }

                }

            }


        })
    }
}