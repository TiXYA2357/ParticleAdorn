package TiXYA2357.ParticleAdorn;

import TiXYA2357.ParticleAdorn.UI.*;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementSlider;
import cn.nukkit.form.element.ElementStepSlider;

import java.util.HashMap;
import java.util.List;

import static TiXYA2357.ParticleAdorn.Configs.*;
import static TiXYA2357.ParticleAdorn.Main.*;
import static TiXYA2357.ParticleAdorn.PlayerI.getOrDefaultPlayerI;
import static TiXYA2357.ParticleAdorn.Utils.*;

public class MainCommand extends Command {
    public MainCommand(String name) {
        super(name,"§r§b" + title + "系统");
    }
    @Override
    public boolean execute(cn.nukkit.command.CommandSender sender, String commandLabel, String[] args) {
        if (args.length == 0) sender.sendMessage(helpTips);
        else {
            if (sender instanceof Player p) {
                Async(() -> {
                    switch (args[0]) {
                        case "my" -> chest_myHas(p, 0, 45);
                        case "shop" -> chest_shop(p, 0, 45);
                        case "sml" -> {
                            var form =  new Form_Custom("§b" + title + "系统性能优化");
                            var pic = getOrDefaultPlayerI(p.getUniqueId() + "");
                            form.add("x", new ElementSlider("§f粒子显示性能优化模式(0级为全部屏蔽)", 0, 5, 1,
                                    pic.performanceOptimizationLevel));
                            form.setSubmit(() -> {
                                pic.performanceOptimizationLevel = (int) (float)form.getRes("x");
                                pic.save();
                                sender.sendMessage(PT + "已设置§f" + title + "§r§7性能优化模式为§f " + pic.performanceOptimizationLevel);
                            });
                            form.asyncShow(p);
                        }
                        case "mgform" -> {
                            var form=new Form_Simple("§b" + title + "系统", "§r");
                            form.add("§b我的" + title, () -> asOPCmd(p, getCmd() + " my"));
                            form.add("§a" + title + "商店", () -> asOPCmd(p, getCmd() + " shop"));
                            if (isAdmin(p)) {
                                form.add("§d指令帮助", () -> asOPCmd(p, getCmd() + " help"));
                                form.add("§f创建" + title, () -> asOPCmd(p, getCmd() + " create"));
                                form.add("§f管理" + title, () -> {
                                    var f2=new Form_Custom("§b管理" + title);
                                    var tmp=new HashMap<String, String>();
                                    nks.getOnlinePlayers().values().forEach(ps -> tmp.put(getPlayerName(p), ps.getUniqueId() + ""));
                                    f2.add("b1", new ElementStepSlider("§f选择操作模式", List.of("创建", "添加", "设置", "删除")));
                                    f2.add("b2", new ElementDropdown("§f选择玩家", tmp.keySet().stream().toList()));
                                    f2.add("b3", new ElementDropdown("§f选择" + title, invts.keySet().stream().toList()));
                                    f2.add("b4", new ElementInput("§f创建输入" + title + "名称,否则输入时间"));
                                    f2.setSubmit(() -> {
                                        switch (f2.getRes("b1").toString()) {
                                            case "创建" -> {
                                                var input=f2.getRes("b4").toString();
                                                if (!input.isEmpty()) asOPCmd(p, getCmd() + " create " + input);
                                                else p.sendMessage(PT + "请输入" + title + "名称");
                                            }
                                            case "添加" -> {
                                                var pu=tmp.get(f2.getRes("b2").toString());
                                                var val=f2.getRes("b3").toString();
                                                var input=f2.getRes("b4").toString();
                                                long time;
                                                try {
                                                    time=!input.isEmpty() ? Integer.parseInt(input) :
                                                            (long) invts.getOrDefault(val, new HashMap<>()).getOrDefault("时效", 300);
                                                } catch (Exception e) {
                                                    p.sendMessage(PT + "请输入正确的数字");
                                                    return;
                                                }
                                                if (addPlayerParticle(pu, val, time))
                                                    sender.sendMessage(PT + "已为 " + getOrDefaultPlayerI(pu).getName() + " §r§7添加"
                                                            + title + " §f" + val + " §r§7期限至§f " + (time < 1 ? "§a永久" :
                                                            secondsToDateTime(secondsLeft(getPlayerParticles(pu)
                                                                    .getOrDefault(val, secondsToDateTime(0))) + time)));
                                                else sender.sendMessage(PT + "添加失败");
                                            }
                                            case "设置" -> {
                                                var pu=tmp.get(f2.getRes("b2").toString());
                                                var val=f2.getRes("b3").toString();
                                                var input=f2.getRes("b4").toString();
                                                long time;
                                                try {
                                                    time=!input.isEmpty() ? Integer.parseInt(input) :
                                                            (long) invts.getOrDefault(val, new HashMap<>()).getOrDefault("时效", 300);
                                                } catch (Exception e) {
                                                    p.sendMessage(PT + "请输入正确的数字");
                                                    return;
                                                }
                                                if (setPlayerParticle(pu, val, time)) sender.sendMessage(PT + "已为 "
                                                        + getOrDefaultPlayerI(pu).getName()
                                                        + " §r§7设置" + title + " §f" + val + " §r§7期限至§f "
                                                        + (time < 1 ? "永久" : secondsToDateTime(time)));
                                                else sender.sendMessage(PT + "设置失败");
                                            }
                                            case "删除" -> {
                                                var pu=tmp.get(f2.getRes("b2").toString());
                                                var val=f2.getRes("b3").toString();
                                                var input=f2.getRes("b4").toString();
                                                long time;
                                                try {
                                                    time=!input.isEmpty() ? Integer.parseInt(input) :
                                                            (long) invts.getOrDefault(val, new HashMap<>()).getOrDefault("时效", 300);
                                                } catch (Exception e) {
                                                    p.sendMessage(PT + "请输入正确的数字");
                                                    return;
                                                }
                                                if (delPlayerParticle(pu, val, time)) {
                                                    var ts=getPlayerParticles(pu);
                                                    var playerName= getOrDefaultPlayerI(pu).getName();
                                                    if (!ts.containsKey(val)) sender.sendMessage(PT
                                                            + "已删除 " + playerName + " §r§7的" + title + "§f " + val);
                                                    else sender.sendMessage(PT + "已减少 " + playerName
                                                            + " §r§7的" + title + "§f " + val + " §r§7期限至§f " + ts.getOrDefault(val, secondsToDateTime(0)));
                                                } else sender.sendMessage(PT + "删除失败");
                                            }
                                        }
                                    });
                                    f2.setClose(() -> Delayed(() -> asOPCmd(p, getCmd() + " mgform"), 3));
                                    f2.asyncShow(p);
                                });
                            }
                            form.asyncShow(p);
                        }
                    }
                });
                if (!isAdmin(p)) return false;
            }
            Async(() -> {
                switch (args[0]) {
                    case "help" -> sender.sendMessage(helpTips);
                    case "reload" -> {
                        if (initConfig()) sender.sendMessage(PT + "配置已重载");
                        else sender.sendMessage(PT + "配置重载失败");
                    }
                    case "create" -> {
                        if (args.length < 2) {
                            sender.sendMessage(PT + "请输入" + title + "名称");
                            return;
                        }
                        var val = args[1];
                        if (invts.containsKey(val)) {
                            sender.sendMessage(PT + "该" + title + "已存在");
                            return;
                        }
                        invts.put(val, def_invt);
                        setConfig("库存", invts);
                        sender.sendMessage(PT + "已创建" + title + ":§f " + val);
                    }
                    case "add" -> {
                        if (args.length < 3) {
                            sender.sendMessage(PT + "请输入 <玩家> <" + title + "> [时长]");
                            return;
                        }
                        var pn = getCmdPlayerByName(sender, args[1]);
                        if (pn == null) {
                            sender.sendMessage(PT + "玩家不存在");
                            return;
                        }
                        if (!invts.containsKey(args[2])) {
                            sender.sendMessage(PT + "库中没有此" + title);
                            return;
                        }
                        var val = args[2];
                        long time;
                        try {
                            time = args.length > 3 ? Integer.parseInt(args[3]) :
                                    (long) invts.getOrDefault(val, new HashMap<>()).getOrDefault("时效", 300);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(PT + "请输入正确的数字");
                            return;
                        }
                        if (addPlayerParticle(pn, val, time))
                            sender.sendMessage(PT + "已为 " + getPlayerName(pn) + " §r§7添加"
                                    + title + " §f" + val + " §r§7期限至§f " + (time < 1 ? "§a永久" :
                                    secondsToDateTime(secondsLeft(getPlayerParticles(pn)
                                            .getOrDefault(val,secondsToDateTime(0))) + time)));
                        else sender.sendMessage(PT + "添加失败");
                    }
                    case "set" -> {
                        if (args.length < 3) {
                            sender.sendMessage(PT + "请输入 <玩家> <" + title + "> [时长]");
                            return;
                        }
                        var pn = getCmdPlayerByName(sender, args[1]);
                        if (pn == null) {
                            sender.sendMessage(PT + "玩家不存在");
                            return;
                        }
                        var val = args[2];
                        long time;
                        try {
                            time = args.length > 3 ? Integer.parseInt(args[3]) :
                                    (long) invts.getOrDefault(val, new HashMap<>()).getOrDefault("时效", 300);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(PT + "请输入正确的数字");
                            return;
                        }
                        if (setPlayerParticle(pn, val, time)) sender.sendMessage(PT + "已为 " + getPlayerName(pn)
                                + " §r§7设置" + title + " §f" + val + " §r§7期限至§f " + (time < 1 ? "永久" : secondsToDateTime(time) ));
                        else sender.sendMessage(PT + "设置失败");
                    }
                    case "del" -> {
                        if (args.length < 2) {
                            sender.sendMessage(PT + "请输入 <玩家> <" + title + "> [时长]");
                            return;
                        }
                        var pn = getCmdPlayerByName(sender, args[1]);
                        if (pn == null) {
                            sender.sendMessage(PT + "玩家不存在");
                            return;
                        }
                        var val = args[2];
                        long time;
                        try {
                            time = args.length > 3 ? Integer.parseInt(args[3]) :
                                    (long) invts.getOrDefault(val, new HashMap<>()).getOrDefault("时效", 300);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(PT + "请输入正确的数字");
                            return;
                        }
                        if (delPlayerParticle(pn, val, time)) {
                            var ts = getPlayerParticles(pn);
                            var playerName = getPlayerName(pn);
                            if (!ts.containsKey(val)) sender.sendMessage(PT
                                    + "已删除 " + playerName + " §r§7的" + title + "§f " + val);
                            else sender.sendMessage(PT + "已减少 " + playerName + " §r§7的" + title
                                    + "§f " + val + " §r§7期限至§f " + ts.getOrDefault(val, secondsToDateTime(0)));
                        } else sender.sendMessage(PT + "删除失败");
                    }
                }
            });
        }
        return false;
    }
    private static final String helpTips = PT + getCmd() + """
                    
                    my 我的{%1}
                    shop {%1}商店
                    sml {%1}显示性能优化设置
                    create <{%1}> 在库中新建{%1}
                    add <玩家> <{%1}> [时长] 为玩家添加{%1}及时长(如果不写时长则寻找库中的,如果库中不存在则默认300s)
                    set <玩家> <{%1}> [时长] 为玩家设置{%1}的时长(如果不写时长则寻找库中的,如果库中不存在则默认300s)
                    del <玩家> <{%1}> 删除{%1}(如果时间<1则删除)
                    mgform 打开管理GUI
                    reload 重载插件配置
                    """.replace("{%1}",title);
}
