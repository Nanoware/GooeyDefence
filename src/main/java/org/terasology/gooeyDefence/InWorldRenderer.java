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

import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.rendering.world.selection.BlockSelectionRenderer;
import org.terasology.utilities.Assets;

import java.util.List;

@RegisterSystem
public class InWorldRenderer extends BaseComponentSystem implements RenderSystem {

    private BlockSelectionRenderer blockSelectionRenderer;

    @In
    private DefenceWorldProvider worldProvider;

    @Override
    public void initialise() {
        blockSelectionRenderer = new BlockSelectionRenderer(Assets.getTexture("GooeyDefence:PathBlock").get());
    }

    @Override
    public void renderAlphaBlend() {
        blockSelectionRenderer.beginRenderOverlay();
        List<List<Vector3i>> paths = worldProvider.getPaths();
        for (List<Vector3i> path : paths) {
            if (path != null) {
                for (Vector3i pos : path) {
                    blockSelectionRenderer.renderMark2(Vector3i.up().add(pos));
                }
            }
        }
        blockSelectionRenderer.endRenderOverlay();
    }

    @Override
    public void renderOpaque() {

    }

    @Override
    public void renderOverlay() {

    }

    @Override
    public void renderShadows() {

    }
}
