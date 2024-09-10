import Vue from 'vue'
import VueI18n from 'vue-i18n'
Vue.use(VueI18n)

import en_US from '../assets/i18n/en'
import zh_TW from '../assets/i18n/tw'
import zh_CN from '../assets/i18n/zh'

import el_en_US from 'element-ui/lib/locale/lang/en'
import el_zh_CN from 'element-ui/lib/locale/lang/zh-CN'
import el_zh_TW from 'element-ui/lib/locale/lang/zh-TW'

const messages = {
    'zh_CN': {...zh_CN, ...el_zh_CN }, // 中文语言包
    'zh_TW': {...zh_TW, ...el_zh_TW }, // 繁体语言包
    'en_US': {...en_US, ...el_en_US } // 英文语言包
}



export default new VueI18n({
    locale: 'zh_CN', // set locale 默认显示
    messages: messages // set locale messages
})