package TiXYA2357.ParticleAdorn.UI;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.ContainerOpenPacket;
import cn.nukkit.network.protocol.RemoveEntityPacket;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class MenuChest extends FakeInventoryChest {
    private final List<Button> buttons = new ArrayList<>();
    long id;
    @Getter
    @Setter
    private Runnable close = () -> {};

    public MenuChest(String title) {
        super(InventoryType.CHEST, null, title);
        this.setName(title);
    }

    public MenuChest(String title, boolean autoClose) {
        super(InventoryType.CHEST, null, title);
        this.setName(title);
        super.setAutoClose(autoClose);
    }

    public MenuChest(String title, boolean autoClose, boolean async) {
        super(InventoryType.CHEST, null, title);
        this.setName(title);
        super.setAutoClose(autoClose);
        super.setAsync(async);
    }

    public MenuChest add(boolean bool, int slot, Item item, Runnable runnable) {
        if (!bool) return this;
        return this.add(slot, item, runnable);
    }

    public MenuChest add(boolean bool, int slot, Item item, Runnable runnable,
                         Item subItem, Runnable subRunnable) {
        if (!bool) return this.add(slot, subItem, subRunnable);
        return this.add(slot, item, runnable);
    }

    public MenuChest add(int slot, Item item, Runnable runnable) {
        slot = min(max(1, slot), this.getSize() + 1);
        this.buttons.add(new Button(slot - 1, item, runnable));
        return this;
    }

    public void show(Player player) {
        this.id = Entity.entityCount++;
        var itemMap = new LinkedHashMap<Integer, Item>();
        var rs = new HashMap<Integer, Runnable>();
        var slots = new ArrayList<Integer>();
        for (var button : buttons) {
            itemMap.put(button.slot, button.item);
            rs.put(button.slot, button.callback);
            slots.add(button.slot);
        }
        this.setContents(itemMap);
        this.setRs(rs);
        this.setSlots(slots);
        this.setPlayer(player);
        player.addWindow(this);
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public void onSlotChange(int index, Item before, boolean send) {
        super.onSlotChange(index, before, send);
    }

    @Override
    public void onOpen(Player who) {
        super.onOpen(who);
        var pk = new ContainerOpenPacket();
        pk.windowId = who.getWindowId(this);
        pk.entityId = id;
        pk.type = InventoryType.CHEST.getNetworkType();
        who.dataPacket(pk);
    }

    @Override
    public void onClose(Player who) {
        var pk = new RemoveEntityPacket();
        pk.eid = id;
        who.dataPacket(pk);
        super.onClose(who);
        if (close != null) close.run();
    }

    static class Button {
        public final int slot;
        public final Item item;
        public final Runnable callback;

        public Button(int slot, Item item, Runnable runnable) {
            this.item = item;
            this.callback = runnable;
            this.slot = slot;
        }
    }
}
