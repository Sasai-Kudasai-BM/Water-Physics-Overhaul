package net.skds.wpo.util.pars;

import static net.skds.wpo.WPO.LOGGER;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidPars {
	public final int isPassable;

	private Set<Fluid> destroyableBy = new HashSet<>();

	public FluidPars(int passable, Set<Fluid> destroyableBy) {
		this.isPassable = passable;
		if (destroyableBy != null)
			this.destroyableBy = destroyableBy;
	}

	public boolean isDestroyableBy(Fluid fluid) {
		return destroyableBy.contains(fluid);
	}

	public static FluidPars readFromJson(JsonElement json, String name) {
		if (json == null) {
			LOGGER.error("Invalid properties: \"" + name + "\"");
			return null;
		}
		JsonObject jsonObject = json.getAsJsonObject();

		JsonElement passableElement = jsonObject.get("isPassable");
		JsonElement dbfElement = jsonObject.get("destroyableByFluids");
		int passable;
		if (passableElement != null) {
			passable = passableElement.getAsInt();
		} else {
			passable = 0;
		}
		Set<Fluid> dby = new HashSet<>();
		if (dbfElement != null) {
			if (!dbfElement.isJsonArray()) {
				LOGGER.error("Property \"destroyableByFluids\" in \"" + name + "\" is invalid");
				return null;
			}
			JsonArray dbfJ = dbfElement.getAsJsonArray();
			if (dbfJ.size() > 0) {
				dbfJ.forEach(jse -> {
					String id = jse.getAsString();
					Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(id));
					if (fluid != null && fluid != Fluids.EMPTY) {
						dby.add(fluid);
					} else {
						LOGGER.error("Fluid \"" + id + "\" does not exist!");
					}
				});
			}
		}

		return new FluidPars(passable, dby);
	}
}