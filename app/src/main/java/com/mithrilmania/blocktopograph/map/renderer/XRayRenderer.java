package com.mithrilmania.blocktopograph.map.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.mithrilmania.blocktopograph.chunk.Chunk;
import com.mithrilmania.blocktopograph.chunk.ChunkManager;
import com.mithrilmania.blocktopograph.chunk.Version;
import com.mithrilmania.blocktopograph.chunk.terrain.TerrainChunkData;
import com.mithrilmania.blocktopograph.map.Block;
import com.mithrilmania.blocktopograph.map.Dimension;


public class XRayRenderer implements MapRenderer {

    /*
    TODO make the X-ray viewable blocks configurable, without affecting performance too much...
     */

    /**
     * Render a single chunk to provided bitmap (bm)
     *
     * @param chunk        ChunkManager, provides chunks, which provide chunk-data
     * @param canvas       Bitmap to render to
     * @param dimension    Mapped dimension
     * @param chunkX       X chunk coordinate (x-block coord / Chunk.WIDTH)
     * @param chunkZ       Z chunk coordinate (z-block coord / Chunk.LENGTH)
     * @param pX           texture X pixel coord to start rendering to
     * @param pY           texture Y pixel coord to start rendering to
     * @param pW           width (X) of one block in pixels
     * @param pL           length (Z) of one block in pixels
     * @param paint
     * @param version
     * @param chunkManager
     * @return bm is returned back
     * @throws Version.VersionException when the version of the chunk is unsupported.
     */
    public void renderToBitmap(Chunk chunk, Canvas canvas, Dimension dimension, int chunkX, int chunkZ, int pX, int pY, int pW, int pL, Paint paint, Version version, ChunkManager chunkManager) throws Version.VersionException {

        //the bottom sub-chunk is sufficient to get heightmap data.
        TerrainChunkData data;

        int x, y, z, color, i, j, tX, tY;

        //render width in blocks
        int rW = 16;
        int size2D = rW * (16);
        int index2D;
        Block[] bestBlock = new Block[size2D];

        int[] minValue = new int[size2D];
        int bValue;
        Block block;

        int average;
        int r, g, b;

        int subChunk;
        for (subChunk = 0; subChunk < version.subChunks; subChunk++) {
            data = chunk.getTerrain((byte) subChunk);
            if (data == null || !data.loadTerrain()) break;

            for (z = 0; z < 16; z++) {
                for (x = 0; x < 16; x++) {

                    for (y = 0; y < version.subChunkHeight; y++) {
                        block = Block.getBlock(data.getBlockTypeId(x, y, z) & 0xff, 0);

                        index2D = (z * rW) + x;
                        if (block == null || block.id <= 1)
                            continue;
                        else if (block == Block.B_56_0_DIAMOND_ORE) {
                            bestBlock[index2D] = block;
                            break;
                        } else if (block == Block.B_129_0_EMERALD_ORE) bValue = 8;
                        else if (block == Block.B_153_0_QUARTZ_ORE) bValue = 7;
                        else if (block == Block.B_14_0_GOLD_ORE) bValue = 6;
                        else if (block == Block.B_15_0_IRON_ORE) bValue = 5;
                        else if (block == Block.B_73_0_REDSTONE_ORE) bValue = 4;
                        else if (block == Block.B_21_0_LAPIS_ORE) bValue = 3;
                            //else if(block == Block.COAL_ORE) bValue = 2;
                            //else if(b == Block.LAVA || b == Block.STATIONARY_LAVA) bValue = 1;
                        else bValue = 0;

                        if (bValue > minValue[index2D]) {
                            minValue[index2D] = bValue;
                            bestBlock[index2D] = block;
                        }
                    }
                }
            }
        }

        if (subChunk == 0) {
            MapType.CHESS.renderer.renderToBitmap(chunk, canvas, dimension, chunkX, chunkZ, pX, pY, pW, pL, paint, version, chunkManager);
            return;
        }

        for (z = 0, tY = pY; z < 16; z++, tY += pL) {
            for (x = 0, tX = pX; x < 16; x++, tX += pW) {
                block = bestBlock[((z - 0) * rW) + (x - 0)];
                if (block == null || block.color == null) {
                    color = 0xff000000;
                } else {

                    r = block.color.red;
                    g = block.color.green;
                    b = block.color.blue;
                    average = (r + g + b) / 3;

                    //make the color better recognizable
                    r += r > average ? (r - average) * (r - average) : -(r - average) * (r - average);
                    g += g > average ? (g - average) * (g - average) : -(g - average) * (g - average);
                    b += b > average ? (b - average) * (b - average) : -(b - average) * (b - average);

                    if (r > 0xff) r = 0xff;
                    else if (r < 0) r = 0;
                    if (g > 0xff) g = 0xff;
                    else if (g < 0) g = 0;
                    if (b > 0xff) b = 0xff;
                    else if (b < 0) b = 0;

                    color = (r << 16) | (g << 8) | (b) | 0xff000000;
                }
                paint.setColor(color);
                canvas.drawRect(new Rect(tX, tY, tX + pW, tY + pL), paint);


            }
        }
    }

}
