package com.nightchallenge.backend.game.controller;

import com.github.bhlangonijr.chesslib.Board;
import com.nightchallenge.backend.game.domain.GameMode;
import com.nightchallenge.backend.game.domain.GameSession;
import com.nightchallenge.backend.game.service.GameMoveService;
import com.nightchallenge.backend.game.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GameControllerTest {

    private GameService gameService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        gameService = mock(GameService.class);
        GameController controller = new GameController(gameService, mock(GameMoveService.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("새 게임을 생성하면 201과 공통 성공 응답을 반환한다")
    void startGameReturnsCreated() throws Exception {
        GameSession session = new GameSession(1L, GameMode.EASY, new Board().getFen());
        ReflectionTestUtils.setField(session, "id", 10L);
        given(gameService.startGame(GameMode.EASY)).willReturn(session);

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mode\":\"EASY\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.mode").value("EASY"));
    }
}
