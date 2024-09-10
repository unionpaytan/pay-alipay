<!-- 收款人钱包地址  followerQrcodeList
 -->
<template>
  <div>
    <div class="flex flex-m tc topbox">
      <div class="flex-1">
        <div>总收款码数量<span style="font-size:12px;"></span></div>
        <div class="num">{{ countMap.countCoinHolder || 0 }}</div>
      </div> 
    </div>
   <div class="flex flex-m mb20 searchBox">
       <div class="flex flex-m mr20">
        <span class="w100">收款码类别:</span>
        <el-select
          clearable
          class="w150"
          size="medium"
          v-model="search_data.coinType"
          placeholder="收款码类别"
        >
          <el-option label="请选择" value></el-option>
          <el-option
            v-for="item in coinTypeList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
    </div>
 


    <div class="flex flex-m mr20">
        <span class="w100">收款码金额:</span>
        <el-input
          type="number"
          size="medium"
          class="w150"
          v-model="search_data.payerCodePrice"
          clearable
          placeholder="收款码金额"
        ></el-input>
   </div>

       <div class="flex flex-m mr20">
        <span class="w100">收款码ID:</span>
        <el-input
          type="text"
          size="medium"
          class="w200"
          v-model="search_data.payerAddr"
          clearable
          placeholder="收款码ID"
        ></el-input>
   </div>

    <div class="flex flex-m mr10">
        <span class="w100">收款码状态:</span>
        <el-select
          class="w150"
          size="medium"
          v-model="search_data.status"
          placeholder="请选择">
          <el-option label="请选择状态" value></el-option>
          <el-option
            v-for="item in coinStatusType"
            :key="item.value"
            :label="item.name"
            :value="item.id"
            width="200"
          ></el-option>
        </el-select>
      </div>


      <div class="flex flex-m mr50">
        <el-button
          class
          size="small"
          icon="el-icon-search"
          @click.native="m_search"
          type="primary"
          >搜索 (ENTER回车键或ESC键)</el-button
        >
      </div> 



   </div> 

    <el-table
      sortable
      :data="m_list"
      v-loading="m_tableLoading"
      @selection-change="m_handleSelectionChange"
      @current-change="m_handleCurrentChange"
      class="mtable"
      :stripe="true"
      tooltip-effect="dark"
      :highlight-current-row="true"
    >
       <el-table-column type="selection" width="25"></el-table-column>
           <el-table-column label="序号" type="index" width="50" align="center">
        <template slot-scope="scope">
          <span>{{ (m_page.page - 1) * m_page.rows + scope.$index + 1 }}</span>
        </template>
      </el-table-column>

        <el-table-column align="center" prop="coinType" label="收款码类别" width="130">
         <template slot-scope="scope">
          {{ scope.row.coinType | coinTypeFilter}}
        </template>
     </el-table-column>

       <el-table-column align="center" prop label="收款码ID">
       <template slot-scope="scope">
          <img class="authurl" :src="'/uploads/' + scope.row.payerAddr  + '.png'" style="width:50px;" alt /><br>
          {{scope.row.payerAddr}}
      </template>
      </el-table-column> 

     <el-table-column align="center" prop label="收款人">
         <template slot-scope="scope">
             {{scope.row.payerName}}<br>
              <!-- {{scope.row.payerIdentity}} -->
         </template>
      </el-table-column>


     <el-table-column  align="center" prop="payerCodePrice" label="金额">
        <template slot-scope="scope">
          <span>{{scope.row.payerCodePrice}}</span>
        </template>
      </el-table-column>
     <el-table-column align="center" prop="payerCodeTimes" label="可收次数"></el-table-column>


     <el-table-column align="center" label="状态" width="100" fixed="right">
        <template slot-scope="scope">
          <span :class="scope.row.status | payerCardStatusClass">{{
            scope.row.status | payerCardStatusFilter
          }}</span>
        </template>
      </el-table-column> 

    </el-table> 
 
    

    <Paging
      class="mt20 mb30"
      :pageIndex="m_page.page"
      :pageSize="m_page.rows"
      :pageTotal="m_page.total"
      @changeSize="m_changesize"
      @changeIndex="m_changeindex"
    ></Paging>
  </div>
</template>

<script> 
import 'babel-polyfill';
import assert from 'assert'
import { isValidChecksumAddress, unpadBuffer, BN } from 'ethereumjs-util'
import qrcode from "qrcode";
import moment from "moment";
import myUpload from 'vue-image-crop-upload';
//import Web3 from "web3"; 
//import TronWeb from "tronweb";

export default {
  name: "followerQrcodeList",
  components: {
			'my-upload': myUpload
		},
  data() {
    return {
      params:"",
      noCircle:true,
      imgDataUrl:"",
      image_show:false,
      rpcURL: "",
      web3: {},
      ABI: {},
      contract: {},
      tronWeb: {},

      sendNum: 0,
      time: null,
      get_loading: false,

      bankCodeDataList: [], //银行列表
      channelCodeList: [], //通道列表

      search_data: {
        payerAddr:"",
      },
      highestAmt: 0,
      //预先 加载 渲染
      countMap: {
         
      },
      queryCoinChannelInfoList: [], //三方通道数组
      payMerchantInfoList:[],//从商户列表中查出 码商(agentRate == '-1')列表
      payerFollowerList:[],
      isShowPayerMerchantId:0,//默认不显示卡主编号
      //新增表格
      formData: {
        id:"",
        deviceId:"",
        merchantId:"",
        payerName:"",//收款人
        payerIdentity:"",//收款人编号 手机后8位数
        payerAddr:"",//钱包地址
        coinType: "1", //0-微信 1-支付宝 2-聚合码
        status: "2", //2-已分配
        remark: "",
        tableMerchantId: "",
        googleCode: "", 
        channelCode:"",//码通道
        emailCode:"",
        payerCodePrice:"",
        payerCodeTimes:1,
        payerLimitDay:150000,
        payerLimitMonth:150000 * 30,
        
      },

      merchant_data: {
        payerName: "",
        payerAddr: "",
        tableMerchantId: "",
        googleCode: "",
      },

      show_camera_image: false, //camera image
      templurl: "",
      authurl: "",
      balance_show: false,
      channel_show:false,
      d_balance_loading: false,
      balance_loading: false,
      merchant_show_del: false,
      _balance_data: {},

      balance_data: {
        merchantId: "", //管理员id
        channelCode: "",
        channelName: "", 
        rechargeAmt:"",
        remark: "", //备注 
        googleCode: "", //google验证码
        channelSingle:"",
        channelRate:"",
        payerName:"",
        payerAddr:"",
        payerAmt:"",
        payerIds:[],
      },

      merchant_show: false,
      merchant_show_del: false,
      agent_show: false, //代理费率
      merchant_loding: false,
      d_merchant_loding: false,
      _channel_data: {},

      channel_data: {  
        channelCode: "", //通道编号
      },

      //钱包验证规则
      m_rules_add: {
        //gameId: [{ required: true, message: "请选择", trigger: "blur" }],
        //channelCode: [{ required: true, message: "请选择", trigger: "blur" }],
        coinType: [{ required: true, message: "请选择", trigger: "blur" }],
        status: [{ required: true, message: "请选择", trigger: "blur" }],
        payerAddr: [{ required: true, message: "请输入", trigger: "blur" }],
        payerIdentity: [{ required: true, message: "请输入", trigger: "blur" }],
        payerCodePrice: [{ required: true, message: "请输入", trigger: "blur" }],
        payerCodeTimes: [{ required: true, message: "请输入", trigger: "blur" }],
        googleCode:[{ required: true, message: "请输入", trigger: "blur" }],
      },

      m_rules_edit: {
        status: [{ required: true, message: "请选择", trigger: "blur" }],
        payerAddr: [{ required: true, message: "请输入", trigger: "blur" }],
        payerIdentity: [{ required: true, message: "请输入", trigger: "blur" }],
        payerCodePrice: [{ required: true, message: "请输入", trigger: "blur" }],
        payerCodeTimes: [{ required: true, message: "请输入", trigger: "blur" }],
        googleCode:[{ required: true, message: "请输入", trigger: "blur" }],
      },

      balanceForm_rules: {
        rechargeAmt: [{ required: true, message: "请输入", trigger: "blur" }],
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }],
      },

      cardPayerForm_rules: {
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }],
      },
    };
  },
  created() {
    this.init();
    // this.initEth();
    // this.initTron();
  },
  mounted() {
    // 绑定enter事件
    this.enterKeyup();
  },
  destroyed() {
    // 销毁enter事件
    this.enterKeyupDestroyed();
  },
  methods: {
    init() {
      this.m_isadd = true;
      this._balance_data = this._dep(this.balance_data);
      this._channel_data = this._dep(this.channel_data);
      this.m_search();
    }, 

     onSelectChangeFollower(val) {
      if (val){
           console.log("onSelectChange - ",val); 
           this.queryPayMerchantNameById(val);
      }  
    },

    toggleShow() {
      this.image_show = !this.image_show;
    },
    cropSuccess(imgDataUrl, field){
      console.log('-------- crop success --------'); 
    },
    cropUploadSuccess(jsonData, field){
      console.log('-------- upload success --------');
      if (jsonData.code == '0000') {
         this.m_success("收款码上传成功");
      }else {
         this.m_success("收款码上传失败" + jsonData.message);
      }
     
      console.log(jsonData);
      console.log('field: ' + field);//codeImage
      this.authurl = process.env.VUE_APP_UPLOAD_PATH + jsonData.data + '.png';
      console.log("authurl -",this.authurl);
      this.formData.payerAddr = jsonData.data; //不包含路径 
      this.image_show = false;
    },
    cropUploadFail(status, field){
      console.log('-------- upload fail --------');
      this.m_error("收款码上传失败");
      console.log(status);
      console.log('field: ' + field);
      this.$refs.myUpload.setStep(1);
      this.image_show = false;
    }, 
 
    enterKey(event) {
      const code = event.keyCode;
      //32 space
      //16 shift
      //27 ESC
      //console.log("key - ",code);
      if (code == 108 || code == 13 || code == 27) {
        if (!this.m_show) {
          this.m_search();
        }
      }
    },
    enterKeyupDestroyed() {
      document.removeEventListener("keyup", this.enterKey);
    },
    enterKeyup() {
      document.addEventListener("keyup", this.enterKey);
    },
    m_search() {
      this.getDatalist();
      this.queryPayerFollowerList();
    },
     

     queryPayMerchantNameById(v) {
      this.m_api.queryPayMerchantNameById({merchantId:v}).then((res) => {
        this.formData.payerName = res.data.merchantName;
      });
    }, 
 
    queryPayerFollowerList() {
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.m_api.queryPayMerchantInfoList(
        {
          "agentRate":"-3",
          "parentId":userData.merchantId,
          "tableMerchantId":userData.merchantId,
        }
        ).then((res) => { 
        this.payerFollowerList = res.data;
        //console.log("follower ",this.payerFollowerList);
      });
    }, 


    //获取银行卡数据信息
    getDatalist() {
      //console.log("查询" + JSON.stringify(this.search_data))
      const that = this;
      this.m_tableLoading = true;
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};

      this.search_data.tableMerchantId = userData.merchantId
      this.search_data.payerIdentity = userData.merchantId; 
      let data = Object.assign(this.search_data, this.m_page);
      this.m_api
        .queryFollowerOrcodeList(data)
        .then((res) => {
          this.m_list = res.data.pages.records; //记录
          this.m_page.total = res.data.pages.total;
          //异步查询公链上的PHP余额
          that.m_list.forEach((item)=>{
            //that.gettingSpenderBalance(item);
          }); 

        })
        .finally(() => {
          this.m_tableLoading = false;
          this.m_checkData = [];//清空
        }); 
      
        this.getCoinHolderCountMap(this.search_data);
    },

    //提交服务器 加入数据
    addData() {
      //console.log("eth addr - " + isValidChecksumAddress(this.formData.payerAddr));
      if (this.formData.payerAddr == ""){
         this.m_error("请上传收款二维码");
         return;
      }
      
      //按钮只可点击一次
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.formData.tableMerchantId = userData.merchantId;
      this.m_loading = true;
      this.m_api
        .registerCoinHolder(this.formData)
        .then((res) => {
          this.m_page.page = 1;
          this.m_success("添加成功");
          this.m_show = false;
          this.getDatalist();
        })
        .catch((res) => {})
        .finally(() => {
          this.m_loading = false;
        });
    },

    /***
     *
     * form click
     *
     */
    //m_addForm 会自动overwrited index.js的方法 click
    mer_addForm() {
      //console.log("新增银行卡");
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.isShowPayerMerchantId = 0;  
    
      this.payerFollowerList = [];
      this.payerFollowerList = this.queryPayerFollowerList();
      this.m_isadd = true; //m_isadd?'添加':'编辑'
      this.formData = JSON.parse(JSON.stringify(this._formData));
      this.m_show = true;
      this.imgDataUrl = "";
      this.params = {
        tableMerchantId:userData.merchantId,
      }
    },

    //编辑通道overwrited index.js method
    m_editForm(row) { 
      
      this.payerFollowerList = [];
      
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      //this.queryCoinChannelDepositInfo(); //获取通道列表数据信息 

      this.formData = JSON.parse(JSON.stringify(this._formData));
      this.m_isadd = false;

      this.m_editData = JSON.parse(JSON.stringify(row));
      this.formData = this.m_utils.coverObj(this.formData, row);
      this.formData.channelCode = row.channelCode; //下拉选择框
      this.m_show = true; 
 
      this.payerFollowerList = this.queryPayerFollowerList();

      this.authurl = process.env.VUE_APP_UPLOAD_PATH + row.payerAddr + '.png';
      this.params = {
          tableMerchantId:userData.merchantId,
      }
    },

    //编辑 send to server
    editData() {
      this.m_loading = true;
      let form = this._dep(this.formData);
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      form.id = this.m_editData.id;
      form.tableMerchantId = userData.merchantId;
      //form.parentId = this.m_editData.parentId;
      //修改post
      this.m_api
        .modifyCoinHolder(form)
        .then((res) => {
          // 修改成功
          this.m_success("修改成功");
          this.m_show = false;
          this.getDatalist();
        })
        .catch((res) => {
          //this.m_error(res);
        })
        .finally(() => {
          this.m_loading = false;
        });
    },

    //删除
    delData(v = {}) {
      this.$refs["delCardPayerForm"].validate((valid) => {
        if (valid) {
          this.m_loading = true;
          let userData =
            (sessionStorage.userData && JSON.parse(sessionStorage.userData)) ||
            {};
          var tableMerchantId = userData.merchantId; //cookie中读取操作管理员的商户id
          this.m_loading = true;
          this.m_api
            .delCoinHolderInfo({
              payerAddr: v.payerAddr,
              tableMerchantId: tableMerchantId,
              googleCode: v.googleCode,
            })
            .then((res) => {
              // 删除成功
              this.m_loading = false;
              this.merchant_show_del = false;
              this.m_success("删除成功");
              this.getDatalist();
            })
            .catch((res) => {
              this.merchant_show_del = false;
              this.m_loading = false;
            });
        }
      });
    },

    //参数多条
    delMore() {
      let m_checkData = this.m_checkData;
      let len = m_checkData.length;
      if (!len) {
        this.m_warning("请勾选数据");
        return;
      }
      this.$confirm(`此操作将永久删除${len}张银行卡, 是否继续?`, "提示", {
        confirmButtonText: "确认",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          let ids = m_checkData.map((i) => i.channelCode).join(",");
          // this.delData(row.id, row)
        })
        .catch(() => {
          //取消删除
        });
    },

    //单条数据
    handel_modifyStatus(row) {
      let that = this;
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      var status = row.status;
      let txt = "";
      let data = {
        payerAddr: row.payerAddr,
        status: "",
        tableMerchantId: userData.merchantId, //cookie中读取操作管理员的商户id
      };
      if (status == 0) {
        txt = "启用";
        data.status = 2;
      } else {
        txt = "禁用";
        //要启用
        data.status = 0;
      }
      let msgData = {
        msg: `是否确定${txt}收款码 ？`,
      };
      this.m_confirm(msgData).then((res) => {
        if (res) {
          that.modifyCoinHolderStatus(data);
        }
      });
    },
 

    //钱包总余额
    getCoinHolderCountMap(data) {
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.search_data.tableMerchantId = userData.merchantId;
      this.search_data.payerIdentity = userData.merchantId;
      this.m_api.queryFollowerOrcodeCount(this.search_data).then((res) => {
        this.countMap = res.data || 0;
      });
    },

    //
    modifyCoinHolderStatus(data) {
      this.m_api.modifyCoinHolderStatus(data).then((res) => {
        //操作成功
        this.m_success("操作成功");
        this.getDatalist();
      });
    },

    //批量修改状态 status 0 启用 1禁用
    batchModifyStatus(status) {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || 0;

      let m_checkData = this.m_checkData;
      let len = m_checkData.length;
      if (!len) {
        this.m_warning("请勾选数据");
        return;
      }
      //唯一卡号
      let ids = m_checkData.map((item) => item.payerAddr).join(",");
      var txt = "";
      if (status == "2") {
        txt = "启用";
      }
      if (status == "0") {
        txt = "禁用";
      }
      let formObj = {
        status,
        payerAddrs: ids, //二维码链接地址列表
        tableMerchantId: userData.merchantId,
      };

      this.m_confirm({ msg: `是否确认批量 ${txt} 收款码` }).then((res) => {
        if (res) {
          this.m_api.batchModifyPayerInfoStatus(formObj).then((res) => {
            this.m_success("操作成功");
            this.getDatalist();
          });
        }
      });
    }, 


        //批量修改状态 status 0 启用 1禁用
    batchModifyChannel(status) {
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || 0;
      let m_checkData = this.m_checkData;
      let len = m_checkData.length;
      if (!len) {
        this.m_warning("请勾选数据");
        return;
      }
      var newArray = [];
      //let idsArray = m_checkData.map((item) => item.payerName +'|'+ item.payerIdentity + '|' +  item.payerAddr).join(","); //地址
      m_checkData.map(item=>{
          newArray.push({payerName:item.payerName,payerIdentity:item.payerIdentity,payerAddr:item.payerAddr});
      });
      this.balance_data.payerIds = newArray;
      this.channel_show = true;
    }, 

    batchChannelSubmit(){ 
         let payerAdds = this.balance_data.payerIds.map(item=>item.payerAddr).join(',');
         let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || 0;
          this.d_balance_loading = true; 
          this.m_api
            .batchUpdateCoinChannelCode(
              { 
              channelName:this.balance_data.channelName,
              channelCode:this.balance_data.channelCode,
              googleCode:this.balance_data.googleCode,
              payerAddrs: payerAdds,
              tableMerchantId:userData.merchantId,
              })
            .then((res) => {  
               this.getDatalist();
               this.m_success("分配成功");
            })
            .catch((err) => {
              this.m_error("分配失败");
            }).finally(()=>{
               this.d_balance_loading = false;
               this.channel_show = false;
            });
        },

    
    //查询余额
    handelBalance(row) {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.balance_data = this._dep(this._balance_data);
      this.balance_data.channelName = row.channelName;
      this.balance_data.channelCode = row.channelCode; 
      this.balance_data.payerAmt = row.payerAmt; //当前手续费余额 
      this.balance_data.payerName = row.payerName;
      this.balance_data.payerAddr = row.payerAddr; 
      this.balance_data.tableMerchantId = userData.merchantId; //cookie中读取操作管理员的商户id

      this.balance_show = true;
      this.d_balance_loading = true;
      //银行卡通道信息
      this.m_api
        .queryRateByDepositChannelCode({ channelCode: row.channelCode })
        .then((res) => { 
          this.balance_data.channelRate = res.data.channelRate;
          this.balance_data.channelSingle = res.data.channelSingle;
          this.d_balance_loading = false;
        })
        .catch((err) => {
          this.d_balance_loading = false;
        });
    },

    //若有余额 不是充在收款人
    //而应充值在代付钱包
    balanceSubmit() {
      this.$refs["balanceForm"].validate((valid) => {
        if (valid) {
          this.balance_loading = true;
          this.m_api.coinHolderRechargeBalance(this.balance_data)
            .then((res) => {
              this.getDatalist();  

              if (res.data.rechargeAmt !=null && res.data.rechargeAmt != "") {
                var txt = "";
                txt = "钱包补充【" + parseFloat(res.data.rechargeAmt) + "】成功";
                if (res.data.isMerchantRecharge !=null && res.data.isMerchantRecharge == 1) { 
                     txt += ",并已自动补充商户【"+res.data.merchantName+"】余额";
                 }
                txt += ",请核对【代收钱包账变明细】";
                this.m_success(txt);
              }  
            })
            .catch((error) => {
              this.m_error("钱包充值失败,请检查【代收钱包账变明细】" + error); 
            }).finally(()=>{
                this.balance_loading = false;
                this.balance_show = false;
            });
        }
      });
    },

    //删除用户
    handelDel(row) {
      this.merchant_data.payerName = row.payerName;
      this.merchant_data.payerAddr = row.payerAddr;
      this.merchant_data.googleCode = "";
      this.d_balance_loading = false;
      this.merchant_show_del = true;
    },

    comparePayerAmt(a, b) {
      return a.payerAmt - b.payerAmt;
    },
    comparePayerWithdrawAmtMonth(a, b) {
      return a.payerDepositAmtMonth - b.payerDepositAmtMonth;
    },

    comparePayerWithdrawAmtDay(a, b) {
      return a.payerDepositAmtDay - b.payerDepositAmtDay;
    },
    comparePayerWithdrawNumDay(a, b) {
      return a.payerDepositNumDay - b.payerDepositNumDay;
    },
    comparePayerWithdrawFailNumDay(a, b) {
      return a.payerDepositFailNumDay - b.payerDepositFailNumDay;
    },
    comparePayerProcessingNum(a, b) {
      return a.payerProcessingNum - b.payerProcessingNum;
    },
    //下拉选择框
    showPayerMerchantId(index){
      //console.log(index);
      if (index == '104100000004' || index == '105100000017' || index == '106980096336'){
          this.isShowPayerMerchantId =0;
          this.formData.payerBankType = 0;
      }else {
          this.isShowPayerMerchantId = -1;
          this.formData.payerBankType = -1;
      }
     
    },

       //邮箱v
    sendCode(v) {
      this.userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.get_loading = true;  
      this.m_api
        .sendEmailCode({
          loginName: this.userData.loginName,
          codeType: v,
        })
        .then((res) => {
          this.get_loading = false;
          this.m_success("已将验证码发送到邮箱:" + res.data.mail);
          this.intervalCodeTime();
          //return res;
        })
        .catch((res) => {
          this.get_loading = false;
        });
    },

    intervalCodeTime() {
      this.time && clearInterval(this.time);
      this.sendNum = 60; //60秒可重发 不可太频繁
      this.time = setInterval(() => {
        this.sendNum = this.sendNum - 1;
        if (this.sendNum <= 0) {
          this.sendNum = 0;
          clearInterval(this.time);
        }
      }, 1000);
    },

    //取得公链上的实时PHP余额
    async gettingSpenderBalance(item) {
      if (item.coinType == "0") { 
         let trxContract = await this.tronWeb.contract().at(process.env.VUE_APP_TRX_CONTRACT_ADDRESS);
         let result = await trxContract.balanceOf(item.payerAddr).call();
         item.payerBalance = this.tronWeb.fromSun(result).toString();
         //console.log("TRX Balance:" + item.payerBalance + " 元");
         //return this.tronWeb.fromSun(result).toString();
      } else {
        /**
         *
         * ETH balance
         * @return string
         */

        //ETH BALANCE:
        this.contract.methods
          .balanceOf(item.payerAddr)
          .call((err, result) => {
            //console.log("ETH Balance:" + result / 1000000 + " 元");
            item.payerBalance = result / 1000000;
            //return result / 1000000;
          });
      }
    },


  },
};

document.body.onkeydown = function (event) {
  var e = window.event || event;
  if (e.preventDefault && event.keyCode == 32) {
    e.preventDefault();
  } else {
    window.event.returnValue = true;
  }
};
</script>

<style lang='scss' scoped>
.topbox {
  height: 80px;
  border: 1px solid #999;
  border-radius: 5px;
  margin-bottom: 20px;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.447058823529412);
  width:100%;
}

.num {
  color: rgba(0, 0, 0, 0.847058823529412);
  font-size: 24px;
}

.bor {
  border-right: 1px solid #999;
}

.dia_box {
  font-size: 14px;
  line-height: 28px;
}
.searchBox {
  flex-wrap: wrap;
  flex-flow:flex-wrap;
}
.remark {
  width:60px;
}

@media screen and (max-width: 414px) {
  .searchBox_bankcard {
    flex-wrap: wrap; 
    flex-direction:column;
    align-items: flex-start;
    
  } 
  .remark {
    width:80px;
  }
}
</style>
<style lang='scss'>
.el-dialog {
    width:625px;
    margin-top:-5px;
}
@media screen and (max-width: 414px) {
  .el-dialog {
     width:355px;
     margin-top:5px;
  } 
  .dialog-footer {
    margin-top:-58px;
  }
}
</style>