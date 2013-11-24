package com.westeroscraft.westerosblocks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.westeroscraft.westerosblocks.blocks.WCBedBlock;
import com.westeroscraft.westerosblocks.blocks.WCCropBlock;
import com.westeroscraft.westerosblocks.blocks.WCCuboidBlock;
import com.westeroscraft.westerosblocks.blocks.WCCuboidNEBlock;
import com.westeroscraft.westerosblocks.blocks.WCCuboidNSEWBlock;
import com.westeroscraft.westerosblocks.blocks.WCCuboidNSEWUDBlock;
import com.westeroscraft.westerosblocks.blocks.WCDoorBlock;
import com.westeroscraft.westerosblocks.blocks.WCFenceBlock;
import com.westeroscraft.westerosblocks.blocks.WCHalfDoorBlock;
import com.westeroscraft.westerosblocks.blocks.WCLadderBlock;
import com.westeroscraft.westerosblocks.blocks.WCLayerBlock;
import com.westeroscraft.westerosblocks.blocks.WCLeavesBlock;
import com.westeroscraft.westerosblocks.blocks.WCLogBlock;
import com.westeroscraft.westerosblocks.blocks.WCPaneBlock;
import com.westeroscraft.westerosblocks.blocks.WCPlantBlock;
import com.westeroscraft.westerosblocks.blocks.WCRailBlock;
import com.westeroscraft.westerosblocks.blocks.WCSandBlock;
import com.westeroscraft.westerosblocks.blocks.WCSlabBlock;
import com.westeroscraft.westerosblocks.blocks.WCSolidBlock;
import com.westeroscraft.westerosblocks.blocks.WCSoulSandBlock;
import com.westeroscraft.westerosblocks.blocks.WCSoundBlock;
import com.westeroscraft.westerosblocks.blocks.WCStairBlock;
import com.westeroscraft.westerosblocks.blocks.WCStepSound;
import com.westeroscraft.westerosblocks.blocks.WCTorchBlock;
import com.westeroscraft.westerosblocks.blocks.WCWallBlock;
import com.westeroscraft.westerosblocks.blocks.WCWebBlock;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.block.StepSound;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;

//
// Template for block configuration data (populated using GSON)
//
public class WesterosBlockDef {
    private static final float DEF_FLOAT = -999.0F;
    private static final int DEF_INT = -999;
    
    public String blockName;                // Locally unique block name
    public int blockID = DEF_INT;           // Block ID number (default)
    public int[] blockIDs = null;           // Block ID numbers (default) - for definitions with more than one block
    public String blockType = "solid";      // Block type ('solid', 'liquid', 'plant', 'log', 'stairs', etc)
    public float hardness = DEF_FLOAT;      // Block hardness
    public String stepSound = null;         // Step sound (powder, wood, gravel, grass, stone, metal, glass, cloth, sand, snow, ladder, anvil)
    public String material = null;          // Generic material (ai, grass, ground, wood, rock, iron, anvil, water, lava, leaves, plants, vine, sponge, etc)
    public float resistance = DEF_FLOAT;    // Explosion resistance
    public int lightOpacity = DEF_INT;      // Light opacity
    public List<HarvestLevel> harvestLevel = null;  // List of harvest levels
    public int harvestItemID = DEF_INT;     // Harvest item ID
    public int fireSpreadSpeed = 0;         // Fire spread speed
    public int flamability = 0;             // Flamability
    public String creativeTab = null;       // Creative tab for items
    public float lightValue = 0.0F;         // Emitted light level (0.0-1.0)
    public List<Subblock> subBlocks = null; // Subblocks
    public String modelBlockName = null;    // Name of solid block modelled from (used by 'stairs' type) - can be number of block ID
    public int modelBlockMeta = DEF_INT;    // Metadata of model block to use 
    public BoundingBox boundingBox = null;  // Bounding box
    public String colorMult = "#FFFFFF";    // Color multiplier ("#rrggbb' for fixed value, 'foliage', 'grass', 'water')
    public String type = "";                // Type field (used for plant types or other block type specific values)
    public boolean alphaRender = false;     // If true, do render on pass 2 (for alpha blending)
    public boolean nonOpaque = false;       // If true, does not block visibility of shared faces (solid blocks) and doesn't allow torches 
                                            // ('solid', 'sound', 'sand', 'soulsand' blocks)
    
    public static class HarvestLevel {
        public String tool;
        public int level;
    }
    public static class BoundingBox {
        public float xMin = 0.0F;
        public float xMax = 1.0F;
        public float yMin = 0.0F;
        public float yMax = 1.0F;
        public float zMin = 0.0F;
        public float zMax = 1.0F;
    }
    public static class Vector {
        float x, y, z;
        
        private void rotate(int xcnt, int ycnt, int zcnt) {
            double xx, yy, zz;
            xx = x - 0.5F; yy = y - 0.5F; zz = z - 0.5F; // Shoft to center of block
            /* Do X rotation */
            double rot = Math.toRadians(xcnt);
            double nval = zz * Math.sin(rot) + yy * Math.cos(rot);
            zz = zz * Math.cos(rot) - yy * Math.sin(rot);
            yy = nval;
            /* Do Y rotation */
            rot = Math.toRadians(ycnt);
            nval = xx * Math.cos(rot) - zz * Math.sin(rot);
            zz = xx * Math.sin(rot) + zz * Math.cos(rot);
            xx = nval;
            /* Do Z rotation */
            rot = Math.toRadians(zcnt);
            nval = yy * Math.sin(rot) + xx * Math.cos(rot);
            yy = yy * Math.cos(rot) - xx * Math.sin(rot);
            xx = nval;
            x = (float)xx + 0.5F; y = (float)yy + 0.5F; z = (float)zz + 0.5F; // Shoft back to corner
            // Clip value
            if (x > 1.0F) x = 1.0F;
            if (y > 1.0F) y = 1.0F;
            if (z > 1.0F) z = 1.0F;
            if (x < 0.0F) x = 0.0F;
            if (y < 0.0F) y = 0.0F;
            if (z < 0.0F) z = 0.0F;
        }

    }
    public static enum CuboidRotation {
        NONE(0, 0, 0, new int[] { 0, 1, 2, 3, 4, 5 }),
        ROTY90(0, 90, 0, new int[] { 0, 1, 4, 5, 3, 2 }),
        ROTY180(0, 180, 0, new int[] { 0, 1, 3, 2, 5, 4 }), 
        ROTY270(0, 270, 0, new int[] { 0, 1, 5, 4, 2, 3 }),
        ROTZ90(0, 0, 90, new int[] { 5, 4, 2, 3, 0, 1 }),
        ROTZ270(0, 0, 270, new int[] { 4, 5, 2, 3, 1, 0 });
        
        int xrot, yrot, zrot;
        int txtidx[];
        
        CuboidRotation(int xr, int yr, int zr, int[] txt_idx) {
            xrot = xr;
            yrot = yr;
            zrot = zr;
            txtidx = txt_idx;
        }
        
    }
    // Shape for normal cuboid (box)
    public static final String SHAPE_BOX = "box";
    // Shape for crossed squares (plant-style) (texture is index 0 in list)
    public static final String SHAPE_CROSSED = "crossed";
    
    public static class Cuboid extends BoundingBox {
        public int[] sideTextures = null;
        public String shape = SHAPE_BOX; // "box" = normal cuboid, "crossed" = plant-style crossed (texture 0)
        
        public Cuboid rotateCuboid(CuboidRotation rot) {
            Cuboid c = new Cuboid();
            Vector v0 = new Vector();
            Vector v1 = new Vector();
            v0.x = xMin; v0.y = yMin; v0.z = zMin;
            v1.x = xMax; v1.y = yMax; v1.z = zMax;
            // Rotate corners
            v0.rotate(rot.xrot, rot.yrot, rot.zrot);
            v1.rotate(rot.xrot, rot.yrot, rot.zrot);
            // Compute net min/max
            c.xMin = Math.min(v0.x,  v1.x);
            c.xMax = Math.max(v0.x,  v1.x);
            c.yMin = Math.min(v0.y,  v1.y);
            c.yMax = Math.max(v0.y,  v1.y);
            c.zMin = Math.min(v0.z,  v1.z);
            c.zMax = Math.max(v0.z,  v1.z);
            if (this.sideTextures != null) {
                c.sideTextures = new int[rot.txtidx.length];
                int cnt = this.sideTextures.length;
                for (int i = 0; i < c.sideTextures.length; i++) {
                    if (i < cnt) {
                        c.sideTextures[i] = this.sideTextures[rot.txtidx[i]];
                    }
                    else {
                        c.sideTextures[i] = this.sideTextures[rot.txtidx[cnt-1]];
                    }
                }
            }
            else {
                c.sideTextures = rot.txtidx;
            }
            return c;
        }
    }
    
    public static class Subblock {
        public int meta;        // Meta value for subblock (base value, if more than one associated with block type)
        public String label;    // Label for item associated with block
        public List<HarvestLevel> harvestLevel = null; // List of harvest levels
        public int harvestItemID = DEF_INT;     // Harvest item ID (-1=use block level)
        public int fireSpreadSpeed = DEF_INT;   // Fire spread speed (-1=use block level)
        public int flamability = DEF_INT;       // Flamability (-1=use block level)
        public float lightValue = DEF_FLOAT;    // Emitted light level (0.0-1.0)
        public int lightOpacity = DEF_INT;      // Light opacity
        public List<String> textures = null;    // List of textures
        public String itemTexture = null;       // Item texture, if any
        public BoundingBox boundingBox = null;  // Bounding box
        public String type = null;              // Block type specific type string (e.g. plant type)
        public int itemTextureIndex = 0;        // Index of texture for item icon
        public String colorMult = null;         // Color multiplier ("#rrggbb' for fixed value, 'foliage', 'grass', 'water')
        public List<Cuboid> cuboids = null;     // List of cuboids composing block (for 'cuboid', and others)
        public List<String> soundList = null;   // List of custom sound names or sound IDs (for 'sound' blocks)
    }

    // Base color multiplier (fixed)
    public static class ColorMultHandler {
        protected int fixedMult;
        
        ColorMultHandler() {
            fixedMult = 0xFFFFFF;
        }
        ColorMultHandler(int mult) {
            fixedMult = mult;
        }
        public int getBlockColor() {
            return fixedMult;
        }
        public int colorMultiplier(IBlockAccess access, int x, int y, int z) {
            return fixedMult;
        }
        protected void setBaseColor() {
        }
        protected void loadRes(String rname, String blkname) {
        }
    }
    // Foliage color multiplier
    public static class FoliageColorMultHandler extends ColorMultHandler {
        FoliageColorMultHandler() {
        }
        @Override
        @SideOnly(Side.CLIENT)
        public int getBlockColor() {
            return ColorizerFoliage.getFoliageColor(0.5, 1.0);
        }
        @Override
        @SideOnly(Side.CLIENT)
        public int colorMultiplier(IBlockAccess access, int x, int y, int z) {
            int red = 0;
            int green = 0;
            int blue = 0;

            for (int xx = -1; xx <= 1; ++xx) {
                for (int zz = -1; zz <= 1; ++zz) {
                    int mult = access.getBiomeGenForCoords(x + xx, z + zz).getBiomeFoliageColor();
                    red += (mult & 0xFF0000) >> 16;
                    green += (mult & 0x00FF00) >> 8;
                    blue += (mult & 0x0000FF);
                }
            }
            return (((red / 9) & 0xFF) << 16) | (((green / 9) & 0xFF) << 8) | ((blue / 9) & 0xFF);
        }
    }
    // Grass color multiplier
    public static class GrassColorMultHandler extends ColorMultHandler {
        GrassColorMultHandler() {
        }
        @Override
        @SideOnly(Side.CLIENT)
        public int getBlockColor() {
            return ColorizerGrass.getGrassColor(0.5, 1.0);
        }
        @Override
        @SideOnly(Side.CLIENT)
        public int colorMultiplier(IBlockAccess access, int x, int y, int z) {
            int red = 0;
            int green = 0;
            int blue = 0;

            for (int xx = -1; xx <= 1; ++xx) {
                for (int zz = -1; zz <= 1; ++zz) {
                    int mult = access.getBiomeGenForCoords(x + xx, z + zz).getBiomeGrassColor();
                    red += (mult & 0xFF0000) >> 16;
                    green += (mult & 0x00FF00) >> 8;
                    blue += (mult & 0x0000FF);
                }
            }
            return (((red / 9) & 0xFF) << 16) | (((green / 9) & 0xFF) << 8) | ((blue / 9) & 0xFF);
        }
    }
    // Water color multiplier
    public static class WaterColorMultHandler extends ColorMultHandler {
        WaterColorMultHandler() {
        }
        @Override
        @SideOnly(Side.CLIENT)
        public int colorMultiplier(IBlockAccess access, int x, int y, int z) {
            int red = 0;
            int green = 0;
            int blue = 0;

            for (int xx = -1; xx <= 1; ++xx) {
                for (int zz = -1; zz <= 1; ++zz) {
                    int mult = access.getBiomeGenForCoords(x + xx, z + zz).getWaterColorMultiplier();
                    red += (mult & 0xFF0000) >> 16;
                    green += (mult & 0x00FF00) >> 8;
                    blue += (mult & 0x0000FF);
                }
            }
            return (((red / 9) & 0xFF) << 16) | (((green / 9) & 0xFF) << 8) | ((blue / 9) & 0xFF);
        }
    }

    public static class PineColorMultHandler extends ColorMultHandler {
        @Override
        @SideOnly(Side.CLIENT)
        public int getBlockColor() {
            return ColorizerFoliage.getFoliageColorPine();
        }
        @Override
        @SideOnly(Side.CLIENT)
        public int colorMultiplier(IBlockAccess access, int x, int y, int z) {
            return ColorizerFoliage.getFoliageColorPine();
        }
    }
    
    public static class BirchColorMultHandler extends ColorMultHandler {
        @Override
        @SideOnly(Side.CLIENT)
        public int getBlockColor() {
            return ColorizerFoliage.getFoliageColorBirch();
        }
        @Override
        @SideOnly(Side.CLIENT)
        public int colorMultiplier(IBlockAccess access, int x, int y, int z) {
            return ColorizerFoliage.getFoliageColorBirch();
        }
    }

    public static class BasicColorMultHandler extends ColorMultHandler {
        @Override
        @SideOnly(Side.CLIENT)
        public int getBlockColor() {
            return ColorizerFoliage.getFoliageColorBasic();
        }
        @Override
        @SideOnly(Side.CLIENT)
        public int colorMultiplier(IBlockAccess access, int x, int y, int z) {
            return ColorizerFoliage.getFoliageColorBasic();
        }
    }

    // Custom color multiplier
    public static class CustomColorMultHandler extends ColorMultHandler {
        private int[] colorBuffer = new int[65536];
        
        CustomColorMultHandler(String rname, String blockName) {
            super();
            
            loadRes(rname, blockName);
        }
        @Override
        @SideOnly(Side.CLIENT)
        public int getBlockColor() {
            return getColor(0.5F, 1.0F);
        }
        @SideOnly(Side.CLIENT)
        @Override
        protected void loadRes(String rname, String blkname) {
            try {
                colorBuffer = TextureUtil.readImageData(Minecraft.getMinecraft().getResourceManager(), new ResourceLocation(rname));
            } catch (IOException e) {
                WesterosBlocks.log.severe(String.format("Invalid color resource '%s' in block '%s'", rname, blkname));
                Arrays.fill(colorBuffer,  0xFFFFFF);
            }
        }

        private int getColor(float tmp, float hum)
        {
            tmp = MathHelper.clamp_float(tmp, 0.0F, 1.0F);
            hum = MathHelper.clamp_float(hum, 0.0F, 1.0F);
            hum *= tmp;
            int i = (int)((1.0D - tmp) * 255.0D);
            int j = (int)((1.0D - hum) * 255.0D);
            return colorBuffer[j << 8 | i];
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public int colorMultiplier(IBlockAccess access, int x, int y, int z) {
            int red = 0;
            int green = 0;
            int blue = 0;

            for (int xx = -1; xx <= 1; ++xx) {
                for (int zz = -1; zz <= 1; ++zz) {
                    BiomeGenBase biome = access.getBiomeGenForCoords(x + xx, z + zz);
                    int mult = getColor(biome.getFloatTemperature(), biome.getFloatRainfall());
                    red += (mult & 0xFF0000) >> 16;
                    green += (mult & 0x00FF00) >> 8;
                    blue += (mult & 0x0000FF);
                }
            }
            return (((red / 9) & 0xFF) << 16) | (((green / 9) & 0xFF) << 8) | ((blue / 9) & 0xFF);
        }
    }

    
    @SideOnly(Side.CLIENT)
    private transient Icon[][] icons_by_meta;
    private transient Icon[] itemicons_by_meta;
    
    private transient Subblock subblock_by_meta[];
    private transient int fireSpreadSpeed_by_meta[] = null;
    private transient int flamability_by_meta[] = null;
    private transient int lightValue_by_meta[] = null;
    private transient int lightOpacity_by_meta[] = null;
    private transient int lightValueInt;
    private transient ColorMultHandler colorMultHandler;
    private transient ColorMultHandler colorMultHandlerByMeta[];
    private transient BoundingBox boundingBoxByMeta[];
    private transient List<String> sounds_by_meta[] = null;
    
    private static final Map<String, Material> materialTable = new HashMap<String, Material>();
    private static final Map<String, StepSound> stepSoundTable = new HashMap<String, StepSound>();
    private static final Map<String, CreativeTabs> tabTable = new HashMap<String, CreativeTabs>();
    private static final Map<String, WesterosBlockFactory> typeTable = new HashMap<String, WesterosBlockFactory>();
    private static final Map<String, ColorMultHandler> colorMultTable = new HashMap<String, ColorMultHandler>();
    
    private int metaMask = 0xF; // Bitmask for translating raw metadata values to base (subblock) meta values
    
    public void setMetaMask(int mask) {
        metaMask = mask;
    }
    
    public Block[] createBlocks() {
        WesterosBlockFactory bf = typeTable.get(blockType);
        if (bf == null) {
            WesterosBlocks.log.severe(String.format("Invalid blockType '%s' in block '%s'", blockType, blockName));
            return null;
        }
        return bf.buildBlockClasses(this);
    }
    
    public Material getMaterial() {
        Material m = materialTable.get(material);
        if (m == null) {
            WesterosBlocks.log.warning(String.format("Invalid material '%s' in block '%s'", material, blockName));
            return Material.rock;
        }
        return m;
    }
    
    public StepSound getStepSound() {
        StepSound ss = stepSoundTable.get(stepSound);
        if (ss == null) {
            WesterosBlocks.log.warning(String.format("Invalid step sound '%s' in block '%s'", stepSound, blockName));
            return Block.soundStoneFootstep;
        }
        return ss;
    }
    
    public CreativeTabs getCreativeTab() {
        CreativeTabs ct = tabTable.get(creativeTab);
        if (ct == null) {
            WesterosBlocks.log.warning(String.format("Invalid tab name '%s' in block '%s'", creativeTab, blockName));
            ct = WesterosBlocksCreativeTab.tabWesterosBlocks;
        }
        return ct;
    }

    @SuppressWarnings("unchecked")
    private void initMeta() {
        subblock_by_meta = new Subblock[metaMask+1];
        lightValueInt = (int)(15.0F * lightValue);
        this.colorMultHandler = getColorHandler(this.colorMult);
        if (this.colorMultHandler == null) {
            WesterosBlocks.log.warning(String.format("Invalid colorMult '%s' in block '%s'", this.colorMult, blockName));
            this.colorMultHandler = getColorHandler("#FFFFFF");
        }
        boundingBoxByMeta = new BoundingBox[16];    // Always do all 16: rotated blocks change this
        for (int i = 0; i < 16; i++) {
            boundingBoxByMeta[i] = this.boundingBox;
        }
        
        if (subBlocks != null) {
            for (Subblock sb : subBlocks) {
                subblock_by_meta[sb.meta] = sb;
                if (sb.fireSpreadSpeed != DEF_INT) {
                    if (fireSpreadSpeed_by_meta == null) {
                        fireSpreadSpeed_by_meta = new int[metaMask+1];
                        if (this.fireSpreadSpeed != DEF_INT) {
                            Arrays.fill(fireSpreadSpeed_by_meta, this.fireSpreadSpeed);
                        }
                    }
                    fireSpreadSpeed_by_meta[sb.meta] = sb.fireSpreadSpeed; 
                }
                if (sb.flamability != DEF_INT) {
                    if (flamability_by_meta == null) {
                        flamability_by_meta = new int[metaMask+1];
                        if (this.flamability != DEF_INT) {
                            Arrays.fill(flamability_by_meta, this.flamability);
                        }
                    }
                    flamability_by_meta[sb.meta] = sb.flamability; 
                }
                if (sb.lightValue != DEF_FLOAT) {
                    if (lightValue_by_meta == null) {
                        lightValue_by_meta = new int[metaMask+1];
                        if (this.lightValue != DEF_FLOAT) {
                            Arrays.fill(lightValue_by_meta, (int)(15.0F * this.lightValue));
                        }
                    }
                    lightValue_by_meta[sb.meta] = (int)(15.0F * sb.lightValue); 
                }
                if (sb.lightOpacity != DEF_INT) {
                    if (lightOpacity_by_meta == null) {
                        lightOpacity_by_meta = new int[metaMask+1];
                        if (this.lightOpacity != DEF_INT) {
                            Arrays.fill(lightOpacity_by_meta, this.lightOpacity);
                        }
                    }
                    lightOpacity_by_meta[sb.meta] = sb.lightOpacity; 
                }
                if (sb.colorMult != null) {
                    if (this.colorMultHandlerByMeta == null) {
                        this.colorMultHandlerByMeta = new ColorMultHandler[metaMask+1];
                        Arrays.fill(this.colorMultHandlerByMeta, this.colorMultHandler);
                    }
                    this.colorMultHandlerByMeta[sb.meta] = getColorHandler(sb.colorMult);
                    if (this.colorMultHandlerByMeta[sb.meta] == null) {
                        WesterosBlocks.log.warning(String.format("Invalid colorMult '%s' in block '%s'", sb.colorMult, blockName));
                        this.colorMultHandlerByMeta[sb.meta] = this.colorMultHandler;
                    }
                }
                if ((sb.boundingBox != null) && (sb.cuboids == null)) {
                    Cuboid c = new Cuboid();
                    c.xMin = sb.boundingBox.xMin;
                    c.xMax = sb.boundingBox.xMax;
                    c.yMin = sb.boundingBox.yMin;
                    c.yMax = sb.boundingBox.yMax;
                    c.zMin = sb.boundingBox.zMin;
                    c.zMax = sb.boundingBox.zMax;
                    sb.cuboids = Collections.singletonList(c);
                }
                if ((sb.cuboids != null) && (sb.boundingBox == null)) {
                    sb.boundingBox = new BoundingBox();
                    sb.boundingBox.xMin = sb.boundingBox.yMin = sb.boundingBox.zMin = 1.0F;
                    sb.boundingBox.xMax = sb.boundingBox.yMax = sb.boundingBox.zMax = 0.0F;
                    for (BoundingBox bb : sb.cuboids) {
                        if (bb.xMin < sb.boundingBox.xMin) sb.boundingBox.xMin = bb.xMin;
                        if (bb.yMin < sb.boundingBox.yMin) sb.boundingBox.yMin = bb.yMin;
                        if (bb.zMin < sb.boundingBox.zMin) sb.boundingBox.zMin = bb.zMin;
                        if (bb.xMax > sb.boundingBox.xMax) sb.boundingBox.xMax = bb.xMax;
                        if (bb.yMax > sb.boundingBox.yMax) sb.boundingBox.yMax = bb.yMax;
                        if (bb.zMax > sb.boundingBox.zMax) sb.boundingBox.zMax = bb.zMax;
                    }
                }
                // If block specific bounding box, copy it to all matching meta slots
                if (sb.boundingBox != null) {
                    for (int i = 0; i < 16; i++) {
                        if ((i & metaMask) == sb.meta) {
                            this.boundingBoxByMeta[i] = sb.boundingBox;
                        }
                    }
                }
                // If custom sounds
                if (sb.soundList != null) {
                    if (this.sounds_by_meta == null) {
                        this.sounds_by_meta = new List[metaMask+1];
                    }
                    this.sounds_by_meta[sb.meta] = sb.soundList;
                }
            }
        }
    }
    
    private Subblock getByMeta(int meta) {
        if (subblock_by_meta == null) {
            initMeta();
        }
        return subblock_by_meta[meta & metaMask];
    }

    // Do standard constructor settings for given block class
    public void doStandardContructorSettings(Block blk) {
        if (this.hardness != DEF_FLOAT) {
            blk.setHardness(this.hardness);
        }
        if (this.lightOpacity != DEF_INT) {
            blk.setLightOpacity(this.lightOpacity);
        }
        if (this.resistance != DEF_FLOAT) {
            blk.setResistance(this.resistance);
        }
        if (this.lightValue != DEF_FLOAT) {
            blk.setLightValue(this.lightValue);
        }
        blk.setUnlocalizedName(this.blockName);
        if (this.stepSound != null) {
            blk.setStepSound(this.getStepSound());
        }
        if ((this.fireSpreadSpeed > 0) || (this.flamability > 0)) {
            Block.setBurnProperties(this.blockID, this.fireSpreadSpeed, this.flamability);
        }
        if (creativeTab != null) {
            blk.setCreativeTab(getCreativeTab());
        }
        if (boundingBox != null) {
            blk.setBlockBounds(boundingBox.xMin, boundingBox.yMin, boundingBox.zMin, boundingBox.xMax, boundingBox.yMax, boundingBox.zMax);
        }
    }
    // Do standard initialize actions
    public void doStandardInitializeActions(Block blk) {
        // Register any harvest levels
        if (harvestLevel != null) { // Do overall first
            for (HarvestLevel hl : harvestLevel) {
                MinecraftForge.setBlockHarvestLevel(blk, hl.tool, hl.level);
            }
        }
        // And do any meta-specific overrides second
        if (this.subBlocks != null) {
            for (Subblock sb : this.subBlocks) {
                if (sb.harvestLevel != null) { // Do overall first
                    for (HarvestLevel hl : sb.harvestLevel) {
                        MinecraftForge.setBlockHarvestLevel(blk, hl.tool, hl.level);
                    }
                }
            }
        }
    }
    
    public String getUnlocalizedName(int blknum) {
        if (blknum == 0)
            return this.blockName;
        else
            return this.blockName + "_" + (blknum+1);
    }
    // Do standard register actions
    public void doStandardRegisterActions(Block blk, Class<? extends ItemBlock> itmclass) {
        doStandardRegisterActions(blk, itmclass, null, 0);
    }
    // Do standard register actions
    public void doStandardRegisterActions(Block blk, Class<? extends ItemBlock> itmclass, Item itm, int idx) {
        // Register the block
        if (itmclass != null) {
            GameRegistry.registerBlock(blk, itmclass, this.getUnlocalizedName(idx));
        }
        else {
            GameRegistry.registerBlock(blk, this.getUnlocalizedName(idx));
        }
        if (itm != null) {
            GameRegistry.registerItem(itm, this.getUnlocalizedName(idx) + "_item");
        }
        // And register strings for each item block
        if ((this.subBlocks != null) && (this.subBlocks.size() > 0)) {
            for (Subblock sb : this.subBlocks) {
                if (sb.label == null) {
                    sb.label = this.blockName + " " + sb.meta;
                }
                if (itm != null) {
                    LanguageRegistry.addName(new ItemStack(itm, 1, sb.meta), sb.label);
                }
                else {
                    LanguageRegistry.addName(new ItemStack(blk, 1, sb.meta), sb.label);
                }
            }
        }
        if (subblock_by_meta == null) {
            initMeta();
        }
    }

    
    @SideOnly(Side.CLIENT)
    public void doStandardRegisterIcons(IconRegister ir) {
        if (subblock_by_meta == null) {
            initMeta();
        }
        icons_by_meta = new Icon[metaMask+1][];
        if (subBlocks != null) {
            HashMap<String, Icon> map = new HashMap<String, Icon>();
            for (Subblock sb : subBlocks) {
                if (sb.textures == null) {
                    WesterosBlocks.log.warning(String.format("No textures for subblock '%d' of block '%s'", sb.meta, this.blockName));
                    sb.textures = Collections.singletonList("INVALID_" + blockName + "_" + sb.meta);
                }
                icons_by_meta[sb.meta] = new Icon[sb.textures.size()];
                for (int i = 0; i < sb.textures.size(); i++) {
                    String txt = sb.textures.get(i);
                    if (txt.indexOf(':') < 0) {
                        txt = "westerosblocks:" + txt;
                    }
                    Icon ico = map.get(txt);
                    if (ico == null) {
                        ico = ir.registerIcon(txt);
                        map.put(txt, ico);
                    }
                    icons_by_meta[sb.meta][i] = ico;
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void doStandardItemRegisterIcons(IconRegister ir) {
        if (subblock_by_meta == null) {
            initMeta();
        }
        itemicons_by_meta = new Icon[metaMask+1];
        if (subBlocks != null) {
            HashMap<String, Icon> map = new HashMap<String, Icon>();
            for (Subblock sb : subBlocks) {
                if (sb.itemTexture != null) {
                    String txt = sb.itemTexture;
                    if (txt.indexOf(':') < 0) {
                        txt = "westerosblocks:" + txt;
                    }
                    Icon ico = map.get(txt);
                    if (ico == null) {
                        ico = ir.registerIcon(txt);
                        map.put(txt, ico);
                    }
                    itemicons_by_meta[sb.meta] = ico;
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public Icon doStandardIconGet(int side, int meta) {
        if (icons_by_meta == null) {
            return null;
        }
        int m = meta & metaMask;
        if (m >= icons_by_meta.length) {
            m = 0;
        }
        Icon[] ico = icons_by_meta[m];
        if (ico != null) {
            if (side >= ico.length) {
                side = ico.length - 1;
            }
            return ico[side];
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public void getStandardSubBlocks(Block blk, int id, CreativeTabs tab, List<ItemStack> list) {
        if (subBlocks != null) {
            for (Subblock sb : subBlocks) {
                list.add(new ItemStack(blk, 1, sb.meta));
            }
        }
    }
    /*
     * Get default texture (first one)
     */
    public String getFirstTexture() {
        if (subBlocks != null) {
            for (Subblock sb : subBlocks) {
                if ((sb.textures != null) && (sb.textures.size() > 0)) {
                    return sb.textures.get(0);
                }
            }
        }
        return "INVALID_" + this.blockName;
    }

    public int getFlammability(IBlockAccess world, int x, int y, int z, int metadata, ForgeDirection face) {
        metadata &= metaMask;
        if (flamability_by_meta != null) {
            return flamability_by_meta[metadata];
        }
        return this.flamability;
    }

    public List<String> getSoundIDList(int metadata) {
        metadata &= metaMask;
        if (this.sounds_by_meta != null) {
            return this.sounds_by_meta[metadata];
        }
        return null;
    }

    public int getFireSpreadSpeed(World world, int x, int y, int z, int metadata, ForgeDirection face) {
        metadata &= metaMask;
        if (fireSpreadSpeed_by_meta != null) {
            return fireSpreadSpeed_by_meta[metadata];
        }
        return this.fireSpreadSpeed;
    }
        
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        if (this.lightValue_by_meta != null) {
            return this.lightValue_by_meta[world.getBlockMetadata(x,  y,  z) & metaMask];
        }
        return this.lightValueInt;
    }
        
    public int getLightOpacity(World world, int x, int y, int z) {
        if (this.lightOpacity_by_meta != null) {
            return this.lightOpacity_by_meta[world.getBlockMetadata(x,  y,  z) & metaMask];
        }
        return Block.lightOpacity[this.blockID];
    }
    
    public int getBlockColor() {
        return this.colorMultHandler.getBlockColor();
    }
    
    public int getRenderColor(int meta) {
        meta &= metaMask;
        if (this.colorMultHandlerByMeta != null) {
            return this.colorMultHandlerByMeta[meta].getBlockColor();
        }
        return this.colorMultHandler.getBlockColor();
    }
    
    public int colorMultiplier(IBlockAccess access, int x, int y, int z) {
        if (this.colorMultHandlerByMeta != null) {
            int meta = access.getBlockMetadata(x, y, z) & metaMask;
            return this.colorMultHandlerByMeta[meta].colorMultiplier(access, x, y, z);
        }
        return this.colorMultHandler.colorMultiplier(access, x, y, z);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getStandardCreativeItems(Block blk, ArrayList itemList) {
        if (subBlocks != null) {
            for (Subblock sb : subBlocks) {
                itemList.add(new ItemStack(blk, 1, sb.meta));
            }
        }
    }

    // Override default bounding box for given meta
    public void setBoundingBox(int meta, float xmin, float ymin, float zmin, float xmax, float ymax, float zmax) {
        BoundingBox bb = new BoundingBox();
        this.boundingBoxByMeta[meta] = bb;
        bb.xMin = xmin;
        bb.xMax = xmax;
        bb.yMin = ymin;
        bb.yMax = ymax;
        bb.zMin = zmin;
        bb.zMax = zmax;
    }
    
    public BoundingBox getBoundingBox(int meta) {
        return boundingBoxByMeta[meta];
    }
    
    public List<Cuboid> getCuboidList(int meta) {
        meta &= metaMask;
        
        Subblock sb = getByMeta(meta);
        if ((sb != null) && (sb.cuboids != null)) {
           return sb.cuboids;
        }
        return null;
    }
    
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        BoundingBox bb = getBoundingBox(meta);
        if (bb != null) {
            return AxisAlignedBB.getAABBPool().getAABB((double)x + bb.xMin, (double)y + bb.yMin, (double)z + bb.zMin, (double)x + bb.xMax, (double)y + bb.yMax, (double)z + bb.zMax);
        }
        return null;
    }
    public void setBlockBoundsBasedOnState(Block blk, IBlockAccess blockaccess, int x, int y, int z) {
        int meta = blockaccess.getBlockMetadata(x, y, z);
        BoundingBox bb = getBoundingBox(meta);
        if (bb != null) {
            blk.setBlockBounds(bb.xMin,  bb.yMin,  bb.zMin, bb.xMax, bb.yMax, bb.zMax);
        }
        else {
            blk.setBlockBounds(0, 0, 0, 1, 1, 1);
        }
    }
    public boolean shouldSideBeRendered(IBlockAccess access, int x, int y, int z, int side) {
        int meta = access.getBlockMetadata(x, y, z);
        BoundingBox bb = getBoundingBox(meta);
        if (bb == null) return true;
        switch (side) {
            case 0: // Bottom
                return (bb.yMin > 0.0F);
            case 1: // Top
                return (bb.yMax < 1.0F);
            case 2: // Zmin
                return (bb.zMin > 0.0F);
            case 3: // Zmax
                return (bb.zMax < 1.0F);
            case 4: // Xmin
                return (bb.xMin > 0.0F);
            case 5: // Xmax
                return (bb.xMax < 1.0F);
            default:
                return true;
        }
    }
    
    public static void addCreativeTab(String name, CreativeTabs tab) {
        tabTable.put(name,  tab);
    }

    public String getType(int meta) {
        meta &= metaMask;
        
        Subblock sb = getByMeta(meta);
        if ((sb != null) && (sb.type != null)) {
            return sb.type;
        }
        return this.type;
    }

    public EnumPlantType getPlantType(int meta) {
        meta &= metaMask;
        EnumPlantType pt = EnumPlantType.Plains;
        String t = getType(meta);
        if (t != null) {
            pt = EnumPlantType.valueOf(t);
            if (pt == null) {
                WesterosBlocks.log.severe(String.format("Invalid plant type '%s' at meta %d of block '%s'", t, meta, this.blockName));
                pt = EnumPlantType.Plains;
                Subblock sb = getByMeta(meta);
                sb.type = pt.name();
            }
        }
        return pt;
    }

    private static final int[] all_meta = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
    
    // Used by factory classes to confirm that configuration has acceptable meta values
    public boolean validateMetaValues(int[] valid_meta, int[] req_meta) {
        if (valid_meta == null) {
            valid_meta = all_meta;
        }
        if (req_meta == null) {
            req_meta = new int[0];
        }
        if (subBlocks != null) {
            for (Subblock sb : subBlocks) {
                if (sb == null) continue;
                int m = sb.meta;
                boolean match = false;
                for (int vmeta : valid_meta) {
                    if (m == vmeta) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    WesterosBlocks.log.severe(String.format("meta value %d for block '%s' is not valid for block type '%s'", sb.meta, this.blockName, this.blockType));
                    return false;
                }
                // If value exceeds metaMask-ed bits
                if ((m & metaMask) != m) {
                    WesterosBlocks.log.severe(String.format("meta value %d for block '%s' is not valid for block type '%s' - metaMask=%x", sb.meta, this.blockName, this.blockType, metaMask));
                    return false;
                }
            }
        }
        // Check for required values
        for (int req : req_meta) {
            boolean match = false;
            if (subBlocks != null) {
                for (Subblock sb : subBlocks) {
                    if (sb == null) continue;
                    if (sb.meta == req) {
                        match = true;
                        break;
                    }
                }
            }
            if (!match) {
                WesterosBlocks.log.severe(String.format("Block '%s' is missing required meta value %d for block type '%s'", this.blockName, req, this.blockType));
                return false;
            }
        }
        return true;
    }
    
    public static boolean sanityCheck(WesterosBlockDef[] defs) {
        HashSet<String> names = new HashSet<String>();
        BitSet ids = new BitSet();
        BitSet metas = new BitSet();
        // Make sure block IDs and names are unique
        for (WesterosBlockDef def : defs) {
            if (def == null) continue;
            if (def.blockName == null) {
                WesterosBlocks.log.severe("Block definition is missing blockName");
                return false;
            }
            if (names.add(def.blockName) == false) {    // If alreay defined
                WesterosBlocks.log.severe(String.format("Block '%s' - blockName duplicated", def.blockName));
                return false;
            }
            if (def.blockIDs != null) {
                for (int i = 0; i < def.blockIDs.length; i++) {
                    if ((def.blockIDs[i] < 0) || (def.blockIDs[i] > 4095)) {
                        WesterosBlocks.log.severe(String.format("Block '%s' - blockIDs[%d] invalid", def.blockName, i));
                        return false;
                    }
                    else if (ids.get(def.blockIDs[i])) {    // If already defined
                        WesterosBlocks.log.severe(String.format("Block '%s' - blockIDs[%d] duplicated", def.blockName, i));
                        return false;
                    }
                    ids.set(def.blockIDs[i]);
                }
                def.blockID = def.blockIDs[0];
            }
            else {
                if ((def.blockID < 0) || (def.blockID > 4095)) {
                    WesterosBlocks.log.severe(String.format("Block '%s' - blockID invalid", def.blockName));
                    return false;
                }
                else if (ids.get(def.blockID)) {    // If already defined
                    WesterosBlocks.log.severe(String.format("Block '%s' - blockID duplicated", def.blockName));
                    return false;
                }
                ids.set(def.blockID);
                def.blockIDs = new int[] { def.blockID };
            }
            // Check for duplicate meta
            metas.clear();
            if (def.subBlocks != null) {
                for (Subblock sb : def.subBlocks) {
                    if (sb == null) continue;
                    if (metas.get(sb.meta)) {
                        WesterosBlocks.log.severe(String.format("Block '%s' - duplicate meta value %d", def.blockName, sb.meta));
                        return false;
                    }
                    metas.set(sb.meta);
                }
            }
        }
        WesterosBlocks.log.info("WesterosBlocks.json passed sanity check");
        return true;
    }
    
    public Icon getItemIcon(int meta) {
        meta &= metaMask;
        if (itemicons_by_meta != null) {
            if (meta >= itemicons_by_meta.length) {
                meta = 0;
            }
            if (itemicons_by_meta[meta] != null) {
                return itemicons_by_meta[meta];
            }
        }
        Subblock sb = getByMeta(meta);
        int idx = 0;
        if (sb != null) {
            idx = sb.itemTextureIndex;
        }
        return doStandardIconGet(idx, meta);
    }
    
    public static void initialize() {
        materialTable.put("air",  Material.air);
        materialTable.put("grass",  Material.grass);
        materialTable.put("ground",  Material.ground);
        materialTable.put("wood",  Material.wood);
        materialTable.put("rock",  Material.rock);
        materialTable.put("iron", Material.iron);
        materialTable.put("anvil", Material.anvil);
        materialTable.put("water", Material.water);
        materialTable.put("lava", Material.lava);
        materialTable.put("leaves", Material.leaves);
        materialTable.put("plants", Material.plants);
        materialTable.put("vine", Material.vine);
        materialTable.put("sponge", Material.sponge);
        materialTable.put("cloth", Material.cloth);
        materialTable.put("fire", Material.fire);
        materialTable.put("sand", Material.sand);
        materialTable.put("circuits", Material.circuits);
        materialTable.put("glass", Material.glass);
        materialTable.put("redstoneLight", Material.redstoneLight);
        materialTable.put("tnt", Material.tnt);
        materialTable.put("coral", Material.coral);
        materialTable.put("ice", Material.ice);
        materialTable.put("snow", Material.snow);
        materialTable.put("craftedSnow", Material.craftedSnow);
        materialTable.put("cactus", Material.cactus);
        materialTable.put("clay", Material.clay);
        materialTable.put("pumpkin", Material.pumpkin);
        materialTable.put("dragonEgg", Material.dragonEgg);
        materialTable.put("portal", Material.portal);
        materialTable.put("cake", Material.cake);
        materialTable.put("web", Material.web);
        materialTable.put("piston", Material.piston);

        stepSoundTable.put("powder", Block.soundPowderFootstep);
        stepSoundTable.put("wood", Block.soundWoodFootstep);
        stepSoundTable.put("gravel", Block.soundGravelFootstep);
        stepSoundTable.put("grass", Block.soundGrassFootstep);
        stepSoundTable.put("stone", Block.soundStoneFootstep);
        stepSoundTable.put("metal", Block.soundMetalFootstep);
        stepSoundTable.put("glass", Block.soundGlassFootstep);
        stepSoundTable.put("cloth", Block.soundClothFootstep);
        stepSoundTable.put("sand", Block.soundSandFootstep);
        stepSoundTable.put("snow", Block.soundSnowFootstep);
        stepSoundTable.put("ladder", Block.soundLadderFootstep);
        stepSoundTable.put("anvil", Block.soundAnvilFootstep);
        // Tab table
        tabTable.put("buildingBlocks", CreativeTabs.tabBlock);
        tabTable.put("decorations", CreativeTabs.tabDecorations);
        tabTable.put("redstone", CreativeTabs.tabRedstone);
        tabTable.put("transportation", CreativeTabs.tabTransport);
        tabTable.put("misc", CreativeTabs.tabMisc);
        tabTable.put("food", CreativeTabs.tabFood);
        tabTable.put("tools", CreativeTabs.tabTools);
        tabTable.put("combat", CreativeTabs.tabCombat);
        tabTable.put("brewing", CreativeTabs.tabBrewing);
        tabTable.put("materials", CreativeTabs.tabMaterials);

        // Standard block types
        typeTable.put("solid", new WCSolidBlock.Factory());
        typeTable.put("stair", new WCStairBlock.Factory());
        typeTable.put("log", new WCLogBlock.Factory());
        typeTable.put("plant", new WCPlantBlock.Factory());
        typeTable.put("crop", new WCCropBlock.Factory());
        typeTable.put("slab", new WCSlabBlock.Factory());
        typeTable.put("fence", new WCFenceBlock.Factory());
        typeTable.put("wall", new WCWallBlock.Factory());
        typeTable.put("pane", new WCPaneBlock.Factory());
        typeTable.put("sand", new WCSandBlock.Factory());
        typeTable.put("cuboid", new WCCuboidBlock.Factory());
        typeTable.put("cuboid-nsew", new WCCuboidNSEWBlock.Factory());
        typeTable.put("cuboid-ne", new WCCuboidNEBlock.Factory());
        typeTable.put("cuboid-nsewud", new WCCuboidNSEWUDBlock.Factory());
        typeTable.put("torch", new WCTorchBlock.Factory());
        typeTable.put("leaves", new WCLeavesBlock.Factory());
        typeTable.put("door", new WCDoorBlock.Factory());
        typeTable.put("layer", new WCLayerBlock.Factory());
        typeTable.put("web", new WCWebBlock.Factory());
        typeTable.put("ladder", new WCLadderBlock.Factory());
        typeTable.put("halfdoor", new WCHalfDoorBlock.Factory());
        typeTable.put("soulsand", new WCSoulSandBlock.Factory());
        typeTable.put("sound", new WCSoundBlock.Factory());
        typeTable.put("rail", new WCRailBlock.Factory());
        typeTable.put("bed", new WCBedBlock.Factory());

        // Standard color multipliers
        colorMultTable.put("#FFFFFF", new ColorMultHandler());
        colorMultTable.put("water", new WaterColorMultHandler());
        colorMultTable.put("foliage", new FoliageColorMultHandler());
        colorMultTable.put("grass", new GrassColorMultHandler());
        colorMultTable.put("pine", new PineColorMultHandler());
        colorMultTable.put("birch", new BirchColorMultHandler());
        colorMultTable.put("basic", new BasicColorMultHandler());
        colorMultTable.put("lily", new ColorMultHandler(2129968));
     }
    // Get color muliplier
    public ColorMultHandler getColorHandler(String hnd) {
        ColorMultHandler cmh = colorMultTable.get(hnd);
        if (cmh == null) {
            hnd = hnd.toUpperCase();
            cmh = colorMultTable.get(hnd);
        }
        if (cmh == null) { 
            // See if color code
            if ((hnd.length() == 7) && (hnd.charAt(0) == '#')) {
                try {
                    cmh = new ColorMultHandler(Integer.parseInt(hnd.substring(1), 16));
                    colorMultTable.put(hnd, cmh);
                } catch (NumberFormatException nfx) {
                }
            }
            // See if resource
            else {
                int idx = hnd.indexOf(':');
                if (idx < 0) {
                    hnd = "westeroscraft:" + hnd;
                    cmh = colorMultTable.get(hnd);
                }
                if (cmh == null) {
                    cmh = new CustomColorMultHandler(hnd, blockName);
                    colorMultTable.put(hnd, cmh);
                }
            }
        }
        return cmh;
    }
    // Register custom step sound
    public static void registerStepSound(WesterosBlockStepSound ss) {
        WCStepSound stepsound = new WCStepSound(ss);
        stepSoundTable.put(ss.name, stepsound);
    }
}
