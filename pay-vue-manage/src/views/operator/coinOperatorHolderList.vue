<!-- 收款人钱包地址  coinOperatorHolderList
 -->
<template>
  <div>
    <div class="flex flex-m tc topbox">
      <div class="flex-1 bor">
        <div>总商家码数量<span style="font-size:12px;"></span></div>
        <div class="num">{{ countMap.countCoinHolder || 0 }}</div>
      </div> 
        <div class="flex-1 bor">
          <div>总挂码数量<span style="font-size:12px;"></span></div>
          <div class="num">{{ countMap.countCoinHolderAvailable || 0 }}</div>
        </div> 
       <div class="flex-2">
        <div>挂码情况<span style="font-size:12px;"></span></div>
        <div class="num">
            <div class="num" style="font-size:16px;line-height:170%;">
             200<span style="font-size:13px;" :class="countMap.count200 < 5 ? (countMap.count200 < 1 ? 'gray' : 'red') : 'green'">[{{countMap.count200 || 0}}]</span> 
             300<span style="font-size:13px;" :class="countMap.count300 < 5 ? (countMap.count300 < 1 ? 'gray' : 'red') : 'green'">[{{countMap.count300 || 0}}]</span> 
             400<span style="font-size:13px;" :class="countMap.count400 < 5 ? (countMap.count400 < 1 ? 'gray' : 'red') : 'green'">[{{countMap.count400 || 0}}]</span> 
             500<span style="font-size:13px;" :class="countMap.count500 < 5 ? (countMap.count500 < 1 ? 'gray' : 'red') : 'green'">[{{countMap.count500 || 0}}]</span> 
             600<span style="font-size:13px;" :class="countMap.count600 < 5 ? (countMap.count600 < 1 ? 'gray' : 'red'): 'green'">[{{countMap.count600 || 0}}]</span> 
             700<span style="font-size:13px;" :class="countMap.count700 < 5 ? (countMap.count700 < 1 ? 'gray' : 'red') : 'green'">[{{countMap.count700 || 0}}]</span> 
             800<span style="font-size:13px;" :class="countMap.count800 < 5 ? (countMap.count800 < 1 ? 'gray' : 'red') : 'green'">[{{countMap.count800 || 0}}]</span> 
             900<span style="font-size:13px;" :class="countMap.count900 < 5 ? (countMap.count900 < 1 ? 'gray' : 'red') : 'green'">[{{countMap.count900 || 0}}]</span> 
            1000<span style="font-size:13px;" :class="countMap.count1000 < 5 ? (countMap.count1000 < 1 ? 'gray' : 'red') : 'green'">[{{countMap.count1000 || 0}}]</span> 
          </div>  
        </div>
      </div> 
    </div>
   <div class="flex flex-m mb20 searchBox">

  <div class="flex flex-m mr20">
        <span class="w100">三方通道:</span>
        <el-select
          clearable
          class="w200"
          size="medium"
          v-model="search_data.channelCode"
          placeholder="请选择"
        >
          <el-option label="请选择三方代收通道" value></el-option>
          <el-option
            v-for="item in queryCoinChannelInfoList"
            :key="item.channelCode"
            :label="item.channelName"
            :value="item.channelCode"
          ></el-option>
        </el-select>
 </div> 

  <!-- <div class="flex flex-m mr10">
          <span class="w100">码商操作员:</span>
          <el-select
            clearable
            class="w150"
            size="medium"
            v-model="search_data.payerOperatorId"
            placeholder="请选择" 
          >
            <el-option label="请选择码商操作员" value></el-option>
            <el-option
              v-for="item in payerOperatorList"
              :key="item.merchantId"
              :label="item.merchantName + ' (' + item.merchantId + ')'"
              :value="item.merchantId"
            ></el-option>
          </el-select>
   </div>  -->

    <!-- <div class="flex flex-m mr20">
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
    </div> -->
     <div class="flex flex-m mr20">
        <span class="w100">收款人:</span>
        <el-input
          size="medium" 
          v-model="search_data.payerName"
          clearable
          placeholder="收款人"
          class="w150"
        ></el-input>
      </div>
          <!-- <div class="flex flex-m mr20">
        <span class="w100">收款人编号:</span>
        <el-input
          size="medium"
          class="w150"
          v-model="search_data.payerIdentity"
          clearable
          placeholder="收款人编号"
        ></el-input>
   </div> -->


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
   <div class="flex flex-m mb20 searchBox"> 
       <el-button 
        icon="el-icon-circle-plus"
        size="small"
        type="success"
        @click="mer_addForm()"
        >新增商家收款码</el-button>
        
        <el-button 
        @click="batchModifyStatus(2)"
        icon="el-icon-circle-circle"
        size="small"
        type="warning"
        >启用</el-button> 
      <el-button 
        @click="batchModifyStatus(0)"
        icon="el-icon-circle-close"
        size="small"
        type="danger"
        >禁用</el-button>

         <el-button 
        @click="batchDelStatus(0)" 
        icon="el-icon-circle-close"
        size="small"
        type="danger"
        >批量删除</el-button>
          <div>&nbsp;&nbsp;<span style="color:#ff0000;font-size:14px;">码商当前可用上码额度为:{{agentAccountBalance}}元<span style="color:#ff0000;font-size:12px;">（每成功收款一笔则扣除对应成功金额的额度。若上码额度为0后，将无法上码，已上的码也不再参与收款。）</span></span></div>
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
      :highlight-current-row="true">

       <el-table-column type="selection" width="45"></el-table-column>
           <el-table-column label="序号" type="index" width="50" align="center">
        <template slot-scope="scope">
          <span>{{ (m_page.page - 1) * m_page.rows + scope.$index + 1 }}</span>
        </template>
      </el-table-column>

     <el-table-column align="center" prop="" label="操作员" >
        <template slot-scope="scope"> 
          {{ scope.row.operatorName}} 
        </template>
      </el-table-column>  
       <!-- <el-table-column align="center" prop="" label="操作员编号" width="180">
        <template slot-scope="scope">  
          {{ scope.row.payerOperatorId}} 
        </template>
      </el-table-column>  -->

      <!-- <el-table-column align="center" prop="coinType" label="收款码类别">
         <template slot-scope="scope">
          {{ scope.row.coinType | coinTypeFilter}}
        </template>
     </el-table-column> -->
      <el-table-column align="center" prop="" width="150" label="三方通道">
        <template slot-scope="scope">
          {{ scope.row.channelName }}
          <!-- <br /> {{ scope.row.channelCode }} -->
        </template>
      </el-table-column> 

      <el-table-column align="center" prop label="收款人" width="190">
         <template slot-scope="scope">
             {{scope.row.payerName}} 
              <!-- {{scope.row.payerIdentity}} -->
              <el-button v-clipboard:copy="scope.row.payerName" @click.native="show_copy(scope.row)" type="text"><span style="font-style:italic;font-size:13px;">复制</span></el-button> 
         </template>
      </el-table-column>

      <el-table-column align="center" prop label="收款码ID" width="168">
        <template slot-scope="scope">
            <!-- <img class="authurl" :src="'/uploads/' + scope.row.payerAddr  + '.png'" style="width:50px;" alt /><br>
            {{scope.row.payerAddr}}<br> -->
             <el-button @click.native="show_receipt(scope.row)" type="text">查看二维码</el-button> 
              <br>{{scope.row.payerAddr}}
        </template>
      </el-table-column> 

    


     <el-table-column  align="center" prop="payerCodePrice" label="金额">
        <template slot-scope="scope">
          <span>{{scope.row.payerCodePrice}}</span>
        </template>
      </el-table-column>
     <!-- <el-table-column align="center" prop="successTimes" label="当天成功"></el-table-column> -->
      <el-table-column align="center" prop="payerCodeShowTimes" label="拉单次数"></el-table-column>
  <el-table-column align="center" prop="remark" label="备注"></el-table-column>

     <el-table-column align="center" label="登录" width="70" fixed="right">
        <template slot-scope="scope">
          <span :class="scope.row.isBind | isBindStatusClass">{{
            scope.row.isBind | isBindStatusFilter
          }}</span>
        </template>
      </el-table-column>

      <el-table-column align="center" label="状态" width="70" fixed="right">
        <template slot-scope="scope">
          <span :class="scope.row.status | payerCardStatusClass">{{
            scope.row.status | payerCardStatusFilter
          }}</span>
        </template>
      </el-table-column>

  <el-table-column align="center" label="操作" fixed="right" width="151">
        <template slot-scope="scope">
          <div>
             <el-button   style="color: #ff0000" @click.native="handelDel(scope.row)" type="text" >删除</el-button  >
             | <el-button 
            
              @click.native="m_editForm(scope.row)"
              type="text"
              > 编辑</el-button
            > |  
            <el-button  @click.native="handel_modifyStatus(scope.row)" type="text">
              <span :class="scope.row.status | payerStatusChangeClass">{{scope.row.status | payerStatusChangeFilter}}</span
              >
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
      :title="(m_isadd ? '添加商家收款码' : '编辑商家收款码') + ''"
      :visible.sync="m_show"
      width="500px"
      style="margin-top: -100px"
    >
      <!-- 通道/银行 -->
      <el-form
        :model="formData"
        :rules="m_isadd ? m_rules_add : m_rules_edit"
        ref="ruleForm"
      > 
       <el-form-item
          label="三方通道"
          label-width="100px"
          prop="channelCode"
        >
          <el-select 
            clearable
            v-model="formData.channelCode"
            placeholder="请选择收款码所属的通道"
          >
            <el-option
              v-for="item in queryCoinChannelInfoList"
              :key="item.channelCode"
              :label="item.channelName"
              :value="item.channelCode"
            ></el-option>
          </el-select>
        </el-form-item> 

     <!-- <el-form-item label="码商操作员" label-width="100px" prop="payerOperatorId">
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
      </el-form-item>   -->

       <el-form-item  label="支付宝商家" label-width="100px" prop="channelCode">
          <el-select clearable v-model="formData.alipayInfoId"  placeholder="请选择所属支付宝商家">
            <el-option
              v-for="item in coinAlipayInfoList"
              :key="item.id"
                :label="item.alipayName + ' ('+item.alipayUid+')'"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item> 

      <!-- 收款人 -->
        <!-- <el-form-item label="选择收款人" label-width="100px" prop="payerIdentity">
          <el-select
            clearable
            @change="onSelectChangeFollower($event)"
            v-model="formData.payerIdentity"
            placeholder="请选择收款人"
          >
            <el-option
              v-for="item in payerFollowerList"
              :key="item.merchantId"
              :label="item.merchantId + ' ('+ item.merchantName + ')'"
              :value="item.merchantId"
            ></el-option>
          </el-select>
        </el-form-item>    -->
        
      <!-- 持卡人 -->
        <el-form-item
          class="flex-1"
          label="收款人名称"
          label-width="100px"
          clearable
          prop="payerName"
        >
        <el-input
            placeholder="收款人名称"
            v-model="formData.payerName"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>

<!-- 是否UID -->
       <!-- <el-form-item
            class="flex-1"
            label="是否UID"
            label-width="100px"
            clearable
            prop="isUid"
          >
            <el-select
              class="w140"
              size="medium"
              v-model="formData.isUid"
              placeholder="请选择"
            >
              <el-option label="请选择是否UID" value></el-option>
              <el-option
                v-for="item in isUidType"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              ></el-option>
            </el-select>
       </el-form-item> -->

        <!-- uid -->  
        <!-- <div  v-if="formData.isUid == '1'"  class="flex flex-m">
           <el-form-item  class="flex-1"  label="收款人UID" label-width="100px"> 
            <el-input style="width:220px;" placeholder="收款人支付宝UID" v-model="formData.uid" clearable  auto-complete="off"></el-input>
            <el-button class="btn" @click="handleGenerateQrcode">生成二维码</el-button>
          </el-form-item> 
        </div> -->

        <!-- 收款码地址 -->  
         <div class="flex flex-m"> 
            <el-form-item  class="flex-1" label="收款码ID" label-width="100px"  clearable readonly="true"  prop="payerAddr">
              <el-input
                style="width:260px;"
                :disabled="!m_isadd"
                placeholder="收款码ID/代付订单编号"
                v-model="formData.payerAddr"
                clearable
                type="text"
                auto-complete="off"
                readonly
              ></el-input><el-button class="btn" @click="toggleShow" v-if="formData.isUid == '0'">上传</el-button>
                <my-upload 
                      field="codeImage" 
                      :params="params"
                      @src-file-set="srcFileSet"
                      @crop-success="cropSuccess"
                      @crop-upload-success="cropUploadSuccess"
                      @crop-upload-fail="cropUploadFail"
                      :modelValue.sync="image_show" 
                      langType="zh"
                      :width="300"
                      :height="300"
                      url="/api/coin/uploadImage" 
                      img-format="png"
                      :noCircle="noCircle"
                      :headers="headers"
                      ref="myUpload"
                      ></my-upload> 
            </el-form-item> 
         </div>
 
           <!-- 二维码 -->  
         <div class="flex flex-m">
           <el-form-item  class="flex-1"  label="收款二维码" label-width="100px"> 
             <img class="authurl" :src="authurl" alt />
          </el-form-item> 
        </div>

        <div class="flex flex-m">
           <el-form-item class="flex-1" label="固定金额" label-width="100px"  clearable  prop="payerCodePrice">
            <el-select clearable class="w120" size="medium" v-model="formData.payerCodePrice" placeholder="金额"> 
              <el-option
                v-for="item in payerCodePriceList"
                :key="item.code"
                :label="item.code"
                :value="item.code"
              ></el-option>
                </el-select> 
          </el-form-item> 
             <!-- <el-form-item
            class="flex-1"
            label="码类别"
            label-width="100px"
            clearable
            prop="coinType"
          >
            <el-select
              class="w120"
              size="medium"
              v-model="formData.coinType"
              placeholder="请选择"
            >
              <el-option label="请选择码类别" value></el-option>
              <el-option
                v-for="item in coinTypeList"
                :key="item.value"
                :label="item.name"
                :value="item.id"
              ></el-option>
            </el-select>
          </el-form-item>
-->
          <el-form-item
            class="flex-1"
            label="收款码状态"
            label-width="100px"
            clearable
            prop="status"
          >
            <el-select
              class="w140"
              size="medium"
              v-model="formData.status"
              placeholder="请选择"
            >
              <el-option label="请选择收款码状态" value></el-option>
              <el-option
                v-for="item in coinStatusType"
                :key="item.value"
                :label="item.name"
                :value="item.id"
              ></el-option>
            </el-select>
          </el-form-item> 
        </div> 

          <div class="flex flex-m"> 
          <el-form-item class="flex-1" label="可收款次数" label-width="100px"  clearable  prop="payerCodeTimes">
             <el-input readonly placeholder="可收款次数" type="number" v-model="formData.payerCodeTimes"  clearable  auto-complete="off" ></el-input>
          </el-form-item> 
           <el-form-item class="flex-1" label="可拉单次数" label-width="100px"  clearable  prop="payerCodeShowTimes">
             <el-input  placeholder="可被拉单次数" type="number" v-model="formData.payerCodeShowTimes"  clearable  auto-complete="off" ></el-input>
          </el-form-item> 
        </div>  

       <!-- 设备 -->
         <!-- <div class="flex flex-m">
          <el-form-item class="flex-1" label="设备ID" label-width="100px" clearable prop="deviceId">
            <el-input placeholder="请输入" v-model="formData.deviceId" clearable auto-complete="off"></el-input>
           </el-form-item>
         </div>   -->
        <!-- REMARK -->
        <div class="flex flex-m">
 
          <el-form-item
            class="flex-1"
            label="备注"
            label-width="100px"
            clearable
            prop="remark"
          >
            <el-input
              placeholder="备注"
              v-model="formData.remark"
              clearable
              auto-complete="off"
            ></el-input>
          </el-form-item>
        </div>

      <!-- <el-form-item label="管理员谷歌" label-width="100px" prop="googleCode">
            <el-input
              type="number"
              placeholder="输入6位谷歌Google验证码"
              v-model="formData.googleCode"
              auto-complete="off"
              clearable
              @mousewheel.native.prevent
            ></el-input>
       </el-form-item>   -->

       <!-- 邮箱二次验证:START -->
          <!-- <el-form-item label="邮箱验证码" label-width="100px" prop="emailCode">
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
              @click="sendCode('wallet')"
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
      <div slot="footer" class="dialog-footer flex" style="margin-top: -50px">
        <div class="flex-2" style="font-size:14px;"></div>
        <div class="flex-1"></div>
         <div class="flex-3"> <!-- 取 消 -->
        <el-button @click="m_show = false">取 消</el-button>
        <!-- 确 定 -->
        <el-button
          type="primary"
          :loading="m_loading"
          @click.native="sureOption"
          >确 定</el-button></div>
       
      </div>
    </el-dialog>

    <!-- 收款钱包补单  -->
    <el-dialog
      v-if="balance_show"
      title="收款钱包补单"
      :visible.sync="balance_show"
      style="margin-top: -100px"
    >
      <div v-loading="d_balance_loading">
        <el-form
          :model="balance_data"
          :rules="balanceForm_rules"
          ref="balanceForm"
        >
          <div class="flex flex-m searchBox_bankcard">
            <el-form-item class="flex-1" label="通道名称/编号" label-width="120px">
              <span>
                {{ balance_data.channelName }} / {{ balance_data.channelCode }}</span>
            </el-form-item> 
          </div>

          <div class="flex flex-m searchBox_bankcard">
            <el-form-item
              class="flex-1"
              label="通道充值费率"
              label-width="120px"
            >
              <span>{{ balance_data.channelRate }} %</span>
            </el-form-item>

            <el-form-item
              class="flex-1"
              label="代收单笔手续费"
              label-width="120px"
            >
              <span>{{ balance_data.channelSingle }} 元/笔</span>
            </el-form-item>
          </div>

          <div class="flex flex-m searchBox_bankcard">
            <el-form-item class="flex-1" label="收款人" label-width="120px">
              <span class>{{ balance_data.payerName }}</span>
            </el-form-item>

            <el-form-item class="flex-1" label="收款码余额" label-width="120px">
              <span class="red">{{ balance_data.payerAmt }}</span>
            </el-form-item>
          </div>

          <div class="flex flex-m">
            <el-form-item class="flex-1" label="收款码" label-width="120px">
              <span >{{ balance_data.payerAddr }}</span>
            </el-form-item>
          </div>

          <el-form-item label="补充金额" label-width="120px" prop="rechargeAmt">
            <div class="flex flex-m">
              <el-input
                @mousewheel.native.prevent
                type="number"
                style="width: 110px"
                placeholder="补充金额"
                v-model="balance_data.rechargeAmt"
                auto-complete="off"
                clearable
              ></el-input>
              <div class="red ml15 fs18 w200">
                {{ balance_data.rechargeAmt + " 元"}}
              </div>
            </div>
          </el-form-item>

          <!-- <el-form-item
            label="google验证码"
            label-width="120px"
            prop="googleCode"
          >
            <el-input
              placeholder="请输入"
              v-model="balance_data.googleCode"
              auto-complete="off"
            ></el-input>
          </el-form-item> -->

          <el-form-item label-width="120px" label="备注">
            <el-input
              style
              type="textarea"
              v-model="balance_data.remark"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 取 消 -->
        <el-button @click="balance_show = false">取 消</el-button>
        <!-- 确 定 -->
        <el-button
          type="primary"
          :loading="balance_loading"
          @click.native="balanceSubmit"
          >确 定</el-button
        >
      </div>
    </el-dialog>

    <!-- GOOGLE密钥删除收款人  -->
    <el-dialog
      v-if="merchant_show_del"
      title="删除收款人"
      :visible.sync="merchant_show_del"
      width="500px"
    >
      <div v-loading="d_balance_loading">
        <el-form
          :model="merchant_data"
          :rules="cardPayerForm_rules"
          ref="delCardPayerForm"
        >
          <el-form-item label="收款人" label-width="110px">
            <span>{{ merchant_data.payerName }}</span>
          </el-form-item>

          <el-form-item label="收款二维码ID" label-width="110px">
            <span>{{ merchant_data.payerAddr }}</span>
          </el-form-item>

          <!-- <el-form-item
            label="Google验证码"
            label-width="110px"
            prop="googleCode"
          >
            <el-input
              type="number"
               @mousewheel.native.prevent
              placeholder="输入6位谷歌Google验证码"
              v-model="merchant_data.googleCode"
              auto-complete="off"
            ></el-input>
          </el-form-item> -->
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 取 消 -->
        <el-button @click="merchant_show_del = false">关 闭</el-button>
        <!-- 确 定 -->
        <el-button
          type="primary"
          :loading="d_balance_loading"
          @click.native="delData(merchant_data)"
          >确 定</el-button
        >
      </div>
    </el-dialog>

    <!-- qrcode  -->
    <el-dialog
      v-if="qrcode_show"
      title
      :visible.sync="qrcode_show"
      width="400px"
      style="margin-top: 50px"
    >
      <div v-loading="d_balance_loading">
        <div align="center">{{merchant_data.payerName}}</div>
        <br>
        <img :src="this.receiptImage" width="350" />
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="qrcode_show = false">关闭</el-button>
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
import * as api from "@/api/public";
import 'babel-polyfill';
import assert from 'assert'
import { isValidChecksumAddress, unpadBuffer, BN } from 'ethereumjs-util'
import qrcode from "qrcode";
import moment from "moment";
import myUpload from 'vue-image-crop-upload';
//import Web3 from "web3"; 
//import TronWeb from "tronweb";

export default {
  name: "coinOperatorHolderList",//码商收款二维码管理
  components: {
			'my-upload': myUpload
		},
  data() {
    return {
      businessType:1,//个码
      payerCodePriceList:[
            {code:200,price:200},
            {code:300,price:300},
            {code:400,price:400},
            {code:500,price:500},
            {code:600,price:600},
            {code:700,price:700},
            {code:800,price:800},
            {code:900,price:900},
            {code:1000,price:1000},
      ],
      params:"",
      noCircle:true,
      headers:{
        "fileName":"",
        "businessType":1, //(因为图片名称一致 dev的时候 因本地图片更新 因此刷新一次)
         "Authorization":sessionStorage.token,
      },
      imgDataUrl:"",
      image_show:false,
      rpcURL: "",
      web3: {},
      ABI: {},
      contract: {},
      tronWeb: {},
      agentAccountBalance:0,
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
      coinAlipayInfoList:[],//支付宝商家信息
      payMerchantInfoList:[],//从商户列表中查出 码商(agentRate == '-1')列表
      payerOperatorList:[],//agentRate == '-2' 操作员
      payerFollowerList:[],//agentRate == '-3' 收款人
      isShowPayerMerchantId:0,//默认不显示卡主编号
      //新增表格
      formData: {
        isUid:"0",
        id:"",
        deviceId:"",
        merchantId:"",
        payerName:"",//收款人
        payerIdentity:"",//收款人编号 手机后8位数
        payerOperatorId:"",
        payerAddr:"",//钱包地址
        coinType: "1", //0-微信 1-支付宝 2-聚合码
        status: "0", //2-已分配 0-禁用
        remark: "",
        tableMerchantId: "",
        googleCode: "", 
        channelCode:"U1713775996149215734",//商家码 || "U1704455353723353150",个码
        emailCode:"",
        payerCodePrice:"200",
        payerCodeTimes:1,
        payerCodeShowTimes:5,
        payerLimitDay:150000,
        payerLimitMonth:150000 * 30,
        businessType:1,//商家码
        alipayInfoId:"",//商家码所属的支付宝商家ID
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
      qrcode_show:false,
      receiptImage:"",
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
        //payerOperatorId: [{ required: true, message: "请输入", trigger: "blur" }], //码商操作员
        payerCodePrice: [{ required: true, message: "请输入", trigger: "blur" }],
        //payerCodeTimes: [{ required: true, message: "请输入", trigger: "blur" }],
       // googleCode:[{ required: true, message: "请输入", trigger: "blur" }],
      },

      m_rules_edit: {
        status: [{ required: true, message: "请选择", trigger: "blur" }],
        payerAddr: [{ required: true, message: "请输入", trigger: "blur" }],
        //payerOperatorId: [{ required: true, message: "请输入", trigger: "blur" }],
        payerCodePrice: [{ required: true, message: "请输入", trigger: "blur" }],
        //payerCodeTimes: [{ required: true, message: "请输入", trigger: "blur" }],
       // googleCode:[{ required: true, message: "请输入", trigger: "blur" }],
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
     this.time = [moment().format("YYYY-MM-DD"), moment().format("YYYY-MM-DD")]; 
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

    onSelectChangeOperator(val) {
      if (val){
        var agentRate = "-3";//收款人
        this.queryPayerFollowerList(val,agentRate);
      }  
    },

     onSelectChangeFollower(val) {
      if (val){ 
           this.queryPayMerchantNameById(val);
      }  
    },

    toggleShow() {
      this.image_show = !this.image_show;
    },
    cropSuccess(imgDataUrl, field){
      console.log('-------- crop success --------'); 
    },
    srcFileSet(fileName,fileType,fileSize) {
       console.log('-------- srcFileSet --------'); 
       console.log(fileName,fileType,fileSize);
       this.headers.fileName = fileName;
    },
    cropUploadSuccess(jsonData, field){
      console.log('-------- upload success --------');
      if (jsonData.code == '0000') {
         this.m_success("收款码上传成功");
      }else {
         this.m_error("收款码上传失败 " + jsonData.message);
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
      this.m_error("上传失败:收款码含有中文名称或空格");
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
      this.getDatalist(1);
      // this.queryPayerOperatorList();//码商操作员 || 添加收款码
      // this.queryPayerFollowerList("","-3");//收款人 || 添加收款码
      this.queryCoinChannelDepositInfo();
      this.queryCoinAlipayInfoList();//支付宝商家
     
    },
    
    show_copy(row){
     this.m_success("[" + row.payerName + "]已复制");
    },

    //凭证
    show_receipt(row) {
      this.merchant_data.payerName = row.payerName;
      this.qrcode_show = true;
      this.receiptImage = '/uploads/' + row.payerAddr  + '.png';
      // this.receiptImage = "https://30pay.info/uploads/20240112104722905097b12af24698a67b7776ed861ef1.png";
    },

    async handleGenerateQrcode(){ 
      //uid 2088342941922323
      const that = this;
      if (this.formData.uid == "" || typeof  this.formData.uid === 'undefined') {
         this.m_error("Uid为空");
        return;
      }
      // console.log("formData.uid",this.formData.uid);
      if (!(this.formData.uid.indexOf("2088") > -1)) {
         this.m_error("Uid不正确");
        return;
      }
      let msgData = {
        msg: `是否生成收款金额为 ${this.formData.payerCodePrice}元的二维码 ？`
      };
      this.m_confirm(msgData).then(res => {
        if (res) {
           
           let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
            const data = {
              "uid":that.formData.uid,
              "payerCodePrice":that.formData.payerCodePrice,
              "tableMerchantId":userData.merchantId,
            }
            that.m_api.generateQrcodeByUid(data).then((res) => {
              console.log("uid res - ",res.code) 
              if (res.code == "9999") {
                  ththatis.m_error("UID二维码失败");
              }else {
                  that.authurl = process.env.VUE_APP_UPLOAD_PATH + res.data + '.png'; 
                  that.formData.payerAddr = res.data; //不包含路径 
                  console.log("payerAddr",that.formData.payerAddr);
                  that.m_success("收款金额为["+that.formData.payerCodePrice+"]元的UID二维码已生成");
              }  
            });  
        }
      });   
    },
    
     queryAgentAccountBalance() {
        let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.m_api.queryAgentBalance({
          type:'self',
          merchantId:userData.merchantId
      }).then((res) => {
        this.agentAccountBalance = res.data.accountBalance;
      });
    }, 
    //获取三方通道列表
    queryCoinChannelDepositInfo() {
      this.m_api.queryCoinChannelDepositInfo({}).then((res) => {
        this.queryCoinChannelInfoList = res.data;
      });
    }, 

    //支付宝商家信息
    queryCoinAlipayInfoList() {
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.m_api.queryCoinAlipayInfoList(
        {  
          "agentRate":"-2",//-1 码商 || -2 操作员
          "payerOperatorId":userData.merchantId,
          "tableMerchantId":userData.merchantId,
        }
        ).then((res) => {
        this.coinAlipayInfoList = res.data;
      });
    }, 

     queryPayMerchantNameById(v) {
      this.m_api.queryPayMerchantNameById({merchantId:v}).then((res) => {
         //this.formData.payerName = res.data.merchantName;
      });
    }, 
    //码商操作员
    queryPayerOperatorList() {
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.m_api.queryPayMerchantInfoList(
        {
          "agentRate":"-2",
          "parentId":userData.merchantId,
          "tableMerchantId":userData.merchantId,
        }
        ).then((res) => { 
        this.payerOperatorList = res.data; 
      });
    }, 
    //收款人
    queryPayerFollowerList(v,agentRate) {
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      if (v == "") { v = userData.merchantId;}
      this.m_api.queryPayMerchantInfoList(
        {
          "agentRate":agentRate,
          "operatorId":v,
          "tableMerchantId":userData.merchantId,
        }
        ).then((res) => { 
        this.payerFollowerList = res.data; 
      });
    }, 


    //获取银行卡数据信息
    getDatalist(page) {
      this.queryAgentAccountBalance(); //查找码商余额
      //console.log("查询" + JSON.stringify(this.search_data))
      const that = this;
      this.m_tableLoading = true;
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.search_data.beginTime = this.time[0] || "";
      this.search_data.endTime = this.time[1] || "";
      this.search_data.tableMerchantId = userData.merchantId
      this.search_data.payerOperatorId = userData.merchantId;
      this.search_data.merchantId = userData.merchantId;
      this.search_data.businessType = that.businessType;
     
      if (page == 1) {
        this.m_page.page = page;
      }
      
      let data = Object.assign(this.search_data, this.m_page);
      this.m_api
        .queryCoinHolderInfoPage(data)
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
        //总挂码情况
        this.getCoinHolderCountMap(this.search_data);

       //码商总数
      //  api
      //   .queryMerchantQueue(data)
      //   .then((res) => { 
      //     this.countMap.masangTotal = res.data.countTotal;  
      //     this.countMap.sumAccountBalance = res.data.sumAccountBalance;  
      //   })
      //   .finally(() => {
      //     this.m_tableLoading = false;
      //     this.loading = false;
      //   });

    },

    //提交服务器 加入数据
    addData() {
      //console.log("eth addr - " + isValidChecksumAddress(this.formData.payerAddr));
      if (this.formData.payerAddr == ""){
         this.m_error("请上传收款二维码");
         return;
      }
      
      //按钮只可点击一次
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.formData.tableMerchantId = userData.merchantId; 
      this.formData.payerOperatorId = userData.merchantId;
      this.m_loading = true;
      this.m_api
        .registerOperatorCoinHolder(this.formData)
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
      // this.payerFollowerList = [];
      // this.payerFollowerList = this.queryPayerFollowerList();
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
        .modifyOperatorCoinHolder(form)
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
        payerMerchantId:row.payerMerchantId,
        payerOperatorId:row.payerOperatorId,
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
 

    //总挂码情况
    getCoinHolderCountMap(data) {
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.search_data.tableMerchantId = userData.merchantId;
      this.search_data.businessType =  this.businessType;
      this.m_api.getCoinHolderCountMap(this.search_data).then((res) => {
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

    batchDelStatus(status) {
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
      var txt = "批量删除";
   
      let formObj = {
        status,
        payerAddrs: ids, //二维码链接地址列表
        //merchantId:userData.merchantId,
        tableMerchantId: userData.merchantId,
      };

      this.m_confirm({ msg: `是否确认 ${txt} 收款码` }).then((res) => {
        if (res) {
          this.m_api.batchDelPayerInfoStatus(formObj).then((res) => {
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
.vue-image-crop-upload .vicp-wrap .vicp-step2 .vicp-crop .vicp-crop-right .vicp-preview{
  overflow: visible;
}
.authurl {
  max-width: 300px;
}
 
</style>