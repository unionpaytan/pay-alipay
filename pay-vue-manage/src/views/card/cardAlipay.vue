<!-- 支付宝商家管理  cardAlipay-->
<template>

  <div>  
     <div class="flex flex-m tc topbox">
      <div class="flex-1">
        <div>支付宝商家<span style="font-size:12px;"></span></div>
        <div class="num">{{ countMap.masangTotal || 0 }}</div>
      </div> 
       <!-- <div class="flex-1">
        <div>码商总余额<span style="font-size:12px;"></span></div>
        <div class="num">{{ countMap.sumAccountBalance || 0 }}</div>
      </div>  -->
    </div>

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
          v-model="search_data.merchantId"
          placeholder="请选择" 
        >
          <el-option label="所属码商" value></el-option>
          <el-option
            v-for="item in payMerchantInfoList"
            :key="item.merchantId"
            :label="item.merchantName + ' ('+item.merchantId+')'"
            :value="item.merchantId"
          ></el-option>
        </el-select>
      </div> 

      <div class="flex flex-m mr20">
        <span class="w195 mr10">商家名称:</span>
        <el-input
          size="medium" 
          v-model="search_data.alipayName"
          clearable
          placeholder="支付宝商家名称"
          class="w150"
        ></el-input>
      </div>


      <div class="flex flex-m mr20">
        <span class="w195 mr10">UID:</span>
        <el-input
          size="medium" 
          v-model="search_data.alipayUid"
          clearable
          placeholder="支付宝 UID"
          class="w150"
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
        type="success"
        @click="adm_addForm()"
        >新增支付宝商家</el-button
      >
    </div>

    <el-table
      v-loading="m_tableLoading"
      @selection-change="m_handleSelectionChange"
      @current-change="m_handleCurrentChange" 
      class="mtable0"
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
     <el-table-column align="center" prop="createTime" label="创建时间"></el-table-column>
     <el-table-column align="center"   label="所属码商">
        <template slot-scope="scope">
          <span>{{ scope.row.merchantName}}</span><br>
          <span>{{ scope.row.payerMerchantId}}</span>
      </template> 
     </el-table-column>

     <el-table-column align="center"   label="码商操作员">
        <template slot-scope="scope">
          <span>{{ scope.row.operatorName}}</span><br>
          <span>{{ scope.row.payerOperatorId}}</span>
      </template> 
     </el-table-column> 

     <el-table-column align="center"   label="支付宝商家/UID" width="200">
        <template slot-scope="scope">
          <div align="center">
           <span align="center">{{ scope.row.alipayName}}</span><br>
        <span>{{ scope.row.alipayUid}}</span>
        </div>
      </template> 
     </el-table-column> 
      <el-table-column align="center" prop label="二维码可用/总数">
      <template slot-scope="scope">
          <div align="center">
           <span>{{ scope.row.countAvailCodes}}</span>/
           <span>{{ scope.row.countTotalCodes}}</span>
        </div>
      </template> 
      </el-table-column> 
  

      <el-table-column align="center" label="是否登录">
        <template slot-scope="scope">
          <span :class="scope.row.isBind == '1' ? 'green' : 'red'">{{
            scope.row.isBind | isBind
          }}</span>
           <div>
            <el-button  
              v-if="scope.row.isBind == '1' "
              @click.native="handle_unbind(scope.row)"
              size="small"
              type="danger">
              <span>退出</span>
            </el-button>
          </div>
        </template>
      </el-table-column>

       <el-table-column align="center" label="启用状态">
        <template slot-scope="scope">
          <!-- <span :class="scope.row.status == '0' ? 'green' : 'red'">{{
            scope.row.status | status
          }}</span> --> 
          <el-switch  
            v-model="scope.row.status" 
            active-color="#13ce66"
            inactive-color="#c0c0c0"
            active-value="2" 
            inactive-value="0"
            @change="handleSwitchChange($event,scope.row)">
          </el-switch>
        </template>
      </el-table-column>

      <el-table-column align="center" label="操作" fixed="right" width="128">
        <template slot-scope="scope">
           <div>
            <el-button   
              @click.native="handle_barcode(scope.row)"
              type="text">
              <span>二维码列表</span>
            </el-button>
          </div>
          <div>
            <el-button  
              :disabled = "scope.row.isBind == '1' ? true : false" 
              @click.native="handle_bindnlogin(scope.row)"
              type="text">
              <span>支付宝登录</span>
            </el-button>
          </div>

          <div>
          <el-button
            v-if="handelAuto('edit')"
            @click.native="adm_editForm(scope.row)"
            type="text">编辑</el-button>
          </div>   

           <div>
            <el-button
              v-if="handelAuto('edit')"
              style="color: #ff0000"
              @click.native="handelDel(scope.row)"
              type="text">删除</el-button>
          </div> 
        </template>
      </el-table-column>
    </el-table>

     <!-- ** @新增/编辑 弹框 -->
    <el-dialog
      :close-on-click-modal="false"
      v-if="m_show"
      :title="m_isadd ? '支付宝商家添加' : '支付宝商家编辑'"
      :visible.sync="m_show" 
      style="margin-top: -100px"
    >
      <el-form :model="formData" :rules="m_isadd ? m_rules_add : m_rules_edit" ref="ruleForm" style="margin-top: -20px">
        <div class>
            <el-form-item label="所属码商" label-width="100px" prop="payerMerchantId">
          <el-select
            clearable
            @change="onSelectChangeAgent($event)"
            v-model="formData.payerMerchantId"
            placeholder="请选择收款码所属码商"
          >
            <el-option
              v-for="item in payMerchantInfoList"
              :key="item.merchantId"
              :label="item.merchantName + ' ('+ item.merchantId + ')'"
              :value="item.merchantId"
            ></el-option>
          </el-select>
        </el-form-item>   

        <el-form-item label="码商操作员" label-width="100px" prop="payerOperatorId">
          <el-select
            clearable
            @change="onSelectChangeOperator($event)"
            v-model="formData.payerOperatorId"
            placeholder="请选择收款码所属码商操作员"
          >
            <el-option
              v-for="item in payerOperatorList"
              :key="item.merchantId"
              :label="item.merchantName + ' ('+ item.merchantId + ')'"
              :value="item.merchantId"
            ></el-option>
          </el-select>
        </el-form-item>  

         <!-- 支付宝商家名称 -->
        <el-form-item class="flex-1" label="商家名称" label-width="100px" clearable  prop="alipayName">
           <el-input  placeholder="支付宝商家名称"  v-model="formData.alipayName"  clearable  auto-complete="off"></el-input>
        </el-form-item>

         <el-form-item class="flex-1" label="商家UID" label-width="100px" clearable  prop="alipayUid">
           <el-input readonly=""  placeholder="支付宝商家UID(登录后自动绑定)"  v-model="formData.alipayUid"  clearable  auto-complete="off"></el-input>
        </el-form-item>

         <el-form-item class="flex-1" label="备注" label-width="100px" clearable  prop="remark">
           <el-input  placeholder="备注"  v-model="formData.remark"  clearable  auto-complete="off"></el-input>
        </el-form-item>

         <el-form-item label="管理员谷歌" label-width="100px" prop="googleCode">
            <el-input
              type="number"
              placeholder="输入6位谷歌Google验证码"
              v-model="formData.googleCode"
              auto-complete="off"
              clearable
              @mousewheel.native.prevent
            ></el-input>
        </el-form-item> 


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
    <!-- 支付宝/个人商家登录 -->
     <el-dialog
      v-if="alipay_login_show"
      :title="alipayTitle"
      :visible.sync="alipay_login_show" 
      style="margin-top: -50px">
      <div v-loading="qrcode_loading"> 
          <div class="barcode-container">
              <canvas ref="qrcodeCanvas" :width="size" :height="size"></canvas>
               <div v-if="show_mask" class="mask"></div>
               <div v-if="show_scanned" class="scanned status"><p>
                <img class="barcode-icon-pic" src="https://t.alipayobjects.com/images/rmsweb/T1HEXhXfdcXXXXXXXX.png" style="width:40px;height:40px;" seed="first-barcodeIconPic" smartracker="on">
               </p>
               <p>扫码成功</p>
              </div>
               <div v-if="show_confirmed" class="scanned status"><p>已确认</p></div>
               <div class="text-below">请使用手机支付宝扫码</div>
          </div>
      </div> 
      <div slot="footer" class="dialog-footer"> 
        <el-button @click="alipay_login_show = false">关 闭</el-button> 
      </div>
      <div v-if="uploadingModal" class="loading-overlay"><img width="25" src='https://images.h-ui.net/icon/detail/loading/058.gif'>&nbsp;{{uploadingTxt}}</div>
    </el-dialog> 
    <!-- 删除商家  -->
    <el-dialog
      v-if="merchant_show_del"
      title="删除支付宝商家"
      :visible.sync="merchant_show_del"
       style="margin-top: -100px"
    >
      <div v-loading="d_merchant_loding">
        <el-form :model="merchant_data" :rules="merchantGoogleForm_rules" ref="googleform">
          <el-form-item label="删除支付宝商家" label-width="110px">
            <span>{{ merchant_data.alipayName }}</span>
          </el-form-item>

          <el-form-item label="UID" label-width="110px">
            <span>{{ merchant_data.alipayUid }}</span>
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
<!-- 二维码显示 -->
    <el-dialog 
      :title="barcodeTitle"
      v-if="isOpenDialog"
      :visible.sync="isOpenDialog"
      style="margin-top:-60px;"
      width="90%"  
      >
      <div :style="{ height: containerHeight }">
        <div class="table-container">
          <div class="div-container">
          <el-button  size="small" type="primary" @click="handleOpenSelectedDialog">导入二维码</el-button>
          </div>
          <div class="div-container">
          <el-button   size="small" type="danger" @click="handlePayedBatchDel(modalData)">删除已付款</el-button>
          </div>
           <div class="div-container">
          <el-button 
        @click="batchModifyStatus(2)"
        icon="el-icon-circle-close"
        size="small"
        type="primary"
        >启用</el-button>
 </div>
 <div class="div-container">
      <el-button
        @click="batchModifyStatus(0)"
        icon="el-icon-circle-close"
        size="small"
        type="warning"
        >禁用</el-button>
 </div>
          <div class="div-container">
          <el-button  size="small" type="danger" @click="handleSelectedBatchDel(modalData)">批量删除</el-button>
          </div>
       
          <div class="div-container">
            <el-button size="small"   @click.native="handleModalClose()">关闭</el-button>
          </div>
 </div> 
        
        <div>
          <el-table
          v-loading="m_tableLoading" 
          @selection-change="modal_handleSelectionChange"
          @current-change="modal_handleCurrentChange"
          class="mtable"
          :stripe="true"
          tooltip-effect="dark"
          :highlight-current-row="true"
          :data="modal_list"
        >

      <!-- prop 数据库里的字段 -->
      <el-table-column type="selection" width="45"></el-table-column>
      <el-table-column label="序号" type="index" width="50" align="center">
        <template slot-scope="scope">
          <span>{{ (modal_page.page - 1) * modal_page.rows + scope.$index + 1 }}</span>
        </template>
      </el-table-column>

          <el-table-column align="center" prop label="收款码" width="182">
           <template slot-scope="scope">
             <img   @click="showImage(scope.row)" class="authurl" :src="'/uploads/' + scope.row.payerAddr  + '.png'" style="width:50px;height:50px;cursor:pointer;" alt /><br>
          </template>
         </el-table-column> 

        <el-table-column  align="center" prop="payerAddr" label="代付订单编号" width="205">
          <template slot-scope="scope">
            <span>{{scope.row.payerAddr}}</span>
          </template>
        </el-table-column> 

         <el-table-column  align="center" prop="payerCodeShowTimes" label="可拉单次数">
            <template slot-scope="scope">
              <span>{{scope.row.payerCodeShowTimes}}</span>
            </template>
          </el-table-column>

          <el-table-column  align="center" prop="payerCodePrice" label="金额(元)">
            <template slot-scope="scope">
              <span>{{scope.row.payerCodePrice}}</span>
            </template>
          </el-table-column>
           <el-table-column align="center" label="状态">
        <template slot-scope="scope">
          <span :class="scope.row.status | payerCardStatusClass">{{
            scope.row.status | payerCardStatusFilter
          }}</span>
        </template>
      </el-table-column>

      <el-table-column align="center" label="操作" width="130">
         <template slot-scope="scope">
           <el-button  @click.native="handelModalModifyStatus(scope.row)" type="text"><span :class="scope.row.status | payerStatusChangeClass">{{scope.row.status | payerStatusChangeFilter}}</span></el-button>
           <el-button  @click.native="handelModalDel(scope.row)" type="text" style="color: #ff0000">删除</el-button>
         </template>
      </el-table-column> 
      </el-table>
 <Paging
      class="mt20 mb30"
      :pageIndex="modal_page.page"
      :pageSize="modal_page.rows"
      :pageTotal="modal_page.total"
      @changeSize="modal_changesize"
      @changeIndex="modal_changeindex"
    ></Paging>

        </div>
      </div>
    </el-dialog>
 
     <!-- 导入二维码 -->
     <el-dialog 
      title="导入二维码 (支付宝订单编号、二维码不能重复)"
      v-if="isOpenSelectedDialog"
      :visible.sync="isOpenSelectedDialog"
      style="margin-top:-30px;"
      width="80%"  
      >
      <div :style="{ height: containerHeight }">
        <div class="table-container">
          <div class="div-container">
             <el-button  type="primary" @click="handleOpenUploadDialog">1.选择二维码</el-button>
          </div>

          <div class="div-container">
             <el-button  type="primary" @click="handleOpenSelectedBatchPrice">2.批量更新金额</el-button>
          </div>
           
          <div class="div-container">
             <el-button  type="warning" @click="handleOpenSelectedAdd">3.确认添加</el-button>
          </div>
          <div class="div-container">
             <el-button type="danger" @click="handleOpenUploadClear">清空</el-button>
          </div>
          <div class="div-container">
             <el-button  @click="handleOpenUploadClose">关闭</el-button>
          </div>
        </div>  
        <!--   :style="{ maxHeight: containerHeight }" -->
        <div>
          <el-table
          v-loading="temp_tableLoading" 
          class="mtable"
          :stripe="true"
          tooltip-effect="dark"
          :highlight-current-row="true"
          :data="m_temp_list" 
         >
         <el-table-column align="center" prop label="序号" width="60">
             <template slot-scope="scope">
               <span>{{scope.$index + 1}}</span>
            </template>
          </el-table-column>
           <el-table-column align="center" prop="name" label="支付宝订单编号">
             <template slot-scope="scope">
               <span>{{scope.row.name}}</span>
            </template>
          </el-table-column>
            <el-table-column align="center" prop label="收款二维码">
             <template slot-scope="scope">
               <span><img style="height:60px;" :src="getImageUrl(scope.row.payerAddr)"></span>
            </template>
           </el-table-column>
          <el-table-column align="center" prop label="启用状态">
             <template slot-scope="scope"> 
              <el-switch  
              v-model="scope.row.status" 
              active-color="#13ce66"
              inactive-color="#c0c0c0"
              active-value="2" 
              inactive-value="0">
            </el-switch>
            </template>
          </el-table-column> 
           <el-table-column align="center" prop label="可拉单次数">
             <template slot-scope="scope"> 
               <el-input  type="number" style="width:98px;" placeholder="5"  v-model="scope.row.payerCodeShowTimes"  clearable  auto-complete="off"></el-input>
            </template>
           </el-table-column>
           <el-table-column align="center" prop label="金额">
             <template slot-scope="scope"> 
               <el-input  type="number" style="width:98px;" placeholder="金额"  v-model="scope.row.amt"  clearable  auto-complete="off"></el-input>
            </template>
           </el-table-column>
           <el-table-column align="center" label="操作" width="130">
            <template slot-scope="scope">
              <el-button type="danger" @click="handleDelete(scope.$index)">删除</el-button>
            </template>
          </el-table-column> 
        </el-table> 
        </div>
      </div>
      <div v-if="uploading" class="loading-overlay"><img width="25" src='https://images.h-ui.net/icon/detail/loading/058.gif'>&nbsp;{{uploadingTxt}}</div>
    </el-dialog>
 

     <!-- 选择二维码 -->
    <el-dialog 
          title="选择二维码 (请一次性不要超过8张）"
          v-if="isOpenUploadDialog"
          :visible.sync="isOpenUploadDialog"
          width="50%" 
          >
          <div class="container">
          <div class="left">
            <div class="upload-container">
              <VueUploadImgs
                  multiple
                  compress
                  :before-read="beforeRead"
                  :after-read="afterRead"
                  :before-remove="beforeRemove"
                  :limit="limit"
                  :type="type"
                  @preview="preview"
                  @exceed="exceed"
                  @oversize="oversize"
                  v-model="files"
                  :label="label"
              ></VueUploadImgs>
          </div> 
            <div v-if="uploadingImage" class="loading-overlay"><img width="25" src='https://images.h-ui.net/icon/detail/loading/058.gif'>&nbsp;{{uploadingTxt}}</div>
         </div>
          <div class="right"><el-button @click="handleUpload">确认上传</el-button></div>
        </div>
    </el-dialog>

      <!-- 批量更新金额 -->
      <el-dialog 
          title="批量更新金额"
          v-if="isOpenBatchPrice"
          :visible.sync="isOpenBatchPrice"
          width="30%" 
          >
          <div class="container">
            <div class="left"> 
               <el-input type="number"  placeholder="金额"  v-model="batchPrice"  clearable  auto-complete="off"></el-input>
            </div>
          <div class="right"><el-button @click="handleBatchPriceUpdate">更新</el-button></div>
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
     <div v-if="imageModal" class="modal-live" @click="closeModalLive">
      <!-- <span class="close-live">&times;</span> -->
      <img class="modal-content"  :src="imageUrlModal">
    </div>
    
  </div>
</template>

<script>
import QRCode from "qrcode";
import * as api from "@/api/public";
let CryptoJS = require("crypto-js");

export default {
  name: "cardAlipay",
  data() {
    return {
      // qrText: 'https://qr.alipay.com/_d?_b=PAI_LOGIN_DY&securityId=web%257Cauthcenter_qrcode_login%257Ca01544ac-0fe6-409d-8193-b3acec615768RZ42',
      // m_checkData:[], 已定义在index.js 
      m_loading:false,
      error:"",
      modalRows: 10, //默认条数
      imageModal:false,
      imageUrlModal:"",
      businessType:1,//绑定商家码
      qrcodeUrl:"",
      qrcodeTimestamp:"",//生成二维码的时间
      logoUrl: 'https://img.alicdn.com/tfs/TB1qEwuzrj1gK0jSZFOXXc7GpXa-32-32.ico',
      size: 300, // QR code size
      logoSize: 50, // Logo size
      show_mask:false,
      show_scanned:false,
      show_confirmed:false,
      payTime: 0, //多长时间后订单无效
      totalSeconds:60 * 4, //4分钟过期 
      intervalTime: "", //倒计时
      intervalMonitor: "", //监听是否成功
      intervalMonitorQrcode: "", //监听是否成功
      search_data: {},
      countMap:{},
      payMerchantInfoList:[],//从商户列表中查出 码商(agentRate == '-1')列表
      payerOperatorList:[],//agentRate == '-2' 操作员
      payerFollowerList:[],//agentRate == '-3' 收款人
      alipay_login_show: false,
      barcodeTitle:"",
      alipayInfoId:"",//支付宝商家的 id，非 UID
      alipayUid:"",
      channelCode:"",
      alipayUid:"",
      modalData:{
         alipayInfoId:"",
         alipayUid:"",
         alipayInfoId:"",
         merchantId:"",
         payerMerchantId:"",
         payerOperatorId:"",
         channelCode:"",
         channelName:"",
      },
      formData: {
        id: "",
        alipayName:"",
        alipayUid:"",
        payerMerchantId:"",//码商
        payerOperatorId:"",//操作员
        tableMerchantId: "", 
        remark:"", 
      },
      authurl: "",
      //邮箱
      sendNum: 0,
      time: null,
      get_loading: false,

      merchant_data: { 
        googleCode:"",
      },

      ipShow: false,
      webShow: false,
      ip_loading: false,
      d_ip_loading: false,

      //ip 白名单
       ip_data: {
        merchantId: "", //商户ID
        tableMerchantId: "", //管理员ID
        ipStr: "", //ip列表
        googleCode:"",
        emailCode:"",
      },

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
      alipayTitle:"",
      merchant_loding: false,
      qrcode_loading: false,
      d_merchant_loding: false,
      merchant_show_google: false, //google
      agent_balance_show:false,
      merchant_show_del: false,
      merchant_show_status: false,
      
      m_rules_add: {
        alipayName: [{ required: true, message: "请输入", trigger: "blur" }], 
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }],
       
      },

      m_rules_edit: {
        alipayName: [{ required: true, message: "请输入", trigger: "blur" }], 
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }],
 
      }, 
      merchantGoogleForm_rules: { 
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }],
        // emailCode: [{ required: true, message: "请输入", trigger: "blur" }]
      }, 
      roleList: [], //角色列表
      isOpenDialog:false,
      isOpenSelectedDialog:false,//导入
      isOpenUploadDialog:false,//上传
      isOpenBatchPrice:false,//批量更新金额
      uploading:false,
      uploadingImage:false,
      uploadingModal:false,
      uploadingTxt:"",
      loading:false,
      that: this,
      batchPrice:200,
      files: [
                  // {
                  //     url: 'https://pic3.zhimg.com/v2-058f646c41b55206f8110489d82fa103_is.jpg',
                  //     name: 'user.jpg'
                  // }
              ],
      maxSize: 1024 * 5, // 5m
      previewIMG: null,
      limit: 30,
      label: "选择图片",
      isPreview: false,
      type: 1, // 0 预览模式 1 列表模式 2 预览模式 + 上传按钮
      containerHeight: `${window.innerHeight * 0.9}px`,
      m_tableLoading:false,
      modal_list:[],
      m_temp_list:[],//临时存放图片
      temp_tableLoading:false,
    };
  },
  created() {
    this.userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
    this.init();
    window.addEventListener('resize', this.updateContainerHeight);
    this.updateContainerHeight();
  },
  mounted() {
     clearInterval(this.intervalMonitor);
     clearInterval(this.intervalTime); 
     clearInterval(this.intervalBarcode);
  },
  
  destroyed() {
    // 销毁enter事件
    //this.enterKeyupDestroyed();
    clearInterval(this.intervalMonitor);
    clearInterval(this.intervalTime); 
    clearInterval(this.intervalBarcode);
    window.removeEventListener('resize', this.updateContainerHeight);
  },
  watch: {
    alipay_login_show: function () {
        if (!this.alipay_login_show) { 
          clearInterval(this.intervalMonitor);
          clearInterval(this.intervalTime); 
          clearInterval(this.intervalBarcode);
          console.log("clear interval alipay")
        }
     },
    payTime: function () {
         if (this.payTime < 1 && this.payTime > -1) {
             clearInterval(this.intervalTime);
             clearInterval(this.intervalMonitor);
             clearInterval(this.intervalBarcode);
            this.alipay_login_show = false;
            this.m_error("扫码过期")
         }
    }
  },
  methods: {
    init() {
      var ciphertext = CryptoJS.AES.encrypt('my message', 'secretKey').toString();
      console.log("ciphertext",ciphertext);
      clearInterval(this.intervalTime);
      clearInterval(this.intervalMonitor);
      clearInterval(this.intervalBarcode);
      this.show_mask = false;
      this.show_scanned = false;
      this.alipay_login_show = false;
      this.show_confirmed = false;
      this.uploadingModal = false;
      this.m_search();
    },   
    m_search() { 
      this.getDatalist();
      // this.queryCoinChannelDepositInfo();
      this.queryPayMerchantInfoList();
    },
   //码商下级操作员
    onSelectChangeAgent(val) {
      if (val){
        var agentRate = "-2";//操作员
        this.queryPayerFollowerList(val,agentRate);
      }  
    },

    onSelectChangeOperator(val) {
      if (val){
        var agentRate = "-3";//收款人
        this.queryPayerFollowerList(val,agentRate);
      }  
    },

   queryPayerFollowerList(v,agentRate) {
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      var data = {}
      if (agentRate == '-2') {
          data = {
             "agentRate":agentRate, 
             "parentId":v,
             "tableMerchantId":userData.merchantId,
            }
      }else {
           data = {
             "agentRate":agentRate,
             "operatorId":v, 
             "tableMerchantId":userData.merchantId,
            }
      }
      this.m_api.queryPayMerchantInfoList(data).then((res) => { 
          if (agentRate == "-2") {
            this.payerOperatorList = res.data;
          }else {
            this.payerFollowerList = res.data;
          }
         
      });
    }, 

    //码商:获取数据信息
    getDatalist(curPage) { 
      this.m_tableLoading = true;
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.search_data.tableMerchantId = userData.merchantId || 0; 
      if (curPage != null && curPage != "") {
           this.m_page.page = curPage; 
      } 
      let data = Object.assign(this.search_data, this.m_page);
     
      api
        .queryAlipayInfo(data)
        .then((res) => {
          this.m_list = res.data.data.records;
          this.m_page.total = res.data.data.total;
          this.countMap.masangTotal = res.data.countTotal;  
          // this.countMap.sumAccountBalance = res.data.sumAccountBalance;  
        })
        .finally(() => {
          this.m_tableLoading = false;
        });
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

    addData() {
      this.m_loading = true;

      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.formData.tableMerchantId = userData.merchantId || 0;
      api
        .addAlipayInfo(this.formData)
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
      let from = this._dep(this.formData);
      from.merchantName = this.formData.userName;
      api
        .updateAlipayInfo(from)
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
      const that = this;
      this.m_loading = true;
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      let tableMerchantId = userData.merchantId || 0;
      // this.$refs['googleForm'].validate((valid) => {
      // if (valid) {
          api.delAlipay({
            id:v.id,
            payerMerchantId: v.payerMerchantId,
            payerOperatorId:v.payerOperatorId,
            tableMerchantId: tableMerchantId,
            googleCode: v.googleCode, 
          })
          .then((res) => {
            // 删除成功
            that.m_loading = false;
            that.m_success("删除成功");
            that.getDatalist();
            that.merchant_show_del = false;
          })
          .catch((res) => {
            //console.log(res);
            that.m_loading = false;
            that.merchant_show_del = false;
          });
      // }

      // });
     
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
  
    async handle_bindnlogin(row){
      const that = this;
       this.show_mask = false;
       this.show_scanned = false; 
       this.show_confirmed = false;
       this.merchant_data.id = row.id;
       this.merchant_data.uuid = row.uuid;
       this.merchant_data.alipayName = row.alipayName
       this.alipayTitle = "支付宝商家【"+ row.alipayName +"】扫码登录"; 
       this.alipay_login_show = true;//支付宝扫码登录框
       this.uploadingModal = false;//modal蒙板
       this.qrcode_loading = true;
       setTimeout(function(){
           that.generateQRCodeWithLogo(row.uuid);//商家ID || 个人码时需传递 payerAddr
       },200)
    },

   async handle_unbind(row){
      const that = this;
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      let data = {
        id:row.id,
        payerMerchantId: row.payerMerchantId, //码商
        payerOperatorId: row.payerOperatorId, //码商操作员
        tableMerchantId:userData.merchantId,//管理员
      };
      let txtData = {
           msg: `退出后商家 ${row.alipayName} 的收款码无法再自动回调,是否确定？`
      };
      this.m_confirm(txtData).then(async res => {
        if (res) { 
          await api.unbindAlipay(data);
          this.m_success("操作成功"); 
          this.getDatalist();
        }else {

        }
      }); 
    },


    //商户Google
    handelMerchantGoogle(row) {
      //this.merchant_data = this._dep(this._merchant_data);
      this.merchant_data.userName = row.userName; 
      this.merchant_data.merchantId = row.merchantId;  
      this.getNewGoogleAuthUrl(row.userName, 1); 
      this.d_merchant_loding = false;
      this.merchant_show_google = true;
    },

    handelAgentBalance(row) { 
      console.log("handle agent balance")
      this.merchant_data.userName = row.userName; 
      this.merchant_data.merchantId = row.merchantId;   
      this.merchant_data.amt = "";
      this.d_merchant_loding = false;
      this.agent_balance_show = true;
      
    },
    //按钮switch
    handleSwitchChange(event,row) { 
      //console.log("switch",row)
      this.handle_updateUserStatus(row);
    }, 

    //启用||禁用
    handle_updateUserStatus(row) {
      const that = this; 
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      var status = row.status;
      var tempStatus = row.status == "2" ? "0" : "2";
      console.log("tempStatus ",row.status);
      let txt = "";
      let msg = "";
      let data = {
        id:row.id,
        merchantId: row.merchantId, //管理员
        status: "",
        tableMerchantId:userData.merchantId,//管理员
      };
      if (status == "2") {
        //用禁用
        txt = "启用";
        msg = `是否确定 ${txt} 商家 ${row.alipayName} ？`
        data.status = "2";
      } else {
        txt = "禁用";
        msg = txt + `后商家 ${row.alipayName} 的收款码无法再自动回调,是否确定？`
        //要启用
        data.status = "0";
      }
      let msgData = {
        msg: msg
      };
      this.m_confirm(msgData).then(async res => {
        if (res) {
          console.log("switch data:",res);
          await api.updateAlipayInfoStatus(data);
          this.m_success("操作成功");
        }else {
          row.status = tempStatus;
        }
      }); 
     
    },


    handelModalDel(row) {
      const that = this;
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || 0;
      //唯一卡号
      let ids = [row.payerAddr].join(",");
      var txt = "删除";
   
      let formObj = {
        status,
        payerAddrs: ids, //二维码链接地址列表
        tableMerchantId: userData.merchantId,
      };

      this.m_confirm({ msg: `是否确认 ${txt} 收款码` }).then((res) => {
        if (res) {
          this.m_api.batchDelPayerInfoStatus(formObj).then((res) => {
            this.m_success("操作成功");
            this.getModalDatalist();
            that.getDatalist(that.m_page.page);//商家列表的当前页面
          });
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
          this.m_api
            .modifyMerchantGoogle({
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


    agentBalanceSubmit(v) {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      let tableMerchantId = userData.merchantId || 0;

      this.$refs["merchantGoogleForm"].validate(valid => {
        if (valid) {
          this.d_merchant_loding = true;
          this.m_api
            .modifyAgentBalance({
              merchantId: v.merchantId,
              amt:v.amt,
              tableMerchantId: tableMerchantId,
              googleCode: v.googleCode,
            })
            .then(res => {
             
              this.getDatalist();
              this.m_success("操作成功");
              this.agent_balance_show = false;
               this.d_merchant_loding = false;
            })
            .catch(res => {
              this.d_merchant_loding = false;
            });
        }
      });
    }, 

   async registerCoinHolder(data) {
      console.log("register to coinholder - ",data);
      if (data.payerAddr == ""){ 
         return false;
      } 
 
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.formData.tableMerchantId = userData.merchantId;
      this.m_loading = true;
      this.m_api
        .registerCoinHolder(data)
        .then((res) => { 
          return true;
        })
        .catch((res) => {
          return false;
        })
        .finally(() => {
         
        });
    },


    //
    updateUserStatus(data) { 
      api.updateAlipayInfoStatus(data).then((res) => {
        //操作成功
        this.merchant_show_status = false;
        this.m_success("操作成功");
        this.getDatalist();
      });
    }, 

    adm_addForm() {  
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
      this.formData.tableMerchantId = row.merchantId;
      this.m_show = true;
    },

    //单条数据
    handelModalModifyStatus(row) {
      let that = this;
      let userData =  (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
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
        msg: `是否确定${txt}收款码 ${row.payerAddr} ？`,
      };
      this.m_confirm(msgData).then((res) => {
        if (res) {
          that.modifyCoinHolderStatus(data);
        }
      });
    },
     modifyCoinHolderStatus(data) {
      const that = this;
      this.m_api.modifyCoinHolderStatus(data).then((res) => {
        //操作成功
        this.m_success("操作成功");
        this.getModalDatalist();
        that.getDatalist(that.m_page.page);//商家列表的当前页面
      });
    },

    // 新增google key
    getNewGoogleAuthUrl(v,type) {
      this.m_api
        .getNewGoogleAuthUrl({ v })
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
      this.merchant_data.id = row.id;
      this.merchant_data.payerMerchantId = row.payerMerchantId;
      this.merchant_data.payerOperatorId = row.payerOperatorId;
      this.merchant_data.alipayName = row.alipayName;
      this.merchant_data.alipayUid = row.alipayUid;
      this.merchant_data.googleCode = ""; 
      this.d_merchant_loding = false;
      this.merchant_show_del = true;
    },

    async generateQRCodeWithLogo(id){ 
       clearInterval(this.intervalMonitor);
       clearInterval(this.intervalBarcode);
       await this.queryQrcodeUrlById(id); 
     
      try {
        // Generate QR code to canvas
        const canvas = this.$refs.qrcodeCanvas;
        const ctx = canvas.getContext('2d');
        await QRCode.toCanvas(canvas, this.qrcodeUrl, {
          errorCorrectionLevel: 'H',
          width: this.size,
        });

        // Load logo image
        const logoImage = new Image();
        logoImage.src = this.logoUrl;
        logoImage.onload = () => {
          // Calculate logo position
          const x = (this.size / 2) - (this.logoSize / 2);
          const y = (this.size / 2) - (this.logoSize / 2); 
          // Draw logo on top of QR code
          ctx.drawImage(logoImage, x, y, this.logoSize, this.logoSize);
        };
        this.qrcodeTimestamp =  new Date().getTime();//生成二维码的时间
        this.monitorSuccess(id);

      } catch (e) {
        console.error('generating QR code with logo ' + this.error);
        this.qrcode_loading = false;
        this.m_error("generating QR code with logo "  + this.error);
      }
      
    },
    //puppeteer 查找QRCODE
    async queryQrcodeUrlById(id){
     const that = this;
      let nodeServer = process.env.VUE_APP_NODE_SERVER;// "http://localhost:3000";//
      try {
        const response = await fetch(nodeServer + "/getBarcode/id/"+id, {
          method: 'POST',
          headers: {
            'Authorization':sessionStorage.token,
            'Content-Type': 'application/json'
          }
        });
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        if (data.code === '0000') {
          that.qrcodeUrl = data.qrcodeUrl;
          that.qrcode_loading = false;
          that.error = ''; 
        } else {
          that.error =  data.msg;
          that.qrcode_loading = false;
        }
      } catch (error) {
        this.error = error.msg;
        that.qrcode_loading = false;
      }
      return this.error;

    },
    //查询二维码
    async monitorSuccess(id) {
      const that = this; 
      that.monitorTokenFromJavaServer(id); //取得 token
      that.monitorBarcodeStatusFromJavaServer(id); //监测二维码状态
      let timestamp = new Date().getTime(); //当前客服端的时间戳 改为服务器时间 
      let serverTimeRes = await this.fetchUrl("/deposit/getServerTime", "", "post");
      if (serverTimeRes != null) {
        console.log("server - local " + (serverTimeRes.data.serverTime - timestamp) / 1000);
        timestamp = serverTimeRes.data.serverTime;
      } 
      var seconds = (
        (timestamp - that.qrcodeTimestamp) /
        1000
      ).toFixed(0); //时间差的秒数
      console.log("seconds - " + seconds);
      that.payTime = that.totalSeconds - seconds; //剩余的秒数
      
      // if (that.payTime < 1) {
      //   that.alipay_login_show = false;
      //   clearInterval(that.intervalMonitor);
      //   clearInterval(that.intervalTime);
      //   that.m_error("扫码过期")
      //   return;
      // }
      that.intervalPayTime();
    },

    /**
     * 监听是否绑定成功
     */
    async monitorTokenFromJavaServer(id) {
      const that = this;
       // Clear any existing interval before setting a new one
      if (this.intervalMonitor) {
        clearInterval(this.intervalMonitor);
      }
      this.intervalMonitor = setInterval(function () {
        //每隔1秒查询服务器订单是否成功
        // console.log("monitor "+id+" alipay\'s token from java server");
        let data = "id=" + id + "&businessType=" + that.businessType;
        let url = "/AlipayAccountManage/monitorToken";
        let method = "post";

        //47173219990642971073535  47173219890642971073535
        fetch(process.env.VUE_APP_SERVER_URL + url, {
          method: method,
          headers: {
            "Authorization":sessionStorage.token,
            "Content-Type": "application/x-www-form-urlencoded",
          },
          body: data,
        })
          .then((res) => {
            return res.json();
          })
          .then((data) => {
            // console.log("server resturn data ",data.data);
            if (data != null && data.code == "0000" && data.data.isBind == "1") {
              that.alipay_login_show = false; 
              that.uploadingModal = false;
              that.m_success("登录成功");
              that.getDatalist();//重新查询一次
            }else if (data.code == "1000"){ 
              that.alipay_login_show = false; 
              that.uploadingModal = false;
              that.m_error("登录失败:" + data.message);
            }
          })
          .catch(function (err) { 
              that.alipay_login_show = false; 
              that.uploadingModal = false;
              that.m_error("登录失败:" + err);
          }).finally(()=>{
           
          });
      }, 1000 * 2); //2秒监听一次
    },

 
  async monitorBarcodeStatusFromJavaServer(id) {
      const that = this;
       // Clear any existing interval before setting a new one
      if (this.intervalBarcode) {
        clearInterval(this.intervalBarcode);
      }
      let seconds = 1;
      this.intervalBarcode = setInterval(function () {
        //每隔1秒查询服务器订单是否成功
        console.log("monitor "+id+" alipay\'s barcode from java server");
        let data = "id=" + id + "&seconds=" + seconds;
        let url = "/AlipayAccountManage/monitorBarcodeStatus";
        let method = "post";

        //47173219990642971073535  47173219890642971073535
        fetch(process.env.VUE_APP_SERVER_URL + url, {
          method: method,
          headers: {
            "Authorization":sessionStorage.token,
            "Content-Type": "application/x-www-form-urlencoded",
          },
          body: data,
        })
          .then((res) => {
            return res.json();
          })
          .then((data) => {
            // console.log("server resturn data ",data.data);
            if (data != null && data.code == "0000"){ 
              if (data.data.barcodeStatus == "scanned") {
                 that.show_mask = true;
                 that.show_scanned = true;
                 that.show_confirmed = false;
                //  that.uploadingModal = true;
                //  that.uploadingTxt = "正在登录..."
              } else if (data.data.barcodeStatus == "confirmed" || data.data.barcodeStatus == ""){
                 that.show_mask = true;
                 that.show_scanned = false;
                 that.show_confirmed = true;
                 that.uploadingModal = true;
                 that.uploadingTxt = "正在登录..."
              }
              
            }else if (data.code == "1000"){
              
            }
          })
          .catch(function (err) { 
              // that.alipay_login_show = false; 
              // that.m_error("登录失败:" + err);
          });
          seconds += 1;
          console.log("seconds",seconds);
      }, 1000 * 1); //1秒监听一次
    },

   intervalPayTime() {
      const that = this;
      this.intervalTime = setInterval(function () {
        that.payTime--;
      }, 1000 * 1);
    },

    async fetchUrl(url, data, method) {
      const that = this;
      return fetch(process.env.VUE_APP_SERVER_URL + url, {
        method: method,
        headers: {
          "Authorization":sessionStorage.token,
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: data,
      })
        .then((res) => {
          return res.json();
        })
        .then((data) => {
          return data;
        })
        .catch(function (err) {
          console.log(err);
          that.d_loding = false;
          return "";
        });
    },
    
    /**
     * upload barcode images
    */

   oversize(file) {
          console.log('oversize')
          console.log('filesize:' + file.size / 1024 + 'KB')
   },
   //选择图片
    afterRead(files) { 
        console.log("after-read images ",files)
        this.checkDDuplicates(files,"1");
    },
    checkDDuplicates(files,clearit){
        console.log("check files",files);
        let fileNameSet = new Set();
        let hasDuplicates = false;
        let duplicateFilename = "";
        files.forEach(item => {
          if (item.name != null) { 
            let fileName = "";
            if (item.name.indexOf(".png") > 0 || item.name.indexOf(".jpg") > 0) {
                fileName = item.name.substring(0, item.name.length - 4);
            }else {
                fileName = item.name;
            }
            if (fileNameSet.has(fileName)) {
              hasDuplicates = true;
              duplicateFilename = fileName;
            } else {
              fileNameSet.add(fileName); 
            }
          }
        });

        if (hasDuplicates) {
          if (clearit == "1") {
            this.files = []
          } 
          alert("支付宝订单编号 "+duplicateFilename+" 重复,请检查");
          console.log("There are duplicate file names in the array.");
          return {
              "duplicateFilename":duplicateFilename,
              "isDuplicate":true,
          };
        } else {
          console.log("No duplicate file names found.");
          return {
              "duplicateFilename":'',
              "isDuplicate":false,
          };
        }
    },

    beforeRemove(index, file) {
        console.log(index, file)
        return true
    },

    preview(index, file) {
        this.previewIMG = file.url
        this.isPreview = true
    },

    exceed() {
        alert(`只能上传${this.limit}张图片`)
    },

    beforeRead(files) {
        console.log('before-read')
        for (let i = 0, len = files.length; i < len; i++) {
            const file = files[i]
            if (file.type != 'image/jpeg' && file.type != 'image/png') {
                alert('只能上传jpg和png格式的图片')
                return false
            }
        } 
        return true
    },

    closePreview() {
        this.isPreview = false
    },
    handleImport(){
        console.log("files import successfully:",this.files);
      
    },

    handleUpload(){
      const that = this;
      console.log("this.files",this.files.length)
      if (this.files.length  == 0) {
          alert('请先选择图片')
          return;
      }
      //检查图片名称是否有重复
      let {duplicateFilename,isDuplicate} = this.checkDDuplicates(this.files,"0");
      if (isDuplicate) {
          console.log("添加的图片名称"+duplicateFilename+"有重复");
          return;
      }
      
      console.log("to upload files:",this.files);

      //1.检查每个图片的格式
      let isImageType = true;
      let isOversize = false;
      this.files.forEach(item=>{
        var validImageTypes = ["image/jpeg", "image/png","image/jpg"];
        if (!validImageTypes.includes(item.type)) {
            console.log(item.name + " invalid file type. Please upload an image.");
            alert(item.name + " invalid file type. Please upload an image.");
            isImageType = false;
            return;
        } 
      });
      if (!isImageType) {return;}
      //2.检查每个图片的大小
      this.files.forEach(item=>{
        var maxSizeInBytes = 5 * 1024 * 1024; // 5MB
        if (item.size > maxSizeInBytes) {
            console.log(item.name + " file size exceeds the 5MB limit.");
            alert(item.name + " file size exceeds the 5MB limit.");
            isOversize = true;
            return;
        }
      });
      if (isOversize) {return;}
      //上传图片到系统 server
      this.uploadingImage = true;   // Show loading overlay
      this.uploadingTxt = "图片正在上传";
      // {
      //           "name":fileName,
      //           "image":item.url,
      //           "qrcodeUrl":"",//服务器的图片路径 ../uploads/
      //           "amt":300,
      //         } 
      
      const uploadFiles = [];
      this.files.forEach(item=>{
          uploadFiles.push({"fileName":item.name,"imageUrl":item.url.split(',')[1]})//item.url
      });
      let eipherFiles =  CryptoJS.AES.encrypt(JSON.stringify(uploadFiles), 'secretKey').toString();
      let uploadObj = {
           "alipayInfoId":this.alipayInfoId,//哪个商家
           "files": JSON.stringify(uploadFiles), //转为JSON进行传递 JSON.stringify(uploadFiles)
      }
      console.log("uploadObj",uploadObj);
      this.m_api.batchUploadImage(uploadObj).then((res) => { 
          if (res.code == "0000") {
            this.m_success("上传成功"); 
            this.isOpenUploadDialog = false; 
            this.files = []; //清空图片  
            that.getTempDataList(res.data); //实际应传 {"name":"","payerAddr":qrcodeUrl}
          }else {
             this.m_error("上传失败" + res.message); 
          }
        
      }).finally(()=>{
          this.uploadingImage = false; 
        
      }); 
   
    },
    //二维码显示 list
    handle_barcode(row){
        this.modal_list = [];
        this.isOpenDialog = true;
        this.barcodeTitle = "【"+ row.alipayName +"】二维码列表"
        this.modalData.alipayInfoId = row.id;//支付宝商家id
        this.modalData.alipayUid = row.alipayUid;
        this.modalData.merchantId = row.payerMerchantId;
        this.modalData.payerMerchantId = row.payerMerchantId;
        this.modalData.payerOperatorId = row.payerOperatorId;
        this.modalData.channelCode = row.channelCode;
        this.modalData.channelName = row.channelName;
        this.updateContainerHeight();
        this.getModalDatalist();
    },
    //1.导入二维码
    handleOpenSelectedDialog(){ 
      console.log("导入二维码");
      this.handleOpenUploadClear();
      this.isOpenSelectedDialog = true; 
      this.updateContainerHeight();
    },

    handlePayedBatchDel(modalData){ 
      const that = this;
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || 0;
        let txt = "删除已付款"
         let formObj = {  
            businessType:this.businessType,
            alipayInfoId: modalData.alipayInfoId,
            payerMerchantId: modalData.payerMerchantId,
            payerOperatorId: modalData.payerOperatorId,
            tableMerchantId: userData.merchantId,
        };
        this.m_confirm({ msg: `是否确认${txt}收款码` }).then((res) => {
        if (res) {
            that.m_tableLoading = true;
            that.m_api.batchDelPayerPayedList(formObj).then((res) => {
            that.m_success("操作成功");
            that.getModalDatalist();
            that.getDatalist(that.m_page.page);//商家列表的当前页面
          });
        }
      });
    },
    handleSelectedBatchDel(modalData,status){
      const that = this;
      console.log("batch delete");
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || 0;
      let m_checkData = this.m_checkData;
      let len = m_checkData.length;
      if (!len) {
        this.m_warning("请勾选数据");
        return;
      }
       //唯一卡号
      let ids = m_checkData.map((item) => item.payerAddr).join(",");
      var txt = "批量删除";
   
      let formObj = { 
        status,
        payerAddrs: ids, //二维码链接地址列表
        tableMerchantId: userData.merchantId,
      };

      this.m_confirm({ msg: `是否确认 ${txt} 收款码` }).then((res) => {
        if (res) {
            that.m_tableLoading = true;
            that.m_api.batchDelPayerInfoStatus(formObj).then((res) => {
            that.m_success("操作成功");
            that.getModalDatalist();
            that.getDatalist(that.m_page.page);//商家列表的当前页面
          });
        }
      });
    }, 
    
     //批量修改状态 status 0 启用 1禁用
    batchModifyStatus(status) {
      const that = this;
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
            that.m_success("操作成功");
            that.getModalDatalist();
            that.getDatalist(that.m_page.page);//商家列表的当前页面
          });
        }
      });
    }, 
    
    //二、上传图片二维码
    handleOpenUploadDialog(){ 
        this.isOpenUploadDialog = true;
        this.updateContainerHeight();
      //  this.isOpenDialog = false;
    },

    //默认是 300的收款金tpkm
    handleOpenSelectedBatchPrice(){
      if (this.m_temp_list.length  == 0) {
          alert('请先选择收款二维码')
          return;
      }
        this.isOpenBatchPrice = true; 
    },
    //确认添加二维码
    async handleOpenSelectedAdd(){
      const that = this;
      let userData =  (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      if (this.m_temp_list.length  == 0) {
          alert('请先选择收款二维码')
          return;
      }
      let {duplicateFilename,isDuplicate} = this.checkDDuplicates(this.m_temp_list,"0");
      if (isDuplicate) {
          console.log("添加的图片名称"+duplicateFilename+"有重复");
          return;
      }
       
      //1.添加进数据库 
      console.log("m_temp_list",this.m_temp_list,this.m_page);
      this.uploading = true;
      this.uploadingTxt = "正在添加";
     
      this.m_temp_list.forEach(async (item)=>{
          let  data = { 
            "coinType":"1",
            "status":item.status,
            "channelCode":that.modalData.channelCode,
            "channelName":that.modalData.channelName,
            "alipayUid": that.modalData.alipayUid,
            "payerAddr":item.payerAddr,
            "merchantId":that.modalData.payerMerchantId,
            "payerMerchantId":that.modalData.payerMerchantId,
            "payerOperatorId":that.modalData.payerOperatorId,
            "payerCodePrice":item.amt, 
            "payerCodeTimes":1,
            "payerCodeShowTimes":item.payerCodeShowTimes, 
            "businessType":that.businessType, 
            "alipayInfoId":that.modalData.alipayInfoId,
            "tableMerchantId":userData.merchantId,
          }
          await that.registerCoinHolder(data);
      }); 
      //3.刷新二维码列表
      setTimeout(()=>{
          //2.添加完成后 关闭弹出窗口 
          this.uploading = false;
          this.isOpenSelectedDialog = false; 
          that.getModalDatalist();
          that.getDatalist(that.m_page.page);//商家列表的当前页面
      },1000) 

    }, 
      //清空
    handleOpenUploadClear(){
      // this.isOpenSelectedDialog = false;
      this.files = [];//清空图片
      this.m_temp_list = [];//清空图片 table
    }, 
    //关闭
    handleOpenUploadClose(){  
        if (this.m_temp_list.length == 0 ) {
            this.isOpenSelectedDialog = false;
        }else {
            this.quitUpload();
        }
    }, 
       //关闭
    handleModalClose(){  
        this.isOpenDialog = false; 
    }, 
    quitUpload() {
      this.$confirm('二维码还未确认添加,是否放弃添加?', '提示', {
        iconClass: "el-icon-question",//自定义图标样式
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(_ => {
          this.isOpenSelectedDialog = false;
          this.handleOpenUploadClear();//清空
      }).catch(_ => { })
    },

    updateContainerHeight() {
      this.containerHeight = `${window.innerHeight * 0.7}px`;
    },
    //获取商家收款二维码
    getModalDatalist() { 
      //console.log("查询" + JSON.stringify(this.search_data))
      const that = this;
      this.m_tableLoading = true;
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.search_data.tableMerchantId = userData.merchantId 
      // this.modal_page.rows = this.modalRows;
      this.search_data.alipayInfoId= this.modalData.alipayInfoId;//row.id;
      let data = Object.assign(this.search_data, this.modal_page);
      this.m_api
        .queryCoinHolderInfoPage(data)
        .then((res) => {
          this.modal_list = res.data.pages.records; //记录
          this.modal_page.total = res.data.pages.total;
          //异步查询公链上的PHP余额
          that.m_list.forEach((item)=>{
            //that.gettingSpenderBalance(item);
          }); 

        })
        .finally(() => {
          this.m_tableLoading = false; 
          this.m_checkData = [];//清空选择项
        }); 
       
    },

    getTempDataList(tempFiles){ 
      let tempList = [];
      tempFiles.forEach(item=>{
          let fileName = item.name != null ? item.name : "";
          tempList.push(
              {
                "name":fileName,
                "payerAddr":item.payerAddr,//服务器的图片路径 ../uploads/
                "amt":200,
                "payerCodeShowTimes":5,
              }
          )
      });

      // console.log("tempFiles ",tempFiles)
      // console.log("tempList ",tempList)
      this.temp_tableLoading = true;
      //this.m_temp_list = tempList;//把数据分配给 table
      this.m_temp_list = [...this.m_temp_list, ...tempList];

      setTimeout(() => { 
          this.temp_tableLoading = false;
        }, 300);  

    },
  
    getImageUrl(imagePath) {
      return `../uploads/${imagePath}.png`;//图片文件的方式
      // return `${imagePath}`; base64的方式 
    },
    handleDelete(index){
        this.m_temp_list.splice(index, 1);
        console.log("m_temp_list - ",this.m_temp_list);
    },
    //批量更新价格
    handleBatchPriceUpdate(){
      const that = this;
      let tempList = [];
      this.temp_tableLoading = true;
      this.m_temp_list.forEach(item=>{
          tempList.push(
              {
                "name":item.name, 
                "status":item.status,//状态
                "payerAddr":item.payerAddr,//服务器的图片路径 ../uploads/
                "amt":that.batchPrice,
                "payerCodeShowTimes":5,
              }
          )
      }); 
      setTimeout(() => {
        that.isOpenBatchPrice = false; 
        that.m_temp_list = tempList;
        that.temp_tableLoading = false;
      }, 200);
    },

    showImage(row){
        this.imageModal = true;
        console.log(row.payerAddr)
        this.imageUrlModal = '/uploads/' + row.payerAddr  + '.png'
    },
    closeModalLive(){
        this.imageModal = false;
    }

  
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

/* barcode */
 .barcode-container {
    position: relative;
    width: 300px; /* 确保宽度和高度与QR码大小一致 */
    height: 300px;
    margin-left: 40px;;
    
  }
  
  .barcode-container canvas {
    display: block;
  }
  
  .barcode-container .mask {
    position: absolute;
    top: 0;
    left: 40;
    width: 100%;
    height: 100%;
    opacity: 0.7;
    background: rgba(0, 0, 0, 1);
    z-index: 1;
  }
  
  .scanned.status {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
    color: white;
    font-size: 14px;
    text-align: center;
    z-index: 2;
  }
  
  .barcode-icon-pic {
    width: 40px;
    height: 40px;
  }
  .text-below {
    font-size: 16px;
    color: #333;
    text-align: center;
    margin: 10px 0;
  } 
</style>
<style scoped>

  .table-container {
    display: flex;
    flex-direction: row;
    align-items: center; 
  }
  .div-container {
    padding-right: 10px;
    padding-bottom: 10px;
  }

  .login-container {
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .upload-container {
    max-height: 300px; /* Adjust the height as needed */
    overflow-y: auto; /* Enable vertical scrolling */
    border: 1px solid #ccc; /* Optional: Add a border to visually separate the scrollable area */
    padding: 10px; /* Optional: Add some padding */
   }
  
  .flex {
    margin-bottom: 20px; /* Adjust as needed */
  }
  
  .upload-button-container {
    align-self: flex-end;
    margin-top: 10px; /* Adjust as needed */
  }
    .container {
       display: flex;
    }

    .left {
    flex: 1;
    }

    .right {
    margin-left: 20px; /* Adjust as needed */
    }
    .loading-overlay {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.5); /* Semi-transparent black */
        z-index: 999; /* Ensure it's above other content */
        display: flex;
        justify-content: center;
        align-items: center;
        color: rgb(255, 255, 255);
    }
 
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
    .passwordClass {
      width:200px;
    }
  }
  .mtable{
    border: 1px solid #eee !important;
    max-height: 540px; /* Adjust the height as needed */
    overflow-y: auto; /* Enable vertical scrolling */
    width:98%;
  }

  .mtable0{
    border: 1px solid #eee !important; 
    overflow-y: auto; /* Enable vertical scrolling */
    width:98%;
  }

  .modal-live {
        display: block;
        position: fixed;
        z-index: 999999;
        padding-top: 100px;
        left: 0;
        top: 0;
        bottom:0;
        right:0;
        width: 100%;
        height: 100%;
        overflow: auto;
        background-color: rgba(0,0,0,0.4);
        border-radius:5px;
    }

    .modal-content {
        margin: auto;
        display: block;
        width: 80%;
        max-width: 450px;
    }

    .close-live {
        position: absolute;
        top: 15px;
        right: 35px;
        color: #fff;
        font-size: 40px;
        font-weight: bold;
        cursor: pointer;
    }

    .close-live:hover,
    .close-live:focus {
        color: #bbb;
        text-decoration: none;
        cursor: pointer;
    }
    
  </style>
