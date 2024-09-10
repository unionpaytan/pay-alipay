"use strict";
import Vue from 'vue';
import VueClipboard from 'vue-clipboard2'
import VueI18n from 'vue-i18n'
import App from './App.vue'
import VueFlex from "vue-flex";
import "vue-flex/dist/vue-flex.css";
import router from './router' 
import ElementUI from 'element-ui'
import Viewer from 'v-viewer'
import 'viewerjs/dist/viewer.css'
import store from './store'
import qrcode from "qrcode";
import FileReader from 'vue-filereader'
import md5 from 'js-md5';
import VueToast from 'vue-toast-notification';
//import 'vue-toast-notification/dist/theme-default.css';
import 'vue-toast-notification/dist/theme-sugar.css';
import setHtmlFontSize from  "./js/rem.js" 
import vuetify from './plugins/vuetify' // path to vuetify export
import 'element-ui/lib/theme-chalk/index.css'
import enLocale from 'element-ui/lib/locale/lang/en'        //引入Element UI的英文包
import zhLocale from 'element-ui/lib/locale/lang/zh-CN'     //引入Element UI的中文包

 

//调用函数,实现功能
setHtmlFontSize();
// require('dotenv').config({
//     path: '.env',
//     encoding: 'utf8',
// })//must be loaded 

 
//Vue.use(ElementUI);  
Vue.use(Viewer);
Vue.use(VueFlex); 
Vue.use(qrcode);
Vue.use(VueToast);
Vue.use(VueClipboard);
Vue.use(VueI18n) ;
Vue.use(ElementUI, {
    i18n: (key, value) => i18n.t(key, value)
  }); //兼容i18n 7.x版本设置
 
Viewer.setDefaults({
    Options: {
        "inline": true,
        "button": true,
        "navbar": true,
        "title": true,
        "toolbar": true,
        "tooltip": true,
        "movable": true,
        "zoomable": true,
        "rotatable": true,
        "scalable": true,
        "transition": true,
        "fullscreen": true,
        "keyboard": true,
        "url": "data-source"
    }
});
Vue.prototype.$md5 = md5;
Vue.config.productionTip = false

const i18n = new VueI18n({
    locale: 'CN',    // 语言标识, 可在相应的VUE页面通过切换locale的值来实现语言切换,this.$i18n.locale 
    messages: {
      //'CN': require('./common/lang/zh'),   // 中文语言包
      //'EN': require('./common/lang/en')    // 英文语言包
    'CN': Object.assign(require('@/common/lang/zh'), zhLocale),  //这里需要注意一下，是如何导入多个语言包的
    'EN': Object.assign(require('@/common/lang/en'), enLocale), 
    }
})

new Vue({
    i18n,
    vuetify,
    router,
    store, 
    render: h => h(App)
}).$mount('#app')