<!-- 外包列表 -->
<template>
  <div>
  
    <div class="flex flex-m tc topbox">
      <div class="flex-1">
        <div>总剩余手续费<span style="font-size:12px;">(USDT)</span></div>
        <div class="num">{{parseFloat(countMap.sumMerchantBalance).toFixed(2) || 0.00}}</div>
      </div>
    </div>

    <!-- * 
         * @搜索栏
         *
    -->
    <div class="flex flex-m mb20">
      <div class="flex flex-m mr20">
        <span class="w80">外包编号:</span>
        <el-input
          size="medium"
          class="w180"
          v-model="search_data.merchantId"
          clearable
          placeholder="请输入外包编号"
        ></el-input>
      </div>
      <div class="flex flex-m mr20">
        <span class="w80">外包名称:</span>
        <el-input
          size="medium"
          class="w180"
          v-model="search_data.merchantName"
          clearable
          placeholder="请输入外包名称"
        ></el-input>
      </div>
      <div class="flex flex-m mr20">
        <span class="w80">状态:</span>
        <el-select class="w180" size="medium" v-model="search_data.status" placeholder="请选择">
          <el-option label="请选择状态" value></el-option>
          <el-option
            v-for="item in statusList"
            :key="item.value"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
      </div>

      <div class="flex flex-m mr20">
        <el-button
          class
          size="small"
          icon="el-icon-search"
          @click.native="m_search"
          type="primary"
        >搜索</el-button>
      </div>
    </div>

    <!-- * 
         * @操作栏
         *
    -->
    <div class="mb20">
      <el-button
        v-if="handelAuto('add')"
        icon="el-icon-circle-plus"
        size="small"
        type="success"
        @click="mer_addForm()"
      >新增外包</el-button>
    </div>

    <!-- * 
         * @列表
         *
    -->
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
      <!-- prop 数据库里的字段 -->
       <el-table-column  label="序号"  type="index" width="50"  align="center">
      <template slot-scope="scope">
        <span>{{(m_page.page - 1) * m_page.rows + scope.$index +1}}</span>
      </template> 
     </el-table-column>
      <el-table-column align="center" prop="merchantId" label="外包编号"></el-table-column>
      <el-table-column align="center" prop="merchantName" label="外包名称"></el-table-column>
      <el-table-column align="center" prop="homepage" width="150" label="网址"></el-table-column>
      <el-table-column align="center" prop label="代付费率%" width="100">
        <template slot-scope="scope">
          <span>{{scope.row.merchantRateOut}}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop label="代付单笔">
        <template slot-scope="scope">
          <span>{{scope.row.merchantSingleOut}}</span>
        </template>
      </el-table-column>

      <el-table-column align="center" prop label="代收费率%" width="100">
        <template slot-scope="scope">
          <span>{{scope.row.merchantRateIn}}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop label="代收单笔">
        <template slot-scope="scope">
          <span>{{scope.row.merchantSingleIn}}</span>
        </template>
      </el-table-column>


      <el-table-column align="center" prop label="手续费余额" sortable :sort-method="compareMerchantBalance"  width="121">
        <template slot-scope="scope">
          <span
            :class="parseInt(scope.row.merchantBalance) | amtClassRemainFilter"
          >{{parseFloat(scope.row.merchantBalance).toFixed(2) || 0.00}}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop="contact" label="联系人"></el-table-column>
      <el-table-column align="center" prop="remark" label="备注"></el-table-column>

      <!--  状态-->
      <el-table-column align="center" label="状态" width="110">
        <template slot-scope="scope">
          <span :class="scope.row.status =='0'?'green':'red'"> {{scope.row.status | partnerStatus}}</span>
        </template>
      </el-table-column>

      <el-table-column align="center" label="操作" width="115" fixed="right">
        <template slot-scope="scope">
          <div>
            <el-button
              class="ml10"
              v-if="handelAuto('edit')"
              @click.native="m_editForm(scope.row)"
              type="text"
            >编辑</el-button>
          </div>
          <div>
            <el-button
              class="ml10"
              v-if="handelAuto('add')"
              @click.native="handelBalance(scope.row)"
              type="text"
            >手续费预充值</el-button>
          </div> 
 
          <div>
            <el-button
              v-if="handelAuto('del')"
              style="color:#ff0000"
              @click.native="handelDel(scope.row)"
              type="text"
            >删除</el-button>
          </div>
           
          <div>
            <el-button
              v-if="handelAuto('add')"
              @click.native="handel_modifyStatus(scope.row)"
              type="text"
            >
              <span
                :class="scope.row.status == '0' ? 'red':'green'"
              >{{scope.row.status =='1' ?'启用':'禁用'}}</span>
            </el-button>
          </div>
          
        </template>
      </el-table-column>
    </el-table>

    <!-- * 
         * @新增/编辑 弹框
         *
    -->
    <el-dialog
      :close-on-click-modal="false"
      v-if="m_show"
      :title="(m_isadd?'添加':'编辑')+'外包'"
      :visible.sync="m_show"
      width="500px"
      style="margin-top:-100px;"
    >
      <el-form :model="formData" :rules="m_rules" ref="ruleForm">
        <el-form-item class="flex-1" label="外包名称" label-width="125px" clearable prop="merchantName">
          <!-- :disabled="!m_isadd" -->
          <el-input placeholder="请输入外包名称" clearable v-model="formData.merchantName" auto-complete="off"></el-input>
        </el-form-item>
        <!-- 下发费率 -->
        <el-form-item
          class="flex-1"
          label="下发费率 %"
          label-width="125px"
          clearable
          prop="merchantRateOut"
        >
          <el-input
            placeholder="下发费率若为0.10%,填:0.10"
            v-model="formData.merchantRateOut"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>

       <el-form-item
          class="flex-1"
          label="下发单笔(USDT)"
          label-width="125px"
          clearable
          prop="merchantSingleOut"
        >
          <el-input
            placeholder="单笔若为0.15,填:0.15"
            v-model="formData.merchantSingleOut"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>


     <!-- 代收费率 -->
        <el-form-item
          class="flex-1"
          label="代收费率 %"
          label-width="125px"
          clearable
          prop="merchantRateIn"
        >
          <el-input
            placeholder="代收费率若为0.50%,填:0.50"
            v-model="formData.merchantRateIn"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>

       <el-form-item
          class="flex-1"
          label="代收单笔(USDT)"
          label-width="125px"
          clearable
          prop="merchantSingleOut"
        >
          <el-input
            placeholder="单笔若为0.05,填:0.05"
            v-model="formData.merchantSingleIn"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>



        <el-form-item class="flex-1" label="网址" label-width="125px" clearable prop="homepage">
          <el-input placeholder="网址" v-model="formData.homepage" clearable auto-complete="off"></el-input>
        </el-form-item>

        <el-form-item
          class="flex-1"
          label="联系人"
          label-width="125px"
          clearable
          prop="merchantContact"
        >
          <el-input
            placeholder="联系人"
            v-model="formData.contact"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>

        <el-form-item class="flex-1" label="手机号" label-width="125px" prop="phone">
          <el-input
            placeholder="手机号"
            type="number"
            @mousewheel.native.prevent
            v-model="formData.phone"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>

        <el-form-item class="flex-1" label="备注" label-width="125px" clearable prop="remark">
          <el-input placeholder="备注" v-model="formData.remark" clearable auto-complete="off"></el-input>
        </el-form-item>

       <el-form-item label="管理员谷歌" label-width="125px" prop="googleCode">
            <el-input
              type="number"
              placeholder="输入6位谷歌Google验证码"
              v-model="formData.googleCode"
              auto-complete="off"
            ></el-input>
       </el-form-item> 

      </el-form>
      <div slot="footer" class="dialog-footer" style="margin-top:-50px;">
        <!-- 取 消 -->
        <el-button @click="m_show = false">取 消</el-button>
        <!-- 确 定 -->
        <el-button type="primary" :loading="m_loading" @click.native="sureOption">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 手续费充值  -->
    <el-dialog v-if="balance_show" title="手续费预充值" :visible.sync="balance_show" width="650px">
      <div v-loading="d_balance_loading">
        <el-form :model="balance_data" :rules="balanceForm_rules" ref="balanceForm">
          <div class="flex flex-m">
            <el-form-item class="flex-1" label="外包名称" label-width="120px">
              <span>{{balance_data.merchantName}}</span>
            </el-form-item>
          </div>

          <div class="flex flex-m">
            <el-form-item class="flex-1" label="外包编号" label-width="120px">
              <span>{{balance_data.merchantId}}</span>
            </el-form-item>
          </div>

          <div class="flex flex-m">
            <el-form-item class="flex-1" label="手续费余额" label-width="120px">
              <span class="red">{{balance_data.merchantBalance}} USDT</span>
            </el-form-item>
          </div>

          <el-form-item label="充值金额" label-width="120px" prop="rechargeAmt">
            <div class="flex flex-m">
              <el-input
                @mousewheel.native.prevent
                type="number"
                style="width:110px;"
                placeholder="充值金额"
                v-model="balance_data.rechargeAmt"
                auto-complete="off"
              ></el-input>
              <div class="red ml15 fs18 w100">{{balance_data.rechargeAmt | convertCurrency}}</div>
            </div>
          </el-form-item>

          <el-form-item label="google验证码" label-width="120px" prop="googleCode">
            <el-input placeholder="请输入" v-model="balance_data.googleCode" auto-complete="off"></el-input>
          </el-form-item>

          <el-form-item label-width="120px" label="备注">
            <el-input style type="textarea" v-model="balance_data.remark"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 取 消 -->
        <el-button @click="balance_show = false">取 消</el-button>
        <!-- 确 定 -->
        <el-button type="primary" :loading="balance_loading" @click.native="balanceSubmit">确 定</el-button>
      </div>
    </el-dialog>  

    <!-- GOOGLE密钥 删除通道  -->
    <el-dialog
      v-if="merchant_show_del"
      title="删除"
      :visible.sync="merchant_show_del"
      width="500px"
    >
      <div v-loading="d_merchant_loding">
        <el-form :model="merchant_data">
          <el-form-item label="外包名称" label-width="110px">
            <span>{{merchant_data.merchantName}}</span>
          </el-form-item>

          <el-form-item label="外包编号" label-width="110px">
            <span>{{merchant_data.merchantId}}</span>
          </el-form-item>

          <el-form-item label="Google验证码" label-width="110px" prop="googleCode">
            <el-input
              type="number"
              placeholder="输入6位谷歌Google验证码"
              v-model="merchant_data.googleCode"
              auto-complete="off"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 取 消 -->
        <el-button @click="merchant_show_del = false">关 闭</el-button>
        <!-- 确 定 -->
        <el-button
          type="primary"
          :loading="merchant_loding"
          @click.native="delData(merchant_data)"
        >确 定</el-button>
      </div>
    </el-dialog>

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
import qrcode from "qrcode";
import moment from "moment";

export default {
  name: "partnerList",
  data() {
    return {
      search_data: {
        merchantId: "", //商户编号
        merchantName: "", //商户名称
        contact:"",
        status: "", //商户状态
        remark: "", //备注
      },

      countMap: {
          sumMerchantBalance:0,
      },

      highestAmt: 0,

      formData: {
        merchantId:"",
        merchantName: "", //商户名称
        merchantRateOut: "", //自动汇率
        merchantSingleOut: "", //人工汇率
        merchantRateIn:"",
        merchantSingleOut:"",
        contact: "",
        homepage: "", // 网址
        phone: "", // 手机号
        remark: "", //备注
        status: "0", //0正常 1-禁用
        tableMerchantId: "",
        googleCode:"",
      },

      merchant_loding: false,
      d_merchant_loding: false,
      merchant_show_google: false, //google
      merchant_show_del: false,

      merchant_data: { 
        merchantName:"",
        merchantId: "", // 通道编号
        googleCode: "",
        tableMerchantId: "",
      },

      balance_show: false,//手续费充值ACTIVITY
      freeze_show:false,//初始跑量ACTIVITY

      d_balance_loading: false,
      balance_loading: false,
      balance_info: {},
      _balance_data: {},
      balance_data: {
        merchantId: "", //管理员id
        merchantName: "", 
        rechargeAmt: "", //充值金额 
        merchantBalance:"",
        freezeBalance:"",//初始跑量
        remark: "", //备注
        googleCode: "", //google验证码
        tableMerchantId:"",
      },

      merchant_show: false,
      agent_show: false, //代理费率
      merchant_loding: false,
      d_merchant_loding: false, 
   

      m_rules: {
        merchantName: [{ required: true, message: "请输入", trigger: "blur" }],
        merchantRateOut: [{ required: true, message: "请输入", trigger: "blur" }],
        merchantSingleOut: [{ required: true, message: "请输入", trigger: "blur" }],
        merchantRateIn: [{ required: true, message: "请输入", trigger: "blur" }],
        merchantSingleIn: [{ required: true, message: "请输入", trigger: "blur" }],
        homepage: [{ required: true, message: "请输入", trigger: "blur" }], 
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }], 
      },

      //手续费充值
      balanceForm_rules: {
        rechargeAmt: [
          { required: true, message: "请输入", trigger: "blur" },
        ],
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }],
      },

      //手续费充值
      freezeForm_rules: {
        freezeBalance: [
          { required: true, message: "请输入", trigger: "blur" },
        ],
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }],
      },


    };
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      //转化为对象
      this._balance_data = this._dep(this.balance_data); 
      this.getDatalist(); //获取通道列表数据信息
    },

    //获取通道列表数据信息
    getDatalist() {
      //console.log("查询" + JSON.stringify(this.search_data))
      this.m_tableLoading = true;
      let data = Object.assign(this.search_data, this.m_page);
      this.m_api
        .queryPartnerList(data)
        .then((res) => {
          this.m_list = res.data.pages.records; //记录
          this.m_page.total = res.data.pages.total;
          //总余额
          this.countMap = res.data.countMap; 
        })
        .finally(() => {
          this.m_tableLoading = false;
          this.m_checkData = [];
        });
    },

    //提交服务器 加入数据
    addData() {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {}; 
      this.formData.tableMerchantId = userData.merchantId || 0;
      this.m_loading = true;
      this.m_api
        .registerPartner(this.formData)
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
      this.m_isadd = true; //m_isadd?'添加':'编辑'
      this.formData = JSON.parse(JSON.stringify(this._formData));
      this.m_show = true;
    },

    //编辑通道overwrited index.js method
    m_editForm(row) {
      this.formData = JSON.parse(JSON.stringify(this._formData));
      this.m_isadd = false;

      this.m_editData = JSON.parse(JSON.stringify(row));
      this.formData = this.m_utils.coverObj(this.formData, row);
      
      this.m_show = true;
    },

    //编辑 send to server
    editData() {
      this.m_loading = true;
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {}; 
      this.formData.tableMerchantId = userData.merchantId || 0;
      let form = this._dep(this.formData);
      form.id = this.m_editData.id;
       
      //修改post
      this.m_api
        .updatePartnerInfo(form)
        .then((res) => {
          // 修改成功
          this.m_success("修改成功");
          this.m_show = false;
          this.getDatalist();
        })
        .catch((res) => {})
        .finally(() => {
          this.m_loading = false;
        });
    },

    //删除
    delData(v = {}) {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      let tableMerchantId = userData.merchantId || 0;

      this.m_loading = true;
      this.m_api
        .deletePartnerInfo({
          merchantId: v.merchantId,
          tableMerchantId: tableMerchantId,
          googleCode: v.googleCode,
        })
        .then((res) => {
          // 删除成功
          this.m_loading = false;
          this.m_success("删除成功");
          this.getDatalist();
          this.merchant_show_del = false;
        })
        .catch((res) => {
          this.m_loading = false;
        });
    },

 
    handel_modifyStatus(row) {
      let status = row.status;
      let txt = "";
      let data = {
        merchantId: row.merchantId,
        status: "",
      };
      if (status == 0) {
        txt = "禁用";
        data.status = 1;
      } else {
        txt = "启用";
        //要启用
        data.status = 0;
      }
      let msgData = {
        msg: `${row.merchantName}状态变更为${txt} ？`,
      };
      this.m_confirm(msgData).then((res) => {
        if (res) {
          this.modifyPartnerStatus(data);
        }
      });
    },

    modifyPartnerStatus(data){

      this.m_api.modifyPartnerStatus(data)
        .then((res) => {
          // 删除成功
          this.m_loading = false; 
          this.getDatalist(); 
        })
        .catch((res) => {
          this.m_loading = false;
        });

    },
  
    handelBalance(row) {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.balance_data = this._dep(this._balance_data);
      this.balance_data.merchantId = row.merchantId;
      this.balance_data.merchantName = row.merchantName;
      this.balance_data.merchantBalance = row.merchantBalance;  
      this.balance_data.tableMerchantId = userData.merchantId; //cookie中读取操作管理员的商户id
      this.balance_show = true; 
    },

    handelFreeze(row) {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.balance_data = this._dep(this._balance_data);
      this.balance_data.merchantId = row.merchantId;
      this.balance_data.merchantName = row.merchantName;
      this.balance_data.freezeBalance = row.freezeBalance;  
      this.balance_data.tableMerchantId = userData.merchantId; //cookie中读取操作管理员的商户id
      this.freeze_show = true; 
    },

    //手续费充值
    balanceSubmit() {
      this.$refs["balanceForm"].validate((valid) => {
        if (valid) {
          this.balance_loading = true;
          this.m_api
            .partnerRecharge(this.balance_data)
            .then((res) => {
              this.getDatalist();
              this.balance_loading = false;
              this.m_success(
                "手续费充值【" +
                  parseFloat(this.balance_data.rechargeAmt).toFixed(2) +
                  "】成功,请核对充值金额并检查【手续费账变明细】"
              ); 
              this.balance_show = false;
            })
            .catch((res) => {
              this.m_error("手续费充值失败,请检查【手续费账变明细】");
              this.balance_loading = false;
            });
        }
      });
    },

    //初始跑量设置
    freezeSubmit() {
      this.$refs["freezeForm"].validate((valid) => {
        if (valid) {
          this.balance_loading = true;
          this.m_api
            .partnerFreeze(this.balance_data)
            .then((res) => {
              this.getDatalist();
              this.balance_loading = false;
              this.m_success(
                "设置【" +
                  parseFloat(this.balance_data.freezeBalance).toFixed(2) +
                  "】成功"
              ); 
              this.freeze_show = false;
            })
            .catch((res) => { 
              this.balance_loading = false;
            });
        }
      });
    },


    //删除通道
    handelDel(row) {
      this.merchant_data.merchantId = row.merchantId; 
      this.merchant_data.merchantName = row.merchantName;
      this.merchant_data.googleCode = "";
      this.d_merchant_loding = false;
      this.merchant_show_del = true;
    },

    compareMerchantBalance(a,b){ 
      return (a.merchantBalance - b.merchantBalance) ;
    },

  },
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
</style>