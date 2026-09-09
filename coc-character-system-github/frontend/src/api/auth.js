import request from './request'

export const authApi = {
  login(data) {
    return request.post('/auth/login', data)
  },

  register(data) {
    return request.post('/auth/register', data)
  },

  getCurrent() {
    return request.get('/auth/current')
  },

  // 修改密码
  changePassword(data) {
    return request.put('/auth/password', data)
  }
}
