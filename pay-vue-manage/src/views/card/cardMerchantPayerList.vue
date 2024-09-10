<!-- 卡主银行卡列表  cardMerchantPayerList
 -->
<template>
  <div>
    <div class="flex flex-m tc topbox">
      <div class="flex-1 bor">
        <div>剩余卡余额</div>
        <div class="num">{{ countMap.sumPayerAmt || 0.0 }}</div>
      </div>

      <div class="flex-1">
        <div>单钱包单笔最高可提现</div>
        <div class="num">{{ highestAmt || 0.0 }}</div>
      </div>
    </div> 

    <!-- * 
         * @搜索栏
         *
    -->
    <div class="flex flex-m mb20 searchBox">
      <div class="flex flex-m mr10">
        <span class="w80">银行名称:</span>
        <el-select
          class="w150"
          size="medium"
          v-model="search_data.bankCode"
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

      <div class="flex flex-m mr10">
        <span class="w80">状态:</span>
        <el-select
          class="w150"
          size="medium"
          v-model="search_data.payerCardStatus"
          placeholder="请选择"
        >
          <el-option label="请选择卡状态" value></el-option>
          <el-option
            v-for="item in payerCardStatusList"
            :key="item.value"
            :label="item.name"
            :value="item.id"
            width="200"
          ></el-option>
        </el-select>
      </div>

      <div class="flex flex-m mr10">
        <span class="w80">卡种:</span>
        <el-select
          class="w150"
          size="medium"
          v-model="search_data.payerCardType"
          placeholder="请选择"
        >
          <el-option label="请选择卡种类" value></el-option>
          <el-option
            v-for="item in payerCardTypeList"
            :key="item.value"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
      </div>

      <div class="flex flex-m mr10">
        <span class="w80">三方通道:</span>
        <el-select
          class="w180"
          size="medium"
          v-model="search_data.cardChannelCode"
          placeholder="请选择"
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

      <div class="flex flex-m mr10">
        <span class="w80">通道名称:</span>
        <el-input
          size="medium"
          class="w180"
          v-model="search_data.chlName"
          clearable
          placeholder="通道名称"
        ></el-input>
      </div>
    </div>

    <div class="flex flex-m mb20 searchBox">
       <div class="flex flex-m mr10">
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
      <div class="flex flex-m mr10">
        <span class="w80">开户地:</span>
        <el-input
          size="medium"
          class="w120"
          v-model="search_data.bankArea"
          clearable
          placeholder="开户所在地"
        ></el-input>
      </div>

      <!-- <div class="flex flex-m mr10">
        <span class="w50">猫池:</span>
        <el-input
          size="medium"
          class="w150"
          v-model="search_data.bankCat"
          clearable
          placeholder="猫池所在地"
        ></el-input>
      </div> -->
      <!-- <div class="flex flex-m mr20">
        <span class="w60">云机:</span>
        <el-input
          size="medium"
          class="w190"
          v-model="search_data.cloudRemark"
          clearable
          placeholder="云机"
        ></el-input>
      </div> -->

      <div class="flex flex-m">
        <span class="remark">备注:</span>
        <el-input
          size="medium"
          class="w180"
          v-model="search_data.remark"
          clearable
          placeholder="备注"
        ></el-input>
      </div>
    </div>

    <div class="flex flex-m mb20 searchBox">
      <div class="flex flex-m mr10">
        <span class="w60">持卡人:</span>
        <el-input
          size="medium"
          class="w140"
          v-model="search_data.payerName"
          clearable
          placeholder="持卡人"
        ></el-input>
      </div>

      <div class="flex flex-m mr10">
        <span class="w60">卡号:</span>
        <el-input
          size="medium"
          class="w200"
          v-model="search_data.payerCardNo"
          clearable
          placeholder="卡号"
        ></el-input>
      </div>

      <div class="flex flex-m mr10">
        <span class="w60">手机:</span>
        <el-input
          size="medium"
          class="w140"
          v-model="search_data.payerPhone"
          clearable
          placeholder="手机"
        ></el-input>
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

      <el-button
        v-if="handelAuto('add')"
        icon="el-icon-circle-plus"
        size="small"
        type="success"
        @click="mer_addForm()"
        >新增银行卡</el-button
      >

      <el-button
        v-if="handelAuto('edit')"
        @click="batchModifyStatus(0)"
        icon="el-icon-circle-close"
        size="small"
        type="warning"
        >禁用</el-button
      >
    </div>

    <!-- * 
         * @操作栏
         *
    -->
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
      <el-table-column type="selection" width="25"></el-table-column>
      <el-table-column label="序号" type="index" width="50" align="center">
        <template slot-scope="scope">
          <span>{{ (m_page.page - 1) * m_page.rows + scope.$index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop="" width="100" label="所属通道">
        <template slot-scope="scope">
          {{ scope.row.cardChannelName }}<br />
          {{ scope.row.cardChannelCode }}
        </template>
      </el-table-column>
      
      <!-- <el-table-column align="center" prop="bankArea" label="开户地"></el-table-column> -->
      <!-- <el-table-column
        align="center"
        prop="bankCat"
        label="猫池"
      ></el-table-column> -->
      <!-- <el-table-column align="center" label="相机" width="100">
        <template slot-scope="scope">
          <div>
            <el-button
              style="color: #666666"
              v-if="
                (scope.row.bankCode == '95580' ||
                  scope.row.bankCode == '95595' ||
                  scope.row.bankCode == '104100000004') &&
                scope.row.payerCardType == 1
              "
              @click.native="checkCameraHeartBeat(scope.row)"
              type="text"
              >心跳检测</el-button
            >
          </div>
          <div>
            <el-button
              style="color: #666666"
              v-if="
                (scope.row.bankCode == 95580 || scope.row.bankCode == 95595) &&
                scope.row.payerCardType == 1
              "
              @click.native="checkCameraImage(scope.row)"
              type="text"
              >令牌图片</el-button
            >
          </div>
        </template>
      </el-table-column> -->
      <el-table-column
        align="center"
        prop="payerMerchantId"
        label="卡主编号"
        width="109"
      >
       <template slot-scope="scope">
          <div>
            {{scope.row.merchantName}}<br/>
            {{scope.row.payerMerchantId}}
          </div>
      </template>
      </el-table-column>

     <el-table-column align="center" prop label="银行名称" width="79">
        <template slot-scope="scope">
          <span>
            {{ scope.row.bankName }}
            <br />
          </span>
        </template>
      </el-table-column>

      <el-table-column
        align="center"
        prop="payerName"
        label="持卡人"
        width="99"
      >
       <template slot-scope="scope">
          <div>
            {{scope.row.payerName}}<br/>
            <span style="font-size:12px;font-style: italic;" :class="scope.row.payerOnline | payerOnlineStatusClass"  v-if="scope.row.payerBankType == -1 && scope.row.payerCardStatus == 2">{{scope.row.payerOnline | payerOnlineFilter}}</span>
          </div>
      </template>

      </el-table-column>
      <el-table-column
        align="center"
        prop="payerCardNo"
        label="卡号"
        width="118"
      ></el-table-column>
      <!-- <el-table-column align="center" prop="payerCardCode" label="数字串" width="100"></el-table-column> -->

      <el-table-column
        align="center"
        prop="payerPhone"
        label="手机"
        width="118"
      ></el-table-column>
      <el-table-column
        align="center"
        prop="payerAmt"
        label="余额"
        sortable
        :sort-method="comparePayerAmt"
        width="80"
      >
      <template slot-scope="scope">
        <span :class="scope.row.payerAmt > 50000 ?'green':''">{{scope.row.payerAmt}}</span>
      </template>
      </el-table-column>
      <el-table-column
        align="center"
        prop="payerWithdrawAmtDay"
        label="已下"
        sortable
        :sort-method="comparePayerWithdrawAmtDay"
        width="80"
      ></el-table-column>
      <el-table-column
        align="center"
        prop="payerWithdrawNumDay"
        label="已转"
        sortable
        :sort-method="comparePayerWithdrawNumDay"
        width="80"
      ></el-table-column>
      <el-table-column
        align="center"
        prop="payerWithdrawFailNumDay"
        label="失败"
        sortable
        :sort-method="comparePayerWithdrawFailNumDay"
        width="80"
      ></el-table-column>

      <el-table-column
        align="center"
        prop="payerProcessingNum"
        label="排队"
        sortable
        :sort-method="comparePayerProcessingNum"
        width="80"
      ></el-table-column>

      <!--  状态-->
      <el-table-column align="center" label="状态" width="70" fixed="right">
        <template slot-scope="scope">
          <span :class="scope.row.payerCardStatus | payerCardStatusClass">{{
            scope.row.payerCardStatus | payerCardStatusFilter
          }}</span>
        </template>
      </el-table-column>
      <!-- <el-table-column
        align="center"
        prop="cloudRemark"
        label="云机"
        width="80"
      ></el-table-column> -->
      <el-table-column
        align="center"
        prop="remark"
        label="备注"
        width="109"
      ></el-table-column>

      <el-table-column align="center" label="操作" fixed="right" width="100">
        <template slot-scope="scope">
          <div>
            <el-button
              class="ml10"
              v-if="handelAuto('edit')"
              @click.native="m_editForm(scope.row)"
              type="text"
              >编辑</el-button
            >
          </div>
          <el-button
            class="ml10"
            v-if="handelAuto('edit') && scope.row.payerCardType == 0"
            @click.native="handelBalance(scope.row)"
            type="text"
            >银行卡充值</el-button
          >
          <div></div>
          <div>
            <el-button
              v-if="handelAuto('edit')"
              style="color: #ff0000"
              @click.native="handelDel(scope.row)"
              type="text"
              >删除</el-button
            >
          </div>
          <div>
            <el-button
              v-if="handelAuto('edit')"
              @click.native="handel_modifyStatus(scope.row)"
              type="text"
            >
              <span
                :class="scope.row.payerCardStatus | switchPayerCardStatusClass"
                >{{ scope.row.payerCardStatus > "0" ? "禁用" : "" }}</span
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
      :title="(m_isadd ? '添加' : '编辑') + '银行卡'"
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
          label="所属通道名称"
          label-width="100px"
          prop="cardChannelCode"
        >
          <el-select
            v-model="formData.cardChannelCode"
            placeholder="请选择银行卡所属的三方通道"
          >
            <el-option
              v-for="item in cardChannelCodeList"
              :key="item.cardChannelCode"
              :label="item.cardChannelName + '(' + item.remark + ')'"
              :value="item.cardChannelCode"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="银行名称" label-width="100px" prop="bankCode">
          <el-select v-model="formData.bankCode" placeholder="请选择所属银行" @change="showPayerMerchantId">
            <el-option
              v-for="item in bankCodeDataList"
              :key="item.bankCode"
              :label="item.bankName + '(' + bankTypeFilter(item.bankType) + ')'"
              :value="item.bankCode"
            ></el-option>
          </el-select>
        </el-form-item>

      <!-- 新增所属卡主 -->
        <el-form-item
          class="flex-1"
          label="所属卡主"
          label-width="100px"
          clearable
          prop="payerMerchantId"
          v-if="isShowPayerMerchantId == -1"
        >
        
         <el-select
          class="w280"
          size="medium"
          v-model="formData.payerMerchantId"
          placeholder="请选择所属卡主"
        >
          <el-option label="请选择所属卡主" value></el-option>
          <el-option
            v-for="item in queryMerchantIdList"
            :key="item.merchantId"
            :label="item.merchantId + '('+ item.merchantName+')'"
            :value="item.merchantId"
          ></el-option>
        </el-select>
        
        </el-form-item>

        <!-- 开户行所在地 -->
        <el-form-item
          class="flex-1"
          label="开户行所在地"
          label-width="100px"
          clearable
          prop="bankArea"
        >
          <el-input
            placeholder="开户行所在地"
            v-model="formData.bankArea"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>

        <!-- 猫池所在地 -->
        <!-- <el-form-item
          class="flex-1"
          label="猫池所在地"
          label-width="100px"
          clearable
          prop="bankCat"
        >
          <el-input
            placeholder="猫池所在地"
            v-model="formData.bankCat"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item> -->

        <!-- 持卡人 -->
        <el-form-item
          class="flex-1"
          label="持卡人"
          label-width="100px"
          clearable
          prop="payerName"
        >
          <el-input
            placeholder="持卡人"
            v-model="formData.payerName"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>
        <!-- 卡号 -->
        <el-form-item
          class="flex-1"
          label="卡号"
          label-width="100px"
          clearable
          prop="payerCardNo"
        >
          <el-input
            :disabled="!m_isadd"
            placeholder="不可再次修改 卡号间无空格"
            v-model="formData.payerCardNo"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>
        <!-- 卡号数字串 -->
        <!-- <el-form-item
          class="flex-1"
          label="卡号数字串"
          label-width="100px"
          clearable
          prop="payerCardCode"
        >
          <el-input
            type="number"
            placeholder="卡号数字串(兴业银行不可留空)"
            v-model="formData.payerCardCode"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item> -->

        <div class="flex flex-m">
          <el-form-item
            class="flex-1"
            label="卡状态"
            label-width="100px"
            clearable
            prop="payerCardStatus"
          >
            <el-select
              class="w140"
              size="medium"
              v-model="formData.payerCardStatus"
              placeholder="请选择"
            >
              <el-option label="请选择卡状态" value></el-option>
              <el-option
                v-for="item in payerCardStatusList"
                :key="item.value"
                :label="item.name"
                :value="item.id"
              ></el-option>
            </el-select>
          </el-form-item>

          <el-form-item
            class="flex-1"
            label="最高日限额"
            label-width="100px"
            clearable
            prop="dayQuota"
          >
            <el-input
              v-model="formData.dayQuota"
              clearable
              auto-complete="off"
            ></el-input>
          </el-form-item>
        </div>

        <div class="flex flex-m">
          <el-form-item
            class="flex-1"
            label="手机"
            label-width="100px"
            clearable
            prop="payerPhone"
          >
            <el-input
              placeholder="手机号"
              v-model="formData.payerPhone"
              class="w140"
              clearable
              auto-complete="off"
            ></el-input>
          </el-form-item>

          <el-form-item
            class="flex-1"
            label="卡种类"
            label-width="100px"
            clearable
            prop="payerCardType"
          >
            <el-select
              class="w120"
              size="medium"
              v-model="formData.payerCardType"
              placeholder="请选择"
            >
              <el-option label="请选择" value></el-option>
              <el-option
                v-for="item in payerCardTypeList"
                :key="item.value"
                :label="item.name"
                :value="item.id"
              ></el-option>
            </el-select>
          </el-form-item>
        </div>

        <div class="flex flex-m">
          <el-form-item
            class="flex-1"
            label="登录密码"
            label-width="100px"
            clearable
            prop="payerLoginPwd"
          >
            <el-input
              :placeholder="(m_isadd ? '登录密码' : '留空不修改') + ''"
              v-model="formData.payerLoginPwd"
              clearable
              auto-complete="off"
              class="w140"
              type="password"
            ></el-input>
          </el-form-item>

          <el-form-item
            class="flex-1"
            label="支付密码"
            label-width="100px"
            clearable
            maxlength="8"
            prop="payerPayPwd"
          >
            <el-input
              :placeholder="(m_isadd ? '支付密码' : '留空不修改') + ''"
              v-model="formData.payerPayPwd"
              clearable
              auto-complete="off"
              type="password"
            ></el-input>
          </el-form-item>
        </div>
        <!-- 设备 -->
        <el-form-item
          class="flex-1"
          label="设备ID"
          label-width="100px"
          clearable
          prop="deviceId"
        >
          <el-input
            placeholder="请输入"
            v-model="formData.deviceId"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>
        <el-form-item
          class="flex-1"
          label="设备IMEI"
          label-width="100px"
          clearable
          prop="deviceImei"
        >
          <el-input
            placeholder="请输入"
            v-model="formData.deviceImei"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>
        <el-form-item
          class="flex-1"
          label="硬件系列号"
          label-width="100px"
          clearable
          prop="deviceSerial"
        >
          <el-input
            placeholder="请输入"
            v-model="formData.deviceSerial"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>
        <!-- REMARK -->
        <div class="flex flex-m">
          <!-- <el-form-item
            class="flex-1"
            label="云机备注"
            label-width="100px"
            clearable
            prop="cloudRemark"
          >
            <el-input
              placeholder="云机备注"
              v-model="formData.cloudRemark"
              clearable
              auto-complete="off"
            ></el-input>
          </el-form-item> -->
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

    <!-- 银行卡充值  -->
    <el-dialog
      v-if="balance_show"
      title="银行卡充值"
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
            <el-form-item class="flex-1" label="通道编号" label-width="120px">
              <span>{{ balance_data.cardChannelCode }}</span>
            </el-form-item>

            <el-form-item class="flex-1" label="通道名称" label-width="120px">
              <span>{{ balance_data.cardChannelName }}</span>
            </el-form-item>
          </div>

          <div class="flex flex-m searchBox_bankcard">
            <el-form-item
              class="flex-1"
              label="通道充值费率"
              label-width="120px"
            >
              <span>{{ balance_data.chlRate }} %</span>
            </el-form-item>

            <el-form-item
              class="flex-1"
              label="单笔下发手续费"
              label-width="120px"
            >
              <span>{{ balance_data.chlSingle }} 元/笔</span>
            </el-form-item>
          </div>

          <div class="flex flex-m searchBox_bankcard">
            <el-form-item class="flex-1" label="持卡人" label-width="120px">
              <span>{{ balance_data.payerName }}</span>
            </el-form-item>

            <el-form-item class="flex-1" label="银行卡号" label-width="120px">
              <span>{{ balance_data.payerCardNo }}</span>
            </el-form-item>
          </div>

          <div class="flex flex-m searchBox_bankcard">
            <el-form-item class="flex-1" label="银行卡余额" label-width="120px">
              <span class="red">{{ balance_data.payerAmtRamin }}元</span>
            </el-form-item>
          </div>

          <el-form-item label="充值金额" label-width="120px" prop="payerAmt">
            <div class="flex flex-m">
              <el-input
                @mousewheel.native.prevent
                type="number"
                style="width: 110px"
                placeholder="充值金额"
                v-model="balance_data.payerAmt"
                auto-complete="off"
              ></el-input>
              <div class="red ml15 fs18 w100">
                {{ balance_data.payerAmt | convertCurrency }}
              </div>
            </div>
          </el-form-item>

          <el-form-item
            label="google验证码"
            label-width="120px"
            prop="googleCode"
          >
            <el-input
              placeholder="请输入"
              v-model="balance_data.googleCode"
              auto-complete="off"
            ></el-input>
          </el-form-item>

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

    <!-- GOOGLE密钥删除银行卡  -->
    <el-dialog
      v-if="merchant_show_del"
      title="删除银行卡"
      :visible.sync="merchant_show_del"
      width="500px"
    >
      <div v-loading="d_balance_loading">
        <el-form
          :model="merchant_data"
          :rules="cardPayerForm_rules"
          ref="delCardPayerForm"
        >
          <el-form-item label="持卡人" label-width="110px">
            <span>{{ merchant_data.payerName }}</span>
          </el-form-item>

          <el-form-item label="银行卡号" label-width="110px">
            <span>{{ merchant_data.payerCardNo }}</span>
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

    <!-- 查看令牌图片  -->
    <el-dialog
      v-if="show_camera_image"
      title="令牌实时图片"
      :visible.sync="show_camera_image"
      width="500px"
    >
      <div style="margin-top: -20px">
        <div>
          1.请保持令牌界面整洁<br />2.令牌界面应避免反光<br />
          3.令牌数字是否与网格线对齐<br />4.令牌是否有足够亮度,过暗则表示相机未开启“常亮”功能(相机左上角)<br />
        </div>
        <div><br /><img class="authurl" :src="authurl" alt /></div>
        <div><img class="authurl" :src="templurl" width="220" alt /></div>
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
  name: "cardMerchantPayerList",
  data() {
    return {
      bankCodeDataList: [], //银行列表
      cardChannelCodeList: [], //通道列表

      search_data: {
        cardChannelCode: "", //通道编号
        chlName: "", //商户状态
        payerName: "",
        payerCardStatus: "", // 邮箱
        payerCardNo: "", // 手机号
        payerCardType: "", //0 银行卡 1手机卡
        payerPhone: "", // 联系人
        bankArea: "",
        bankCat: "",
        remark: "",
        cloudRemark: "",
        tableMerchantId: "",
        payerMerchantId:"",//所属卡主
        payerBankType:"-1",// -1:手动人工 0:自动
        payerOnline:"",
      },
      highestAmt: 0,
      //预先 加载 渲染
      countMap: {
        sumPayerAmt: 0.0,
        countProcessingNum: 0,
      },
      queryCardChannelInfoList: [], //三方通道数组
      queryMerchantIdList:[],//卡主列表数组
      isShowPayerMerchantId:-1,//默认不显示卡主编号
      //新增表格
      formData: {
        cardChannelCode: "", //通道编号
        bankCode: "", //CCB建行 行联号 105100000017 
        bankArea: "",
        bankCat: "",
        dayQuota: "299999",
        payerMerchantId:"",//新增所属卡主
        payerName: "",
        payerCardNo: "", // 邮箱
        payerCardCode: "", //卡号数字串
        payerCardType: "0", //0 银行卡 1手机卡
        payerCardStatus: "2",
        payerPhone: "", // 手机号
        payerLoginPwd: "",
        payerPayPwd: "",
        deviceId: "",
        deviceImei: "",
        deviceSerial: "",
        cloudRemark: "",
        remark: "",
        tableMerchantId: "",
        googleCode: "",
        payerBankType:0,
      },

      merchant_data: {
        payerName: "",
        payerCardNo: "",
        tableMerchantId: "",
        googleCode: "",
      },

      show_camera_image: false, //camera image
      templurl: "",
      authurl: "",
      balance_show: false,
      d_balance_loading: false,
      balance_loading: false,
      merchant_show_del: false,
      _balance_data: {},

      balance_data: {
        merchantId: "", //管理员id
        cardChannelCode: "",
        cardChannelName: "",
        bankCode: "",
        bankName: "",
        remark: "", //备注
        chlRate: "", //费率
        chlSingle: "", //单下发手续费
        googleCode: "", //google验证码
      },

      merchant_show: false,
      merchant_show_del: false,
      agent_show: false, //代理费率
      merchant_loding: false,
      d_merchant_loding: false,
      _channel_data: {},

      channel_data: {
        chlContact: "", //联系人
        chlName: "", //通道名称
        cardChannelCode: "", //商户信息列表中的  商户号 (非登陆后接口返回的)
        channelCode: "", //通道编号
      },

      //银行卡验证规则
      m_rules_add: {
        bankCode: [{ required: true, message: "请选择", trigger: "blur" }],
        payerName: [{ required: true, message: "请输入", trigger: "blur" }],
        payerCardNo: [{ required: true, message: "请输入", trigger: "blur" }],
        payerPhone: [{ required: true, message: "请输入", trigger: "blur" }],
        payerLoginPwd: [{ required: true, message: "请输入", trigger: "blur" }],
        payerPayPwd: [{ required: true, message: "请输入", trigger: "blur" }],
        deviceId: [{ required: true, message: "请输入", trigger: "blur" }],
        deviceImei: [{ required: true, message: "请输入", trigger: "blur" }],
        deviceSerial: [{ required: true, message: "请输入", trigger: "blur" }],
      },

      m_rules_edit: {
        bankCode: [{ required: true, message: "请选择", trigger: "blur" }],
        payerName: [{ required: true, message: "请输入", trigger: "blur" }],
        payerCardNo: [{ required: true, message: "请输入", trigger: "blur" }],
        payerPhone: [{ required: true, message: "请输入", trigger: "blur" }],
        // payerLoginPwd: [{ required: true, message: "请输入", trigger: "blur" }],
        // payerPayPwd: [{ required: true, message: "请输入", trigger: "blur" }],
        deviceId: [{ required: true, message: "请输入", trigger: "blur" }],
        deviceImei: [{ required: true, message: "请输入", trigger: "blur" }],
        deviceSerial: [{ required: true, message: "请输入", trigger: "blur" }],
      },

      balanceForm_rules: {
        payerAmt: [{ required: true, message: "请输入", trigger: "blur" }],
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }],
      },

      cardPayerForm_rules: {
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }],
      },
    };
  },
  created() {
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
      this.m_isadd = true;
      this._balance_data = this._dep(this.balance_data);
      this._channel_data = this._dep(this.channel_data);
      this.getDatalist();
      this.queryCardChannelInfo(); //获取三方通道列表数据信息
      this.queryMerchantIdListInfo(); //获取卡主列表数据信息
      this.getBankCodeListFromServer();//获取银行卡列表
    },

    enterKey(event) {
      const code = event.keyCode;
      //32 space
      //16 shift
      //27 ESC
      console.log(code);
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
    },
    //获取银行列表
    getBankCodeListFromServer() {
      this.m_api.querybankCodeDataList({
        payerBankType:-1,
      }).then((res) => {
        this.bankCodeDataList = res.data;
      });
    },

    //获取通道列表
    getCardChannelCodeListFromServer() {
      console.log("获取通道列表");
      this.m_api.queryCardChannelCodeDataList({}).then((res) => {
        this.cardChannelCodeList = res.data;
      });
    },

    //获取三方通道列表
    queryCardChannelInfo() {
      this.m_api.queryCardChannelInfo({}).then((res) => {
        this.queryCardChannelInfoList = res.data;
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

    //获取银行卡数据信息
    getDatalist() {
      //console.log("查询" + JSON.stringify(this.search_data))
      this.m_tableLoading = true;
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.search_data.tableMerchantId = userData.merchantId
      let data = Object.assign(this.search_data, this.m_page);
      this.m_api
        .queryCardPayerInfoPage(data)
        .then((res) => {
          this.m_list = res.data.pages.records; //记录
          this.m_page.total = res.data.pages.total;
        })
        .finally(() => {
          this.m_tableLoading = false;
          this.m_checkData = [];
        });

      this.getCardPayerHighestAmt(this.search_data); //最高单笔
      this.getCardPayerCountMap(this.search_data);
    },

    //提交服务器 加入数据
    addData() {
      //按钮只可点击一次
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.formData.tableMerchantId = userData.merchantId;
      this.m_loading = true;
      this.m_api
        .registerCardPayer(this.formData)
        .then((res) => {
          this.m_page.page = 1;
          this.m_success("添加银行卡成功");
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
 
      this.isShowPayerMerchantId = -1;
      this.getBankCodeListFromServer(); //获取银行卡列表
      this.getCardChannelCodeListFromServer(); //获取通道列表数据信息
      this.queryMerchantIdListInfo(); //重新获取
      
      // let cardChannelCode = this.formData.cardChannelCode != null ? this.formData.cardChannelCode : "";
      this.m_isadd = true; //m_isadd?'添加':'编辑'
      this.formData = JSON.parse(JSON.stringify(this._formData));
      this.m_show = true;
    },

    //编辑通道overwrited index.js method
    m_editForm(row) {
      
      this.getBankCodeListFromServer(); //获取银行卡列表
      this.getCardChannelCodeListFromServer(); //获取通道列表数据信息
      this.queryMerchantIdListInfo(); //重新获取

      this.formData = JSON.parse(JSON.stringify(this._formData));
      this.m_isadd = false;

      this.m_editData = JSON.parse(JSON.stringify(row));
      this.formData = this.m_utils.coverObj(this.formData, row);
      this.formData.cardChannelCode = row.cardChannelCode; //下拉选择框
      this.m_show = true;
      this.isShowPayerMerchantId = -1; //人工打款显示卡主
      
    },

    //编辑 send to server
    editData() {
      this.m_loading = true;
      let form = this._dep(this.formData);
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      form.id = this.m_editData.id;
      form.tableMerchantId = userData.merchantId;
      //form.parentId = this.m_editData.parentId;
      //修改post
      this.m_api
        .modifyCardPayer(form)
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
      this.$refs["delCardPayerForm"].validate((valid) => {
        if (valid) {
          this.m_loading = true;
          let userData =
            (sessionStorage.userData && JSON.parse(sessionStorage.userData)) ||
            {};
          var tableMerchantId = userData.merchantId; //cookie中读取操作管理员的商户id
          this.m_loading = true;
          this.m_api
            .delCardPayerInfo({
              payerCardNo: v.payerCardNo,
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
          let ids = m_checkData.map((i) => i.cardChannelCode).join(",");
          // this.delData(row.id, row)
        })
        .catch(() => {
          //取消删除
        });
    },

    handel_modifyStatus(row) {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      let status = row.payerCardStatus;
      let txt = "";
      let data = {
        payerCardNo: row.payerCardNo,
        status: "",
        merchantId: userData.merchantId, //cookie中读取操作管理员的商户id
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
        msg: `是否确定 ${txt} 卡 ${row.payerCardNo} ？`,
      };
      this.m_confirm(msgData).then((res) => {
        if (res) {
          this.modifyCardPayerStatus(data);
        }
      });
    },

    //最高的银行卡余额
    getCardPayerHighestAmt() {
      this.m_api.getCardPayerHighestAmt(this.search_data).then((res) => {
        this.highestAmt = res.data.highestAmt;
      });
    },

    //银行卡总余额 下发金额 总笔数 排队中
    getCardPayerCountMap(data) {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.search_data.tableMerchantId = userData.merchantId;

      //查找服务器
      this.m_api.getCardPayerCountMap(this.search_data).then((res) => {
        this.countMap = res.data || 0;
      });
    },

    //
    modifyCardPayerStatus(data) {
      this.m_api.modifyCardPayerStatus(data).then((res) => {
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
      let ids = m_checkData.map((item) => item.payerCardNo).join(",");
      var txt = "";
      if (status == "1") {
        txt = "启用";
      }
      if (status == "0") {
        txt = "禁用";
      }
      let formObj = {
        status,
        payerCardNos: ids, //cardChannelCode 列表
        tableMerchantId: userData.merchantId,
      };

      this.m_confirm({ msg: `是否确 批量 ${txt} 银行卡` }).then((res) => {
        if (res) {
          this.m_api.batchModifyCardPayerInfoStatus(formObj).then((res) => {
            this.m_success("操作成功");
            this.getDatalist();
          });
        }
      });
    },

    //查询相机是否正常
    checkCameraHeartBeat(row) {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.m_api
        .checkCameraHeartBeat({
          phoNum: row.payerPhone,
          tableMerchantId: userData.merchantId,
        })
        .then((res) => {
          // console.log("camera:" + JSON.stringify(res));
          // this.card_channel_info = this._dep(res.data);
          // this.balance_data.chlRate = res.data.chlRate;
          // this.balance_data.chlSingle = res.data.chlSingle;
          // this.d_balance_loading = false;
          this.m_success("心跳正常");
        })
        .catch((err) => {
          //console.log(err);
          //console.log("res:" + JSON.stringify(res.code));
          //this.m_error("心跳异常");
        });
    },

    //查询相机拍摄的令牌图片
    checkCameraImage(row) {
      //this.authurl = "https://t1.upload.30pay.info/token/IMG_20201125_082908.jpg.png";
      //this.templurl = "https://t1.upload.30pay.info/templ/templ.png";
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.m_api
        .checkCameraImage({
          phoNum: row.payerPhone,
          tableMerchantId: userData.merchantId,
        })
        .then((res) => {
          this.show_camera_image = true;
          this.authurl =
            "https://t1.upload.30pay.info/token/" + res.data.cameraImage;
          console.log(res);
        })
        .catch((err) => {
          this.show_camera_image = false;
        });
    },
    //查询余额
    handelBalance(row) {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.balance_data = this._dep(this._balance_data);
      this.balance_data.cardChannelName = row.cardChannelName;
      this.balance_data.cardChannelCode = row.cardChannelCode;

      this.balance_data.payerAmtRamin = row.payerAmt; //当前手续费余额

      this.balance_data.payerName = row.payerName;
      this.balance_data.payerCardNo = row.payerCardNo;
      this.balance_data.bankCode = row.bankCode;
      this.balance_data.bankName = row.bankName;
      this.balance_data.merchantId = userData.merchantId; //cookie中读取操作管理员的商户id

      this.balance_show = true;
      this.d_balance_loading = true;
      //银行卡通道信息
      this.m_api
        .queryRateByCardChannelCode({ cardChannelCode: row.cardChannelCode })
        .then((res) => {
          //  console.log("银行卡通道信息:" + res.data.chlRate);
          // this.card_channel_info = this._dep(res.data);
          this.balance_data.chlRate = res.data.chlRate;
          this.balance_data.chlSingle = res.data.chlSingle;
          this.d_balance_loading = false;
        })
        .catch((err) => {
          this.d_balance_loading = false;
        });
    },

    balanceSubmit() {
      this.$refs["balanceForm"].validate((valid) => {
        if (valid) {
          this.balance_loading = true;
          this.m_api.cardPayerRechargeBalance(this.balance_data)
            .then((res) => {
              this.getDatalist();  

              if (res.data.payerAmt !=null && res.data.payerAmt != "") {
                var txt = "";
                txt = "银行卡充值【" + parseFloat(res.data.payerAmt).toFixed(2) + "】成功";
                if (res.data.isMerchantRecharge !=null && res.data.isMerchantRecharge == 1) { 
                     txt += ",并已自动充值商户【"+res.data.merchantName+"】余额";
                 }
                txt += ",请核对充值流水";
                this.m_success(txt);
              }  
            })
            .catch((error) => {
              this.m_error("银行卡充值失败,请检查【银行卡充值流水】" + error);
              
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
      this.merchant_data.payerCardNo = row.payerCardNo;
      this.merchant_data.googleCode = "";
      this.d_balance_loading = false;
      this.merchant_show_del = true;
    },

    bankTypeFilter(v) {
      if (v == "0") {
        return "自动";
      } else if (v == "-1") {
        return "人工";
      }
    },
    comparePayerAmt(a, b) {
      return a.payerAmt - b.payerAmt;
    },
    comparePayerWithdrawAmtDay(a, b) {
      return a.payerWithdrawAmtDay - b.payerWithdrawAmtDay;
    },
    comparePayerWithdrawNumDay(a, b) {
      return a.payerWithdrawNumDay - b.payerWithdrawNumDay;
    },
    comparePayerWithdrawFailNumDay(a, b) {
      return a.payerWithdrawFailNumDay - b.payerWithdrawFailNumDay;
    },
    comparePayerProcessingNum(a, b) {
      return a.payerProcessingNum - b.payerProcessingNum;
    },
    //下拉选择框
    showPayerMerchantId(index){
       this.isShowPayerMerchantId = -1;
       this.formData.payerBankType = -1;
      // if (index == '104100000004' || index == '105100000017' || index == '106980096336'){
      //     this.isShowPayerMerchantId =0;
      //     this.formData.payerBankType = 0;
      // }else {
      //     this.isShowPayerMerchantId = -1;
      //     this.formData.payerBankType = -1;
      // }
     
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
    width:525px;
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
