package com.daisy.health.ai;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@Profile("mysql")
public class JdbcAiChatRepository implements AiChatRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcAiChatRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<AiChatMessage> recent(long accountId) {
        return jdbcTemplate.query(
                "select id, role, content, created_at from ai_chat_message " +
                        "where account_id = ? and created_at >= date_sub(now(), interval 7 day) " +
                        "order by created_at, id",
                (resultSet, rowNum) -> message(
                        resultSet.getLong("id"),
                        resultSet.getString("role"),
                        resultSet.getString("content"),
                        resultSet.getTimestamp("created_at")
                ),
                accountId
        );
    }

    @Override
    public List<AiChatMessage> recentForContext(long accountId, int limit) {
        return jdbcTemplate.query(
                "select id, role, content, created_at from (" +
                        "select id, role, content, created_at from ai_chat_message " +
                        "where account_id = ? and created_at >= date_sub(now(), interval 7 day) " +
                        "order by created_at desc, id desc limit ?" +
                        ") recent_messages order by created_at, id",
                (resultSet, rowNum) -> message(
                        resultSet.getLong("id"),
                        resultSet.getString("role"),
                        resultSet.getString("content"),
                        resultSet.getTimestamp("created_at")
                ),
                accountId,
                limit
        );
    }

    @Override
    @Transactional
    public AiChatExchange saveExchange(long accountId, String userContent, String assistantContent) {
        LocalDateTime createdAt = LocalDateTime.now();
        long userId = insert(accountId, "user", userContent, createdAt);
        long assistantId = insert(accountId, "assistant", assistantContent, createdAt);
        return new AiChatExchange(
                new AiChatMessage(userId, "user", userContent, createdAt),
                new AiChatMessage(assistantId, "assistant", assistantContent, createdAt)
        );
    }

    @Override
    public int deleteExpired() {
        return jdbcTemplate.update("delete from ai_chat_message where created_at < date_sub(now(), interval 7 day)");
    }

    private long insert(long accountId, String role, String content, LocalDateTime createdAt) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "insert into ai_chat_message(account_id, role, content, created_at) values(?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setLong(1, accountId);
            statement.setString(2, role);
            statement.setString(3, content);
            statement.setTimestamp(4, Timestamp.valueOf(createdAt));
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) throw new IllegalStateException("聊天记录保存失败");
        return key.longValue();
    }

    private AiChatMessage message(long id, String role, String content, Timestamp timestamp) {
        return new AiChatMessage(id, role, content, timestamp.toLocalDateTime());
    }
}
