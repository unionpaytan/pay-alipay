<!-- by your name -->
<template>
  <div class="main">
    <div id="sidebar" class="sidebar" :style="{width:sidebarWidth,display:menuShow}">
      <div class="topLogo flex flex-m">
        <img :src="imgSrc" />
      </div>
      <el-menu :unique-opened="true" :default-active="$route.path" class="el-menu-vertical-demo"  text-color="#fff" active-text-color="rgb(64, 158, 255)" background-color="#304156" :collapse="isCollapse">
        <div v-for="(item ,index) in menuList" :key="index">
          <div v-if="!item.children.length">
            <el-menu-item :index="item.path" @click.native="pushRouter({name:item.name,path:item.path})">
              <!-- <i class="el-icon-setting"></i> -->
              <img class="meun_icon" src="../assets/img/meun_icon.png" />
              <span slot="title">{{item.name}}</span>
            </el-menu-item>
          </div>

          <div v-else>
            <el-submenu :index="'m'+index">
              <template slot="title">
                <!-- <i class="el-icon-setting"></i> -->
                <img class="meun_icon" src="../assets/img/meun_icon.png" />
                <span>{{item.name}}</span>
              </template>
              <div @click="showArrowIcon(item.children)" v-for="(citem , cindex) in item.children" :key="'a'+cindex">
                <el-menu-item-group @click.native="pushRouter({name:citem.name,path:citem.path})">
                  <el-menu-item :index="citem.path">{{citem.name}}</el-menu-item>
                </el-menu-item-group>
              </div>

            </el-submenu>
          </div>
        </div>
      </el-menu>
    </div>

    <div class="content" :style="{'margin-left':sidebarWidth}">
      <!-- 悬浮球 -->
      <div><FloatBall :text="'菜单'"></FloatBall></div>
      <!-- <div><Roll  :text="'↓'"></Roll></div> -->
      <div class="topBar">
        <el-button
          @click="isCollapse=!isCollapse"
          type="text"
          class="iconfont icon-other collapse fl"
        ></el-button>
        <el-breadcrumb separator-class="el-icon-arrow-right" class="fl topBar-title">
          <!-- <el-breadcrumb-item :to="{ path: '/index/index' }">管理平台</el-breadcrumb-item>  -->
          <el-breadcrumb-item v-if="$route.matched.length">{{$route.matched[0].name}}</el-breadcrumb-item>
          <el-breadcrumb-item v-for="(item ,index ) in handelPathName()" v-show="item" :key="index" class="no-redirect">{{item}}
          </el-breadcrumb-item>
        </el-breadcrumb>

        <div class="fr mr20 up">
          <el-button @click="signOut" style="padding: 6px 20px;margin:11px 10px 11px 0;" class="fr">退出登录</el-button>
        </div>
      <!-- <el-dropdown class="fr" @command="handleCommand">
          <el-button icon="el-icon-setting" class="fr btn-loading" size="medium"></el-button>
          <el-dropdown-menu slot="dropdown">
           &lt;!&ndash; <el-dropdown-item command="a">
              <b>修改密码</b>
            </el-dropdown-item>&ndash;&gt;
            <el-dropdown-item command="b">
              <b>退出登录</b>
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
        <el-button @click="signOut" style="padding: 6px 20px;margin:11px 10px 11px 0;" class="fr">退出登录</el-button>
        <el-button :loading="loading" v-if="loading" size="medium" class="fr btn-loading"></el-button>-->
      </div>
      <div class="viewTagBox">
        <div class="viewTag">
          <el-tag v-for="(tag,index) in viewTagList" :key="tag.name" closable :class="{active:tag.path==$route.path}" @close="handleClose(index,tag.path)" @click.native="jump(tag.path)">{{tag.name}}</el-tag>
        </div>
      </div>

      <el-dialog class="editpass" :visible.sync="editpassstate" width="400px" center>
        <div style="height:100%">
          <el-scrollbar style="height:100%;" ref="scrollbar">
            <div element-loading-text="加载中">
              <el-form :model="passform" label-width="100px" label-position="left" :rules="passformrules" ref="passform">
                <el-row :gutter="20" style="margin-left:0;margin-right:0;">
                  <el-col :span="24">
                    <el-form-item label="旧密码" prop="old_password">
                      <el-input v-model="passform.old_password" placeholder="旧密码"></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="24">
                    <el-form-item label="新密码" prop="password">
                      <el-input type="password" v-model="passform.password" placeholder="新密码"></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="24">
                    <el-form-item label="重复新密码" prop="confirm">
                      <el-input type="password" v-model="passform.confirm" placeholder="重复新密码"></el-input>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </div>
          </el-scrollbar>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button size="medium" @click="editpassword" type="primary" :loading="loading">确认</el-button>
          <el-button size="medium" @click="editpassstate = false">取 消</el-button>
        </span>
      </el-dialog>

      <div class="routerView"> 
        <transition name="el-fade-in-linear"> 
          <router-view class="routerView_main" v-show="show"></router-view>
        </transition>
      </div>
    </div>

    <div class="footer" :style="{'padding-left':sidebarWidth}"><a target="_blank" href="https://t.me/pauItan" title="技术支持"><span style="color:#777777;">Copyright {{bottomTxt}} © 2022 - 2024</span></a> &nbsp;</div>
  </div>
</template>

<script>
//import menuList from './menuList.js'
import { mapState, mapGetters, mapActions } from 'vuex'
import * as api from '@/api/login'
import router from '@/router/index'
export default {
  name: 'index',
  data() {
    return {
      bottomTxt:"",
      imgSrc:"",
      menuList: [],
      show: true,
      isCollapse: false,
      menuShow:'',
      sidebarWidth: '200px',
      editpassstate: false,
      passform: {
        old_password: '',
        password: '',
        confirm: ''
      },
      passformrules: {
        old_password: [{ required: true, message: '请输入旧密码' }],
        password: [{ required: true, message: '请输入新密码' }],
        confirm: [{ required: true, message: '请再次输入新密码' }]
      }
    }
  },
  created() {
    this.menuList = JSON.parse(sessionStorage.menu)
    this.init();
  },
  beforeRouteLeave(to, from, next) {
    //  console.log(from,to,123123)

    next()
  },
  mounted() {
    
  },
  methods: {
    init(){ 
       //this.intervalQueryNewOrder(); //页面打开初始化时就同步一次;
       //this.queryNewOrder();
       let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
       this.bottomTxt = "技术支持" 
       this.imgSrc = (userData.agentRate == "-3") ? '/upload/title.png' : '/upload/title.png';
    },
    ...mapActions(['setViewTagList']),
    //导航栏跳转
    pushRouter(val) {
      this.jump(val.path)
      if (this.viewTagList.filter(t => t.path == val.path).length == 0) {
        this.viewTagList.push({ 'name': val.name, 'path': val.path })
        this.setViewTagList(this.viewTagList)
      }
    },
    jump(path) {
      if (path !== this.$route.path) {
        this.$router.push(path)
      }
    },
    //tag 关闭
    handleClose(index, path) {
      console.log("close:",index,path);
      this.viewTagList.splice(index, 1)
      this.setViewTagList(this.viewTagList)
      if (this.viewTagList.length !== 0) {
        if (this.$route.path == path) {
          this.$router.push(this.viewTagList[this.viewTagList.length - 1].path)
        }
      } else {
        this.$router.push('/index/home')
      }
    },
    signOut() {
      this.$confirm('是否确认退出登录?', '提示', {
        iconClass: "el-icon-question",//自定义图标样式
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(_ => {
        let userData  = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
        let uri =  (userData.agentRate == "-3") ? '/amazon' : '/login';
        sessionStorage.clear()
        this.$router.push(uri)
        setTimeout(() => {
          window.location.reload()
        }, 300)
      }).catch(_ => { })
    },
    editpassword() {
      this.$refs['passform'].validate((valid) => {
        if (valid) {
          if (this.passform.password !== this.passform.confirm) {
            this.$message({
              message: '重复新密码不正确',
              type: 'warning'
            })
            return
          }
          api.editpassword(this.passform).then(res => {
            this.editpassstate = false
            this.$message({
              message: '修改成功',
              type: 'success'
            })
          }).catch(() => { })
        } else {
          return false
        }
      })
    },
    handleCommand(command) {
      if (command == 'a') {
        this.passform = {
          old_password: '',
          password: '',
          confirm: ''
        }
        this.editpassstate = true
      } else if (command == 'b') {
        this.signOut()
      }
    },

    handelPathName() {
      let m = this.menuList
      let path = this.$route.path
      let arr = []
      //  console.log(path )
      for (let i = 0; i < m.length; i++) {

        if (m[i].path == path) {
          arr.push(m[i].name)
          return arr
        }
        m[i].children.forEach(el => {
          if (el.path == path) {
            arr.push(m[i].name)
            arr.push(el.name)
          }
        })
      }
      return arr
    },

    //每1分钟检查是否有新订单
    intervalQueryNewOrder() {
      setInterval(() => {
        //this.queryNewOrder();
      }, 1000 * 15 * 1);
    }, 
    //获取实时成功交易总金额
    queryNewOrder(){
         let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
         if (userData.accountType == 'merchant' && (userData.agentRate == "-3" || userData.agentRate == "-2" || userData.agentRate == "-1")) {
            //console.log("卡主来单提醒");
            this.m_api.queryNewOrder({ 
              merchantId: userData.merchantId, 
            })
            .then((res) => { 
              if (res != null && res !== 'undefined' && res.data.newOrder == "1"){
                console.log("有新订单");
                let audioUrl = "/neworder.mp3"; //public路径
                var audio = new Audio(audioUrl);
                audio.currentTime = 0;
                audio.play();
              } 

            })
            .catch((error) => { 
                 
            }).finally(()=>{
              
            });
         }else {
           //console.log("no music");
          
         }
         
    },
    showArrowIcon(v){
          // console.log("index menu - ",v)
          // var arrowIcon = document.getElementById('arrowIcon');
          // arrowIcon.style.display = 'block';
    }

  },
  watch: {
    isCollapse(v) { 
      console.log(v);
      this.sidebarWidth = v ? '0' : '200px' + ' !important'
      this.menuShow = v ? 'none' : ''; 
      console.log("isCollapse")
      //this.$refs.dragIcon.style.display = 'none';
       var arrowIcon = document.getElementById('arrowIcon');
       console.log("arrowIcon",arrowIcon);
       v ? arrowIcon.style.display = 'none' : arrowIcon.style.display = 'block';
    },
    $route(val) {
      this.pushRouter(val)
    }
  },
  computed: {
    ...mapState(['loading', 'routerList', 'viewTagList']),
    ...mapGetters(['loading', 'routerList', 'viewTagList'])
  },
  //  methods: { 
  //     showArrowIcon(){
         
  //     }
  //  }
}
</script>
<style lang='scss' scoped>
.main {
  position: relative;
  height: 100%;
  width: 100%;
  background: #eee;
}
.main .icon {
  font-size: 21px;
  margin-right: 5px;
  width: 24px;
  vertical-align: middle;
}
.main a {
  text-decoration: none;
}
.sidebar {
  transition: width 0.28s;
  width: 180px;
  height: 100%;
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 1001;
  background-color: #304156;
  box-sizing: border-box;
  display:'';
  overflow-y: auto;
}

.sidebar::-webkit-scrollbar {
  width:10 px;
}
.sidebar::-webkit-scrollbar-track {
  background: #2c3e50; /* 滚动条的背景颜色 */
}

.sidebar::-webkit-scrollbar-thumb {
  background-color: #1abc9c; /* 滚动条滑块的颜色 */
  border-radius: 10px; /* 滑块的圆角半径 */
  border: 2px solid #2c3e50; /* 滑块的边框 */
}

.sidebar::-webkit-scrollbar-thumb:hover {
  background-color: #16a085; /* 滑块悬停时的颜色 */
}

.footer {
  height: 40px;
  width: 100%;
  // background: #f3f3f4;
  background: #fff;
  z-index: 1000;
  position: fixed;
  bottom: 0;
  line-height: 40px;
  text-align: right;
  font-family: "ArialMT", "Arial";
  color: #797979;
  font-size: 13px;
  box-sizing: border-box;
}

.content {
  min-height: 100%;
  -webkit-transition: margin-left 0.28s;
  transition: margin-left 0.28s;
  margin-left: 180px;
  background: #eee;
  padding-bottom: 40px;
  display:'';
}

.topBar {
  line-height: 50px;
  height: 50px;

  // border-bottom: solid 1px #e6e6e6;
}

.collapse {
  font-size: 25px;
  color: #000;
  padding-left: 10px;
}

.topBar-title {
  color: #97a8be;
  font-size: 14px;
  padding-left: 20px;
  line-height: 50px;
}

.btn-loading {
  margin-top: 5px;
  margin-right: 10px;
  font-size: 25px;
  padding: 5px 10px !important;
}
.routerView {
  box-sizing: border-box;
  padding: 0 10px 10px 10px;
}
.routerView_main {
  padding: 10px;
  background: #fff;
  box-sizing: border-box;
  width: 100%;
  min-height: 80vh;
}
.viewTagBox {
  padding: 0 10px;
  box-sizing: border-box;
}
.viewTag {
  background: #fff;
  min-height: 30px;
  padding: 1px 0 5px 10px;
  border-bottom: 1px solid #d8dce5;
  // -webkit-box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.12),
  //   0 0 3px 0 rgba(0, 0, 0, 0.04);
  // box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.12), 0 0 3px 0 rgba(0, 0, 0, 0.04);
}

.el-submenu .el-menu-item {
  height: 45px;
  line-height: 45px;
}

.el-menu-item{
    height: 50px;
    line-height: 50px; 
}

.el-submenu__title {
    height: 50px;
    line-height: 50px;
}

.el-tag {
  cursor: pointer;
  margin-left: 15px;
  display: inline-block;
  position: relative;
  height: 25px;
  line-height: 25px;
  border: 1px solid #d8dce5;
  color: #495060;
  background: #fff;
  padding: 0 8px;
  font-size: 12px;
  margin-left: 5px;
  margin-top: 5px;
}
.el-tag.active {
  background-color: #42b983;
  color: #fff;
  border-color: #42b983;
}
.topLogo {
  height: 65px;
  width: 100%;
  background-color: rgba(0, 40, 77, 1);

  img {
    display: block;
    width: 139px;
    height: auto;
    margin: 0 auto;
  }
}

.meun_icon {
  display: inline-block;
  width: 16px !important;
  height: auto !important;
  margin-right: 7px;
}
</style>
<style >
.main .no-redirect .el-breadcrumb__inner {
  color: #606266;
  font-weight: 500;
  cursor: text;
}
.editpass .el-dialog.is-fullscreen.el-dialog--center {
  height: 400px;
  top: 25%;
}
@media screen and (max-width: 414px) {
  .el-message-box__btns{
       width:330px;
   }
    .up{
     margin-top: -7px;
   }
}

@media screen and (max-width: 375px) {
  .el-message-box__btns{
       width:330px;
   }
   .up{
     margin-top: -7px;
   }
}
</style>
