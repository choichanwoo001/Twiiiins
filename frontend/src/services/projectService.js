import axios from '../api/axios'
import { cachedApiCall, createCacheKey, apiCache } from '../utils/apiCache'
import { getErrorMessage, logError } from '../utils/errorHandler'
import { unwrapApiResponse } from './apiResponse'

export const projectService = {
    async getAllProjects() {
        const cacheKey = createCacheKey('/projects')
        return cachedApiCall(
            async () => {
                try {
                    const response = await axios.get('/projects')
                    // 응답 구조가 표준화되어 있지 않은 경우를 대비 (기존 로직 유지)
                    return unwrapApiResponse(response, [])
                } catch (error) {
                    logError(error, 'getAllProjects')
                    throw new Error(getErrorMessage(error))
                }
            },
            cacheKey,
            5 * 60 * 1000
        )
    },

    async getProjectBySlug(slug) {
        try {
            const response = await axios.get(`/projects/slug/${slug}`)
            return unwrapApiResponse(response)
        } catch (error) {
            logError(error, 'getProjectBySlug')
            throw new Error(getErrorMessage(error))
        }
    }
}
