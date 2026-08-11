package com.nightchallenge.backend.game.controller;

import com.nightchallenge.backend.game.domain.GameSession;
import com.nightchallenge.backend.game.dto.request.GameMoveRequest;
import com.nightchallenge.backend.game.dto.request.GameStartRequest;
import com.nightchallenge.backend.game.dto.response.GameStateResponse;
import com.nightchallenge.backend.game.dto.response.MoveResultResponse;
import com.nightchallenge.backend.game.service.GameMoveService;
import com.nightchallenge.backend.game.service.GameService;
import com.nightchallenge.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 용도: 게임 진행 HTTP 요청 처리.
 * 게임 시작, 이동, 진행 중인 게임 조회 요청을 Service에 전달하고 공통 ApiResponse 형식으로 반환한다.
 */
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final GameMoveService gameMoveService;

    /**
     * 용도: 새 게임 시작 API.
     * 선택한 난이도로 새 게임 세션을 생성한다.
     */
    @PostMapping
    public ApiResponse<GameStateResponse> startGame(
            @Valid @RequestBody GameStartRequest request
    ) {
        GameSession session = gameService.startGame(request.mode());
        return ApiResponse.success("게임이 시작되었습니다.", GameStateResponse.from(session));
    }

    /**
     * 용도: 진행 중인 게임 조회 API.
     * 화면 2.1(재진입)에서 이어할 게임이 있는지 확인할 때 사용한다.
     */
    @GetMapping("/active")
    public ApiResponse<GameStateResponse> getActiveGame() {
        GameSession session = gameService.getActiveGame();
        return ApiResponse.success(GameStateResponse.from(session));
    }

    /**
     * 용도: 이동 실행 API.
     * 사용자의 이동을 처리하고, 게임이 계속되면 이어서 AI의 응수까지 처리한 결과를 반환한다.
     */
    @PostMapping("/{gameSessionId}/moves")
    public ApiResponse<MoveResultResponse> move(
            @PathVariable Long gameSessionId,
            @Valid @RequestBody GameMoveRequest request
    ) {
        GameSession session = gameService.getGameSession(gameSessionId);
        GameMoveService.MoveResult result = gameMoveService.processTurn(session, request.from(), request.to());

        return ApiResponse.success(MoveResultResponse.from(result));
    }
}