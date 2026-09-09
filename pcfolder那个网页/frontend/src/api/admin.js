import request from './request'

export const adminApi = {
  // 邀请码管理
  getInviteCodes() {
    return request.get('/admin/invite-codes')
  },

  createInviteCode() {
    return request.post('/admin/invite-codes')
  },

  deleteInviteCode(id) {
    return request.delete(`/admin/invite-codes/${id}`)
  },

  // 用户管理
  getUsers() {
    return request.get('/admin/users')
  },

  deleteUser(userId) {
    return request.delete(`/admin/users/${userId}`)
  },

  resetPassword(userId) {
    return request.put(`/admin/users/${userId}/reset-password`)
  }
}
