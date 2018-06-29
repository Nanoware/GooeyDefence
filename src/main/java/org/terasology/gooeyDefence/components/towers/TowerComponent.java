/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.gooeyDefence.components.towers;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;

import java.util.HashSet;
import java.util.Set;

/**
 * Component for the abstract tower entity.
 * Stores the ID's of all the blocks that make up the tower.
 */
public class TowerComponent implements Component {
    public Set<Long> cores = new HashSet<>();
    public Set<Long> effector = new HashSet<>();
    public Set<Long> targeter = new HashSet<>();
    public Set<Long> plains = new HashSet<>();
    public Set<EntityRef> lastTargets = new HashSet<>();
}
