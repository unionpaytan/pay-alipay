import axios from 'axios'
import store from '@/store'
import config from '@/config'
import { Message } from 'element-ui'
import { MessageBox } from 'element-ui';
import Router from '@/router/index'

const ENV = process.env.VUE_APP_ENV
export default (Vue) => {
  Object.defineProperties(Vue.prototype, {
    $http: {
      value: axios
    }
  })

  // axios配置
  axios.defaults.baseURL = config[ENV].host
  axios.defaults.timeout = 15000 //15秒后超时
  axios.defaults.withCredentials = true
  //  添加拦截器
  // request拦截器
  axios.interceptors.request.use(function (config) {
    let _token = sessionStorage.token || ''
    // console.log(config.data)
    let f = new FormData()
    let c_data = config.data || {}
    let dateKeys = Object.keys(c_data)
    dateKeys.forEach(i => {
      f.append(i, c_data[i])
    })
    config.data = f
    // config.headers.Authorization = `Bearer ${_token}`;
    config.headers.Authorization = `${_token}`;
    store.dispatch('setLoading', true)
    return config
  }, function (error) {
    store.dispatch('setLoading', false)
    return Promise.reject(error)
  })

  //response 拦截器
  axios.interceptors.response.use(function (response) {
    // console.log(response,'response')
    store.dispatch('setLoading', false)
    if (Number(response.data.code) !== 0) {
      Message({
        message: response.data.message,
        type: 'error',
        duration: 2 * 1000
      })
      if (Number(response.data.code) == 6000) {
        Router.push('/login')
        sessionStorage.clear()
        setTimeout(() => {
          window.location.reload()
        }, 300)

      }
      return Promise.reject(response.data)
    } else {
      return Promise.resolve(response.data)
    }
  }, function (error) {
    // console.log(error.config)
    store.dispatch('setLoading', false)
    if (error.response) {
      console.log(error.response.status)
      switch (error.response.status) {
        case 401:
          console.log('401 未授权')
          break
        case 500:
          console.log('内部服务器出错')
          break

        default:
          console.log('发生错误了')
      }
    } else {
      console.log('超时')
    }

    Message.error('网络异常' + error)
    return Promise.reject(error)
  })
}
