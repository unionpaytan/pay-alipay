<template>
  <div class="login-container">
    <el-form
      v-if="!findPwdShow"
      class="card-box login-form"
      autocomplete="on"
      :model="loginForm"
      :rules="loginRules"
      ref="loginForm"
      label-position="left"
    >
      <div v-if="nextShow">
        <div align="center">
          <img src="../../assets/img/avatar.png" height="80" width="80" style="border-radius:5px;" alt />
        </div>
        <h3 class="title">聚合系统<span style="font-size:13px;"></span></h3>
        <el-form-item prop="loginName">
          <span class="pl10 c iconfont icon-denglu"></span>
          <el-input
            name="name"
            type="text"
            v-model="loginForm.loginName"
            autocomplete="on"
            placeholder="登录帐户"
            value=""
          />
        </el-form-item>

        <el-form-item prop="password">
          <span class="pl10 c iconfont icon-mima"></span>
          <el-input
            name="password"
            :type="pwdType"
            v-model="loginForm.password"
            autocomplete="on"
            placeholder="6 - 16位密码，区分大小写"
            value=""
          />
        </el-form-item>

        <!-- <el-form-item prop="loginName">
          <span class="pl10 c el-icon-edit"></span>
          <el-input
            type="number"
            maxlength="6"
            v-model="googleCode"
            autocomplete="off"
            placeholder="google谷歌验证码"
            oninput="javascript: if (this.value.length > this.maxLength) this.value = this.value.slice(0, this.maxLength);"
            @mousewheel.native.prevent
          />
        </el-form-item> -->
      </div>

      <div>
        <div class="tc mt20">
          <el-button @click="sureLogin" :loading="sure_loading" type="primary"
            >登录</el-button
          >
        </div>
 
      </div>
    </el-form>
  </div>
</template>

<script>
import { mapActions, mapGetters } from "vuex";
import findPwd from "./findPwd";
import addRouter from "../../router/authority/addRouter.js";

export default {
  components: { findPwd },
  name: "login",
  data() {
    return {
      that: this,
      findPwdShow: false,
      nextShow: true,
      loginForm: {
        loginName: "",
        password: "",
      },

      googleCode: "", //google验证码
      mail: "", //邮箱
      loginRules: {
        loginName: [
          { required: true, trigger: "blur", message: "请输入登录帐户" },
        ],
        password: [{ required: true, trigger: "blur", message: "请输入密码" }],
        googleCode: [
          { required: true, trigger: "blur", message: "google验证码" },
        ],
      },
      pwdType: "password",
      loading: false,
      sure_loading: false,
      get_loading: false,
      showDialog: false,

      sendNum: 0,
      time: null,
    };
  },
  created() {
    this.loginForm.loginName = localStorage.loginName || "";
  },
  destroyed() {
    this.time && clearInterval(this.time);
  },
  methods: {
    ...mapActions(["setRouterList", "setViewTagList", "GenerateRoutes"]),
    showPwd() {
      if (this.pwdType === "password") {
        this.pwdType = "";
      } else {
        this.pwdType = "password";
      }
    },

    /*  handleLogin() {
                this.$refs.loginForm.validate(valid => {
                  if (valid) {
                    this.loading = true
                    return this.m_api.login(this.loginForm).then(res => {
                      //去除邮箱
                      this.mail = res.data.mail
                      this.nextShow = true
                      this.loading = false
                      return res
                    }).catch(res => {
                      this.loading = false
                    })
                    //addRouter('lever_1','系统')
                    //this.$router.push('index')
                  } else {
                    console.log('error submit!!')
                    return false
                  }
                })
              },*/

    /*    sendCodnTime() {
                  this.time && clearInterval(this.time)
                  this.sendNum = 60
                  this.time = setInterval(() => {
                    this.sendNum = this.sendNum - 1
                    if (this.sendNum <= 0) {
                      this.sendNum = 0
                      clearInterval(this.time)

                    }
                  }, 1000)
                },*/

    /*
                sendCode() {
                  this.get_loading = true
                  this.m_api.login(this.loginForm).then(res => {
                    this.mail = res.data.mail
                    this.get_loading = false

                    this.m_success('重新发送成功')
                    this.sendCodnTime()
                    return res
                  }).catch(res => {
                    this.get_loading = false
                  })
                },
            */

    sureLogin() {
      if ((this.googleCode + "").length < 6) {
        this.m_error("google谷歌验证码不能为空或者小于6位");
      } else {
        let data = {
          googleCode: this.googleCode,
          loginName: this.loginForm.loginName,
          password: this.loginForm.password,
        };
        this.sure_loading = true;
        this.m_api
          .confirmLogin(data)
          .then((res) => {
            this.sure_loading = false;
            sessionStorage.token = res.data.token;
            //会员等级
            let type = res.data.accountType;
            let agentRate = res.data.agentRate; //0 普通 ｜ 1代理 ｜ 2总代

            localStorage.loginName = this.loginForm.loginName;
            sessionStorage.userData = JSON.stringify(res.data);
            // console.log("type:"+type+",agentRate",agentRate);
            let sysName = "系统";
            if (type == "admin" || type == "administrator") {
              sysName = "聚合系统";
              sessionStorage.permissionInfo = JSON.stringify(
                res.data.permissionInfo
              );
              addRouter(type, sysName);
            } else if (type == "merchant" && agentRate == 0) {
              sysName = "聚合系统商户平台";
              addRouter(type, sysName);
            } else if (type == "merchant" && agentRate > 0) {
              sysName = "聚合系统代理平台";
              addRouter("agent", sysName);
            } else if (type == "merchant" && agentRate == "-1") {
              sysName = "聚合系统码商平台";
              addRouter("card", sysName); 
            }else if (type == "merchant" && agentRate == "-2") {
               sysName = "聚合系统码商操作员平台";
              addRouter("operator", sysName); 
            }else if (type == "merchant" && agentRate == "-3") {
               sysName = "Amazon助手";
              addRouter("follower", sysName); 
            }

            document.title = sysName;

            this.$router.push("/index/home");
          })
          .catch((err) => {
            this.sure_loading = false;
          });
      }
    },
  },
  computed: {},
};
</script>

<style rel="stylesheet/scss" lang="scss">
.c {
  color: #889aa4;
}

@import "../../assets/scss/mixin.scss";
$bg: #2d3a4b;
$dark_gray: #889aa4;
$light_gray: #eee;
$strong_gray: #2d3a4b;

.m_text {
  color: #fff;
  margin-top: 40px;
}

.c1 {
  color: #eee;
}

.findPwd {
  width: 700px;
  margin: 0 auto;
}

@media screen and (max-width: 2500px) {
  /*当屏幕尺寸小于600px时，应用下面的CSS样式*/
  .login-form {
    position: absolute;
    left: 0;
    right: 0;
    width: 400px;
    padding: 35px 35px 15px 35px;
    margin: 180px auto;
  }

  .login-container {
    @include relative;
    height: 100vh;
    background-color: $bg;
    input:-webkit-autofill {
      -webkit-box-shadow: 0 0 0px 1000px #293444 inset !important;
      -webkit-text-fill-color: #fff !important;
    }

    input {
      background: transparent;
      border: 0px;
      -webkit-appearance: none;
      border-radius: 0px;
      padding: 12px 5px 12px 15px;
      color: $light_gray;
      height: 47px;
    }

    .el-input {
      display: inline-block;
      height: 47px;
      width: 85%;
    }

    .tips {
      font-size: 14px;
      color: #fff;
      margin-bottom: 10px;
    }

    .svg-container {
      padding: 6px 5px 6px 15px;
      color: $dark_gray;
      vertical-align: middle;
      width: 30px;
      display: inline-block;

      &_login {
        font-size: 20px;
      }
    }

    .title {
      font-size: 26px;
      font-weight: 400;
      color: $light_gray;
      margin: 0px auto 40px auto;
      text-align: center;
      font-weight: bold;
    }

    .el-form-item {
      border: 1px solid rgba(255, 255, 255, 0.1);
      background: rgba(0, 0, 0, 0.1);
      border-radius: 5px;
      color: #454545;
      width: 100%;
    }

    .show-pwd {
      position: absolute;
      right: 10px;
      top: 7px;
      font-size: 16px;
      color: $dark_gray;
      cursor: pointer;
      user-select: none;
    }

    .thirdparty-button {
      position: absolute;
      right: 35px;
      bottom: 28px;
    }
  }
}

@media screen and (max-width: 414px) {
  /*当屏幕尺寸小于600px时，应用下面的CSS样式*/
  .login-form {
    position: absolute;
    left: 0;
    right: 0;
    width: 310px;
    padding: 35px 35px 15px 35px;
    margin: 180px auto;
  }

  .login-container {
    @include relative;
    height: 100vh;
    background-color: #ffffff;
    input:-webkit-autofill {
      -webkit-box-shadow: 0 0 0px 1000px #293444 inset !important;
      -webkit-text-fill-color: #fff !important;
    }

    input {
      background: transparent;
      border: 0px;
      -webkit-appearance: none;
      border-radius: 0px;
      padding: 12px 5px 12px 15px;
      color: $strong_gray;
      height: 47px;
    }

    .el-input {
      display: inline-block;
      height: 47px;
      width: 85%;
    }

    .tips {
      font-size: 14px;
      color: #fff;
      margin-bottom: 10px;
    }

    .svg-container {
      padding: 6px 5px 6px 15px;
      color: $dark_gray;
      vertical-align: middle;
      width: 30px;
      display: inline-block;

      &_login {
        font-size: 20px;
      }
    }

    .title {
      font-size: 26px;
      font-weight: 400;
      color: $light_gray;
      margin: 0px auto 40px auto;
      text-align: center;
      font-weight: bold;
      color: $strong_gray;
    }

    .el-form-item {
      border: 1px solid rgba(255, 255, 255, 0.1);
      background: rgba(0, 0, 0, 0.1);
      border-radius: 5px;
      color: #454545;
      width: 100%;
    }

    .show-pwd {
      position: absolute;
      right: 10px;
      top: 7px;
      font-size: 16px;
      color: $dark_gray;
      cursor: pointer;
      user-select: none;
    }

    .thirdparty-button {
      position: absolute;
      right: 35px;
      bottom: 28px;
    }
  }
}
@media screen and (max-width: 375px) {
  /*当屏幕尺寸小于600px时，应用下面的CSS样式*/
  .login-form {
    position: absolute;
    left: 0;
    right: 0;
    width: 270px;
    padding: 35px 35px 15px 35px;
    margin: 180px auto;
  }
}

@media screen and (max-width: 320px) {
  /*当屏幕尺寸小于600px时，应用下面的CSS样式*/
  .login-form {
    position: absolute;
    left: 0;
    right: 0;
    width: 240px;
    padding: 35px 35px 15px 35px;
    margin: 180px auto;
  }
}
</style>
