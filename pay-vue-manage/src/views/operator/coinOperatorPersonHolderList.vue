<!-- 操作员:收款人钱包地址  coinOperatorPersonHolderList
 -->
<template>
  <div>
    <div class="flex flex-m tc topbox">
      <div class="flex-1 bor">
        <div>总个码数量<span style="font-size:12px;"></span></div>
        <div class="num">{{ countMap.countCoinHolder || 0 }}</div>
      </div> 
       <div class="flex-1 bor">
          <div>总挂码数量<span style="font-size:12px;"></span></div>
          <div class="num">{{ countMap.countCoinHolderAvailable || 0 }}</div>
        </div> 
      <div class="flex-2">
        <div>个码挂码情况<span style="font-size:12px;"></span></div>
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
        >新增个码</el-button>
        
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

      <el-table-column align="center" prop label="收款人" width="128">
         <template slot-scope="scope">
             {{scope.row.payerName}} 
              <!-- {{scope.row.payerIdentity}} -->
              <el-button v-clipboard:copy="scope.row.payerName" @click.native="show_copy(scope.row)" type="text"><span style="font-style:italic;font-size:13px;">复制</span></el-button> 
         </template>
      </el-table-column>

      <el-table-column align="center" prop label="收款码ID" width="168">
        <template slot-scope="scope">
            <!-- <img class="authurl" :src="'/uploads/' + scope.row.payerAddr  + '.png'" style="width:50px;" alt /><br>
            -->
             <el-button @click.native="show_receipt(scope.row)" type="text">查看二维码</el-button> <br>
              {{scope.row.payerAddr}}
        </template>
      </el-table-column> 
    <el-table-column  align="center" prop label="金额范围">
        <template slot-scope="scope">
          <span>{{scope.row.payerCodeMinPrice + " ~ " + scope.row.payerCodeMaxPrice}}</span>
        </template>
      </el-table-column> 

    <el-table-column align="center" prop="payerCodeTimes" label="剩余收款次数"></el-table-column> 
       <el-table-column align="center" prop="payerCodeShowTimes" label="剩余拉单次数"></el-table-column>
  <el-table-column align="center" prop="remark" label="备注"></el-table-column>

       <el-table-column align="center" label="是否登录" width="90" fixed="right">
        <template slot-scope="scope">
          <span :class="scope.row.isBind | isBindStatusClass">{{
            scope.row.isBind | isBindStatusFilter
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
            <el-button  
            :disabled = "scope.row.isBind == '1' ? true : false" 
            @click.native="handel_bindnlogin(scope.row)"
            type="primary">
            <span>支付宝登录</span>
          </el-button>
          </div>
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
      :title="(m_isadd ? '添加收款码' : '编辑收款码') + ''"
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
                placeholder="收款码ID"
                v-model="formData.payerAddr"
                clearable
                type="text"
                auto-complete="off"
                readonly
              ></el-input><el-button class="btn" @click="toggleShow" v-if="formData.isUid == '0'">上传</el-button>
                <my-upload  
                      field="codeImage"
                      :params="params" 
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
                      @drop="handleChange"
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
          </el-form-item> -->

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
            <el-form-item class="flex-1" label="起始金额" label-width="100px"  clearable  prop="payerCodeMinPrice">
                <el-input  placeholder="起始金额" type="number" v-model="formData.payerCodeMinPrice"  clearable  auto-complete="off" ></el-input>
           </el-form-item>
            <el-form-item class="flex-1" label="结束金额" label-width="100px"  clearable  prop="payerCodeMaxPrice">
                <el-input  placeholder="结束金额" type="number" v-model="formData.payerCodeMaxPrice"  clearable  auto-complete="off" ></el-input>
           </el-form-item> 
        </div>  
      <div class="flex flex-m"> 
          <el-form-item class="flex-1" label="可收款次数" label-width="100px"  clearable  prop="payerCodeTimes">
             <el-input  placeholder="可收款次数" type="number" v-model="formData.payerCodeTimes"  clearable  auto-complete="off" ></el-input>
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

     <!-- 支付宝/个人商家登录 -->
     <el-dialog
      v-if="alipay_login_show"
      :title="alipayTitle"
      :visible.sync="alipay_login_show" 
      style="margin-top: -50px;">
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
import QRCode from "qrcode";
import moment from "moment";
import myUpload from 'vue-image-crop-upload';
//import Web3 from "web3"; 
//import TronWeb from "tronweb";

export default {
  name: "coinOperatorPersonHolderList",//码商收款二维码管理
  components: {
			'my-upload': myUpload
		},
  data() {
    return {
      businessType:0,//个码
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
         "businessType":0, //(因为图片名称一致 dev的时候 因本地图片更新 因此刷新一次)
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
      businessType:0,//绑定个码
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
      alipay_login_show: false,

      queryCoinChannelInfoList: [], //三方通道数组
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
        channelCode:"U1704455353723353150",//三方码通道
        emailCode:"",
        payerCodePrice:"200",
        payerCodeMinPrice:"200",
        payerCodeMaxPrice:"200",
        payerCodeTimes:1,//可成功收款次数
        payerCodeShowTimes:10,//可被拉单次数
        payerLimitDay:150000,
        payerLimitMonth:150000 * 30,
        businessType:0,//个码
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
      isOpenDialog:false,
      isOpenSelectedDialog:false,//导入
      isOpenUploadDialog:false,//上传
      isOpenBatchPrice:false,//批量更新金额
      uploading:false,
      uploadingImage:false,
      uploadingModal:false,
      uploadingTxt:"正在登录...",
      loading:false,
      that: this,
      batchPrice:300,
      alipayTitle:"",
      qrcode_loading: false,
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
     clearInterval(this.intervalMonitor);
     clearInterval(this.intervalTime); 
     clearInterval(this.intervalBarcode);
  },
  destroyed() {
    // 销毁enter事件
    this.enterKeyupDestroyed();
    clearInterval(this.intervalMonitor);
     clearInterval(this.intervalTime); 
     clearInterval(this.intervalBarcode); 
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
         this.m_error("收款码上传失败 " + jsonData.code);
      }
     
      console.log(jsonData);
      console.log('field: ' + field);//codeImage
      this.authurl = '/uploads/' + jsonData.data + '.png';
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
      this.getDatalist(1);
      // this.queryPayerOperatorList();//码商操作员 || 添加收款码
      // this.queryPayerFollowerList("","-3");//收款人 || 添加收款码
      this.queryCoinChannelDepositInfo();
     
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
           msg: `退出后 ${row.payerName} 的收款码无法再自动回调,是否确定？`
      };
      this.m_confirm(txtData).then(async res => {
        if (res) { 
          await api.unbindCoinHolder(data);
          this.m_success("操作成功"); 
          this.getDatalist();
        }else {

        }
      }); 
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
      console.log("page -",page);
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
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.formData.tableMerchantId = userData.merchantId;
      this.formData.merchantId = userData.merchantId;
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


   async handel_bindnlogin(row){
      const that = this;
       this.show_mask = false;
       this.show_scanned = false;
       this.show_confirmed = false;
       this.merchant_data.id = row.id;
       this.merchant_data.payerName = row.payerName
       this.alipayTitle = "支付宝【"+ row.payerName +"】扫码登录"; 
       this.alipay_login_show = true; //要先显示
       this.uploadingModal = false;
       this.qrcode_loading = true;//alipay载入登录二维码
       setTimeout(function(){
           that.generateQRCodeWithLogo(row.payerAddr);//商家ID || 个人码时需传递 payerAddr
       },200)
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
        console.error('Error generating QR code with logo:', this.error);
        this.qrcode_loading = false;
        this.m_error("generating QR code with logo:" + this.error);
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
          // this.error = 'Failed to get QR code';
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
      that.monitorTokenFromJavaServer(id); 
      that.monitorBarcodeStatusFromJavaServer(id); 
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
        let data = "id=" + id + "&businessType=" + that.businessType; //this读取不到 用THAT
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
              that.m_error("登录失败:" + data.message);
               that.alipay_login_show = false; 
               that.uploadingModal = false;
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
              } else if (data.data.barcodeStatus == "confirmed"){
                 that.show_mask = true;
                 that.show_scanned = false;
                 that.show_confirmed = true;
                 that.uploadingModal = true;
              }
              // that.alipay_login_show = false; 
              // that.m_success("登录成功");
              // that.getDatalist();//重新查询一次
            }else if (data.code == "1000"){
              // that.alipay_login_show = false; 
              // that.m_error("登录失败:" + data.message);
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
  .authurl {
  max-width: 300px;
}
 
 
</style>