package com.questionnaire.area.game.repository;

import com.questionnaire.area.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

/**
 * Created by ChaosFire on 23.4.2017 г
 */
@Repository
@SuppressWarnings("all")
public interface GameRepository extends JpaRepository<Game, Long> {

    @Query("SELECT gq.id " +
            "FROM Game AS g " +
            "INNER JOIN g.answeredQuestions AS gq " +
            "WHERE g.id = :id AND gq.questionLevel = :questionLevel")
    List<Long> findAnsweredQuestionsIdForGame(@Param("id") Long id, @Param("questionLevel") Byte questionLevel);

    @Modifying
    @Transactional
    @Query("UPDATE Game AS g " +
            "SET g.isFinished = true, g.timeLimit = null " +
            "WHERE g.id = :gameId")
    void finishGame(@Param("gameId") Long gameId);

    @Modifying
    @Transactional
    @Query("UPDATE Game AS g " +
            "SET g.totalTime = g.totalTime + :answerTime, g.currentQuestion = g.currentQuestion + 1, g.timeLimit = null " +
            "WHERE g.id = :gameId")
    void updateGame(@Param("answerTime") long answerTime, @Param("gameId") long gameId);

    @Query(value = "SELECT g.current_question, g.total_time, u.username " +
                        "FROM games AS g " +
                        "INNER JOIN users AS u " +
                        "ON g.user_id = u.id " +
                        "INNER JOIN (SELECT MAX(ing.current_question) AS question, inu.id AS uid " +
                                        "FROM games AS ing " +
                                        "INNER JOIN users AS inu " +
                                        "ON ing.user_id = inu.id " +
                                        "WHERE ing.total_time != 0 " +
                                        "GROUP BY inu.username ) AS umg " +
                        "ON g.current_question = umg.question AND u.id = umg.uid " +
                        "WHERE g.is_finished = TRUE " +
                        "ORDER BY g.current_question DESC, g.total_time ASC",
            nativeQuery = true)
    List<Object[]> findAllByCurrentQuestionRank();

    @Query("SELECT MAX(g.currentQuestion) AS question, g.totalTime, u.username " +
            "FROM Game AS g " +
            "INNER JOIN g.user AS u " +
            "WHERE g.isFinished = true AND u.username LIKE CONCAT('%', :username, '%') " +
            "GROUP BY u.username")
    List<Object[]> findAllByCurrentQuestionRankNameMatch(@Param("username") String username);

    @Query(value = "SELECT users.username, MIN((games.total_time / (games.current_question - 1)) / 1000) AS average " +
                    "FROM games " +
                    "INNER JOIN users " +
                    "ON games.user_id = users.id " +
                    "WHERE games.is_finished = true " +
                    "GROUP BY users.username " +
                    "ORDER BY average",
        nativeQuery = true)
    List<Object[]> findAllByAverageAnswerTime();

    @Modifying
    @Transactional
    @Query("UPDATE Game AS g " +
            "SET g.timeLimit = :timeLimit " +
            "WHERE g.id = :gameId")
    void addAnswerTimeLimit(@Param("timeLimit") Date limit, @Param("gameId") Long gameId);
}