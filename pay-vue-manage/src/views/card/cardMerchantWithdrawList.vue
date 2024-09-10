<!-- cardMerchantWithdrawList 三方代付订单列表 -->

<template>
  <div>
    <div class="flex flex-m tc topbox">
      <div class="flex-1 bor">
        <div>总交易金额</div>
        <div class="num">{{ count.sumCardPayerWithdraw || 0.0 }}</div>
      </div>

      <div class="flex-1 bor">
        <div>总交易笔数</div>
        <div class="num">{{ count.countCardPayerWithdraw || 0 }}</div>
      </div>

      <div class="flex-1 bor">
        <div>总手续费</div>
        <div class="num">
          {{
            parseFloat(
              parseFloat(count.sumCardPayerWithdrawRateFee) +
                parseFloat(count.sumCardPayerWithdrawFee)
            ).toFixed(2) || 0.0
          }}
        </div>
      </div>

      <div class="flex-1 bor">
        <div>总下发手续费</div>
        <div class="num">{{ count.sumCardPayerWithdrawRateFee || 0.0 }}</div>
      </div>

      <div class="flex-1">
        <div>总单笔手续费</div>
        <div class="num">{{ count.sumCardPayerWithdrawFee || 0.0 }}</div>
      </div>
    </div>

    <!-- * 
         * @搜索栏
         *
    --> 

    <div class="flex flex-m mb20 searchBox">
      <div class="flex flex-m mr20">
        <span class="w100">商户订单号:</span>
        <el-input
          size="medium"
          class="w200"
          v-model="search_data.merchantWithdrawNo"
          clearable
          placeholder="商户订单号"
        ></el-input>
      </div>

      <div class="flex flex-m mr20">
        <span class="w100">平台订单号:</span>
        <el-input
          size="medium"
          class="w200"
          v-model="search_data.withdrawNo"
          clearable
          placeholder="平台订单号"
        ></el-input>
      </div>

      <div class="flex flex-m mr20">
        <span class="w109">银行卡流水号:</span>
        <el-input
          size="medium"
          class="w200"
          v-model="search_data.cardWithdrawNo"
          clearable
          placeholder="银行卡流水号"
        ></el-input>
      </div>
      <div class="flex flex-m mr20 echartOff">
        <el-button
          v-if="handelAuto('edit')"
          class="mr20"
          size="small"
          icon="el-icon-document"
          @click="handel_upload_excel"
          type="normal"
          >导出</el-button
        >
      </div>
    </div>


    <div class="flex flex-m mb20 searchBox">
     

      <div class="flex flex-m mr10">
        <span class="w60">持卡人:</span>
        <el-input
          size="medium"
          class="w120"
          v-model="search_data.payerName"
          clearable
          placeholder="持卡人"
        ></el-input>
      </div>

      <div class="flex flex-m mr20">
        <span class="w60">卡号:</span>
        <el-input
          size="medium"
          class="w200"
          v-model="search_data.payerCardNo"
          clearable
          placeholder="卡号"
        ></el-input>
      </div>
      <div class="flex flex-m mr20">
        <span class="w60">收款人:</span>
        <el-input
          size="medium"
          class="w120"
          v-model="search_data.receiverName"
          clearable
          placeholder="收款人"
        ></el-input>
      </div>
      <div class="flex flex-m mr20">
        <span class="w110">收款人卡号:</span>
        <el-input
          size="medium"
          class="w200"
          v-model="search_data.receiverCardNo"
          clearable
          placeholder="收款人卡号"
          style="margin-left: -15px"
        ></el-input>
      </div>
    </div>

<div class="flex flex-m mb20 searchBox">

   <div class="flex flex-m mr20">
        <span class="w80">银行名称:</span>
        <el-select
          class="w150"
          size="medium"
          v-model="search_data.payerBankCode"
          placeholder="请选择银行"
        >
          <el-option label="请选择银行" value></el-option>
          <el-option
            v-for="item in bankCodeDataList"
            :key="item.bankCode"
            :label="item.bankName + '(' + bankTypeFilter(item.bankType) + ')'"
            :value="item.bankCode"
          ></el-option>
        </el-select>
   </div>

       <div class="flex flex-m mr20">
       
        <span class="w80">所属卡主:</span>
        <el-select
          class="w180"
          size="medium"
          v-model="search_data.payerMerchantId"
          placeholder="请选择卡主"
        >
          <el-option label="请选择卡主" value></el-option>
          <el-option
            v-for="item in queryMerchantIdList"
            :key="item.merchantId"
            :label="item.merchantId + '('+ item.merchantName+')'"
            :value="item.merchantId"
          ></el-option>
        </el-select>
    
       </div>

    <div class="flex flex-m mr20">
        <span class="w80" style="margin-left: 0px">三方通道:</span>
        <el-select
          class="w180"
          size="medium"
          v-model="search_data.cardChannelCode"
          placeholder="请选择三方通道"
        >
          <el-option label="请选择三方通道" value></el-option>
          <el-option
            v-for="item in queryCardChannelInfoList"
            :key="item.channelName"
            :label="item.channelName"
            :value="item.cardChannelCode"
          ></el-option>
        </el-select>
    </div> 
</div>

 <div class="flex flex-m mb20 searchBox">
      <div class="mr15">
        <span class="w100 mr20">日期:</span>
        <el-date-picker
          v-model="time"
          type="daterange"
          align="right"
          unlink-panels
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          style="margin-left: -10px"
          class="w250"
        ></el-date-picker>
      </div>

   <div class="flex flex-m mr20">
        <span class="w80 ">订单类别:</span>
        <el-select
          class="w120"
          size="medium"
          v-model="search_data.payerBankType"
          placeholder="请选择类别"
          disabled
        >
          <el-option label="请选择类别" value></el-option>
          <el-option
            v-for="item in payerBankTypeStatus"
            :key="item.value"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
   </div>


      <div class="flex flex-m mr20">
        <span class="w80">订单状态:</span>
        <el-select
          class="w120"
          size="medium"
          v-model="search_data.withdrawStatus"
          placeholder="请选择状态"
        >
          <el-option label="请选择状态" value></el-option>
          <el-option
            v-for="item in payerWithdrawStatusList"
            :key="item.value"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
      </div>

      <div class="flex flex-m mr20">
        <span class="w80">备 注:</span>
        <el-input
          size="medium"
          class="w120"
          v-model="search_data.remark"
          clearable
          placeholder="备注"
        ></el-input>
      </div>

      <div style="margin-left: 1px" class="flex flex-m mr20">
        <el-button
          class
          size="small"
          icon="el-icon-search"
          :loading="loadingStatus"
          @click.native="m_search"
          type="primary"
          >搜索 (ENTER回车键或ESC键)</el-button
        >
      </div>
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
      <el-table-column align="center" label="交易时间" width="90">
        <template slot-scope="scope">
          <span>{{ scope.row.createTime | time }}</span>
        </template>
      </el-table-column>
      <!-- <el-table-column align="center" label="任务开始" width="90">
        <template slot-scope="scope">
          <span>{{scope.row.taskStartTime | time }}</span>
        </template>
      </el-table-column>-->
      <el-table-column
        align="center"
        prop="merchantWithdrawNo"
        label="商户订单号"
        width="115"
      ></el-table-column>
      <el-table-column
        align="center"
        prop="withdrawNo"
        label="平台订单号"
        width="115"
      ></el-table-column>
      <!-- <el-table-column
        align="center"
        prop="cardWithdrawNo"
        label="银行卡流水号"
        width="129"
      ></el-table-column> -->
      <el-table-column align="center" prop label="三方通道" width="115">
        <template slot-scope="scope">
          <span>
            {{ scope.row.cardChannelName }}
            <br />
            {{ scope.row.cardChannelCode }}
          </span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop label="所属卡主" width="100">
        <template slot-scope="scope">
          <span v-if="scope.row.payerBankType == '-1'">
            {{ scope.row.merchantName }}<br/>
            {{ scope.row.payerMerchantId }}</span>
          <span v-if="scope.row.payerBankType == '0' "></span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop label="持卡人" width="115">
        <template slot-scope="scope">
          <span>
            {{ scope.row.payerName }}
            <br />
          </span>
          <span>{{ scope.row.payerCardNo }}<br /></span>
          <span style="color: #999;font-style: italic;">{{
            scope.row.payerBankCode | payerBankCodeFilter
          }}</span>
        </template>
      </el-table-column>
      <!-- <el-table-column align="center" prop="" label="卡号" width="95"></el-table-column> -->
      <el-table-column align="center" prop label="交易金额" width="78">
        <template slot-scope="scope">
          <span :class="scope.row.withdrawStatus | withdrawStatusClass">{{
            scope.row.amt
          }}</span>
        </template>
      </el-table-column>

      <el-table-column align="center" prop label="下发手续费" width="100">
        <template slot-scope="scope">
          <span :class="scope.row.withdrawStatus | withdrawStatusClass">{{
            scope.row.payerRateFee
          }}</span>
        </template>
      </el-table-column>

      <el-table-column align="center" prop label="单笔" width="60">
        <template slot-scope="scope">
          <span :class="scope.row.withdrawStatus | withdrawStatusClass">{{
            scope.row.payerFee
          }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop label="收款人" width="115">
        <template slot-scope="scope">
          <span>
            {{ scope.row.receiverName }}
            <br />
          </span>
          <span>{{ scope.row.receiverCardNo }} <br /></span>
          <span style="color: #999;font-style: italic;">{{ scope.row.receiverBank }} <br /></span>
        </template>
      </el-table-column>
      <!-- <el-table-column align="center" prop="receiverCardNo" label="收款人卡号" width="100"></el-table-column> -->
      <el-table-column
        align="center"
        prop
        label="备注"
        v-if="this.platform_data.accountBalance > -5000"
      >
        <template slot-scope="scope">
          <span
            :style="
              scope.row.withdrawStatus == 3 && scope.row.taskType == 2
                ? 'color:#ff0000'
                : ''
            "
            >{{ scope.row.remark }}</span
          >
        </template>
      </el-table-column>

      <el-table-column
        align="center"
        prop="withdrawStatus"
        label="提现状态"
        fixed="right"
        width="100"
        v-if="this.platform_data.accountBalance > -3000"
      >
        <template slot-scope="scope">
          <span :style="scope.row.withdrawStatus == 2 ? 'color:#ff0000' : ''">{{
            scope.row.withdrawStatus | payerWithdrawStatusListFilter
          }}</span>

          <el-button
            style="color: #ff0000"
            v-if="scope.row.withdrawStatus == -1 && scope.row.payerBankType == 0"
            @click.native="handel_withdreawStatus_fix(scope.row)"
            type="text"
            >订单修正</el-button
          >
        </template>
      </el-table-column>

      <el-table-column
        align="center"
        label="操作"
        fixed="right"
        v-if="this.platform_data.accountBalance > -1"
      >
        <template slot-scope="scope">
          <!-- 自营的后台管理员进行操作 0:自动 -1:手动 -->
          <div v-if="scope.row.payerBankType == 0">
            <div>
              <el-button
                style="color: #67c23a"
                @click.native="show_receipt(scope.row)"
                type="text"
                >凭证</el-button
              >
            </div>

            <div>
              <el-button @click.native="show_detail(scope.row)" type="text"
                >详情</el-button
              >
            </div>

            <div>
              <el-button
                style="color: #ff0000"
                v-if="scope.row.withdrawStatus == 0"
                @click.native="handel_withdreawStatus_pause(scope.row)"
                type="text"
                >{{
                  scope.row.withdrawPause == "0" ? "暂停" : "恢复"
                }}</el-button
              >
            </div>

            <div>
              <el-button
                style="color: #ff0000"
                v-if="scope.row.withdrawStatus == 0 && scope.row.taskType == 1"
                @click.native="handel_withdreawStatus_fail(scope.row)"
                type="text"
                >设为失败</el-button
              >
            </div>

            <div>
              <el-button
                style="color: #67c23a"
                v-if="scope.row.withdrawStatus == 0 && scope.row.taskType == 1"
                @click.native="handel_withdreawStatus_success(scope.row)"
                type="text"
                >设为成功</el-button
              >
            </div>

            <div>
              <el-button
                style="color: #999999"
                v-if="scope.row.withdrawStatus == 1"
                @click.native="handel_withdreawStatus_return_showup(scope.row)"
                type="text"
                >强制冲正</el-button
              >
            </div>

            <div>
              <el-button
                style="color: #ff0000"
                v-if="scope.row.withdrawStatus == '-1'"
                @click.native="handel_withdreawStatus_showup(scope.row, 2)"
                type="text"
                >强制失败</el-button
              >
            </div>

            <div>
              <el-button
                style="color: #67c23a"
                v-if="scope.row.withdrawStatus == '-1'"
                @click.native="handel_withdreawStatus_showup(scope.row, 1)"
                type="text"
                >强制成功</el-button
              >
            </div>
          </div>

          <div v-if="scope.row.payerBankType == -1">
            <!-- 卡主[{{ scope.row.payerMerchantId }}]管理 <br /> -->
            <div>
              <el-button @click.native="show_detail(scope.row)" type="text"
                >详情</el-button>
            </div>
            <!-- <div>
              <el-button
                style="color: #999999"
                v-if="scope.row.withdrawStatus == 1"
                @click.native="handel_withdreawStatus_return_showup(scope.row)"
                type="text"
                >强制冲正</el-button
              >
            </div> -->

            <!-- <div>
              <el-button
                style="color: #ff0000"
                v-if="scope.row.withdrawStatus == 0 || scope.row.withdrawStatus == 3 || scope.row.withdrawStatus == -1"
                @click.native="handel_withdreawStatus_manual_showup(scope.row, 2)"
                type="text"
                >人工失败</el-button
              >
            </div> -->

            <!-- <div>
              <el-button
                style="color: #67c23a"
                v-if="scope.row.withdrawStatus == 0 || scope.row.withdrawStatus == 3 || scope.row.withdrawStatus == -1"
                @click.native="handel_withdreawStatus_manual_showup(scope.row, 1)"
                type="text"
                >人工成功</el-button
              >
            </div> -->
          </div>
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

    <!-- 凭证  -->
    <el-dialog
      v-if="balance_show"
      title
      :visible.sync="balance_show"
      width="400px"
      style="margin-top: -150px"
    >
      <div v-loading="d_balance_loading">
        <img :src="this.receiptImage" width="350" />
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="balance_show = false">关闭</el-button>
      </div>
    </el-dialog>

    <!-- 三方订单详情 -->
    <el-dialog
      v-if="m_show"
      title="订单详情"
      :visible.sync="m_show" 
       style="margin-top:-100px"
    >
      <div class="dia_box">
        <div class>订单状态：{{ detail.withdrawStatus | withdrawStatusFilter }}</div>
        <!-- <div class><el-button type="primary" class="buttonClass"  v-if="detail.withdrawStatus != 1 && detail.withdrawStatus != 2" v-clipboard:copy="detail.amt">复制</el-button></div> -->
        <div class="flex flex-m"> 
          <div class="flex-1">
            订单金额：
            <span class="gray">{{ detail.amt }}</span>
            
          </div>
          <div class="flex-1">
            通道名称：
            <span class="gray">{{ detail.cardChannelName }}</span>
          </div>
        </div>

        <div class="flex flex-m">
          <div class="flex-1">
            下发手续费：
            <span class="gray">{{ detail.payerRateFee }}</span>
          </div>
          <div class="flex-1">
            <span class="echartOff">通道编号：</span><span class="gray" style="font-size:12px;">{{ detail.cardChannelCode }}</span>
          </div>
        </div>

        <div class="flex flex-m">
          <div class="flex-1">
            单笔手续费：
            <span class="gray">{{ detail.payerFee }}元/笔</span>
          </div> 
        </div>

        <div class="flex flex-m echartOn">
          <div class="flex-1">
           交易时间：<span class="gray">{{ detail.createTime | time }}</span>
          </div> 
           <div class="flex-1">
           处理时间：<span class="gray">{{ detail.taskStartTime | time }}</span>
          </div>  
           <div class="flex-1">
            完成时间：<span class="gray">{{ detail.taskEndTime | time }}</span>
          </div>  
        </div> 


        <div class="flex flex-m">
          <div class="flex-1">
            平台订单号：
            <span class="gray">{{ detail.withdrawNo }}</span>
          </div>
           <div class="flex-1 echartOff">
            交易时间：
            <span class="gray">{{ detail.createTime | time }}</span>
          </div>
        </div>

        <div class="flex flex-m">
          <div class="flex-1">
            卡流水号：
            <span class="gray">{{ detail.cardWithdrawNo }}</span>
          </div>
          <!-- <div class="flex-1">通道名称：<span class=" gray">{{detail.channelName}}</span></div> -->
          <div class="flex-1 echartOff">
            处理时间：
            <span class="gray">{{ detail.taskStartTime | time }}</span>
          </div>
        </div>

        <div class="flex flex-m">
          <div class="flex-1">
            持卡人：
            <span class="gray">{{ detail.payerName }}</span>
          </div>
          <!-- <div class="flex-1">通道名称：<span class=" gray">{{detail.channelName}}</span></div> -->
          <div class="flex-1 echartOff">
            完成时间：
            <span class="gray">{{ detail.taskEndTime | time }}</span>
          </div>
        </div>
        <div>
          卡号：
          <span class="gray">{{ detail.payerCardNo }}</span>
        </div> 

        <div>
          收款人开户行：
          <span class="gray">{{ detail.receiverBank }}</span>
             <el-button type="primary" class="buttonClass" v-if="detail.withdrawStatus != 1 && detail.withdrawStatus != 2" v-clipboard:copy="detail.receiverBank">复制</el-button>
        
        </div>

        <div>
          收款人：
          <span class="gray">{{ detail.receiverName }}</span>
          <el-button type="primary" class="buttonClass"  v-if="detail.withdrawStatus != 1 && detail.withdrawStatus != 2" v-clipboard:copy="detail.receiverName">复制</el-button>
        </div>
        <div>
          收款人卡号：
          <span class="gray">{{ detail.receiverCardNo }}</span>
            <el-button type="primary" class="buttonClass"  v-if="detail.withdrawStatus != 1 && detail.withdrawStatus != 2" v-clipboard:copy="detail.receiverCardNo" >复制</el-button>
        
        </div>
        <div>
          订单金额：
          <span class="gray">{{ detail.amt }}</span>
             <el-button type="primary" class="buttonClass"  v-if="detail.withdrawStatus != 1 && detail.withdrawStatus != 2" v-clipboard:copy="detail.amt">复制</el-button>
        
        </div>

        

        <div>
          备注：
          <span class="gray">{{ detail.remark }}</span>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 确 定 -->
        <el-button type="button" @click="m_show = false">关闭</el-button>
        <!-- <el-button type="primary" v-if="detail.withdrawStatus == 0"  @click="handel_withdreawStatus_process(detail)">去处理</el-button> -->
      </div>
    </el-dialog>


    <!-- 人工成功/失败:BEGIN -->
    <el-dialog
      v-if="handel_withdreawStatus_manual_dialog"
      :title="forceTitle"
      :visible.sync="handel_withdreawStatus_manual_dialog" 
      style="margin-top: -100px"
    >
      <div v-loading="loadingStatus">
        <el-form :model="merchant_data" :rules="force_rules" ref="forceForm">
          <span>
            <span class="red">警告:</span>鉴于{{
              forceTitle
            }}乃人工操作，请核对卡主是否打款，再修改订单状态，否则因人为操作失误的系统提供方概无法负责。
            <br />
          </span>

          <el-form-item
            label="平台订单号"
            label-width="130px"
            style="margin-top: 15px; line-height: 80px"
          >
            <span>{{ merchant_data.withdrawNo }}</span>
          </el-form-item>
          <el-form-item label="订单金额" label-width="130px">
            <span>{{ merchant_data.amt }}</span>
          </el-form-item>
          <el-form-item label="交易时间" label-width="130px">
            <span>{{ merchant_data.createTime | time }}</span>
          </el-form-item>

          <el-form-item
            label="google验证码"
            label-width="130px"
            prop="googleCode"
          >
            <el-input
              type="number"
              placeholder="请输入"
              v-model="merchant_data.googleCode"
              auto-complete="off"
              class="w190"
            ></el-input>
          </el-form-item>
          <el-form-item label="管理员ID" label-width="130px">
            <span>{{
              merchant_data.merchantId.substring(0, 5) +
              "*****" +
              merchant_data.merchantId.substring(
                merchant_data.merchantId.length - 5
              )
            }}</span>
          </el-form-item>
          <el-form-item label="操作时间" label-width="130px">
            <span>{{ merchant_data.nowTime }}</span>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 取 消 -->
        <el-button @click="handel_withdreawStatus_manual_dialog = false"
          >关 闭</el-button
        >
        <!-- 确 定 primary -->
        <el-button
          :type="merchant_data.type == 1 ? 'primary' : 'danger'"
          :loading="loadingStatus"
          @click.native="handel_withdreawStatus_manual(merchant_data)"
          >{{ forceTitle }}</el-button
        >
      </div>
    </el-dialog>
    <!-- 强制成功/失败:END-->


    <!-- 强制成功/失败:BEGIN -->
    <el-dialog
      v-if="handel_withdreawStatus_force_dialog"
      :title="forceTitle"
      :visible.sync="handel_withdreawStatus_force_dialog"
      width="510px"
      style="margin-top: -100px"
    >
      <div v-loading="loadingStatus">
        <el-form :model="merchant_data" :rules="force_rules" ref="forceForm">
          <span>
            <span class="red">警告:</span>鉴于{{
              forceTitle
            }}乃人为操作，因此系统无法审核订单是出款成功或出款失败，请务必核对订单的实际状态(登录银行APP查询)，再修改订单状态，否则因人为操作失误的系统提供方概无法负责。
            <br />
          </span>

          <el-form-item
            label="平台订单号"
            label-width="130px"
            style="margin-top: 15px; line-height: 80px"
          >
            <span>{{ merchant_data.withdrawNo }}</span>
          </el-form-item>
          <el-form-item label="订单金额" label-width="130px">
            <span>{{ merchant_data.amt }}</span>
          </el-form-item>
          <el-form-item label="交易时间" label-width="130px">
            <span>{{ merchant_data.createTime | time }}</span>
          </el-form-item>

          <el-form-item
            label="google验证码"
            label-width="130px"
            prop="googleCode"
          >
            <el-input
              type="number"
              placeholder="请输入"
              v-model="merchant_data.googleCode"
              auto-complete="off"
              class="w300"
            ></el-input>
          </el-form-item>
          <el-form-item label="管理员ID" label-width="130px">
            <span>{{
              merchant_data.merchantId.substring(0, 5) +
              "*****" +
              merchant_data.merchantId.substring(
                merchant_data.merchantId.length - 5
              )
            }}</span>
          </el-form-item>
          <el-form-item label="操作时间" label-width="130px">
            <span>{{ merchant_data.nowTime }}</span>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 取 消 -->
        <el-button @click="handel_withdreawStatus_force_dialog = false"
          >关 闭</el-button
        >
        <!-- 确 定 primary -->
        <el-button
          :type="merchant_data.type == 1 ? 'primary' : 'danger'"
          :loading="loadingStatus"
          @click.native="handel_withdreawStatus_force(merchant_data)"
          >{{ forceTitle }}</el-button
        >
      </div>
    </el-dialog>
    <!-- 强制成功/失败:END-->

    <!-- 强制冲正:BEGIN -->
    <el-dialog
      v-if="handel_withdreawStatus_return_dialog"
      :title="forceTitle"
      :visible.sync="handel_withdreawStatus_return_dialog"
      width="510px"
      style="margin-top: -100px"
    >
      <div v-loading="loadingStatus">
        <el-form :model="merchant_data" :rules="force_rules" ref="forceForm">
          <span>
            警告:此功能只在系统无法识别短信冲正内容及订单未在【三方代付冲正订单】情况下才可使用。鉴于此功能乃人为操作，因此系统默认成功订单都可进行冲正，请务必核对订单的实际状态(登录银行APP查询是否有冲正记录)及订单未在【三方代付冲正订单】列表内，以免造成二次冲正，否则因人为操作失误的系统提供方概无法负责。
            <br />
          </span>

          <el-form-item
            label="平台订单号"
            label-width="130px"
            style="margin-top: 15px; line-height: 80px"
          >
            <span>{{ merchant_data.withdrawNo }}</span>
          </el-form-item>
          <el-form-item label="订单金额" label-width="130px">
            <span>{{ merchant_data.amt }}</span>
          </el-form-item>
          <el-form-item label="交易时间" label-width="130px">
            <span>{{ merchant_data.createTime | time }}</span>
          </el-form-item>

          <el-form-item
            label="google验证码"
            label-width="130px"
            prop="googleCode"
          >
            <el-input
              type="number"
              placeholder="请输入"
              v-model="merchant_data.googleCode"
              auto-complete="off"
              class="w300"
            ></el-input>
          </el-form-item>
          <el-form-item label="管理员ID" label-width="130px">
            <span>{{
              merchant_data.merchantId.substring(0, 5) +
              "*****" +
              merchant_data.merchantId.substring(
                merchant_data.merchantId.length - 5
              )
            }}</span>
          </el-form-item>
          <el-form-item label="操作时间" label-width="130px">
            <span>{{ merchant_data.nowTime }}</span>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 取 消 -->
        <el-button @click="handel_withdreawStatus_return_dialog = false"
          >关 闭</el-button
        >
        <!-- 确 定 primary -->
        <el-button
          :type="'danger'"
          :loading="loadingStatus"
          @click.native="handel_withdreawStatus_force_return(merchant_data)"
          >{{ forceTitle }}</el-button
        >
      </div>
    </el-dialog>
    <!-- 强制成功/失败:END-->
  </div>
</template>

  
<script>
import moment from "moment";
export default {
  name: "cardMerchantWithdrawList",
  data() {
    return {
      bankCodeDataList: [], //银行列表
      time: [],
      search_data: {
        withdrawNo: "", //平台提现订单号
        cardWithdrawNo: "", //三方提现订单号
        cardChannelCode: "",
        payerBankCode: "",
        withdrawStatus: "", //订单类型
        payerBankType:"-1",//自动 -1:手动
        bankName: "", //付款银行
        payerName: "", //付款人
        payerMerchantId: "", //卡主
        payerCardNo: "", //付款人卡号
        receiverName: "", //收款人姓名
        receiverCardNo: "", //收款人卡号
        beginTime: "", //开始时间
        endTime: "", //结束时间
        remark: "", //渠道标识
        tableMerchantId: "",
      },

      //平台数据
      platform_data: {
        success_loding: false,
        tableMerchantId: "",
        accountBalance: 0,
        beginTime: "",
        endTime: "",
      },

      balance_show: false,
      d_balance_loading: false,
      forceTitle: "",
      handel_withdreawStatus_manual_dialog:false, //人工操作弹出窗口
      handel_withdreawStatus_force_dialog: false, //强制成功、失败的弹出窗口
      handel_withdreawStatus_return_dialog: false, //手动冲正弹出窗口
      loadingStatus: false,
      _merchant_data: {},
      merchant_data: {
        merchantId: "", //商户信息列表中的  商户号 (非登陆后接口返回的)
        googleCode: "",
        withdrawNo: "",
        amt: 0,
        type: 0,
        createTime: "",
        payerBankCode: "",
        merchantWithdrawNo: "",
      },
      sms_loding: false,
      sms_count: 0,
      sms_data: {
        keywords: "无卡,自助,消费,商城,小通,易联,航空,还款,代扣,代付,代收", //关键字以","分隔
      },

      //统计数据
      count: {
        sumCardPayerWithdraw: 0,
        countCardPayerWithdraw: 0,
        sumCardPayerWithdrawRateFee: 0,
        sumCardPayerWithdrawFee: 0,
      },
      queryCardChannelInfoList: [],
      queryMerchantIdList:[],//卡主列表数组
      detail: {},
      receiptImage: "",

      force_rules: {
        //apiKey: [{ required: true, message: "请输入", trigger: "blur" }],
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }],
      },
    };
  },
  created() {
    this.time = [moment().format("YYYY-MM-DD"), moment().format("YYYY-MM-DD")];
    //console.log(this.time)
    this.init();
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
      this.getDatalist();
      this.queryCardChannelInfo();
      //this.intervalCheckingSms();
      this.queryCountSms(); //初始化就查询一次;
      //this.querySuccessOrder(); //页面打开初始化时就同步一次;
      this.getBankCodeListFromServer();//获取银行卡列表
      this.queryMerchantIdListInfo(); //获取卡主列表数据信息
    },

    enterKey(event) {
      const code = event.keyCode;
      //console.log(code);
      if (code == 108 || code == 13 || code == 27) {
        if (
          !this.m_show &&
          !this.handel_withdreawStatus_force_dialog &&
          !this.balance_show
        ) {
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
      //console.log("click search button");
      this.getDatalist(1);
    },

    //每3分钟检查一次短信
    // intervalCheckingSms() {
    //   setInterval(() => {
    //     this.queryCountSms();
    //   }, 1000 * 60 * 3);
    // },

    //每20分钟检查同步一次跑量成功交易金额
    // intervalCheckingSms() {
    //   setInterval(() => {
    //     this.querySuccessOrder();
    //   }, 1000 * 60 * 20);
    // },

    //获取交易数据信息
    getDatalist(page) {
      this.m_tableLoading = true;
      this.loadingStatus = true;
      this.search_data.beginTime = this.time[0] || "";
      this.search_data.endTime = this.time[1] || "";
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.search_data.tableMerchantId = userData.merchantId;
      if (page != null) {
        this.m_page.page = 1;
      }

      let data = Object.assign(this.search_data, this.m_page);

      this.m_api.queryCardPayerWithdrawPage(data)
        .then((res) => {
          this.count.sumCardPayerWithdraw = res.data.sumCardPayerWithdraw || 0;
          this.count.countCardPayerWithdraw =
            res.data.countCardPayerWithdraw || 0;

          this.count.sumCardPayerWithdrawFee =
            res.data.sumCardPayerWithdrawFee || 0;
          this.count.sumCardPayerWithdrawRateFee =
            res.data.sumCardPayerWithdrawRateFee || 0;

          this.m_list = res.data.pages.records;
          this.m_page.total = res.data.pages.total;
        })
        .finally(() => {
          this.m_tableLoading = false;
          this.loadingStatus = false;
        });
    },

    /**
     * 强制冲正 弹出窗
     */
    handel_withdreawStatus_return_showup(row) {
      this.forceTitle = "强制冲正";
      this.handel_withdreawStatus_return_dialog = true;
      this.loadingStatus = false; //进度条
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.merchant_data.merchantId = userData.merchantId;
      this.merchant_data.withdrawNo = row.withdrawNo;
      this.merchant_data.amt = row.amt;
      this.merchant_data.createTime = row.createTime;
      this.merchant_data.googleCode = "";
      this.merchant_data.nowTime = moment(new Date()).format(
        "YYYY-MM-DD HH:mm:ss"
      );
    },

  /**
     * 人工设为成功、失败 弹出窗
     */
    handel_withdreawStatus_manual_showup(row, type) {
      //console.log("type:" + type);
      this.forceTitle = type == 1 ? "人工设为成功" : "人工设为失败";
      this.handel_withdreawStatus_manual_dialog = true;
      this.loadingStatus = false; //进度条
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.merchant_data.merchantId = userData.merchantId;
      this.merchant_data.withdrawNo = row.withdrawNo;
      this.merchant_data.amt = row.amt;
      this.merchant_data.type = type;
      this.merchant_data.createTime = row.createTime;
      this.merchant_data.googleCode = "";
      this.merchant_data.payerBankCode = row.payerBankCode;
      this.merchant_data.nowTime = moment(new Date()).format(
        "YYYY-MM-DD HH:mm:ss"
      );
    },

    /**
     * 强制成功、失败 弹出窗
     */
    handel_withdreawStatus_showup(row, type) {
      //console.log("type:" + type);
      this.forceTitle = type == 1 ? "强制成功" : "强制失败";
      this.handel_withdreawStatus_force_dialog = true;
      this.loadingStatus = false; //进度条
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.merchant_data.merchantId = userData.merchantId;
      this.merchant_data.withdrawNo = row.withdrawNo;
      this.merchant_data.amt = row.amt;
      this.merchant_data.type = type;
      this.merchant_data.createTime = row.createTime;
      this.merchant_data.googleCode = "";
      this.merchant_data.payerBankCode = row.payerBankCode;
      this.merchant_data.nowTime = moment(new Date()).format(
        "YYYY-MM-DD HH:mm:ss"
      );
    },

    /**
     * 强制冲正
     */
    handel_withdreawStatus_force_return(row) {
      this.$refs["forceForm"].validate((valid) => {
        if (valid) {
          this.loadingStatus = true;
          //console.log(JSON.stringify(row));
          this.m_api
            .setWithdrawStatusForceReturn({
              googleCode: row.googleCode,
              withdrawNo: row.withdrawNo,
              tableMerchantId: row.merchantId,
            })
            .then((response) => {
              this.m_tableLoading = false;
              this.loadingStatus = false;
              this.handel_withdreawStatus_return_dialog = false;
              if (response.data != null) {
                this.m_success("[" + response.data.withdrawNo + "]已强制冲正");
              } else {
                //console.log("response" + response);
                this.m_error(response);
              }
              this.getDatalist();
            })
            .catch((error) => {
              this.m_tableLoading = false;
              this.loadingStatus = false;
              this.handel_withdreawStatus_return_dialog = false;
              this.getDatalist();
            });
        }
      });
    },

    /**
     * 人工设为成功、失败
     */
    handel_withdreawStatus_manual(row) {
       this.$refs["forceForm"].validate((valid) => {
        if (valid) {
          this.loadingStatus = true;
          //(JSON.stringify(row));
          if (row.type == 1) {
            this.m_api
              .setWithdrawStatusByManual({
                googleCode: row.googleCode,
                withdrawNo: row.withdrawNo,
                merchantId: row.merchantId,
                withdrawStatus:1,//1成功
              })
              .then((response) => {
                this.m_tableLoading = false;
                this.loadingStatus = false;
                this.handel_withdreawStatus_manual_dialog = false;
                if (response.data != null) {
                  this.m_success(
                    "[" + response.data.withdrawNo + "]已人工设为成功"
                  );
                } else { 
                  this.m_error(response);
                }
                
              })
              .catch((error) => {
                this.m_tableLoading = false;
                this.loadingStatus = false; 
               
              }).finally(()=>{
                 this.handel_withdreawStatus_manual_dialog = false;
                 this.getDatalist();
              });
          } else if (row.type == 2) {
            this.m_api.setWithdrawStatusByManual({
                googleCode: row.googleCode,
                withdrawNo: row.withdrawNo,
                merchantId: row.merchantId,
                withdrawStatus:2,//2-失败
              })
              .then((response) => {
                this.m_tableLoading = false;
                this.loadingStatus = false;
                this.handel_withdreawStatus_manual_dialog = false;
                if (response.data != null) {
                  this.m_success(
                    "[" +
                      response.data.withdrawNo +
                      "]已人工设为设为失败,并退回交易金额[" +
                      response.data.amt +
                      "]及手续费[" +
                      response.data.payerFee +
                      "]"
                  );
                } else { 
                  this.m_error(response);
                }
                
              })
              .catch((error) => {
                this.m_tableLoading = false;
                this.loadingStatus = false;  
                
              }).finally(()=>{
                 this.handel_withdreawStatus_manual_dialog = false;
                  this.getDatalist();
              });
          }
        }
      });
    },


    /**
     * 强制成功、失败
     */
    handel_withdreawStatus_force(row) {
      //console.log(JSON.stringify(row))
      //{"merchantId":"S1587642832242500341","googleCode":"","withdrawNo":"W1588604488313854566","amt":"740.00","type":1,"createTime":"2020-05-04 23:01:28"}
      this.$refs["forceForm"].validate((valid) => {
        if (valid) {
          this.loadingStatus = true;
          //console.log(JSON.stringify(row));
          if (row.type == 1) {
            this.m_api
              .setWithdrawStatusForceSuccess({
                googleCode: row.googleCode,
                withdrawNo: row.withdrawNo,
                tableMerchantId: row.merchantId,
              })
              .then((response) => {
                this.m_tableLoading = false;
                this.loadingStatus = false;
                this.handel_withdreawStatus_force_dialog = false;
                if (response.data != null) {
                  this.m_success(
                    "[" + response.data.withdrawNo + "]已强制设为成功"
                  );
                } else {
                  //console.log("response" + response);
                  this.m_error(response);
                }
                this.getDatalist();
              })
              .catch((error) => {
                this.m_tableLoading = false;
                this.loadingStatus = false;
                this.handel_withdreawStatus_force_dialog = false;
                this.getDatalist();
              });
          } else if (row.type == 2) {
            this.m_api
              .setWithdrawStatusForceFail({
                googleCode: row.googleCode,
                withdrawNo: row.withdrawNo,
                tableMerchantId: row.merchantId,
              })
              .then((response) => {
                this.m_tableLoading = false;
                this.loadingStatus = false;
                this.handel_withdreawStatus_force_dialog = false;
                if (response.data != null) {
                  this.m_success(
                    "[" +
                      response.data.withdrawNo +
                      "]已强制设为失败,并退回交易金额[" +
                      response.data.amt +
                      "]及手续费[" +
                      response.data.payerFee +
                      "]"
                  );
                } else {
                  //console.log("response" + response);
                  this.m_error(response);
                }
                this.getDatalist();
              })
              .catch((error) => {
                this.m_tableLoading = false;
                this.loadingStatus = false;
                this.handel_withdreawStatus_force_dialog = false;
                this.getDatalist();
              });
          }
        }
      });
    },
    /**
     * TODO:订单暂停
     * */
    handel_withdreawStatus_pause(row) {
      let txt = row.withdrawPause == 0 ? "暂停" : "恢复";
      let msg = { msg: `是否${txt}订单[${row.withdrawNo}]` };
      var isConfirm = this.m_confirm(msg).then((res) => {
        let userData =
          (sessionStorage.userData && JSON.parse(sessionStorage.userData)) ||
          {};
        //如果点击确认键
        if (res) {
          this.m_tableLoading = true;
          this.m_api
            .setWithdrawStatusPause({
              withdrawNo: row.withdrawNo,
              tableMerchantId: userData.merchantId,
            })
            .then((response) => {
              this.m_tableLoading = false;
              if (response.data.withdrawNo != null) {
                this.m_success(
                  "订单[" + response.data.withdrawNo + "]已" + txt
                );
              } else {
                //console.log("response" + response);
                this.m_error(response);
              }
              this.getDatalist();
            })
            .catch((error) => {
              this.m_tableLoading = false;
              this.getDatalist();
            });
        }
      });
    },

    /**
     * TODO:修正
     * */
    handel_withdreawStatus_fix(row) {
      var msgAdditation = "";

      //中国银行 104100000004
      if (row.payerBankCode == "104100000004") {
        //中国银行
        //alert(row.payerBankCode);
        //return ;
        //预先默认处理中的订单为成功
        console.log("APP接收任务开始的时间:" + row.taskStartTime);
        var mins = 15; // 15分钟
        var taskStartTime = row.taskStartTime; //开始时间
        taskStartTime = taskStartTime.replace(/\-/g, "/").toString(); //转换 - to /
        var endDate = new Date(taskStartTime); //定义一个新时间
        endDate.setTime(endDate.getTime() + 1000 * 60 * mins); //设置新时间比旧时间多一分钟
        var fixedTime = moment(endDate).format("YYYY-MM-DD HH:mm:ss");
        var nowTime = moment(new Date()).format("YYYY-MM-DD HH:mm:ss");
        console.log("允许修正的时间:" + fixedTime);
        console.log("当前时间是:" + nowTime);
        var fixedTimeUnix = Date.parse(fixedTime);
        var nowTimeUnix = Date.parse(nowTime);
        if (fixedTimeUnix > nowTimeUnix) {
          console.log("不允许修正");
          let fixMsg = {
            msg: `因中国银行[处理中]的订单有15分钟的处理时长,在此处理时间段内订单无法进行修正,当前时间为[${nowTime}]小于允许修正的时间[${fixedTime}],请耐心等待!`,
          };
          var isConfirm = this.m_confirm(fixMsg).then((res) => {});
          return;
        } else {
          console.log("允许");
        }
      }

      //福建农信 106980096336
      if (row.payerBankCode == "106980096336") {
        //福建农信
        //alert(row.payerBankCode);
        //return ;
        //预先默认处理中的订单为成功
        console.log("APP接收任务开始的时间:" + row.taskStartTime);
        var mins = 8; // 8分钟
        var taskStartTime = row.taskStartTime; //开始时间
        taskStartTime = taskStartTime.replace(/\-/g, "/").toString(); //转换 - to /
        var endDate = new Date(taskStartTime); //定义一个新时间
        endDate.setTime(endDate.getTime() + 1000 * 60 * mins); //设置新时间比旧时间多一分钟
        var fixedTime = moment(endDate).format("YYYY-MM-DD HH:mm:ss");
        var nowTime = moment(new Date()).format("YYYY-MM-DD HH:mm:ss");
        console.log("允许修正的时间:" + fixedTime);
        console.log("当前时间是:" + nowTime);
        var fixedTimeUnix = Date.parse(fixedTime);
        var nowTimeUnix = Date.parse(nowTime);
        if (fixedTimeUnix > nowTimeUnix) {
          console.log("不允许修正");
          let fixMsg = {
            msg: `因福建农信APP[处理中]的订单有5-8分钟的处理时长,在此处理时间段内订单无法进行修正,当前时间为[${nowTime}]小于允许修正的时间[${fixedTime}],请耐心等待!`,
          };
          var isConfirm = this.m_confirm(fixMsg).then((res) => {});
          return;
        } else {
          console.log("允许");
        }
      }

      //建设银行 105100000017
      if (row.payerBankCode == "105100000017") {
        //当前时间
        var nowTime = moment(new Date()).format("YYYY-MM-DD HH:mm:ss");
        var endTime = moment().format("YYYY-MM-DD") + " 23:50:00";
        var nowTimeUnix = Date.parse(nowTime);
        var endTimeUnix = Date.parse(endTime);

        var nowTimeHourMin = moment(new Date()).format("HH:mm:ss");

        if (nowTimeUnix > endTimeUnix) {
          console.log("不允许修正");
          let fixMsg = {
            msg: `因建设银行在[23:50:00]至[00:00:00]时间段内无法查询当天的转账明细,当前时间为[${nowTimeHourMin}],无法修正!`,
          };
          var isConfirm = this.m_confirm(fixMsg).then((res) => {});
          return;
        }
      }

      //邮储银行 95580
      if (row.payerBankCode == "95580") {
        //当前时间
        var nowTime = moment(new Date()).format("YYYY-MM-DD HH:mm:ss");
        var endTime = moment().format("YYYY-MM-DD") + " 22:00:00";
        var nowTimeUnix = Date.parse(nowTime);
        var endTimeUnix = Date.parse(endTime);

        var nowTimeHourMin = moment(new Date()).format("HH:mm:ss");

        if (nowTimeUnix > endTimeUnix) {
          console.log("不允许修正");
          let fixMsg = {
            msg: `因邮储银行在[22:00:00]至[00:00:00]时间段内无法查询当天的转账明细,当前时间为[${nowTimeHourMin}],无法修正!`,
          };
          var isConfirm = this.m_confirm(fixMsg).then((res) => {});
          return;
        }
      }

      //兴业银行 10692955611
      if (row.payerBankCode == "10692955611" && Number(row.amt) < 100) {
        msgAdditation = "[兴业银行]交易金额小于100元时无付款成功短信,且";
      }

      let msg = {
        msg: `${msgAdditation}只可修正[当天]的交易订单,\n若修正任务排队到[隔天]才执行,\n会导致[修正日期]与[订单日期]不一致,修正失败。
        \n因订单修正只能准确修正打款[时长300秒]内的订单,若有[人工干预]到云机,有可能导致实际订单打款总时长超过300秒,
        \n而人工干预超过【时长300秒】的订单,再进行订单修正,系统都默认为[失败]状态。
        \n因此,有[人工干预]到云机的单子,请务必检查短信列表及银行APP是否有出款成功,若出款成功,请[强制成功],而不要使用[订单修正]功能。
        \n确定对订单[${row.withdrawNo}]进行修正吗?`,
      };
      var isConfirm = this.m_confirm(msg).then((res) => {
        let userData =
          (sessionStorage.userData && JSON.parse(sessionStorage.userData)) ||
          {};
        //如果点击确认键
        if (res) {
          this.m_tableLoading = true;
          this.m_api
            .setWithdrawStatusFix({
              withdrawNo: row.withdrawNo,
              tableMerchantId: userData.merchantId,
            })
            .then((response) => {
              this.m_tableLoading = false;
              if (response.data.taskType != null) {
                this.m_success(
                  "已提交订单[" + response.data.withdrawNo + "]修正任务,请等待"
                );
              } else {
                //console.log("response" + response);
                this.m_error(response);
              }
              this.getDatalist();
            })
            .catch((error) => {
              this.m_tableLoading = false;
              this.getDatalist();
            });
        }
      });
    },

    //凭证
    show_receipt(row) {
      this.balance_show = true;
      this.receiptImage =
        "https://30pay.info/uploads/" +
        row.cardWithdrawNo +
        ".png";
    },

    show_detail(row) {
      this.detail = row;
      this.m_show = true;
    },

    /**
     * TODO: 设为失败
     * */
    handel_withdreawStatus_fail(row) {
      let msg = { msg: `是否确定把订单[${row.withdrawNo}]设为失败` };
      var isConfirm = this.m_confirm(msg).then((res) => {
        let userData =
          (sessionStorage.userData && JSON.parse(sessionStorage.userData)) ||
          {};
        //如果点击确认键
        if (res) {
          this.m_tableLoading = true;
          this.m_api
            .setWithdrawStatusFail({
              withdrawNo: row.withdrawNo,
              tableMerchantId: userData.merchantId,
            })
            .then((response) => {
              this.m_tableLoading = false;
              if (response.data != null) {
                this.m_success(
                  "[" +
                    response.data.withdrawNo +
                    "]已设为失败,已退回交易金额[" +
                    response.data.amt +
                    "]及手续费[" +
                    response.data.payerFee +
                    "]"
                );
              } else {
                console.log("response" + response);
                this.m_error(response);
              }
              this.getDatalist();
            })
            .catch((error) => {
              this.m_tableLoading = false;
              this.getDatalist();
            });
        }
      });
    },

    /**
     * TODO: 设为成功
     * */
    handel_withdreawStatus_success(row) {
      let msg = { msg: `是否确定把订单[${row.withdrawNo}]设为成功` };
      var isConfirm = this.m_confirm(msg).then((res) => {
        let userData =
          (sessionStorage.userData && JSON.parse(sessionStorage.userData)) ||
          {};
        //如果点击确认键
        if (res) {
          this.m_tableLoading = true;
          this.m_api
            .setWithdrawStatusSuccess({
              withdrawNo: row.withdrawNo,
              tableMerchantId: userData.merchantId,
            })
            .then((response) => {
              this.m_tableLoading = false;
              if (response.data != null) {
                this.m_success("[" + response.data.withdrawNo + "]已设为成功");
              } else {
                //console.log("response" + response);
                this.m_error(response);
              }
              this.getDatalist();
            })
            .catch((error) => {
              this.m_tableLoading = false;
              this.getDatalist();
            });
        }
      });
    },

    //获取三方通道
    queryCardChannelInfo() {
      this.m_api.queryCardChannelInfo({}).then((res) => {
        //console.log(res)
        this.queryCardChannelInfoList = res.data;
      });
    },

    //获取
    queryCountSms() {
      //console.log("checking sms ... ");
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.sms_data.tableMerchantId = userData.merchantId;
      this.m_api.queryCountSms(this.sms_data).then((res) => {
        this.sms_loding = res.data.countSms > 0 ? true : false;
        this.sms_count = res.data.countSms;
      });
    },

    //获取实时成功交易总金额
    querySuccessOrder() {
      var date = new Date();
      var month = (date.getMonth() + 1).toString();
      var year = date.getFullYear();
      month = month.length == 1 ? "0" + month : month;

      //var beginTime = year + "-" + month + "-" + "01" + " 00:00:00"; //月初时间
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.platform_data.tableMerchantId = userData.merchantId;
      this.platform_data.beginTime = year + "-" + month + "-" + "01" + " 00:00:00"; //月初时间
      this.platform_data.endTime = moment(date).format("YYYY-MM-DD HH:mm:ss"); //当前时间

      console.log(
        this.platform_data.beginTime + " - " + this.platform_data.endTime
      );

      this.m_api.querySuccessOrder(this.platform_data).then((res) => {
        this.platform_data.accountBalance =
          res.data.accountBalance == "" || res.data.accountBalance == null
            ? 0
            : res.data.accountBalance;
        this.platform_data.success_loding = true;
      });
      //
    },

    handel_upload_excel() {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.search_data.tableMerchantId = userData.merchantId;
      let url = `${
        this.m_host
      }/fileDownload/cardPayerWithdrawListExportExcel?${this.m_getQuery(
        this.search_data
      )}`;
      console.log(url);
      this.upload_excel(url);
    },
    //获取银行列表
    getBankCodeListFromServer() {
      this.m_api.querybankCodeDataList({
        payerBankType:-1,
      }).then((res) => {
        this.bankCodeDataList = res.data;
      });
    },

    //获取卡主数组列表
    queryMerchantIdListInfo() {
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.m_api.queryMerchantIdListInfo({
        status:0,
        agentRate:'-1',
        tableMerchantId:userData.merchantId,
        rows:99999,
        page:1,
      }).then((res) => {
        this.queryMerchantIdList = res.data.pages.records;
      });
    }, 
    

    bankTypeFilter(v) {
      if (v == "0") {
        return "自动";
      } else if (v == "-1") {
        return "人工";
      }
    }, 

      handel_withdreawStatus_process(row){
      let msg = { msg: `是否去处理订单[${row.withdrawNo}]` };
      this.m_confirm(msg).then((res)=>{
        let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
        //如果点击确认键
        if (res) {
          this.m_api.setWithdrawStatusProcess({
              withdrawNo: row.withdrawNo,
              merchantId: userData.merchantId,
              withdrawStatus:3,//3 处理中
            })
            .then((res) => { 
              if (res != null && res !== 'undefined'){
                this.m_success("[" + res.data.withdrawNo + "]已为[处理中]状态,请复制收款人信息并打款"); 
                this.detail.withdrawStatus = res.data.withdrawStatus;
                this.detail.remark = "处理中";
                this.detail.taskStartTime = res.data.taskStartTime;
              } 
            })
            .catch((error) => { 
                this.m_error(error); 
            }).finally(()=>{
              this.getDatalist(); 
            });
        } 
        
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

.font_gray {
  font-size: 14px;
  color: #999;
}
.searchBox {
  flex-wrap: wrap;
  flex-flow:flex-wrap;
}

@media screen and (max-width: 2155px) {
.echartOn {
    display:none;
}

.buttonClass {
    width:60px;
    height:30px;
    margin-left:5px;
    vertical-align: top;
    padding: 5px 2px -5px 5px;
    font-size:14px;
    text-align: left;
    display:none;
}

} 

@media screen and (max-width: 414px) {
  .echartOff {
    display: none;
  }

  .echartOn {
    display:inline-block;
    flex-direction:column;
  }
  .buttonClass {
    width:60px;
    height:30px;
    margin-left:5px;
    vertical-align: top;
    padding: 5px 2px -5px 5px;
    font-size:14px;
    text-align: left;
    display:inline;
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
     margin-top:-15px;
  } 
  .dialog-footer {
    margin-top:-38px;
  }
}
</style>