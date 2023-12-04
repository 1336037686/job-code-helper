import request from '@/utils/request'

export default {
  chatCreate(data) {
    return request({
      url: '/api/v1/qa/chat/create',
      method: 'post',
      data
    })
  },
  chatUpdate(data) {
    return request({
      url: '/api/v1/qa/chat/update',
      method: 'post',
      data
    })
  },
  chatDelete(data) {
    return request({
      url: '/api/v1/qa/chat/delete',
      method: 'post',
      data
    })
  },
  chatQuery() {
    return request({
      url: '/api/v1/qa/chat/query',
      method: 'post'
    })
  },
  chatQueryById(id) {
    return request({
      url: '/api/v1/qa/chat/query/' + id,
      method: 'post'
    })
  },
  messageCreate(id, mid, title) {
    return 'http://localhost:8100/api/v1/qa/chat/message/create?id=' + id + '&mid=' + mid + '&title=' + title
  },
  messageSend(data) {
    return request({
      url: '/api/v1/qa/chat/message/send',
      method: 'post',
      data
    })
  },
  messageIsReady(data) {
    return request({
      url: '/api/v1/qa/chat/message/is-ready',
      method: 'post',
      data
    })
  },
  messageClose(data) {
    return request({
      url: '/api/v1/qa/chat/message/close',
      method: 'post',
      data
    })
  }


}


