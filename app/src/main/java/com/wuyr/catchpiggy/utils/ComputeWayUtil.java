package com.wuyr.catchpiggy.utils;

import com.wuyr.catchpiggy.customize.views.Item;
import com.wuyr.catchpiggy.models.WayData;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

/**
 * Created by wuyr on 17-10-16 下午11:54.
 */

public class ComputeWayUtil {
    private static final int STATE_WALKED = 3;

    public static WayData findNextUnSelected(int[][] items, List<WayData> ignorePos, WayData currentPos) {
        int verticalCount = items.length;
        int horizontalCount = items[0].length;
        Queue<WayData> way = new ArrayDeque<>();
        int[][] pattern = new int[verticalCount][horizontalCount];
        for (int vertical = 0; vertical < verticalCount; vertical++) {
            System.arraycopy(items[vertical], 0, pattern[vertical], 0, horizontalCount);
        }
        way.offer(currentPos);
        pattern[currentPos.y][currentPos.x] = STATE_WALKED;
        while (!way.isEmpty()) {
            WayData header = way.poll();
            List<WayData> directions = getCanArrivePosUnchecked(pattern, header);
            for (int i = 0; i < directions.size(); i++) {
                WayData direction = directions.get(i);
                if (!currentPos.equals(direction) && items[direction.y][direction.x] == Item.STATE_UNSELECTED && !(ignorePos != null && ignorePos.contains(direction))) {
                    return direction;
                } else {
                    way.offer(direction);
                }
            }
        }
        return null;
    }

    public static WayData findWay(int level, int[][] items, WayData currentPos, List<WayData> data) {
        if (level < 0 || level > 10) {
            List<WayData> list = findWay2(items, currentPos);
            if (list != null && list.size() >= 2) {
                return list.get(1);
            }
        }
        WayData result = null;
        for (WayData tmp : data) {
            if (!tmp.isBlock) {
                result = tmp;
                break;
            }
        }
        return result;
    }

    /**
     * 当前pos先入队
     * 进入循环
     * 队头出队
     * 根据当前pos拿到nextPos[]，遍历
     * 从header开始添加，每次创建新的list对象储存
     * 每次检查是否已到达边界
     * 入队当前遍历的pos
     * 添加新的全部，开始下一轮
     */
    public static List<WayData> findWay2(int[][] items, WayData currentPos) {
        int verticalCount = items.length;
        int horizontalCount = items[0].length;
        Queue<WayData> way = new ArrayDeque<>();
        List<List<WayData>> footprints = new ArrayList<>();
        int[][] pattern = new int[verticalCount][horizontalCount];
        for (int vertical = 0; vertical < verticalCount; vertical++) {
            System.arraycopy(items[vertical], 0, pattern[vertical], 0, horizontalCount);
        }

        way.offer(currentPos);
        List<WayData> temp = new ArrayList<>();
        temp.add(currentPos);
        footprints.add(temp);
        pattern[currentPos.y][currentPos.x] = STATE_WALKED;
        while (!way.isEmpty()) {
            WayData header = way.poll();
            List<WayData> directions = getCanArrivePos(pattern, header);
            List<List<WayData>> list = new ArrayList<>();
            for (int i = 0; i < directions.size(); i++) {
                WayData direction = directions.get(i);
                for (List<WayData> tmp : footprints) {
                    if (canLinks(header, tmp)) {
                        List<WayData> list2 = new ArrayList<>();
                        list2.addAll(tmp);
                        list2.add(direction);
                        list.add(list2);
                    }
                }
                if (isEdge(verticalCount, horizontalCount, direction)) {
                    if (!list.isEmpty()) {
                        footprints.addAll(list);
                    }

                    for (List<WayData> list2 : footprints) {
                        if (!list2.isEmpty() && isEdge2(verticalCount, horizontalCount, list2)) {
                            return list2;
                        }
                    }
                }
                way.offer(direction);

            }
            if (!list.isEmpty()) {
                footprints.addAll(list);
            }
        }
        return null;
    }

    public static boolean isCurrentPositionCanSet(int[][] items, WayData[] occupiedPos, WayData currentPos, List<WayData> result) {
        int verticalCount = items.length;
        int horizontalCount = items[0].length;
        Queue<WayData> way = new ArrayDeque<>();
        int[][] pattern = new int[verticalCount][horizontalCount];
        for (int vertical = 0; vertical < verticalCount; vertical++) {
            System.arraycopy(items[vertical], 0, pattern[vertical], 0, horizontalCount);
        }
        for (WayData tmp : occupiedPos) {
            if (tmp != null) {
                pattern[tmp.y][tmp.x] = Item.STATE_UNSELECTED;
            }
        }

        way.offer(currentPos);
        pattern[currentPos.y][currentPos.x] = STATE_WALKED;
        if (items[currentPos.y][currentPos.x] != Item.STATE_SELECTED) {
            result.add(currentPos);
        }
        while (!way.isEmpty()) {
            WayData header = way.poll();
            List<WayData> directions = getCanArrivePos(pattern, header);
            for (int i = 0; i < directions.size(); i++) {
                WayData direction = directions.get(i);
                result.add(direction);
                way.offer(direction);
            }
        }
        int count = 0;
        for (WayData tmp : occupiedPos) {
            if (tmp != null && result.contains(tmp)) {
                count++;
            }
        }
        return count < result.size();
    }

    public static List<WayData> findWay4(int[][] items, WayData[] ignorePositions, WayData currentPos) {
        int verticalCount = items.length;
        int horizontalCount = items[0].length;
        Queue<WayData> way = new ArrayDeque<>();
        List<List<WayData>> footprints = new ArrayList<>();
        int[][] pattern = new int[verticalCount][horizontalCount];
        for (int vertical = 0; vertical < verticalCount; vertical++) {
            System.arraycopy(items[vertical], 0, pattern[vertical], 0, horizontalCount);
        }
        for (WayData tmp : ignorePositions) {
            if (tmp != null) {
                pattern[tmp.y][tmp.x] = Item.STATE_UNSELECTED;
            }
        }
        way.offer(currentPos);
        List<WayData> temp = new ArrayList<>();
        temp.add(currentPos);
        footprints.add(temp);
        pattern[currentPos.y][currentPos.x] = STATE_WALKED;
        while (!way.isEmpty()) {
            WayData header = way.poll();
            List<WayData> directions = getCanArrivePos(pattern, header);
            List<List<WayData>> list = new ArrayList<>();
            for (int i = 0; i < directions.size(); i++) {
                WayData direction = directions.get(i);
                for (List<WayData> tmp : footprints) {
                    if (canLinks(header, tmp)) {
                        List<WayData> list2 = new ArrayList<>();
                        list2.addAll(tmp);
                        list2.add(direction);
                        list.add(list2);
                    }
                }
                if (isEdge(verticalCount, horizontalCount, direction)) {
                    if (!list.isEmpty()) {
                        footprints.addAll(list);
                    }

                    for (List<WayData> list2 : footprints) {
                        if (!list2.isEmpty() && isEdge2(verticalCount, horizontalCount, list2)) {
                            return list2;
                        }
                    }
                }
                way.offer(direction);

            }
            if (!list.isEmpty()) {
                footprints.addAll(list);
            }
        }
        return footprints.isEmpty() ? null : footprints.get(footprints.size() - 1);
    }

    public static List<WayData> findWay3(int[][] items, WayData currentPos) {
        int verticalCount = items.length;
        int horizontalCount = items[0].length;
        Queue<WayData> way = new ArrayDeque<>();
        List<List<WayData>> footprints = new ArrayList<>();
        int[][] pattern = new int[verticalCount][horizontalCount];
        for (int vertical = 0; vertical < verticalCount; vertical++) {
            System.arraycopy(items[vertical], 0, pattern[vertical], 0, horizontalCount);
        }
        way.offer(currentPos);
        List<WayData> temp = new ArrayList<>();
        temp.add(currentPos);
        footprints.add(temp);
        pattern[currentPos.y][currentPos.x] = STATE_WALKED;
        List<List<WayData>> result = new ArrayList<>();
        while (!way.isEmpty()) {
            WayData header = way.poll();
            List<WayData> directions = getCanArrivePos(pattern, header);
            List<List<WayData>> list = new ArrayList<>();
            for (int i = 0; i < directions.size(); i++) {
                WayData direction = directions.get(i);
                for (List<WayData> tmp : footprints) {
                    if (canLinks(header, tmp)) {
                        List<WayData> list2 = new ArrayList<>();
                        list2.addAll(tmp);
                        list2.add(direction);
                        list.add(list2);
                    }
                }
                if (isEdge(verticalCount, horizontalCount, direction)) {
                    if (!list.isEmpty()) {
                        footprints.addAll(list);
                    }

                    for (List<WayData> list2 : footprints) {
                        if (!list2.isEmpty() && isEdge2(verticalCount, horizontalCount, list2)) {
                            result.add(list2);
                        }
                    }
                }
                way.offer(direction);
            }
            if (!list.isEmpty()) {
                footprints.addAll(list);
            }
        }
        Collections.shuffle(result);
        return result.isEmpty() ? null : result.get(0);
    }

    public static boolean isEdge(int verticalCount, int horizontalCount, WayData direction) {
        return direction.x == 0 || direction.x == horizontalCount - 1 || direction.y == 0 || direction.y == verticalCount - 1;
    }

    private static boolean isEdge2(int verticalCount, int horizontalCount, List<WayData> list2) {
        return list2.get(list2.size() - 1).y == 0
                || list2.get(list2.size() - 1).x == 0
                || list2.get(list2.size() - 1).y == verticalCount - 1
                || list2.get(list2.size() - 1).x == horizontalCount - 1;
    }

    private static boolean canLinks(WayData src, List<WayData> list) {
        boolean isCanLinks = false;
        if (!list.isEmpty()) {
            WayData lastItem = list.get(list.size() - 1);
            if (lastItem.y == src.y && lastItem.x == src.x) {
                isCanLinks = true;
            }
        }
        return isCanLinks;
    }

    public static List<WayData> getCanArrivePos(int[][] items, WayData currentPos) {
        int verticalCount = items.length;
        int horizontalCount = items[0].length;
        List<WayData> result = new ArrayList<>();
        int offset = currentPos.y % 2 == 0 ? 0 : 1, offset2 = currentPos.y % 2 == 0 ? 1 : 0;
        for (int i = 0; i < 6; i++) {
            WayData tmp = getNextPosition(currentPos, offset, offset2, i);
            if ((tmp.x > -1 && tmp.x < horizontalCount) && (tmp.y > -1 && tmp.y < verticalCount)) {
                if (items[tmp.y][tmp.x] != Item.STATE_SELECTED && items[tmp.y][tmp.x] != Item.STATE_OCCUPIED && items[tmp.y][tmp.x] != STATE_WALKED) {
                    result.add(tmp);
                    items[tmp.y][tmp.x] = STATE_WALKED;
                }
            }
        }
        Collections.shuffle(result);
        return result;
    }

    private static List<WayData> getCanArrivePosUnchecked(int[][] items, WayData currentPos) {
        int verticalCount = items.length;
        int horizontalCount = items[0].length;
        List<WayData> result = new ArrayList<>();
        int offset = currentPos.y % 2 == 0 ? 0 : 1, offset2 = currentPos.y % 2 == 0 ? 1 : 0;
        for (int i = 0; i < 6; i++) {
            WayData tmp = getNextPosition(currentPos, offset, offset2, i);
            if ((tmp.x > -1 && tmp.x < horizontalCount) && (tmp.y > -1 && tmp.y < verticalCount)) {
                result.add(tmp);
                items[tmp.y][tmp.x] = STATE_WALKED;
            }
        }
        Collections.shuffle(result);
        return result;
    }

    public static boolean isHasExit(int[][] items, WayData currentPos) {
        int verticalCount = items.length;
        int horizontalCount = items[0].length;
        Queue<WayData> way = new ArrayDeque<>();
        int[][] pattern = new int[verticalCount][horizontalCount];
        for (int vertical = 0; vertical < verticalCount; vertical++) {
            System.arraycopy(items[vertical], 0, pattern[vertical], 0, horizontalCount);
        }
        way.offer(currentPos);
        pattern[currentPos.y][currentPos.x] = STATE_WALKED;
        while (!way.isEmpty()) {
            WayData header = way.poll();
            List<WayData> directions = getCanArrivePos(pattern, header);
            for (WayData tmp : directions) {
                if (isEdge(verticalCount, horizontalCount, tmp)) {
                    return true;
                }
                way.offer(tmp);
            }
        }
        return false;
    }

    public static void printItemStatus(int[][] items) {
        int verticalCount = items.length;
        int horizontalCount = items[0].length;
        StringBuilder s = new StringBuilder();
        for (int vertical = 0; vertical < verticalCount; vertical++) {
            for (int horizontal = 0; horizontal < horizontalCount; horizontal++) {
                s.append(items[vertical][horizontal] == Item.STATE_SELECTED ? "▆" :
                        items[vertical][horizontal] == Item.STATE_OCCUPIED ? "0" : ".").append("  ");
            }
            LogUtil.print((vertical % 2 == 0 ? "  " : "") + s);
            s = new StringBuilder();
        }
        LogUtil.print("-\n\n-");
    }

    private static WayData getNextPosition(WayData currentPos, int offset, int offset2, int direction) {
        WayData result = new WayData(currentPos.x, currentPos.y);
        switch (direction) {
            case 0:
                result.x -= 1;
                break;
            case 1:
                result.x -= offset;
                result.y -= 1;
                break;
            case 2:
                result.x -= offset;
                result.y += 1;
                break;
            case 3:
                result.x += 1;
                break;
            case 4:
                result.x += offset2;
                result.y -= 1;
                break;
            case 5:
                result.x += offset2;
                result.y += 1;
                break;
        }
        return result;
    }

}