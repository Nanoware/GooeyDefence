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
package org.terasology.gooeyDefence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.flexiblepathfinding.PathfinderSystem;
import org.terasology.gooeyDefence.components.DestructibleBlockComponent;
import org.terasology.gooeyDefence.components.ShrineComponent;
import org.terasology.gooeyDefence.events.DamageShrineEvent;
import org.terasology.gooeyDefence.events.OnFieldActivated;
import org.terasology.logic.characters.events.AttackEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.health.DestroyEvent;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.logic.inventory.events.DropItemEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.world.OnChangedBlock;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.entity.CreateBlockDropsEvent;
import org.terasology.world.block.entity.placement.PlaceBlocks;
import org.terasology.world.block.items.BlockItemFactory;
import org.terasology.world.sun.CelestialSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class that provides dynamic information about the Defence Field.
 * Also performs all high level actions, delegating specifics to other systems.
 * Static information is given by {@link DefenceField}
 *
 * @see DefenceField
 */
@Share(DefenceWorldManager.class)
@RegisterSystem
public class DefenceWorldManager extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(DefenceWorldManager.class);
    private int numPathCalculation = 0;


    private List<List<Vector3i>> paths = new ArrayList<>(Collections.nCopies(DefenceField.entranceCount(), null));
    @In
    private PathfinderSystem pathfinderSystem;
    @In
    private CelestialSystem celestialSystem;
    @In
    private EntityManager entityManager;

    @In
    private BlockManager blockManager;
    private BlockItemFactory factory;


    @Override
    public void initialise() {
        if (!celestialSystem.isSunHalted()) {
            celestialSystem.toggleSunHalting(0.5f);
        }
        factory = new BlockItemFactory(entityManager);
    }

    /**
     * Make blocks destroy instantly
     */
    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
    public void onAttackEntity(AttackEvent event, EntityRef targetEntity) {
        event.consume();
        if (targetEntity.hasComponent(DestructibleBlockComponent.class)) {
            targetEntity.send(new DestroyEvent(event.getInstigator(), event.getDirectCause(), EngineDamageTypes.PHYSICAL.get()));
        }
    }

    /**
     * Make the world gen blocks drop a plain block as an item
     */
    @ReceiveEvent
    public void onCreateBlockDrops(CreateBlockDropsEvent event, EntityRef entity, LocationComponent component) {
        if (entity.getParentPrefab().getName().equals("GooeyDefence:PlainWorldGen")) {
            event.consume();
            factory.newInstance(blockManager.getBlockFamily("GooeyDefence:Plain")).send(new DropItemEvent(component.getWorldPosition()));
        }
    }


    @ReceiveEvent
    public void onDamageShrine(DamageShrineEvent event, EntityRef entity) {
        ShrineComponent component = DefenceField.getShrineEntity().getComponent(ShrineComponent.class);
        if (component != null) {
            component.reduceHealth(event.getDamage());
        }
    }

    /**
     * Update path on a block placed
     */
    @ReceiveEvent
    public void onPlaceBlocks(PlaceBlocks event, EntityRef entity) {
        if (DefenceField.isFieldActivated()) {
            calculatePaths();
        }
    }
    /**
     * Update path on a block removed
     */
    @ReceiveEvent
    public void onChangedBlock(OnChangedBlock event, EntityRef entity) {
        if (DefenceField.isFieldActivated()) {
            calculatePaths();
        }
    }

    /**
     * Activate the world on a interaction
     * TODO: remove and replace with UI interaction
     */
    @ReceiveEvent
    public void onActivate(ActivateEvent event, EntityRef entity) {
        if (!DefenceField.isFieldActivated()) {
            setupWorld();
        }
    }

    /**
     * Initialises the defence field
     */
    private void setupWorld() {
        logger.info("Setting up the world.");

        DefenceField.getShrineEntity().send(new OnFieldActivated());

        calculatePaths(DefenceField::setFieldActivated);
    }


    /**
     * Calculate the path from an entrance to the centre
     *
     * @param id       The entrance to calculate from
     * @param callback A callback to be invoked after all pending path calculations have completed
     */
    private void calculatePath(int id, Runnable callback) {
        numPathCalculation++;
        pathfinderSystem.requestPath(
                DefenceField.fieldCentre(), DefenceField.entrancePos(id), (path, end) -> {
                    paths.set(id, path);
                    numPathCalculation--;
                    if (callback != null && numPathCalculation <= 0) {
                        callback.run();
                    }
                });
    }

    /**
     * Calculate paths from all the entrances to the centre.
     */
    private void calculatePaths() {
        calculatePaths(null);
    }

    /**
     * Calculates all paths from all entrance to the centre
     *
     * @param callback A callback to invoke when all paths have finished being calculated
     */
    private void calculatePaths(Runnable callback) {
        for (int id = 0; id < DefenceField.entranceCount(); id++) {
            calculatePath(id, callback);
        }
    }

    /**
     * @return All paths from entrance to centre
     */
    public List<List<Vector3i>> getPaths() {
        return paths;
    }

    /**
     * Get a path. Will return null if the path has not been calculated yet.
     *
     * @param pathID Which entrance the path should come from
     * @return The given path, or null if it doesn't exist yet.
     */
    public List<Vector3i> getPath(int pathID) {
        return paths.get(pathID);
    }
}
