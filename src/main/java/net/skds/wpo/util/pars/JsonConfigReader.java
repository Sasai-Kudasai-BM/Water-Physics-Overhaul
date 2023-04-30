package net.skds.wpo.util.pars;

import static net.skds.wpo.WPO.LOGGER;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.skds.wpo.WPO;
import net.skds.wpo.util.pars.ParsApplier.ParsGroup;

public class JsonConfigReader {

	File fileF;

	File dir = Paths.get(System.getProperty("user.dir"), "config", WPO.MOD_ID).toFile();
	Set<Map.Entry<String, JsonElement>> blockListSet = new HashSet<>();
	Set<Map.Entry<String, JsonElement>> propertyListSet = new HashSet<>();
	Map<String, JsonElement> propertyListMap = new HashMap<>();

	public Map<String, ParsGroup<FluidPars>> FP = new HashMap<>();

	Gson GSON = new Gson();
	boolean created = false;

	public void run() {

		dir.mkdir();

		try {
			fileF = new File(dir, "fluid-config.json");
			boolean exsists = fileF.exists();

			if (!exsists) {
				create(false);
			}
			if (!readFluid(fileF) && !created) {
				create(true);
				readFluid(fileF);
			}
		} catch (IOException e) {
			LOGGER.error("Error while reading config: ", e);
		}

	}

	private void create(boolean existError) throws IOException {
		created = true;
		boolean copydeleted = true;
		File copyFile = new File(dir, "fluid-config-backup.json");
		if (existError && fileF.exists()) {
			LOGGER.warn("Fluid config resers to default");
			if (copyFile.exists()) {
				copydeleted = copyFile.delete();
			}
			if (copydeleted) {
				Files.copy(fileF.toPath(), copyFile.toPath());
				LOGGER.warn("Fluid config backup created");
			}
		}
		BufferedInputStream is = new BufferedInputStream(
				WPO.class.getClassLoader().getResourceAsStream(Paths.get(WPO.MOD_ID, "special", "fluids.json").toString()));
		boolean ex = fileF.exists();
		if (ex) {
			fileF.delete();
			// LOGGER.info(fileF.delete());
		}
		Files.copy(is, fileF.toPath());
		is.close();
	}

	private boolean readFluid(File fileF) throws IOException {
		JsonObject jsonobject = new JsonObject();

		InputStream inputStream = new FileInputStream(fileF);
		Reader r = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		JsonReader jsonReader = new JsonReader(r);
		try {
			jsonobject = GSON.getAdapter(JsonObject.class).read(jsonReader);
		} catch (IOException e) {
			LOGGER.error("Empty or invalid fluid config file!");

			inputStream.close();
			create(true);
			inputStream = new FileInputStream(fileF);
			r = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			jsonReader = new JsonReader(r);

			jsonobject = GSON.getAdapter(JsonObject.class).read(jsonReader);
		}
		r.close();
		jsonReader.close();

		JsonElement bls = jsonobject.get("BlockLists");
		JsonElement pls = jsonobject.get("PropertyLists");
		if (bls == null || pls == null) {
			LOGGER.error("Invalid fluid config file!");
			return false;
		}
		blockListSet = bls.getAsJsonObject().entrySet();
		propertyListSet = pls.getAsJsonObject().entrySet();

		if (blockListSet.size() == 0) {
			LOGGER.error("Empty block list file!");
			return false;
		}

		propertyListSet.forEach(entry -> {
			propertyListMap.put(entry.getKey(), entry.getValue());
		});

		for (Map.Entry<String, JsonElement> entry : blockListSet) {
			ArrayList<String> blockIDs = new ArrayList<>();
			String key = entry.getKey();
			JsonElement listElement = entry.getValue();
			if (!listElement.isJsonArray()) {
				LOGGER.error("Block list \"" + key + "\" is not a list!");
				return false;
			}
			JsonArray blocklist = listElement.getAsJsonArray();
			if (blocklist.size() == 0) {
				LOGGER.warn("Block list \"" + key + "\" is empty!");
			}
			JsonElement properties = propertyListMap.get(key);
			if (properties == null) {
				LOGGER.error("Block list \"" + key + "\" have no properties!");
				return false;
			}
			blocklist.forEach(element -> {
				blockIDs.add(element.getAsString());
			});

			// LOGGER.info(key + blocklist.toString() + properties.toString());

			addFluidParsGroup(key, blocklist, FluidPars.readFromJson(properties, key));

		}
		return true;
	}

	private void addFluidParsGroup(String key, JsonArray blockNames, FluidPars pars) {
		if (pars == null) {
			return;
		}
		Set<Block> blocks = getBlocksFromJA(blockNames);

		ParsGroup<FluidPars> group = new ParsGroup<FluidPars>(pars, blocks);
		FP.put(key, group);
	}

	public static Set<Block> getBlocksFromString(Set<String> list) {
		Set<Block> blocks = new HashSet<>();
		for (String id : list) {
			if (id.charAt(0) == '#') {
				id = id.substring(1);
				Tag<Block> tag = BlockTags.getAllTags().getTag(new ResourceLocation(id));
				if (tag == null) {
					LOGGER.error("Block tag \"" + id + "\" does not exist!");
					continue;
				}
				blocks.addAll(tag.getValues());
				continue;
			}
			Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
			if (block != null && block != Blocks.AIR) {
				blocks.add(block);
			} else {
				LOGGER.error("Block \"" + id + "\" does not exist!");
			}
		}
		return blocks;
	}

	public static Set<Block> getBlocksFromJA(JsonArray arr) {
		Set<Block> blocks = new HashSet<>();
		for (JsonElement je : arr) {
			String id = je.getAsString();
			if (id.charAt(0) == '#') {
				id = id.substring(1);
				Tag<Block> tag = BlockTags.getAllTags().getTag(new ResourceLocation(id));
				if (tag == null) {
					LOGGER.error("Block tag \"" + id + "\" does not exist!");
					continue;
				}
				blocks.addAll(tag.getValues());
				continue;
			}
			Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
			if (block != null && block != Blocks.AIR) {
				blocks.add(block);
			} else {
				LOGGER.error("Block \"" + id + "\" does not exist!");
			}
		}
		return blocks;
	}
}
