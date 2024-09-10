import axios from 'axios'

// 登陆
export const login = (data) => {
    return axios.post("/admin/login", data)
}

export const logout = () => {
    return axios.get('/admin/logout', {})
}

export const editpassword = (data) => {
    return axios.post('/admin/change_password', data)
}