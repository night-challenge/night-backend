package com.nightchallenge.backend.game.controller;

import com.nightchallenge.backend.game.domain.GameSession;
import com.nightchallenge.backend.game.dto.request.GameMoveRequest;
import com.nightchallenge.backend.game.dto.request.GameStartRequest;
import com.nightchallenge.backend.game.dto.response.GameDetailResponse;
import com.nightchallenge.backend.game.dto.response.GameStateResponse;
import com.nightchallenge.backend.game.dto.response.GameStatsResponse;
import com.nightchallenge.backend.game.dto.response.LegalMovesResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * 용도: 게임 진행 HTTP 요청 처리.
 * 게임 시작, 이동, 조회, 합법 이동 조회, 누적 통계 조회 요청을 Service에 전달하고
 * 공통 ApiResponse 형식으로 반환한다.
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
    @ResponseStatus(HttpStatus.CREATED)
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
     * 용도: 게임 누적 통계 조회 API.
     * 화면 2.1(재진입)의 최고 포인트, 플레이 횟수 표시에 사용한다.
     */
    @GetMapping("/stats")
    public ApiResponse<GameStatsResponse> getStats() {
        return ApiResponse.success(GameStatsResponse.from(gameService.getStats()));
    }

    /**
     * 용도: 게임 세션 상세 조회 API.
     * 화면 4(나이트 동선 보여주기)처럼 각인 생성 이전 시점에 나이트 이동 궤적을 미리 확인할 때 사용한다.
     */
    @GetMapping("/{gameSessionId}")
    public ApiResponse<GameDetailResponse> getGameDetail(
            @PathVariable Long gameSessionId
    ) {
        GameSession session = gameService.getGameSession(gameSessionId);
        return ApiResponse.success(GameDetailResponse.from(session));
    }

    /**
     * 용도: 합법적인 이동 가능 칸 조회 API.
     * 화면 3.1(말 선택)에서 선택한 말이 이동 가능한 칸을 하이라이트할 때 사용한다.
     */
    @GetMapping("/{gameSessionId}/legal-moves")
    public ApiResponse<LegalMovesResponse> getLegalMoves(
            @PathVariable Long gameSessionId,
            @RequestParam String square
    ) {
        List<String> legalMoves = gameService.getLegalMoves(gameSessionId, square);
        return ApiResponse.success(new LegalMovesResponse(square.toUpperCase(), legalMoves));
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
