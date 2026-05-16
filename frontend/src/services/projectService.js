import axios from '../api/axios'
import { cachedApiCall, createCacheKey, apiCache } from '../utils/apiCache'
import { getErrorMessage, logError } from '../utils/errorHandler'

export const projectService = {
    async getAllProjects() {
        const cacheKey = createCacheKey('/projects')
        return cachedApiCall(
            async () => {
                try {
                    const response = await axios.get('/projects')
                    return response.data.data
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
            return response.data.data
        } catch (error) {
            logError(error, 'getProjectBySlug')
            throw new Error(getErrorMessage(error))
        }
    },

    async createProject(projectData) {
        const response = await axios.post('/projects', projectData)
        apiCache.deletePattern('^/projects')
        return response.data.data
    },

    async updateProject(id, projectData) {
        const response = await axios.put(`/projects/${id}`, projectData)
        apiCache.deletePattern('^/projects')
        return response.data.data
    },

    async deleteProject(id) {
        await axios.delete(`/projects/${id}`)
        apiCache.deletePattern('^/projects')
    }
}
