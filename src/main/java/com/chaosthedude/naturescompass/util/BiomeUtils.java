package com.chaosthedude.naturescompass.util;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraftforge.registries.ForgeRegistries;

public class BiomeUtils {
	
	public static int getIDForBiome(Biome biome) {
		return IRegistry.field_212624_m.getId(biome);
	}

	public static List<Biome> getAllowedBiomes() {
		final List<Biome> biomes = new ArrayList<Biome>();
		for (Biome biome : Biome.BIOMES) {
			if (biome != null && !biomeIsBlacklisted(biome)) {
				biomes.add(biome);
			}
		}

		return biomes;
	}

	public static SearchResult searchForBiome(World world, ItemStack stack, Biome biome, BlockPos startPos) {
		if (stack.isEmpty() || stack.getItem() != NaturesCompass.naturesCompass) {
			return null;
		}

		final int sampleSpace = ConfigHandler.GENERAL.sampleSpaceModifier.get() * getBiomeSize(world);
		final int maxDistance = ConfigHandler.GENERAL.distanceModifier.get() * getBiomeSize(world);
		if (maxDistance <= 0 || sampleSpace <= 0) {
			return new SearchResult(0, 0, maxDistance, 0, false);
		}

		int direction = -1;
		int samples = 0;
		int nextLength = sampleSpace;
		int x = startPos.getX();
		int z = startPos.getZ();
		while (nextLength / 2 <= maxDistance && samples <= ConfigHandler.GENERAL.maxSamples.get()) {
			final int fixedDirection = direction == -1 ? -1 : direction % 4;
			for (int i = 0; i < nextLength && samples <= ConfigHandler.GENERAL.maxSamples.get(); i += sampleSpace) {
				if (fixedDirection == 0) {
					x += sampleSpace;
				} else if (fixedDirection == 1) {
					z -= sampleSpace;
				} else if (fixedDirection == 2) {
					x -= sampleSpace;
				} else if (fixedDirection == 3) {
					z += sampleSpace;
				}

				final BlockPos pos = new BlockPos(x, world.getHeight(Type.WORLD_SURFACE, x, z), z);
				final Biome biomeAtPos = world.getChunk(pos).getBiome(pos);
				if (biomeAtPos == biome) {
					return new SearchResult(x, z, nextLength / 2, samples, true);
				}

				samples++;
			}

			if (direction >= 0) {
				nextLength += sampleSpace;
			}
			direction++;
		}

		return new SearchResult(0, 0, nextLength / 2, samples, false);
	}

	public static int getBiomeSize(World world) {
		// TODO
		//final String settings = world.getWorldInfo().getGeneratorOptions();
		//return ChunkGeneratorSettings.Factory.jsonToFactory(settings).build().biomeSize;
		return 4;
	}

	public static int getDistanceToBiome(EntityPlayer player, int x, int z) {
		return (int) player.getDistance(x, player.posY, z);
	}

	public static String getBiomeNameForDisplay(Biome biome) {
		if (biome != null) {
			if (ConfigHandler.CLIENT.fixBiomeNames.get()) {
				final String original = I18n.format(biome.getTranslationKey());
				String fixed = "";
				char pre = ' ';
				for (int i = 0; i < original.length(); i++) {
					final char c = original.charAt(i);
					if (Character.isUpperCase(c) && Character.isLowerCase(pre) && Character.isAlphabetic(pre)) {
						fixed = fixed + " ";
					}
					fixed = fixed + String.valueOf(c);
					pre = c;
				}

				return fixed;
			}

			return I18n.format(biome.getTranslationKey());
		}

		return "";
	}
	
	public static String getBiomeName(Biome biome) {
		return I18n.format(biome.getTranslationKey());
	}

	public static String getBiomeName(int biomeID) {
		return getBiomeName(Biome.getBiome(biomeID, null));
	}

	public static boolean biomeIsBlacklisted(Biome biome) {
		final List<String> biomeBlacklist = ConfigHandler.GENERAL.biomeBlacklist.get();
		final ResourceLocation biomeResourceLocation = ForgeRegistries.BIOMES.getKey(biome);
		return biomeBlacklist.contains(String.valueOf(BiomeUtils.getIDForBiome(biome)))
				|| biomeBlacklist.contains(getBiomeName(biome))
				|| biomeBlacklist.contains(I18n.format(biome.getTranslationKey()))
				|| (biomeResourceLocation != null && biomeBlacklist.contains(biomeResourceLocation.toString()));
	}

}
