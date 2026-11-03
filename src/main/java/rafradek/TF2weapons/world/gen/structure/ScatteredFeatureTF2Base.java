package rafradek.TF2weapons.world.gen.structure;

import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import java.util.Map.Entry;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class ScatteredFeatureTF2Base extends StructureComponentTemplate {

	public static String[] templateNames = { "red", "blu" };
	private String templateName;
	private Rotation rotation;
	private Mirror mirror;

	private int groundLevel;

	public ScatteredFeatureTF2Base() {}

	public ScatteredFeatureTF2Base(StructureTemplateManager manager, String name, BlockPos pos, Rotation rot, Level world) {
		this(manager, name, pos, rot, Mirror.NONE, world);
	}

	public ScatteredFeatureTF2Base(StructureTemplateManager p_i47356_1_, String p_i47356_2_, BlockPos p_i47356_3_,
			Rotation p_i47356_4_, Mirror p_i47356_5_, Level world) {
		super(0);
		this.templateName = p_i47356_2_;
		this.templatePosition = p_i47356_3_;
		this.rotation = p_i47356_4_;
		this.mirror = p_i47356_5_;
		this.loadTemplate(p_i47356_1_);
		// this.offsetToAverageGroundLevel(world);
	}

	private void loadTemplate(StructureTemplateManager p_191081_1_) {
		StructureTemplate template = p_191081_1_.getTemplate((MinecraftServer) null,
				new ResourceLocation(TF2weapons.MOD_ID, "tf2base/" + this.templateName));
		StructurePlaceSettings placementsettings = (new StructurePlaceSettings()).setIgnoreEntities(false)
				.setRotation(this.rotation).setMirror(this.mirror).setBoundingBox(boundingBox);
		this.setup(template, this.templatePosition, placementsettings);
		for (Entry<BlockPos, String> entr : template.getDataBlocks(this.templatePosition, placementsettings)
				.entrySet()) {
			if (entr.getValue().equals("GLevel"))
				this.groundLevel = entr.getKey().getY() - this.boundingBox.minY;
		}

	}

	/**
	 * (abstract) Helper method to write subclass data to NBT
	 */
	@Override
	protected void writeStructureToNBT(CompoundTag tagCompound) {
		super.writeStructureToNBT(tagCompound);
		tagCompound.putString("Template", this.templateName);
		tagCompound.putString("Rot", this.placeSettings.getRotation().name());
		tagCompound.putString("Mi", this.placeSettings.getMirror().name());
	}

	/**
	 * (abstract) Helper method to read subclass data from NBT
	 */
	@Override
	protected void readStructureFromNBT(CompoundTag tagCompound, StructureTemplateManager p_143011_2_) {
		super.readStructureFromNBT(tagCompound, p_143011_2_);
		this.templateName = tagCompound.getString("Template");
		this.rotation = Rotation.valueOf(tagCompound.getString("Rot"));
		this.mirror = Mirror.valueOf(tagCompound.getString("Mi"));
		this.loadTemplate(p_143011_2_);
	}

	@Override
	public boolean addComponentParts(Level world, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
		if (this.templatePosition.getY() == 64) {
			int averageGroundLvl = this.getAverageGroundLevel(world, structureBoundingBoxIn);

			if (averageGroundLvl < 0)
				return true;

			this.move(0, averageGroundLvl - this.boundingBox.minY - this.groundLevel, 0);
		}

		boolean done = super.addComponentParts(world, randomIn, structureBoundingBoxIn);
		if (done) {
			for (int i = 0; i < this.boundingBox.getXSize(); i++)
				for (int j = 0; j < this.boundingBox.getZSize(); j++)
					this.replaceAirAndLiquidDownwards(world, Blocks.DIRT.defaultBlockState(), i, -1 + groundLevel, j,
							structureBoundingBoxIn);
		}
		return done;
	}

	protected int getAverageGroundLevel(Level world, StructureBoundingBox structurebb) {
		int i = 0;
		int j = 0;
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

		for (int k = this.boundingBox.minZ; k <= this.boundingBox.maxZ; ++k) {
			for (int l = this.boundingBox.minX; l <= this.boundingBox.maxX; ++l) {
				blockpos$mutableblockpos.setPos(l, 64, k);

				if (structurebb.isVecInside(blockpos$mutableblockpos)) {
					i += Math.max(world.getTopSolidOrLiquidBlock(blockpos$mutableblockpos).getY(),
							world.provider.getAverageGroundLevel() - 1);
					++j;
				}
			}
		}

		if (j == 0) {
			return -1;
		} else {
			return i / j;
		}
	}

	@Override
	protected void handleDataMarker(String function, BlockPos pos, Level world, Random rand,
			StructureBoundingBox sbb) {
		if (function.startsWith("ChestB")) {
			BlockPos off = pos.down();
			BlockEntity ent = world.getBlockEntity(off);
			((ChestBlockEntity) ent).setLootTable(TF2weapons.lootTF2Base, rand.nextLong());
		}
	}

	public static class Start extends StructureStart {
		public Start() {}

		public Start(Level world, Random random, IChunkGenerator provider, int chunkX, int chunkZ) {
			this(world, random, provider, chunkX, chunkZ,
					world.getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8)));
		}

		public Start(Level world, Random random, IChunkGenerator provider, int chunkX, int chunkZ, Biome biomeIn) {
			super(chunkX, chunkZ);
			Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
			BlockPos pos = new BlockPos(chunkX * 16 + 8, 64, chunkZ * 16 + 8);
			this.components.add(new ScatteredFeatureTF2Base(world.getSaveHandler().getStructureTemplateManager(),
					templateNames[random.nextInt(templateNames.length)], pos, rotation, world));

			this.updateBoundingBox();
		}
	}

	public static class MapGen extends MapGenStructure {
		private int distance;

		private IChunkGenerator provider;

		public MapGen(IChunkGenerator provider) {
			this.provider = provider;
			this.distance = TF2ConfigVars.baseChance;
		}

		@Override
		public String getStructureName() {
			return "TF2BaseScatt";
		}

		@Override
		protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
			int i = chunkX;
			int j = chunkZ;

			if (chunkX < 0) {
				chunkX -= this.distance - 1;
			}

			if (chunkZ < 0) {
				chunkZ -= this.distance - 1;
			}

			int k = chunkX / this.distance;
			int l = chunkZ / this.distance;
			Random random = this.level.setRandomSeed(k, l, 1456463);
			k = k * this.distance;
			l = l * this.distance;
			k = k + random.nextInt(Math.max(1, this.distance - 8));
			l = l + random.nextInt(Math.max(1, this.distance - 8));

			if (i == k && j == l) {
				boolean flag = this.level.getBiomeProvider().areBiomesViable(i * 16 + 8, j * 16 + 8, 0,
						MapGenVillage.VILLAGE_SPAWN_BIOMES);

				if (flag) {
					return true;
				}
			}

			return false;
		}

		@Override
		public BlockPos getNearestStructurePos(Level world, BlockPos pos, boolean findUnexplored) {
			this.level = world;
			return findNearestStructurePosBySpacing(world, this, pos, this.distance, 8, 10387312, false, 100,
					findUnexplored);
		}

		@Override
		protected StructureStart getStructureStart(int chunkX, int chunkZ) {
			return new Start(this.level, this.rand, this.provider, chunkX, chunkZ);
		}
	}
}
