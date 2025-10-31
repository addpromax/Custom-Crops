/*
 * 作物收获追踪系统使用示例
 * 
 * 这个文件展示了如何在你的插件中使用 Custom-Crops 的收获追踪功能
 */

package example;

import net.momirealms.customcrops.api.BukkitCustomCropsPlugin;
import net.momirealms.customcrops.api.data.HarvestDataManager;
import net.momirealms.customcrops.api.data.PlayerHarvestData;
import net.momirealms.customcrops.api.event.CropBreakEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;

public class HarvestTrackingExample extends JavaPlugin implements Listener {

    private HarvestDataManager harvestDataManager;

    @Override
    public void onEnable() {
        // 获取 Custom-Crops 的收获数据管理器
        harvestDataManager = BukkitCustomCropsPlugin.getInstance().getHarvestDataManager();
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(this, this);
        
        getLogger().info("作物收获追踪示例插件已启用！");
    }

    // ========== 示例 1: 查询玩家收获数据 ==========
    
    public void example1_GetPlayerHarvestData(Player player) {
        UUID playerId = player.getUniqueId();
        
        // 获取玩家数据
        PlayerHarvestData data = harvestDataManager.getData(playerId);
        
        if (data != null) {
            // 获取总收获次数
            int totalHarvests = data.getTotalHarvests();
            player.sendMessage("你总共收获了 " + totalHarvests + " 次作物！");
            
            // 获取特定作物的收获次数
            int strawberryCount = data.getHarvestCount("strawberry");
            player.sendMessage("你收获了 " + strawberryCount + " 次草莓！");
            
            // 检查是否收获过某种作物
            boolean hasHarvestedCarrot = data.hasHarvested("carrot");
            if (hasHarvestedCarrot) {
                player.sendMessage("你已经解锁了胡萝卜！");
            }
        }
    }

    // ========== 示例 2: 显示收获排行榜 ==========
    
    public void example2_ShowTopCrops(Player player) {
        PlayerHarvestData data = harvestDataManager.getData(player.getUniqueId());
        
        if (data == null) {
            player.sendMessage("§c你还没有收获过任何作物！");
            return;
        }
        
        player.sendMessage("§6§l你的收获排行榜:");
        
        // 获取所有收获数据并排序
        data.getHarvestCounts().entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(5)  // 只显示前5名
            .forEach(entry -> {
                String cropName = getCropDisplayName(entry.getKey());
                int count = entry.getValue();
                player.sendMessage(String.format("§e%s: §f%d 次", cropName, count));
            });
    }

    // ========== 示例 3: 成就系统 ==========
    
    @EventHandler
    public void onCropHarvest(CropBreakEvent event) {
        if (!(event.entityBreaker() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.entityBreaker();
        UUID playerId = player.getUniqueId();
        String cropId = event.cropConfig().id();
        
        // 获取当前收获次数
        int cropCount = harvestDataManager.getHarvestCount(playerId, cropId);
        int totalCount = harvestDataManager.getTotalHarvests(playerId);
        
        // 检查作物特定成就
        if (cropCount == 10) {
            player.sendMessage("§6§l[成就] §e初级农夫：收获 " + getCropDisplayName(cropId) + " 10 次！");
            giveReward(player, "small");
        } else if (cropCount == 100) {
            player.sendMessage("§6§l[成就] §e高级农夫：收获 " + getCropDisplayName(cropId) + " 100 次！");
            giveReward(player, "medium");
        } else if (cropCount == 1000) {
            player.sendMessage("§6§l[成就] §e传奇农夫：收获 " + getCropDisplayName(cropId) + " 1000 次！");
            giveReward(player, "large");
        }
        
        // 检查总收获成就
        if (totalCount == 100) {
            player.sendMessage("§6§l[成就] §e勤劳的农民：总共收获 100 次作物！");
            giveReward(player, "medium");
        }
    }

    // ========== 示例 4: 图鉴系统集成 ==========
    
    public java.util.List<String> getCropEncyclopediaLore(Player player, String cropId) {
        java.util.List<String> lore = new java.util.ArrayList<>();
        
        UUID playerId = player.getUniqueId();
        int harvestCount = harvestDataManager.getHarvestCount(playerId, cropId);
        
        if (harvestCount > 0) {
            lore.add("§a✓ 已解锁");
            lore.add("§7收获次数: §f" + harvestCount);
            
            // 根据收获次数显示不同等级
            String rank = getRankByCount(harvestCount);
            lore.add("§7等级: " + rank);
            
            lore.add("");
            lore.add("§7首次收获: §f已解锁");
        } else {
            lore.add("§7✗ 未解锁");
            lore.add("§8收获一次以解锁详情");
        }
        
        return lore;
    }

    // ========== 示例 5: 统计命令 ==========
    
    public void showPlayerStats(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerHarvestData data = harvestDataManager.getData(playerId);
        
        if (data == null || data.getTotalHarvests() == 0) {
            player.sendMessage("§c你还没有收获过任何作物！");
            return;
        }
        
        player.sendMessage("§6§l=== §e你的农场统计 §6§l===");
        player.sendMessage("§7总收获次数: §f" + data.getTotalHarvests());
        player.sendMessage("§7已解锁作物: §f" + data.getHarvestCounts().size() + " 种");
        
        // 计算最喜欢的作物
        Map.Entry<String, Integer> favorite = data.getHarvestCounts().entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElse(null);
        
        if (favorite != null) {
            player.sendMessage("§7最喜欢的作物: §f" + getCropDisplayName(favorite.getKey()) + 
                             " §7(" + favorite.getValue() + " 次)");
        }
        
        player.sendMessage("§7上次更新: §f" + formatTime(data.getLastUpdated()));
    }

    // ========== 示例 6: 重置数据 ==========
    
    public void resetPlayerData(Player player) {
        PlayerHarvestData data = harvestDataManager.getOrCreateData(player.getUniqueId());
        data.clear();
        harvestDataManager.savePlayerData(player.getUniqueId());
        player.sendMessage("§a你的收获数据已重置！");
    }

    // ========== 示例 7: 导出数据 ==========
    
    public void exportPlayerData(Player player) {
        PlayerHarvestData data = harvestDataManager.getData(player.getUniqueId());
        
        if (data == null) {
            player.sendMessage("§c没有数据可导出！");
            return;
        }
        
        player.sendMessage("§6§l数据导出:");
        player.sendMessage("§7玩家: §f" + player.getName());
        player.sendMessage("§7UUID: §f" + player.getUniqueId());
        player.sendMessage("§7总收获: §f" + data.getTotalHarvests());
        player.sendMessage("§7详细数据:");
        
        data.getHarvestCounts().forEach((cropId, count) -> 
            player.sendMessage("  §e" + cropId + ": §f" + count));
    }

    // ========== 辅助方法 ==========
    
    private String getCropDisplayName(String cropId) {
        // 这里应该从配置或语言文件中获取显示名称
        return cropId;
    }
    
    private String getRankByCount(int count) {
        if (count >= 1000) return "§6★★★ 传奇";
        if (count >= 500) return "§5★★ 大师";
        if (count >= 100) return "§b★ 专家";
        if (count >= 50) return "§a熟练";
        if (count >= 10) return "§7初级";
        return "§8新手";
    }
    
    private void giveReward(Player player, String tier) {
        // 实现奖励逻辑
        player.sendMessage("§a你获得了奖励！");
    }
    
    private String formatTime(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) return days + " 天前";
        if (hours > 0) return hours + " 小时前";
        if (minutes > 0) return minutes + " 分钟前";
        return seconds + " 秒前";
    }
}

