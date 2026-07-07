import { apiClient } from '@/shared/api/http'
import type { ChatChannel, ChatMessage, PageResponse } from '@/types'

export const chatApi = {
  getChannels: () => apiClient.get<ChatChannel[]>('/chat/channels'),
  getMessages: (channelId: number, page = 0, size = 50) =>
    apiClient.get<PageResponse<ChatMessage>>(`/chat/channels/${channelId}/messages`, {
      params: { page, size },
    }),
}
