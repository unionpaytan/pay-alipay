<!-- 码商下挂操作员  cardMerchantOperator-->
<template>
  <div>
    <!-- * 
         * @搜索栏
         *
    --> 
      <div class="flex flex-m mb20 searchBox">
        <div class="flex flex-m mr10">
          <span class="w80">所属码商:</span>
          <el-select
            clearable
            class="w180"
            size="medium"
            v-model="search_data.parentId"
            placeholder="请选择" 
          >
            <el-option label="请选择码商" value></el-option>
            <el-option
              v-for="item in payMerchantInfoList"
              :key="item.merchantId"
              :label="item.merchantName + ' (' + item.merchantId + ')'"
              :value="item.merchantId"
            ></el-option>
          </el-select>
        </div> 

      <div class="flex flex-m mr20">
        <span class="w195 mr10">操作员名称:</span>
        <el-input
          size="medium" 
          v-model="search_data.merchantName"
          clearable
          placeholder="操作员名称"
          class="w150"
        ></el-input>
      </div>

       <div class="flex flex-m mr20">
        <span class="w195 mr10">操作员编号:</span>
        <el-input
          size="medium" 
          v-model="search_data.merchantId"
          clearable
          placeholder="操作员编号"
          class="w180"
        ></el-input>
      </div>


         <div class="flex flex-m mr50">
        <el-button
          class
          size="small"
          icon="el-icon-search"
          @click.native="m_search"
          type="primary"
          >搜索</el-button>
      </div> 


      </div>
    <div class="mb20">
      <el-button
        v-if="handelAuto('add')"
        icon="el-icon-circle-plus"
        size="small"
        type="primary"
        @click="adm_addForm()"
        >新增码商下挂操作员</el-button
      >
    </div>

    <el-table
      v-loading="m_tableLoading"
      @selection-change="m_handleSelectionChange"
      @current-change="m_handleCurrentChange"
      class="mtable"
      :stripe="true"
      tooltip-effect="dark"
      :highlight-current-row="true"
      :data="m_list"
    >
      <!-- <el-table-column type="selection" width="55"></el-table-column> -->
     <el-table-column label="序号" type="index" width="60" align="center">
        <template slot-scope="scope">
          <span>{{ (m_page.page - 1) * m_page.rows + scope.$index + 1 }}</span>
        </template>
      </el-table-column>
    <el-table-column align="center" label="所属码商">
     <template slot-scope="scope">
        <span>{{scope.row.parentName}}</span><br>
        <span>{{scope.row.parentId}}</span>
     </template>
    </el-table-column> 

      <el-table-column
        align="center"
        prop="merchantName"
        label="操作员名称"
      ></el-table-column>
       <el-table-column
        align="center"
        prop="merchantId"
        label="操作员编号"
      ></el-table-column> 

      <el-table-column align="center" prop="loginName" label="操作员登录帐户"></el-table-column> 
      <el-table-column align="center" prop="countOperator" label="操作员可用/总收款码数量">
        <template slot-scope="scope">
          <span :class="'green'">{{scope.row.countOperatorAvailable}}</span>/<span>{{scope.row.countOperator}}</span>
        </template>
      </el-table-column> 
 

      <el-table-column align="center" label="帐户状态">
        <template slot-scope="scope">
          <span :class="scope.row.status == '0' ? 'green' : 'red'">{{
            scope.row.status | status
          }}</span>
        </template>
      </el-table-column>

      <el-table-column align="center" label="操作" fixed="right" width="128">
        <template slot-scope="scope">
          <div>
          <el-button
            v-if="handelAuto('edit')"
            @click.native="adm_editForm(scope.row)"
            type="text"
            >编辑</el-button
          >
          </div> 

           <div>
          <el-button
          v-if="handelAuto('edit')" 
           @click.native="handelMerchantGoogle(scope.row)"
            type="text"
            >重置谷歌</el-button>
          </div>

           <div>
            <el-button 
              v-if="handelAuto('edit')"
              @click.native="handelWeb(scope.row)"
              type="text"
            >web IP白名单</el-button>
          </div>

          <div>
          <el-button
            v-if="handelAuto('del')"
            style="color: #ff0000"
            @click.native="handelDel(scope.row)"
            type="text"
            >删除</el-button
          >
          </div>
          <div>
          <el-button
            v-if="handelAuto('edit')"
            @click.native="handel_updateUserStatus(scope.row)"
            type="text"
          >
            <span :class="scope.row.status == '1' ? 'green' : 'red'">{{
              scope.row.status | f_status
            }}</span>
          </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑/新增操作员框 -->
    <el-dialog
      :close-on-click-modal="false"
      v-if="m_show"
      :title="m_isadd ? '添加操作员' : '编辑操作员'"
      :visible.sync="m_show" 
      style="margin-top: -100px"
    >
      <el-form :model="formData" :rules="m_rules" ref="ruleForm" style="margin-top: -20px">
        <div class>

         <el-form-item
          label="所属码商"
          label-width="100px"
          prop="parentId"
        >
          <el-select
            clearable 
            v-model="formData.parentId"
            placeholder="请选择操作员所属码商"
          >
            <el-option
              v-for="item in payMerchantInfoList"
              :key="item.merchantId"
              :label="item.merchantName + ' ('+ item.merchantId + ')'"
              :value="item.merchantId"
            ></el-option>
          </el-select>
        </el-form-item> 

          <el-form-item
            class="flex-1"
            label="操作员名称"
            label-width="95px"
            prop="merchantName"
          >
            <el-input
              placeholder="操作员名称"
              v-model="formData.merchantName"
              auto-complete="off"
            ></el-input>
          </el-form-item>
 
          <el-form-item
            v-if="m_isadd"
            class="flex-1"
            label="登陆账号"
            label-width="95px"
            prop="loginName"
          >
            <el-input
              placeholder="请输入"
              v-model="formData.loginName"
              auto-complete="off"
            ></el-input>
          </el-form-item>

          <el-form-item
            class="flex-1"
            label="邮箱"
            label-width="95px"
            prop="maillbox"
          >
            <el-input
              placeholder="请输入"
              v-model="formData.maillbox"
              auto-complete="off"
            ></el-input>
          </el-form-item>

          <el-form-item
           
            class="flex-1"
            label="密码"
            label-width="95px"
            prop="passwrod" 
          >
            <el-input
              placeholder="请输入"
              v-model="formData.passwrod"
              auto-complete="off"
              type="password"
            ></el-input>
          </el-form-item>

        <el-form-item 
            v-if="m_isadd"
            class="flex-1"
            label="谷歌密钥"
            label-width="95px"
            prop="googleKey"
          >
            <el-input
              readonly
              placeholder="google谷歌密钥"
              type="text"
              v-model="formData.googleKey"
              clearable
              auto-complete="off"
            ></el-input>
            <img class="authurl" :src="authurl" alt />
          </el-form-item> 
          <el-form-item label="谷歌验证码" label-width="95px" prop="googleCode" style="margin-top: -20px">
            <el-input
              type="number"
              clearable
              placeholder="谷歌Google验证码"
              v-model="formData.googleCode"
              auto-complete="off"
            ></el-input>
          </el-form-item>

          <!-- 邮箱二次验证:START -->
         <!-- <el-form-item label="邮箱验证码" label-width="95px" prop="emailCode">
            <el-input
              type="text"
              clearable
              placeholder="邮箱验证码"
              v-model="formData.emailCode"
              auto-complete="off"
              class="w150"
            ></el-input>
            <el-button
              type="primary"
              class="ml20"
              @click="sendCode('cardAdd')"
              :disabled="sendNum != 0"
              :loading="get_loading"
            >
              <span v-if="sendNum != 0">{{ sendNum }}秒后重新</span>获取<span
                v-if="sendNum == 0"
                >验证码</span
              >
            </el-button>
          </el-form-item>   -->
          <!-- 邮箱二次验证:END -->
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer" style="margin-top: -50px">
        <!-- 取 消 -->
        <el-button @click="m_show = false">取 消</el-button>
        <!-- 确 定 -->
        <el-button
          type="primary"
          :loading="m_loading"
          @click.native="sureOption"
          >确 定</el-button
        >
      </div>
    </el-dialog>

  <!-- 重置码商谷歌GOOGLE  -->
    <el-dialog
      v-if="merchant_show_google"
      title="重置谷歌GOOGLE"
      :visible.sync="merchant_show_google" 
      style="margin-top: -100px"
    >
      <div v-loading="d_merchant_loding">
        <el-form :model="merchant_data" :rules="merchantGoogleForm_rules" ref="merchantGoogleForm">
          <el-form-item label="操作员编号" label-width="110px">
            <span>{{merchant_data.merchantId}}</span>
          </el-form-item>

          <el-form-item label="操作员名称" label-width="110px">
            <span>{{merchant_data.merchantName}}</span>
          </el-form-item>

          <el-form-item class="flex-1" label="新谷歌密钥" label-width="110px" prop="googleKey">
            <el-input
              readonly
              placeholder="码商新Google谷歌密钥"
              type="text"
              v-model="merchant_data.googleKey"
              clearable
              auto-complete="off"
            ></el-input>
            <img class="authurl" :src="authurl" alt />
          </el-form-item>

          <el-form-item label="Google验证码" label-width="110px" prop="googleCode">
            <el-input
              type="number"
              placeholder="谷歌Google验证码"
              v-model="merchant_data.googleCode"
              auto-complete="off"
            ></el-input>
          </el-form-item>

          <!-- 邮箱二次验证:START -->
          <!-- <el-form-item label="邮箱验证码" label-width="110px" prop="emailCode">
            <el-input
              type="text"
              clearable
              placeholder="邮箱验证码"
              v-model="merchant_data.emailCode"
              auto-complete="off"
              class="w150"
            ></el-input>
            <el-button
              type="primary"
              class="ml20"
              @click="sendCode('merchantResetGoogle')"
              :disabled="sendNum != 0"
              :loading="get_loading"
            >
              <span v-if="sendNum != 0">{{ sendNum }}秒后重新</span>获取<span
                v-if="sendNum == 0"
                >验证码</span
              >
            </el-button>
          </el-form-item> -->
          <!-- 邮箱二次验证:END -->

        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 取 消 -->
        <el-button @click="merchant_show_google = false">关 闭</el-button>
        <!-- 确 定 -->
        <el-button
          type="primary"
          :loading="merchant_loding"
          @click.native="merchantGoogleSubmit(merchant_data)"
        >确 定</el-button>
      </div>
    </el-dialog>

    <!-- GOOGLE 删除系统用户  -->
    <el-dialog
      v-if="merchant_show_del"
      title="删除操作员"
      :visible.sync="merchant_show_del"
       style="margin-top: -100px"
    >
      <div v-loading="d_merchant_loding">
        <el-form :model="merchant_data">
          <el-form-item label="删除操作员" label-width="110px">
            <span>{{ merchant_data.merchantName }}</span>
          </el-form-item>

          <el-form-item label="码商编号" label-width="110px">
            <span>{{ merchant_data.merchantId }}</span>
          </el-form-item>

          <el-form-item
            label="Google验证码"
            label-width="110px"
            prop="googleCode"
          >
            <el-input
              type="number"
              clearable
              placeholder="输入6位谷歌Google验证码"
              v-model="merchant_data.googleCode"
              auto-complete="off"
            ></el-input>
          </el-form-item>

          <!-- 邮箱二次验证:START -->
          <!-- <el-form-item label="邮箱验证码" label-width="110px" prop="emailCode">
            <el-input
              type="text"
              clearable
              placeholder="邮箱验证码"
              v-model="merchant_data.emailCode"
              auto-complete="off"
              class="w150"
            ></el-input>
            <el-button
              type="primary"
              class="ml20"
              @click="sendCode('cardDel')"
              :disabled="sendNum != 0"
              :loading="get_loading"
            >
              <span v-if="sendNum != 0">{{ sendNum }}秒后重新</span>获取<span
                v-if="sendNum == 0"
                >验证码</span
              >
            </el-button>
          </el-form-item> -->
          <!-- 邮箱二次验证:END -->
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
          >确 定</el-button
        >
      </div>
    </el-dialog>

    <!-- GOOGLE 修改系统用户状态  -->
    <el-dialog
      v-if="merchant_show_status"
      title="修改码商状态"
      :visible.sync="merchant_show_status"
      style="margin-top: -100px"
    >
      <div v-loading="d_merchant_loding">
          <el-form-item
          label="所属码商"
          label-width="100px"
          prop="parentId"
        >
          <el-select
            clearable 
            v-model="formData.parentId"
            placeholder="请选择操作员所属码商"
          >
            <el-option
              v-for="item in payMerchantInfoList"
              :key="item.merchantId"
              :label="item.merchantName + ' ('+ item.merchantId + ')'"
              :value="item.merchantId"
            ></el-option>
          </el-select>
        </el-form-item> 

          <el-form :model="merchant_data">
          <el-form-item label="操作员名称" label-width="110px">
            <span>{{ merchant_data.merchantName }}</span>
          </el-form-item>

          <el-form-item label="操作员编号" label-width="110px">
            <span>{{ merchant_data.merchantId }}</span>
          </el-form-item>

          <el-form-item label="操作" label-width="110px">
            <span>{{ merchant_data.statusText }}</span>
          </el-form-item>

          <el-form-item
            label="Google验证码"
            label-width="110px"
            prop="googleCode"
          >
            <el-input
              type="number"
              placeholder="输入6位谷歌Google验证码"
              v-model="merchant_data.googleCode"
              auto-complete="off"
            ></el-input>
          </el-form-item>

          <!-- 邮箱二次验证:START -->
          <!-- <el-form-item label="邮箱验证码" label-width="110px" prop="emailCode">
            <el-input
              type="text"
              clearable
              placeholder="邮箱验证码"
              v-model="merchant_data.emailCode"
              auto-complete="off"
              class="w150"
            ></el-input>
            <el-button
              type="primary"
              class="ml20"
              @click="sendCode('cardStatus')"
              :disabled="sendNum != 0"
              :loading="get_loading"
            >
              <span v-if="sendNum != 0">{{ sendNum }}秒后重新</span>获取<span
                v-if="sendNum == 0"
                >验证码</span
              >
            </el-button>
          </el-form-item> -->
          <!-- 邮箱二次验证:END -->
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 取 消 -->
        <el-button @click="merchant_show_status = false">关 闭</el-button>
        <!-- 确 定 -->
        <el-button
          type="primary"
          :loading="merchant_loding"
          @click.native="updateUserStatus(merchant_data)"
          >确 定</el-button
        >
      </div>
    </el-dialog>

    <!-- WEB代付白名单  -->
    <el-dialog v-if="webShow" title="WEB上码:配置IP白名单" :visible.sync="webShow">
      <div v-loading="d_ip_loading">
        <el-button
          icon="el-icon-circle-plus"
          size="small"
          type="success"
          @click="webStrList.push({ip:''})"
        >新增</el-button>

         <el-form :model="web_data" :rules="ruleWebIpForm_rules" ref="ruleWebIpForm"> 
          <el-table class="mt10" :data="webStrList" border style="width: 100%">
          <el-table-column align="center" type="index" width="60" label="序号"></el-table-column>
          <el-table-column align="center" prop="date" label="WEB:IP白名单">
            <template slot-scope="scope">
              <el-input v-model="scope.row.ip" placeholder="请输入IP白名单"></el-input>
            </template>
          </el-table-column>

          <el-table-column align="center" prop="date" label="操作" width="70px">
            <template slot-scope="scope">
              <el-button
                style="color:#ff0000"
                @click.native="webStrList.splice(scope.$index,1)"
                type="text"
              >删除</el-button>
            </template>
          </el-table-column>
        </el-table>

          <el-form-item label="google验证码" label-width="130px" prop="googleCode" style="margin-top:5px;">
            <el-input
              type="number"
              placeholder="请输入6位google验证码"
              v-model="web_data.googleCode"
              auto-complete="off"
            ></el-input>
          </el-form-item>

 <!-- 邮箱二次验证:START -->
          <!-- <el-form-item label="邮箱验证码" label-width="130px" prop="emailCode">
            <el-input
              type="text"
              clearable
              placeholder="邮箱验证码"
              v-model="web_data.emailCode"
              auto-complete="off"
              class="w150"
            ></el-input>
            <el-button
              type="primary"
              class="ml20"
              @click="sendCode('merchantApiIp')"
              :disabled="sendNum != 0"
              :loading="get_loading"
            >
              <span v-if="sendNum != 0">{{ sendNum }}秒后重新</span>获取<span
                v-if="sendNum == 0"
                >验证码</span
              >
            </el-button>
          </el-form-item> -->
          <!-- 邮箱二次验证:END -->

         </el-form> 
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 取 消 -->
        <el-button @click="webShow = false">取 消</el-button>
        <!-- 确 定 -->
        <el-button type="primary" :loading="ip_loading" @click.native="surWebSubmit">确 定</el-button>
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
import * as api from "@/api/public";

export default {
  name: "cardMerchantOperator",
  data() {
    return {
      search_data: {},
      payMerchantInfoList:[],//从商户列表中查出 码商(agentRate == '-1')列表
      formData: {
        id: "",
        merchantName: "", //用户名称
        loginName: "", // 登陆账号
        maillbox: "", // 邮箱
        passwrod: "", // 密码
        roleId: "", // 角色ID
        googleKey: "", //google key
        googleUrl: "",
        authurl: "",
        tableMerchantId: "",
        googleCode: "",
        emailCode: "", //邮箱code
      },
      authurl: "",
      //邮箱
      sendNum: 0,
      time: null,
      get_loading: false,

      merchant_data: {
        merchantName: "",
        merchantId: "",
        googleCode: "",
        emailCode: "",
      },

     ipShow: false,
      webShow: false,
      ip_loading: false,
      d_ip_loading: false,


      //web 白名单
       web_data: {
        merchantId: "", //商户ID
        tableMerchantId: "", //管理员ID
        ipStr: "", //ip列表
        googleCode:"",
        emailCode:"",
      },
      ipStrList: [], //ip列表
      webStrList: [], //web ip列表

      merchant_loding: false,
      d_merchant_loding: false,
      merchant_show_google: false, //google
      merchant_show_del: false,
      merchant_show_status: false,
      m_rules: {
        merchantName: [{ required: true, message: "请输入", trigger: "blur" }],
        loginName: [{ required: true, message: "请输入", trigger: "blur" }],
        maillbox: [{ required: true, message: "请输入", trigger: "blur" }],
        passwrod: [{ required: true, message: "请输入", trigger: "blur" }],
        roleId: [{ required: true, message: "请输入", trigger: "change" }],
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }],
       // emailCode: [{ required: true, message: "请输入", trigger: "blur" }],
      },

      merchantGoogleForm_rules: { 
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }],
        //emailCode: [{ required: true, message: "请输入", trigger: "blur" }]
      },


      roleList: [], //角色列表
    };
  },
  created() {
    this.userData =
      (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
    this.init();
  },
  methods: {
    init() { 
      this.m_search();
    },   
    m_search(){
        this.getDatalist();
         this.queryPayMerchantInfoList();
    },
     //获取码商列表
    queryPayMerchantInfoList() {
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.m_api.queryPayMerchantInfoList(
        {
          "agentRate":"-1",
          "tableMerchantId":userData.merchantId,
        }
        ).then((res) => {
        this.payMerchantInfoList = res.data;
      });
    }, 

    //码商:获取数据信息
    getDatalist() {
      this.m_tableLoading = true;
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.search_data.tableMerchantId = userData.merchantId || 0;
      this.search_data.agentRate = "-2";//操作员
      let data = Object.assign(this.search_data, this.m_page);
      api
        .queryMerchantFollowers(data)
        .then((res) => {
          this.m_list = res.data.records;
          this.m_page.total = res.data.total;
        })
        .finally(() => {
          this.m_tableLoading = false;
        });
    },

    addData() {
      this.m_loading = true;

      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.formData.tableMerchantId = userData.merchantId || 0;
      this.formData.agentRate = "-2";//操作员
      api
        .addMerchantFollower(this.formData)
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

    //编辑
    editData() {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.formData.tableMerchantId = userData.merchantId || 0;

      this.m_loading = true;
      let form = this._dep(this.formData);
      form.merchantName = this.formData.merchantName;
      form.merchantId = this.formData.merchantId;
      api
        .updateMerchantFollower(form)
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
    delData(v) {
      this.m_loading = true;
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      let tableMerchantId = userData.merchantId || 0;

      api.delMerchantFollower({
          merchantId: v.merchantId,
          tableMerchantId: tableMerchantId,
          googleCode: v.googleCode
        })
        .then((res) => {
          // 删除成功
          this.m_loading = false;
          this.m_success("删除成功");
          this.getDatalist();
          this.merchant_show_del = false;
        })
        .catch((res) => {
          //console.log(res);
          this.m_loading = false;
          this.merchant_show_del = false;
        });
    },

    handelWeb(row) {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.webShow = true;
      this.web_data.merchantId = row.merchantId;
      this.web_data.googleCode = "";
      this.d_ip_loading = true;
      this.m_api
        .queryWebIpList({
          merchantId: row.merchantId,
          tableMerchantId: userData.merchantId
        })
        .then(res => {
          this.webStrList = res.data.map(i => {
            return { ip: i };
          });
          this.d_ip_loading = false;
        })
        .catch(err => {
          this.d_ip_loading = false;
        });
    },

     surWebSubmit() {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};

      let webStrList = this.webStrList.filter(i => i.ip).map(i => i.ip);
      if (!webStrList.length) {
        this.m_error("请填写WEB IP");
        return;
      }

       this.$refs["ruleWebIpForm"].validate(valid => {
        if (valid) {

      this.web_data.ipStr = webStrList.join(",");
      this.web_data.tableMerchantId = userData.merchantId;
      this.ip_loading = true;
      this.m_api
        .bindWebIp(this.web_data)
        .then(res => {
          this.m_success("设置成功");
          this.ip_loading = false;
          this.webShow = false;
        })
        .catch(err => {
          this.ip_loading = false;
        });

        }
        }); 
   
    },



    //商户Google
    handelMerchantGoogle(row) {
      //this.merchant_data = this._dep(this._merchant_data);
      this.merchant_data.merchantName = row.merchantName; 
      this.merchant_data.merchantId = row.merchantId;  
      this.getNewGoogleAuthUrl(row.merchantName, 1); 
      this.d_merchant_loding = false;
      this.merchant_show_google = true;
    },

  //邮箱v
    sendCode(v) {
      this.get_loading = true; 
      var codeType = v;
      this.m_api
        .sendEmailCode({
          loginName: this.userData.loginName,
          codeType: codeType,
        })
        .then((res) => {
          this.get_loading = false;
          this.m_success("已将验证码发送到邮箱:" + res.data.mail);
          this.intervalCodeTime();
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


    handel_updateUserStatus(row) {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      // let tableMerchantId = userData.merchantId || 0;
      // this.merchant_data.tableMerchantId = tableMerchantId;
      // this.merchant_data.status = row.status == 0 ? 1 : 0;
      // this.merchant_data.merchantName = row.merchantName;
      // this.merchant_data.merchantId = row.merchantId;
      // this.merchant_data.googleCode = "";
      // this.d_merchant_loding = false;
      // this.merchant_show_status = true;
      // this.merchant_data.statusText = row.status == 0 ? "禁用" : "启用";
      var status = row.status;
      let txt = "";
      let data = {
        merchantId: row.merchantId,
        status: "",
        tableMerchantId:userData.merchantId,
      };
      if (status == 0) {
        //用禁用
        txt = "禁用";
        data.status = 1;
      } else {
        txt = "禁用";
        //要启用
        data.status = 0;
      }
      let msgData = {
        msg: `是否确定 ${txt} 用户 ${row.merchantName} ？`
      };
      this.m_confirm(msgData).then(res => {
        if (res) {
          this.updateUserStatus(data);
        }
      });
    },

 //google更换
    merchantGoogleSubmit(v) {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      let tableMerchantId = userData.merchantId || 0;

      this.$refs["merchantGoogleForm"].validate(valid => {
        if (valid) {
          this.d_merchant_loding = true;
          this.m_api.modifyMerchantGoogle({
              merchantId: v.merchantId,
              tableMerchantId: tableMerchantId,
              googleCode: v.googleCode,
              googleKey: v.googleKey,
              emailCode: v.emailCode,
            })
            .then(res => {
              this.d_merchant_loding = false;
              this.m_success("操作成功");
              this.merchant_show_google = false;
            })
            .catch(res => {
              this.d_merchant_loding = false;
            });
        }
      });
    },

    //
    updateUserStatus(data) {
      api.updateMerchantFollowerStatus(data).then((res) => {
        //操作成功
        this.merchant_show_status = false;
        this.m_success("操作成功");
        this.getDatalist();
      });
    }, 

    adm_addForm() { 
      let loginName =
        this.formData.loginName != null ? this.formData.loginName : "";
      this.getNewGoogleAuthUrl(loginName);
      this.m_isadd = true; //m_isadd?'添加':'编辑'
      this.formData = JSON.parse(JSON.stringify(this._formData));
      this.m_show = true;
    },

    //编辑用户overwrited index.js method
    adm_editForm(row) {
       //console.log("编辑码商" + row.merchantId);
      this.formData = JSON.parse(JSON.stringify(this._formData));
      this.m_isadd = false;
      // 编辑关闭GOOGLE
      this.m_editData = JSON.parse(JSON.stringify(row));
      this.formData = this.m_utils.coverObj(this.formData, row);
      this.formData.merchantId = row.merchantId;
      this.formData.tableMerchantId = row.parentId;
      this.formData.parentId = row.parentId;
      this.m_show = true;
    },

    // 新增google key
    getNewGoogleAuthUrl(v,type) {
      this.m_api.getNewGoogleAuthUrl({ v })
        .then((res) => {
          let authKey = res.data.googleKey;
          let authurl = res.data.googleAuthUrl;
          this.formData.googleKey = authKey;
          this.formData.googleUrl = authurl;
            if (type == 1) {
            this.merchant_data.googleKey = authKey;
            this.merchant_data.googleUrl = authurl;
          }

          var opts = {
            errorCorrectionLevel: "H",
            type: "image/jpeg",
            rendererOpts: {
              quality: 1,
            },
          };
          let that = this;
          qrcode.toDataURL(authurl, opts, function (err, url) {
            if (err) throw err;
            that.authurl = url;
          });
        })
        .catch((err) => {
          console.log(err);
        });
    },

    //删除用户
    handelDel(row) {
      this.merchant_data.merchantName = row.merchantName;
      this.merchant_data.merchantId = row.merchantId;
      this.merchant_data.googleCode = "";
      this.merchant_data.emailCode = "";
      this.d_merchant_loding = false;
      this.merchant_show_del = true;
    },
  },
  filters: {
    f_status(v) {
      if (v == 0) {
        return "禁用";
      }
      if (v == 1) {
        return "启用";
      }
    },
  },
};
</script>
<style lang='scss' scoped>
 .authurl {
   height: 180px;
 }
</style>
<style lang='scss'>
.el-dialog {
    width:425px;
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