package com.nightchallenge.backend.engraving.service;

import com.nightchallenge.backend.engraving.domain.ConstellationData;
import com.nightchallenge.backend.engraving.domain.ConstellationPoint;
import com.nightchallenge.backend.engraving.domain.ConstellationShape;
import com.nightchallenge.backend.game.domain.KnightMoveLog;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 용도: 나이트 이동 궤적을 별자리 데이터로 변환.
 * 게임판 격자 좌표의 원본 궤적(before)을 받아, 중복 제거·밀도 보정·최근접 이웃 연결·그룹 연결 보정을
 * 거쳐 300x300 캔버스 좌표의 최종 별자리(after)를 생성한다.
 */
@Component
public class ConstellationGenerationService {

    private static final int CANVAS_SIZE = 300;
    private static final int BOARD_SIZE = 8;
    private static final double CANVAS_MARGIN_RATIO = 0.1;

    private static final int MIN_POINTS = 10;
    private static final int MAX_POINTS = 18;
    private static final double DENSIFY_RATIO_MIN = 0.3;
    private static final double DENSIFY_RATIO_MAX = 0.5;
    private static final double RANDOM_OFFSET_RANGE = 30.0;

    private static final int NEAREST_NEIGHBOR_MIN = 2;
    private static final int NEAREST_NEIGHBOR_MAX = 3;

    private final java.util.Random random = new java.util.Random();

    /**
     * 용도: 별자리 데이터 생성.
     * 사용자 나이트 이동 기록으로 원본 궤적(before)을 구성하고, 이를 재구성해 최종 별자리(after)를 생성한다.
     */
    public ConstellationData generate(List<KnightMoveLog> knightMoveLog) {
        ConstellationShape before = buildBefore(knightMoveLog);
        ConstellationShape after = buildAfter(before);
        return new ConstellationData(before, after);
    }

    /**
     * 용도: 원본 궤적(before) 구성.
     * 이동 순서대로 새 id를 부여하며, 같은 칸을 다시 방문해도 별개의 점으로 취급해 실제 이동 순서를 보존한다.
     */
    private ConstellationShape buildBefore(List<KnightMoveLog> logs) {
        List<ConstellationPoint> points = new ArrayList<>();
        List<List<Integer>> connections = new ArrayList<>();

        if (logs.isEmpty()) {
            return new ConstellationShape(points, connections);
        }

        int id = 0;
        points.add(new ConstellationPoint(id, logs.get(0).fromX(), logs.get(0).fromY()));
        int previousId = id;
        id++;

        for (KnightMoveLog log : logs) {
            points.add(new ConstellationPoint(id, log.toX(), log.toY()));
            connections.add(List.of(previousId, id));
            previousId = id;
            id++;
        }

        return new ConstellationShape(points, connections);
    }

    /**
     * 용도: 최종 별자리(after) 구성.
     * before의 격자 좌표를 캔버스 좌표로 변환하며 중복 좌표를 하나의 점으로 합치고,
     * 점을 보강한 뒤 최근접 이웃 방식으로 연결 관계를 새로 만들고, 분리된 그룹을 다리로 이어 붙인다.
     */
    private ConstellationShape buildAfter(ConstellationShape before) {
        List<ConstellationPoint> points = dedupAndScaleToCanvas(before.points());
        densify(points);

        List<List<Integer>> connections = connectNearestNeighbors(points);
        connections = bridgeDisconnectedGroups(points, connections);

        return new ConstellationShape(points, connections);
    }

    /**
     * 용도: 중복 좌표 제거 및 캔버스 좌표 변환.
     * 같은 격자 좌표를 가진 점들을 하나로 합치고 새 id를 순서대로 부여하며, 좌표를 300x300 캔버스 기준으로 변환한다.
     */
    private List<ConstellationPoint> dedupAndScaleToCanvas(List<ConstellationPoint> beforePoints) {
        Map<String, Integer> gridKeyToNewId = new LinkedHashMap<>();
        List<ConstellationPoint> dedupedPoints = new ArrayList<>();

        for (ConstellationPoint point : beforePoints) {
            String gridKey = point.x() + "," + point.y();
            gridKeyToNewId.computeIfAbsent(gridKey, key -> {
                int newId = dedupedPoints.size();
                dedupedPoints.add(new ConstellationPoint(newId, toCanvasCoordinate(point.x()), toCanvasCoordinate(point.y())));
                return newId;
            });
        }

        return dedupedPoints;
    }

    /**
     * 용도: 격자 좌표를 캔버스 좌표로 변환.
     * 0~7 범위의 격자 좌표를 여백을 둔 300x300 캔버스 좌표로 스케일링한다.
     */
    private int toCanvasCoordinate(int gridCoordinate) {
        double margin = CANVAS_SIZE * CANVAS_MARGIN_RATIO;
        double usableSize = CANVAS_SIZE - (margin * 2);
        double scaled = margin + (gridCoordinate / (double) (BOARD_SIZE - 1)) * usableSize;
        return (int) Math.round(scaled);
    }

    /**
     * 용도: 점 추가(밀도 보정).
     * 기존 점 사이에 랜덤 오프셋을 적용한 점을 원본 점 개수의 30~50% 비율로 추가하되,
     * 전체 점 개수를 10~18개 범위로 제한하고 좌표를 캔버스 범위 안으로 clamp한다.
     */
    private void densify(List<ConstellationPoint> points) {
        int originalCount = points.size();
        if (originalCount == 0) {
            return;
        }

        double ratio = DENSIFY_RATIO_MIN + random.nextDouble() * (DENSIFY_RATIO_MAX - DENSIFY_RATIO_MIN);
        int additional = (int) Math.round(originalCount * ratio);
        int targetTotal = Math.min(MAX_POINTS, Math.max(MIN_POINTS, originalCount + additional));
        int toAdd = targetTotal - originalCount;

        for (int i = 0; i < toAdd; i++) {
            ConstellationPoint base = points.get(random.nextInt(points.size()));
            int x = clamp(base.x() + randomOffset(), 0, CANVAS_SIZE);
            int y = clamp(base.y() + randomOffset(), 0, CANVAS_SIZE);
            points.add(new ConstellationPoint(points.size(), x, y));
        }
    }

    private int randomOffset() {
        return (int) Math.round((random.nextDouble() * 2 - 1) * RANDOM_OFFSET_RANGE);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 용도: 최근접 이웃 방식 연결 구성.
     * 각 점마다 가장 가까운 점 2~3개와 연결선을 만들고, 방향과 관계없이 동일한 두 점을 잇는 중복 선은 하나만 남긴다.
     */
    private List<List<Integer>> connectNearestNeighbors(List<ConstellationPoint> points) {
        Set<String> addedPairs = new LinkedHashSet<>();
        List<List<Integer>> connections = new ArrayList<>();

        for (ConstellationPoint point : points) {
            List<ConstellationPoint> sortedByDistance = points.stream()
                    .filter(other -> other.id() != point.id())
                    .sorted(Comparator.comparingDouble(other -> distance(point, other)))
                    .toList();

            int neighborCount = NEAREST_NEIGHBOR_MIN + random.nextInt(NEAREST_NEIGHBOR_MAX - NEAREST_NEIGHBOR_MIN + 1);
            for (int i = 0; i < Math.min(neighborCount, sortedByDistance.size()); i++) {
                ConstellationPoint neighbor = sortedByDistance.get(i);
                String pairKey = unorderedKey(point.id(), neighbor.id());
                if (addedPairs.add(pairKey)) {
                    connections.add(List.of(Math.min(point.id(), neighbor.id()), Math.max(point.id(), neighbor.id())));
                }
            }
        }

        return connections;
    }

    /**
     * 용도: 분리된 그룹 연결 보정.
     * Union-Find로 연결된 그룹을 파악하고, 서로 다른 그룹 사이에서 가장 가까운 점 한 쌍을 찾아 다리를 놓아
     * 모든 점이 하나로 이어진 별자리가 되도록 한다.
     */
    private List<List<Integer>> bridgeDisconnectedGroups(List<ConstellationPoint> points, List<List<Integer>> connections) {
        int pointCount = points.size();
        int[] parent = new int[pointCount];
        for (int i = 0; i < pointCount; i++) {
            parent[i] = i;
        }
        for (List<Integer> connection : connections) {
            union(parent, connection.get(0), connection.get(1));
        }

        Map<Integer, List<ConstellationPoint>> groups = new LinkedHashMap<>();
        for (ConstellationPoint point : points) {
            groups.computeIfAbsent(find(parent, point.id()), key -> new ArrayList<>()).add(point);
        }

        List<List<ConstellationPoint>> groupList = new ArrayList<>(groups.values());
        List<List<Integer>> result = new ArrayList<>(connections);

        while (groupList.size() > 1) {
            List<ConstellationPoint> groupA = groupList.get(0);
            List<ConstellationPoint> groupB = groupList.get(1);

            ConstellationPoint[] closestPair = findClosestPair(groupA, groupB);
            result.add(List.of(
                    Math.min(closestPair[0].id(), closestPair[1].id()),
                    Math.max(closestPair[0].id(), closestPair[1].id())
            ));

            groupA.addAll(groupB);
            groupList.remove(1);
        }

        return result;
    }

    /**
     * 용도: 두 그룹 사이의 최단 거리 점 쌍 탐색.
     * 두 그룹에 속한 모든 점 쌍의 거리를 비교해 가장 가까운 점 쌍을 반환한다.
     */
    private ConstellationPoint[] findClosestPair(List<ConstellationPoint> groupA, List<ConstellationPoint> groupB) {
        ConstellationPoint bestA = groupA.get(0);
        ConstellationPoint bestB = groupB.get(0);
        double bestDistance = Double.MAX_VALUE;

        for (ConstellationPoint a : groupA) {
            for (ConstellationPoint b : groupB) {
                double currentDistance = distance(a, b);
                if (currentDistance < bestDistance) {
                    bestDistance = currentDistance;
                    bestA = a;
                    bestB = b;
                }
            }
        }

        return new ConstellationPoint[]{bestA, bestB};
    }

    private double distance(ConstellationPoint a, ConstellationPoint b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private String unorderedKey(int a, int b) {
        return Math.min(a, b) + "-" + Math.max(a, b);
    }

    private int find(int[] parent, int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x;
    }

    private void union(int[] parent, int a, int b) {
        int rootA = find(parent, a);
        int rootB = find(parent, b);
        if (rootA != rootB) {
            parent[rootA] = rootB;
        }
    }
}