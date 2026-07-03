import axios from '../api/axios'
import { cachedApiCall, createCacheKey, apiCache } from '../utils/apiCache'
import { getErrorMessage, logError } from '../utils/errorHandler'
import { unwrapApiResponse } from './apiResponse'
import {
    buildProjectCreatePayload,
    buildProjectUpdatePayload
} from './payloadMappers'

const invalidateProjectCache = () => {
    apiCache.deletePattern('^/projects')
}

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

    async createProject(projectData) {
        try {
            const payload = buildProjectCreatePayload(projectData)
            const response = await axios.post('/projects', payload)
            invalidateProjectCache()
            return unwrapApiResponse(response)
        } catch (error) {
            logError(error, 'createProject')
            throw new Error(getErrorMessage(error))
        }
    },

    async updateProject(id, projectData) {
        try {
            const payload = buildProjectUpdatePayload(projectData)
            const response = await axios.put(`/projects/${id}`, payload)
            invalidateProjectCache()
            return unwrapApiResponse(response)
        } catch (error) {
            logError(error, 'updateProject')
            throw new Error(getErrorMessage(error))
        }
    },

    async updateProjectImages(id, imageUrls) {
        try {
            const response = await axios.put(`/projects/${id}/images`, {
                imageUrls: Array.isArray(imageUrls) ? imageUrls : []
            })
            invalidateProjectCache()
            return unwrapApiResponse(response)
        } catch (error) {
            logError(error, 'updateProjectImages')
            throw new Error(getErrorMessage(error))
        }
    },

    async deleteProject(id) {
        try {
            await axios.delete(`/projects/${id}`)
            invalidateProjectCache()
        } catch (error) {
            logError(error, 'deleteProject')
            throw new Error(getErrorMessage(error))
        }
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
