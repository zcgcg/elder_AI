package com.daisy.health.ai;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcAiChatRepositoryTest {
    @Test
    void historyIsAccountScopedIncludesTheSevenDayBoundaryAndIsChronological() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JdbcAiChatRepository repository = new JdbcAiChatRepository(jdbc);

        repository.recent(88L);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).query(sql.capture(), any(RowMapper.class), eq(88L));
        String query = compact(sql.getValue());
        assertTrue(query.contains("where account_id = ?"));
        assertTrue(query.contains("created_at >= date_sub(now(), interval 7 day)"));
        assertTrue(query.contains("order by created_at, id"));
    }

    @Test
    void modelContextUsesOnlyTheNewestTwentyThenRestoresChronologicalOrder() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JdbcAiChatRepository repository = new JdbcAiChatRepository(jdbc);

        repository.recentForContext(91L, 20);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).query(sql.capture(), any(RowMapper.class), eq(91L), eq(20));
        String query = compact(sql.getValue());
        assertTrue(query.contains("where account_id = ?"));
        assertTrue(query.contains("order by created_at desc, id desc limit ?"));
        assertTrue(query.endsWith("order by created_at, id"));
    }

    @Test
    void cleanupDeletesOnlyMessagesStrictlyOlderThanSevenDays() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(any(String.class))).thenReturn(3);
        JdbcAiChatRepository repository = new JdbcAiChatRepository(jdbc);

        repository.deleteExpired();

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).update(sql.capture());
        assertTrue(compact(sql.getValue()).contains("created_at < date_sub(now(), interval 7 day)"));
    }

    @Test
    void scheduledCleanupDelegatesToTheSameExpiryRule() {
        AiChatRepository repository = mock(AiChatRepository.class);

        new AiChatCleanupJob(repository).deleteExpiredMessages();

        verify(repository).deleteExpired();
    }

    private String compact(String sql) {
        return sql.replaceAll("\\s+", " ").trim().toLowerCase();
    }
}
