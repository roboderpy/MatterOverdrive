/*
 * This file is part of Matter Overdrive
 * Copyright (c) 2015., Simeon Radivoev, All rights reserved.
 *
 * Matter Overdrive is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Matter Overdrive is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Matter Overdrive.  If not, see <http://www.gnu.org/licenses>.
 */

package matteroverdrive.handler.quest;

import matteroverdrive.data.quest.Quest;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Simeon on 11/19/2015.
 */
public class Quests
{
    private static final int MIN_QUEST_BIT = 0;
    private static final int MAX_QUEST_IDS = Short.MAX_VALUE;
    private BitSet bitSet;
    private Map<Integer,Quest> questMap;
    private Map<Quest,Integer> questIntegerMap;
    private Map<String,Quest> stringQuestMap;

    public Quests()
    {
        bitSet = new BitSet(MAX_QUEST_IDS);
        questMap = new HashMap<>();
        questIntegerMap = new HashMap<>();
        stringQuestMap = new HashMap<>();
    }

    public Quest getQuestWithID(int questID)
    {
        return questMap.get(questID);
    }

    public int getQuestID(Quest quest)
    {
        return questIntegerMap.get(quest);
    }

    public Quest getQuestByName(String name)
    {
        return stringQuestMap.get(name);
    }

    public void registerQuest(String name,Quest quest)
    {
        int id = bitSet.nextClearBit(MIN_QUEST_BIT);
        questMap.put(id,quest);
        questIntegerMap.put(quest,id);
        stringQuestMap.put(name,quest);
        bitSet.set(id,true);
    }

    public void registerQuestAt(Integer id,String name,Quest quest)
    {
        questMap.put(id,quest);
        questIntegerMap.put(quest,id);
        bitSet.set(id,true);
        stringQuestMap.put(name,quest);
    }

    public Set<String> getAllQuestName()
    {
        return stringQuestMap.keySet();
    }
}