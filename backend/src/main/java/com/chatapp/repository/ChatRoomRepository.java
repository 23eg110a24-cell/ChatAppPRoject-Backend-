package com.chatapp.repository;

import com.chatapp.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByDirectKey(String directKey);

    /**
     * Every room the user belongs to, with the complete member list. The membership test is a
     * subquery so it does not filter the fetched collection down to just the viewer.
     */
    @Query("""
            select distinct r from ChatRoom r
            join fetch r.members
            where r.id in (
                select r2.id from ChatRoom r2 join r2.members m2 where m2.id = :userId
            )
            """)
    List<ChatRoom> findAllForMember(@Param("userId") Long userId);

    @Query("""
            select distinct r from ChatRoom r
            join r.members m
            where m.id = :userId and r.type = com.chatapp.domain.RoomType.CHANNEL
            """)
    List<ChatRoom> findChannelsForMember(@Param("userId") Long userId);

    @Query("""
            select r from ChatRoom r
            where r.type = com.chatapp.domain.RoomType.CHANNEL
            order by r.name asc
            """)
    List<ChatRoom> findAllChannels();

    @Query("""
            select r from ChatRoom r
            where r.type = com.chatapp.domain.RoomType.CHANNEL
              and lower(r.name) like lower(concat('%', :term, '%'))
            order by r.name asc
            """)
    List<ChatRoom> searchChannels(@Param("term") String term);

    boolean existsByIdAndMembersId(Long roomId, Long userId);
}