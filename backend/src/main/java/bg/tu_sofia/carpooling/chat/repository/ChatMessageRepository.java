package bg.tu_sofia.carpooling.chat.repository;

import bg.tu_sofia.carpooling.chat.domain.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByChannelIdOrderByCreatedAtAsc(Long channelId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.channel.id = :channelId AND m.sender.id != :userId AND m.readAt IS NULL")
    long countUnreadMessages(@Param("channelId") Long channelId, @Param("userId") Long userId);
}
