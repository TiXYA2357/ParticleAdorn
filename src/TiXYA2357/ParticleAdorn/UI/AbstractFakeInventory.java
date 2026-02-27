package TiXYA2357.ParticleAdorn.UI;

import cn.nukkit.Player;
import cn.nukkit.event.inventory.InventoryTransactionEvent;
import cn.nukkit.inventory.ContainerInventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.ContainerOpenPacket;
import cn.nukkit.network.protocol.UpdateBlockPacket;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static TiXYA2357.ParticleAdorn.Main.nks;
import static TiXYA2357.ParticleAdorn.Utils.Async;

public abstract class AbstractFakeInventory extends ContainerInventory {
    public final static HashMap<String, Runnable> BagOpened = new HashMap<>();
    private static final BlockVector3 ZERO = new BlockVector3(0, 0, 0);
    private static final Map<String, AbstractFakeInventory> OPEN = new ConcurrentHashMap<>();
    public static boolean IS_PM1E = false;
    public static final Map<String, List<BlockVector3>> blockPositions = new HashMap<>();
    private final String title;

    AbstractFakeInventory(InventoryType type, InventoryHolder holder, String title) {
        super(holder, type);
        this.title = title == null ? type.getDefaultTitle() : title;
    }

    @Getter
    @Setter
    private HashMap<Integer, Runnable> rs = new HashMap<>();
    @Getter
    @Setter
    private List<Integer> slots = new ArrayList<>();
    @Getter
    @Setter
    private InventoryTransactionEvent event;
    @Getter
    @Setter
    private boolean autoClose = false;
    @Getter
    @Setter
    private Player player;
    @Getter
    @Setter
    private boolean async = false;
    @Getter
    @Setter
    private boolean isBag = false;


    @Override
    public void onOpen(Player who) {
//        checkForClosed();
        this.viewers.add(who);
        if (OPEN.putIfAbsent(who.getName(), this) != null) {
            throw new IllegalStateException("箱子已被打开");
        }
        var blocks = onOpenBlock(who);
        blockPositions.put(who.getName(), blocks);
        onFakeOpen(who, blocks);
    }

    void onFakeOpen(Player who, List<BlockVector3> blocks) {
        var blockPosition = blocks.isEmpty() ? ZERO : blocks.get(0);

        ContainerOpenPacket containerOpen = new ContainerOpenPacket();
        containerOpen.windowId = who.getWindowId(this);
        containerOpen.type = this.getType().getNetworkType();
        containerOpen.x = blockPosition.x;
        containerOpen.y = blockPosition.y;
        containerOpen.z = blockPosition.z;

        who.dataPacket(containerOpen);
        this.sendContents(who);
    }

    /**
     * 玩家开启
     * @param who 玩家
     * @return 方块坐标*/
    protected abstract List<BlockVector3> onOpenBlock(Player who);

    @Override
    public void onClose(Player who) {
        super.onClose(who);
        OPEN.remove(who.getName(), this);
        var blocks = blockPositions.get(who.getName());
        if (blocks == null) return;
        for (int i = 0, size = blocks.size(); i < size; i++) {
            final var index = i;
            Async(() -> {
                var blockPosition = blocks.get(index).asVector3();
                var updateBlock = new UpdateBlockPacket();
                if (nks.netEaseMode) {
                    if (IS_PM1E) updateBlock.blockRuntimeId=GlobalBlockPalette.
                            getOrCreateRuntimeId(who.getGameVersion(), who.getLevel().getBlock(blockPosition).getFullId());
                    else updateBlock.blockRuntimeId=GlobalBlockPalette.
                            getOrCreateRuntimeId(who.getLevel().getBlock(blockPosition).getFullId());
                } else {
                    if (IS_PM1E) updateBlock.blockRuntimeId=GlobalBlockPalette.
                            getOrCreateRuntimeId(who.protocol, who.getLevel().getBlock(blockPosition).getFullId());
                    else updateBlock.blockRuntimeId=GlobalBlockPalette.
                            getOrCreateRuntimeId(who.getLevel().getBlock(blockPosition).getFullId());
                }

                updateBlock.flags = UpdateBlockPacket.FLAG_ALL_PRIORITY;
                updateBlock.x = blockPosition.getFloorX();
                updateBlock.y = blockPosition.getFloorY();
                updateBlock.z = blockPosition.getFloorZ();
                who.dataPacket(updateBlock);});}
    }

    @Override
    public String getTitle() {return title;}

}
