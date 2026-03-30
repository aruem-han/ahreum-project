import axios from 'axios'
 
const api = axios.create({ baseURL: '/api' })

 
export const getFavorites = () =>
  api.get('/stations/favorites').then(r => r.data)
 
export const searchStations = (q) =>
  api.get('/stations/search', { params: { q } }).then(r => r.data)
 
export const getCongestion = (stationId) =>
  api.get(`/congestion/${stationId}`).then(r => r.data)
 
// export const getAiInsight = (body) =>
//   api.post('/ai/insight', body).then(r => r.data)

export const getAiInsight = (body) =>
  api.post('/ai/test', body).then(r => r.data)
 