import axios from '../api/axios'
import { cachedApiCall, createCacheKey } from '../utils/apiCache'
import { getErrorMessage, logError } from '../utils/errorHandler'

export const projectService = {
    // 프로젝트 목록 조회
    async getAllProjects() {
        const cacheKey = createCacheKey('/projects')
        return cachedApiCall(
            async () => {
                try {
                    const response = await axios.get('/projects')
                    // 응답 구조가 표준화되어 있지 않은 경우를 대비 (기존 로직 유지)
                    return response.data.data || response.data || []
                } catch (error) {
                    logError(error, 'getAllProjects')
                    throw new Error(getErrorMessage(error))
                }
            },
            cacheKey,
            5 * 60 * 1000 // 5분 캐시 (프로젝트는 자주 변하지 않음)
        )
    },

    // 프로젝트 상세 조회 (Slug 사용)
    async getProjectBySlug(slug) {
        try {
            // 상세 조회는 캐싱하지 않음 (또는 짧게 설정) - 현재는 실시간성이 중요할 수 있으므로 캐시 제외
            const response = await axios.get(`/projects/slug/${slug}`)
            return response.data.data || response.data
        } catch (error) {
            logError(error, 'getProjectBySlug')
            throw new Error(getErrorMessage(error)) // View에서 null 처리 등을 할 수 있도록 에러 전파
        }
    }
}
