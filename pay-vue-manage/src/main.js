import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import Element from 'element-ui'
import './assets/scss/base.scss'
import 'element-ui/lib/theme-chalk/index.css';
import './assets/icon/icon/iconfont.css'
import './assets/icon/icon1/iconfont.css'
import './assets/icon/icon2/iconfont.css'
import './router/interception'
import plugin from '@/plugins'
import i18n from './utils/i18n'
import VCharts from 'v-charts'
import VueClipboard from 'vue-clipboard2'
import VueUploadImgs from 'vue-upload-imgs'

Vue.use(VueUploadImgs)
Vue.use(VueClipboard);
Vue.use(plugin, {})
Vue.use(VCharts)
Vue.use(Element, {
    i18n: (key, value) => i18n.t(key, value)
});

import Viewer from 'v-viewer'
import 'viewerjs/dist/viewer.css'

Vue.use(Viewer);
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

Vue.config.productionTip = false
new Vue({
    router,
    store,
    i18n,
    render: h => h(App)
}).$mount('#app')