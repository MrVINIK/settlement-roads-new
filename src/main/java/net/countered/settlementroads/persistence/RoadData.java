package net.countered.settlementroads.persistence;

import net.countered.settlementroads.SettlementRoads;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import com.mojang.serialization.Codec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RoadData extends PersistentState {

    public static final Logger LOGGER = LoggerFactory.getLogger(SettlementRoads.MOD_ID);

    private final static String STRUCTURE_LOCATION_KEY = "villages";
    private final static String CONNECTION_LOCATION_KEY = "connections";
    private final static String START_END_POS_KEY = "start_end_pos";

    private final List<BlockPos> structureLocations = new ArrayList<BlockPos>();
    private final List<BlockPos> structureConnections = new ArrayList<BlockPos>();
    private final List<BlockPos> startEndPlacedPos = new ArrayList<BlockPos>();

    private final ServerWorld world;

    public static RoadData getOrCreateRoadData(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                new PersistentStateType<RoadData>(
                        "road_data",
                        () -> new RoadData(world),
                        Codec.unit(() -> new RoadData(world)),
                        DataFixTypes.CHUNK
                )
        );
    }

    public RoadData(ServerWorld world) {
        this.world = world;
        this.markDirty();
    }

    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList nbtList = new NbtList();
        for (BlockPos pos : structureLocations) {
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putInt("X", pos.getX());
            nbtCompound.putInt("Y", pos.getY());
            nbtCompound.putInt("Z", pos.getZ());
            nbtList.add(nbtCompound);
        }
        nbt.put(STRUCTURE_LOCATION_KEY, nbtList);

        LOGGER.info("Wrote structure locations");
        return nbt;
    }

    public static RoadData fromNbt(ServerWorld world, NbtCompound nbt) {
        RoadData roadData = new RoadData(world);
        NbtList nbtList = nbt.getList(STRUCTURE_LOCATION_KEY).orElse(new NbtList());

        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound villageData = nbtList.getCompound(i).orElse(new NbtCompound());
            int x = villageData.getInt("X").orElse(0);
            int y = villageData.getInt("Y").orElse(0);
            int z = villageData.getInt("Z").orElse(0);
            roadData.structureLocations.add(new BlockPos(x, y, z));
        }
        return roadData;
    }

    public List<BlockPos> getStructureLocations() {
        return structureLocations;
    }
}
