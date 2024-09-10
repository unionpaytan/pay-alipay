<!-- 三方通道列表  
 -->
<template>
  <div>
    <div class="flex flex-m tc topbox">
   
      <div class="flex-1">
        <div>总剩余手续费</div>
        <div  class="num">{{parseFloat(countMap.chlFeeChargeAmt).toFixed(2) || 0.00}}</div>
      </div>
    </div>

    <!-- * 
         * @搜索栏
         *
    -->
    <div class="flex flex-m mb20 searchBox">
      <div class="flex flex-m mr20">
        <span class="w120">三方通道编号:</span>
        <el-input
          size="medium"
          class="w180"
          v-model="search_data.cardChannelCode"
          clearable
          placeholder="请输入三方通道编号"
        ></el-input>
      </div>
      <div class="flex flex-m mr20">
        <span class="w120">三方通道名称:</span>
        <el-input
          size="medium"
          class="w180"
          v-model="search_data.chlName"
          clearable
          placeholder="请输入三方通道名称"
        ></el-input>
      </div>
      <div class="flex flex-m mr20">
        <span class="w120">三方通道状态:</span>
        <el-select class="w180" size="medium" v-model="search_data.chlStatus" placeholder="请选择">
          <el-option label="请选择状态" value></el-option>
          <el-option
            v-for="item in chlStatusList"
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
      >新增通道</el-button>
      <!-- <el-button icon="el-icon-delete" type="danger" size="small" @click="delMore">删除</el-button> -->
      <el-button
        v-if="handelAuto('status')"
        @click="batchModifyMerchantInfoStatus(1)"
        icon="el-icon-circle-check"
        size="small"
        type="primary"
      >启用</el-button>
      <el-button
        v-if="handelAuto('status')"
        @click="batchModifyMerchantInfoStatus(0)"
        icon="el-icon-circle-close"
        size="small"
        type="warning"
      >禁用</el-button>
    </div>

    <!-- * 
         * @列表
         *
    -->
    <el-table
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
      <el-table-column type="selection" width="46"></el-table-column>
      <el-table-column align="center" prop="cardChannelCode" label="通道编号"></el-table-column>
        <el-table-column align="center" prop="chlName" label="通道名称"></el-table-column>
      <el-table-column align="center" prop   label="通道费率 %">
         <template slot-scope="scope">
          <span>{{scope.row.chlRate}}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop="chlSingle" label="单笔手续费(元/笔)" width="150"></el-table-column>
    
      <el-table-column align="center" prop="chlCardNum" label="绑卡数量"></el-table-column>
       <!-- <el-table-column align="center" prop="chlChargeAmt" label="充值总额"></el-table-column>
      <el-table-column align="center" width="70" label="单卡最高额">
         <template slot-scope="scope">
          <span>{{scope.row.cardChannelCode || 0.00}}</span>
        </template>
      </el-table-column> 
      <el-table-column align="center" prop="chlWithdrawAmt" label="下发金额"></el-table-column>
      <el-table-column align="center" prop="chlWithdrawNum" label="下发笔数"></el-table-column>

      <el-table-column align="center" prop label="未下发" width="70">
        <template slot-scope="scope">
          <span>{{parseFloat(scope.row.chlChargeAmt - scope.row.chlWithdrawAmt).toFixed(2) || 0.00}}</span>
        </template>
      </el-table-column> -->
      <el-table-column align="center" prop label="剩余手续费" width="100">
        <template slot-scope="scope">
          <span
            :class="parseInt(scope.row.chlFeeChargeAmt - scope.row.chlFeeWithdrawAmt) | amtClassRemainFilter"
          >{{parseFloat(scope.row.chlFeeChargeAmt - scope.row.chlFeeWithdrawAmt).toFixed(2) || 0.00}}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop="remark" label="标识"  ></el-table-column>

      <!--  状态-->
      <el-table-column align="center" label="状态"  width="110" >
        <template slot-scope="scope">
          <span :class="scope.row.chlStatus =='1'?'green':'red'">{{scope.row.chlStatus | chlStatus}}</span>
        </template>
      </el-table-column>

      <el-table-column align="center" label="操作" fixed="right">
        <template slot-scope="scope">
          <div>
            <el-button 
              v-if="handelAuto('edit')"
              @click.native="m_editForm(scope.row)"
              type="text"
            >编辑</el-button>
</div>
<div>
            <el-button 
              v-if="handelAuto('recharge')"
              @click.native="handelBalance(scope.row)"
              type="text"
            >手续费充值</el-button>
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
              v-if="handelAuto('status')"
              @click.native="handel_modifyChannelStatus(scope.row)"
              type="text"
            >
              <span
                :class="scope.row.chlStatus =='0' ? 'green':'red'"
              >{{scope.row.chlStatus =='1' ?'禁用':'启用'}}通道</span>
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
      :title="(m_isadd?'添加':'编辑')+'通道'"
      :visible.sync="m_show" 
      style="margin-top:-100px;"
    >
      <el-form :model="formData" :rules="m_rules" ref="ruleForm">
        <el-form-item class="flex-1" label="通道名称" label-width="100px" clearable prop="chlName">
           <!-- :disabled="!m_isadd" -->
          <el-input 
            placeholder="请输入"
            clearable
            v-model="formData.chlName"
            auto-complete="off"
          ></el-input>
        </el-form-item>

        <el-form-item class="flex-1" label="下发费率 %" label-width="100px" clearable prop="chlRate">
          <el-input
            placeholder="商户下发汇率若为0.55%,填:0.55"
            v-model="formData.chlRate"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>

        <el-form-item class="flex-1" label="单笔手续费" label-width="100px" clearable prop="chlSingle">
          <el-input 
            placeholder="商户手续费若为1.00元/笔,填:1 设为0则不收取"
            v-model="formData.chlSingle"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>

        <el-form-item class="flex-1" label="通道标识" label-width="100px" clearable prop="label">
          <el-input placeholder="通道标识" v-model="formData.remark" clearable auto-complete="off"></el-input>
        </el-form-item>

        <el-form-item class="flex-1" label="联系人" label-width="100px" clearable prop="chlContact">
          <el-input placeholder="联系人" v-model="formData.chlContact" clearable auto-complete="off"></el-input>
        </el-form-item>

        <el-form-item class="flex-1" label="邮箱" label-width="100px" clearable prop="chlEmail">
          <el-input placeholder="邮箱" v-model="formData.chlEmail" clearable auto-complete="off"></el-input>
        </el-form-item>

        <el-form-item class="flex-1" label="手机号" label-width="100px" prop="chlPhone">
          <el-input
            placeholder="手机号"
            type="number"
            @mousewheel.native.prevent
            v-model="formData.chlPhone"
            clearable
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
    <el-dialog v-if="balance_show" title="手续费充值" :visible.sync="balance_show">
      <div v-loading="d_balance_loading">
        <el-form :model="balance_data" :rules="balanceForm_rules" ref="balanceForm">
          <div class="flex flex-m">
            <el-form-item class="flex-1" label="通道编号" label-width="120px">
              <span>{{balance_data.cardChannelCode}}</span>
            </el-form-item>
          </div>

          <div class="flex flex-m">
            <el-form-item class="flex-1" label="通道名称" label-width="120px">
              <span>{{balance_data.chlName}}</span>
            </el-form-item>
          </div>

          <div class="flex flex-m">
            <el-form-item class="flex-1" label="手续费余额" label-width="120px">
              <span class="red">{{balance_data.chlFeeChargeAmtRemain}}元</span>
            </el-form-item>
          </div>

          <el-form-item label="充值金额" label-width="120px" prop="chlFeeChargeAmt">
            <div class="flex flex-m">
              <el-input
                @mousewheel.native.prevent
                type="number"
                style="width:110px;"
                placeholder="充值金额"
                v-model="balance_data.chlFeeChargeAmt"
                auto-complete="off"
              ></el-input>
              <div class="red ml15 fs18 w100">{{balance_data.chlFeeChargeAmt | convertCurrency}}</div>
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
      title="删除通道"
      :visible.sync="merchant_show_del" 
    >
      <div v-loading="d_merchant_loding">
        <el-form :model="merchant_data">
          <el-form-item label="通道名称" label-width="110px">
            <span>{{merchant_data.chlName}}</span>
          </el-form-item>

          <el-form-item label="通道编号" label-width="110px">
            <span>{{merchant_data.cardChannelCode}}</span>
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
  name: "cardChannelList",
  data() {
    return {
      search_data: {
        cardChannelCode: "", //通道编号
        chlStatus: "", //商户状态
        chlName: "", //通道名称
        chlEmail: "", // 邮箱
        chlPhone: "", // 手机号
        chlContact: "" // 联系人
      },

      countMap: {
        chlFeeChargeAmt: 0.0,
        chlChargeAmt: 0.0,
        chlFeeWithdrawAmt: 0.0,
        chlWithdrawAmt: 0.0
      },
      
      highestAmt:0,

      formData: {
        chlRate: "", //下发费率
        chlSingle: "", //下发单笔
        chlName: "", //通道名称
        chlEmail: "", // 邮箱
        chlPhone: "", // 手机号
        chlContact: "", // 联系人
        remark: ""
      },

      merchant_loding: false,
      d_merchant_loding: false,
      merchant_show_google: false, //google
      merchant_show_del: false,

      merchant_data:{
        chlName: "", //通道名称
        cardChannelCode: "", // 通道编号
        googleCode:"",
        tableMerchantId:"",
      },

      balance_show: false,
      d_balance_loading: false,
      balance_loading: false,
      balance_info: {},
      _balance_data: {},
      balance_data: {
        merchantId: "", //管理员id
        chlContact: "",
        cardChannelCode: "", //登陆后接口返回的商户ID
        chlName: "", //商户列表中 商户名称
        chlFeeChargeAmt: "", //充值金额
        chlFeeChargeAmtRemain: "", //手续费余额
        remark: "", //备注
        googleCode: "" //google验证码
      },

      merchant_show: false,
      agent_show: false, //代理费率
      merchant_loding: false,
      d_merchant_loding: false,
      _channel_data: {},

      channel_data: {
        chlContact: "", //联系人
        chlName: "", //通道名称
        cardChannelCode: "", //商户信息列表中的  商户号 (非登陆后接口返回的)
        cardChannelCode: "" //通道编号
      },

      m_rules: {
        chlRate: [{ required: true, message: "请输入", trigger: "blur" }],
        chlSingle: [{ required: true, message: "请输入", trigger: "blur" }],
        chlName: [{ required: true, message: "请输入", trigger: "blur" }],
        chlEmail: [{ required: true, message: "请输入", trigger: "blur" }],
        chlPhone: [{ required: true, message: "请输入", trigger: "blur" }],
        chlContact: [{ required: true, message: "请输入", trigger: "blur" }]
      },

      balanceForm_rules: {
        chlFeeChargeAmt: [
          { required: true, message: "请输入", trigger: "blur" }
        ],
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }]
      }
    };
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      this._balance_data = this._dep(this.balance_data);
      this._channel_data = this._dep(this.channel_data);
      this.getDatalist(); //获取通道列表数据信息
      
    },

    
    //获取通道列表数据信息
    getDatalist() {
      //console.log("查询" + JSON.stringify(this.search_data))
      this.m_tableLoading = true;
      let data = Object.assign(this.search_data, this.m_page);
      this.m_api
        .queryCardChannelInfoList(data)
        .then(res => {
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
      this.m_loading = true;
      this.m_api.registerCardChannel(this.formData)
        .then(res => {
          this.m_page.page = 1;
          this.m_success("添加通道成功");
          this.m_show = false;
          this.getDatalist();
        })
        .catch(res => {})
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
      console.log("新增通道 chlList");
      let chlName = this.formData.chlName != null ? this.formData.chlName : "";

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
      this.formData.cardChannelCode = row.cardChannelCode;
      this.m_show = true;
    },

    //编辑 send to server
    editData() {
      this.m_loading = true;

      let form = this._dep(this.formData);
      form.id = this.m_editData.id;
      //form.parentId = this.m_editData.parentId;
      //修改post
      this.m_api
        .modifyCardChannel(form)
        .then(res => {
          // 修改成功
          this.m_success("修改成功");
          this.m_show = false;
          this.getDatalist();
        })
        .catch(res => {})
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
        .deleteCardChannelInfo({ 
          cardChannelCode: v.cardChannelCode,
          tableMerchantId: tableMerchantId,
          googleCode:v.googleCode
         })
        .then(res => {
          // 删除成功
          this.m_loading = false;
          this.m_success("删除成功");
          this.getDatalist();
          this.merchant_show_del = false;
        })
        .catch(res => {
          this.m_loading = false;
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
      this.$confirm(`此操作将永久删除${len}个商户, 是否继续?`, "提示", {
        confirmButtonText: "确认",
        cancelButtonText: "取消",
        type: "warning"
      })
        .then(() => {
          let ids = m_checkData.map(i => i.cardChannelCode).join(",");
          console.log(ids);
          // this.delData(row.id, row)
        })
        .catch(() => {
          //取消删除
        });
    },

    handel_modifyChannelStatus(row) {
      let status = row.chlStatus;
      let txt = "";
      let data = {
        cardChannelCode: row.cardChannelCode,
        status: ""
      };
      if (status == 0) {
        txt = "启用";
        data.status = 1;
      } else {
        txt = "禁用";
        //要启用
        data.status = 0;
      }
      let msgData = {
        msg: `是否确定 ${txt} 通道 ${row.chlName} ？`
      };
      this.m_confirm(msgData).then(res => {
        if (res) {
          this.modifyCardChannelStatus(data);
        }
      });
    },

   //返回通道内已分配卡的最高余额
   gettingHighestAmtBycardChannelCode(v){
      //异步操作
      this.m_api.gettingHighestAmtBycardChannelCode({cardChannelCode:v}).then(res => {
        
        console.log(res.data) 
        return res.data;
       });  
   },
 
    modifyCardChannelStatus(data) {
      this.m_api.modifyCardChannelStatus(data).then(res => {
        //操作成功
        this.m_success("操作成功");
        this.getDatalist();
      });
    },

    //批量修改状态 status 0 启用 1禁用
    batchModifyMerchantInfoStatus(status) {
      let m_checkData = this.m_checkData;
      let len = m_checkData.length;
      if (!len) {
        this.m_warning("请勾选数据");
        return;
      }
      let ids = m_checkData.map(item => item.cardChannelCode).join(",");
      let txt = status == "1" ? "启用" : "禁用";
      let form = {
        status,
        cardChannelCodes: ids //cardChannelCode 列表
      };

      this.m_confirm({ msg: `是否确 批量 ${txt} 通道` }).then(res => {
        if (res) {
          this.m_api.batchModifyCardChannelInfoStatus(form).then(res => {
            this.m_success("操作成功");
            this.getDatalist();
          });
        }
      });
    },

    handelBalance(row) {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.balance_data = this._dep(this._balance_data);
      this.balance_data.chlName = row.chlName;
      this.balance_data.chlFeeChargeAmtRemain =
        row.chlFeeChargeAmt - row.chlFeeWithdrawAmt; //当前手续费余额
      this.balance_data.cardChannelCode = row.cardChannelCode;
      this.balance_data.chlContact = row.chlContact;

      this.balance_data.merchantId = userData.merchantId; //cookie中读取操作管理员的商户id

      this.balance_show = true;
      this.d_balance_loading = true;
      this.m_api
        .queryMerchantChannel({ cardChannelCode: row.cardChannelCode })
        .then(res => {
          console.log(res);
          this.balance_info = this._dep(res.data);
          // this.balance_info.chlName = res.chlName
          this.d_balance_loading = false;
        })
        .catch(err => {
          this.d_balance_loading = false;
        });
    },

    balanceSubmit() {
      this.$refs["balanceForm"].validate(valid => {
        if (valid) {
          this.balance_loading = true;
          this.m_api
            .cardChannelRecharge(this.balance_data)
            .then(res => {
              this.getDatalist();
              this.balance_loading = false;
              this.m_success(
                "手续费充值【" +
                  parseFloat(this.balance_data.chlFeeChargeAmt).toFixed(2) +
                  "】成功,请核对充值金额并检查【手续费充值流水】"
              );
              // this.m_success('请二次核对充值金额【'+this.balance_data.chlFeeChargeAmt+'】');
              this.balance_show = false;
            })
            .catch(res => {
              this.m_error("手续费充值失败,请检查【手续费充值流水】");
              this.balance_loading = false;
            });
        }
      });
    },


    //删除通道
    handelDel(row) {
      this.merchant_data.chlName = row.chlName;
      this.merchant_data.cardChannelCode = row.cardChannelCode;
      this.merchant_data.googleCode = "";
      this.d_merchant_loding = false;
      this.merchant_show_del = true;
    },


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
</style>
<style lang='scss'>
.el-dialog {
    width:525px;
    margin-top:-5px;
}
@media screen and (max-width: 414px) {
  .el-dialog {
     width:355px;
     margin-top:-15px;
  } 
  .dialog-footer {
    margin-top:-38px;
  }
}
</style>