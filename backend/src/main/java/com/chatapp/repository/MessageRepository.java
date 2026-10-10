package com.chatapp.repository;

import com.chatapp.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Newest messages of a room first. The caller reverses the result to render oldest-first.
     */
    @Query("""
            select m from Message m
            where m.room.id = :roomId
            order by m.id desc
            """)
    List<Message> findLatest(@Param("roomId") Long roomId, Pageable pageable);

    @Query("""
            select m from Message m
            where m.room.id = :roomId and m.id < :beforeId
            order by m.id desc
            """)
    List<Message> findLatestBefore(@Param("roomId") Long roomId,
                                   @Param("beforeId") Long beforeId,
                                   Pageable pageable);

    /**
     * The single most recent message for each of the supplied rooms, fetched in one query.
     */
    @Query("""
            select m from Message m
            where m.room.id in :roomIds
              and m.id = (
                select max(m2.id) from Message m2 where m2.room.id = m.room.id
              )
            """)
    List<Message> findLatestPerRoom(@Param("roomIds") Collection<Long> roomIds);

    long countByRoomId(Long roomId);
}