import request from './request'

export const pcApi = {
  // 获取角色卡列表
  getPcList() {
    return request.get('/pc')
  },

  // 获取角色卡完整数据
  getPcDetail(pcId) {
    return request.get(`/pc/${pcId}`)
  },

  // 创建空白角色卡
  createPc(data) {
    return request.post('/pc', data)
  },

  // 更新角色卡（覆盖更新）
  updatePc(pcId, data) {
    return request.put(`/pc/${pcId}`, data)
  },

  // 删除角色卡
  deletePc(pcId) {
    return request.delete(`/pc/${pcId}`)
  },

  // 更新自定义显示列
  updateDisplayFields(displayFields) {
    return request.put('/pc/display-fields', { display_fields: displayFields })
  },

  // 手动排序（按传入的 pcId 顺序）
  reorderPcs(pcIds) {
    return request.put('/pc/reorder', { pc_ids: pcIds })
  },

  // 上传 Excel 文件
  uploadExcel(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/pc/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 搜索包含某字段的角色卡
  searchField(field) {
    return request.get('/pc/search-field', { params: { field } })
  }
}
