import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/api/v1/auth/user/login',
    method: 'post',
    data
  })
}

export function getInfo(token) {
  return request({
    url: '/api/v1/auth/user/info',
    method: 'get',
    params: { token }
  })
}

export function logout() {
  return request({
    url: '/api/v1/auth/user/logout',
    method: 'post'
  })
}
